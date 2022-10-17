package me.third.right.discordBotCLI.command.commands;

import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.utils.client.utils.ChatUtils;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;
import org.javacord.api.event.message.MessageCreateEvent;

@Cmd.CmdInfo(name = {"connect", "join"}, description = "Connects to a Minecraft Server.", authority = Authority.ADMIN)
public class ConnectCmd extends Cmd {

    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        if(args.length == 0) {
            event.getChannel().sendMessage("Please specify a server to connect to.");
            return;
        }

        final String server = args[1];
        if(mc.getConnection() != null) {
            final String currentServer = ChatUtils.getFormattedServerIP();
            if (currentServer.equalsIgnoreCase("singleplayer")) {
                event.getChannel().sendMessage("You are currently in SinglePlayer world.");
                return;
            } else if (currentServer.equalsIgnoreCase(server.contains(":") ? server.split(":")[0] : server)) {
                event.getChannel().sendMessage("You are already connected to this server.");
                return;
            }


            mc.getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString("Disconnected by " + event.getMessageAuthor().getDisplayName() + " via Discord.")));
        }

        final ServerData serverData;
        if(server.contains(":")) {
            final String[] split = server.split(":");
            final String ip = split[0];
            final int port = Integer.parseInt(split[1]);
            serverData = new ServerData(server, (ip + ":" + port).trim(), false);
        } else {
            serverData = new ServerData(server, (server + ":25565").trim(), false);
        }

        connect(serverData);
        event.getChannel().sendMessage("Connecting to " + server + "...");
    }

    public void connect(ServerData serverData) {
        mc.addScheduledTask(() -> mc.displayGuiScreen(new GuiConnecting(mc.currentScreen, mc, serverData)));
    }
}
