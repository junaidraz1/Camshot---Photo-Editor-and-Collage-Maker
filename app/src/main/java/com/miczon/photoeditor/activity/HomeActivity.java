package com.miczon.photoeditor.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.LoadAdError;
import com.jaredrummler.android.device.DeviceName;
import com.miczon.photoeditor.BaseActivity;
import com.miczon.photoeditor.BuildConfig;
import com.miczon.photoeditor.R;
import com.miczon.photoeditor.eventListeners.BannerAdLoadListener;
import com.miczon.photoeditor.eventListeners.FragmentClickListener;
import com.miczon.photoeditor.fragments.FreeTrailFragment;
import com.miczon.photoeditor.helper.BannerAdManager;
import com.miczon.photoeditor.helper.DialogHandler;
import com.miczon.photoeditor.helper.PrefsManager;
import com.miczon.photoeditor.utils.Constants;
import com.miczon.photoeditor.utils.InterstitialAdHandler;
import com.miczon.photoeditor.utils.Utility;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;

import java.io.File;
import java.io.IOException;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

@SuppressWarnings("ALL")
public class HomeActivity extends BaseActivity implements FragmentClickListener, BannerAdLoadListener {

    String TAG = "HomeActivity", model = "", manufacturer = "", osVersion = "", apiLevel = "", lastFrom = "";

    ImageView ivHome, ivPremium, ivSavedFiles, navBackLayout, ivTryCartoonFilter;
    LinearLayout iphoneCamLayout, samsungCamLayout, nikonCamLayout, sonyCamLayout, navCameraLayout, navGalleryLayout, navCollageLayout, navLanguageLayout, navSavedFilesLayout, navShareLayout, navRateLayout, navPrivacyLayout,
            navFeedbackLayout, navMoreAppLayout, navExitLayout, filterLayout, cropLayout, adjustLayout, blurLayout, splashLayout, overlayLayout, brushLayout,
            stickersLayout, textLayout, beautyLayout, bgRemoverLayout, collageLayout, mosaicLayout;
    RelativeLayout navDrawerLayout, navDrawer, premiumLayout, adDisplayLayout;
    ImageCarousel carousel;
    RelativeLayout chooseImageLayout;
    TextView tvAppVersion, tvTryCartoonFilter, tvLoadingAd;
    LinearLayout contentLayout, accessDenyLayout;
    Button storageAccessBtn;
    LottieAnimationView animationView;
    DrawerLayout drawerLayout;
    FrameLayout fragmentContainer, adContainer;

    PrefsManager prefsManager;
    BannerAdManager bannerAdManager;

    File camPhotoFile = null;
    Uri capturedImageUri;

    boolean isFirstTime = true, isFromSettings = true,
            storagePermFlag = false, cameraPermFlag = false;
    int activityFlag;
    Dialog choosePictureDialog;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        carousel = findViewById(R.id.ic_imageCarousel);
        iphoneCamLayout = findViewById(R.id.ll_iphone);
        samsungCamLayout = findViewById(R.id.ll_samsung);
        nikonCamLayout = findViewById(R.id.ll_nikon);
        sonyCamLayout = findViewById(R.id.ll_sony);
        contentLayout = findViewById(R.id.ll_content);
        accessDenyLayout = findViewById(R.id.ll_accessDenyHint);
        storageAccessBtn = findViewById(R.id.btn_allowAccess);
        animationView = findViewById(R.id.animation_view);
        beautyLayout = findViewById(R.id.ll_beauty);
        bgRemoverLayout = findViewById(R.id.ll_remover);
        collageLayout = findViewById(R.id.ll_collage);
        mosaicLayout = findViewById(R.id.ll_mosaic);
        ivHome = findViewById(R.id.iv_Home);
        ivSavedFiles = findViewById(R.id.iv_myCollection);
        ivPremium = findViewById(R.id.iv_premium);
        chooseImageLayout = findViewById(R.id.rl_chooseImage);
        navDrawerLayout = findViewById(R.id.rl_drawer);
        drawerLayout = findViewById(R.id.drawer_layout);
        navDrawer = findViewById(R.id.nav_drawer);
        navCameraLayout = findViewById(R.id.ll_camera);
        navGalleryLayout = findViewById(R.id.ll_gallery);
        navCollageLayout = findViewById(R.id.ll_navCollage);
        navLanguageLayout = findViewById(R.id.ll_navLanguage);
        navSavedFilesLayout = findViewById(R.id.ll_navSavedFiles);
        navShareLayout = findViewById(R.id.ll_navShare);
        navRateLayout = findViewById(R.id.ll_navRateUs);
        navPrivacyLayout = findViewById(R.id.ll_navPrivacyPolicy);
        navFeedbackLayout = findViewById(R.id.ll_navFeedback);
        navMoreAppLayout = findViewById(R.id.ll_navMoreApps);
        navExitLayout = findViewById(R.id.ll_navExit);
        navBackLayout = findViewById(R.id.iv_back);
        tvAppVersion = findViewById(R.id.tV_appVersion);
        tvTryCartoonFilter = findViewById(R.id.tv_tryCartoonFilter);
        tvLoadingAd = findViewById(R.id.tv_loadingAd);
        premiumLayout = findViewById(R.id.rl_premium);
        fragmentContainer = findViewById(R.id.fl_fragmentContainer);
        filterLayout = findViewById(R.id.ll_filterHome);
        cropLayout = findViewById(R.id.ll_cropHome);
        adjustLayout = findViewById(R.id.ll_adjustHome);
        blurLayout = findViewById(R.id.ll_blurHome);
        splashLayout = findViewById(R.id.ll_splashHome);
        overlayLayout = findViewById(R.id.ll_overlayHome);
        brushLayout = findViewById(R.id.ll_brushLayout);
        ivTryCartoonFilter = findViewById(R.id.iv_tryFilter);
        stickersLayout = findViewById(R.id.ll_stickersHome);
        textLayout = findViewById(R.id.ll_textHome);
        adContainer = findViewById(R.id.fl_adContainer);
        adDisplayLayout = findViewById(R.id.rl_adContainer);

        prefsManager = new PrefsManager(this);
        bannerAdManager = new BannerAdManager(this, this);

        if (!Utility.getInstance().isPremiumActive(this)) {
            adDisplayLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
            bannerAdManager.loadBannerAd(adContainer);

        } else {
            adDisplayLayout.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
        }

        tvAppVersion.setText(getString(R.string.app_ver_label) + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

        YoYo.with(Techniques.Pulse)
                .duration(2000)
                .repeat(Animation.INFINITE)
                .playOn(premiumLayout);

        getNotificationPermission();

        carousel.registerLifecycle(getLifecycle());
        Utility.getInstance().initializeCarousel(this, carousel);

        if (prefsManager.getSelectedLanguage() != null) {
            if (prefsManager.getSelectedLanguage().equalsIgnoreCase("ar") ||
                    prefsManager.getSelectedLanguage().equalsIgnoreCase("fa") ||
                    prefsManager.getSelectedLanguage().equalsIgnoreCase("iw")) {
                navExitLayout.setBackgroundResource(R.drawable.nav_exit_rtl_bg);

            } else {
                navExitLayout.setBackgroundResource(R.drawable.nav_exit_btn_bg);
            }
        }

        /*
         * Check for storage and camera permission
         */
        askStoragePermission("start");

        /*
         * Click listener to allow storage access when revoke by user
         */
        storageAccessBtn.setOnClickListener(v -> askStoragePermission("settings"));

        iphoneCamLayout.setOnClickListener(v -> {
            InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    activityFlag = 1;
                    askStoragePermission("beautyCamera");
                    return null;
                }
            });
        });

        samsungCamLayout.setOnClickListener(v -> {
            InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    activityFlag = 2;
                    askStoragePermission("beautyCamera");
                    return null;
                }
            });
        });

        nikonCamLayout.setOnClickListener(v -> {
            InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    activityFlag = 3;
                    askStoragePermission("beautyCamera");
                    return null;
                }
            });
        });

        sonyCamLayout.setOnClickListener(v -> {
            InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    activityFlag = 4;
                    askStoragePermission("beautyCamera");
                    return null;
                }
            });
        });

        bgRemoverLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        askStoragePermission("bgRemover");
                        return null;
                    }
                });
            }
        });

        mosaicLayout.setOnClickListener(v -> Toast.makeText(HomeActivity.this, R.string.coming_soon_msg, Toast.LENGTH_SHORT).show());

        /*
        To open cartoon filter screen
         */
        ivTryCartoonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        startActivity(new Intent(HomeActivity.this, CartoonFiltersActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        return null;
                    }
                });
            }
        });

        /*
        To open image editor via beauty icon click
         */
        beautyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open image editor via filter icon click
         */
        filterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open image editor via crop icon click
         */
        cropLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open image editor via adjust icon click
         */
        adjustLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open image editor via blur icon click
         */
        blurLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open image editor via splash icon click
         */
        splashLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open image editor via overlay icon click
         */
        overlayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open image editor via brush icon click
         */
        brushLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open image editor via stickers icon click
         */
        stickersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open image editor via text icon click
         */
        textLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open collage make activity
         */
        collageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        askStoragePermission("collage");
                        return null;
                    }
                });
            }
        });

        /*
         To refresh same activity i.e Home
         */
        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        startActivity(new Intent(HomeActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        return null;
                    }
                });
            }
        });

        /*
         To navigate to saved files screen
         */
        ivSavedFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        if (storagePermFlag) {
                            startActivity(new Intent(HomeActivity.this, MyCreationActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        } else {
                            askStoragePermission("savedFiles");
                        }
                        return null;
                    }
                });
            }
        });

        /*
         To open image chooser dialog when plus button is tapped on home screen
         */
        chooseImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open navigation drawer from home screen
         */
        navDrawerLayout.setOnClickListener(v -> openDrawer());

        /*
         To open camera from navigation drawer
         */
        navCameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        closeDrawer();
                        choosePictureDialog();
                        return null;
                    }
                });
            }
        });

        /*
         To open gallery from navigation drawer
         */
        navGalleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        closeDrawer();
                        if (storagePermFlag) {
                            openImageChooser();
                        } else {
                            askStoragePermission("gallery");
                        }
                        return null;
                    }
                });
            }
        });

        /*
         To open collage from navigation drawer
         */
        navCollageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        closeDrawer();
                        askStoragePermission("collage");
                        return null;
                    }
                });
            }
        });

        /*
         To open language panel from navigation drawer
         */
        navLanguageLayout.setOnClickListener(v -> {
            closeDrawer();
            startActivity(new Intent(HomeActivity.this, SelectLanguageActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        /*
         To open saved files from navigation drawer
         */
        navSavedFilesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterstitialAdHandler.Companion.showInterstitialAd(HomeActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        closeDrawer();
                        if (storagePermFlag) {
                            startActivity(new Intent(HomeActivity.this, MyCreationActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        } else {
                            askStoragePermission("savedFiles");
                        }
                        return null;
                    }
                });
            }
        });

        /*
         To open share app panel from navigation drawer
         */
        navShareLayout.setOnClickListener(v -> {
            closeDrawer();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_body) + "\n"
                    + "https://play.google.com/store/apps/details?id=" + getPackageName());

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_header)));
        });

        /*
         To open rate us dialog from navigation drawer
         */
        navRateLayout.setOnClickListener(v -> {
            closeDrawer();
            DialogHandler.getInstance().rateUsDialog(HomeActivity.this, true, (status, message, data, alertDialog) -> {
                if (status != null && data != null) {
                    float ratingVal = Float.parseFloat(data);

                    if (status.equals("2")) {
                        alertDialog.dismiss();

                    } else if (ratingVal < 4) {
                        getDeviceInfo();
                        alertDialog.dismiss();

                    } else {
                        alertDialog.dismiss();
                        Intent appPlayStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.cartoonpic.aiphotoeditor"));
                        appPlayStoreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(appPlayStoreIntent);
                    }
                }
            });
        });

        /*
         To open privacy policy from navigation drawer
         */
        navPrivacyLayout.setOnClickListener(v -> {
            closeDrawer();
            DialogHandler.getInstance().privacyDialog(HomeActivity.this, true, (status, message, data, alertDialog) -> {
                if (status != null) {
                    switch (status) {
                        case "1":
                        case "3":
                            alertDialog.dismiss();
                            break;
                        case "2":
                            Uri webpage = Uri.parse("https://airportflightsstatus.com/");
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, webpage);
                            alertDialog.dismiss();
                            startActivity(browserIntent);
                            break;
                    }
                }
            });
        });

        /*
         To open feedback from navigation drawer
         */
        navFeedbackLayout.setOnClickListener(v -> {
            closeDrawer();
            getDeviceInfo();
        });

        /*
         To open more apps from navigation drawer
         */
        navMoreAppLayout.setOnClickListener(v -> {
            closeDrawer();
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Airport%20Flights%20Status™"));
            playStoreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(playStoreIntent);
        });

        /*
         To exit app from navigation drawer
         */
        navExitLayout.setOnClickListener(v -> {
            closeDrawer();
            showExitDialog();
        });

        /*
         To close navigation drawer
         */
        navBackLayout.setOnClickListener(v -> closeDrawer());

        /*
         To open in app screen from navigation drawer
         */
        premiumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTrailFragment();
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
        bundle.putString("from", "home");
        existingFragment.setArguments(bundle);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_fragmentContainer, existingFragment, fragmentTag).commit();
        fragmentContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Method to open different camera screens from one method call
     *
     * @param i: to identify which camera activity has been invoked
     */
    private void startBeautyCamera(int i) {
        if (i == 1) {
            startActivity(new Intent(HomeActivity.this, IphoneCameraActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else if (i == 2) {
            startActivity(new Intent(HomeActivity.this, SamsungCameraActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else if (i == 3) {
            startActivity(new Intent(HomeActivity.this, NikonCameraActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else if (i == 4) {
            startActivity(new Intent(HomeActivity.this, SonyCameraActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else if (i == 5) {
            startActivity(new Intent(HomeActivity.this, RemoveBackgroundActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    /**
     * Helper method to open the drawer programmatically
     */
    private void openDrawer() {
        drawerLayout.openDrawer(navDrawer);
    }

    /**
     * Helper method to close the drawer programmatically
     */
    private void closeDrawer() {
        drawerLayout.closeDrawer(navDrawer);
    }

    /**
     * Method to get device info
     */
    private void getDeviceInfo() {
        model = android.os.Build.MODEL;
        manufacturer = android.os.Build.MANUFACTURER;
        osVersion = Build.VERSION.RELEASE + " " + Build.DISPLAY + " ";
        apiLevel = String.valueOf(Build.VERSION.SDK_INT);

        DeviceName.with(this).request((info, error) -> {
            if (info.model.equalsIgnoreCase(info.marketName)) {
                model = info.marketName;
            } else {
                model = info.marketName + " " + info.model;
            }
        });

        if (Constants.F_MAIL == null || Constants.F_MAIL.isEmpty()) {
            Constants.F_MAIL = "info@airportflightsstatus.com";
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.F_MAIL});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.device_info_label) + "\n" + getString(R.string.os_ver_label) +
                osVersion + "\n" + getString(R.string.api_level_label) + apiLevel + "\n" + getString(R.string.dev_label) + manufacturer + "\n" + getString(R.string.model_label) + model + "(" + manufacturer + ")" + "\n\n" +
                getString(R.string.feedback_msg) + "\n");

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "getDeviceInfo: exception: " + e.getLocalizedMessage());
            Toast.makeText(this, R.string.no_email_txt, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Choose picture full screen dialog
     */
    public void choosePictureDialog() {
        choosePictureDialog = new Dialog(HomeActivity.this, android.R.style.Theme_Light);
        choosePictureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        choosePictureDialog.setContentView(R.layout.layout_dialog_choose_image_home);

        choosePictureDialog.setCancelable(true);

        RelativeLayout galleryLayout = choosePictureDialog.findViewById(R.id.rl_gallery);
        RelativeLayout cameraLayout = choosePictureDialog.findViewById(R.id.rl_camera);
        RelativeLayout collageLayout = choosePictureDialog.findViewById(R.id.rl_collage);
        RelativeLayout closeLayout = choosePictureDialog.findViewById(R.id.rl_close);
        FrameLayout adContainer = choosePictureDialog.findViewById(R.id.fl_adContainer);
        RelativeLayout adDisplayLayout = choosePictureDialog.findViewById(R.id.rl_adContainer);
        TextView tvLoadingAdText = choosePictureDialog.findViewById(R.id.tv_loadingAd);

        if (!Utility.getInstance().isPremiumActive(this)) {
            adDisplayLayout.setVisibility(View.VISIBLE);
            adContainer.setVisibility(View.VISIBLE);
            bannerAdManager.loadBannerAd(adContainer);

        } else {
            adDisplayLayout.setVisibility(View.GONE);
            adContainer.setVisibility(View.GONE);
        }


        Log.e(TAG, "choosePictureDialog: storage perm: " + storagePermFlag);

        galleryLayout.setOnClickListener(v -> {
          /*  if (storagePermFlag) {
                choosePictureDialog.dismiss();
                openImageChooser();
            } else {*/
            choosePictureDialog.dismiss();
            askStoragePermission("gallery");
            /*   }*/
        });

        cameraLayout.setOnClickListener(v -> {
            choosePictureDialog.dismiss();
           /* if (cameraPermFlag) {
                choosePictureDialog.dismiss();
                startCameraIntent();
            } else {
                choosePictureDialog.dismiss();*/
            askStoragePermission("camera");
            /* }*/
        });

        collageLayout.setOnClickListener(v -> {
            choosePictureDialog.dismiss();
            startActivity(new Intent(HomeActivity.this, SelectImageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        closeLayout.setOnClickListener(v -> choosePictureDialog.dismiss());

        choosePictureDialog.show();
    }


    /**
     * Method to handle camera and media access permissions
     *
     * @param from : To check if this method is called via on create or on click on camera or gallery layout
     */
    private void askStoragePermission(String from) {
        String permission;

        /*
         * to handle permission when it called from on create or gallery or settings layout click
         */
        lastFrom = from;
        Log.e(TAG, "askStoragePermission: last from: " + from);
        if (from.equalsIgnoreCase("start") || from.equalsIgnoreCase("gallery") ||
                from.equalsIgnoreCase("settings") || from.equalsIgnoreCase("savedFiles") ||
                from.equalsIgnoreCase("collage") || from.equalsIgnoreCase("bgRemover")) {
            Log.e(TAG, "askStoragePermission: inside if from: " + from);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission = Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            }
            if (ContextCompat.checkSelfPermission(HomeActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "askStoragePermission: inside second if from: " + from);
                if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permission)) {
                    Log.e(TAG, "askStoragePermission: inside 3rd if from: " + from);
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission},
                            Constants.REQUEST_GALLERY_ACCESS);

                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permission)) {
                    if (!from.equalsIgnoreCase("settings")) {
                        Log.e(TAG, "askStoragePermission: inside 3rd if's else if from: " + from);
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission},
                                Constants.REQUEST_GALLERY_ACCESS);
                    } else {
                        Log.e(TAG, "askStoragePermission: inside 3rd if's else from: " + from);
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission},
                                Constants.PERMISSION_REQUEST_SETTINGS);
                    }
                } else {
                    Log.e(TAG, "askStoragePermission: inside 3rd if's else from: " + from);
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{permission},
                            Constants.REQUEST_GALLERY_ACCESS);
                }
            } else {
                storagePermFlag = true;
                if (from.equalsIgnoreCase("gallery")) {
                    openImageChooser();

                } else if (from.equalsIgnoreCase("camera")) {
                    startCameraIntent();

                } else if (from.equalsIgnoreCase("savedFiles")) {
                    startActivity(new Intent(HomeActivity.this, MyCreationActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                } else if (from.equalsIgnoreCase("Collage")) {
                    startActivity(new Intent(HomeActivity.this, SelectImageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                } else if (from.equalsIgnoreCase("bgRemover")) {
                    startActivity(new Intent(HomeActivity.this, RemoveBackgroundActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            }
        }

        /*
         * to handle permission when it is called from camera layout click
         */
        else if (from.equalsIgnoreCase("camera")) {
            Log.e(TAG, "askStoragePermission: inside if");
            if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "askStoragePermission: inside second if");
                if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {
                    Log.e(TAG, "askStoragePermission: inside third if");
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {
                    Log.e(TAG, "askStoragePermission: inside third else if");
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

                } else {
                    Log.e(TAG, "askStoragePermission: inside third else");
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);
                }
            } else {
                Log.e(TAG, "askStoragePermission: inside else of first if");
                cameraPermFlag = true;
            }
        }

        /*
         * to handle permission when it is called from beauty camera layout click
         */
        else if (from.equalsIgnoreCase("beautyCamera")) {
            Log.e(TAG, "askStoragePermission: inside if");
            if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "askStoragePermission: inside second if");
                if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {
                    Log.e(TAG, "askStoragePermission: inside third if");
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_BEAUTY_CAMERA_ACCESS);

                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {
                    Log.e(TAG, "askStoragePermission: inside third else if");
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_BEAUTY_CAMERA_ACCESS);

                } else {
                    Log.e(TAG, "askStoragePermission: inside third else");
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_BEAUTY_CAMERA_ACCESS);
                }
            } else {
                Log.e(TAG, "askStoragePermission: inside else of first if");
                Log.e(TAG, "askStoragePermission: activity flag val: " + activityFlag);
                startBeautyCamera(activityFlag);
            }
        }
    }

    /**
     * Method to handle permission requests
     *
     * @param requestCode:  result code
     * @param permissions:  permissions asked
     * @param grantResults: request status e.g. granted/denied
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean showStorageAccessSettings = false;
        boolean isGranted = false;

        Log.e(TAG, "onRequestPermissionsResult: called");

        /*
         * When Permission to gallery access is requested
         */
        if (requestCode == Constants.REQUEST_GALLERY_ACCESS) {
            Log.e(TAG, "onRequestPermissionsResult: inside gallery access");
            for (int result : grantResults) {
                Log.e(TAG, "onRequestPermissionsResult: inside for");
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onRequestPermissionsResult: inside if");
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.READ_MEDIA_IMAGES)) {
                        Log.e(TAG, "onRequestPermissionsResult: inside nested if");
                        storageAccessBtn.setText(R.string.open_settings);
                    }
                    accessDenyLayout.setVisibility(View.VISIBLE);
                    animationView.playAnimation();
                    contentLayout.setVisibility(View.GONE);
                    carousel.setVisibility(View.GONE);
                    /*adDisplayLayout.setVisibility(View.GONE);*/

                } else {
                    Log.e(TAG, "onRequestPermissionsResult: inside else");
                    accessDenyLayout.setVisibility(View.GONE);
            /*        if (isFirstTime) {
                        getNotificationPermission();
                        isFirstTime = false;
                    } else {*/
                    Log.e(TAG, "onRequestPermissionsResult: last from: " + lastFrom);
                    if (lastFrom.equalsIgnoreCase("gallery")) {
                        openImageChooser();
                    } else if (lastFrom.equalsIgnoreCase("savedFiles")) {
                        startActivity(new Intent(HomeActivity.this, MyCreationActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                    /*                    }*/
                }
            }
        }

        /*
         * When permission to camera access is requested
         */
        else if (requestCode == Constants.REQUEST_CAMERA_ACCESS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {

                        if (!isFirstTime) {
                            showStorageAccessSettings = true;

                        } else {
                            isFirstTime = false;
                        }

                    } else {
                        Toast.makeText(this, getString(R.string.cam_denied), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    isGranted = true;
                }
            }
            if (isGranted) {
                startCameraIntent();
            }

            if (showStorageAccessSettings) {
                openSettings();
            }
        }

        /*
         * When permission to beauty camera access is requested
         */
        else if (requestCode == Constants.REQUEST_BEAUTY_CAMERA_ACCESS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {

                        if (!isFirstTime) {
                            showStorageAccessSettings = true;

                        } else {
                            isFirstTime = false;
                        }

                    } else {
                        Toast.makeText(this, getString(R.string.cam_denied), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    isGranted = true;
                }
            }
            if (isGranted) {
                if (activityFlag > 0) {
                    startBeautyCamera(activityFlag);
                } else {
                    Toast.makeText(this, R.string.camera_perm_success_msg, Toast.LENGTH_SHORT).show();
                }
            }

            if (showStorageAccessSettings) {
                openSettings();
            }
        }

        /*
         * When permission to open settings is requested
         */
        else if (requestCode == Constants.PERMISSION_REQUEST_SETTINGS) {
            Log.e(TAG, "onRequestPermissionsResult: PERMISSION_REQUEST_SETTINGS working");
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            !ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        storageAccessBtn.setText(R.string.open_settings);
                        showStorageAccessSettings = true;
                    }
                    accessDenyLayout.setVisibility(View.VISIBLE);
                    animationView.playAnimation();
                    contentLayout.setVisibility(View.GONE);
                    carousel.setVisibility(View.GONE);
                    /*  adDisplayLayout.setVisibility(View.GONE);*/

                } else {
                    accessDenyLayout.setVisibility(View.GONE);
                }
            }
            if (showStorageAccessSettings) {
                openSettings();
            }
        } else if (requestCode == Constants.REQUEST_NOTIFICATION_ACCESS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onRequestPermissionsResult: permission granted");

            } else {
                Toast.makeText(this, R.string.noti_msg_body, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onRequestPermissionsResult: permission not granted");
            }
        }
    }

    /**
     * Method to ask for notifications permission (Devices Greater or equal to android 13)
     */
    public void getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        Constants.REQUEST_NOTIFICATION_ACCESS);
            } else {
                Log.e(TAG, "getNotificationPermission: no permission needed");
            }
        } catch (Exception e) {
            Log.e(TAG, "getNotificationPermission: exception: " + e.getLocalizedMessage());
        }
    }

    /**
     * Method to open settings to allow permission when "don't ask again" is selected by user
     */
    private void openSettings() {
        Log.e(TAG, "openSettings: working");
        Constants.isSelectingFile = true;
        isFromSettings = true;
        Intent settingsIntent = new Intent();
        settingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        settingsIntent.setData(uri);
        startActivity(settingsIntent);
    }

    /**
     * Method to open gallery when access is granted
     */
    private void openCollageActivity() {
        startActivity(new Intent(HomeActivity.this, SelectImageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    /**
     * Method to open image editor when access is granted
     */
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.REQUEST_GALLERY);
    }

    /**
     * Method to open camera when camera access is granted
     */
    private void startCameraIntent() {
        Constants.isSelectingFile = true;
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Log.e(TAG, "startCameraIntent: inside if");

        try {
            camPhotoFile = Utility.getInstance().createCamFile(this);
        } catch (IOException ex) {
            Log.e(TAG, "startCameraIntent: exception: " + ex.getLocalizedMessage());
        }

        if (camPhotoFile != null) {
            try {
                Log.e(TAG, "startCameraIntent: file: " + camPhotoFile.getAbsolutePath());
                capturedImageUri = FileProvider.getUriForFile(this,
                        this.getPackageName() + ".provider",
                        camPhotoFile);

                Log.e(TAG, "startCameraIntent: captured uri: " + capturedImageUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                startActivityForResult(takePictureIntent, Constants.CAMERA_PIC_REQUEST);

            } catch (Exception e) {
                Log.e(TAG, "startCameraIntent: camPhotoFile exception: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Method to display exit dialog on back press
     */
    private void showExitDialog() {
        DialogHandler.getInstance().exitDialog(HomeActivity.this, true, (status, message, data, alertDialog) -> {
            if (status != null) {
                if (status.equals("0")) {
                    alertDialog.dismiss();

                } else {
                    alertDialog.dismiss();
                    finishAffinity();
                }
            }
        });
    }

    /**
     * To handle camera intent and gallery intent requests
     *
     * @param requestCode: to identify if gallery intent is invoked or camera intent
     * @param resultCode:  to identify if intent result is successful
     * @param data:        data received via intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Constants.isSelectingFile = true;
        if (requestCode == Constants.REQUEST_GALLERY && resultCode == RESULT_OK) {
            assert data != null;
            Uri path = data.getData();
            Intent intent = new Intent(HomeActivity.this, ImageEditActivity.class);
            intent.putExtra("image_uri", path.toString());
            startActivity(intent);

        } else if (requestCode == Constants.CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(camPhotoFile.getAbsolutePath());

                // Rotate the bitmap based on Exif orientation
                Bitmap rotatedBitmap = rotateBitmap(bitmap, camPhotoFile.getAbsolutePath());

                Uri path = Utility.getInstance().bitmapToUri(this, rotatedBitmap, "");
                Log.e(TAG, "onActivityResult: path: " + path.getPath());
                Intent intent = new Intent(HomeActivity.this, ImageEditActivity.class);
                intent.putExtra("image_uri", path.toString());
                startActivity(intent);

            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: error: " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Data is Null");
        }
    }

    /**
     * Method to rotate image to it's default state when captured by camera
     *
     * @param bitmap:    bitmap of captured image
     * @param imagePath: local path of image in cache
     * @return: Bitmap
     */
    private Bitmap rotateBitmap(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Log.e(TAG, "rotateBitmap: orientation: " + orientation);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            Log.e(TAG, "rotateBitmap: exception: " + e.getLocalizedMessage());
            return bitmap;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        InterstitialAdHandler.Companion.loadInterstitialAd(this, Constants.AdMob_Main_Interstitial_Ad_Id);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onResume: inside perm if");
            animationView.cancelAnimation();
            accessDenyLayout.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
            carousel.setVisibility(View.VISIBLE);
            /*  adDisplayLayout.setVisibility(View.VISIBLE);*/
        } else {
            /*      adDisplayLayout.setVisibility(View.GONE);*/
            Log.e(TAG, "onResume: inside perm else: ");
        }

/*        if (isPremium) {
            adDisplayLayout.setVisibility(View.GONE);
            nativeAdContainer.setVisibility(View.GONE);
        } else {
            adDisplayLayout.setVisibility(View.VISIBLE);
            nativeAdContainer.setVisibility(View.VISIBLE);
        }*/

        if (!isFromSettings) {
            Constants.isSelectingFile = false;
        }

        isFromSettings = false;
        Log.e(TAG, "onResume: isSelectingFile: " + Constants.isSelectingFile);
    }

    @Override
    public void onBackPressed() {
        if (choosePictureDialog != null && choosePictureDialog.isShowing()) {
            choosePictureDialog.dismiss();
        } else {
            Log.e(TAG, "onBackPressed: choose picture else: ");
            showExitDialog();
        }
    }

    @Override
    public void itemClicked() {

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