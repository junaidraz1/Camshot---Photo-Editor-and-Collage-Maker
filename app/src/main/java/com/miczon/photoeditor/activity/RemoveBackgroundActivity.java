package com.miczon.photoeditor.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.miczon.photoeditor.R;
import com.miczon.photoeditor.helper.ConnectionManager;
import com.miczon.photoeditor.helper.DialogHandler;
import com.miczon.photoeditor.retrofit.apiUtils;
import com.miczon.photoeditor.utils.Constants;
import com.miczon.photoeditor.utils.Utility;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RemoveBackgroundActivity extends AppCompatActivity {

    String TAG = "RemoveBackgroundActivity", savedImagePath = "";

    ImageView editedImage, viewOriginalImage;
    Button uploadImageBtn;
    TextView tvSavingImage;
    RelativeLayout saveImageLayout, backLayout, imagePreviewLayout, savingImageLayout;
    LinearLayout uploadImageMsgLayout;
    LottieAnimationView animationView, animationView2;

    File camPhotoFile = null;
    Uri capturedImageUri, originalImageUri;
    Bitmap finalBitmapResult;
    AlertDialog alertDialog;
    Call<ResponseBody> uploadImageCall;

    boolean isPhotoSaved = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_background);

        editedImage = findViewById(R.id.iv_displayImage);
        uploadImageBtn = findViewById(R.id.btn_uploadImage);
        viewOriginalImage = findViewById(R.id.iv_original);
        saveImageLayout = findViewById(R.id.rl_saveImage);
        backLayout = findViewById(R.id.rl_back);
        uploadImageMsgLayout = findViewById(R.id.ll_uploadImage);
        imagePreviewLayout = findViewById(R.id.rl_previewLayout);
        animationView = findViewById(R.id.animation_view);
        animationView2 = findViewById(R.id.animation_view2);
        savingImageLayout = findViewById(R.id.rl_savingPicture);
        tvSavingImage = findViewById(R.id.tv_savingImage);

        choosePictureDialog();

        uploadImageBtn.setOnClickListener(v -> choosePictureDialog());

        viewOriginalImage.setOnLongClickListener(v -> {
            if (originalImageUri != null) {
                editedImage.setImageURI(originalImageUri);
            } else {
                viewOriginalImage.setVisibility(View.GONE);
                Log.e(TAG, "onLongClick: original bitmap is null");
            }
            return false;
        });

        viewOriginalImage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (finalBitmapResult != null) {
                    Glide.with(RemoveBackgroundActivity.this)
                            .load(finalBitmapResult)
                            .into(editedImage);
                } else {
                    viewOriginalImage.setVisibility(View.GONE);
                    Log.e(TAG, "onLongClick: finalImageBitmap is null");
                }
            }
            return false;
        });

        backLayout.setOnClickListener(v -> onBackPressed());

        saveImageLayout.setOnClickListener(v -> {
            savingImageLayout.setVisibility(View.VISIBLE);
            uploadImageBtn.setVisibility(View.GONE);
            YoYo.with(Techniques.FadeIn)
                    .duration(2000)
                    .repeat(Animation.INFINITE)
                    .playOn(tvSavingImage);
            animationView2.playAnimation();

            if (finalBitmapResult != null) {
                new Handler().postDelayed(() -> {
                    savingImageLayout.setVisibility(View.GONE);
                    savedImagePath = Utility.getInstance().saveImageToGallery(RemoveBackgroundActivity.this, finalBitmapResult);
                    isPhotoSaved = true;
                    Log.e(TAG, "onCreate: image path: " + savedImagePath);
                    uploadImageBtn.setVisibility(View.VISIBLE);

                    DialogHandler.getInstance().showSaveDialog(RemoveBackgroundActivity.this, false, "", (status, message, data, alertDialog) -> {
                        if (status.equals("0")) {
                            Intent intent = new Intent(RemoveBackgroundActivity.this, ImageEditActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("from", TAG);
                            intent.putExtra("image_uri", savedImagePath);
                            alertDialog.dismiss();
                            startActivity(intent);
                        } else if (status.equals("1")) {
                            alertDialog.dismiss();
                            Intent intent = new Intent(RemoveBackgroundActivity.this, ShowImageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            if (!savedImagePath.isEmpty()) {
                                intent.putExtra("image_uri", savedImagePath);
                                intent.putExtra("from", TAG);
                                startActivity(intent);
                            } else {
                                Log.e(TAG, "onCreate: save bitmap path is null");
                            }
                        }
                    });
                }, 5000);
            }
        });
    }

    private void choosePictureDialog() {
        DialogHandler.getInstance().choosePictureDialog(RemoveBackgroundActivity.this, true, (status, message, data, alertDialog) -> {
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

    private void checkCameraPerm() {
        Log.e(TAG, "askStoragePermission: inside if");
        if (ContextCompat.checkSelfPermission(RemoveBackgroundActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "askStoragePermission: inside second if");
            if (ActivityCompat.shouldShowRequestPermissionRationale(RemoveBackgroundActivity.this, Manifest.permission.CAMERA)) {
                Log.e(TAG, "askStoragePermission: inside third if");
                ActivityCompat.requestPermissions(RemoveBackgroundActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(RemoveBackgroundActivity.this, Manifest.permission.CAMERA)) {
                Log.e(TAG, "askStoragePermission: inside third else if");
                ActivityCompat.requestPermissions(RemoveBackgroundActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);

            } else {
                Log.e(TAG, "askStoragePermission: inside third else");
                ActivityCompat.requestPermissions(RemoveBackgroundActivity.this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA_ACCESS);
            }
        } else {
            Log.e(TAG, "askStoragePermission: inside else of first if");
            startCameraIntent();
        }
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
            camPhotoFile = Utility.getInstance().createCamFile(RemoveBackgroundActivity.this);
        } catch (IOException ex) {
            Log.e(TAG, "startCameraIntent: exception: " + ex.getLocalizedMessage());
        }

        if (camPhotoFile != null) {
            try {
                Log.e(TAG, "startCameraIntent: file: " + camPhotoFile.getAbsolutePath());
                capturedImageUri = FileProvider.getUriForFile(RemoveBackgroundActivity.this,
                        RemoveBackgroundActivity.this.getPackageName() + ".provider",
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
            if (path != null) {
                originalImageUri = path;
                String imageString = Utility.getInstance().getPath(this, path);
                Log.e(TAG, "onActivityResult: image string: " + imageString);
                uploadImage(imageString);
            } else {
                Log.e(TAG, "onActivityResult: bitmap null");
            }

          /*  if (from != null && from.equalsIgnoreCase("intent")) {
                handleIntentAction(path, "intent");

            } else {
                handleIntentAction(path, "home");
            }*/

        } else if (requestCode == Constants.CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(camPhotoFile.getAbsolutePath());

                // Rotate the bitmap based on Exif orientation
                Bitmap rotatedBitmap = rotateBitmap(bitmap, camPhotoFile.getAbsolutePath());

                Uri path = Utility.getInstance().bitmapToUri(RemoveBackgroundActivity.this, rotatedBitmap, "");
                Log.e(TAG, "onActivityResult: path: " + path.getPath());
                originalImageUri = path;
                String imageString = Utility.getInstance().getPath(this, path);
                uploadImage(imageString);
                /*handleIntentAction(path, "camera");*/


            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: error: " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Data is Null");
        }
    }

    private void uploadImage(String path) {
        if (ConnectionManager.getInstance().isNetworkAvailable(this)) {
            alertDialog = DialogHandler.getInstance().filterProgress(RemoveBackgroundActivity.this, getString(R.string.convert_header),
                    getString(R.string.convert_body), "uploadImage", 2, alertDialog);
            File file = new File(path);
            if (!file.exists()) {
                Log.e(TAG, "uploadImage: File does not exist");
                return;
            } else {
                Log.e(TAG, "uploadImage: file exists: " + file.getPath());
            }

            Log.e(TAG, "uploadImage: file is: " + file);
            Log.e(TAG, "uploadImage: path is: " + path);

            String type = URLConnection.guessContentTypeFromName(file.getName());

            Log.e(TAG, "uploadImage: file type is: " + type);

            MultipartBody.Part fileToSend = MultipartBody.Part.createFormData("image", file.getName(), RequestBody.create(MediaType.parse(type), file));

            uploadImageCall = apiUtils.getAPIService(RemoveBackgroundActivity.this, "bgRemover").removeBg(fileToSend);
            uploadImageCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (alertDialog != null && alertDialog.isShowing()) {
                                alertDialog.dismiss();
                            }
                            finalBitmapResult = BitmapFactory.decodeStream(response.body().byteStream());
                            editedImage.setImageBitmap(finalBitmapResult);
                            saveImageLayout.setVisibility(View.VISIBLE);
                            viewOriginalImage.setVisibility(View.VISIBLE);
                            animationView.playAnimation();
                            imagePreviewLayout.setVisibility(View.VISIBLE);
                            uploadImageMsgLayout.setVisibility(View.GONE);

                        } else {
                            Log.e(TAG, "onResponse: response body is null");
                            saveImageLayout.setVisibility(View.GONE);
                            DialogHandler.isFirstTime = true;
                            if (alertDialog != null && alertDialog.isShowing()) {
                                alertDialog.dismiss();
                            }
                            DialogHandler.getInstance().showBottomSheet(RemoveBackgroundActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                                    "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                        if (alertDialog != null && alertDialog.isShowing()) {
                                            alertDialog.dismiss();
                                        }
                                        startActivity(new Intent(RemoveBackgroundActivity.this, HomeActivity.class)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    });
                        }

                    } else {
                        Log.e(TAG, "onResponse: response not successfull: " + response.message());
                        saveImageLayout.setVisibility(View.GONE);
                        DialogHandler.isFirstTime = true;
                        if (alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                        DialogHandler.getInstance().showBottomSheet(RemoveBackgroundActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                                "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                    if (alertDialog != null && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    startActivity(new Intent(RemoveBackgroundActivity.this, HomeActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
                    saveImageLayout.setVisibility(View.GONE);
                    DialogHandler.isFirstTime = true;
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                    DialogHandler.getInstance().showBottomSheet(RemoveBackgroundActivity.this, getString(R.string.app_name), getString(R.string.con_error_msg),
                            "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                startActivity(new Intent(RemoveBackgroundActivity.this, HomeActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            });
                }
            });
        } else {
            saveImageLayout.setVisibility(View.GONE);
            DialogHandler.getInstance().showBottomSheet(RemoveBackgroundActivity.this, getString(R.string.app_name), getString(R.string.internet_con_msg),
                    "", "", getString(R.string.ok_label), "connectionErr", false, itemClicked -> {
                        if (alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                        startActivity(new Intent(RemoveBackgroundActivity.this, HomeActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    });
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
    public void onBackPressed() {
        DialogHandler.getInstance().exitDialog(RemoveBackgroundActivity.this, true, (status, message, data, alertDialog) -> {
            if (status != null) {
                if (status.equals("0")) {
                    alertDialog.dismiss();

                } else {
                    alertDialog.dismiss();
                    if (uploadImageCall != null && !uploadImageCall.isCanceled()) {
                        Log.e(TAG, "onBackPressed: uploadImageCall is cancelled");
                        uploadImageCall.cancel();
                    }
                    finish();
                    startActivity(new Intent(RemoveBackgroundActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            }
        });
    }

    /**
     * OnDestroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        if (uploadImageCall != null && !uploadImageCall.isCanceled()) {
            Log.e(TAG, "onDestroy: uploadImageCall is cancelled");
            uploadImageCall.cancel();

        } else {
            Log.e(TAG, "onDestroy: uploadImageCall obj is null");
        }
    }
}