@file:Suppress("DEPRECATION")

package com.miczon.photoeditor.activity


import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.LoadAdError
import com.miczon.photoeditor.BaseActivity
import com.miczon.photoeditor.R
import com.miczon.photoeditor.activity.MainActivity.Companion.isFromSaved
import com.miczon.photoeditor.databinding.ActivityMyCreationBinding
import com.miczon.photoeditor.eventListeners.BannerAdLoadListener
import com.miczon.photoeditor.helper.BannerAdManager
import com.miczon.photoeditor.helper.DialogHandler
import com.miczon.photoeditor.utils.Constants
import com.miczon.photoeditor.utils.InterstitialAdHandler.Companion.showInterstitialAd
import com.miczon.photoeditor.utils.Utility
import java.io.File

class MyCreationActivity : BaseActivity(), BannerAdLoadListener {

    private var TAG = "MyCreationActivity"
    private lateinit var imgPath1: java.util.ArrayList<FileModel>
    private lateinit var imagePathList: java.util.ArrayList<String>
    private lateinit var imageNameList: java.util.ArrayList<String>
    private var mLastClickTime: Long = 0
    private lateinit var binding: ActivityMyCreationBinding
    private lateinit var galleryUri: Uri
    private lateinit var shareImageUri: Uri
    private lateinit var selectionArgs: Array<String>
    private lateinit var path: String
    private lateinit var newName: String
    private var isForcedOverwrite = false
    private var isFromRename = false
    private lateinit var bannerAdManager: BannerAdManager


    private var deleteLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var renameLauncher: ActivityResultLauncher<IntentSenderRequest?>? = null

    fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePathList = java.util.ArrayList()
        imageNameList = java.util.ArrayList()

        binding = ActivityMyCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bannerAdManager = BannerAdManager(this, this)

        if (!Utility.getInstance().isPremiumActive(this@MyCreationActivity)) {
            bannerAdManager.loadBannerAd(findViewById(R.id.fl_adContainer))
            binding.rlAdContainer.visibility = View.VISIBLE
            binding.flAdContainer.visibility = View.VISIBLE
        } else {
            binding.rlAdContainer.visibility = View.GONE
            binding.flAdContainer.visibility = View.GONE
        }

        bannerAdManager.loadBannerAd(binding.flAdContainer)

        binding.listCreation.layoutManager =
            GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)

        binding.rlBack.setOnClickListener {
            onBackPressed()
        }

        handleDeleteLauncherIntent()
        handleRenameLauncherIntent()

        Log.e("Page", "My creation")

        displayGallery()

    }

    /**
     * Method to handle picture delete request in android version greater than 10
     */
    private fun handleDeleteLauncherIntent() {
        deleteLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                Log.e(TAG, "handleLauncherIntent: result code: " + result.resultCode)
                // Handle the result of the IntentSender request here
                if (result.resultCode === RESULT_OK) {
                    displayGallery()
                    if (!isFromRename) {
                        DialogHandler.getInstance().showDeleteBottomSheet(
                            this@MyCreationActivity,
                            "delSuccess",
                            true
                        ) { }
                    }
                } else {
                    Log.e(TAG, "onAdDismissedFullScreenContent: not deleted")
                }
            }
    }

    /**
     * Method to handle picture rename request in android version greater than 10
     */
    private fun handleRenameLauncherIntent() {
        renameLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                Log.e(TAG, "handleLauncherIntent: result code: " + result.resultCode)
                // Handle the result of the IntentSender request here
                if (result.resultCode === RESULT_OK) {
                    if (path.isNotEmpty() && newName.isNotEmpty()) {
                        renameImage(this@MyCreationActivity, path, newName)
                    } else {
                        Log.e(TAG, "onActivityResult: path: $path")
                        Log.e(TAG, "onActivityResult: new name: $newName")
                        Toast.makeText(
                            this@MyCreationActivity,
                            getString(R.string.con_error_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(TAG, "onAdDismissedFullScreenContent: not deleted")
                }
            }
    }

    @SuppressLint("Recycle")
    /*  private fun displayGallery() {
          Log.e("Test", "updateFileList: called")
          val projection = arrayOf(MediaStore.Images.Media.DATA)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              Log.e("Test", "updateFileList: inside if")

              // For Android 10 (API level 29) and above, use MediaStore
              galleryUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
              selectionArgs = arrayOf("image/png", "%" + "/Pictures/Photo Editor/%")
          } else {
              Log.e("Test", "updateFileList: inside else")

              galleryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
              selectionArgs = arrayOf("image/png", "%" + "/DCIM/Photo Editor/%")
          }
          val selection =
              MediaStore.Images.Media.MIME_TYPE + "=? AND " + MediaStore.Images.Media.DATA + " LIKE ?"
          val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
          imgPath1 = java.util.ArrayList()
          imagePathList = java.util.ArrayList()
          try {
              Log.e("Test", "updateFileList: inside try")

              contentResolver.query(galleryUri, projection, selection, selectionArgs, sortOrder)
                  .use { cursor ->
                      if (cursor != null) {
                          while (cursor.moveToNext()) {
                              val columnIndexData =
                                  cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                              val imagePath = cursor.getString(columnIndexData)
                              val updatedImageName: String = getImageNameFromMediaStore(imagePath)
                              val fileModel = FileModel()
                              fileModel.filePath = imagePath
                              fileModel.fileTitle = updatedImageName
                              Log.e(TAG, "displayGallery: file path: " + fileModel.filePath)
                              Log.e(TAG, "displayGallery: file name: " + fileModel.fileTitle)
                              imgPath1.add(fileModel)
                              imagePathList.add(imagePath)
                          }
                          Log.e(TAG, "displayGallery: image path size: " + imgPath1.size)
                          if (imgPath1.size > 0) {

                              binding.rlNoSavedImage.visibility = View.GONE
                              binding.listCreation.visibility = View.VISIBLE
                              val creationAdapter = CreationAdapter(imgPath1)
                              binding.listCreation.adapter = creationAdapter

                          } else {
                              binding.rlNoSavedImage.visibility = View.VISIBLE
                              binding.listCreation.visibility = View.GONE
                          }
                      }
                  }
          } catch (e: Exception) {
              Log.e("test", "updateFileList: exception: ${e.localizedMessage}")
          }
      }*/
    private fun displayGallery() {
        Log.e(TAG, "displayGallery: called")
        imagePathList.clear()
        imageNameList.clear()
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val galleryUri: Uri
        val selectionArgs: Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.e(TAG, "displayGallery: inside version if")
            // For Android 10 (API level 29) and above, use MediaStore
            galleryUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            selectionArgs = arrayOf("image/png", "%" + "/Pictures/Photo Editor/%")
        } else {
            Log.e(TAG, "displayGallery: inside version else")
            galleryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            selectionArgs = arrayOf("image/png", "%" + "/DCIM/Photo Editor/%")
        }
        val selection =
            MediaStore.Images.Media.MIME_TYPE + "=? AND " + MediaStore.Images.Media.DATA + " LIKE ?"
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
        try {
            Log.e(TAG, "displayGallery: inside try")
            contentResolver.query(galleryUri, projection, selection, selectionArgs, sortOrder)
                .use { cursor ->
                    if (cursor != null) {
                        Log.e(TAG, "displayGallery: inside cursor not null: ")
                        Log.e(TAG, "Number of results: ${cursor.count}")
                        while (cursor.moveToNext()) {
                            Log.e(TAG, "displayGallery: inside while ")
                            val columnIndexData =
                                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                            val imagePath = cursor.getString(columnIndexData)
                            val updatedImageName = getImageNameFromMediaStore(imagePath)
                            if (imagePath.isNotEmpty() && updatedImageName.isNotEmpty()) {
                                imagePathList.add(imagePath)
                                imageNameList.add(updatedImageName)
                                Log.e(TAG, "displayGallery: image path: $imagePath")
                            } else {
                                binding.rlNoSavedImage.visibility = View.VISIBLE
                                binding.listCreation.visibility = View.GONE
                                Log.e(TAG, "displayGallery: data is null")
                            }
                        }
                    } else {
                        Log.e(TAG, "displayGallery: cursor is null")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "displayGallery: exception: " + e.localizedMessage)
            e.printStackTrace()
        }
        if (imagePathList.size > 0) {

            binding.rlNoSavedImage.visibility = View.GONE
            binding.listCreation.visibility = View.VISIBLE
            val creationAdapter = CreationAdapter(imagePathList, imageNameList, this)
            binding.listCreation.adapter = creationAdapter

        } else {
            binding.rlNoSavedImage.visibility = View.VISIBLE
            binding.listCreation.visibility = View.GONE
        }
    }

    /**
     * Method to get image name
     *
     * @param imagePath: image path
     * @return: name of image
     */
    private fun getImageNameFromMediaStore(imagePath: String): String {
        var imageName = ""
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        val selection = MediaStore.Images.Media.DATA + "=?"
        val selectionArgs = arrayOf(imagePath)
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            imageName = cursor.getString(columnIndex)
            cursor.close()
        }
        return imageName
    }

    /**
     * Method to delete images
     *
     * @param imagePath: path of image
     */
    private fun deleteImageFromFolder(imagePath: String) {
        val contentResolver = contentResolver
        val imgUri: Uri = Utility.mInstance.getImageContentUri(this@MyCreationActivity, imagePath)
        Log.e(TAG, "deleteImageFromFolder: img uri: $imgUri")
        try {
            //delete object using resolver
            contentResolver.delete(imgUri, null, null)
            displayGallery()
            if (!isFromRename) {
                DialogHandler.getInstance().showDeleteBottomSheet(
                    this@MyCreationActivity,
                    "delSuccess",
                    true
                ) { }
            }
            Log.e(TAG, "delete: inside try")
        } catch (e: SecurityException) {
            Log.e(TAG, "delete: inside catch")
            var pendingIntent: PendingIntent? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val collection = ArrayList<Uri>()
                collection.add(imgUri)
                pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //if exception is recoverable then again send delete request using intent
                if (e is RecoverableSecurityException) {
                    pendingIntent = e.userAction.actionIntent
                }
            } else {
                Log.e(TAG, "deleteImageFromFolder: last case")
                if (imagePath.contains("/DCIM/Cartoon Photo Editor/") || imagePath.contains("/Pictures/")) {
                    val imageFile = File(imagePath)
                    if (imageFile.exists()) {
                        if (imageFile.delete()) {
                            DialogHandler.getInstance().showDeleteBottomSheet(
                                this@MyCreationActivity,
                                "delSuccess",
                                true
                            ) { }
                            val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            scanIntent.data = Uri.fromFile(imageFile)
                            sendBroadcast(scanIntent)
                            MediaScannerConnection.scanFile(
                                this@MyCreationActivity, arrayOf(imageFile.absolutePath),
                                null
                            ) { path: String?, uri: Uri? -> runOnUiThread { this.displayGallery() } }
                        } else {
                            // Failed to delete the image file
                            DialogHandler.getInstance().showBottomSheet(
                                this@MyCreationActivity,
                                getString(R.string.app_name),
                                getString(R.string.con_error_msg),
                                "",
                                "",
                                getString(R.string.ok_label),
                                "connectionErr",
                                true
                            ) { }
                            Log.e(TAG, "Failed to delete the image file: $imagePath")
                        }
                    }
                } else {
                    // Image is not from the "Cartoon Photo Editor" folder
                    DialogHandler.getInstance().showBottomSheet(
                        this@MyCreationActivity,
                        getString(R.string.app_name),
                        getString(R.string.con_error_msg),
                        "",
                        "",
                        getString(R.string.ok_label),
                        "connectionErr",
                        true
                    ) { itemClicked -> }
                    Log.e(
                        TAG,
                        "Image is not from the Cartoon Photo Editor folder: $imagePath"
                    )
                }
            }
            if (pendingIntent != null) {
                val sender = pendingIntent.intentSender
                val request = IntentSenderRequest.Builder(sender).build()
                deleteLauncher?.launch(request)
            }
        }
    }

    /**
     * Method to display rename dialog
     *
     * @param imagePath:   path of image
     * @param showErrMsg:  error message flag
     * @param changedName: updated name of image (if exists)
     */
    private fun showRenameDialog(imagePath: String, showErrMsg: Boolean, changedName: String) {
        DialogHandler.getInstance().showRenameDialog(
            this@MyCreationActivity,
            true,
            imagePath,
            changedName,
            showErrMsg
        ) { status, message, data, alertDialog ->
            if (status != null) {
                when (status) {
                    "0", "2" -> alertDialog.dismiss()
                    "1" -> {
                        showInterstitialAd(this@MyCreationActivity, Constants.AdMob_Main_Interstitial_Ad_Id) {
                            isForcedOverwrite = false
                            renameImage(this, message, data)
                        }
//                        loadInterstitialAd("rename", message, data)
                        alertDialog.dismiss()
                    }
                    "3" -> {
                        showInterstitialAd(this@MyCreationActivity, Constants.AdMob_Main_Interstitial_Ad_Id) {
                            isForcedOverwrite = true
                            renameImage(this, message, data)
                        }

//                        loadInterstitialAd("rename", imagePath, changedName)
                        alertDialog.dismiss()
                    }
                }
            }
        }
    }

    /**
     * Method to rename image
     *
     * @param context:          context
     * @param originalFilePath: original path
     * @param newFileName:      new image name
     */
    private fun renameImage(context: Context, originalFilePath: String, newFileName: String) {
        val imageFile = File(originalFilePath)
        val fileExtension = originalFilePath.substring(originalFilePath.lastIndexOf("."))
        val newFilePath = imageFile.parent + File.separator + newFileName + fileExtension
        val newImageFile = File(newFilePath)
        newName = newFileName
        val originalFileUri: Uri
        var pendingIntent: PendingIntent? = null
        if (newImageFile.exists() && !isForcedOverwrite) {
            showRenameDialog(originalFilePath, true, newName)
        } else {
            if (newImageFile.exists() && isForcedOverwrite) {
                Log.e(TAG, "renameImage: image file: $imageFile")
                for (i in imagePathList.indices) {
                    if (newImageFile.absolutePath.equals(imagePathList[i])) {
                        Log.e(TAG, "renameImage: file found")
                        val fileToDelete: File = File(imagePathList[i])
                        if (fileToDelete.exists()) {
                            isFromRename = true
                            deleteImageFromFolder(imagePathList[i])
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                originalFileUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendPath(getImageId(context, originalFilePath).toString())
                    .build()
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DISPLAY_NAME, newFileName)
                try {
                    context.contentResolver.update(originalFileUri, values, null, null)
                    MediaScannerConnection.scanFile(
                        context, arrayOf(originalFilePath, newFilePath),
                        null
                    ) { path: String, uri: Uri? ->
                        if (path == originalFilePath) {
                            Log.e(TAG, "Old image file scanned: $path")
                        } else {
                            runOnUiThread {
                                displayGallery()
                                DialogHandler.getInstance()
                                    .renameSuccessDialog(this@MyCreationActivity, true)
                            }
                            Log.e(TAG, "New image file scanned: $path")
                        }
                    }
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "delete: inside catch")
                    Log.e(TAG, "renameImage: exception: " + e.localizedMessage)

                    //if exception is recoverable then again send delete request using intent
                    if (e is RecoverableSecurityException) {
                        pendingIntent = e.userAction.actionIntent
                    }
                    if (pendingIntent != null) {
                        path = originalFilePath
                        val sender = pendingIntent.intentSender
                        val request = IntentSenderRequest.Builder(sender).build()
                        renameLauncher?.launch(request)
                    }
                }
            } else {
                // For devices running on Android versions prior to 10, use the old method
                if (imageFile.renameTo(newImageFile)) {
                    if (imageFile.exists() && imageFile.delete()) {
                        Log.e(TAG, "Old image file deleted: $originalFilePath")
                    } else {
                        Log.e(TAG, "Failed to delete old image file: $originalFilePath")
                    }
                    MediaScannerConnection.scanFile(
                        context, arrayOf(originalFilePath, newImageFile.absolutePath), null
                    ) { path: String, uri: Uri? ->
                        if (path == originalFilePath) {
                            Log.e(TAG, "Old image file scanned: $path")
                        } else {
                            runOnUiThread {
                                displayGallery()
                                DialogHandler.getInstance()
                                    .renameSuccessDialog(this@MyCreationActivity, true)
                            }
                            Log.e(TAG, "New image file scanned: $path")
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to update the file name: $originalFilePath")
                    Toast.makeText(context, getString(R.string.con_error_msg), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    /**
     * Method to get image id from media store
     *
     * @param context:   context
     * @param imagePath: path of image
     * @return: id of image
     */
    private fun getImageId(context: Context, imagePath: String): Long {
        var imageId: Long = -1
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = MediaStore.Images.Media.DATA + "=?"
        val selectionArgs = arrayOf(imagePath)
        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    imageId = cursor.getLong(columnIndex)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return imageId
    }

    /**
     * Method to share image with other apps
     *
     * @param imagePath: path of image
     */
    fun shareImage(imagePath: String) {
        Log.e(TAG, "shareImage: path: $imagePath")
        val lastSlashIndex = imagePath.lastIndexOf('/')
        val imageName = imagePath.substring(lastSlashIndex + 1)
        Log.e(TAG, "shareImage: imageName: $imageName")
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        val bitmapResult: Bitmap =
            Utility.getInstance().loadImageFromPath(this@MyCreationActivity, imagePath)
        Log.e(TAG, "shareImage: bitmap: $bitmapResult")
        shareImageUri = Utility.getInstance().shareBitmapToUri(this, bitmapResult, imageName)
        Log.e(TAG, "shareImage: share image Uri: $shareImageUri")
        shareIntent.putExtra(Intent.EXTRA_STREAM, shareImageUri)
        shareIntent.type = "image/*"
        shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startActivity(shareIntent)
        } else {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_header)))
        }
    }


    inner class FileModel {
        lateinit var filePath: String
        lateinit var fileTitle: String
    }

    inner class CreationAdapter(
        imgPath: java.util.ArrayList<String>,
        imgName: java.util.ArrayList<String>,
        activity: Activity
    ) :
        RecyclerView.Adapter<CreationAdapter.CreationHolder>() {

        private var paths = imgPath
        private var names = imgName
        private var activity = activity

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreationHolder {
            val view = LayoutInflater.from(this@MyCreationActivity)
                .inflate(R.layout.item_creation, parent, false)

            isFromSaved = false
            return CreationHolder(view)
        }

        override fun getItemCount(): Int {
            return paths.size
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindViewHolder(holder: CreationHolder, position: Int) {

            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(dm)
            val width = dm.widthPixels

            holder.imgCreation.layoutParams = RelativeLayout.LayoutParams(width / 2, width / 2)

            holder.imgCreation.setImageURI(Uri.parse(paths[holder.adapterPosition]))
            holder.txtTitle.text = names[holder.adapterPosition]


            holder.imgCreation.setOnClickListener {
                checkClick()
                showInterstitialAd(activity, Constants.AdMob_Main_Interstitial_Ad_Id) {
                    val intent = Intent(this@MyCreationActivity, ShowImageActivity::class.java)
                    intent.putExtra("image_uri", paths[holder.adapterPosition])
                    intent.putExtra("from", "saved")
                    startActivity(intent)
                    finish()
                }
            }

            holder.menuIv.setOnClickListener {
                val popupWindow: PopupWindow = PopupWindow(this@MyCreationActivity)
                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

                val view: View = inflater.inflate(R.layout.layout_savedfile_menu, null)

                popupWindow.isFocusable = true
                popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
                popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                popupWindow.contentView = view
                val location = IntArray(2)
                holder.mainLayout.getLocationInWindow(location)

                val deleteLayout = view.findViewById<LinearLayout>(R.id.ll_delete)
                val shareLayout = view.findViewById<LinearLayout>(R.id.ll_share)
                val renameLayout = view.findViewById<LinearLayout>(R.id.ll_rename)

                deleteLayout.setOnClickListener {
                    DialogHandler.getInstance().showDeleteBottomSheet(
                        this@MyCreationActivity,
                        "delImage",
                        true
                    ) { itemClicked ->
                        showInterstitialAd(activity, Constants.AdMob_Main_Interstitial_Ad_Id) {
                            if (itemClicked.equals("1")) {
                                deleteImageFromFolder(paths[holder.adapterPosition])
                            }
                        }

                    }
                    popupWindow.dismiss()
                }

                shareLayout.setOnClickListener {
                    showInterstitialAd(activity, Constants.AdMob_Main_Interstitial_Ad_Id) {
                        shareImage(paths[holder.adapterPosition])
                    }
                    popupWindow.dismiss()
                }

                renameLayout.setOnClickListener {
                    showRenameDialog(paths[holder.adapterPosition], false, "")
                    popupWindow.dismiss()
                }

                popupWindow.showAtLocation(
                    holder.mainLayout,
                    Gravity.NO_GRAVITY,
                    location[0] + 20,
                    location[1] + holder.menuLayout.height - 150
                )
            }

        }

        inner class CreationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imgCreation: ImageView = itemView.findViewById(R.id.img_creation)
            var menuIv: ImageView = itemView.findViewById(R.id.iv_menu)
            var txtTitle: TextView = itemView.findViewById(R.id.txt_title)
            var menuLayout: RelativeLayout = itemView.findViewById(R.id.rl_menu)
            var mainLayout: RelativeLayout = itemView.findViewById(R.id.rl_image)
        }
    }

    override fun onResume() {
        super.onResume()
        if (imagePathList.size > 0) {

            binding.rlNoSavedImage.visibility = View.GONE
            binding.listCreation.visibility = View.VISIBLE
            val creationAdapter = CreationAdapter(imagePathList, imageNameList, this)
            binding.listCreation.adapter = creationAdapter

        } else {
            binding.rlNoSavedImage.visibility = View.VISIBLE
            binding.listCreation.visibility = View.GONE
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(this@MyCreationActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onAdClicked() {
        TODO("Not yet implemented")
    }

    override fun onAdLoaded() {
        TODO("Not yet implemented")
        Log.e(TAG, "onAdLoaded: ")
        binding.tvLoadingAd.visibility = View.GONE
    }

    override fun onAdFailedToLoad(loadAdError: LoadAdError?) {
        TODO("Not yet implemented")
    }
}
