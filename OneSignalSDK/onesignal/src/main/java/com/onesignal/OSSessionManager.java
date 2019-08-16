package com.onesignal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Manager in charge to check what type of session is active
 * <p>
 * Types of sessions
 * - Direct: the session occurred due to a push
 * - Indirect: the session occurred on a time frame less than 24hrs
 * - Unattributed: the session was not influenced nor was on the time frame os a push
 */
public class OSSessionManager {

    private static final String TAG = OSSessionManager.class.getCanonicalName();
    private static final long TWENTY_FOUR_HOURS_MILLISECONDS = 24 * 60 * 60 * 1000;

    public enum Session {
        DIRECT,
        INDIRECT,
        UNATTRIBUTED,
        ;

        public static @NonNull
        Session fromString(String value) {
            if (value == null || value.isEmpty())
                return UNATTRIBUTED;

            for (Session type : Session.values()) {
                if (type.name().equalsIgnoreCase(value))
                    return type;
            }
            return UNATTRIBUTED;
        }
    }

    private Session session = null;

    private String notificationId = null;

    public OSSessionManager() {
    }

    public void resetSession() {
        this.session = null;
    }

    @NonNull
    public Session getSession() {
        return session != null ? session : Session.UNATTRIBUTED;
    }

    @Nullable
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Set the type of active session
     */
    void onSessionStarted() {
        if (session != null)
            //session already set
            return;
        String jsonString = NotificationData.getLastNotificationReceivedData();
        if (jsonString != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                long time = jsonObject.getLong(NotificationData.TIME);
                long currentTime = new Date().getTime();
                long difference = currentTime - time;
                if (difference < TWENTY_FOUR_HOURS_MILLISECONDS) {
                    session = Session.INDIRECT;
                    OneSignal.Log(OneSignal.LOG_LEVEL.DEBUG, "Session indirect with notificationId: " + jsonObject.getString(NotificationData.NOTIFICATION_ID));
                } else {
                    onSessionNotInfluenced();
                }
            } catch (JSONException e) {
                OneSignal.Log(OneSignal.LOG_LEVEL.ERROR, "Creating from string notification arrived:JSON Failed.", e);
                onSessionNotInfluenced();
            }
        } else {
            onSessionNotInfluenced();
        }
    }

    /**
     * Set active session type to {@link Session#UNATTRIBUTED}
     */
    void onSessionNotInfluenced() {
        session = Session.UNATTRIBUTED;
        OneSignal.Log(OneSignal.LOG_LEVEL.DEBUG, "Session not influenced");
    }

    /**
     * Set active session type to {@link Session#DIRECT}
     */
    void onSessionFromNotification(String notificationId) {
        this.session = Session.DIRECT;
        this.notificationId = notificationId;
        OneSignal.Log(OneSignal.LOG_LEVEL.DEBUG, "Session Direct with notificationId: " + notificationId);
    }

}