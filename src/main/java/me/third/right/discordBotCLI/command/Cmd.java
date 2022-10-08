package me.third.right.discordBotCLI.command;

import me.third.right.discordBotCLI.hacks.DiscordBotCLI;
import me.third.right.discordBotCLI.utils.enums.Authority;
import net.minecraft.client.Minecraft;
import org.javacord.api.event.message.MessageCreateEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class Cmd {
    protected final Minecraft mc = Minecraft.getMinecraft();
    protected final DiscordBotCLI discordBotCLI = DiscordBotCLI.INSTANCE;

    private final String[] name;
    private final String description;
    private final Authority authority;

    public Cmd() {
        name = getInfo().name();
        description = getInfo().description();
        authority = getInfo().authority();
    }


    public abstract void onMessage(MessageCreateEvent event, String[] args);

    private CmdInfo getInfo() {
        if(getClass().isAnnotationPresent(CmdInfo.class)) {
            return getClass().getAnnotation(CmdInfo.class);
        }
        throw new IllegalStateException("Issued Caused by "+getClass().getCanonicalName());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface CmdInfo {
        String[] name();
        String description();
        Authority authority() default Authority.NONE;
    }

    public String getNameDisplay() {
        return name[0];
    }

    public String[] getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Authority getAuthority() {return authority;}
}
