package fr.onecraft.halloween.core.objects;

import java.util.UUID;

public class LeaderboardUser {
    private int id;
    private String name;
    private UUID uuid;
    int foundCount;
    private int placement;
    private long wonAt;

    public LeaderboardUser(int id, UUID uuid, String name, int placement, int foundCount, long winAt) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
        this.placement = placement;
        this.wonAt = winAt;
        this.foundCount = foundCount;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public int getFoundCount() {
        return this.foundCount;
    }

    public int getPlacement() {
        return placement;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public long getWonAt() {
        return wonAt;
    }

    public void setWonAt(long wonAt) {
        this.wonAt = wonAt;
    }
}