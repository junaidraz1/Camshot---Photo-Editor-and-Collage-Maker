package com.miczon.photoeditor;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.miczon.photoeditor.helper.PrefsManager;

import java.util.ArrayList;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    String TAG = "BaseLocaleActivity";
    PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(BaseActivity.this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        initializeAdMob();
        prefsManager = new PrefsManager(BaseActivity.this);
        setAppLanguage(prefsManager.getSelectedLanguage());

        Log.e(TAG, "onCreate: locale: " + getLocale());

    }

    @Override
    protected void onResume() {
        super.onResume();
        String selectedLanguage = prefsManager.getSelectedLanguage();
        if (!getLocale().getLanguage().equals(selectedLanguage)) {
            setAppLanguage(selectedLanguage);
        }

        Log.e(TAG, "onResume: locale: " + getLocale());
    }

    /**
     * Method to initialize Ad Mob
     */
    public void initializeAdMob() {
        MobileAds.initialize(this, initializationStatus -> {
            ArrayList<String> testDeviceIds = new ArrayList<>();
            testDeviceIds.add("D8258FEBB3EE361B3D889A4064C01567");
            RequestConfiguration configuration = new RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds) // Add your test device ID here
//                    .setTestDeviceIds(List.of("ca-app-pub-3940256099942544/6300978111")) // Add your test device ID here
                    .build();
            MobileAds.setRequestConfiguration(configuration);
        });
    }

    /**
     * To change app's language
     *
     * @param languageCode: language code of language that is to be changed
     */
    protected void setAppLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, displayMetrics);
    }

    /**
     * get current selected language of app
     *
     * @return: Locale
     */
    protected Locale getLocale() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.getLocales().get(0);
    }
}