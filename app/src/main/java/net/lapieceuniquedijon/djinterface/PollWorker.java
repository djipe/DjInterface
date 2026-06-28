package net.lapieceuniquedijon.djinterface;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PollWorker extends Worker {

    private static final String TAG = "PollWorker";
    private static final SimpleDateFormat TS_FMT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);

    public PollWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        AppPrefs prefs = new AppPrefs(ctx);

        if (!prefs.isNotifEnabled() || prefs.isSnoozed()) {
            return Result.success();
        }

        int delayMin = prefs.getNotifDelayMin();
        String notifType = prefs.getNotifType();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Request req = new Request.Builder().url(AppPrefs.API_BASE_URL).build();

        List<TrackRequest> requests;
        try {
            Response response = client.newCall(req).execute();
            if (!response.isSuccessful() || response.body() == null) {
                return Result.retry();
            }
            String body = response.body().string();
            response.close();
            Type listType = new TypeToken<List<TrackRequest>>(){}.getType();
            requests = new Gson().fromJson(body, listType);
        } catch (IOException e) {
            Log.w(TAG, "Echec polling : " + e.getMessage());
            return Result.retry();
        }

        if (requests == null || requests.isEmpty()) {
            return Result.success();
        }

        long now = System.currentTimeMillis();
        long thresholdMs = TimeUnit.MINUTES.toMillis(delayMin);
        int overdue = 0;

        for (TrackRequest r : requests) {
            if (!"Demande".equals(r.getStatus()) && !"Demandé".equals(r.getStatus())) continue;
            String ts = r.getTimestamp();
            if (ts == null || ts.isEmpty()) continue;
            try {
                Date requestDate = TS_FMT.parse(ts);
                if (requestDate != null && (now - requestDate.getTime()) >= thresholdMs) {
                    overdue++;
                }
            } catch (ParseException e) {
                Log.w(TAG, "Timestamp invalide : " + ts);
            }
        }

        if (overdue > 0) {
            NotificationHelper.sendNotification(ctx, overdue, delayMin, notifType);
        }

        return Result.success();
    }
}
