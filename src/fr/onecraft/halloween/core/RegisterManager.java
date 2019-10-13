package fr.onecraft.halloween.core;

import fr.onecraft.halloween.Halloween;
import fr.onecraft.halloween.commands.CmdHalloween;
import fr.onecraft.halloween.listeners.PlayerListeners;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class RegisterManager {
    private Halloween plugin;

    public RegisterManager(Halloween plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // listeners
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new PlayerListeners(plugin), plugin);

        // commands
        plugin.getCommand("halloween").setExecutor(new CmdHalloween(plugin));
    }
}
