package io.github.bluelhf.stayup;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class StayUp extends JavaPlugin implements Listener {
    public static BooleanFlag BLOCK_GRAVITY_FLAG;

    private BukkitTask ghostBlockTask;
    private LinkedBlockingQueue<org.bukkit.Location> ghostBlockQueue = new LinkedBlockingQueue<>();

    @Override
    public void onLoad() {
        // Register our WorldGuard flag
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            BooleanFlag flag = new BooleanFlag("disable-block-gravity");
            registry.register(flag);
            BLOCK_GRAVITY_FLAG = flag;
        } catch (FlagConflictException e) {
            getLogger().info("BLOCK PHYSICS FLAG COULD NOT BE REGISTERED!");
            getLogger().info("DISABLING...");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        reloadConfig();
        loadGhostBlockTask();
    }


    @Override
    public void onDisable() {
        if (ghostBlockTask != null) {
            pollGhostBlocks();
            if (!ghostBlockTask.isCancelled()) ghostBlockTask.cancel();
        }
    }
    public void loadGhostBlockTask() {
        if (ghostBlockTask != null && !ghostBlockTask.isCancelled()) ghostBlockTask.cancel();
        // Start polling ghost block queue
        ghostBlockTask = new BukkitRunnable() {@Override public void run() {
            pollGhostBlocks();
        }}.runTaskTimer(this, 0, getConfig().getInt("ghost-block-rate"));
    }
    private void pollGhostBlocks() {
        ArrayList<org.bukkit.Location> ghostStack = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            org.bukkit.Location l = ghostBlockQueue.poll();
            if (l == null) break;
            ghostStack.add(l);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (org.bukkit.Location l : ghostStack) p.sendBlockChange(l, l.getBlock().getBlockData());
        }
    }




    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFall(EntityChangeBlockEvent ev) {

        // Use WG's BukkitAdapter to get the block as a WorldGuard location
        Location loc = BukkitAdapter.adapt(ev.getBlock().getLocation());

        // Magic
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);

        // We use a null regionAssociable because our flag is not associated with a player
        Boolean b = set.queryValue(null, BLOCK_GRAVITY_FLAG);
        if (b == null || !b) return;

        if (ev.getEntity().getType() == EntityType.FALLING_BLOCK && ev.getBlock().getType().hasGravity()) {
            ev.setCancelled(true);
            ghostBlockQueue.add(ev.getBlock().getLocation());
        }
    }



    public static StayUp get() {
        return (StayUp) JavaPlugin.getProvidingPlugin(StayUp.class);
    }
}
