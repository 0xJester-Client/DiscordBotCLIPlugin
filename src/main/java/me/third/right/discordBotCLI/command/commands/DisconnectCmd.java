package me.third.right.discordBotCLI.command.commands;

import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.modules.Other.AutoReconnect;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;
import org.javacord.api.event.message.MessageCreateEvent;

@Cmd.CmdInfo(name = {"disconnect", "dc"}, description = "Disconnects from the current Minecraft Server.", authority = Authority.ADMIN)
public class DisconnectCmd extends Cmd {

    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        if(mc.getConnection() == null) {
            event.getChannel().sendMessage("You are not connected to a server.");
            return;
        }

        AutoReconnect.INSTANCE.disable();
        mc.getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString(String.format("Disconnected by %s via Discord.", event.getMessageAuthor().getDiscriminatedName()))));
        event.getChannel().sendMessage("Disconnected from the server.");
    }
}
