package com.gmail.frogocomics.flexiblelogin;

import com.github.games647.flexiblelogin.FlexibleLogin;

import org.apache.commons.lang3.exception.ExceptionUtils;

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

    @Deprecated
    public static void logError(Error e) {
        FlexibleLogin.getInstance().getLogger().error("A error: " + e.getClass().getName() + ", has been found. The full error stack trace is as follows:");
        FlexibleLogin.getInstance().getLogger().trace(ExceptionUtils.getStackTrace(e));
    }
}
