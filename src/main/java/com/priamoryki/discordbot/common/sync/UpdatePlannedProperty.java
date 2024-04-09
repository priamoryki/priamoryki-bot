package com.priamoryki.discordbot.common.sync;

/**
 * @author Pavel Lymar
 */
public final class UpdatePlannedProperty {
    private static boolean planned = false;

    private UpdatePlannedProperty() {

    }

    public static boolean getPlanned() {
        return planned;
    }

    public static void setPlanned(boolean isPlanned) {
        planned = isPlanned;
    }
}
