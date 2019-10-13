package fr.onecraft.halloween.core.objects;

import fr.onecraft.halloween.core.helpers.Database;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class User {
    private int id;
    private String name;
    private UUID uuid;
    private int placement;
    private long winAt;
    private Set<Integer> foundCandies;

    private static Map<UUID, User> USERS = new HashMap<>();

    public User(int id, UUID uuid, int placement, long winAt, Set<Integer> foundCandies) {
        this.id = id;
        this.name = Bukkit.getOfflinePlayer(uuid).getName();
        this.uuid = uuid;
        this.placement = placement;
        this.winAt = winAt;
        this.foundCandies = foundCandies;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public static User fromUuid(UUID uuid) {
        return USERS.get(uuid);
    }

    public Set<Integer> getFoundCandies() {
        return foundCandies;
    }

    public void found(int candy) {
        foundCandies.add(candy);
    }

    public boolean hasFoundAll() {
        return getGlobalRemaining() == 0;
    }

    public int getGlobalRemaining() {
        return Candy.getAll().size() - foundCandies.size();
    }

    public int getLocalRemaining() {
        return (int) Candy.getAll().stream()
                .filter(candy -> candy.getServerName().equals(Bukkit.getServerName()))
                .filter(candy -> !foundCandies.contains(candy.getId()))
                .count();
    }

    public int getPlacement() {
        return placement;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public long getWinAt() {
        return winAt;
    }

    public void setWinAt(long winAt) {
        this.winAt = winAt;
    }

    public boolean hasFound(int candy) {
        return foundCandies.contains(candy);
    }

    public static void loadUser(UUID uuid) {
        USERS.put(uuid, Database.getUser(uuid));
    }

    public static void removeUserCache(UUID uuid) {
        USERS.remove(uuid);
    }
}
