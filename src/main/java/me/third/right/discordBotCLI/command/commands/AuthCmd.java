package me.third.right.discordBotCLI.command.commands;


import me.bush.eventbus.annotation.EventListener;
import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.events.RegCompleteEvent;
import me.third.right.discordBotCLI.events.UserAddRemoveEvent;
import me.third.right.discordBotCLI.managers.AuthManagement;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.utils.client.objects.Pair;
import me.third.right.utils.client.utils.LoggerUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static me.third.right.discordBotCLI.utils.Tools.idCleanup;
import static me.third.right.discordBotCLI.utils.Tools.stringToEnum;
import static me.third.right.utils.client.utils.MathUtils.isLong;

@Cmd.CmdInfo(
        name = { "auth", "user" },
        description = "Authority management.\n"
                + "-auth [add/a] [id/@] [Blocked/Admin] Add users auth data.\n"
                + "-auth [remove/r/del] [id/@] Remove users auth data.\n"
                + "-auth [list/l] lists all user auth data.",
        authority = Authority.ADMIN
)
public class AuthCmd extends Cmd {
    private final HashSet<String> dataCache = new HashSet<>();
    private String authorityCache = null;

    public AuthCmd() {
        ThirdMod.EVENT_PROCESSOR.subscribe(this);
        cacheData();
        cacheLevels();
    }

    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        switch (args.length){
            case 2:
                final EmbedBuilder embedBuilder;
                switch (args[1].toLowerCase(Locale.ROOT)) {
                    case "list":
                    case "l":
                        if(dataCache.isEmpty()) {
                            LoggerUtils.logDebug(String.format("CMD: %s No Cached user data?", AuthCmd.class.getName()));
                            cacheData();
                        }

                        embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("AuthData");
                        final StringBuilder builder = new StringBuilder();
                        for(String line : dataCache) {
                            builder.append(line).append("\n");
                        }
                        embedBuilder.setDescription(builder.toString());

                        final Optional<User> userOp = event.getMessageAuthor().asUser();
                        userOp.ifPresent(user -> user.sendMessage(embedBuilder));
                        break;
                    case "levels":
                    case "level":
                        if(dataCache.isEmpty()) {
                            LoggerUtils.logDebug(String.format("CMD: %s No Cached auth data?", AuthCmd.class.getName()));
                            cacheLevels();
                        }

                        embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("Authority");
                        embedBuilder.setDescription(authorityCache);
                        event.getChannel().sendMessage(embedBuilder);
                        break;
                    default:
                        event.getChannel().sendMessage("Invalid arguments. Use -help auth.");
                        break;
                }
                break;
            case 3:
                switch (args[1].toLowerCase(Locale.ROOT)) {
                    case "remove":
                    case "del":
                    case "d":
                        if(args[2].isEmpty()) {
                            event.getChannel().sendMessage("Invalid arguments. Use -help auth.");
                            return;
                        }

                        final String userID = idCleanup(args[2]);
                        if(!isLong(userID)) {
                            event.getChannel().sendMessage("Invalid arguments. Use @ or users ID.");
                            return;
                        }
                        final long uid = Long.parseLong(userID);

                        final User user;
                        final Optional<User> userOptional = discordBotCLI.getApi().getCachedUserById(uid);
                        if(!userOptional.isPresent()) {
                            try {
                                user = discordBotCLI.getApi().getUserById(uid).get();
                            } catch (ExecutionException | InterruptedException exception) {
                                LoggerUtils.logDebug(AuthCmd.class.getName() + exception);
                                return;
                            }
                        } else user = userOptional.get();

                        switch(discordBotCLI.getAuth().removeUser(uid)) {
                            case FAILURE:
                                event.getChannel().sendMessage(String.format("Failed to remove %s.", user.getName()));
                                break;
                            case SUCCESS:
                                event.getChannel().sendMessage(String.format("User: %s has been removed.", user.getName()));
                                break;
                        }
                        break;
                }
                break;
            case 4:
                switch (args[1].toLowerCase(Locale.ROOT)) {
                    case "add":
                    case "a":
                        if(args[2].isEmpty() || args[3].isEmpty()) {
                            event.getChannel().sendMessage("Invalid arguments. Use -help auth.");
                            return;
                        }

                        final String userID = idCleanup(args[2]);
                        if(!isLong(userID)) {
                            event.getChannel().sendMessage("Invalid arguments. Use @ or users ID.");
                            return;
                        }
                        final long uid = Long.parseLong(userID);

                        final User user;
                        final Optional<User> userOptional = discordBotCLI.getApi().getCachedUserById(uid);
                        if(!userOptional.isPresent()) {
                            try {
                                user = discordBotCLI.getApi().getUserById(uid).get();
                            } catch (ExecutionException | InterruptedException exception) {
                                LoggerUtils.logDebug(AuthCmd.class.getName() + exception);
                                return;
                            }
                        } else user = userOptional.get();

                        final Authority auth = stringToEnum(Authority.class, args[3]);
                        if(auth == null) {
                            event.getChannel().sendMessage("Invalid Authority arguments. Use -auth levels.");
                            return;
                        }

                        switch(discordBotCLI.getAuth().addUser(new Pair<>(uid, auth))) {
                            case FAILURE:
                                event.getChannel().sendMessage(String.format("Failed to give %s %s.", user.getName(), auth));
                                break;
                            case SUCCESS:
                                event.getChannel().sendMessage(String.format("User: %s has been given %s.", user.getName(), auth));
                                break;
                        }
                        break;
                    default:
                        event.getChannel().sendMessage("Invalid arguments. Use -help auth.");
                        break;
                }
                break;
            default:
                event.getChannel().sendMessage("Invalid arguments. Use -help auth.");
                break;
        }
    }

    // * Events

    @EventListener
    public void onRegCompleteEvent(RegCompleteEvent event) {
        cacheData();
        cacheLevels();
    }

    @EventListener
    public void onUserAddRemove(UserAddRemoveEvent event) {
        cacheData();
    }

    // * Method

    private void cacheLevels() {
        final StringBuilder stringBuilder = new StringBuilder();
        for(Authority authority : Authority.values()) {
            stringBuilder.append(authority.toString()).append(" ");
        }
        authorityCache = stringBuilder.toString();
    }

    private void cacheData () {
        dataCache.clear();
        final AuthManagement authManagement = discordBotCLI.getAuth();
        if(authManagement == null) {
            LoggerUtils.logDebug("No AuthManagement?");
            return;
        }
        final HashSet<Pair<Long, Authority>> userData = authManagement.getUserList();
        if(userData.isEmpty()) {
            LoggerUtils.logDebug("No user data?");
            return;
        }
        for(Pair<Long, Authority> pair : userData) {
            String username;
            final User user;
            final Optional<User> userOptional = discordBotCLI.getApi().getCachedUserById(pair.getFirst());
            if(!userOptional.isPresent()) {
                try {
                    user = discordBotCLI.getApi().getUserById(pair.getFirst()).get();
                } catch (ExecutionException | InterruptedException exception) {
                    LoggerUtils.logDebug(AuthCmd.class.getName() + exception);
                    return;
                }
            } else user = userOptional.get();
            username = user.getName();
            dataCache.add(String.format("User: %s Auth: %s", username, pair.getSecond().toString()));
        }
    }
}
