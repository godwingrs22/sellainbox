package it.sellainbox.connection;

import android.content.Context;

import it.sellainbox.cache.SellaCache;

/**
 * Created by GodwinRoseSamuel on 8/21/2015.
 */
public class ServerConfig {
    private final static String HTTP_PROTOCOL = "http";
    private final static String HOSTNAME = "192.168.0.101";
    private final static String PORT_NO = "8080";
    private final static String NOTIFICATION_API = "/biot/rest/notifications";
    private final static String CHECKINOUT_API = "/biot/rest/notifications";
    private final static String REGISTER_API = "/biot/rest/notifications/user";
    private final static String FEEDBACK_API = "/biot/rest/notifications";
    private final static String IMAGE_API = "/biot/static/images";

    public static String getServerURL(Context context) {

        return HTTP_PROTOCOL + "://" + SellaCache.getCache("ipAddress", "192.168.0.105", context) + ":" + PORT_NO;
    }

    public static String getNotificationURL(Context context) {
        return HTTP_PROTOCOL + "://" + SellaCache.getCache("ipAddress", "192.168.0.105", context) + ":" + PORT_NO + NOTIFICATION_API;
    }

    public static String getCheckInOutURL(Context context) {
        return HTTP_PROTOCOL + "://" + SellaCache.getCache("ipAddress", "192.168.0.105", context) + ":" + PORT_NO + CHECKINOUT_API;
    }

    public static String getRegisterURL(Context context) {
        return HTTP_PROTOCOL + "://" + SellaCache.getCache("ipAddress", "192.168.0.105", context) + ":" + PORT_NO + REGISTER_API;
    }

    public static String getFeedbackURL(Context context) {
        return HTTP_PROTOCOL + "://" + SellaCache.getCache("ipAddress", "192.168.0.105", context) + ":" + PORT_NO + FEEDBACK_API;
    }
    public static String getImageURL(Context context) {
        return HTTP_PROTOCOL + "://" + SellaCache.getCache("ipAddress", "192.168.0.105", context) + ":" + PORT_NO + IMAGE_API;
    }
}
