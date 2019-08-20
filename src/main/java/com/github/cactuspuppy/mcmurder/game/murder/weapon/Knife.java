package com.github.cactuspuppy.mcmurder.game.murder.weapon;

import com.github.cactuspuppy.mcmurder.game.murder.event.PlayerMurderEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;

public class Knife implements Listener, Runnable {
    private static HashSet<Item> thrownKnives = new HashSet<>();

    private Entity throwKnife(Player p) {
        Item knife = p.getWorld().dropItem(p.getEyeLocation(),
                                           new ItemStack(Material.IRON_SWORD, 1));
        knife.setThrower(p.getUniqueId());
        knife.setVelocity(p.getLocation().getDirection().multiply(1.4));
        knife.setPickupDelay(Integer.MAX_VALUE);
        knife.setOwner(p.getUniqueId());
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
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Item i = e.getItem();
        Player p = (Player) e.getEntity();
        // Do not pick up item if it has an owner
        if (i.getOwner() != null && !i.getOwner().equals(p.getUniqueId())) {
            e.setCancelled(true);
        }
        p.sendMessage("Picking up knife...");
        e.getItem().remove();
        p.getInventory().setItem(1, new ItemStack(Material.IRON_SWORD));
        e.setCancelled(true);
    }


    @Override
    public void run() {
        for (Item knife : new ArrayList<>(thrownKnives)) {
            //Summon smoke particles every so often
            if (knife.getTicksLived() % 3 == 0) {
                knife.getWorld().spawnParticle(Particle.SMOKE_NORMAL, knife.getLocation(), 3, 0, 0, 0, 0);
            }

            if (knife.isOnGround()) {
                knife.setPickupDelay(40);
                thrownKnives.remove(knife);
                continue;
            }
            Player closestPlayer = (Player) knife.getNearbyEntities(1, 1, 1).stream()
                                            .filter(e -> e instanceof Player && e.getUniqueId().equals(knife.getOwner()))
                                            .min((o1, o2) -> (int) (o1.getLocation().distance(knife.getLocation()) - o2.getLocation().distance(knife.getLocation())))
                                            .orElse(null);
            if (closestPlayer == null) {
                continue;
            } else if (knife.getOwner() != null
                        && knife.getTicksLived() < 20
                        && knife.getOwner().equals(closestPlayer.getUniqueId())) {
                // Prevent sprinting into own knife
                continue;
            }
            BoundingBox playerBox = closestPlayer.getBoundingBox();
            if (!playerBox.contains(knife.getLocation().toVector())) {
                return;
            }
            closestPlayer.sendMessage("You got hit!");
            Bukkit.getPluginManager().callEvent(new PlayerMurderEvent(closestPlayer));
            knife.setVelocity(new Vector(0, 0, 0));
            knife.setPickupDelay(40);
            thrownKnives.remove(knife);
        }
    }
}
