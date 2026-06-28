package net.lapieceuniquedijon.djinterface;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private AppPrefs prefs;
    private SwitchMaterial switchNotifEnabled;
    private SeekBar seekBarDelay;
    private TextView tvDelayValue;
    private RadioGroup radioGroupNotifType;
    private SwitchMaterial switchSnooze;
    private TextView tvSnoozeUntil;

    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("HH:mm", Locale.FRANCE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new AppPrefs(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        switchNotifEnabled  = findViewById(R.id.switchNotifEnabled);
        seekBarDelay        = findViewById(R.id.seekBarDelay);
        tvDelayValue        = findViewById(R.id.tvDelayValue);
        radioGroupNotifType = findViewById(R.id.radioGroupNotifType);
        switchSnooze        = findViewById(R.id.switchSnooze);
        tvSnoozeUntil       = findViewById(R.id.tvSnoozeUntil);

        loadPrefs();

        switchNotifEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean checked) {
                prefs.setNotifEnabled(checked);
                updateControlsEnabled(checked);
            }
        });

        seekBarDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                int minutes = progress + 3;
                tvDelayValue.setText(minutes + " min");
                if (fromUser) prefs.setNotifDelayMin(minutes);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        radioGroupNotifType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                prefs.setNotifType(radioIdToType(checkedId));
            }
        });

        switchSnooze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean checked) {
                if (checked) {
                    prefs.activateSnooze12h();
                    updateSnoozeLabel();
                } else {
                    prefs.clearSnooze();
                    tvSnoozeUntil.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadPrefs() {
        boolean notifEnabled = prefs.isNotifEnabled();
        switchNotifEnabled.setChecked(notifEnabled);
        int delay = prefs.getNotifDelayMin();
        seekBarDelay.setProgress(delay - 3);
        tvDelayValue.setText(delay + " min");
        radioGroupNotifType.check(typeToRadioId(prefs.getNotifType()));
        boolean snoozed = prefs.isSnoozed();
        switchSnooze.setChecked(snoozed);
        if (snoozed) updateSnoozeLabel();
        updateControlsEnabled(notifEnabled);
    }

    private void updateControlsEnabled(boolean enabled) {
        seekBarDelay.setEnabled(enabled);
        tvDelayValue.setAlpha(enabled ? 1f : 0.4f);
        for (int i = 0; i < radioGroupNotifType.getChildCount(); i++) {
            radioGroupNotifType.getChildAt(i).setEnabled(enabled);
        }
        switchSnooze.setEnabled(enabled);
    }

    private void updateSnoozeLabel() {
        long until = prefs.getSnoozeUntil();
        if (until > 0) {
            String timeStr = TIME_FMT.format(new Date(until));
            tvSnoozeUntil.setText(getString(R.string.snooze_active_until, timeStr));
            tvSnoozeUntil.setVisibility(View.VISIBLE);
        }
    }

    private String radioIdToType(int id) {
        if (id == R.id.radioSound)  return "sound";
        if (id == R.id.radioBoth)   return "both";
        if (id == R.id.radioVisual) return "visual";
        return "vibration";
    }

    private int typeToRadioId(String type) {
        if (type == null) return R.id.radioVibration;
        switch (type) {
            case "sound":  return R.id.radioSound;
            case "both":   return R.id.radioBoth;
            case "visual": return R.id.radioVisual;
            default:       return R.id.radioVibration;
        }
    }
}
