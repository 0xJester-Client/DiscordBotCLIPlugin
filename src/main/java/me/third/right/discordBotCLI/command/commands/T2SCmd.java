package me.third.right.discordBotCLI.command.commands;

import com.mojang.text2speech.Narrator;
import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.utils.enums.Authority;
import org.javacord.api.event.message.MessageCreateEvent;

@Cmd.CmdInfo(name = "t2s",
        description = "Allows you to convert text to speech.\n" +
                "Syntax:\n" +
                "t2s <text> - Will convert the text to speech.",
        authority = Authority.NONE
)
public class T2SCmd extends Cmd {
    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        if(args.length == 1) {
            event.getChannel().sendMessage("Please provide some text to convert to speech.");
            return;
        }
        final StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i != args.length; i++) {
            stringBuilder.append(args[i]).append(" ");
        }
        event.getChannel().sendMessage("Done.");
        Narrator.getNarrator().say(stringBuilder.toString());
    }
}
