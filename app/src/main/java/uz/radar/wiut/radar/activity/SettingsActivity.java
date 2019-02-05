package uz.radar.wiut.radar.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Locale;

import uz.radar.wiut.radar.MainActivity;
import uz.radar.wiut.radar.MyApplication;
import uz.radar.wiut.radar.R;
import uz.radar.wiut.radar.utils.Const;
import uz.radar.wiut.radar.utils.CustomUtils;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, Const {

    private LinearLayout language;
    private ToggleButton notification;
    private MyApplication app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        checkLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        language = (LinearLayout) findViewById(R.id.language);
        language.setOnClickListener(this);
        findViewById(R.id.back_image).setOnClickListener(this);
        notification = (ToggleButton) findViewById(R.id.notification);

        app = (MyApplication) getApplication();
    }

    private void checkLanguage() {
        if (UZBEK.equals(CustomUtils.getSharedPreferencesString(this, LANGUAGE))) {
            CustomUtils.getSharedPreferencesString(this, LANGUAGE);

            Configuration conf = getResources().getConfiguration();
            conf.locale = new Locale(UZBEK);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Resources resources = new Resources(getAssets(), metrics, conf);
        } else if (RUSSIAN.equals(CustomUtils.getSharedPreferencesString(SettingsActivity.this, LANGUAGE))) {
            CustomUtils.getSharedPreferencesString(this, LANGUAGE);

            Configuration conf = getResources().getConfiguration();
            conf.locale = new Locale(RUSSIAN);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Resources resources = new Resources(getAssets(), metrics, conf);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.language:
                new AlertDialog.Builder(SettingsActivity.this).setItems(R.array.languages, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                setLanguage(RUSSIAN);

                                break;
                            case 1:
                                setLanguage(UZBEK);
                                break;
                        }
                    }
                }).setTitle(R.string.select_language).setNegativeButton(R.string.cancel, null).create().show();
                break;
            case R.id.notification:
                if (notification.isChecked()) {
                    Toast.makeText(SettingsActivity.this, "ON", Toast.LENGTH_SHORT).show();
                    CustomUtils.putSharedPrefBoolean(SettingsActivity.this, NOTIFICATION, true);
                } else {
                    Toast.makeText(SettingsActivity.this, "OFF", Toast.LENGTH_SHORT).show();
                    CustomUtils.putSharedPrefBoolean(SettingsActivity.this, NOTIFICATION, false);

                }
                break;
            case R.id.back_image:
                onBackPressed();
                break;
        }

    }

    private void setLanguage(String language) {
        if (language.equals(CustomUtils.getSharedPreferencesString(SettingsActivity.this, LANGUAGE))) {
            return;
        }
        if (app.initDefaults(language)) {
            CustomUtils.putSharedPrefString(this, LANGUAGE, language);
            refreshUI();
        }
    }


    public void refreshUI() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}