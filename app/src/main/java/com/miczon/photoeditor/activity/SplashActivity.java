package com.miczon.photoeditor.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.LoadAdError;
import com.miczon.photoeditor.BaseActivity;
import com.miczon.photoeditor.BuildConfig;
import com.miczon.photoeditor.R;
import com.miczon.photoeditor.adapter.CountryAdapter;
import com.miczon.photoeditor.eventListeners.BannerAdLoadListener;
import com.miczon.photoeditor.eventListeners.FragmentClickListener;
import com.miczon.photoeditor.fragments.FreeTrailFragment;
import com.miczon.photoeditor.helper.BannerAdManager;
import com.miczon.photoeditor.helper.DialogHandler;
import com.miczon.photoeditor.helper.PrefsManager;
import com.miczon.photoeditor.utils.Constants;
import com.miczon.photoeditor.utils.InterstitialAdHandler;
import com.miczon.photoeditor.utils.Utility;
import com.sasank.roundedhorizontalprogress.RoundedHorizontalProgressBar;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity implements FragmentClickListener, BannerAdLoadListener {

    String TAG = "SplashActivity", from = "";
    Button continueButton;
    TextView tvLoadingAd;
    ImageView animImage1, animImage2, animImage3;
    RoundedHorizontalProgressBar progressBar;
    FrameLayout fragmentContainer, adContainer;
    RelativeLayout adLayout;

    PrefsManager prefsManager;

    CountDownTimer countDownTimer;

    int selectedLanguagePosition = 0;
    boolean isProgressBarVisible = true;
    boolean showAgreementDialog = false;
    boolean isFragmentVisible = false;

    Intent intent;

    BannerAdManager bannerAdManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        animImage1 = findViewById(R.id.iv_animSplash1);
        animImage2 = findViewById(R.id.iv_animSplash2);
        animImage3 = findViewById(R.id.iv_animSplash3);
        continueButton = findViewById(R.id.btn_continue);
        progressBar = findViewById(R.id.progressBar);
        adLayout = findViewById(R.id.rl_ads);
        adContainer = findViewById(R.id.fl_adContainer);
        fragmentContainer = findViewById(R.id.fl_fragmentContainer);
        tvLoadingAd = findViewById(R.id.tv_loadingAd);

        prefsManager = new PrefsManager(this);
        bannerAdManager = new BannerAdManager(this, this);

        if (!Utility.getInstance().verifyInstallerId(this) && !BuildConfig.DEBUG) {
            progressBar.setVisibility(View.GONE);
            DialogHandler.getInstance().showPiracyCheckerDialog(this, (position, path, action) -> {
                if (position == 0) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (RuntimeException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }

                } else if (position == 1) {
                    finishAffinity();

                }
            });

        } else {
            bannerAdManager.loadBannerAd(adContainer);
            intent = getIntent();
            if (intent == null) {
                Log.e(TAG, "onCreate: Intent is null");
                animateProgressBar();

            } else {
                if (intent.getStringExtra("from") != null) {
                    Log.e(TAG, "onCreate: language: " + prefsManager.getSelectedLanguage());
                    if (intent.getStringExtra("from").equalsIgnoreCase("self") && prefsManager.getPrivacyBit()) {
                        progressBar.setVisibility(View.GONE);
                        continueButton.setVisibility(View.VISIBLE);

                    } else {
                        Log.e(TAG, "onCreate: calling agreement dialog");
                        progressBar.setVisibility(View.GONE);
                        showAgreementDialog();
                    }
                } else {
                    animateProgressBar();
                }
            }

            continueButton.setOnClickListener(v -> {
                isFragmentVisible = true;
                loadTrailFragment();
            });

        }
    }

    /**
     * Method to animate progress bar and hide it after it is completed
     */
    private void animateProgressBar() {
        progressBar.animateProgress(10000, 0, 100);
        countDownTimer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                isProgressBarVisible = false;
                if (prefsManager.getSelectedLanguage() == null || prefsManager.getSelectedLanguage().isEmpty()) {
                    displaySelectLanguageDialog();

                } else if (prefsManager.getPrivacyBit()) {
                    progressBar.setVisibility(View.GONE);
                    continueButton.setVisibility(View.VISIBLE);

                } else if (!prefsManager.getSelectedLanguage().isEmpty()) {
//                    progressBar.setVisibility(View.GONE);
                    showAgreementDialog();
                }
            }
        }.start();
    }

    /**
     * Method to display language selection full screen dialog
     */
    public void displaySelectLanguageDialog() {
        Dialog languageDialog = new Dialog(SplashActivity.this, android.R.style.Theme_Light);
        languageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        languageDialog.setContentView(R.layout.activity_select_language);

        languageDialog.setCancelable(false);

        Button btnSelectLanguage, btnCancel;
        RecyclerView.LayoutManager layoutManager;
        RecyclerView rvCountries;
        RelativeLayout adLayout;
        FrameLayout adContainer;

        CountryAdapter countryAdapter;
        PrefsManager prefsManager;

        rvCountries = languageDialog.findViewById(R.id.rv_countries);
        btnSelectLanguage = languageDialog.findViewById(R.id.btn_accept);
        btnCancel = languageDialog.findViewById(R.id.btn_cancel);
        adContainer = languageDialog.findViewById(R.id.fl_adContainer);
        adLayout = languageDialog.findViewById(R.id.rl_adContainer);

        prefsManager = new PrefsManager(SplashActivity.this);
        layoutManager = new LinearLayoutManager(SplashActivity.this);

        btnSelectLanguage.setVisibility(View.VISIBLE);

        if (!Utility.getInstance().isPremiumActive(this)) {
            adLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
            bannerAdManager.loadBannerAd(adContainer);
        } else {
            adLayout.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
        }

        countryAdapter = new CountryAdapter(this, Utility.getInstance().countryNames(),
                Utility.getInstance().countryFlags(), (position, path, action) -> {

            selectedLanguagePosition = position;
            Log.e(TAG, "itemClick: selected language position: " + position + " " + "name is: " + action);
        });

        rvCountries.setAdapter(countryAdapter);
        rvCountries.setLayoutManager(layoutManager);

        btnSelectLanguage.setOnClickListener(v -> {
            switch (selectedLanguagePosition) {
                case 0:
                    prefsManager.setSelectedLanguage("en");
                    prefsManager.setLangPos(0);
                    break;
                case 1:
                    prefsManager.setSelectedLanguage("af");
                    prefsManager.setLangPos(1);
                    break;
                case 2:
                    prefsManager.setSelectedLanguage("ar");
                    prefsManager.setLangPos(2);
                    break;
                case 3:
                    prefsManager.setSelectedLanguage("zh");
                    prefsManager.setLangPos(3);
                    break;
                case 4:
                    prefsManager.setSelectedLanguage("cs");
                    prefsManager.setLangPos(4);
                    break;
                case 5:
                    prefsManager.setSelectedLanguage("nl");
                    prefsManager.setLangPos(5);
                    break;
                case 6:
                    prefsManager.setSelectedLanguage("fr");
                    prefsManager.setLangPos(6);
                    break;
                case 7:
                    prefsManager.setSelectedLanguage("de");
                    prefsManager.setLangPos(7);
                    break;
                case 8:
                    prefsManager.setSelectedLanguage("el");
                    prefsManager.setLangPos(8);
                    break;
                case 9:
                    prefsManager.setSelectedLanguage("hi");
                    prefsManager.setLangPos(9);
                    break;
                case 10:
                    prefsManager.setSelectedLanguage("in");
                    prefsManager.setLangPos(10);
                    break;
                case 11:
                    prefsManager.setSelectedLanguage("it");
                    prefsManager.setLangPos(11);
                    break;
                case 12:
                    prefsManager.setSelectedLanguage("ja");
                    prefsManager.setLangPos(12);
                    break;
                case 13:
                    prefsManager.setSelectedLanguage("ko");
                    prefsManager.setLangPos(13);
                    break;
                case 14:
                    prefsManager.setSelectedLanguage("ms");
                    prefsManager.setLangPos(14);
                    break;
                case 15:
                    prefsManager.setSelectedLanguage("no");
                    prefsManager.setLangPos(15);
                    break;
                case 16:
                    prefsManager.setSelectedLanguage("fa");
                    prefsManager.setLangPos(16);
                    break;
                case 17:
                    prefsManager.setSelectedLanguage("pt");
                    prefsManager.setLangPos(17);
                    break;
                case 18:
                    prefsManager.setSelectedLanguage("ru");
                    prefsManager.setLangPos(18);
                    break;
                case 19:
                    prefsManager.setSelectedLanguage("es");
                    prefsManager.setLangPos(19);
                    break;
                case 20:
                    prefsManager.setSelectedLanguage("th");
                    prefsManager.setLangPos(20);
                    break;
                case 21:
                    prefsManager.setSelectedLanguage("tr");
                    prefsManager.setLangPos(21);
                    break;
                case 22:
                    prefsManager.setSelectedLanguage("vi");
                    prefsManager.setLangPos(22);
                    break;
                default:
                    break;
            }

            if (Utility.getInstance().isPremiumActive(this)) {
                adLayout.setVisibility(View.GONE);
            }

            Intent intent = new Intent(SplashActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "self");
            startActivity(intent);
            languageDialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            if (prefsManager.getSelectedLanguage() == null || prefsManager.getSelectedLanguage().isEmpty()) {
                prefsManager.setSelectedLanguage("en");
            }
            if (languageDialog.isShowing()) {
                Log.e(TAG, "displaySelectLanguageDialog: if working");
                languageDialog.dismiss();
            } else {
                Log.e(TAG, "displaySelectLanguageDialog: else working");
            }
            showAgreementDialog();
        });

        languageDialog.show();
    }

    /**
     * Method to display agreement dialog
     */
    private void showAgreementDialog() {
        Log.e(TAG, "showAgreementDialog: ");
        progressBar.setVisibility(View.GONE);

        if (Utility.getInstance().isPremiumActive(this)) {
            adLayout.setVisibility(View.GONE);
        }

        DialogHandler.getInstance().showAgreementDialog(SplashActivity.this, false, (status, message, data, alertDialog) -> {
            if (status != null) {
                if (status.equals("0")) {
                    Uri webpage = Uri.parse("https://airportflightsstatus.com/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    startActivity(intent);
                    showAgreementDialog = true;
                } else if (status.equals("1")) {
                    alertDialog.dismiss();
                    prefsManager.setPrivacyBit(true);
                    /*   if (!Utility.mInstance.isPremiumActive(SplashActivity.this)) {*/
                    isFragmentVisible = true;
                    loadTrailFragment();

                 /*   } else {
                        loadInterstitialAd("continue");
                    }*/
                }
            }
        });
    }

    /**
     * Method to display In App Purchase fragment
     */
    public void loadTrailFragment() {
        Log.e(TAG, "loadTrailFragment: working");
        FragmentManager manager = getSupportFragmentManager();
        String fragmentTag = "TrailFrag";
        Fragment existingFragment = new FreeTrailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("from", "splash");
        existingFragment.setArguments(bundle);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_fragmentContainer, existingFragment, fragmentTag).commit();
        Log.e(TAG, "loadTrailFragment: inside else");
        fragmentContainer.setVisibility(View.VISIBLE);
    }


    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
        Log.e(TAG, "onBackPressed: frag val: " + isFragmentVisible);
        if (isFragmentVisible) {
            Log.e(TAG, "onBackPressed: inside if");
            if (prefsManager.getBoardingBit()) {
                fragmentContainer.setVisibility(View.GONE);
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, OnBoardActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                prefsManager.setBoardingBit(true);
            }
            isFragmentVisible = false;

        } else {
            Log.e(TAG, "onBackPressed: inside else ");
            finishAffinity();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Utility.getInstance().addSplashAnimation(animImage1, animImage2, animImage3, true);

        InterstitialAdHandler.Companion.loadInterstitialAd(this, Constants.AdMob_Splash_Interstitial_Ad_Id);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Utility.getInstance().addSplashAnimation(animImage1, animImage2, animImage3, false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.getInstance().addSplashAnimation(animImage1, animImage2, animImage3, false);

    }

    @Override
    public void itemClicked() {
        Log.e(TAG, "itemClicked: boarding bit: " + prefsManager.getBoardingBit());
        if (!prefsManager.getBoardingBit()) {
            startActivity(new Intent(SplashActivity.this, OnBoardActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            prefsManager.setBoardingBit(true);
        } else {
            InterstitialAdHandler.Companion.showInterstitialAd(this, Constants.AdMob_Main_Interstitial_Ad_Id, () -> {
                startActivity(new Intent(SplashActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return null;
            });
        }
    }

    @Override
    public void onAdClicked() {
    }

    @Override
    public void onAdLoaded() {
        tvLoadingAd.setVisibility(View.GONE);
    }

    @Override
    public void onAdFailedToLoad(LoadAdError loadAdError) {

    }
}