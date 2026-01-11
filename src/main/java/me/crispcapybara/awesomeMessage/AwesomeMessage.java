package me.crispcapybara.awesomeMessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class AwesomeMessage extends JavaPlugin implements Listener{

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AwesomeMessages enabled.");
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        getLogger().info("AwesomeMessages disabled.");
        // Plugin shutdown logic
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        boolean enabled = getConfig().getBoolean("join.enabled", true);
        if (!enabled) {
            event.joinMessage(null);
            return;
        }

        String raw = getConfig().getString("join.message", "&a+ &f{player} &7joined the server!");
        Component msg = render(raw, event.getPlayer());
        event.joinMessage(msg);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        boolean enabled = getConfig().getBoolean("leave.enabled", true);
        if (!enabled) {
            event.quitMessage(null);
            return;
        }

        String raw = getConfig().getString("leave.message", "&c- &f{player} &7left the server!");
        Component msg = render(raw, event.getPlayer());
        event.quitMessage(msg);
    }

    private Component render(String input, Player player) {
        String replaced = applyPlaceholders(Objects.toString(input, ""), player);
        boolean useMM = getConfig().getBoolean("use-minimessage", false);

        // If mm is enabled, parse as mm, otherwise fall back to legacy.
        if (useMM) {
            try {
                return miniMessage.deserialize(replaced);

            } catch (Exception ex) {
                getLogger().warning("MiniMessage parsing failed. Falling back to legacy codes.");
                return legacy.deserialize(replaced);
            }
        }

        return legacy.deserialize(replaced);
    }

    private String applyPlaceholders(String msg, Player p) {
        return msg
                .replace("{player}", p.getName())
                .replace("{displayname}", stripToPlain(p.displayName()))
                .replace("{uuid}", p.getUniqueId().toString())
                .replace("{world}", (p.getWorld() != null ? p.getWorld().getName() : "unknown"));
    }

    private String stripToPlain(Component component) {
        String legacyStr = legacy.serialize(component);
        return legacyStr.replaceAll("(?i)&[0-9A-FK-OR]", "");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("amreload")) return false;

        if (!sender.hasPermission("am.reload")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        reloadConfig();
        sender.sendMessage("§aAwesomeMessages config reloaded successfully!");
        return true;
    }
}
