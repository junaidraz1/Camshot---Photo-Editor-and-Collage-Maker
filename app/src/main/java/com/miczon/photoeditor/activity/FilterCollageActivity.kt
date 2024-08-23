package com.miczon.photoeditor.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.miczon.photoeditor.BaseActivity
import com.miczon.photoeditor.R
import com.miczon.photoeditor.activity.MainActivity.Companion.isFromSaved
import com.miczon.photoeditor.adapter.FilterNameAdapter
import com.miczon.photoeditor.databinding.ActivityFilterCollageBinding
import com.miczon.photoeditor.helper.DialogHandler
import com.miczon.photoeditor.model.FilterData
import com.miczon.photoeditor.utils.AndroidUtils
import java.io.File
import java.io.FileOutputStream
import java.util.*

@Suppress("DEPRECATION")
class FilterCollageActivity : BaseActivity(), View.OnClickListener {
    private var mLastClickTime: Long = 0
    private lateinit var binding: ActivityFilterCollageBinding

    private fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_save -> {
                checkClick()
                isFromSaved = true
                try {
                    savedImageUri = Uri.parse(saveBitmap(screenShot))
                } catch (th: Throwable) {
                    th.printStackTrace()
                }
                if (savedImageUri != null) {
                    DialogHandler.getInstance().showSaveDialog(
                        this@FilterCollageActivity,
                        true, ""
                    ) { status, _, _, alertDialog ->
                        if (status.equals("0", ignoreCase = true)) {
                            alertDialog.dismiss()
                            startActivity(
                                Intent(
                                    this@FilterCollageActivity,
                                    HomeActivity::class.java
                                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                        } else if (status.equals("1", ignoreCase = true)) {
                            alertDialog.dismiss()
                            val intent = Intent(this, ShowImageActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            intent.putExtra("image_uri", savedImageUri!!.toString())
                            intent.putExtra("from", TAG)
                            startActivityForResult(intent, 2)
                            finish()
                        }
                    }

                }
            }
        }
    }

    private val screenShot: Bitmap
        get() {
            val findViewById = findViewById<View>(R.id.img_collage)
            findViewById.background = null
            findViewById.destroyDrawingCache()
            findViewById.isDrawingCacheEnabled = true
            val createBitmap = Bitmap.createBitmap(findViewById.drawingCache)
            findViewById.isDrawingCacheEnabled = false
            val createBitmap2 = Bitmap.createBitmap(
                createBitmap.width,
                createBitmap.height,
                Bitmap.Config.ARGB_8888
            )
            findViewById.draw(Canvas(createBitmap2))
            return createBitmap2
        }

    private var savedImageUri: Uri? = null

    private fun saveBitmap(imageBitmap: Bitmap): String {
        var path = ""
        val galleryFolderName = "Photo Editor"
        val imageFileName = "Image_" + System.currentTimeMillis() + ".png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API level 29) and higher, use MediaStore
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
            values.put(MediaStore.Images.Media.DESCRIPTION, "Saved from Photo Editor app")
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + galleryFolderName
            )
            val contentResolver = this.contentResolver
            val imageUri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (imageUri != null) {
                try {
                    val outputStream = contentResolver.openOutputStream(imageUri)
                    if (outputStream != null) {
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        outputStream.close()
                        //                        Toast.makeText(context, context.getString(R.string.saved_gall), Toast.LENGTH_SHORT).show();
                        path = imageUri.toString()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // For Android 9 (API level 28) and below, use the old approach
            val externalStorageDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val galleryDir = File(externalStorageDir, galleryFolderName)
            galleryDir.mkdirs()
            val imageFile = File(galleryDir, imageFileName)
            Log.e("path", "saveImageToGallery: path is: $imageFile")
            try {
                val outputStream = FileOutputStream(imageFile)
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                //                Toast.makeText(context, context.getString(R.string.saved_gall), Toast.LENGTH_SHORT).show();
                // Skip the media scan for the image file
                MediaScannerConnection.scanFile(this, arrayOf(imageFile.toString()), null, null)
                path = imageFile.toString()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return path
    }

    companion object {
        var red: Float = 0F
        var green: Float = 0F
        var blue: Float = 0F
        var saturation: Float = 0F
    }

    lateinit var bmp: Bitmap
    var TAG = "FilterCollageActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFilterCollageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bitmapPath = this.cacheDir.absolutePath + "/tempBMP"
        bmp = BitmapFactory.decodeFile(bitmapPath)

        binding.imgCollage.setImageBitmap(bmp)
        binding.thumbnailFilter.setImageBitmap(bmp)

        binding.imgSave.setOnClickListener(this)

        binding.listFilterstype.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        var filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_clr1)
        binding.listFilterstype.adapter = filterTypeAdapter

        binding.thumbnailFilter.setOnClickListener {

            binding.rlFilteritem.setBackgroundResource(R.drawable.selected_image_bg)
            binding.imgCollage.setImageBitmap(bmp)

            filterTypeAdapter.clearSelection()
        }

        binding.filterNames.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        var filterNameAdapter = FilterNameAdapter(this, resources.getStringArray(R.array.filters))

        filterNameAdapter.setOnFilterNameClick(object : FilterNameAdapter.FilterNameClickListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemClick(view: View, position: Int) {

                if (position == 0) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_clr1)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 1) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_clr2)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 2) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_duo)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 3) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_pink)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 4) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_fresh)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 5) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_euro)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 6) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_dark)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 7) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_ins)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 8) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_elegant)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 9) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_golden)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 10) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_tint)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 11) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_film)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 12) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_lomo)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 13) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_movie)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 14) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_retro)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else if (position == 15) {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_bw)
                    binding.listFilterstype.adapter = filterTypeAdapter
                } else {
                    filterTypeAdapter = FilterDetailAdapter(AndroidUtils.filter_clr1)
                    binding.listFilterstype.adapter = filterTypeAdapter
                }
                filterNameAdapter.notifyDataSetChanged()
                filterTypeAdapter.notifyDataSetChanged()
            }
        })

        binding.filterNames.adapter = filterNameAdapter

    }

    inner class FilterDetailAdapter(filters: Array<FilterData>) :
        RecyclerView.Adapter<FilterDetailAdapter.FilterDetailHolder>() {
        var filterType = filters
        var selectedindex = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterDetailHolder {
            var view = LayoutInflater.from(this@FilterCollageActivity)
                .inflate(R.layout.item_filter, parent, false)
            return FilterDetailHolder(view)
        }

        override fun getItemCount(): Int {
            return filterType.size
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindViewHolder(holder: FilterDetailHolder, position: Int) {

            if (selectedindex == holder.adapterPosition) {
                holder.rlFilterItem.setBackgroundResource(R.drawable.selected_image_bg)
            } else {
                holder.rlFilterItem.setBackgroundColor(resources.getColor(R.color.transparent))
            }

            holder.thumbnailFilter.setImageResource(R.drawable.thumb_filter)

            red = filterType[holder.adapterPosition].red
            green = filterType[holder.adapterPosition].green
            blue = filterType[holder.adapterPosition].blue
            saturation = filterType[holder.adapterPosition].saturation

            var bitmap = Bitmap.createBitmap(
                bmp.width,
                bmp.height,
                Bitmap.Config.ARGB_8888
            )
            var canvas = Canvas(bitmap)

            var paint = Paint()
            var colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(saturation)

            var colorScale = ColorMatrix()
            colorScale.setScale(
                red,
                green,
                blue, 1F
            )
            colorMatrix.postConcat(colorScale)

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(bmp, 0F, 0F, paint)

            holder.thumbnailFilter.setImageBitmap(bitmap)

            holder.filterName.text = filterType[holder.adapterPosition].text

            if (selectedindex == position) {
                holder.rlFilterItem.setBackgroundResource(R.drawable.selected_image_bg)

                ImageEditActivity.red = filterType[holder.absoluteAdapterPosition].red
                ImageEditActivity.green = filterType[holder.absoluteAdapterPosition].green
                ImageEditActivity.blue = filterType[holder.absoluteAdapterPosition].blue
                ImageEditActivity.saturation = filterType[holder.absoluteAdapterPosition].saturation

                ImageEditActivity.Async_Filter(
                    bmp,
                    binding.imgCollage
                ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    ImageEditActivity.red,
                    ImageEditActivity.green,
                    ImageEditActivity.blue
                )

            } else {
                holder.rlFilterItem.setBackgroundColor(resources.getColor(R.color.transparent))
            }

            holder.rlFilterItem.setOnClickListener {
                binding.rlFilteritem.setBackgroundResource(R.color.transparent)

                selectedindex = holder.adapterPosition

                red = filterType[holder.adapterPosition].red
                green = filterType[holder.adapterPosition].green
                blue = filterType[holder.adapterPosition].blue
                saturation = filterType[holder.adapterPosition].saturation

                AsyncFilter(
                    bmp,
                    binding.imgCollage
                ).executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    red,
                    green,
                    blue
                )
                notifyDataSetChanged()
            }
        }

        fun clearSelection() {
            val previousSelectedItem: Int = selectedindex
            selectedindex = -1
            notifyItemChanged(previousSelectedItem)
        }

        inner class FilterDetailHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var thumbnailFilter: ImageView
            var filterName: TextView
            var rlFilterItem: RelativeLayout

            init {
                thumbnailFilter = itemView.findViewById(R.id.thumbnail_filter)
                filterName = itemView.findViewById(R.id.filterName)
                rlFilterItem = itemView.findViewById(R.id.rl_filteritem)
            }
        }
    }

    class AsyncFilter() : AsyncTask<Float, Void, Bitmap>() {

        lateinit var originalBitmap: Bitmap
        lateinit var imgMain: ImageView

        constructor(originalBitmap: Bitmap, imgMain: ImageView) : this() {
            this.originalBitmap = originalBitmap
            this.imgMain = imgMain
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Float?): Bitmap {
            var r = params[0]
            var g = params[1]
            var b = params[2]

            var bitmap = Bitmap.createBitmap(
                this.originalBitmap.width,
                this.originalBitmap.height,
                Bitmap.Config.ARGB_8888
            )
            var canvas = Canvas(bitmap)

            var paint = Paint()
            var colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(saturation)

            var colorScale = ColorMatrix()
            colorScale.setScale(r!!, g!!, b!!, 1F)
            colorMatrix.postConcat(colorScale)

            paint.setColorFilter(ColorMatrixColorFilter(colorMatrix))
            canvas.drawBitmap(this.originalBitmap, 0F, 0F, paint)

            return bitmap
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)

            this.imgMain.setImageBitmap(result)

        }
    }

}
