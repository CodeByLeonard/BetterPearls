package com.ranoe.betterPearls.logic;

import com.ranoe.betterPearls.BetterPearls;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PearlLogic {

    public static String displayName(ItemStack itemStack) {
        String displayName = PlainTextComponentSerializer.plainText().serialize(itemStack.displayName());
        return displayName.replace("[", "").replace("]", "");
    }

    /**
     * @return Returns either a correct target block or null if the display name does not match a Location
     */
    public static @Nullable Block getTargetBlock(Player player, ItemStack pearl) {
        if (!pearl.getItemMeta().hasDisplayName()) return null;
        String pearlName = displayName(pearl);

        Pattern pattern = Pattern.compile("^(-?\\d+)/(-?\\d+)/(-?\\d+)$");
        Matcher matcher = pattern.matcher(pearlName);
        if (matcher.matches()) {
            double x = Integer.parseInt(matcher.group(1));
            double y = Integer.parseInt(matcher.group(2));
            double z = Integer.parseInt(matcher.group(3));

            Location location = new Location(player.getWorld(), x, y, z);
            return location.getBlock();
        } else return null;
    }

    /**
     * @return Returns either a valid block or null if the surroundings is not suitable
     */
    public static @Nullable Block validTarget(Block targetBlock) {
        Material target = targetBlock.getType();
        Block belowBlock = targetBlock.getRelative(BlockFace.DOWN);
        Material below = targetBlock.getRelative(BlockFace.DOWN).getType();
        Block aboveBlock = targetBlock.getRelative(BlockFace.UP);
        Material above = targetBlock.getRelative(BlockFace.UP).getType();

        if (target != Material.AIR && !targetBlock.isPassable() && !targetBlock.isLiquid() && above == Material.AIR) return aboveBlock;
        if (target == Material.AIR && below != Material.AIR && !belowBlock.isPassable() && !belowBlock.isLiquid()) return targetBlock;
        return null;
    }

    public static void executeAlongPath(Player player, Block targetBlock, Consumer<Vector> executeAtPosition) {
        Location eyeLocation = player.getEyeLocation();
        Vector eyeDirection = eyeLocation.getDirection().normalize();
        Vector start = eyeLocation.toVector().add(eyeDirection);
        Vector end = targetBlock.getLocation().toCenterLocation().toVector();
        end = end.subtract(new Vector(0, 0.5, 0));
        double distance = start.distance(end);

        for (double t = 0; t <= 1; t += 1.5 / distance) { //0.03 increment for 50 blocks is excellent;
            Vector position = Utils.calculateCurve(start, end, eyeDirection, 1., t);
            executeAtPosition.accept(position);
        }
    }

    public static void executeGraduallyAlongPath(Player player, Block targetBlock, TriConsumer<Vector, Vector, Boolean> executeAtPosition) {
        Location eyeLocation = player.getEyeLocation();
        Vector eyeDirection = eyeLocation.getDirection().normalize();
        Vector start = eyeLocation.toVector().add(eyeDirection);
        Vector end = targetBlock.getLocation().toCenterLocation().toVector().subtract(new Vector(0, 0.5, 0));
        double distance = start.distance(end);
        new BukkitRunnable() {
            final double step = 0.15 / distance;
            double t = 0;

            @Override
            public void run() {
                boolean finished = false;
                if (t >= 1) {
                    cancel();
                    finished = true;
                    return;
                }

                Vector position = Utils.calculateCurve(start, end, eyeDirection, 1.0, t);
                Vector next = Utils.calculateCurve(start, end, eyeDirection, 1.0, t + 0.03);
                executeAtPosition.accept(position, next, finished);
                t += step;
            }
        }.runTaskTimer(BetterPearls.instance, 0L, 1L);
    }


    /**
     * @deprecated in favour of {@link #executeAlongPath(Player, Block, Consumer)}
     */
    @Deprecated
    public static void generatePath(@NonNull Player player, Block targetBlock) {
        Location eyeLocation = player.getEyeLocation();
        Vector eyeDirection = eyeLocation.getDirection().normalize();
        Vector start = eyeLocation.toVector().add(eyeDirection);
        Vector end = targetBlock.getLocation().toCenterLocation().toVector();

        for (double t = 0; t <= 1; t += 0.03) {
            Vector position = Utils.calculateCurve(start, end, eyeDirection, 1., t);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    position.getX(), position.getY(), position.getZ(),
                    0, 0, 0, 0, 0, null);
        }
    }

    public static void runTaskTimer20() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            if (itemInMainHand.getType() != Material.ENDER_PEARL) return;
            Block targetBlock = PearlLogic.getTargetBlock(player, itemInMainHand);
            if (targetBlock == null) return;
            Block validTarget = PearlLogic.validTarget(targetBlock);
            if (validTarget != null) PearlLogic.executeAlongPath(player, validTarget, (position) -> {
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                        position.getX(), position.getY(), position.getZ(),
                        0, 0, 0, 0, 0, null);
            });
        }
    }
}