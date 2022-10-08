package me.third.right.discordBotCLI.managers;

import me.bush.eventbus.annotation.EventListener;
import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.hacks.DiscordBotCLI;
import me.third.right.events.client.PacketEvent;
import me.third.right.modules.Client.Command;
import me.third.right.utils.client.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketChat;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.Optional;

public class ChatForwardingManager implements MessageCreateListener {
    protected Minecraft mc = Minecraft.getMinecraft();
    protected DiscordBotCLI discordBotCLI;
    private final long serverID;
    private final long channelID;

    public ChatForwardingManager(long serverID, long channelID) {
        this.serverID = serverID;
        this.channelID = channelID;
        discordBotCLI = DiscordBotCLI.INSTANCE;
        discordBotCLI.getApi().addListener(this);
        ThirdMod.EVENT_PROCESSOR.subscribe(this);
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if(mc.player == null || mc.world == null) return;

        final Message message = event.getMessage();
        if(message.getAuthor().isBotUser()) return;

        final Optional<ServerTextChannel> serverTextChannel = message.getServerTextChannel();
        if(serverTextChannel.isPresent()) {
            final ServerChannel channel = serverTextChannel.get();
            final Server server = channel.getServer();
            if(server.getId() == serverID && channel.getId() == channelID) {
                final String content = message.getContent();
                if(content.startsWith(discordBotCLI.getPrefix())) return;

                if(discordBotCLI.pushToPublicChat()) {
                    final String formattedMessage = String.format("%s: %s", message.getAuthor().getDisplayName(), content);
                    if(formattedMessage.length() >= 256) {
                        event.getChannel().sendMessage("Message too long to send to in-game chat.");

                    } else {
                        if(formattedMessage.startsWith("/") || formattedMessage.startsWith(Command.INSTANCE.commandPrefix.getString())) {
                            event.getChannel().sendMessage("Commands not allowed to be sent to in-game chat.");
                            return;
                        }

                        mc.player.sendChatMessage(formattedMessage);
                    }
                } else {
                    ChatUtils.moduleMessage(String.format("<%s> %s", message.getAuthor().getDisplayName(), content), discordBotCLI);
                }
            }
        }
    }

    @EventListener
    public void onChat(PacketEvent.Receive event) {
        if(mc.player == null || mc.world == null || !(event.getPacket() instanceof SPacketChat)) return;
        final SPacketChat packet = (SPacketChat) event.getPacket();
        final Optional<Server> server = discordBotCLI.getApi().getServerById(serverID);
        if(server.isPresent()) {
            final Optional<ServerTextChannel> channel = server.get().getTextChannelById(channelID);
            final String message = packet.getChatComponent().getUnformattedText();
            channel.ifPresent(serverTextChannel -> {
                final EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setDescription(message);
                serverTextChannel.sendMessage(embedBuilder);
            });
        }
    }
}
