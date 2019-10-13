package fr.onecraft.halloween.core.objects;

import fr.onecraft.halloween.core.helpers.Database;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandyItem {
    private int id;
    private String texture;

    public static final String CANDY_NAME = "§eBonbon d'Halloween";
    public static final String ID_PREFIX = "§6ID §e";

    private static final Map<Integer, CandyItem> CANDIES_MODELS = new HashMap<>();

    public CandyItem(int id, String texture) {
        this.id = id;
        this.texture = texture;
    }

    public int getId() {
        return this.id;
    }

    public String getTexture() {
        return texture;
    }

    public static void loadTextures() {
        CANDIES_MODELS.clear();
        CANDIES_MODELS.putAll(Database.getCandies());
    }

    public static CandyItem fromItem(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();
        int id = Integer.parseInt(lore.get(lore.size() - 1).substring(ID_PREFIX.length()));
        return CANDIES_MODELS.get(id);
    }

    public static CandyItem fromId(int id) {
        return CANDIES_MODELS.get(id);
    }
}
