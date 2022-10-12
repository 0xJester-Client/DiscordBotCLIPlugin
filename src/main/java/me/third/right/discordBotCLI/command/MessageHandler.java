package me.third.right.discordBotCLI.command;

import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.events.RegCompleteEvent;
import me.third.right.discordBotCLI.hacks.DiscordBotCLI;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.utils.client.objects.Pair;
import me.third.right.utils.client.utils.LoggerUtils;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class MessageHandler implements MessageCreateListener {
    public static MessageHandler INSTANCE;
    private final DiscordBotCLI discordBotCLI;

    private final LinkedHashSet<Cmd> cmdList = new LinkedHashSet<>();

    public MessageHandler() {
        INSTANCE = this;
        discordBotCLI = DiscordBotCLI.INSTANCE;
        registerCmd();
        discordBotCLI.getApi().addListener(this);
    }


    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        final Message message = event.getMessage();

        if(discordBotCLI.strictCommandChannel()) {
            if(discordBotCLI.getCommandServerID().isEmpty() || discordBotCLI.getCommandChannelID().isEmpty()) {
                LoggerUtils.logError("Command server ID or command channel ID is empty. Please set them in the config.");
                return;
            }

            final Optional<ServerTextChannel> serverTextChannel = message.getServerTextChannel();
            if(serverTextChannel.isPresent()) {
                final ServerChannel channel = serverTextChannel.get();
                final Server server = channel.getServer();
                if (!(server.getId() + "").equals(discordBotCLI.getCommandServerID()) || !(channel.getId() + "").equals(discordBotCLI.getCommandChannelID())) {
                    return;
                }
            }
        }

        if(message.getAuthor().isBotUser() || !message.getContent().startsWith(discordBotCLI.getPrefix())) return;
        final String phase1 = message.getContent().replaceFirst(discordBotCLI.getPrefix(), "");

        for(Cmd command : cmdList) {
            for (String s : command.getName()) {
                if (!phase1.toLowerCase().startsWith(s.toLowerCase())) continue;

                final long uid = event.getMessageAuthor().getId();
                if (!canUseCommand(uid, command)) {
                    event.getMessage().getChannel().sendMessage("You lack the required permissions to use this command.");
                    return;
                }

                final String phase2 = phase1.replaceFirst(s, "");
                command.onMessage(event, phase2.split(" "));
                return;
            }
        }
    }

    private void registerCmd() {
        final String packageDir = Cmd.class.getPackage().getName()+".commands";
        final Set<Class<? extends Cmd>> classList = findClasses(packageDir);
        classList.forEach(c -> {
            if(c.isAnnotationPresent(Cmd.CmdInfo.class)) {
                try {
                    final Cmd cmd = c.getConstructor().newInstance();
                    cmdList.add(cmd);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    LoggerUtils.logError(e.toString());
                }
            }
        });

        ThirdMod.EVENT_PROCESSOR.post(new RegCompleteEvent());
    }

    private Set<Class<? extends Cmd>> findClasses(String pack) {
        Reflections reflections = new Reflections(pack);
        return reflections.getSubTypesOf(Cmd.class);
    }

    /**
     * Register a new Discord command.
     * @param cmd The command to register.
     */
    public void registerCmd(Cmd cmd) {
        if(cmdList.contains(cmd)) return;
        cmdList.add(cmd);
    }

    /**
     * Checks the Authors ID whether they have perms to use the given command.
     */
    public boolean canUseCommand(final long id, Cmd command) {
        final Pair<Long, Authority> user = discordBotCLI.getAuth().getByID(id);
        return user == null && command.getAuthority().equals(Authority.NONE)
                || user != null && user.getSecond().getPowerLevel() >= command.getAuthority().getPowerLevel();
    }

    /**
     * Gets the command by class.
     * @param cmd Command Class.
     * @return Command instance.
     */
    public Cmd getCommand(Class<Cmd> cmd) {return getCommand(cmd.getName());}
    /**
     * Gets the command by name.
     * @param name Command name.
     * @return Command instance.
     */
    public Cmd getCommand(String name) {
        for(Cmd c : cmdList) {
            for(String s : c.getName()) {
                if (s.equalsIgnoreCase(name)) return c;
            }
        }
        return null;
    }

    /**
     * Gets the command list.
     * @return Command list.
     */
    public LinkedHashSet<Cmd> getCmdList() {
        return cmdList;
    }

    // Returns END
}
