package net.lapieceuniquedijon.djinterface;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "dj_requests_channel";
    private static final int NOTIF_ID = 1001;

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(context.getString(R.string.notif_channel_desc));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 400, 200, 400});
            NotificationManager nm = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    public static void sendNotification(Context context, int pendingCount,
                                        int delayMin, String notifType) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // PendingIntent compatible toutes versions
        int piFlags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, piFlags);

        String title = context.getString(R.string.notif_title);
        String text  = context.getString(R.string.notif_text, pendingCount, delayMin);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if ("sound".equals(notifType) || "both".equals(notifType)) {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(soundUri);
        } else {
            builder.setSound(null);
        }

        if ("vibration".equals(notifType) || "both".equals(notifType)) {
            builder.setVibrate(new long[]{0, 400, 200, 400});
        }

        try {
            NotificationManagerCompat nm = NotificationManagerCompat.from(context);
            nm.notify(NOTIF_ID, builder.build());
        } catch (SecurityException e) {
            // permission non accordée
        }

        if ("vibration".equals(notifType) || "both".equals(notifType)) {
            triggerVibration(context);
        }
    }

    private static void triggerVibration(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;
        long[] pattern = {0, 400, 200, 400};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            v.vibrate(pattern, -1);
        }
    }
}
