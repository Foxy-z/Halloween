package fr.onecraft.halloween.core.helpers;

import fr.onecraft.halloween.core.database.DatabaseManager;
import fr.onecraft.halloween.core.database.exceptions.DatabaseConnectionException;
import fr.onecraft.halloween.core.database.exceptions.DatabaseQueryException;
import fr.onecraft.halloween.core.database.objects.Query;
import fr.onecraft.halloween.core.objects.Candy;
import fr.onecraft.halloween.core.objects.CandyItem;
import fr.onecraft.halloween.core.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Database {
    private final static String USERS = "halloween_users";
    private final static String FOUND = "halloween_found";
    private final static String LOCATIONS = "halloween_locations";
    private final static String CANDIES = "halloween_candies";
    private final static String WINNERS = "halloween_winners";


    /**
     * Get a user data
     *
     * @param uuid UUID of the player
     * @return User object with player data
     */
    public static User getUser(UUID uuid) {
        try {
            ResultSet resultSet = new Query(DatabaseManager.getConnection())
                    .from(USERS)
                    .select()
                    .where("uuid", uuid.toString())
                    .join(FOUND, "id", "user_id")
                    .join(WINNERS, "id", "user_id")
                    .execute();

            // if user not registered in database create it
            if (!resultSet.next()) {
                int row = new Query(DatabaseManager.getConnection())
                        .from(USERS)
                        .insert("uuid", uuid.toString())
                        .getRow();
                return new User(row, uuid, -1, -1, new HashSet<>());
            }

            User user = new User(
                    resultSet.getInt("id"),
                    uuid,
                    resultSet.getInt(WINNERS + ".id"),
                    resultSet.getLong(WINNERS + ".won_at"),
                    new HashSet<>()
            );

            resultSet.previous();
            // add found candies
            while (resultSet.next()) {
                if (resultSet.getInt("candy_id") == 0) continue;
                user.getFoundCandies().add(resultSet.getInt("candy_id"));
            }
            return user;
        } catch (SQLException | DatabaseQueryException | DatabaseConnectionException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get location of each candy to find
     *
     * @return Map
     */
    public static Map<Integer, Candy> getLocations() {
        Map<Integer, Candy> result = new HashMap<>();
        try {
            ResultSet resultSet = new Query(DatabaseManager.getConnection())
                    .from(LOCATIONS)
                    .select()
                    .execute();

            while (resultSet.next()) {
                result.put(
                        resultSet.getInt("id"),
                        new Candy(
                                resultSet.getInt("id"),
                                CandyItem.fromId(resultSet.getInt("candy_id")),
                                resultSet.getString("server"),
                                new Location(
                                        Bukkit.getWorld(resultSet.getString("world")),
                                        resultSet.getInt("x"),
                                        resultSet.getInt("y"),
                                        resultSet.getInt("z")
                                )
                        )
                );
            }

        } catch (DatabaseConnectionException | SQLException | DatabaseQueryException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Add a candy to find list
     *
     * @param location Location of the candy
     * @param candyId  int id of the candy
     * @return int its database id
     */
    public static int addCandyLocation(Location location, int candyId) {
        try {
            return new Query(DatabaseManager.getConnection())
                    .from(LOCATIONS)
                    .insert("candy_id", candyId)
                    .insert("server", Bukkit.getServerName())
                    .insert("world", location.getWorld().getName())
                    .insert("x", (int) location.getX())
                    .insert("y", (int) location.getY())
                    .insert("z", (int) location.getZ())
                    .getRow();

        } catch (SQLException | DatabaseQueryException | DatabaseConnectionException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void removeCandyLocation(int id) {
        try {
            new Query(DatabaseManager.getConnection())
                    .from(LOCATIONS)
                    .where("id", id)
                    .delete();

        } catch (SQLException | DatabaseQueryException | DatabaseConnectionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load every candy model from database
     *
     * @return Map
     */
    public static Map<Integer, CandyItem> getCandies() {
        Map<Integer, CandyItem> result = new HashMap<>();
        try {
            ResultSet resultSet = new Query(DatabaseManager.getConnection())
                    .from(CANDIES)
                    .select()
                    .execute();

            while (resultSet.next()) {
                result.put(
                        resultSet.getInt("id"),
                        new CandyItem(
                                resultSet.getInt("id"),
                                resultSet.getString("texture")
                        )
                );
            }

            resultSet.close();
        } catch (DatabaseConnectionException | SQLException | DatabaseQueryException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Remove every heads of a server from database
     *
     * @param serverName String
     */
    public static void removeAll(String serverName) {
        try {
            new Query(DatabaseManager.getConnection())
                    .from(LOCATIONS)
                    .where("server", serverName)
                    .delete();

        } catch (SQLException | DatabaseConnectionException | DatabaseQueryException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a candy to find list of a user from database
     *
     * @param userId  int
     * @param candyId int
     */
    public static void addFoundCandy(int userId, int candyId) {
        try {
            new Query(DatabaseManager.getConnection())
                    .from(FOUND)
                    .insert("user_id", userId)
                    .insert("candy_id", candyId)
                    .insert("found_at", System.currentTimeMillis())
                    .execute();

        } catch (SQLException | DatabaseConnectionException | DatabaseQueryException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a user to winner list
     *
     * @param userId int
     * @return the placement of the player
     */
    public static int addWinner(int userId) {
        try {
            return new Query(DatabaseManager.getConnection())
                    .from(WINNERS)
                    .insert("user_id", userId)
                    .insert("won_at", System.currentTimeMillis())
                    .getRow();

        } catch (SQLException | DatabaseConnectionException | DatabaseQueryException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Get winners ordered by win time
     *
     * @param amount int of player to returns
     * @return List
     */
    public static List<User> getWinners(int amount) {
        try {
            ResultSet resultSet = new Query(DatabaseManager.getConnection())
                    .from(WINNERS)
                    .select()
                    .join(USERS, "user_id", "id")
                    .limit(amount)
                    .execute();

            // rif there is no winner
            if (!resultSet.next()) {
                return null;
            }

            resultSet.previous();
            List<User> users = new ArrayList<>();
            Set<Integer> candies = getLocations().keySet();

            // add each winner
            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getInt("user_id"),
                        UUID.fromString(resultSet.getString("uuid")),
                        resultSet.getInt(WINNERS + ".id"),
                        resultSet.getLong(WINNERS + ".won_at"),
                        candies
                ));
            }
            return users;
        } catch (SQLException | DatabaseQueryException | DatabaseConnectionException e) {
            e.printStackTrace();
        }

        return null;
    }
}
