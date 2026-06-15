package cc.sbsj.polang.goodstrade.commands;


import cc.sbsj.polang.goodstrade.GoodsTrade;
import cc.sbsj.polang.goodstrade.commands.annotation.SubCommand;
import cc.sbsj.polang.goodstrade.commands.annotation.SubCommandAnnotation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@SubCommandAnnotation(name = "goodstrade")
@SuppressWarnings("unused")
public class GoodsTradeCommand implements SubCommand, CommandExecutor, TabCompleter {

    private final CommandManager subCommandManager = new CommandManager();

    public GoodsTradeCommand(JavaPlugin plugin) {
        // 使用注解自动注册子命令
        subCommandManager.registerAnnotatedCommands(getClass().getPackage().getName() + ".sub", plugin);
    }

    @Override
    public String getPermission() {
        return "goodstrade.command";
    }

    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission(getPermission())) return false;
        if (args.length == 0) {
            sender.sendMessage(GoodsTrade.getPrefix() + GoodsTrade.lang.getString("command.help"));
            for (SubCommand command : subCommandManager.getCommands().values()) {
                if (sender.hasPermission(command.getPermission()))
                {
                    //只给玩家看他能看的命令
                    sender.sendMessage(GoodsTrade.getPrefix() + "/GoodsTrade " + command.getName());
                }
            }
            return false;
        }

        return subCommandManager.onCommand(sender, cmd, label, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(subCommandManager.getCommands().keySet());
        }

        return subCommandManager.onTabComplete(sender, cmd, label, args);
    }

    @Override
    public boolean onCommand( CommandSender sender,  Command cmd, String label, String  [] args) {
        return execute(sender, cmd, label, args);
    }

    @Override
    public List<String> onTabComplete( CommandSender sender,  Command cmd, String alias, String  [] args) {
        return tabComplete(sender, cmd, alias, args);
    }
}
