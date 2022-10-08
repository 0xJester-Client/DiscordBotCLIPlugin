package me.third.right.discordBotCLI.utils;

import java.io.File;
import java.nio.file.Path;

/**
 * For universal Methods used across multiple classes.
 */
public class Tools {

    public static File[] getFilesInDirectory(Path path) {
        return path.toFile().listFiles();
    }

    public static  <E extends Enum<E>> E stringToEnum(Class<E> eClass, String val) {
        if(eClass != null && val != null) {
            try {
                return Enum.valueOf(eClass, val.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                //TODO add error handling.
            }
        }
        return null;
    }

    public static String idCleanup(String id) {
        if(id.contains("<@") && id.contains(">")) {
            id = id.replaceAll("<@", "");
            id = id.replaceAll(">", "");
        }
        return id;
    }
}
