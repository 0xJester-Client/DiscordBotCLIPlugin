package me.third.right.discordBotCLI.command.commands;

import me.bush.eventbus.annotation.EventListener;
import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.command.MessageHandler;
import me.third.right.discordBotCLI.events.RegCompleteEvent;
import me.third.right.discordBotCLI.managers.AuthManagement;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.utils.client.objects.Pair;
import me.third.right.utils.client.utils.LoggerUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.*;


//Embed info https://javacord.org/wiki/basic-tutorials/embeds.html#creating-an-embed
@Cmd.CmdInfo(
        name = {"help", "h"},
        description = "Displays a list of available commands.\n"
                +" \n"
                + "Syntax:\n"
                + "help - Lists all the commands available to you.\n"
                + "help <command> - Will display the commands help information."
)
public class HelpCmd extends Cmd {
    private int total = 0;
    private final HashMap<Authority, HashSet<String>> cmdCount = new HashMap<>();

    public HelpCmd() {
        ThirdMod.EVENT_PROCESSOR.subscribe(this);
    }

    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        final AuthManagement authorityManager = discordBotCLI.getAuth();
        final Pair<Long, Authority> user = authorityManager.getByID(event.getMessage().getAuthor().getId());
        final EmbedBuilder embedBuilder;

        if(cmdCount == null || cmdCount.isEmpty()) {
            LoggerUtils.logDebug("HelpCmd cmdCount is null or empty?");
            createCmdCount();
        }

        switch (args.length) {
            case 1:
                final List<String> list = new ArrayList<>((user == null ? cmdCount.get(Authority.NONE) : cmdCount.get(user.getSecond())));
                embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(String.format("CommandList (%d/%d)", list.size(), total));

                final StringBuilder stringBuilder = new StringBuilder("Available commands:\n");
                for(int i = 0; i != list.size(); i++) {
                    if(i == list.size() - 1)
                        stringBuilder.append(list.get(i)).append(" ");
                    else
                        stringBuilder.append(list.get(i)).append(", ");
                }
                embedBuilder.setDescription(stringBuilder.toString());
                event.getChannel().sendMessage(embedBuilder);
                break;
            case 2:
                final Cmd command = MessageHandler.INSTANCE.getCommand(args[1]);
                if(command == null) {
                    event.getChannel().sendMessage("The Requested command couldn't be found.");
                    return;
                }
                if(!canUseCommand(user == null ? Authority.NONE : user.getSecond(), command)) {
                    event.getChannel().sendMessage("You lack the required permissions to see this.");
                    return;
                }

                embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(command.getName()[0]);
                embedBuilder.setDescription(command.getDescription());
                embedBuilder.addField("Alt:", Arrays.toString(command.getName()));
                event.getChannel().sendMessage(embedBuilder);
                break;
            default:
                event.getChannel().sendMessage("Given too many arguments.");
                break;
        }
    }

    // * Events
    @EventListener
    public void onRegFinish(RegCompleteEvent event) {
        createCmdCount();
    }

    // * Methods

    private void createCmdCount() {
        cmdCount.clear();
        final LinkedHashSet<Cmd> set = MessageHandler.INSTANCE.getCmdList();
        for(Authority authority : Authority.values()) {
            final HashSet<String> cmdNames = new HashSet<>();

            for(Cmd command : set) {
                if(!canUseCommand(authority, command)) continue;
                cmdNames.add(command.getName()[0]);
            }

            cmdCount.put(authority, cmdNames);
        }
        total = set.size();
    }

    private boolean canUseCommand(Authority authority, Cmd command) {
        return authority.getPowerLevel() >= command.getAuthority().getPowerLevel();
    }
}
