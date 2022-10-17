package me.third.right.discordBotCLI.command.commands;

import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.Main;
import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.utils.client.utils.ChatUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

@Cmd.CmdInfo(name = "server", description = "Get Server information.", authority = Authority.ADMIN)
public class ServerCmd extends Cmd {

    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        if(args.length == 0) {
            event.getChannel().sendMessage("Invalid arguments. Please use help for more information.");
            return;
        }

        if(mc.player == null || mc.world == null) {
            event.getChannel().sendMessage("You must be in game to use this command.");
            return;
        }

        if(ChatUtils.getFormattedServerIP().equalsIgnoreCase("Singleplayer")) {
            event.getChannel().sendMessage("You must be in a server to use this command.");
            return;
        }

        final EmbedBuilder embedBuilder = new EmbedBuilder();
        switch (args[1]) {
            case "info":
            case "status":
                embedBuilder.setTitle("Server Info: ");
                embedBuilder.addField("IP: ", ChatUtils.getFormattedServerIP());
                embedBuilder.addField("TPS: ", String.valueOf(ChatUtils.applyReplacements("<tps>")));
                embedBuilder.addField("Ping: ", String.valueOf(ChatUtils.applyReplacements("<ms>")));
                embedBuilder.setFooter(String.format("Plugin Version: %s Client Version: %s", Main.INSTANCE.getVersion(), ThirdMod.VERSION) );
                event.getChannel().sendMessage(embedBuilder);
                break;
            case "tab":
            case "players":
                embedBuilder.setTitle("Server Players: ");

                final StringBuilder players = new StringBuilder();
                if(mc.getConnection() == null || mc.getConnection().getPlayerInfoMap().isEmpty()) {
                    players.append("No players online.");
                    embedBuilder.addField("Players: ", players.toString());
                    return;
                }

                mc.getConnection().getPlayerInfoMap().forEach(playerInfo -> players.append(playerInfo.getGameProfile().getName()).append(", "));
                embedBuilder.addField("Players: ", players.toString());
                event.getChannel().sendMessage(embedBuilder);
                break;
            case "client":
                embedBuilder.setTitle("Client Info: ");

                embedBuilder.addField("Username: ", mc.getSession().getUsername());
                embedBuilder.addField("Health: ", String.format("%.1f", mc.player.getHealth() + mc.player.getAbsorptionAmount()));
                embedBuilder.addField("FPS: ", String.valueOf(ChatUtils.applyReplacements("<fps>")));
                embedBuilder.addField("Ping: ", String.valueOf(ChatUtils.applyReplacements("<ms>")));
                embedBuilder.addField("CPU: ", String.valueOf(ChatUtils.applyReplacements("<cpu>")));

                event.getChannel().sendMessage(embedBuilder);
                break;
            default:
                event.getChannel().sendMessage("Invalid argument.");
                break;
        }
    }
}
