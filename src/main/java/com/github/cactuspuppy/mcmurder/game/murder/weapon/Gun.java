package com.github.cactuspuppy.mcmurder.game.murder.weapon;

import com.github.cactuspuppy.mcmurder.game.murder.event.PlayerMurderEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Gun implements Listener {
    @EventHandler
    public void onGunShoot(PlayerInteractEvent e) {
        if (!(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            return;
        }
        if (e.getItem() == null || !e.getItem().getType().equals(Material.BOW)) {
            return;
        }
        shootGun(e.getPlayer());
    }

    private void shootGun(Player p) {
        RayTraceResult result = p.getWorld().rayTrace(p.getEyeLocation(), p.getLocation().getDirection(),
                                                    200, FluidCollisionMode.NEVER, true, 0,
                                                       entity -> (entity instanceof Player && !entity.getUniqueId().equals(p.getUniqueId())));
        Location startPoint = p.getEyeLocation();
        Location endPoint;
        boolean resultNull = false;
        if (result == null) {
            endPoint = p.getEyeLocation().add(p.getLocation().getDirection().multiply(200));
            resultNull = true;
        } else {
            endPoint = vectorToLocation(result.getHitPosition(), p.getWorld());
        }
        int subdivs = 3;
        Vector base = p.getLocation().getDirection().normalize().multiply(1./subdivs);
        double iterations = startPoint.distance(endPoint) * subdivs;
        for (int i = 0; i < iterations; i++) {
            Vector delta = base.clone().multiply(i);
            Location l = startPoint.clone().add(delta);
            p.sendMessage(String.format("Location %d: %s", i, l.toString()));
            l.getWorld().spawnParticle(Particle.SMOKE_NORMAL, l, 3, 0, 0, 0, 0);
        }
        if (resultNull) {
            return;
        }
        if (result.getHitBlock() != null) {
            Block block = result.getHitBlock();
            BlockData currData = block.getBlockData();

        } else if (result.getHitEntity() != null && result.getHitEntity() instanceof Player) {
            Player victim = (Player) result.getHitEntity();
            //TODO: Pass death to Game or Murder
            victim.sendMessage("BANG"); //FIXME
            Bukkit.getPluginManager().callEvent(new PlayerMurderEvent(victim));
        }
    }

    private Location vectorToLocation(Vector v, World w) {
        return new Location(w, v.getX(), v.getY(), v.getZ());
    }
}
