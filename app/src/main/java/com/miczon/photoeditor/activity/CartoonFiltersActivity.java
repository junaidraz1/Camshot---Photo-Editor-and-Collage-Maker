package com.miczon.photoeditor.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.ads.LoadAdError;
import com.miczon.photoeditor.BaseActivity;
import com.miczon.photoeditor.R;
import com.miczon.photoeditor.adapter.CartoonFiltersAdapter;
import com.miczon.photoeditor.eventListeners.BannerAdLoadListener;
import com.miczon.photoeditor.helper.BannerAdManager;
import com.miczon.photoeditor.helper.DialogHandler;
import com.miczon.photoeditor.helper.PrefsManager;
import com.miczon.photoeditor.utils.Constants;
import com.miczon.photoeditor.utils.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CartoonFiltersActivity extends BaseActivity implements BannerAdLoadListener {

    String TAG = "TrendingFiltersFragment", from = "";

    TextView tvHeader, tvLoadingAd;
    RelativeLayout backLayout, buyPremiumLayout, adDisplayLayout;
    RecyclerView rvTrendingFilter;
    FrameLayout adContainer;
    PrefsManager prefsManager;

    StaggeredGridLayoutManager staggeredGridLayoutManager;
    CartoonFiltersAdapter cartoonFiltersAdapter;

    ArrayList<String> selectedFilterIds;

    File camPhotoFile = null;
    Uri capturedImageUri;

    boolean isFromSettings = false;
    boolean isFirstTime = true;

    BannerAdManager bannerAdManager;

    boolean isRewardedAdShown = false;
    boolean isInAppShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartoon_filters);

        tvHeader = findViewById(R.id.tv_header);
        backLayout = findViewById(R.id.rl_back);
        /* buyPremiumLayout = findViewById(R.id.rl_premium);*/
        rvTrendingFilter = findViewById(R.id.rV_trendingFilters);
        adDisplayLayout = findViewById(R.id.rl_adLayout);
        adContainer = findViewById(R.id.fl_adContainer);
        tvLoadingAd = findViewById(R.id.tv_loadingAd);

        prefsManager = new PrefsManager(this);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        selectedFilterIds = new ArrayList<>();
        bannerAdManager = new BannerAdManager(this, this);

        bannerAdManager.loadBannerAd(adContainer);

        tvHeader.setText(R.string.cartoon_filters_header);

        cartoonFiltersAdapter = new CartoonFiltersAdapter(this, Utility.getInstance().filterPreview(), (position, path, action) -> {
            Log.e(TAG, "onCreateView: adapter click working");
            selectedFilterIds = checkPosition(position);

           /* if (!Utility.getInstance().isPremiumActive(CartoonFiltersActivity.this)) {
                //For rewarded
                if (position == 2 || position == 14 || position == 10 || position == 13 || position == 15) {
                    DialogHandler.getInstance().displayPreRewardAdDialog(CartoonFiltersActivity.this, true, new DialogClickListener() {
                        @Override
                        public void onButtonClick(String status, String message, String data, AlertDialog alertDialog) {
                            if (status.equalsIgnoreCase("0")) {
                                alertDialog.dismiss();
                                loadRewardedInterstitialAd("filters");
                            } else if (status.equalsIgnoreCase("1")) {
                                alertDialog.dismiss();
                                loadTrailFragment("rewarded");
                            } else if (status.equalsIgnoreCase("2")) {
                                alertDialog.dismiss();
                            }
                        }
                    });
                }
                //For in app
                else if (position == 1 || position == 3 || position == 5 || position == 6 ||
                        position == 7 || position == 8 || position == 9 || position == 11 ||
                        position == 12 || position == 4) {
                    loadInterstitialAd("purchase");
                }
                //For free
                else {
                    loadInterstitialAd("choose");
                }
            } else {*/
            loadInterstitialAd("choose");
            /* }*/

        });

        // on below line we are attaching this snap helper to our recycler view.
        rvTrendingFilter.setLayoutManager(staggeredGridLayoutManager);
        rvTrendingFilter.setAdapter(cartoonFiltersAdapter);
        rvTrendingFilter.setItemAnimator(new DefaultItemAnimator());

        backLayout.setOnClickListener(v -> onBackPressed());

        /*buyPremiumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTrailFragment("buyPrem");
            }
        });*/

    }

    private void choosePictureDialog() {
        DialogHandler.getInstance().choosePictureDialog(CartoonFiltersActivity.this, true, (status, message, data, alertDialog) -> {
            if (status.equalsIgnoreCase("0")) {
                alertDialog.dismiss();
                loadInterstitialAd("camera");

            } else if (status.equalsIgnoreCase("1")) {
                alertDialog.dismiss();
                loadInterstitialAd("gallery");
            }
        });
    }

    /**
     * Method to load and display interstitial add
     *
     * @param from: from where it is called e.g. onResume, onCreate etc
     */
    public void loadInterstitialAd(String from) {
       /* if (!Utility.getInstance().isPremiumActive(CartoonFiltersActivity.this)) {
            InterstitialAd.load(CartoonFiltersActivity.this, "ca-app-pub-3940256099942544/1033173712", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mInterstitialAd = interstitialAd;
                            Log.e(TAG, "mInterstitialAdLoaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e(TAG, loadAdError.toString());
                            mInterstitialAd = null;
                        }
                    });
            if (mInterstitialAd != null && !from.equalsIgnoreCase("resume")) {
                SplashActivity.adCounter++;
                prefsManager.setAdCount(SplashActivity.adCounter);

                Log.e(TAG, "loadInterstitialAd: ad count: " + prefsManager.getAdCount());

                if (prefsManager.getAdCount() > 0 && prefsManager.getAdCount() % 3 == 0) {
                    mInterstitialAd.show(CartoonFiltersActivity.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Constants.isInterstitialVisible = true;
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Constants.isInterstitialVisible = false;
                            Log.e(TAG, "Ad dismissed fullscreen content.");
                            mInterstitialAd = null;
                            if (from.equalsIgnoreCase("choose")) {
                                choosePictureDialog();
                            } else if (from.equalsIgnoreCase("camera")) {
                                checkCameraPerm();
                            } else if (from.equalsIgnoreCase("gallery")) {
                                galleryClickAction();
                            } else if (from.equalsIgnoreCase("purchase")) {
                                loadTrailFragment(from);
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            mInterstitialAd = null;
                            if (from.equalsIgnoreCase("choose")) {
                                choosePictureDialog();
                            } else if (from.equalsIgnoreCase("camera")) {
                                checkCameraPerm();
                            } else if (from.equalsIgnoreCase("gallery")) {
                                galleryClickAction();
                            } else if (from.equalsIgnoreCase("purchase")) {
                                loadTrailFragment(from);
                            }
                        }
                    });
                } else {
                    if (from.equalsIgnoreCase("choose")) {
                        choosePictureDialog();
                    } else if (from.equalsIgnoreCase("camera")) {
                        checkCameraPerm();
                    } else if (from.equalsIgnoreCase("gallery")) {
                        galleryClickAction();
                    } else if (from.equalsIgnoreCase("purchase")) {
                        loadTrailFragment(from);
                    }
                }
            } else {
                Log.e(TAG, "The interstitial ad wasn't ready yet.");
                if (from.equalsIgnoreCase("choose")) {
                    choosePictureDialog();
                } else if (from.equalsIgnoreCase("camera")) {
                    checkCameraPerm();
                } else if (from.equalsIgnoreCase("gallery")) {
                    galleryClickAction();
                } else if (from.equalsIgnoreCase("purchase")) {
                    loadTrailFragment(from);
                }
            }
        } else {*/
        Log.e(TAG, "No Premium");
        if (from.equalsIgnoreCase("choose")) {
            choosePictureDialog();
        } else if (from.equalsIgnoreCase("camera")) {
            checkCameraPerm();
        } else if (from.equalsIgnoreCase("gallery")) {
            galleryClickAction();
        }
        /*}*/
    }

    /**
     * Method to open camera when camera access is granted
     */
    @SuppressWarnings("deprecation")
    private void startCameraIntent() {
        Constants.isSelectingFile = true;
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Log.e(TAG, "startCameraIntent: inside if");

        try {
            camPhotoFile = Utility.getInstance().createCamFile(CartoonFiltersActivity.this);
        } catch (IOException ex) {
            Log.e(TAG, "startCameraIntent: exception: " + ex.getLocalizedMessage());
        }

        if (camPhotoFile != null) {
            try {
                Log.e(TAG, "startCameraIntent: file: " + camPhotoFile.getAbsolutePath());
                capturedImageUri = FileProvider.getUriForFile(CartoonFiltersActivity.this,
                        CartoonFiltersActivity.this.getPackageName() + ".fileprovider",
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
     * Method to handle click event when gallery button is clicked
     */
    @SuppressWarnings("deprecation")
    private void galleryClickAction() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.REQUEST_GALLERY);
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
            if (from != null && from.equalsIgnoreCase("intent")) {
                handleIntentAction(path, "intent");

            } else {
                handleIntentAction(path, "home");
            }

        } else if (requestCode == Constants.CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(camPhotoFile.getAbsolutePath());

                // Rotate the bitmap based on Exif orientation
                Bitmap rotatedBitmap = rotateBitmap(bitmap, camPhotoFile.getAbsolutePath());

                Uri path = Utility.getInstance().bitmapToUri(CartoonFiltersActivity.this, rotatedBitmap, "");
                Log.e(TAG, "onActivityResult: path: " + path.getPath());
                handleIntentAction(path, "camera");

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

    /**
     * to open Apply Effect Activity and pass
     * gallery selected image or camera captured image
     *
     * @param path: path of gallery selected image or camera captured image
     */
    private void handleIntentAction(Uri path, String from) {
        String selectedImagePath = Utility.getInstance().getPath(CartoonFiltersActivity.this, path);
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            Intent intent = new Intent(CartoonFiltersActivity.this, ApplyEffectActivity.class);
            intent.putExtra("path", selectedImagePath);
            intent.putExtra("from", from);
            intent.putExtra("ids", selectedFilterIds);
            intent.putExtra("via", "trendingFrag" + from);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.e(TAG, "handleIntentAction: image path: " + selectedImagePath);
            Log.e(TAG, "handleIntentAction: from: " + from);
            for (int i = 0; i < selectedFilterIds.size(); i++) {
                Log.e(TAG, "handleIntentAction: filter ids: " + selectedFilterIds.get(i));
            }
            startActivity(intent);
        } else {
            Toast.makeText(CartoonFiltersActivity.this, getString(R.string.con_error_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCameraPerm() {
        Log.e(TAG, "askStoragePermission: inside if");
        if (ContextCompat.checkSelfPermission(CartoonFiltersActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "askStoragePermission: inside second if");
            if (ActivityCompat.shouldShowRequestPermissionRationale(CartoonFiltersActivity.this, Manifest.permission.CAMERA)) {
                Log.e(TAG, "askStoragePermission: inside third if");
                ActivityCompat.requestPermissions(CartoonFiltersActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(CartoonFiltersActivity.this, Manifest.permission.CAMERA)) {
                Log.e(TAG, "askStoragePermission: inside third else if");
                ActivityCompat.requestPermissions(CartoonFiltersActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

            } else {
                Log.e(TAG, "askStoragePermission: inside third else");
                ActivityCompat.requestPermissions(CartoonFiltersActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);
            }
        } else {
            Log.e(TAG, "askStoragePermission: inside else of first if");
            startCameraIntent();
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
         * When permission to camera access is requested
         */
        if (requestCode == Constants.REQUEST_CAMERA_ACCESS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(CartoonFiltersActivity.this, Manifest.permission.CAMERA)) {

                        if (!isFirstTime) {
                            showStorageAccessSettings = true;

                        } else {
                            isFirstTime = false;
                        }

                    } else {
                        Toast.makeText(CartoonFiltersActivity.this, getString(R.string.cam_denied), Toast.LENGTH_SHORT).show();
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
        Uri uri = Uri.fromParts("package", CartoonFiltersActivity.this.getPackageName(), null);
        settingsIntent.setData(uri);
        startActivity(settingsIntent);
    }

    public ArrayList<String> checkPosition(int pos) {
        Log.e(TAG, "checkPosition: position: " + pos);
        ArrayList<String> filterIdList = new ArrayList<>();
        if (pos == 0) {
            filterIdList.add("6632");
            filterIdList.add("3690");
            filterIdList.add("6821");
            filterIdList.add("6472");
            filterIdList.add("2301");
        } else if (pos == 1) {
            filterIdList.add("7088");
        } else if (pos == 2) {
            filterIdList.add("2679");
            filterIdList.add("2354");
            filterIdList.add("6472");
            filterIdList.add("2349");
        } else if (pos == 3) {
            filterIdList.add("1978");
            filterIdList.add("7088");
        } else if (pos == 4) {
            filterIdList.add("6632");
            filterIdList.add("3757");
        } else if (pos == 5) {
            filterIdList.add("6632");
            filterIdList.add("2275");
        } else if (pos == 6) {
            filterIdList.add("6632");
            filterIdList.add("2275");
            filterIdList.add("1850");
        } else if (pos == 7) {
            filterIdList.add("6632");
            filterIdList.add("2351");
        } else if (pos == 8) {
            filterIdList.add("6632");
            filterIdList.add("3690");
        } else if (pos == 9) {
            filterIdList.add("7088");
            filterIdList.add("3694");
        } else if (pos == 10) {
            filterIdList.add("7088");
            filterIdList.add("3699");
        } else if (pos == 11) {
            filterIdList.add("6957");
        } else if (pos == 12) {
            filterIdList.add("3699");
            filterIdList.add("3854");
        } else if (pos == 13) {
            filterIdList.add("6632");
            filterIdList.add("2275");
        } else if (pos == 14) {
            filterIdList.add("6611");
            filterIdList.add("1978");
        } else if (pos == 15) {
            filterIdList.add("6632");
            filterIdList.add("4652");
            filterIdList.add("2218");
            filterIdList.add("2218");
            filterIdList.add("2341");
            filterIdList.add("3734");
            filterIdList.add("2176");
        } else if (pos == 16) {
            filterIdList.add("6632");
            filterIdList.add("2341");
            filterIdList.add("2176");
        } else if (pos == 17) {
            filterIdList.add("2963");
            filterIdList.add("6611");
            filterIdList.add("6472");
        } else if (pos == 18) {
            filterIdList.add("7088");
            filterIdList.add("4652");
        } else if (pos == 19) {
            filterIdList.add("6956");
        } else if (pos == 20) {
            filterIdList.add("7088");
            filterIdList.add("1978");
            filterIdList.add("3392");
            filterIdList.add("2275");
        } else if (pos == 21) {
            filterIdList.add("6632");
            filterIdList.add("2570");
            filterIdList.add("2351");
        } else if (pos == 22) {
            filterIdList.add("6970");
        } else if (pos == 23) {
            filterIdList.add("6977");
        } else if (pos == 24) {
            filterIdList.add("1860");
            filterIdList.add("7088");
            filterIdList.add("2570");
        } else if (pos == 25) {
            filterIdList.add("7088");
            filterIdList.add("1096");
        } else if (pos == 26) {
            filterIdList.add("7088");
            filterIdList.add("8807");
        } else if (pos == 27) {
            filterIdList.add("7088");
            filterIdList.add("2343");
        } else if (pos == 28) {
            filterIdList.add("7225");
            filterIdList.add("8807");
        } else if (pos == 29) {
            filterIdList.add("1673");
            filterIdList.add("956");
        }
        return filterIdList;
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