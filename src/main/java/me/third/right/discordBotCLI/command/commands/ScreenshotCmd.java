package me.third.right.discordBotCLI.command.commands;

import me.bush.eventbus.annotation.EventListener;
import me.third.right.ThirdMod;
import me.third.right.discordBotCLI.command.Cmd;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.events.client.TickEvent;
import me.third.right.utils.client.utils.FileUtils;
import me.third.right.utils.client.utils.LoggerUtils;
import net.minecraft.util.ScreenShotHelper;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Cmd.CmdInfo(name = "screenshot", description = "Takes a screenshot and sends it to you.", authority = Authority.ADMIN)
public class ScreenshotCmd extends Cmd {
    private boolean isTakingScreenshot = false;
    private CompletableFuture<File> future = new CompletableFuture<>();

    //The reason It's so hacky is that OpenGL can only be used on the main thread. So we have to use the tick event to take the screenshot as it's executed by main thread.
    @Override
    public void onMessage(MessageCreateEvent event, String[] args) {
        if(isTakingScreenshot) {
            event.getChannel().sendMessage("I'm already taking a screenshot!");
            return;
        }
        isTakingScreenshot = true;
        future = new CompletableFuture<>();
        LoggerUtils.moduleLog(discordBotCLI, String.format("Screenshot taken by %s", event.getMessageAuthor().getDiscriminatedName()));
        ThirdMod.EVENT_PROCESSOR.subscribe(this);

        while (!future.isDone()) {//Prob not needed so more testing is needed.
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final File image = future.join();
        if (image.exists()) {
            event.getChannel().sendMessage("Here's the Screenshotty bro.", image);
            LoggerUtils.moduleLog(discordBotCLI, String.format("Screenshot sent to %s", event.getMessageAuthor().getDiscriminatedName()));
        } else {
            event.getChannel().sendMessage("Something went wrong, I couldn't find the screenshot.");
            LoggerUtils.moduleLog(discordBotCLI, String.format("Screenshot failed to send to %s", event.getMessageAuthor().getDiscriminatedName()));
        }
        image.deleteOnExit();
    }


    @EventListener
    public void onTick(TickEvent event) {
        final Path file = discordBotCLI.tempPath;
        FileUtils.folderExists(file);
        ScreenShotHelper.saveScreenshot(file.toFile(), "TEMPSCREEN.png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
        future.complete(file.resolve("screenshots").resolve("TEMPSCREEN.png").toFile());
        ThirdMod.EVENT_PROCESSOR.unsubscribe(this);
        isTakingScreenshot = false;
    }
}
