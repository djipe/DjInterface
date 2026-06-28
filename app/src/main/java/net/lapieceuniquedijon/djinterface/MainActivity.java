package net.lapieceuniquedijon.djinterface;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.lapieceuniquedijon.djinterface.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String WORK_TAG = "dj_poll_work";
    private ActivityMainBinding binding;
    private RequestAdapter adapter;
    private final List<TrackRequest> requestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter(requestList, new RequestAdapter.OnActionListener() {
            @Override
            public void onPlay(TrackRequest request, int position) {
                handleAction(request, "Joue", position);
            }
            @Override
            public void onSkip(TrackRequest request, int position) {
                handleAction(request, "Ignore", position);
            }
        });
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setColorSchemeResources(R.color.spotify_green);
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.bg_card);
        binding.swipeRefresh.setOnRefreshListener(new androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadRequests();
            }
        });

        binding.btnSpotifyAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpotifyAuth();
            }
        });
        binding.btnClearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmClear();
            }
        });

        NotificationHelper.createChannel(this);
        schedulePollWorker();

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        loadRequests();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadRequests();
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadRequests() {
        binding.swipeRefresh.setRefreshing(true);
        binding.tvEmpty.setVisibility(View.GONE);

        ApiClient.getInstance().fetchRequests(new ApiClient.RequestsCallback() {
            @Override
            public void onSuccess(final List<TrackRequest> requests) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.swipeRefresh.setRefreshing(false);
                        requestList.clear();
                        requestList.addAll(requests);
                        adapter.notifyDataSetChanged();
                        binding.tvEmpty.setVisibility(
                                requests.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }
            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        if (requestList.isEmpty()) {
                            binding.tvEmpty.setText(message);
                            binding.tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    private void handleAction(final TrackRequest request, final String newStatus,
                              final int position) {
        ApiClient.getInstance().updateStatus(
                request.getTimestamp(), request.getIp(), newStatus, request.getSpotifyId(),
                new ApiClient.ActionCallback() {
                    @Override
                    public void onSuccess(final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        message, Toast.LENGTH_SHORT).show();
                                adapter.removeAt(position);
                                if (requestList.isEmpty())
                                    binding.tvEmpty.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    @Override
                    public void onError(final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.resetAlphaAt(position);
                                Toast.makeText(MainActivity.this,
                                        "Erreur : " + message, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
        );
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_clear_title)
                .setMessage(R.string.confirm_clear_message)
                .setPositiveButton(R.string.yes, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface d, int w) {
                        doClearList();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void doClearList() {
        ApiClient.getInstance().clearLog(new ApiClient.ActionCallback() {
            @Override
            public void onSuccess(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadRequests();
                    }
                });
            }
            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "Erreur : " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void startSpotifyAuth() {
        ApiClient.getInstance().clearDjToken(new ApiClient.ActionCallback() {
            @Override
            public void onSuccess(String m) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { openSpotifyOAuth(); }
                });
            }
            @Override
            public void onError(String m) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { openSpotifyOAuth(); }
                });
            }
        });
    }

    private void openSpotifyOAuth() {
        String scopes = "user-modify-playback-state user-read-playback-state " +
                "user-read-currently-playing user-read-recently-played";
        String url = "https://accounts.spotify.com/authorize" +
                "?response_type=code" +
                "&client_id=" + Uri.encode(AppPrefs.SPOTIFY_CLIENT_ID) +
                "&scope=" + Uri.encode(scopes) +
                "&redirect_uri=" + Uri.encode(AppPrefs.SPOTIFY_REDIRECT_URI);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void schedulePollWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest workReq = new PeriodicWorkRequest.Builder(
                PollWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag(WORK_TAG)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, workReq);
    }
}
