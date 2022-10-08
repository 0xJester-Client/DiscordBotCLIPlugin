package me.third.right.discordBotCLI.hacks;

import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.command.MessageHandler;
import me.third.right.discordBotCLI.managers.AuthManagement;
import me.third.right.discordBotCLI.managers.ChatForwardingManager;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.modules.Hack;
import me.third.right.settings.setting.CheckboxSetting;
import me.third.right.settings.setting.EnumSetting;
import me.third.right.settings.setting.StringSetting;
import me.third.right.utils.client.enums.Category;
import me.third.right.utils.client.objects.Pair;
import me.third.right.utils.client.utils.ChatUtils;
import me.third.right.utils.client.utils.FileUtils;
import me.third.right.utils.client.utils.LoggerUtils;
import me.third.right.utils.client.utils.MathUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.nio.file.Path;


@Hack.HackInfo(name = "DiscordBotCLI", description = "Allows you to send commands to client via discord.", category = Category.CLIENT)
public class DiscordBotCLI extends Hack {
    //Vars
    public static DiscordBotCLI INSTANCE;
    public final Path path = ThirdMod.configFolder.resolve("DiscordBotCLI");
    public final Path tempPath = path.resolve("TEMP");
    private DiscordApi api = null;
    private MessageHandler messageHandler = null;
    private ChatForwardingManager chatForwardingManager = null;
    private AuthManagement authManagement = null;

    private enum Page { Config, Discord, Permissions }
    //Settings
    private final EnumSetting<Page> page = setting(new EnumSetting<>("Page", Page.values(), Page.Config));
    // * Config
    private final StringSetting token = setting(new StringSetting("Token","Discord API Token.", "", Integer.MAX_VALUE, X -> !page.getSelected().equals(Page.Config)));
    private final StringSetting prefix = setting(new StringSetting("Prefix","The starting character to active commands. (In Discord)","-", 1, X -> !page.getSelected().equals(Page.Config)));

    // * Permissions
    private final StringSetting ownerID = setting(new StringSetting("OwnerID","The ID of the owner of the bot.", "", Integer.MAX_VALUE, X -> !page.getSelected().equals(Page.Permissions)));
    //TODO add permission GUI

    // * Chat Forwarding
    private final StringSetting serverID = setting(new StringSetting("ServerID","The ID of the server to send messages to.", "", Integer.MAX_VALUE, X -> !page.getSelected().equals(Page.Discord)));
    private final CheckboxSetting chatForwarding = setting(new CheckboxSetting("ChatForwarding","Forwards chat messages to discord.", false, X -> !page.getSelected().equals(Page.Discord)));
    private final CheckboxSetting publicChat = setting(new CheckboxSetting("PublicChat","Pushes discord messages to public chat.", false, X -> !page.getSelected().equals(Page.Discord)));
    private final StringSetting chatForwardChannelID = setting(new StringSetting("ChatForwardChannelID","The ID of the channel to send messages to.", "", Integer.MAX_VALUE, X -> !chatForwarding.isChecked() || !page.getSelected().equals(Page.Discord)));
    private final CheckboxSetting strictCommandChannel = setting(new CheckboxSetting("StrictCommandChannel","Only allow commands to br executed in a dedicated channel.", false, X -> !page.getSelected().equals(Page.Discord)));
    private final StringSetting commandChannelID = setting(new StringSetting("CommandChannelID","The ID of the Channel to accept commands from.", "", Integer.MAX_VALUE, X -> !page.getSelected().equals(Page.Discord)));

    //Overrides

    public DiscordBotCLI() {//TODO known issues AuthManagement is not detected when the module is toggled repeatedly.
        FileUtils.folderExists(path);
        FileUtils.folderExists(tempPath);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        String tokenString = this.token.getString();
        final boolean isInGame = nullCheck();

        if(tokenString.isEmpty()) {
            if(isInGame) {
                LoggerUtils.moduleLog(this, "Token is empty, disabling.");
            } else {
                ChatUtils.moduleMessage( "Token is empty, disabling.",this);
            }
            disable();
            return;
        }

        if(!isPresent()) {
            if (isInGame) {
                LoggerUtils.moduleLog(this, "Discord API is not present, disabling.");
            } else {
                ChatUtils.moduleMessage("Discord API is not present, disabling.", this);
            }
            disable();
            return;
        }

        authManagement = new AuthManagement();

        if(MathUtils.isLong(ownerID.getString())) {
            authManagement.addUser(new Pair<>(Long.parseLong(ownerID.getString()), Authority.OWNER));
        } else LoggerUtils.moduleLog(this, "OwnerID is not a valid, ignoring.");

        api = new DiscordApiBuilder().setToken(tokenString).login().join();
        messageHandler = new MessageHandler();


        if(chatForwarding.isChecked()) {
            if(!MathUtils.isLong(serverID.getString()) || !MathUtils.isLong(chatForwardChannelID.getString())) {
                if (isInGame) {
                    LoggerUtils.moduleLog(this, "Invalid ServerID or ChannelID.");
                } else {
                    ChatUtils.moduleMessage("Invalid ServerID or ChannelID.", this);
                }
                return;
            }

            final long sID = Long.parseLong(serverID.getString());
            final long cID = Long.parseLong(chatForwardChannelID.getString());
            chatForwardingManager = new ChatForwardingManager(sID, cID);
        }
    }

    @Override
    public void onDisable() {
        shutdown();
    }

    @Override
    public void onClose() {
        shutdown();
    }

    //Methods

    public boolean isPresent() {//TODO create a class todo this so we cache it and don't run a catch each time doing this type of check.
        try {
            Class.forName(DiscordApi.class.getName());
            return true;
        } catch (NoClassDefFoundError | ClassNotFoundException var1) {
            return false;
        }
    }

    public boolean strictCommandChannel() {
        return strictCommandChannel.isChecked();
    }

    public String getCommandServerID() {
        return serverID.getString();
    }

    public String getCommandChannelID() {
        return commandChannelID.getString();
    }

    public String getPrefix() {
        return prefix.getString();
    }

    public String getOwnerID() {
        return ownerID.getString();
    }

    public boolean pushToPublicChat() {
        return publicChat.isChecked();
    }

    public DiscordApi getApi() {
        return api;
    }

    public AuthManagement getAuth() {
        return authManagement;
    }

    public void shutdown() {
        if(api != null) {
            authManagement.saveData();
            ThirdMod.EVENT_PROCESSOR.unsubscribe(chatForwardingManager);
            api.disconnect();
        }
        System.gc();
    }

}
