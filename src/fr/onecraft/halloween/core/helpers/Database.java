package fr.onecraft.halloween.core.helpers;

import fr.onecraft.halloween.core.database.DatabaseManager;
import fr.onecraft.halloween.core.database.enums.SQLCondition;
import fr.onecraft.halloween.core.database.enums.SQLOrder;
import fr.onecraft.halloween.core.database.exceptions.DatabaseConnectionException;
import fr.onecraft.halloween.core.database.exceptions.DatabaseQueryException;
import fr.onecraft.halloween.core.database.objects.Query;
import fr.onecraft.halloween.core.database.objects.SubQuery;
import fr.onecraft.halloween.core.objects.Candy;
import fr.onecraft.halloween.core.objects.CandyItem;
import fr.onecraft.halloween.core.objects.LeaderboardUser;
import fr.onecraft.halloween.core.objects.PlayerUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

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
    public static PlayerUser getUser(UUID uuid) {
        try {
            // get user from database
            ResultSet userResult = new Query(DatabaseManager.getConnection())
                    .from(USERS)
                    .select()
                    .where("uuid", uuid.toString())
                    .join(WINNERS, "id", "user_id")
                    .execute();

            // if user not registered in database create it
            if (!userResult.next()) {
                // get target name
                String name = "?";
                OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
                if (target.isOnline() || !target.getName().isEmpty()) {
                    name = target.getName();
                }

                // insert into database
                int row = new Query(DatabaseManager.getConnection())
                        .from(USERS)
                        .insert("uuid", uuid.toString())
                        .insert("name", name)
                        .getRow();

                return new PlayerUser(
                        row,
                        uuid,
                        name,
                        -1,
                        0,
                        -1,
                        new HashSet<>()
                );
            }

            int userId = userResult.getInt("id");

            // get found candies
            ResultSet candyResult = new Query(DatabaseManager.getConnection())
                    .from(FOUND)
                    .select("candy_id")
                    .where("user_id", userId)
                    .execute();

            // add found candies
            Set<Integer> foundCandies = new HashSet<>();
            while (candyResult.next()) {
                foundCandies.add(candyResult.getInt("candy_id"));
            }

            PlayerUser user = new PlayerUser(
                    userId,
                    uuid,
                    userResult.getString("name"),
                    userResult.getInt(WINNERS + ".id"),
                    foundCandies.size(),
                    userResult.getLong(WINNERS + ".won_at"),
                    foundCandies
            );

            updateName(user);
            return user;
        } catch (SQLException | DatabaseQueryException | DatabaseConnectionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void updateName(PlayerUser user) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(user.getUuid());
        // cancel if name is up to date
        if (player.getName().equals(user.getName())) return;
        user.setName(player.getName());

        try {
            // update to database
            new Query(DatabaseManager.getConnection())
                    .from(USERS)
                    .update("name", player.getName())
                    .where("uuid", user.getUuid().toString())
                    .execute();

        } catch (DatabaseConnectionException | SQLException | DatabaseQueryException e) {
            e.printStackTrace();
        }
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
    public static List<LeaderboardUser> getWinners(int amount) {
        try {
            ResultSet resultSet = new Query(DatabaseManager.getConnection())
                    .from(WINNERS)
                    .select()
                    .join(USERS, "user_id", "id")
                    .limit(amount)
                    .execute();

            // if there is no winner
            if (!resultSet.next()) {
                return null;
            }

            resultSet.previous();
            List<LeaderboardUser> users = new ArrayList<>();

            // add each winner
            while (resultSet.next()) {
                users.add(new LeaderboardUser(
                        resultSet.getInt("user_id"),
                        UUID.fromString(resultSet.getString("uuid")),
                        resultSet.getString("name"),
                        resultSet.getInt(WINNERS + ".id"),
                        -1, // TODO MAX
                        resultSet.getLong(WINNERS + ".won_at")
                ));
            }
            return users;
        } catch (SQLException | DatabaseQueryException | DatabaseConnectionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<LeaderboardUser> getProgressRanking(int amount) {
        try {
            // TODO UPDATE
            Query query = new Query(DatabaseManager.getConnection());

            query = query.from(FOUND)
                    .select("name", "user_id", "uuid", "count(*)")
                    .group("user_id")
                    .having(
                            SQLCondition.NON_EQUALS,
                            "count(*)",
                            new SubQuery()
                                    .from(LOCATIONS)
                                    .select("count(*)")
                    )
                    .join(USERS, "user_id", "id")
                    .order(SQLOrder.DESC, "count(*)")
                    .limit(amount);

            ResultSet resultSet = query.execute();

            // if there is no player
            if (!resultSet.next()) {
                return null;
            }

            resultSet.previous();
            List<LeaderboardUser> users = new ArrayList<>();

            // add each winner
            while (resultSet.next()) {
                users.add(new LeaderboardUser(
                        resultSet.getInt("user_id"),
                        UUID.fromString(resultSet.getString("uuid")),
                        resultSet.getString("name"),
                        -1,
                        resultSet.getInt("count(*)"), // TODO alias found
                        -1
                ));
            }
            return users;
        } catch (SQLException | DatabaseQueryException | DatabaseConnectionException e) {
            e.printStackTrace();
        }

        return null;
    }
}
