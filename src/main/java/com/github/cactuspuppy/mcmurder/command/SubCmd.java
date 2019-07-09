package com.github.cactuspuppy.mcmurder.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public interface SubCmd extends CommandExecutor, TabCompleter {
    String getUsage();
    String getDescription();
    String getName();
    List<String> getAliases();
    boolean hasPermission(CommandSender sender, String alias, String[] args);
}
