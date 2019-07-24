package com.github.cactuspuppy.mcmurder.game.murder.weapon;

import com.github.cactuspuppy.mcmurder.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class Consumable implements Listener {
    protected final Material itemType;

    public Consumable(Material type) {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        itemType = type;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (!(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            return;
        }
        if (e.getItem() == null || !e.getItem().getType().equals(itemType)) {
            return;
        }
        e.getItem().setAmount(e.getItem().getAmount() - 1);
        onUse();
    }

    protected abstract void onUse();
}
