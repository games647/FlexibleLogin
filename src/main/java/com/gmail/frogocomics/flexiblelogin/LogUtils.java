package com.gmail.frogocomics.flexiblelogin;

import com.github.games647.flexiblelogin.FlexibleLogin;

/**
 *
 *
 * @author Jeff Chen
 */
public class LogUtils {

    private LogUtils() {
    }

    public static void logException(Exception e) {
        FlexibleLogin.getInstance().getLogger().error("A exception has been caught.", e);
    }
}
