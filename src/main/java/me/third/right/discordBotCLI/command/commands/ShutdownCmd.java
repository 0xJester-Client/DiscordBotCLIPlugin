package me.third.right.discordBotCLI.command.commands;

import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.utils.enums.Authority;
import org.javacord.api.event.message.MessageCreateEvent;

@Cmd.CmdInfo(name = "shutdown", description = "Shutdown the Minecraft instance.", authority = Authority.OWNER)
public class ShutdownCmd extends Cmd {

    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        event.getChannel().sendMessage("Shutting down...");
        System.exit(0);
    }
}
