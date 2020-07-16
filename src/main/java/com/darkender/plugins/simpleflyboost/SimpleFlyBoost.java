package com.darkender.plugins.simpleflyboost;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class SimpleFlyBoost extends JavaPlugin implements Listener
{
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                for(Player player : getServer().getOnlinePlayers())
                {
                    if(player.isSneaking() && player.isGliding() && player.hasMetadata("sneakpos"))
                    {
                        player.setVelocity(new Vector(0, 0.1, 0));
                        Location loc = (Location) player.getMetadata("sneakpos").get(0).value();
                        TeleportUtils.teleport(player, loc);
                    }
                }
            }
        }, 1L, 1L);
    }
    
    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.getAction() == Action.LEFT_CLICK_AIR && event.getPlayer().isGliding() && event.getItem() == null
                && event.getPlayer().hasPermission("simpleflyboost.boost"))
        {
            Player p = event.getPlayer();
            p.setVelocity(p.getEyeLocation().getDirection().normalize().multiply(2.0));
            p.playSound(p.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 1.0f);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onSneakToggle(PlayerToggleSneakEvent event)
    {
        if(event.isSneaking() && event.getPlayer().isGliding() && event.getPlayer().hasPermission("simpleflyboost.freeze"))
        {
            Player p = event.getPlayer();
            p.setVelocity(new Vector(0.0, 0.0, 0.0));
            p.setMetadata("sneakpos", new FixedMetadataValue(this, p.getLocation()));
            p.playSound(p.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 1.0f);
        }
        else if(!event.isSneaking() && event.getPlayer().hasMetadata("sneakpos"))
        {
            event.getPlayer().removeMetadata("sneakpos", this);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event)
    {
        // Cancel block breaking while gliding
        if(event.getPlayer().isGliding() && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR
            && event.getPlayer().hasPermission("simpleflyboost.boost"))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if(!(event.getDamager() instanceof Player))
        {
            return;
        }
        Player p = (Player) event.getDamager();
        // Cancel entity damage while gliding
        if(p.isGliding() && p.getInventory().getItemInMainHand().getType() == Material.AIR
                && p.hasPermission("simpleflyboost.boost"))
        {
            event.setCancelled(true);
        }
    }
}
