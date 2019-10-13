package fr.onecraft.halloween.listeners;

import fr.onecraft.halloween.Halloween;
import fr.onecraft.halloween.core.helpers.Database;
import fr.onecraft.halloween.core.objects.Candy;
import fr.onecraft.halloween.core.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListeners implements Listener {
    private JavaPlugin plugin;

    public PlayerListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)

    public void on(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();
        User user = User.fromUuid(player.getUniqueId());
        if (user == null) return;

        Block clicked = event.getClickedBlock();
        if (!clicked.getType().equals(Material.SKULL)) return;

        Candy candy = Candy.fromLocation(clicked.getLocation());
        if (candy == null) return;

        // check if player has already found this candy
        if (user.hasFound(candy.getId())) {
            player.sendMessage(Halloween.PREFIX + "Vous avez déjà trouvé cette friandise !");
            return;
        }

        // save that user has found this candy and tell him / player animation
        player.sendMessage(Halloween.PREFIX + "Bouh ! Vous avez trouvé une nouvelle friandise !");
        candy.spawnAnimation();
        user.found(candy.getId());

        Bukkit.getScheduler().runTaskAsynchronously(Halloween.INSTANCE, () -> {
            // add candy to database
            Database.addFoundCandy(user.getId(), candy.getId());

            // check if player has found all candies
            if (user.hasFoundAll()) {
                Bukkit.getScheduler().runTaskAsynchronously(Halloween.INSTANCE, () -> {
                    // get placement
                    int placement = Database.addWinner(user.getId());
                    user.setPlacement(placement);
                    user.setWinAt(System.currentTimeMillis());
                    player.sendMessage(Halloween.PREFIX + "Mais... Vous avez trouvé toutes les friandises, félicitations ! " +
                            "Vous avez arrivé §a" + placement + (placement == 1 ? "er" : "ème") + " §7de l'événement !");
                });
            } else {
                int localRemaining = user.getLocalRemaining();
                String localRemainingStr = localRemaining != 0
                        ? "§a" + localRemaining + " §7friandises à trouver sur ce serveur"
                        : "";

                int globalRemaining = user.getGlobalRemaining();
                player.sendMessage(Halloween.PREFIX + "Il vous reste "
                        + localRemainingStr
                        + (localRemaining > 0 ? " et " : "") + "§a"
                        + globalRemaining
                        + (localRemaining == 0 ? " §7friandise à trouver" : "")
                        + " §7sur le network !");
            }
        });
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> User.loadUser(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        User.removeUserCache(event.getPlayer().getUniqueId());
    }
}
