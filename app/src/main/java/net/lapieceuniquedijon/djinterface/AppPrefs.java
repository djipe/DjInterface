package net.lapieceuniquedijon.djinterface;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Accès centralisé aux préférences de l'application.
 * Clés stockées dans SharedPreferences "dj_prefs".
 */
public class AppPrefs {

    private static final String PREFS_NAME        = "dj_prefs";

    // Notifications
    public static final String KEY_NOTIF_ENABLED  = "notif_enabled";
    public static final String KEY_NOTIF_DELAY    = "notif_delay_min";   // int
    public static final String KEY_NOTIF_TYPE     = "notif_type";         // String: vibration|sound|both|visual

    // Snooze
    public static final String KEY_SNOOZE_UNTIL   = "snooze_until_ms";   // long, 0 = pas de snooze

    // Valeurs par défaut
    public static final boolean DEFAULT_NOTIF_ENABLED = true;
    public static final int     DEFAULT_NOTIF_DELAY   = 3;   // minutes
    public static final String  DEFAULT_NOTIF_TYPE    = "vibration";

    // URL de l'API (modifiable ici si le domaine change)
    public static final String API_BASE_URL = "https://lapieceuniquedijon.great-site.net/spotify/dj_api.php";
    public static final String SPOTIFY_CLIENT_ID = "bc3aa6e32833442294930bc5acfc1a6a";
    public static final String SPOTIFY_REDIRECT_URI = "https://lapieceuniquedijon.great-site.net/spotify/spotify-callback.php";

    // -------------------------------------------------------------------------

    private final SharedPreferences prefs;

    public AppPrefs(Context context) {
        this.prefs = context.getApplicationContext()
                            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // --- Notifications ---

    public boolean isNotifEnabled() {
        return prefs.getBoolean(KEY_NOTIF_ENABLED, DEFAULT_NOTIF_ENABLED);
    }

    public void setNotifEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply();
    }

    public int getNotifDelayMin() {
        return prefs.getInt(KEY_NOTIF_DELAY, DEFAULT_NOTIF_DELAY);
    }

    public void setNotifDelayMin(int minutes) {
        prefs.edit().putInt(KEY_NOTIF_DELAY, minutes).apply();
    }

    public String getNotifType() {
        return prefs.getString(KEY_NOTIF_TYPE, DEFAULT_NOTIF_TYPE);
    }

    public void setNotifType(String type) {
        prefs.edit().putString(KEY_NOTIF_TYPE, type).apply();
    }

    // --- Snooze ---

    /**
     * @return timestamp en ms jusqu'auquel le snooze est actif, 0 si inactif.
     */
    public long getSnoozeUntil() {
        return prefs.getLong(KEY_SNOOZE_UNTIL, 0L);
    }

    public void setSnoozeUntil(long timestampMs) {
        prefs.edit().putLong(KEY_SNOOZE_UNTIL, timestampMs).apply();
    }

    /**
     * Active le snooze pour 12 heures à partir de maintenant.
     */
    public void activateSnooze12h() {
        long until = System.currentTimeMillis() + 12L * 60 * 60 * 1000;
        setSnoozeUntil(until);
    }

    /**
     * Désactive le snooze.
     */
    public void clearSnooze() {
        prefs.edit().putLong(KEY_SNOOZE_UNTIL, 0L).apply();
    }

    /**
     * @return true si le snooze est actuellement actif.
     */
    public boolean isSnoozed() {
        long until = getSnoozeUntil();
        return until > 0 && System.currentTimeMillis() < until;
    }
}
