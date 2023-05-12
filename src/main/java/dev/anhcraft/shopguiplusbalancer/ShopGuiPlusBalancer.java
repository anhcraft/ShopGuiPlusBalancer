package dev.anhcraft.shopguiplusbalancer;

import net.brcdev.shopgui.exception.shop.ShopsNotLoadedException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShopGuiPlusBalancer extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("sgpb").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
                try {
                    new Runner(commandSender).run();
                } catch (ShopsNotLoadedException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        });
    }
}
