package me.third.right.discordBotCLI.events;

import me.bush.eventbus.event.Event;
import me.third.right.discordBotCLI.utils.enums.Authority;
import me.third.right.discordBotCLI.utils.enums.Type;
import me.third.right.utils.client.objects.Pair;

public class UserAddRemoveEvent extends Event {

    private long uid;

    private Authority authority;

    private Type type;

    public UserAddRemoveEvent(long uid, Authority authority, Type type) {
        this.uid = uid;
        this.authority = authority;
    }

    public UserAddRemoveEvent(Pair<Long, Authority> pair, Type type) {
        this(pair.getFirst(), pair.getSecond(), type);
    }

    @Override
    protected boolean isCancellable() {
        return false;
    }

    public long getUid() {
        return uid;
    }

    public Authority getAuthority() {
        return authority;
    }

    public Type getType() {
        return type;
    }
}
