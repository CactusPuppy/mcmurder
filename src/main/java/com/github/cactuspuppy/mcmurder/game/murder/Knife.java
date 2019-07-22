package com.github.cactuspuppy.mcmurder.game.murder;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.github.cactuspuppy.mcmurder.Main;
import com.github.cactuspuppy.mcmurder.game.Game;
import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@AllArgsConstructor
public class Knife implements Listener {
    private Game game;

    private Entity throwKnife(Player p) {
        p.addAttachment(Main.getInstance(), "murder.throw_time." + System.currentTimeMillis(), true);
        Item knife = p.getWorld().dropItem(p.getLocation().add(0, 1.75, 0),
            new ItemStack(Material.IRON_SWORD)
        );
        knife.setPickupDelay(20);
        knife.addAttachment(Main.getInstance(), "murder.owner." + p.getUniqueId().toString(), true);
        knife.addAttachment(Main.getInstance(), "murder.knife", true);

        Arrow arrow = p.launchProjectile(Arrow.class, p.getLocation().getDirection().multiply(2));
        arrow.addAttachment(Main.getInstance(), "murder.knife", true);
        arrow.addPassenger(knife);

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
        if (item.isOnGround()) {
            String pickupPerm = "murder.owner." + p.getUniqueId().toString();
            if (!item.hasPermission(pickupPerm)) {
                e.setCancelled(true);
                return;
            }
            p.getInventory().setItem(1, new ItemStack(Material.IRON_SWORD));
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onKnifeCollision(ProjectileHitEvent e) {
        if (!e.getEntity().getType().equals(EntityType.ARROW)
            || !e.getEntity().hasPermission("murder.knife")) {
            return;
        }
        Arrow a = (Arrow) e.getEntity();
        if (e.getHitBlock() != null) {
            for (Entity rider : a.getPassengers()) {
                if (rider instanceof Item && rider.hasPermission("murder.knife")) {
                    ((Item) rider).setPickupDelay(5);
                }
            }
            e.getEntity().remove();
        } else if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
            //FIXME: Move to method in Game or Murder
            Player p = (Player) e.getHitEntity();
            p.setGameMode(GameMode.SPECTATOR);
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2, 0, true, false, false));
            p.sendTitle(ChatColor.RED + "You Died!", "", 0, 40, 20);
            //TODO: Blood particles and head drop
        }
    }

    @EventHandler
    public void preventNonPlayerCollision(ProjectileCollideEvent e) {
        if (!e.getEntity().hasPermission("murder.knife")) {
            return;
        }
        if (e.getCollidedWith() instanceof Player) {
            return;
        }
        e.setCancelled(true);
    }
}
