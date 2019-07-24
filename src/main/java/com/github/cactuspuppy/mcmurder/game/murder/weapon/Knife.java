package com.github.cactuspuppy.mcmurder.game.murder.weapon;

import com.github.cactuspuppy.mcmurder.Main;
import com.github.cactuspuppy.mcmurder.game.murder.event.PlayerMurderEvent;
import org.bukkit.Bukkit;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class Knife implements Listener, Runnable {
    private static HashSet<Item> thrownKnives = new HashSet<>();

    private Entity throwKnife(Player p) {
        Item knife = p.getWorld().dropItem(p.getEyeLocation().add(p.getLocation().getDirection().multiply(0.5)),
                                           new ItemStack(Material.IRON_SWORD, 1));
        knife.setThrower(p.getUniqueId());
        knife.setVelocity(p.getLocation().getDirection().multiply(1.75));
        knife.setPickupDelay(Integer.MAX_VALUE);
        knife.setOwner(p.getUniqueId());
        knife.setMetadata(p.getUniqueId().toString(), new FixedMetadataValue(Main.getInstance(), ""));
        knife.setCanMobPickup(false);
        thrownKnives.add(knife);
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
        if (e instanceof Player) {
            return;
        }
        Item i = e.getItem();
        Player p = (Player) e.getEntity();
        if (!i.getMetadata(p.getUniqueId().toString()).isEmpty()) {
            return;
        }
        if (i.getOwner() != null && !i.getOwner().equals(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }


    @Override
    public void run() {
        for (Item knife : thrownKnives) {
            Player closestPlayer = (Player) knife.getNearbyEntities(1, 1, 1).stream()
                                            .filter(e -> e instanceof Player)
                                            .min((o1, o2) -> (int) (o1.getLocation().distance(knife.getLocation()) - o2.getLocation().distance(knife.getLocation())))
                                            .orElse(null);
            if (closestPlayer == null) {
                continue;
            }
            BoundingBox playerBox = closestPlayer.getBoundingBox();
            if (!playerBox.expand(0.1, 0, 0.1).contains(knife.getLocation().toVector())) {
                return;
            }
            Bukkit.getPluginManager().callEvent(new PlayerMurderEvent(closestPlayer));
            knife.setVelocity(new Vector(0, 0, 0));
            knife.setPickupDelay(40);
            thrownKnives.remove(knife);
        }
    }
}
