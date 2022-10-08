package me.third.right.discordBotCLI.command.commands;

import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.managers.AuthManagement;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.modules.Client.Command;
import me.third.right.utils.client.objects.Pair;
import org.javacord.api.event.message.MessageCreateEvent;

@Cmd.CmdInfo(name = "say", description = "This commands send public chat messages.")
public class SayCmd extends Cmd {

    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        if(args.length == 0) {
            event.getChannel().sendMessage("Please enter a message.");
            return;
        }
        final StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg).append(" ");
        }
        final String message = stringBuilder.toString().trim();

        if(message.startsWith("/") || message.startsWith(Command.INSTANCE.commandPrefix.getString())) {
            final AuthManagement authManagement = discordBotCLI.getAuth();
            final Pair<Long, Authority> pair = authManagement.getByID(event.getMessageAuthor().getId());
            if(pair == null || pair.getSecond().getPowerLevel() <= 0) {
                event.getChannel().sendMessage("You lack the required permissions to use this command.");
                return;
            }
        }
        mc.player.sendChatMessage(message);
    }
}
