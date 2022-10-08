package me.third.right.discordBotCLI.events;

import me.bush.eventbus.event.Event;

public class RegCompleteEvent extends Event {
    @Override
    protected boolean isCancellable() {
        return false;
    }
}
