package com.ranoe.betterPearls.event;

import com.ranoe.betterPearls.logic.PearlLogic;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PlayerInteractHandler implements Listener {

    @EventHandler
    public void onEnderPearlThrow(PlayerInteractEvent event) {
        if (event.getMaterial() != Material.ENDER_PEARL && !event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

        Block targetBlock = PearlLogic.getTargetBlock(player, itemInMainHand);
        if (targetBlock == null) return;

        Block validTarget = PearlLogic.validTarget(targetBlock);
        if (validTarget == null) return;

        event.setCancelled(true);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1f, 1f);

        EnderPearl pearl = player.launchProjectile(EnderPearl.class);
        pearl.setGravity(false);

        PearlLogic.executeGraduallyAlongPath(player, validTarget, (position, next, finished) -> {
            if (finished) { pearl.collidesAt(pearl.getLocation()); }
            Vector tangent = next.clone().subtract(position).normalize();
            pearl.teleport(position.toLocation(pearl.getWorld()));
            pearl.setVelocity(tangent.multiply(0.2));

        });
    }


}
