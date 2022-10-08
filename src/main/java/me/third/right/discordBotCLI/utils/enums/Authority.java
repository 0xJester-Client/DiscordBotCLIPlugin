package me.third.right.discordBotCLI.utils.enums;

public enum Authority {
    OWNER(2),
    ADMIN(1),
    NONE(0),
    BLOCKED(-1);

    private final int powerLevel;
    Authority(int powerLevel) {
        this.powerLevel = powerLevel;
    }

    public int getPowerLevel() {
        return powerLevel;
    }
}
