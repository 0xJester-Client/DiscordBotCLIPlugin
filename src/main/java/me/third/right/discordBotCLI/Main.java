package me.third.right.discordBotCLI;

import me.third.right.commands.Command;
import me.third.right.discordBotCLI.hacks.DiscordBotCLI;
import me.third.right.hud.Hud;
import me.third.right.modules.Hack;
import me.third.right.plugins.PluginBase;

@PluginBase.PluginInfo(name = "DiscordBotCLI", author = "ThirdRight")
public class Main extends PluginBase {
    public static Main INSTANCE;

    public Main() {
        INSTANCE = this;
    }

    @Override
    public Hack[] registerHacks() {
        return new Hack[] { new DiscordBotCLI() };
    }

    @Override
    public Hud[] registerHuds() {
        return new Hud[0];
    }

    @Override
    public Command[] registerCommands() {
        return new Command[0];
    }


}
