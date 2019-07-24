package com.github.cactuspuppy.mcmurder.game.murder.weapon;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.github.cactuspuppy.mcmurder.Main;
import com.github.cactuspuppy.mcmurder.game.murder.event.PlayerMurderEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Knife implements Listener {
    private Entity throwKnife(Player p) {
        p.addAttachment(Main.getInstance(), "murder.throw_time." + System.currentTimeMillis(), true);
        Item knife = p.getWorld().dropItem(p.getLocation().add(0, 1.75, 0),
            new ItemStack(Material.IRON_SWORD, 0)
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
            PlayerMurderEvent event = new PlayerMurderEvent(p);
            Bukkit.getServer().getPluginManager().callEvent(event);
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

    private class ThrownKnife implements Projectile {
        private Item item;
        private ProjectileSource owner;

        public ThrownKnife(Item item, ProjectileSource source) {
            this.item = item;
            owner = source;
        }

        @Override
        public @Nullable ProjectileSource getShooter() {
            return owner;
        }

        @Override
        public void setShooter(@Nullable ProjectileSource projectileSource) {
            owner = projectileSource;
        }

        @Override
        public boolean doesBounce() {
            return false;
        }

        @Override
        public void setBounce(boolean b) { }

        @Override
        public @NotNull Location getLocation() {
            return item.getLocation();
        }

        @Override
        public @Nullable Location getLocation(@Nullable Location location) {
            if (location == null) {
                return null;
            }
            location.setDirection(item.getLocation().getDirection());
            location.setX(item.getLocation().getX());
            location.setY(item.getLocation().getY());
            location.setZ(item.getLocation().getZ());
            return location;
        }

        @Override
        public void setVelocity(@NotNull Vector vector) {

        }

        @Override
        public @NotNull Vector getVelocity() {
            return item.getVelocity();
        }

        @Override
        public double getHeight() {
            return item.getHeight();
        }

        @Override
        public double getWidth() {
            return item.getWidth();
        }

        @Override
        public @NotNull BoundingBox getBoundingBox() {
            return item.getBoundingBox();
        }

        @Override
        public boolean isOnGround() {
            return item.isOnGround();
        }

        @Override
        public @NotNull World getWorld() {
            return item.getWorld();
        }

        @Override
        public void setRotation(float v, float v1) {
            item.setRotation(v, v1);
        }

        @Override
        public boolean teleport(@NotNull Location location) {
            return item.teleport(location);
        }

        @Override
        public boolean teleport(@NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause teleportCause) {
            return item.teleport(location, teleportCause);
        }

        @Override
        public boolean teleport(@NotNull Entity entity) {
            return item.teleport(entity);
        }

        @Override
        public boolean teleport(@NotNull Entity entity, PlayerTeleportEvent.@NotNull TeleportCause teleportCause) {
            return item.teleport(entity, teleportCause);
        }

        @Override
        public @NotNull List<Entity> getNearbyEntities(double v, double v1, double v2) {
            return item.getNearbyEntities(v, v1, v2);
        }

        @Override
        public int getEntityId() {
            return item.getEntityId();
        }

        @Override
        public int getFireTicks() {
            return item.getFireTicks();
        }

        @Override
        public int getMaxFireTicks() {
            return item.getMaxFireTicks();
        }

        @Override
        public void setFireTicks(int i) {
            item.setFireTicks(i);
        }

        @Override
        public void remove() {
            item.remove();
        }

        @Override
        public boolean isDead() {
            return item.isDead();
        }

        @Override
        public boolean isValid() {
            return item.isValid();
        }

        @Override
        public void sendMessage(@NotNull String s) {
            item.sendMessage(s);
        }

        @Override
        public void sendMessage(@NotNull String[] strings) {
            item.sendMessage(strings);
        }

        @Override
        public @NotNull Server getServer() {
            return item.getServer();
        }

        @Override
        public @NotNull String getName() {
            return item.getName();
        }

        @Override
        public boolean isPersistent() {
            return item.isPersistent();
        }

        @Override
        public void setPersistent(boolean b) {
            item.setPersistent(b);
        }

        @Override
        public @Nullable Entity getPassenger() {
            return item.getPassenger();
        }

        @Override
        public boolean setPassenger(@NotNull Entity entity) {
            return item.setPassenger(entity);
        }

        @Override
        public @NotNull List<Entity> getPassengers() {
            return item.getPassengers();
        }

        @Override
        public boolean addPassenger(@NotNull Entity entity) {
            return item.addPassenger(entity);
        }

        @Override
        public boolean removePassenger(@NotNull Entity entity) {
            return item.removePassenger(entity);
        }

        @Override
        public boolean isEmpty() {
            return item.isEmpty();
        }

        @Override
        public boolean eject() {
            return item.eject();
        }

        @Override
        public float getFallDistance() {
            return item.getFallDistance();
        }

        @Override
        public void setFallDistance(float v) {
            item.setFallDistance(v);
        }

        @Override
        public void setLastDamageCause(@Nullable EntityDamageEvent entityDamageEvent) {
            item.setLastDamageCause(entityDamageEvent);
        }

        @Override
        public @Nullable EntityDamageEvent getLastDamageCause() {
            return item.getLastDamageCause();
        }

        @Override
        public @NotNull UUID getUniqueId() {
            return item.getUniqueId();
        }

        @Override
        public int getTicksLived() {
            return item.getTicksLived();
        }

        @Override
        public void setTicksLived(int i) {
            item.setTicksLived(i);
        }

        @Override
        public void playEffect(@NotNull EntityEffect entityEffect) {
            item.playEffect(entityEffect);
        }

        @Override
        public @NotNull EntityType getType() {
            return item.getType();
        }

        @Override
        public boolean isInsideVehicle() {
            return item.isInsideVehicle();
        }

        @Override
        public boolean leaveVehicle() {
            return item.leaveVehicle();
        }

        @Override
        public @Nullable Entity getVehicle() {
            return item.getVehicle();
        }

        @Override
        public void setCustomNameVisible(boolean b) {
            item.setCustomNameVisible(b);
        }

        @Override
        public boolean isCustomNameVisible() {
            return item.isCustomNameVisible();
        }

        @Override
        public void setGlowing(boolean b) {
            item.setGlowing(b);
        }

        @Override
        public boolean isGlowing() {
            return item.isGlowing();
        }

        @Override
        public void setInvulnerable(boolean b) {
            item.setInvulnerable(b);
        }

        @Override
        public boolean isInvulnerable() {
            return item.isInvulnerable();
        }

        @Override
        public boolean isSilent() {
            return item.isSilent();
        }

        @Override
        public void setSilent(boolean b) {
            item.setSilent(b);
        }

        @Override
        public boolean hasGravity() {
            return item.hasGravity();
        }

        @Override
        public void setGravity(boolean b) {
            item.setGravity(b);
        }

        @Override
        public int getPortalCooldown() {
            return item.getPortalCooldown();
        }

        @Override
        public void setPortalCooldown(int i) {
            item.setPortalCooldown(i);
        }

        @Override
        public @NotNull Set<String> getScoreboardTags() {
            return item.getScoreboardTags();
        }

        @Override
        public boolean addScoreboardTag(@NotNull String s) {
            return item.addScoreboardTag(s);
        }

        @Override
        public boolean removeScoreboardTag(@NotNull String s) {
            return item.removeScoreboardTag(s);
        }

        @Override
        public @NotNull PistonMoveReaction getPistonMoveReaction() {
            return item.getPistonMoveReaction();
        }

        @Override
        public @NotNull BlockFace getFacing() {
            return item.getFacing();
        }

        @Override
        public @NotNull Pose getPose() {
            return item.getPose();
        }

        @Override
        public @NotNull Spigot spigot() {
            return item.spigot();
        }

        @Override
        public @Nullable String getCustomName() {
            return item.getCustomName();
        }

        @Override
        public void setCustomName(@Nullable String s) {
            item.setCustomName(s);
        }

        @Override
        public void setMetadata(@NotNull String s, @NotNull MetadataValue metadataValue) {
            item.setMetadata(s, metadataValue);
        }

        @Override
        public @NotNull List<MetadataValue> getMetadata(@NotNull String s) {
            return item.getMetadata(s);
        }

        @Override
        public boolean hasMetadata(@NotNull String s) {
            return item.hasMetadata(s);
        }

        @Override
        public void removeMetadata(@NotNull String s, @NotNull Plugin plugin) {
            item.removeMetadata(s, plugin);
        }

        @Override
        public boolean isPermissionSet(@NotNull String s) {
            return item.isPermissionSet(s);
        }

        @Override
        public boolean isPermissionSet(@NotNull Permission permission) {
            return item.isPermissionSet(permission);
        }

        @Override
        public boolean hasPermission(@NotNull String s) {
            return item.hasPermission(s);
        }

        @Override
        public boolean hasPermission(@NotNull Permission permission) {
            return item.hasPermission(permission);
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b) {
            return item.addAttachment(plugin, s, b);
        }

        @Override
        public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
            return item.addAttachment(plugin);
        }

        @Override
        public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i) {
            return item.addAttachment(plugin, s, b, i);
        }

        @Override
        public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
            return item.addAttachment(plugin, i);
        }

        @Override
        public void removeAttachment(@NotNull PermissionAttachment permissionAttachment) {
            item.removeAttachment(permissionAttachment);
        }

        @Override
        public void recalculatePermissions() {
            item.recalculatePermissions();
        }

        @Override
        public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
            return item.getEffectivePermissions();
        }

        @Override
        public boolean isOp() {
            return item.isOp();
        }

        @Override
        public void setOp(boolean b) {
            item.setOp(b);
        }

        @Override
        public @NotNull PersistentDataContainer getPersistentDataContainer() {
            return item.getPersistentDataContainer();
        }
    }
}
