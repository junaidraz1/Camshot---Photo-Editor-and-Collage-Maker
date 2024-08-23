package com.miczon.photoeditor.activity

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.miczon.photoeditor.BaseActivity
import com.miczon.photoeditor.R
import com.miczon.photoeditor.adapter.BackgroundAdapter
import com.miczon.photoeditor.adapter.FrameAdapter
import com.miczon.photoeditor.databinding.ActivityCollageBinding
import com.miczon.photoeditor.frame.FramePhotoLayout
import com.miczon.photoeditor.model.TemplateItem
import com.miczon.photoeditor.multitouch.PhotoView
import com.miczon.photoeditor.utils.AndroidUtils
import com.miczon.photoeditor.utils.FrameImageUtils
import com.miczon.photoeditor.utils.ImageUtils
import java.io.*

@Suppress("DEPRECATION")
class CollageActivity : BaseActivity(), View.OnClickListener,
    FrameAdapter.OnFrameClickListener, BackgroundAdapter.OnBGClickListener {

    var TAG = "CollageActivity"

    var mFramePhotoLayout: FramePhotoLayout? = null
    var defaultSpace: Float = 0.0f
    var maxSpace: Float = 0.0f
    var maxCorner: Float = 0.0f

    protected val ratioSquare = 0
    protected val ratioGolden = 2

    private var mSpace = defaultSpace
    private var mCorner = 0f
    val maxSpaceProgress = 300.0f
    val maxCornerProgress = 200.0f
    private var mBackgroundColor = Color.WHITE
    private var mBackgroundImage: Bitmap? = null
    private var mBackgroundUri: Uri? = null
    private var mSavedInstanceState: Bundle? = null
    protected var mLayoutRatio = ratioSquare
    protected lateinit var mPhotoView: PhotoView
    protected var mOutputScale = 1f
    protected var mSelectedTemplateItem: TemplateItem? = null
    private var mImageInTemplateCount = 0
    protected var mTemplateItemList: ArrayList<TemplateItem>? = ArrayList()
    protected var mSelectedPhotoPaths: MutableList<String> = java.util.ArrayList()

    lateinit var frameAdapter: FrameAdapter
    lateinit var imageBackground: ImageView
    lateinit var contract: ActivityResultLauncher<String>

    private lateinit var binding: ActivityCollageBinding

    private var mLastClickTime: Long = 0
    private fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onBGClick(drawable: Drawable) {
        var bmp = mFramePhotoLayout!!.createImage()
        var bitmap = (drawable as BitmapDrawable).bitmap
        mBackgroundImage = AndroidUtils.resizeImageToNewSize(bitmap, bmp.width, bmp.height)

        imageBackground.setImageBitmap(mBackgroundImage)
    }

    override fun onFrameClick(templateItem: TemplateItem) {

        mSelectedTemplateItem!!.isSelected = false

        for (idx in 0 until mSelectedTemplateItem!!.photoItemList.size) {
            val photoItem = mSelectedTemplateItem!!.photoItemList[idx]
            if (photoItem.imagePath != null && photoItem.imagePath!!.isNotEmpty()) {
                if (idx < mSelectedPhotoPaths.size) {
                    mSelectedPhotoPaths.add(idx, photoItem.imagePath!!)
                } else {
                    mSelectedPhotoPaths.add(photoItem.imagePath!!)
                }
            }
        }

        val size = mSelectedPhotoPaths.size.coerceAtMost(templateItem.photoItemList.size)
        for (idx in 0 until size) {
            val photoItem = templateItem.photoItemList[idx]
            if (photoItem.imagePath == null || photoItem.imagePath!!.isEmpty()) {
                photoItem.imagePath = mSelectedPhotoPaths[idx]
            }
        }

        mSelectedTemplateItem = templateItem
        mSelectedTemplateItem!!.isSelected = true
        frameAdapter.notifyDataSetChanged()
        buildLayout(templateItem)
    }

    inner class SpaceListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            mSpace = maxSpace * seekBar!!.progress / maxSpaceProgress
            if (mFramePhotoLayout != null)
                mFramePhotoLayout!!.setSpace(mSpace, mCorner)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    inner class CornerListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            mCorner = maxCorner * seekBar!!.progress / maxCornerProgress
            if (mFramePhotoLayout != null)
                mFramePhotoLayout!!.setSpace(mSpace, mCorner)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {

            R.id.tab_layout -> {
                binding.tabLayout.setBackgroundResource(R.drawable.bg_lightpurple_selected_bg)
                binding.tabBorder.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
                binding.tabBg.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
                binding.tvLayout.setTextColor(resources.getColor(R.color.new_purple))
                binding.tvBorder.setTextColor(resources.getColor(R.color.white))
                binding.tvBg.setTextColor(resources.getColor(R.color.white))

                binding.llFrame.visibility = View.VISIBLE
                binding.llBorder.visibility = View.GONE
                binding.llBg.visibility = View.GONE
            }

            R.id.tab_border -> {
                binding.tabLayout.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
                binding.tabBorder.setBackgroundResource(R.drawable.bg_lightpurple_selected_bg)
                binding.tabBg.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
                binding.tvLayout.setTextColor(resources.getColor(R.color.white))
                binding.tvBorder.setTextColor(resources.getColor(R.color.new_purple))
                binding.tvBg.setTextColor(resources.getColor(R.color.white))

                binding.llFrame.visibility = View.GONE
                binding.llBorder.visibility = View.VISIBLE
                binding.llBg.visibility = View.GONE
            }

            R.id.tab_bg -> {
                binding.tabLayout.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
                binding.tabBorder.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
                binding.tabBg.setBackgroundResource(R.drawable.bg_lightpurple_selected_bg)
                binding.tvLayout.setTextColor(resources.getColor(R.color.white))
                binding.tvBorder.setTextColor(resources.getColor(R.color.white))
                binding.tvBg.setTextColor(resources.getColor(R.color.new_purple))

                binding.llFrame.visibility = View.GONE
                binding.llBorder.visibility = View.GONE
                binding.llBg.visibility = View.VISIBLE

            }

            R.id.btn_next -> {

                checkClick()

                var outStream: FileOutputStream? = null
                try {
                    val collageBitmap = createOutputImage()
                    outStream = FileOutputStream(File(cacheDir, "tempBMP"))
                    collageBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream)
                    outStream.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val intent = Intent(this, FilterCollageActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.rl_back -> {

                checkClick()

                val intent = Intent(this, SelectImageActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCollageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        defaultSpace = ImageUtils.pxFromDp(this, 2F)
        maxSpace = ImageUtils.pxFromDp(this, 30F)
        maxCorner = ImageUtils.pxFromDp(this, 60F)
        mSpace = defaultSpace

        if (savedInstanceState != null) {
            mSpace = savedInstanceState.getFloat("mSpace")
            mCorner = savedInstanceState.getFloat("mCorner")
            mSavedInstanceState = savedInstanceState
        }

        binding.tabLayout.setBackgroundResource(R.drawable.bg_lightpurple_selected_bg)
        binding.tabBorder.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
        binding.tabBg.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)

        binding.tvLayout.setTextColor(resources.getColor(R.color.new_purple))
        binding.tvBorder.setTextColor(resources.getColor(R.color.white))
        binding.tvBg.setTextColor(resources.getColor(R.color.white))


        binding.rlRotate.setBackgroundResource(R.drawable.bg_less_rounded_dialog_btn)
        binding.rlResize.setBackgroundResource(R.drawable.bg_dark_grey_less_rounded)



        contract = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                val image = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                    MediaStore.Images.Media.getBitmap(contentResolver, it)
                else {
                    val source = ImageDecoder.createSource(contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                val imgPath = getRealPathFromURI(it)
                mFramePhotoLayout?.getFrameImageView { frameImageView ->
                    frameImageView?.replaceImage(image, imgPath, frameImageView)
                }
            }
        }

        mImageInTemplateCount = intent.getIntExtra("imagesinTemplate", 0)
        val extraImagePaths = intent.getStringArrayListExtra("selectedImages")

        Log.e(TAG, "onCreate: template val: $mImageInTemplateCount")

        if (extraImagePaths != null) {
            Log.e(TAG, "onCreate: template val: " + extraImagePaths.size)
        } else {
            Log.e(TAG, "onCreate: extra image path is null")
        }

        binding.listBg.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listBg.adapter = BackgroundAdapter(this, this)

        binding.tabLayout.setOnClickListener(this)
        binding.tabBorder.setOnClickListener(this)
        binding.tabBg.setOnClickListener(this)
        binding.rlBack.setOnClickListener(this)

        binding.seekbarSpace.setOnSeekBarChangeListener(SpaceListener())
        binding.seekbarCorner.setOnSeekBarChangeListener(CornerListener())

        mPhotoView = PhotoView(this)
        binding.rlContainer.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mOutputScale = ImageUtils.calculateOutputScaleFactor(
                        binding.rlContainer.width,
                        binding.rlContainer.height
                    )
                    buildLayout(mSelectedTemplateItem!!)
                    // remove listener
                    binding.rlContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

        imageBackground = findViewById(R.id.img_background)

        loadFrameImages()

        binding.listFrames.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        frameAdapter = FrameAdapter(this, mTemplateItemList!!, this)
        binding.listFrames.adapter = frameAdapter

        mSelectedTemplateItem = mTemplateItemList!![0]
        mSelectedTemplateItem!!.isSelected = true

        if (extraImagePaths != null) {
            val size =
                extraImagePaths.size.coerceAtMost(mSelectedTemplateItem!!.photoItemList.size)
            for (i in 0 until size)
                mSelectedTemplateItem!!.photoItemList[i].imagePath = extraImagePaths[i]
        }

        binding.btnNext.setOnClickListener(this)

    }

    private fun loadFrameImages() {
        val mAllTemplateItemList = java.util.ArrayList<TemplateItem>()

        mAllTemplateItemList.addAll(FrameImageUtils.loadFrameImages(this))

        mTemplateItemList = java.util.ArrayList<TemplateItem>()
        if (mImageInTemplateCount > 0) {
            for (item in mAllTemplateItemList)
                if (item.photoItemList.size == mImageInTemplateCount) {
                    mTemplateItemList!!.add(item)
                }
        } else {
            mTemplateItemList!!.addAll(mAllTemplateItemList)
        }
    }

    fun getActivityResultLauncher(resultActivityLauncher: (ActivityResultLauncher<String>) -> Unit) {
        resultActivityLauncher.invoke(contract)
    }

    fun getBinding(callback: (ActivityCollageBinding) -> Unit) {
        callback.invoke(binding)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat("mSpace", mSpace)
        outState.putFloat("mCornerBar", mCorner)
        if (mFramePhotoLayout != null) {
            mFramePhotoLayout!!.saveInstanceState(outState)
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val returnCursor = contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(filesDir, name)
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream?.available() ?: 0
            //int bufferSize = 1024;
            val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream?.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)
        } catch (e: Exception) {
            Log.e("Exception", e.message!!)
        }
        returnCursor.close()
        return file.path
    }

    fun buildLayout(item: TemplateItem) {
        mFramePhotoLayout = FramePhotoLayout(this, item.photoItemList)

//        if (mBackgroundImage != null && !mBackgroundImage!!.isRecycled()) {
//            if (Build.VERSION.SDK_INT >= 16)
//                binding.rlContainer.setBackground(BitmapDrawable(resources, mBackgroundImage))
//            else
//                binding.rlContainer.setBackgroundDrawable(BitmapDrawable(resources, mBackgroundImage))
//        } else {
//            binding.rlContainer.setBackgroundColor(mBackgroundColor)
//        }

        var viewWidth = binding.rlContainer.getWidth()
        var viewHeight = binding.rlContainer.getHeight()

        if (mLayoutRatio === ratioSquare) {
            if (viewWidth > viewHeight) {
                viewWidth = viewHeight
            } else {
                viewHeight = viewWidth
            }
        } else if (mLayoutRatio === ratioGolden) {
            val goldenRatio = 1.61803398875
            if (viewWidth <= viewHeight) {
                if (viewWidth * goldenRatio >= viewHeight) {
                    viewWidth = (viewHeight / goldenRatio).toInt()
                } else {
                    viewHeight = (viewWidth * goldenRatio).toInt()
                }
            } else if (viewHeight <= viewWidth) {
                if (viewHeight * goldenRatio >= viewWidth) {
                    viewHeight = (viewWidth / goldenRatio).toInt()
                } else {
                    viewWidth = (viewHeight * goldenRatio).toInt()
                }
            }
        }

        mOutputScale = ImageUtils.calculateOutputScaleFactor(viewWidth, viewHeight)
        mFramePhotoLayout!!.build(viewWidth, viewHeight, mOutputScale, mSpace, mCorner)
        if (mSavedInstanceState != null) {
            mFramePhotoLayout!!.restoreInstanceState(mSavedInstanceState!!)
            mSavedInstanceState = null
        }
        val params = RelativeLayout.LayoutParams(viewWidth, viewHeight)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        binding.rlContainer.removeAllViews()

        binding.rlContainer.removeView(imageBackground)
        binding.rlContainer.addView(imageBackground, params)

        binding.rlContainer.addView(mFramePhotoLayout, params)
        //add sticker view
        binding.rlContainer.removeView(mPhotoView)
        binding.rlContainer.addView(mPhotoView, params)
        //reset space and corner seek bars

        binding.seekbarSpace.setProgress((maxSpaceProgress * mSpace / maxSpace).toInt())
        binding.seekbarCorner.setProgress((maxCornerProgress * mCorner / maxCorner).toInt())
    }

    @Throws(OutOfMemoryError::class)
    fun createOutputImage(): Bitmap {
        try {
            var template = mFramePhotoLayout!!.createImage()
            val result =
                Bitmap.createBitmap(template!!.width, template.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            if (mBackgroundImage != null && !mBackgroundImage!!.isRecycled()) {
                canvas.drawBitmap(
                    mBackgroundImage!!,
                    Rect(0, 0, mBackgroundImage!!.getWidth(), mBackgroundImage!!.getHeight()),
                    Rect(0, 0, result.width, result.height),
                    paint
                )
            } else {
                canvas.drawColor(mBackgroundColor)
            }

            canvas.drawBitmap(template, 0f, 0f, paint)
            template.recycle()
            var stickers = mPhotoView.getImage(mOutputScale)
            canvas.drawBitmap(stickers!!, 0f, 0f, paint)
            stickers.recycle()
            stickers = null
            System.gc()
            return result
        } catch (error: OutOfMemoryError) {
            throw error
        }
    }
}
