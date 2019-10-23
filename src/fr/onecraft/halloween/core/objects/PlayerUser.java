package fr.onecraft.halloween.core.objects;

import fr.onecraft.halloween.core.helpers.Database;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerUser extends LeaderboardUser {

    private Set<Integer> foundCandies;

    private static Map<UUID, PlayerUser> USERS = new HashMap<>();

    public PlayerUser(int id, UUID uuid, String name, int placement, int foundCount, long winAt, Set<Integer> foundCandies) {
        super(id, uuid, name, placement, foundCount, winAt);
        this.foundCandies = foundCandies;
    }

    public static PlayerUser fromUuid(UUID uuid) {
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
