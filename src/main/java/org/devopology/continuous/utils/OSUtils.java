package org.devopology.continuous.utils;

/**
 * Created by Doug on 11/6/2016.
 */
public class OSUtils {

    private static String OS = null;

    public static String getOsName()
    {
        if (OS == null) {
            OS = System.getProperty("os.name");
        }

        return OS;
    }

    public static boolean isWindows()
    {
        return getOsName().startsWith("Windows");
    }
}
