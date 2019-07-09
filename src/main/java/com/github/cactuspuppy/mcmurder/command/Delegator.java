package com.github.cactuspuppy.mcmurder.command;

import com.github.cactuspuppy.mcmurder.utils.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
