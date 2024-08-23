package com.miczon.photoeditor.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.miczon.photoeditor.BaseActivity;
import com.miczon.photoeditor.R;
import com.miczon.photoeditor.adapter.IphoneFilterAdapter;
import com.miczon.photoeditor.helper.DialogHandler;
import com.miczon.photoeditor.utils.CenterDecoration;
import com.miczon.photoeditor.utils.CenterSnapHelper;
import com.miczon.photoeditor.utils.Constants;
import com.miczon.photoeditor.utils.InterstitialAdHandler;
import com.miczon.photoeditor.utils.Utility;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Grid;
import com.otaliastudios.cameraview.controls.Hdr;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.controls.WhiteBalance;
import com.otaliastudios.cameraview.filter.Filters;
import com.otaliastudios.cameraview.gesture.Gesture;
import com.otaliastudios.cameraview.gesture.GestureAction;
import com.otaliastudios.cameraview.markers.DefaultAutoFocusMarker;

public class SamsungCameraActivity extends BaseActivity {
    String TAG = "SamsungCameraActivity", savedImagePath = "";

    ImageView capturedResultView, backImageView, captureImageView, retakeImageView, closeFiltersView, flipCameraView, flashOnView, flashOffView, enableGridView;
    CameraView cameraView;
    RelativeLayout showFilters, optionsLayout, flashLayout;
    Button saveImageBtn;
    Bitmap capturedImageBitmap;
    RecyclerView rvFilterPreview;
    int lastPosition;
    IphoneFilterAdapter iphoneFilterAdapter;
    LinearLayoutManager linearLayoutManager;
    SnapHelper snapHelper;

    boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samsung_camera);

        cameraView = findViewById(R.id.cv_cameraView);
        capturedResultView = findViewById(R.id.iv_capturedResult);
        saveImageBtn = findViewById(R.id.btn_save);
        retakeImageView = findViewById(R.id.iv_retakeImage);
        captureImageView = findViewById(R.id.iv_captureImage);
        backImageView = findViewById(R.id.iv_back);
        rvFilterPreview = findViewById(R.id.recyclerView);
        showFilters = findViewById(R.id.rl_showFilters);
        closeFiltersView = findViewById(R.id.iv_closeFilters);
        flipCameraView = findViewById(R.id.iv_flip_cam);
        flashOnView = findViewById(R.id.iv_flash_on);
        flashOffView = findViewById(R.id.iv_flash_off);
        enableGridView = findViewById(R.id.iv_grid);
        optionsLayout = findViewById(R.id.rl_options);
        flashLayout = findViewById(R.id.rl_flash);

        snapHelper = new CenterSnapHelper();
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        iphoneFilterAdapter = new IphoneFilterAdapter(this, Utility.getInstance().iphoneCamFilterPreviews());

        cameraView.setLifecycleOwner(this);

        cameraView.setMode(Mode.PICTURE);
        cameraView.setExposureCorrection(2f);
        cameraView.setAutoFocusMarker(new DefaultAutoFocusMarker());
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM); // Pinch to zoom!
        cameraView.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS); // Tap to focus!
//        cameraView.setGrid(Grid.DRAW_3X3);

        /*
        To set default filter when camera is opened
         */
        setDefaultFilter();


        /*
        To set filter recyclerview
         */
        recyclerViewSetup();

        /*
         Contains implementation of all the click listeners
         */
        clickListeners();

        /*
        Implementation of double tap to switch camera
         */
        setDoubleTap();

    }


    /**
     * To display filters recycler and automatically set last applied filter
     */
    private void showFilters() {
        setNextFilter(lastPosition);
        if (lastPosition > 0) {
            captureImageView.setImageTintList(ColorStateList.valueOf(getColor(R.color.light_purple)));
        }
        rvFilterPreview.setVisibility(View.VISIBLE);
        showFilters.setVisibility(View.GONE);
        closeFiltersView.setVisibility(View.VISIBLE);
    }

    /**
     * Implementation of setting up recycler view adapter
     */
    private void recyclerViewSetup() {
        rvFilterPreview.setAdapter(iphoneFilterAdapter);
        rvFilterPreview.setLayoutManager(linearLayoutManager);
        rvFilterPreview.addItemDecoration(new CenterDecoration(0));
        snapHelper.attachToRecyclerView(rvFilterPreview);

        rvFilterPreview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                View centerView = snapHelper.findSnapView(linearLayoutManager);
                Integer pos = centerView != null ? linearLayoutManager.getPosition(centerView) : null;
                if (newState == RecyclerView.SCROLL_STATE_IDLE || (pos != null && pos == 0 && newState == RecyclerView.SCROLL_STATE_DRAGGING)) {
                    if (pos != null && !pos.equals(lastPosition)) {
                        Log.e(TAG, "positionView SCROLL_STATE_IDLE: " + pos);
                        lastPosition = pos;
                        if (pos == 0) {
                            captureImageView.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            setDefaultFilter();

                        } else {
                            captureImageView.setImageTintList(ColorStateList.valueOf(getColor(R.color.light_purple)));
                            setNextFilter(pos);
                        }
                    }
                }
            }
        });
    }

    /**
     * Method to handle single and double taps over camera screen
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setDoubleTap() {
        cameraView.setOnTouchListener(new View.OnTouchListener() {
            final GestureDetector gestureDetector = new GestureDetector(SamsungCameraActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.e(TAG, "onDoubleTap: camera face: " + cameraView.getFacing());
                    switchCamera();
                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent event) {
                    showFilters();
                    return false;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    /**
     * Method that contains implementation of click listeners
     */
    private void clickListeners() {
        captureImageView.setOnClickListener(v -> {
            cameraView.takePictureSnapshot();
            cameraView.setVisibility(View.GONE);
            retakeImageView.setVisibility(View.VISIBLE);
            backImageView.setVisibility(View.GONE);
            captureImageView.setVisibility(View.GONE);
            rvFilterPreview.setVisibility(View.GONE);
            showFilters.setVisibility(View.GONE);
            optionsLayout.setVisibility(View.GONE);
            closeFiltersView.setVisibility(View.GONE);
            saveImageBtn.setVisibility(View.VISIBLE);
        });

        retakeImageView.setOnClickListener(v -> retakeImage());

        backImageView.setOnClickListener(v -> onBackPressed());

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureShutter() {
                Log.e(TAG, "onPictureShutter: ");
                cameraView.setFlash(Flash.ON);
            }

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                Log.e("TAG", "onPictureTaken: image captured");
                InterstitialAdHandler.Companion.showInterstitialAd(SamsungCameraActivity.this, Constants.AdMob_Main_Interstitial_Ad_Id, () -> {
                    result.toBitmap(bitmap -> {
                        cameraView.setFlash(Flash.OFF);
                        capturedResultView.setImageBitmap(bitmap);
                        capturedImageBitmap = bitmap;
                    });
                    capturedResultView.setVisibility(View.VISIBLE);
                    return null;
                });
            }
        });

        showFilters.setOnClickListener(v -> showFilters());

        closeFiltersView.setOnClickListener(v -> {
            setDefaultFilter();
            closeFiltersView.setVisibility(View.GONE);
            rvFilterPreview.setVisibility(View.GONE);
            captureImageView.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
            showFilters.setVisibility(View.VISIBLE);
        });

        saveImageBtn.setOnClickListener(v -> {
            savedImagePath = Utility.getInstance().saveImageToGallery(SamsungCameraActivity.this, capturedImageBitmap);
            if (savedImagePath != null && !savedImagePath.isEmpty()) {
                DialogHandler.getInstance().showSaveDialog(SamsungCameraActivity.this, true, "camEdits", (status, message, data, alertDialog) -> {
                    if (status.equalsIgnoreCase("0")) {
                        Intent intent = new Intent(SamsungCameraActivity.this, ImageEditActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("from", TAG);
                        intent.putExtra("image_uri", savedImagePath);
                        alertDialog.dismiss();
                        startActivity(intent);

                    } else if (status.equalsIgnoreCase("1")) {
                        alertDialog.dismiss();
                        Intent intent = new Intent(SamsungCameraActivity.this, ShowImageActivity.class);
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
            }
        });

        enableGridView.setOnClickListener(v -> {
            if (isFirstTime) {
                enableGridView.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_purple)));
                cameraView.setGrid(Grid.DRAW_3X3);
                isFirstTime = false;
            } else {
                enableGridView.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                cameraView.setGrid(Grid.OFF);
                isFirstTime = true;
            }
        });

        flipCameraView.setOnClickListener(v -> switchCamera());

        flashOnView.setOnClickListener(v -> handleFlashLight());

        flashOffView.setOnClickListener(v -> handleFlashLight());
    }

    private void handleFlashLight() {
        Log.e(TAG, "handleFlashLight: flash: " + cameraView.getFlash());
        if (cameraView.getFlash().equals(Flash.OFF)) {
            cameraView.setFlash(Flash.TORCH);
            flashOffView.setVisibility(View.GONE);
            flashOnView.setVisibility(View.VISIBLE);

        } else if (cameraView.getFlash().equals(Flash.TORCH)) {
            cameraView.setFlash(Flash.OFF);
            flashOffView.setVisibility(View.VISIBLE);
            flashOnView.setVisibility(View.GONE);
        }
    }

    /**
     * Method to control visibility of settings while retake image button is clicked
     */
    private void retakeImage() {
        capturedResultView.setVisibility(View.GONE);
        cameraView.setVisibility(View.VISIBLE);
        retakeImageView.setVisibility(View.GONE);
        backImageView.setVisibility(View.VISIBLE);
        captureImageView.setVisibility(View.VISIBLE);
        showFilters.setVisibility(View.VISIBLE);
        optionsLayout.setVisibility(View.VISIBLE);
        saveImageBtn.setVisibility(View.GONE);
        flashOffView.setVisibility(View.VISIBLE);
        flashOnView.setVisibility(View.GONE);
    }

    /**
     * This contains the implementation of applying filter when screen is shown for the first time
     */
    private void setDefaultFilter() {
        cameraView.setDrawHardwareOverlays(true);
        cameraView.setHdr(Hdr.ON);
        cameraView.setWhiteBalance(WhiteBalance.AUTO);
        cameraView.setFilter(Filters.CONTRAST.newInstance());
    }

    /**
     * Method to switch camera i.e. front camera or back camera
     */
    private void switchCamera() {
        if (cameraView.getFacing().equals(Facing.BACK)) {
            cameraView.setFacing(Facing.FRONT);
            flashLayout.setVisibility(View.GONE);
        } else if (cameraView.getFacing().equals(Facing.FRONT)) {
            cameraView.setFacing(Facing.BACK);
            flashLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * To set next filter as the item is swiped
     *
     * @param position: position of filter preview image to retrieve respective filter
     */
    private void setNextFilter(int position) {
        if (position == 1) {
            cameraView.setFilter(Filters.GRAYSCALE.newInstance());

        } else if (position == 2) {
            cameraView.setFilter(Filters.HUE.newInstance());

        } else if (position == 3) {
            cameraView.setFilter(Filters.INVERT_COLORS.newInstance());

        } else if (position == 4) {
            cameraView.setFilter(Filters.LOMOISH.newInstance());

        } else if (position == 5) {
            cameraView.setFilter(Filters.POSTERIZE.newInstance());

        } else if (position == 6) {
            cameraView.setFilter(Filters.SATURATION.newInstance());

        } else if (position == 7) {
            cameraView.setFilter(Filters.SEPIA.newInstance());

        } else if (position == 8) {
            cameraView.setFilter(Filters.SHARPNESS.newInstance());

        } else if (position == 9) {
            cameraView.setFilter(Filters.TEMPERATURE.newInstance());

        } else if (position == 10) {
            cameraView.setFilter(Filters.TINT.newInstance());

        } else if (position == 11) {
            cameraView.setFilter(Filters.VIGNETTE.newInstance());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }

    @Override
    public void onBackPressed() {
        DialogHandler.getInstance().exitDialog(SamsungCameraActivity.this, true, (status, message, data, alertDialog) -> {
            if (status != null) {
                if (status.equals("0")) {
                    alertDialog.dismiss();

                } else {
                    alertDialog.dismiss();
                    if (cameraView != null && cameraView.isTakingPicture() && cameraView.isOpened()) {
                        cameraView.destroy();
                    }
                    startActivity(new Intent(SamsungCameraActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            }
        });
    }
}