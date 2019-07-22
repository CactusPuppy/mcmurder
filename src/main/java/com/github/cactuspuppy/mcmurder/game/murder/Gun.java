package com.github.cactuspuppy.mcmurder.game.murder;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
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
                                                       entity -> (entity instanceof Player));
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
        Vector dir = p.getLocation().getDirection().normalize().multiply(1./subdivs);
        double iterations = startPoint.distance(endPoint) * subdivs;
        for (int i = 0; i < iterations; i++) {
            Location l = startPoint.add(dir.multiply(i));

            ParticleBuilder builder = new ParticleBuilder(Particle.SMOKE_NORMAL);
            builder.force(true);
            builder.allPlayers();
            builder.source(p);
            builder.location(l);
            builder.spawn();
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
            //TODO: Pass death to Game
        }
    }

    private Location vectorToLocation(Vector v, World w) {
        return new Location(w, v.getX(), v.getY(), v.getZ());
    }
}