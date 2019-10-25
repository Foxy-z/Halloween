package fr.onecraft.halloween.core.objects;

import fr.onecraft.halloween.Halloween;
import fr.onecraft.halloween.core.helpers.Database;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Candy {
    private int id;
    private CandyItem item;
    private String serverName;
    private Location location;

    private static final Map<Integer, Candy> LOCATIONS = new HashMap<>();

    public Candy(int id, CandyItem item, String serverName, Location location) {
        this.id = id;
        this.item = item;
        this.serverName = serverName;
        this.location = location;
    }

    public int getId() {
        return this.id;
    }

    public Location getLocation() {
        return location;
    }

    public CandyItem getItem() {
        return this.item;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void spawnAnimation() {
        Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(20);
        fwm.addEffect(
                FireworkEffect.builder()
                        .withColor(Color.WHITE)
                        .withFlicker()
                        .withTrail()
                        .withFade(Color.ORANGE)
                        .withFade(Color.BLACK)
                        .build()
        );

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    public static Candy fromId(int candyId) {
        return LOCATIONS.get(candyId);
    }

    public static Candy fromLocation(Location location) {
        for (Candy candy : LOCATIONS.values()) {
            // skip if candy is on different server
            if (!candy.getServerName().equals(Bukkit.getServerName())) continue;

            Location cLoc = candy.getLocation();
            if (cLoc.getBlockX() == location.getBlockX()
                    && cLoc.getBlockY() == location.getBlockY()
                    && cLoc.getBlockZ() == location.getBlockZ()) {
                return candy;
            }
        }

        return null;
    }

    public static Collection<Candy> getAll() {
        return LOCATIONS.values();
    }

    public static Map<String, Integer> getTotalPerServer() {
        Map<String, Integer> result = new HashMap<>();
        for (Candy candy : LOCATIONS.values()) {
            String server = candy.getServerName();
            if (!result.containsKey(server)) {
                result.put(server, 1);
            } else {
                result.put(server, result.get(server) + 1);
            }
        }

        return result;
    }

    public static Map<String, Integer> getFoundPerServer(Set<Integer> found) {
        Map<String, Integer> result = new HashMap<>();
        for (Candy candy : LOCATIONS.values()) {
            String server = candy.getServerName();
            if (!result.containsKey(server)) {
                result.put(server, 0);
            }

            if (!found.contains(candy.getId())) continue;
            result.put(server, result.get(server) + 1);
        }

        return result;
    }

    public static void add(Halloween plugin, Player player, Location location, CandyItem type) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int id = Database.addCandyLocation(location, type.getId());
            if (id == -1) return;
            Candy candy = new Candy(
                    id,
                    type,
                    Bukkit.getServerName(),
                    location
            );

            LOCATIONS.put(id, candy);

            plugin.logToFile("PLACE", "Type " + type.getId() + " head placed by "
                    + player.getName() + " (" + player.getUniqueId() + ") at "
                    + "world: " + location.getWorld().getName()
                    + "x: " + location.getX()
                    + "y: " + location.getY()
                    + "z: " + location.getZ()
            );
        });
    }

    public static boolean remove(Halloween plugin, Player player, Location location) {
        for (Candy candy : LOCATIONS.values()) {
            // skip if candy is on different server
            if (!candy.getServerName().equals(Bukkit.getServerName())) continue;

            Location cLoc = candy.getLocation();
            if (cLoc.getBlockX() == location.getBlockX()
                    && cLoc.getBlockY() == location.getBlockY()
                    && cLoc.getBlockZ() == location.getBlockZ()) {
                LOCATIONS.remove(candy.getId());
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Database.removeCandyLocation(candy.getId()));
                plugin.logToFile("REMOVE", "Type " + candy.getItem().getId() + " head (id: " + candy.getId() + ") removed from "
                        + "world: " + location.getWorld().getName()
                        + "x: " + location.getX()
                        + "y: " + location.getY()
                        + "z: " + location.getZ()
                        + " by " + player.getName() + " (" + player.getUniqueId() + ")"
                );
                return true;
            }
        }

        return false;
    }

    public static void loadLocations() {
        LOCATIONS.clear();
        LOCATIONS.putAll(Database.getLocations());
    }

    public static void clearAll(Halloween plugin, Player player) {
        // remove each block
        for (Candy candy : LOCATIONS.values()) {
            // skip if candy is on different server
            if (!candy.getServerName().equals(Bukkit.getServerName())) continue;

            candy.getLocation().getBlock().setType(Material.AIR);
        }

        LOCATIONS.clear();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Database.removeAll(Bukkit.getServerName()));

        plugin.logToFile("REMOVE_ALL", player.getName() + " (" + player.getUniqueId() + ") " +
                "has removed every heads from this server.");
    }
}
