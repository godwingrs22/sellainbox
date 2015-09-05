package it.sellainbox.connection;

/**
 * Created by GodwinRoseSamuel on 8/21/2015.
 */
public class ServerConfig {
    private final static String HTTP_PROTOCOL = "http";
    private final static String HOSTNAME = "192.168.0.100";
    private final static String PORT_NO = "8080";
    private final static String NOTIFICATION_API = "/biot/rest/notifications";
    private final static String IMAGE_API = "/biot/static/images/";

    public static String getServerURL() {
        return HTTP_PROTOCOL + "://" + HOSTNAME + ":" + PORT_NO;
    }

    public static String getNotificationURL() {
        return HTTP_PROTOCOL + "://" + HOSTNAME + ":" + PORT_NO + NOTIFICATION_API;
    }

    public static String getImageURL() {
        return HTTP_PROTOCOL + "://" + HOSTNAME + ":" + PORT_NO + IMAGE_API;
    }
//    private final static String HTTP_PROTOCOL = "http";
//    private final static String HOSTNAME = "www.friuno.com";
//    private final static String NOTIFICATION_API = "/sellainbox/announcement.json";
//    private final static String IMAGE_API = "/biot/static/images/";
//
//    public static String getServerURL() {
//        return HTTP_PROTOCOL + "://" + HOSTNAME;
//    }
//
//    public static String getNotificationURL() {
//        return HTTP_PROTOCOL + "://" + HOSTNAME + NOTIFICATION_API;
//    }
//
//    public static String getImageURL() {
//        return HTTP_PROTOCOL + "://" + HOSTNAME + IMAGE_API;
//    }
}
