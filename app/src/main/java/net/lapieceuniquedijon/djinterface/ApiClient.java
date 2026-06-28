package net.lapieceuniquedijon.djinterface;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static ApiClient instance;
    private final OkHttpClient http;
    private final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private ApiClient() {
        http = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    public interface RequestsCallback {
        void onSuccess(List<TrackRequest> requests);
        void onError(String message);
    }

    public void fetchRequests(final RequestsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request req = new Request.Builder()
                        .url(AppPrefs.API_BASE_URL)
                        .build();
                try {
                    Response response = http.newCall(req).execute();
                    if (!response.isSuccessful()) {
                        callback.onError("Erreur HTTP " + response.code());
                        response.close();
                        return;
                    }
                    String body = response.body() != null ? response.body().string() : "[]";
                    response.close();
                    Type listType = new TypeToken<List<TrackRequest>>(){}.getType();
                    List<TrackRequest> list = gson.fromJson(body, listType);
                    callback.onSuccess(list);
                } catch (IOException e) {
                    callback.onError("Impossible de joindre le serveur : " + e.getMessage());
                }
            }
        }).start();
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String message);
    }

    public void updateStatus(final String timestamp, final String ip,
                             final String newStatus, final String spotifyId,
                             final ActionCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("action", "update_status");
                    json.put("timestamp", timestamp);
                    json.put("ip", ip);
                    json.put("newStatus", newStatus);
                    json.put("spotifyId", spotifyId);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request req = new Request.Builder()
                            .url(AppPrefs.API_BASE_URL)
                            .post(body)
                            .build();
                    Response response = http.newCall(req).execute();
                    String respBody = response.body() != null ? response.body().string() : "{}";
                    response.close();
                    JSONObject result = new JSONObject(respBody);
                    if ("success".equals(result.optString("status"))) {
                        callback.onSuccess(result.optString("message", "OK"));
                    } else {
                        callback.onError(result.optString("message", "Erreur inconnue"));
                    }
                } catch (Exception e) {
                    callback.onError("Erreur reseau : " + e.getMessage());
                }
            }
        }).start();
    }

    public void clearLog(final ActionCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("action", "clear_log");
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request req = new Request.Builder()
                            .url(AppPrefs.API_BASE_URL)
                            .post(body)
                            .build();
                    Response response = http.newCall(req).execute();
                    String respBody = response.body() != null ? response.body().string() : "{}";
                    response.close();
                    JSONObject result = new JSONObject(respBody);
                    if ("success".equals(result.optString("status"))) {
                        callback.onSuccess(result.optString("message", "Liste effacee"));
                    } else {
                        callback.onError(result.optString("message", "Erreur"));
                    }
                } catch (Exception e) {
                    callback.onError("Erreur reseau : " + e.getMessage());
                }
            }
        }).start();
    }

    public void clearDjToken(final ActionCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("action", "clear_dj_token");
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request req = new Request.Builder()
                            .url(AppPrefs.API_BASE_URL)
                            .post(body)
                            .build();
                    Response response = http.newCall(req).execute();
                    response.close();
                    callback.onSuccess("Token supprime");
                } catch (Exception e) {
                    callback.onError("Erreur reseau : " + e.getMessage());
                }
            }
        }).start();
    }
}
