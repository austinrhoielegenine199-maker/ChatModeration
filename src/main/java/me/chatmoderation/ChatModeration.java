package me.chatmoderation;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ChatModeration extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("ChatModeration enabled!");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        if (!getConfig().getBoolean("settings.enabled")) {
            return;
        }

        String bypassPermission =
                getConfig().getString("settings.bypass-permission");

        if (bypassPermission != null
                && event.getPlayer().hasPermission(bypassPermission)) {
            return;
        }

        String message = event.getMessage();
        String lowerMessage = message.toLowerCase();

        // =========================
        //        WORD FILTER
        // =========================

        if (getConfig().getBoolean("word-filter.enabled")) {

            List<String> blockedWords =
                    getConfig().getStringList(
                            "word-filter.blocked-words"
                    );

            for (String word : blockedWords) {

                if (lowerMessage.contains(word.toLowerCase())) {

                    event.setCancelled(true);

                    String blockedMessage =
                            getConfig().getString(
                                    "word-filter.message",
                                    "&cPlease keep the chat clean."
                            );

                    event.getPlayer().sendMessage(
                            ChatColor.translateAlternateColorCodes(
                                    '&',
                                    blockedMessage
                            )
                    );

                    return;
                }
            }
        }

        // =========================
        //    ANTI-ADVERTISING
        // =========================

        if (getConfig().getBoolean(
                "anti-advertising.enabled"
        )) {

            boolean isAdvertisement = false;

            // Detect IP addresses
            if (getConfig().getBoolean(
                    "anti-advertising.block-ips"
            ) && message.matches(
                    ".*\\d{1,3}(\\.\\d{1,3}){3}.*"
            )) {

                isAdvertisement = true;
            }

            // Detect Discord invites
            if (getConfig().getBoolean(
                    "anti-advertising.block-discord"
            ) && (
                    lowerMessage.contains("discord.gg/")
                            || lowerMessage.contains(
                            "discord.com/invite/"
                    )
            )) {

                isAdvertisement = true;
            }

            // Detect domains
            if (getConfig().getBoolean(
                    "anti-advertising.block-domains"
            ) && lowerMessage.matches(
                    ".*\\b[a-z0-9-]+\\.(com|net|org|xyz|to|fun|gg|me|tk|ml|ga|cf|pw)\\b.*"
            )) {

                isAdvertisement = true;
            }

            if (isAdvertisement) {

                event.setCancelled(true);

                String advertisementMessage =
                        getConfig().getString(
                                "anti-advertising.message",
                                "&cAdvertising is not allowed."
                        );

                event.getPlayer().sendMessage(
                        ChatColor.translateAlternateColorCodes(
                                '&',
                                advertisementMessage
                        )
                );

                return;
            }
        }
    }

    // =========================
    //      RELOAD COMMAND
    // =========================

    // /chatmoderation reload
    // /cm reload

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (args.length == 1
                && args[0].equalsIgnoreCase("reload")) {

            if (!sender.hasPermission(
                    "chatmoderation.reload"
            )) {

                sender.sendMessage(
                        ChatColor.RED
                                + "You do not have permission to do that."
                );

                return true;
            }

            reloadConfig();

            sender.sendMessage(
                    ChatColor.GREEN
                            + "ChatModeration configuration reloaded!"
            );

            return true;
        }

        sender.sendMessage(
                ChatColor.YELLOW
                        + "Usage: /chatmoderation reload"
        );

        return true;
    }
}
