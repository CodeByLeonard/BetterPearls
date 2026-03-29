package com.ranoe.betterPearls;

import com.ranoe.betterPearls.event.PlayerInteractHandler;
import com.ranoe.betterPearls.logic.PearlLogic;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterPearls extends JavaPlugin {
    public static BetterPearls instance;

    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getPluginManager().registerEvents(new PlayerInteractHandler(), instance);
        Bukkit.getScheduler().runTaskTimer(instance, PearlLogic::runTaskTimer20, 0L, 20L);
    }
}
