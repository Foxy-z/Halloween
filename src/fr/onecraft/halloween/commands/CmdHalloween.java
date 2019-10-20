package fr.onecraft.halloween.commands;

import fr.onecraft.halloween.Halloween;
import fr.onecraft.halloween.core.helpers.Database;
import fr.onecraft.halloween.core.objects.Candy;
import fr.onecraft.halloween.core.objects.CandyItem;
import fr.onecraft.halloween.core.objects.User;
import fr.onecraft.halloween.utils.SkullUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class CmdHalloween implements CommandExecutor {
    private Halloween plugin;
    private String pluginName;

    public CmdHalloween(Halloween plugin) {
        this.plugin = plugin;
        this.pluginName = plugin.getName().toLowerCase();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Halloween.ERROR + "Cette commande n'est disponible que pour les joueurs.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String action = args[0].toLowerCase();
        if (!sender.hasPermission(pluginName + "." + action)) {
            player.sendMessage(Halloween.ERROR + "Tu n'as pas la permission.");
        } else if (action.equals("info")) {
            info(player, args);
        } else if (action.equals("remove")) {
            remove(player);
        } else if (action.equals("placing")) {
            placing(player);
        } else if (action.equals("clearall")) {
            clearAll(player);
        } else if (action.equals("dbreload")) {
            dbreload(player);
        } else if (args.length < 2) {
            showHelp(player);
        } else {
            if (action.equals("place")) {
                place(player, args);
            } else {
                showHelp(player);
            }
        }
        return true;
    }

    private void clearAll(Player player) {
        Candy.clearAll(plugin, player);
        player.sendMessage(Halloween.PREFIX + "Toutes friandises de ce serveur ont été retirées !");
    }

    private void dbreload(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Halloween.INSTANCE, () -> {
            CandyItem.loadTextures();
            Candy.loadLocations();

            player.sendMessage(Halloween.PREFIX + "Les données ont été rechargées !");
        });
    }

    private void info(Player player, String[] args) {
        // TODO tester le .info.other
        if (args.length == 1 || !player.hasPermission(pluginName + ".info.other")) {
            User target = User.fromUuid(player.getUniqueId());
            infoOf(player, target);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);

                User targetUser = User.fromUuid(targetPlayer.getUniqueId());
                infoOf(player, targetUser);
            });
        }
    }

    private void infoOf(Player player, User target) {
        String suffix = player.getUniqueId() != target.getUuid() ? " pour §a" + target.getName() + "§7 :" : "";
        int p = target.getPlacement();
        String placement = p == -1 ? "non classé" : p == 1 ? "§61er" : p < 4 ? "§e" + p + "ème" : "§f" + p + "ème";

        player.sendMessage(Halloween.PREFIX + "Information de l'événement Halloween" + suffix
                + "\n§7 - Placement : " + placement
                + "\n§7 - Bonbons trouvés : §f" + target.getFoundCandies().size()
        );
    }

    private void placing(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Halloween.INSTANCE, () -> {
            List<User> winners = Database.getWinners(15);
            if (winners == null || winners.size() == 0) {
                player.sendMessage(Halloween.PREFIX + "Il n'y a aucun gagnant pour le moment...");
                return;
            }

            player.sendMessage(Halloween.PREFIX + "Classement des joueurs : ");

            String color = "§6";
            for (int i = 0; i < winners.size(); i++) {
                User user = winners.get(i);
                String time = new SimpleDateFormat("MM/dd à HH:mm")
                        .format(new Date(user.getWinAt()));

                player.sendMessage("§7 - " + color + user.getName() + "§7, le " + time);

                // switch colors
                if (i == 0) {
                    color = "§e";
                } else if (i == 2) {
                    color = "§f";
                }
            }
        });
    }

    private void place(Player player, String[] args) {
        // get candy id
        String candyName = args[1];
        int candyId;
        try {
            candyId = Integer.parseInt(candyName);
        } catch (NumberFormatException e) {
            player.sendMessage(Halloween.ERROR + "Ce numéro de bonbon est invalide.");
            return;
        }

        // get candy object from id
        CandyItem candy = CandyItem.fromId(candyId);
        if (candy == null) {
            player.sendMessage(Halloween.ERROR + "Ce type de bonbon n'existe pas.");
            return;
        }

        // get target block
        Block playerTarget = player.getTargetBlock((Set<Material>) null, 10);
        if (playerTarget == null) {
            player.sendMessage(Halloween.ERROR + "Vous devez regarder un bloc !");
            return;
        }

        // get target relative
        Block target = playerTarget.getRelative(BlockFace.UP);
        if (!target.getType().equals(Material.AIR)) {
            player.sendMessage(Halloween.ERROR + "Le bloc cible doit être vide !");
            return;
        }

        // add candy / apply texture
        SkullUtils.applyTextureToBlock(player, candy.getTexture(), target);

        target.setData((byte) 1, true);
        Candy.add(plugin, player, target.getLocation(), candy);

        player.sendMessage(Halloween.PREFIX + "La friandise a été placée !");
    }

    private void remove(Player player) {
        // get target candy (block)
        Block target = player.getTargetBlock((Set<Material>) null, 10);
        if (target == null) {
            player.sendMessage(Halloween.ERROR + "Vous devez regarder un bloc !");
            return;
        }

        // remove candy if there is one
        if (!Candy.remove(plugin, player, target.getLocation())) {
            player.sendMessage(Halloween.ERROR + "Il n'y a pas de bonbon ici...");
            return;
        }

        target.setType(Material.AIR);
        player.sendMessage(Halloween.PREFIX + "La friandise a été retirée !");
    }

    private void showHelp(Player player) {
        List<String> cmds = new ArrayList<>();

        if (player.hasPermission(pluginName + ".place"))
            cmds.add("§b/" + pluginName + " place <id> §7place un bonbon");
        if (player.hasPermission(pluginName + ".remove"))
            cmds.add("§b/" + pluginName + " remove §7retire un bonbon");
        if (player.hasPermission(pluginName + ".placing"))
            cmds.add("§b/" + pluginName + " placing §7donne le classement");
        if (player.hasPermission(pluginName + ".info"))
            cmds.add("§b/" + pluginName + " info [player] §7avancées d'un joueur");
        if (player.hasPermission(pluginName + ".clearall"))
            cmds.add("§b/" + pluginName + " clearall §7supprime les bonbons du serveur");
        if (player.hasPermission(pluginName + ".dbreload"))
            cmds.add("§b/" + pluginName + " dbreload §7met à jour les données");

        if (cmds.isEmpty()) {
            player.sendMessage(Halloween.ERROR + "Tu n'as pas la permission.");
        } else {
            player.sendMessage(Halloween.PREFIX + "Gère l'événement Halloween");
            cmds.forEach(player::sendMessage);
        }
    }
}
