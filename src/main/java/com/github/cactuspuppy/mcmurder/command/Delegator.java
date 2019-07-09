package com.github.cactuspuppy.mcmurder.command;

import com.github.cactuspuppy.mcmurder.Main;
import com.github.cactuspuppy.mcmurder.utils.Logger;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Delegator implements CommandExecutor, TabCompleter {
    private static Map<String, SubCmd> subCmdMap = new HashMap<>();
    private static Map<String, SubCmd> aliasMap = new HashMap<>();

    public static void registerSubCmd(SubCmd subCmd) {
        subCmdMap.put(subCmd.getName(), subCmd);
        for (String alias : subCmd.getAliases()) {
            if (aliasMap.containsKey(alias)) {
                Logger.logWarning(Delegator.class, String.format("Alias %s is already registered!", alias));
                continue;
            }
            aliasMap.put(alias, subCmd);
        }
    }

    private static SubCmd getHandler(String subcmd) {
        SubCmd candidate = subCmdMap.get(subcmd);
        if (candidate != null) {
            return candidate;
        }
        candidate = aliasMap.get(subcmd);
        return candidate;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String alias, @NotNull String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.GOLD + Main.getInstance().getDescription().getName()
                                      + ChatColor.GREEN + " v" + Main.getInstance().getDescription().getVersion()
                                      + "for " + ChatColor.BLUE + Main.getInstance().getDescription().getAPIVersion());

            TextComponent interactive = new TextComponent("/" + command.getLabel() + " help");
            interactive.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            interactive.setUnderlined(true);
            interactive.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to run this command").color(net.md_5.bungee.api.ChatColor.GOLD).create()
            ));
            interactive.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + command.getLabel() + " help"
            ));
            ComponentBuilder helpSuggest = new ComponentBuilder("For a list of commands, run ")
                                           .color(net.md_5.bungee.api.ChatColor.DARK_AQUA)
                                           .append(interactive).retain(ComponentBuilder.FormatRetention.NONE);
            commandSender.sendMessage(helpSuggest.toString());
            return true;
        }
        String subcmd = args[0];
        if (subcmd.equals("help")) {
            //TODO
            return true;
        }
        SubCmd handler = getHandler(subcmd);
        if (handler == null) {
            commandSender.sendMessage(ChatColor.RED + "Unknown subcommand or alias " + ChatColor.RESET + subcmd);
            return true;
        }
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        if (!handler.hasPermission(commandSender, subcmd, newArgs)) {
            commandSender.sendMessage(ChatColor.RED + command.getPermissionMessage());
            return true;
        }
        return handler.onCommand(commandSender, command, alias, newArgs);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        /* Handle ambiguity about whether tab complete
        * args include command label */
        if (args.length > 0 && args[0].equals(command.getLabel())) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        List<String> empty = new ArrayList<>();
        if (args.length == 0) {
            return empty;
        }
        SubCmd handler = getHandler(args[0]);
        if (handler == null) {
            return empty;
        }
        return handler.onTabComplete(commandSender, command, alias, args);
    }
}
