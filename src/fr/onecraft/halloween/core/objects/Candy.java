package fr.onecraft.halloween.core.objects;

import fr.onecraft.halloween.Halloween;
import fr.onecraft.halloween.core.helpers.Database;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public String getServerName() {
        return this.serverName;
    }

    public static void add(Location location, CandyItem type) {
        Bukkit.getScheduler().runTaskAsynchronously(Halloween.INSTANCE, () -> {
            int id = Database.addCandyLocation(location, type.getId());
            if (id == -1) return;
            Candy candy = new Candy(
                    id,
                    type,
                    Bukkit.getServerName(),
                    location
            );

            LOCATIONS.put(id, candy);
        });
    }

    public static boolean remove(Location location) {
        for (Candy candy : LOCATIONS.values()) {
            // skip if candy is on different server
            if (!candy.getServerName().equals(Bukkit.getServerName())) continue;

            Location cLoc = candy.getLocation();
            if (cLoc.getBlockX() == location.getBlockX()
                    && cLoc.getBlockY() == location.getBlockY()
                    && cLoc.getBlockZ() == location.getBlockZ()) {
                LOCATIONS.remove(candy.getId());
                Database.removeCandyLocation(candy.getId());
                return true;
            }
        }

        return false;
    }

    public Location getLocation() {
        return location;
    }

    public int getId() {
        return this.id;
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

    public static void loadLocations() {
        LOCATIONS.clear();
        LOCATIONS.putAll(Database.getLocations());
    }

    public static void clearAll() {
        // remove each block
        for (Candy candy : LOCATIONS.values()) {
            // skip if candy is on different server
            if (!candy.getServerName().equals(Bukkit.getServerName())) continue;

            candy.getLocation().getBlock().setType(Material.AIR);
        }

        LOCATIONS.clear();
        Bukkit.getScheduler().runTaskAsynchronously(Halloween.INSTANCE, () -> Database.removeAll(Bukkit.getServerName()));
    }
}
