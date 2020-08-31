package io.github.bluelhf.stayup.command;

import io.github.bluelhf.stayup.StayUp;
import io.github.bluelhf.stayup.util.Timing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StayUpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§aReloading...");
            long configReloadTime = Timing.time(() -> StayUp.get().reloadConfig());
            sender.sendMessage("  §7Reloaded configuration in " + configReloadTime + "ms.");
            long taskReloadTime = Timing.time(() -> StayUp.get().loadGhostBlockTask());
            sender.sendMessage("  §7Reloaded ghost block task in " + taskReloadTime + "ms.");
            sender.sendMessage("§aReloaded in " + (configReloadTime + taskReloadTime) + "ms.");
            return true;
        }
        sender.sendMessage("§7Usage: §a/stayup <reload>");
        return true;
    }
}
