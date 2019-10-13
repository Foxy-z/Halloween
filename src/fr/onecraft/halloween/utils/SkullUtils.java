package fr.onecraft.halloween.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.TileEntitySkull;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SkullUtils {

    public static void applyTextureToBlock(Player player, String skinUrl, Block block) {
        block.setType(Material.SKULL);

        Skull skullData = (Skull) block.getState();
        skullData.setSkullType(SkullType.PLAYER);
        skullData.setRotation(getCardinalDirection(player));
        skullData.update(true);

        TileEntitySkull skullTile = (TileEntitySkull) ((CraftWorld) block.getWorld()).getHandle()
                .getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));

        skullTile.setGameProfile(profileWithTexture(skinUrl));
        block.getState().update(true);
    }

    private static GameProfile profileWithTexture(String texture) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        Property skin = new Property("textures", texture);
        profile.getProperties().put("textures", skin);
        return profile;
    }

    public static BlockFace getCardinalDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 90.0F) % 360.0F;
        if (rotation < 0.0D) {
            rotation += 360.0D;
        }

        if ((0.0D <= rotation) && (rotation < 22.5D)) {
            return BlockFace.EAST;
        }
        if ((22.5D <= rotation) && (rotation < 67.5D)) {
            return BlockFace.SOUTH_EAST;
        }
        if ((67.5D <= rotation) && (rotation < 112.5D)) {
            return BlockFace.SOUTH;
        }
        if ((112.5D <= rotation) && (rotation < 157.5D)) {
            return BlockFace.SOUTH_WEST;
        }
        if ((157.5D <= rotation) && (rotation < 202.5D)) {
            return BlockFace.WEST;
        }
        if ((202.5D <= rotation) && (rotation < 247.5D)) {
            return BlockFace.NORTH_WEST;
        }
        if ((247.5D <= rotation) && (rotation < 292.5D)) {
            return BlockFace.NORTH;
        }
        if ((292.5D <= rotation) && (rotation < 337.5D)) {
            return BlockFace.NORTH_EAST;
        }
        if ((337.5D <= rotation) && (rotation < 360.0D)) {
            return BlockFace.EAST;
        }
        return BlockFace.EAST;
    }
}
