package cz.tomasdvorak.users;

import cz.tomasdvorak.myapp.settings.SystemConstants;
import cz.tomasdvorak.myapp.settings.UserConstants;

/**
 * This class demonstrates usage of the generated Constants classes. All of those constants are available during
 * compile time, with correct type. If any of those constants is missing or defined as not expected data type,
 * UserUtils cannot be compiled.
 */
public class UserUtils {
    public static String getHomeDirectory(String username) {
        return SystemConstants.HOME_ROOT + "/" + username.toLowerCase();
    }

    public static double getMaxMemory() {
        if(UserConstants.BOOST_ENABLED) {
            return SystemConstants.MAX_MEMORY * UserConstants.BOOST;
        } else {
            return SystemConstants.MAX_MEMORY;
        }
    }

    public static double getDiskQuota() {
        if(UserConstants.BOOST_ENABLED) {
            return SystemConstants.DISK_QUOTA * UserConstants.BOOST;
        } else {
            return SystemConstants.DISK_QUOTA;
        }

//        int memory = Config.getInstance().getIntValue("SystemConstants", "MAX_MEMORY");
    }
}
