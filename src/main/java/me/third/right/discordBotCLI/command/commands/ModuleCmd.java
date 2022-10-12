package me.third.right.discordBotCLI.command.commands;

import me.bush.eventbus.annotation.EventListener;
import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.events.RegCompleteEvent;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.modules.Hack;
import me.third.right.modules.HackList;
import me.third.right.utils.client.utils.LoggerUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Cmd.CmdInfo(name = "module",
        description = "Allows you to see and toggle available Modules/Hacks.\n" +
                "Syntax:\n" +
                "module - Lists all the modules available to you.\n" +
                "module <module> - Will display the modules help information.\n" +
                "module <module> toggle - Will toggle the module on or off.",
        authority = Authority.ADMIN
)
public class ModuleCmd extends Cmd {
    private final HackList hackList = ThirdMod.getHax();
    private final List<String> cacheNames = new ArrayList<>();

    public ModuleCmd() {
        ThirdMod.EVENT_PROCESSOR.subscribe(this);
    }

    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        final EmbedBuilder embedBuilder;

        if(cacheNames.isEmpty()) {
            LoggerUtils.logDebug("ModuleCmd cache is empty?");
            createCache();
        }

        switch (args.length) {
            case 1:
                final List<String> list = cacheNames;
                embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(String.format("HackList (%d)", list.size()));

                final StringBuilder stringBuilder = new StringBuilder("Available Hacks:\n");
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
                final Hack hack = hackList.getHack(args[1]);
                if(hack == null) {
                    event.getChannel().sendMessage("Hack not found");
                    return;
                }

                embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(String.format("Hack: %s", hack.getName()));
                embedBuilder.setDescription(String.format("Description: %s", hack.getDescription()));
                embedBuilder.addField("Enabled", String.valueOf(hack.isEnabled()));
                embedBuilder.addField("Category", hack.getCategory().name());

                final List<String> settings = hack.getSettings().keySet().stream().map(Object::toString).collect(Collectors.toList());
                final StringBuilder settingBuilder = new StringBuilder();
                for(int i = 0; i != settings.size(); i++) {
                    if(i == settings.size() - 1)
                        settingBuilder.append(settings.get(i)).append(" ");
                    else
                        settingBuilder.append(settings.get(i)).append(", ");
                }
                embedBuilder.addField("Settings:", settingBuilder.toString());
                event.getChannel().sendMessage(embedBuilder);
                break;
            case 3:
                final Hack hack1 = hackList.getHack(args[1]);
                if(hack1 == null) {
                    event.getChannel().sendMessage("Hack not found");
                    return;
                }

                if(args[2].equalsIgnoreCase("toggle")) {
                    hack1.setEnabled(!hack1.isEnabled());
                    event.getChannel().sendMessage(String.format("Hack %s has been %s", hack1.getName(), hack1.isEnabled() ? "enabled" : "disabled"));
                }
                break;
        }

    }

    @EventListener
    public void onRegFinish(RegCompleteEvent event) {
        createCache();
    }


    private void createCache() {
        cacheNames.clear();
        cacheNames.addAll(hackList.getHackList().stream().map(Hack::getName).collect(Collectors.toList()));
    }

}
