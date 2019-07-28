package com.github.cactuspuppy.mcmurder.game.murder.weapon;

import com.destroystokyo.paper.ParticleBuilder;
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
        Location startPoint = p.getEyeLocation().add(p.getLocation().getDirection().normalize().multiply(0.2));
        RayTraceResult result = p.getWorld().rayTrace(p.getEyeLocation(), p.getLocation().getDirection(),
                                                    200, FluidCollisionMode.NEVER, true, 0,
                                                       entity -> (entity instanceof Player && !entity.getUniqueId().equals(p.getUniqueId())));
        Location endPoint;
        boolean resultNull = false;
        if (result == null) {
            endPoint = p.getEyeLocation().add(p.getLocation().getDirection().multiply(200));
            resultNull = true;
        } else {
            endPoint = vectorToLocation(result.getHitPosition(), p.getWorld());
        }
        p.sendMessage("Start point " + startPoint.toString());
        p.sendMessage("End point " + endPoint.toString());
        int subdivs = 3;
        Vector dir = p.getLocation().getDirection().normalize().multiply(1./subdivs);
        p.sendMessage("direction vector " + dir.toString());
        double iterations = startPoint.distance(endPoint) * subdivs;
        p.sendMessage("iterations " + iterations);
        for (int i = 0; i < iterations; i++) {
            Location l = startPoint.clone();
            p.sendMessage("Dir: " + dir.multiply(i).toString());
            l = l.add(dir.multiply(i));

            ParticleBuilder builder = new ParticleBuilder(Particle.SMOKE_NORMAL);
            builder.force(true);
            builder.allPlayers();
            builder.count(1);
            builder.source(p);
            builder.location(l);
            builder.extra(0);
            builder.offset(0, 0, 0);
            builder.spawn();
            p.sendMessage("Location " + i + " " + l.toString());
        }
        if (resultNull) {
            return;
        }
        if (result.getHitBlock() != null) {
            Block block = result.getHitBlock();
            BlockData currData = block.getBlockData();
            block.breakNaturally();
            block.setBlockData(currData);
        } else if (result.getHitEntity() != null && result.getHitEntity() instanceof Player) {
            Player victim = (Player) result.getHitEntity();
            //TODO: Pass death to Game or Murder
        }
    }

    private Location vectorToLocation(Vector v, World w) {
        return new Location(w, v.getX(), v.getY(), v.getZ());
    }
}
