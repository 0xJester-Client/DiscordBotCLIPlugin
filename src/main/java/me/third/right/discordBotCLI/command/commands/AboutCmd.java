package me.third.right.discordBotCLI.command.commands;

import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.Main;
import me.third.right.discordBotCLI.command.Cmd;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

@Cmd.CmdInfo(name = { "about", "info" }, description = "About the bot.")
public class AboutCmd extends Cmd {

    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("About");
        embed.setDescription("This bot plugin was created by @³ʳᵈ#1703." +
                "\nPlugin used in ThirdMod(Wurst-2)");
        embed.setFooter(String.format("Plugin Version: %s Client Version: %s", Main.INSTANCE.getVersion(), ThirdMod.VERSION) );
        event.getChannel().sendMessage(embed);
    }
}
