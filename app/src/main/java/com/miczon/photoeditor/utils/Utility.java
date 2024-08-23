package com.miczon.photoeditor.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.miczon.photoeditor.R;
import com.miczon.photoeditor.helper.InAppBillingHelper;
import com.miczon.photoeditor.helper.PrefsManager;
import com.yalantis.ucrop.UCrop;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import org.imaginativeworld.whynotimagecarousel.model.CarouselType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class Utility {

    String TAG = "Utility";
    public static Utility mInstance = null;
    Bitmap urlToBitmap;
    public static boolean isImageLoaded = false;

    public static Utility getInstance() {
        if (mInstance == null) {
            mInstance = new Utility();
        }
        return mInstance;
    }

    public void addSplashAnimation(View view1, View view2, View view3, boolean shouldStart) {
        TranslateAnimation topToBottom1 = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -400);

        topToBottom1.setDuration(10000);
        topToBottom1.setFillAfter(true);
        topToBottom1.setRepeatCount(Animation.INFINITE);
        topToBottom1.setRepeatMode(Animation.REVERSE);
        topToBottom1.setInterpolator(new LinearInterpolator());

        TranslateAnimation topToBottom2 = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -300);

        topToBottom2.setDuration(10000);
        topToBottom2.setFillAfter(true);
        topToBottom2.setRepeatCount(Animation.INFINITE);
        topToBottom2.setRepeatMode(Animation.REVERSE);
        topToBottom2.setInterpolator(new LinearInterpolator());

        TranslateAnimation topToBottom3 = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -200);

        topToBottom3.setDuration(10000);
        topToBottom3.setFillAfter(true);
        topToBottom3.setRepeatCount(Animation.INFINITE);
        topToBottom3.setRepeatMode(Animation.REVERSE);
        topToBottom3.setInterpolator(new LinearInterpolator());

        if (shouldStart) {
            view1.startAnimation(topToBottom1);
            view2.startAnimation(topToBottom3);
            view3.startAnimation(topToBottom2);
        } else {
            view1.getAnimation().cancel();
            view2.getAnimation().cancel();
            view3.getAnimation().cancel();
        }
    }

    /**
     * Method to verify whether if app is downloaded from playstore or not
     *
     * @return: boolean flag
     */
    public boolean verifyInstallerId(Context context) {
        // A list with valid installers package names
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));
        //A list with invalid installers package names
        List<String> invalidInstallers = new ArrayList<>(Arrays.asList("com.lenovo.anyshare.gps", "com.dewmobile.kuaiya.play"));
        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        if (installer != null) {
            if (validInstallers.contains(installer))
                return true;
            else if (invalidInstallers.contains(installer))
                return false;
            else
                return false;
        } else
            return false;
    }

    /**
     * To check if premium sub is bought or not
     *
     * @param activity: from where it is called
     * @return: flag that contains state of premium i.e bought or not
     */
    public boolean isPremiumActive(Activity activity) {
        PrefsManager prefsManager = new PrefsManager(activity.getApplicationContext());
        InAppBillingHelper.getInstance().initialiseBillingClient(activity);
        InAppBillingHelper.getInstance().establishConnection();
        InAppBillingHelper.getInstance().purchasedSubVerification();
        if (prefsManager.getIsPremium()) {

            Log.e(TAG, "isPremiumActive: is subscribed: " + prefsManager.getIsPremium());
            return true;

        } else {
            Log.e(TAG, "isPremiumActive: not subscribed: " + prefsManager.getIsPremium());
            return false;
        }
    }

    /**
     * To get URI of image from its path
     *
     * @param context:   from where it is called
     * @param imagePath: path of image
     * @return: uri of image
     */
    @SuppressLint("Range")
    public Uri getImageContentUri(Context context, String imagePath) {
        if (imagePath == null) {
            return null;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{imagePath};
        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = contentResolver.query(contentUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long imageId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                return Uri.withAppendedPath(contentUri, "" + imageId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the original content URI if the image's content URI is not found
        return Uri.parse("file://" + imagePath);
    }

    /**
     * to modify crop library toolbar's color
     *
     * @param context: from where it is invoked
     * @return options object to apply it on library object
     */
    public static UCrop.Options imageCropperStyle(Context context) {
        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true);
        options.setToolbarTitle(context.getString(R.string.cropper_lbl));
        options.setToolbarColor(context.getResources().getColor(R.color.black));
        options.setToolbarWidgetColor(context.getResources().getColor(R.color.white));
        options.setStatusBarColor(context.getResources().getColor(R.color.black));
        options.setToolbarCancelDrawable(R.drawable.ic_nav_back);
        options.setToolbarCropDrawable(R.drawable.ic_golden_tick);
        return options;
    }

    /**
     * To get only file name part from whole path
     *
     * @param filePath: file path from where name is to be extracted
     * @return: file name
     */
    public static String extractFileName(String filePath) {
        int lastIndex = filePath.lastIndexOf('/');
        if (lastIndex == -1) {
            return filePath;
        }

        String lastPart = filePath.substring(lastIndex + 1);

        int dotIndex = lastPart.lastIndexOf('.');
        if (dotIndex == -1) {
            return lastPart;
        }

        return lastPart.substring(0, dotIndex);
    }

    /**
     * To get path of image when it is selected via gallery by user
     *
     * @param uri: uri of picture whose path is to be retrieved
     */
    public String getPath(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }

        if (result == null && "content".equalsIgnoreCase(uri.getScheme())) {
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                File file = createTempFile(context);
                OutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                outputStream.close();
                result = file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Method to save image to gallery
     *
     * @param context:     from where it is called
     * @param imageBitmap: bitmap of image to be saved
     * @return: path of image that is saved
     */
    public String saveImageToGallery(Context context, Bitmap imageBitmap) {
        String path = "";

        String galleryFolderName = "Photo Editor";
        String imageFileName = "Image_" + System.currentTimeMillis() + ".png";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API level 29) and higher, use MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            values.put(MediaStore.Images.Media.DESCRIPTION, "Saved from Photo Editor app");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + galleryFolderName);

            ContentResolver contentResolver = context.getContentResolver();
            Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                try {
                    OutputStream outputStream = contentResolver.openOutputStream(imageUri);
                    if (outputStream != null) {
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
//                        Toast.makeText(context, context.getString(R.string.saved_gall), Toast.LENGTH_SHORT).show();
                        path = imageUri.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // For Android 9 (API level 28) and below, use the old approach
            File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

            File galleryDir = new File(externalStorageDir, galleryFolderName);
            galleryDir.mkdirs();

            File imageFile = new File(galleryDir, imageFileName);

            Log.e(TAG, "saveImageToGallery: path is: " + imageFile);

            try {
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
//                Toast.makeText(context, context.getString(R.string.saved_gall), Toast.LENGTH_SHORT).show();
                // Skip the media scan for the image file
                MediaScannerConnection.scanFile(context, new String[]{imageFile.toString()}, null, null);

                path = imageFile.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    /**
     * to create temporary file to hold path of uri requested
     *
     * @param context: from where it is called
     * @return temporary file path
     */
    public File createTempFile(Context context) throws IOException {
        Log.e(TAG, "createTempFile: called");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        return File.createTempFile("IMG_", ".jpg", storageDir);
    }


    /**
     * to convert images from url and local path into bitmap
     *
     * @param imagePath: image path/url
     */
    public Bitmap loadImageFromPath(Context context, String imagePath) {
        Log.e(TAG, "loadImageFromPath: working");
        File imageFile = new File(imagePath);
        Log.e(TAG, "run: path: " + imagePath);
        Log.e(TAG, "run: file: " + imageFile);
        if (imageFile.exists()) {
            Log.e(TAG, "loadImageFromPath: inside if");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(imagePath, options);
        } else {
            if (urlToBitmap == null) {
                final CountDownLatch latch = new CountDownLatch(1);

                new Thread(() -> {
                    try {
                        Log.e(TAG, "loadImageFromPath: inside try path: " + imagePath);
                        if (imagePath.contains("http") || imagePath.contains("https")) {
                            InputStream inputStream = new URL(imagePath).openStream();
                            urlToBitmap = BitmapFactory.decodeStream(inputStream);
                        } else {
                            ContentResolver contentResolver = context.getContentResolver();
                            InputStream inputStream = contentResolver.openInputStream(Uri.parse(imagePath));
                            urlToBitmap = BitmapFactory.decodeStream(inputStream);
                        }
                        Log.e(TAG, "run: bitmap" + urlToBitmap);
                    } catch (Exception e) {
                        Log.e(TAG, "loadImageFromPath: exception: " + e);
                    } finally {
                        latch.countDown();// Signal the completion of the thread
                    }
                }).start();

                try {
                    latch.await(); // Wait until the latch count reaches zero
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "loadImageFromPath: else working" + urlToBitmap);
            }

            // Retrieve the bitmap and reset the variable to null
            Bitmap bitmap = urlToBitmap;
            isImageLoaded = true;
            urlToBitmap = null;
            return bitmap;
        }
    }

    /**
     * to convert bitmap to uri
     *
     * @param context: from where it is invoked
     * @param bitmap:  image in bitmap
     */
    public Uri bitmapToUri(Context context, Bitmap bitmap, String imgName) {
        Log.e(TAG, "bitmapToUri: inside ");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "", mimeType;

        if (!imgName.isEmpty()) {
            imageName = imgName;
            mimeType = "";
        } else {
            imageName = "Image_" + timeStamp + ".jpg";
            mimeType = "image/jpg";
        }

        // Create a subdirectory within the "Pictures" directory for your app
        File appPicturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Photo Editor");
        if (!appPicturesDir.exists()) {
            appPicturesDir.mkdirs();
        }

        // Create the image file within your app's subdirectory
        File imageFile = new File(appPicturesDir, imageName);

        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType);

        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        try {
            OutputStream outputStream = resolver.openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageUri;
    }

    /**
     * To get URI of image from Bitmap
     *
     * @param context: from where it is called
     * @param bitmap:  bitmap of image to be converted to URI
     * @param imgName: name of same image
     * @return: URI of image
     */
    public Uri shareBitmapToUri(Context context, Bitmap bitmap, String imgName) {
        File imageFile = new File(context.getExternalFilesDir(null), imgName);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", imageFile);
    }

    /**
     * To create temp file when captured from camera
     *
     * @param context: from where it is called
     * @return: file
     * @throws: IOException
     */
    public File createCamFile(Context context) throws IOException {
        Log.e(TAG, "createTempFile: called");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp;
        Log.e(TAG, "createCamFile: file name: " + fileName);
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    /**
     * To display loader
     *
     * @param context:     the activity that it is called from
     * @param strokeWidth: the width of circle's stroke
     * @param radius:      the round radius of circle
     * @param color:       color of circle
     */
    public CircularProgressDrawable showLoader(Context context, int strokeWidth, int radius, int color) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(strokeWidth);
        circularProgressDrawable.setCenterRadius(radius);
        circularProgressDrawable.setColorSchemeColors(color);
        circularProgressDrawable.start();

        return circularProgressDrawable;
    }



    public void initializeCarousel(Context context, ImageCarousel carousel) {

        List<CarouselItem> list = new ArrayList<>();

        list.add(new CarouselItem(R.drawable.ic_home_carousel_1));
        list.add(new CarouselItem(R.drawable.ic_home_carousel_2));
        list.add(new CarouselItem(R.drawable.ic_home_carousel_3));
        list.add(new CarouselItem(R.drawable.ic_home_carousel_4));
        list.add(new CarouselItem(R.drawable.ic_home_carousel_5));

        carousel.setShowIndicator(true);
        carousel.setShowNavigationButtons(false);
        carousel.setImageScaleType(ImageView.ScaleType.CENTER);
        carousel.setCarouselBackground(new ColorDrawable(Color.parseColor("#000000")));
        carousel.setCarouselType(CarouselType.BLOCK);
        carousel.setScaleOnScroll(true);
        carousel.setScalingFactor(.1f);
        carousel.setAutoWidthFixing(true);
        carousel.setAutoPlay(true);
        carousel.setAutoPlayDelay(2000);
        carousel.setTouchToPause(true);
        carousel.setInfiniteCarousel(true);

        carousel.setData(list);
    }

    /**
     * Method to pass country names to country adapter, to display it in select language
     *
     * @return: array list of names
     */
    public ArrayList<String> countryNames() {
        ArrayList<String> countryName = new ArrayList<>();
        countryName.add("English " + "-" + " English");
        countryName.add("Afrikaans " + "-" + " Afrikaans");
        countryName.add("Arabic " + "-" + "العربية ");
        countryName.add("Chinese " + "-" + " 中文");
        countryName.add("Czech " + "-" + " Čeština");
        countryName.add("Dutch " + "-" + " Niederländisch");
        countryName.add("French " + "-" + " Français");
        countryName.add("German " + "-" + " Deutsch");
        countryName.add("Greek " + "-" + " Ελληνικά");
        countryName.add("Hindi " + "-" + " हिन्दी");
        countryName.add("Indonesian " + "-" + " Bahasa Indonesia");
        countryName.add("Italian " + "-" + " Italiano");
        countryName.add("Japanese " + "-" + " 日本語");
        countryName.add("Korean " + "-" + " 한국어");
        countryName.add("Malay " + "-" + " Melayu");
        countryName.add("Norwegian " + "-" + " Norsk");
        countryName.add("Persian " + "-" + " فارسی");
        countryName.add("Portuguese " + "-" + " Português");
        countryName.add("Russian " + "-" + " Pусский");
        countryName.add("Spanish " + "-" + " Español");
        countryName.add("Thai " + "-" + " ไทย");
        countryName.add("Turkish " + "-" + " Türkçe");
        countryName.add("Vietnamese " + "-" + " Tiếng Việt");

        return countryName;
    }

    /**
     * Method to pass country images to country adapter, to display it in select language
     *
     * @return: array list of country flag id's stored in drawable
     */
    public ArrayList<Integer> iphoneCamFilterPreviews() {
        ArrayList<Integer> camPreview = new ArrayList<>();
        camPreview.add(R.drawable.ic_capture_image);
        camPreview.add(R.drawable.test);
        camPreview.add(R.drawable.test);
        camPreview.add(R.drawable.test);
        camPreview.add(R.drawable.test);
        camPreview.add(R.drawable.test);
        camPreview.add(R.drawable.test);
        camPreview.add(R.drawable.test);
        camPreview.add(R.drawable.test);
        camPreview.add(R.drawable.test);

        return camPreview;
    }

    /**
     * Method to pass country images to country adapter, to display it in select language
     *
     * @return: array list of country flag id's stored in drawable
     */
    public ArrayList<Integer> countryFlags() {
        ArrayList<Integer> countryFlags = new ArrayList<>();
        countryFlags.add(R.drawable.ic_english);
        countryFlags.add(R.drawable.ic_afrikaans);
        countryFlags.add(R.drawable.ic_arabic);
        countryFlags.add(R.drawable.ic_china);
        countryFlags.add(R.drawable.ic_czech);
        countryFlags.add(R.drawable.ic_netherlands);
        countryFlags.add(R.drawable.ic_france);
        countryFlags.add(R.drawable.ic_germany);
        countryFlags.add(R.drawable.ic_greece);
        countryFlags.add(R.drawable.ic_india);
        countryFlags.add(R.drawable.ic_indonesia);
        countryFlags.add(R.drawable.ic_italy);
        countryFlags.add(R.drawable.ic_japan);
        countryFlags.add(R.drawable.ic_korean);
        countryFlags.add(R.drawable.ic_malay);
        countryFlags.add(R.drawable.ic_norway);
        countryFlags.add(R.drawable.ic_perisan);
        countryFlags.add(R.drawable.ic_portugal);
        countryFlags.add(R.drawable.ic_russia);
        countryFlags.add(R.drawable.ic_spain);
        countryFlags.add(R.drawable.ic_thailand);
        countryFlags.add(R.drawable.ic_turkey);
        countryFlags.add(R.drawable.ic_vietnam);

        return countryFlags;
    }

    /**
     * Method to pass filter preview images to filter adapter inside apply effect activity
     *
     * @return: array list of filter preview picture ids stored in drawable
     */
    public ArrayList<Integer> filterPreview() {
        ArrayList<Integer> filterInfo = new ArrayList<>();
//        filterInfo.add(R.drawable.ic_filter_0);
        filterInfo.add(R.drawable.ic_filter_20);
        filterInfo.add(R.drawable.ic_filter_3);
        filterInfo.add(R.drawable.ic_filter_4);
        filterInfo.add(R.drawable.ic_filter_5);
        filterInfo.add(R.drawable.ic_filter_6);
        filterInfo.add(R.drawable.ic_filter_7);
        filterInfo.add(R.drawable.ic_filter_8);
        filterInfo.add(R.drawable.ic_filter_9);
        filterInfo.add(R.drawable.ic_filter_10);
        filterInfo.add(R.drawable.ic_filter_11);
        filterInfo.add(R.drawable.ic_filter_12);
        filterInfo.add(R.drawable.ic_filter_13);
        filterInfo.add(R.drawable.ic_filter_14);
        filterInfo.add(R.drawable.ic_filter_15);
        filterInfo.add(R.drawable.ic_filter_16);
        filterInfo.add(R.drawable.ic_filter_17);
        filterInfo.add(R.drawable.ic_filter_18);
        filterInfo.add(R.drawable.ic_filter_19);
        filterInfo.add(R.drawable.ic_filter_29);
        filterInfo.add(R.drawable.ic_filter_21);
        filterInfo.add(R.drawable.ic_filter_22);
        filterInfo.add(R.drawable.ic_filter_23);
        filterInfo.add(R.drawable.ic_filter_24);
        filterInfo.add(R.drawable.ic_filter_25);
        filterInfo.add(R.drawable.ic_filter_26);
        filterInfo.add(R.drawable.ic_filter_27);
        filterInfo.add(R.drawable.ic_filter_28);
        filterInfo.add(R.drawable.ic_filter_1);
        filterInfo.add(R.drawable.ic_filter_2);
        filterInfo.add(R.drawable.ic_filter_30);

        return filterInfo;
    }


}
