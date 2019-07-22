package com.github.cactuspuppy.mcmurder.weapons;

import com.github.cactuspuppy.mcmurder.Main;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Knife implements Listener {
    private static Entity throwKnife(Player p) {
        Item knife = p.getWorld().dropItem(p.getLocation().add(0, 1.75, 0),
            new ItemStack(Material.IRON_SWORD)
        );
        knife.setPickupDelay(20);
        knife.setVelocity(p.getLocation().getDirection().multiply(1.2));
        knife.addAttachment(Main.getInstance(), "murder.owner." + p.getUniqueId().toString(), true);
        knife.addAttachment(Main.getInstance(), "murder.knife", true);

        return knife;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (!(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            || item == null
            || !(item.getType().equals(Material.IRON_SWORD) && e.getPlayer().getInventory().getHeldItemSlot() == 1)) {
            return;
        }
        throwKnife(e.getPlayer());
        item.setType(Material.WOODEN_SWORD);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        Item item = e.getItem();
        if (!(e.getEntity() instanceof Player) || !item.hasPermission("murder.knife")) {
            return;
        }
        Player p = (Player) e.getEntity();
        String pickupPerm = "murder.owner." + p.getUniqueId().toString();
        if (!item.hasPermission(pickupPerm)) {
            e.setCancelled(true);
            return;
        }
        p.getInventory().setItem(1, new ItemStack(Material.IRON_SWORD));
        item.remove();
    }
}
