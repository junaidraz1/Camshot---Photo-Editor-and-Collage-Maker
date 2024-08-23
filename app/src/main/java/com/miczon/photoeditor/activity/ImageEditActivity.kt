@file:Suppress(
    "OverrideDeprecatedMigration", "OverrideDeprecatedMigration",
    "OverrideDeprecatedMigration", "OverrideDeprecatedMigration"
)

package com.miczon.photoeditor.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isseiaoki.simplecropview.CropImageView
import com.miczon.photoeditor.BaseActivity
import com.miczon.photoeditor.R
import com.miczon.photoeditor.adapter.*
import com.miczon.photoeditor.databinding.ActivityImageEditBinding
import com.miczon.photoeditor.helper.DialogHandler
import com.miczon.photoeditor.model.EffectData
import com.miczon.photoeditor.model.FilterData
import com.miczon.photoeditor.stickerView.StickerImageView
import com.miczon.photoeditor.stickerView.StickerTextView
import com.miczon.photoeditor.stickerView.StickerView
import com.miczon.photoeditor.utils.AndroidUtils
import com.miczon.photoeditor.utils.AndroidUtils.filter_bw
import com.miczon.photoeditor.utils.AndroidUtils.filter_clr1
import com.miczon.photoeditor.utils.AndroidUtils.filter_clr2
import com.miczon.photoeditor.utils.AndroidUtils.filter_dark
import com.miczon.photoeditor.utils.AndroidUtils.filter_duo
import com.miczon.photoeditor.utils.AndroidUtils.filter_elegant
import com.miczon.photoeditor.utils.AndroidUtils.filter_euro
import com.miczon.photoeditor.utils.AndroidUtils.filter_film
import com.miczon.photoeditor.utils.AndroidUtils.filter_fresh
import com.miczon.photoeditor.utils.AndroidUtils.filter_golden
import com.miczon.photoeditor.utils.AndroidUtils.filter_ins
import com.miczon.photoeditor.utils.AndroidUtils.filter_lomo
import com.miczon.photoeditor.utils.AndroidUtils.filter_movie
import com.miczon.photoeditor.utils.AndroidUtils.filter_pink
import com.miczon.photoeditor.utils.AndroidUtils.filter_retro
import com.miczon.photoeditor.utils.AndroidUtils.filter_tint
import com.miczon.photoeditor.utils.GPUImageFilterTools
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import java.io.*
import java.util.*

@Suppress(
    "DEPRECATION", "OverrideDeprecatedMigration", "OverrideDeprecatedMigration",
    "OverrideDeprecatedMigration", "OverrideDeprecatedMigration", "OverrideDeprecatedMigration"
)
class ImageEditActivity : BaseActivity(), View.OnClickListener,
    ResizeAdapter.OnResizeClickListener {

    val TAG = "ImageEditActivity"

    private lateinit var binding: ActivityImageEditBinding
    var isGone = false

    companion object {
        var selectedPosition: Int = 0
        var adjust_position: Int = 0
        var red: Float = 0F
        var green: Float = 0F
        var blue: Float = 0F
        var saturation: Float = 0F
        lateinit var blend_bitmap: Bitmap
        lateinit var alert: AlertDialog
    }

    var pickImage: Int = 111

    var light1Array: Array<EffectData> = arrayOf(
        EffectData("Light1_1", R.drawable.light1_1),
        EffectData("Light1_2", R.drawable.light1_2),
        EffectData("Light1_3", R.drawable.light1_3),
        EffectData("Light1_4", R.drawable.light1_4),
        EffectData("Light1_5", R.drawable.light1_5),
        EffectData("Light1_6", R.drawable.light1_6),
        EffectData("Light1_7", R.drawable.light1_7),
        EffectData("Light1_8", R.drawable.light1_8),
        EffectData("Light1_8", R.drawable.light1_9)
    )

    var light2Array: Array<EffectData> = arrayOf(
        EffectData("Light2_1", R.drawable.light2_1),
        EffectData("Light2_2", R.drawable.light2_2),
        EffectData("Light2_3", R.drawable.light2_3),
        EffectData("Light2_4", R.drawable.light2_4),
        EffectData("Light2_4", R.drawable.light2_5),
        EffectData("Light2_4", R.drawable.light2_6),
        EffectData("Light2_4", R.drawable.light2_7),
        EffectData("Light2_5", R.drawable.light2_8)
    )

    var festivalArray: Array<EffectData> = arrayOf(
        EffectData("festival_1", R.drawable.festival_1),
        EffectData("festival_2", R.drawable.festival_2),
        EffectData("festival_3", R.drawable.festival_3),
        EffectData("festival_4", R.drawable.festival_4),
        EffectData("festival_5", R.drawable.festival_5),
        EffectData("festival_6", R.drawable.festival_6)
    )

    var loveArray: Array<EffectData> = arrayOf(
        EffectData("love_1", R.drawable.love_1),
        EffectData("love_2", R.drawable.love_2),
        EffectData("love_3", R.drawable.love_3),
        EffectData("love_4", R.drawable.love_4),
        EffectData("love_5", R.drawable.love_5)
    )

    var prismArray: Array<EffectData> = arrayOf(
        EffectData("prism_1", R.drawable.prism_1),
        EffectData("prism_2", R.drawable.prism_2),
        EffectData("prism_3", R.drawable.prism_3),
        EffectData("prism_4", R.drawable.prism_4),
        EffectData("prism_5", R.drawable.prism_5)
    )

    var neonArray: Array<EffectData> = arrayOf(
        EffectData("neon_1", R.drawable.neon_1),
        EffectData("neon_2", R.drawable.neon_2),
        EffectData("neon_3", R.drawable.neon_3),
        EffectData("neon_4", R.drawable.neon_4),
        EffectData("neon_5", R.drawable.neon_5)
    )

    var dustArray: Array<EffectData> = arrayOf(
        EffectData("Dust_1", R.drawable.dust_1),
        EffectData("Dust_2", R.drawable.dust_2),
        EffectData("Dust_3", R.drawable.dust_3),
        EffectData("Dust_4", R.drawable.dust_4),
        EffectData("Dust_5", R.drawable.dust_5)
    )
    var scratchArray: Array<EffectData> = arrayOf(
        EffectData("scratch_1", R.drawable.scratch_1),
        EffectData("scratch_2", R.drawable.scratch_2),
        EffectData("scratch_3", R.drawable.scratch_3),
        EffectData("scratch_4", R.drawable.scratch_4),
        EffectData("scratch_5", R.drawable.scratch_5)
    )

    var stainArray: Array<EffectData> = arrayOf(
        EffectData("stain_1", R.drawable.stain_1),
        EffectData("stain_2", R.drawable.stain_2),
        EffectData("stain_3", R.drawable.stain_3),
        EffectData("stain_4", R.drawable.stain_4),
        EffectData("stain_5", R.drawable.stain_5)
    )

    var vintageArray: Array<EffectData> = arrayOf(
        EffectData("vintage_1", R.drawable.vintage_1),
        EffectData("vintage_2", R.drawable.vintage_2),
        EffectData("vintage_3", R.drawable.vintage_3),
        EffectData("vintage_4", R.drawable.vintage_4),
        EffectData("vintage_5", R.drawable.vintage_5)
    )

    var cloudArray: Array<EffectData> = arrayOf(
        EffectData("cloud_1", R.drawable.cloud_1),
        EffectData("cloud_2", R.drawable.cloud_2),
        EffectData("cloud_3", R.drawable.cloud_3),
        EffectData("cloud_4", R.drawable.cloud_4),
        EffectData("cloud_5", R.drawable.cloud_5)
    )

    var fogArray: Array<EffectData> = arrayOf(
        EffectData("fog_1", R.drawable.fog_1),
        EffectData("fog_2", R.drawable.fog_2),
        EffectData("fog_3", R.drawable.fog_3),
        EffectData("fog_4", R.drawable.fog_4),
        EffectData("fog_5", R.drawable.fog_5)
    )

    var snowArray: Array<EffectData> = arrayOf(
        EffectData("snow_1", R.drawable.snow_1),
        EffectData("snow_2", R.drawable.snow_2),
        EffectData("snow_3", R.drawable.snow_3),
        EffectData("snow_4", R.drawable.snow_4),
        EffectData("snow_5", R.drawable.snow_5)
    )

    var sunlightArray: Array<EffectData> = arrayOf(
        EffectData("sunlight_1", R.drawable.sunlight_1),
        EffectData("sunlight_2", R.drawable.sunlight_2),
        EffectData("sunlight_3", R.drawable.sunlight_3),
        EffectData("sunlight_4", R.drawable.sunlight_4),
        EffectData("sunlight_5", R.drawable.sunlight_5)
    )

    private var mLastClickTime: Long = 0
    private fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onResizeClick(position: Int) {
        when (position) {
            0 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.FIT_IMAGE)
            }

            1 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.FREE)
            }

            2 -> {

                binding.cropImageView.setCustomRatio(1, 1)
            }

            3 -> {
                binding.cropImageView.setCustomRatio(1, 2)
            }

            4 -> {
                binding.cropImageView.setCustomRatio(2, 1)
            }

            5 -> {
                binding.cropImageView.setCustomRatio(2, 3)
            }

            6 -> {
                binding.cropImageView.setCustomRatio(3, 2)
            }

            7 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_3_4)
            }

            8 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_4_3)
            }

            9 -> {
                binding.cropImageView.setCustomRatio(4, 5)
            }

            10 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_9_16)
            }

            11 -> {
                binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_16_9)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.crop_cancel -> {
                binding.llCrop.visibility = View.GONE
                isGone = true
            }

            R.id.crop_confirm -> {
                originalBitmap1 = binding.cropImageView.croppedBitmap
                binding.imgMain.setImageBitmap(originalBitmap1)
                binding.llCrop.visibility = View.GONE
            }

            R.id.filter_cancel -> {
                binding.llFilter.visibility = View.GONE
                binding.imgMain.setImageBitmap(originalBitmap1)
            }

            R.id.filter_confirm -> {
                val bitmap = (binding.imgMain.drawable as BitmapDrawable).bitmap
                originalBitmap1 = bitmap

                binding.imgMain.setImageBitmap(originalBitmap1)
                binding.llFilter.visibility = View.GONE
            }

            R.id.done_filter, R.id.done_adjust -> {
                val bitmap = (binding.imgMain.drawable as BitmapDrawable).bitmap
                originalBitmap1 = bitmap

                binding.imgMain.setImageBitmap(originalBitmap1)
                Toast.makeText(
                    this@ImageEditActivity,
                    getString(R.string.changes_appl_lbl),
                    Toast.LENGTH_SHORT
                ).show()
            }

            R.id.effect_cancel -> {
                binding.llEffect.visibility = View.GONE
                binding.imgMain.setImageBitmap(originalBitmap1)

                binding.overlayLight.visibility = View.GONE
                binding.overlayTexture.visibility = View.GONE
                binding.overlayWeather.visibility = View.GONE
            }

            R.id.effect_confirm -> {
                val bitmap = (binding.imgMain.drawable as BitmapDrawable).bitmap
                originalBitmap1 = bitmap

                binding.imgMain.setImageBitmap(originalBitmap1)

                binding.llEffect.visibility = View.GONE
            }

            R.id.effect_back -> {
                binding.llEffectType.visibility = View.VISIBLE
                binding.llBlendType.visibility = View.GONE
                binding.seekbarBlend.visibility = View.GONE
            }

            R.id.adjust_cancel -> {
                binding.llAdjust.visibility = View.GONE
                binding.imgMain.setImageBitmap(originalBitmap1)
            }

            R.id.adjust_confirm -> {
                val bitmap = (binding.imgMain.drawable as BitmapDrawable).bitmap
                originalBitmap1 = bitmap

                binding.imgMain.setImageBitmap(originalBitmap1)

                binding.llAdjust.visibility = View.GONE
                Log.d("onClick", "adjust confirmed clicked")
            }

            R.id.hsl_cancel -> {
                binding.llHsl.visibility = View.GONE
                binding.imgMain.setImageBitmap(originalBitmap1)
            }

            R.id.hsl_confirm -> {
                val bitmap = (binding.imgMain.drawable as BitmapDrawable).bitmap
                originalBitmap1 = bitmap

                binding.imgMain.setImageBitmap(originalBitmap1)

                binding.llHsl.visibility = View.GONE
            }

            R.id.layers_cancel -> {
                HideStickers()
                binding.llLayers.visibility = View.GONE
            }

            R.id.layers_confirm -> {
                HideStickers()
                binding.llLayers.visibility = View.GONE
            }

            R.id.txt_resize -> {
                binding.llRotate.visibility = View.GONE
                binding.llResize.visibility = View.VISIBLE
            }

            R.id.txt_rotate -> {
                binding.llResize.visibility = View.GONE
                binding.llRotate.visibility = View.VISIBLE
            }

            R.id.crop_rotate_left -> {
                binding.cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D)
            }

            R.id.crop_rotate_right -> {
                binding.cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
            }

            R.id.flip_horizontal -> {
                binding.cropImageView.imageBitmap =
                    flip(binding.cropImageView.imageBitmap, LinearLayoutManager.HORIZONTAL)
            }

            R.id.flip_vertical -> {
                binding.cropImageView.imageBitmap =
                    flip(binding.cropImageView.imageBitmap, LinearLayoutManager.VERTICAL)
            }

            R.id.ll_blend -> {
                binding.effectGallery.visibility = View.VISIBLE
                binding.seekbarBlend.visibility = View.GONE
                binding.listBlend.adapter = BlendAdapter(img_blend)
                binding.listBlendType.adapter = BlendTypeAdapter(img_blend)
                binding.llEffectType.visibility = View.GONE
                binding.llBlendType.visibility = View.VISIBLE
            }

            R.id.ll_light -> {
                binding.effectGallery.visibility = View.GONE
                binding.seekbarBlend.visibility = View.VISIBLE
                binding.seekbarBlend.setOnSeekBarChangeListener(EffectLightListener())
                val lightAdapter =
                    FilterNameAdapter(this, resources.getStringArray(R.array.effect_light))
                binding.listBlendType.adapter = lightAdapter

                binding.listBlend.adapter = LightAdapter(light1Array, binding.overlayLight)

                setLight(binding.overlayLight, light1Array)

                binding.overlayLight.visibility = View.VISIBLE

                lightAdapter.setOnFilterNameClick(object :
                    FilterNameAdapter.FilterNameClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        when (position) {
                            0 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(light1Array, binding.overlayLight)
                                setLight(binding.overlayLight, light1Array)
                            }
                            1 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(light2Array, binding.overlayLight)
                                setLight(binding.overlayLight, light2Array)
                            }
                            2 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(festivalArray, binding.overlayLight)
                                setLight(binding.overlayLight, festivalArray)
                            }
                            3 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(loveArray, binding.overlayLight)
                                setLight(binding.overlayLight, loveArray)
                            }
                            4 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(prismArray, binding.overlayLight)
                                setLight(binding.overlayLight, prismArray)
                            }
                            5 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(neonArray, binding.overlayLight)
                                setLight(binding.overlayLight, neonArray)
                            }
                            else -> {
                                binding.listBlend.adapter =
                                    LightAdapter(light1Array, binding.overlayLight)
                                setLight(binding.overlayLight, light1Array)
                            }
                        }
                    }
                })

                binding.llEffectType.visibility = View.GONE
                binding.llBlendType.visibility = View.VISIBLE
            }

            R.id.ll_texture -> {
                binding.effectGallery.visibility = View.GONE
                binding.seekbarBlend.visibility = View.VISIBLE
                binding.seekbarBlend.setOnSeekBarChangeListener(EffectTextureListener())
                val textureAdapter =
                    FilterNameAdapter(this, resources.getStringArray(R.array.effect_texture))
                binding.listBlendType.adapter = textureAdapter

                binding.listBlend.adapter = LightAdapter(dustArray, binding.overlayTexture)
                binding.overlayTexture.visibility = View.VISIBLE

                textureAdapter.setOnFilterNameClick(object :
                    FilterNameAdapter.FilterNameClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        when (position) {
                            0 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(dustArray, binding.overlayTexture)
                                setLight(binding.overlayTexture, dustArray)
                            }
                            1 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(stainArray, binding.overlayTexture)
                                setLight(binding.overlayTexture, stainArray)
                            }
                            2 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(vintageArray, binding.overlayTexture)
                                setLight(binding.overlayTexture, vintageArray)
                            }
                            3 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(scratchArray, binding.overlayTexture)
                                setLight(binding.overlayTexture, scratchArray)
                            }
                            else -> {
                                binding.listBlend.adapter =
                                    LightAdapter(dustArray, binding.overlayTexture)
                                setLight(binding.overlayTexture, dustArray)
                            }
                        }
                    }
                })

                binding.llEffectType.visibility = View.GONE
                binding.llBlendType.visibility = View.VISIBLE
            }

            R.id.ll_weather -> {
                binding.effectGallery.visibility = View.GONE
                binding.seekbarBlend.visibility = View.VISIBLE
                binding.seekbarBlend.setOnSeekBarChangeListener(EffectWeatherListener())

                val weatherAdapter =
                    FilterNameAdapter(this, resources.getStringArray(R.array.effect_weather))
                binding.listBlendType.adapter = weatherAdapter

                binding.listBlend.adapter = LightAdapter(snowArray, binding.overlayWeather)
                binding.overlayWeather.visibility = View.VISIBLE

                weatherAdapter.setOnFilterNameClick(object :
                    FilterNameAdapter.FilterNameClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        when (position) {
                            0 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(snowArray, binding.overlayWeather)
                                setLight(binding.overlayWeather, snowArray)
                            }
                            1 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(cloudArray, binding.overlayWeather)
                                setLight(binding.overlayWeather, cloudArray)
                            }
                            2 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(fogArray, binding.overlayWeather)
                                setLight(binding.overlayWeather, fogArray)
                            }
                            3 -> {
                                binding.listBlend.adapter =
                                    LightAdapter(sunlightArray, binding.overlayWeather)
                                setLight(binding.overlayWeather, sunlightArray)
                            }
                            else -> {
                                binding.listBlend.adapter =
                                    LightAdapter(snowArray, binding.overlayWeather)
                                setLight(binding.overlayWeather, snowArray)
                            }
                        }
                    }

                })

                binding.llEffectType.visibility = View.GONE
                binding.llBlendType.visibility = View.VISIBLE
            }

            R.id.ll_text -> {
                checkClick()
                opendialogtext()
            }

            R.id.ll_sticker -> {
                checkClick()
                opendialogSticker()
            }

            R.id.ll_border -> {
                binding.layerLayout.visibility = View.GONE
                binding.borderLayout.visibility = View.VISIBLE
            }

            R.id.border_back -> {
                binding.layerLayout.visibility = View.VISIBLE
                binding.borderLayout.visibility = View.GONE
            }

            R.id.effect_gallery -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_PICK
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), pickImage)
            }

            R.id.border_blur -> {
                val gpuImage = GPUImage(this@ImageEditActivity)
                gpuImage.setImage(blurBitmap)
                gpuImage.setFilter(GPUImageGaussianBlurFilter(5F))

                binding.frameLayout.background =
                    BitmapDrawable(resources, gpuImage.bitmapWithFilterApplied)
            }

            R.id.img_save -> {
                checkClick()

                binding.imgReset.visibility = View.GONE
                binding.llCrop.visibility = View.GONE
                binding.llFilter.visibility = View.GONE
                binding.llEffect.visibility = View.GONE
                binding.llAdjust.visibility = View.GONE
                binding.llHsl.visibility = View.GONE
                binding.llLayers.visibility = View.GONE

                MainActivity.isFromSaved = true

                try {
                    savedImageUri = Uri.parse(saveBitmap(screenShot))
                } catch (ex: RuntimeException) {
                    Log.e(TAG, "onClick: exception: $ex")
                }

                alert = DialogHandler.getInstance().showSaveLayout(this, false)

                Handler().postDelayed({
                    if (alert.isShowing) {
                        alert.dismiss()
                        if (savedImageUri != null) {
                            val intent =
                                Intent(this@ImageEditActivity, ShowImageActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            intent.putExtra("image_uri", savedImageUri!!.toString())
                            intent.putExtra("from", TAG)
                            startActivity(intent)
                            finish()
                        }
                    }

                }, 1000)

/*
                DialogHandler.getInstance().showSaveDialog(this, false, "editImage")
                { status, _, _, alertDialog ->
                    if (status.equals("0", ignoreCase = true)) {
                        alertDialog.dismiss()
                    } else if (status.equals("1", ignoreCase = true)) {
                        alertDialog.dismiss()
                        val intent = Intent(this, ShowImageActivity::class.java)
                        if (savedImageUri != null) {
                            intent.putExtra("image_uri", savedImageUri!!.toString())
                            startActivityForResult(intent, 2)
                            finish()
                        }
                    }
                }
*/
            }

            R.id.img_reset -> {
                checkClick()

                DialogHandler.getInstance().revertChangeDialog(
                    this,
                    true
                ) { status, _, _, alertDialog ->
                    if (status.equals("0", ignoreCase = true)) {
                        alertDialog.dismiss()
                    } else {
                        var doBreak = false
                        while (!doBreak) {
                            val childcount: Int = binding.frameLayout.childCount
                            if (childcount > 0) {
                                var i = 0
                                for (i in 0 until childcount) {

                                    var v: View = binding.frameLayout.getChildAt(i)

                                    if (v is StickerView) {
                                        binding.frameLayout.removeView(v)
                                        break
                                    }
                                }
                                if (childcount == 1) {
                                    doBreak = true
                                }
                            }
                        }

                        binding.imageFrame.setPadding(0, 0, 0, 0)
                        binding.frameLayout.setBackgroundColor(resources.getColor(R.color.transparent))

                        binding.overlayLight.visibility = View.GONE
                        binding.overlayWeather.visibility = View.GONE
                        binding.overlayTexture.visibility = View.GONE

                        originalBitmap1 = imageBitmap
                        binding.imgMain.setImageBitmap(originalBitmap1)

                        alertDialog!!.dismiss()
                    }
                }
            }
            R.id.rl_back -> {
                onBackPressed()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setLight(img_light: ImageView, effect: Array<EffectData>) {
        img_light.visibility = View.VISIBLE
        val mainBitmap = (binding.imgMain.drawable as BitmapDrawable).bitmap
        var bitmap = (resources.getDrawable(effect[0].icon) as BitmapDrawable).bitmap
        bitmap = Bitmap.createScaledBitmap(
            bitmap,
            mainBitmap.width,
            mainBitmap.height,
            true
        )
        img_light.setImageBitmap(bitmap)

        binding.seekbarBlend.progress = 90
        img_light.imageAlpha = binding.seekbarBlend.progress

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            if (data != null) {
                try {
                    var uri: Uri = data.data!!

                    var inputStream: InputStream?
                    try {
                        inputStream = contentResolver.openInputStream(uri)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        inputStream = null
                    }

                    isFromGallery = true
                    blend_bitmap = BitmapFactory.decodeStream(inputStream)

                    CreateBitmap().executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        blend_bitmap
                    )

                    /* var bmp = blend_bitmap
                     bmp = AndroidUtils.resizeImageToNewSize(
                         bmp,
                         bmp.width / 2,
                         bmp.height / 2
                     )

                     var stream = ByteArrayOutputStream()
                     bmp.compress(Bitmap.CompressFormat.JPEG, 20, stream)
                     blend_bitmap = bmp

                     var gpuImage1 = GPUImage(this@ImageEditActivity)
                     gpuImage1.setImage(original_bitmap)
                     gpuImage1.setFilter(
                         createBlendFilter(
                             filters_blend[blendfilter_position],
                             blend_bitmap
                         )
                     )
                     binding.imgMain.setImageBitmap(gpuImage1.bitmapWithFilterApplied)*/

                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class CreateBitmap : AsyncTask<Bitmap, Void, Bitmap>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Bitmap?): Bitmap? {

            var bmp = params[0]
            bmp = AndroidUtils.resizeImageToNewSize(
                bmp!!,
                bmp.width / 2,
                bmp.height / 2
            )

            var stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 20, stream)
            blend_bitmap = bmp

            if (originalBitmap1.width > originalBitmap1.height) {

                blend_bitmap = ThumbnailUtils.extractThumbnail(
                    bmp,
                    originalBitmap1.width,
                    originalBitmap1.height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                )
            } else if (originalBitmap1.width < originalBitmap1.height) {
                blend_bitmap = ThumbnailUtils.extractThumbnail(
                    bmp,
                    originalBitmap1.width,
                    originalBitmap1.height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                )
            } else {
                blend_bitmap = bmp
            }

            return blend_bitmap
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)

            var gpuImage1 = GPUImage(this@ImageEditActivity)
            gpuImage1.setImage(originalBitmap1)
            gpuImage1.setFilter(
                createBlendFilter(
                    filters_blend[blendfilter_position],
                    blend_bitmap
                )
            )
            binding.imgMain.setImageBitmap(gpuImage1.bitmapWithFilterApplied)
        }
    }

    private val screenShot: Bitmap
        get() {
            val findViewById = findViewById<View>(R.id.ll_root)
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


    private fun opendialogtext() {
        var dialog = android.app.AlertDialog.Builder(this)
        val factory = LayoutInflater.from(this)

        val subview: View = factory.inflate(R.layout.textdialog_layout, null)

        val alertDialog: android.app.AlertDialog = dialog.create()

        alertDialog.setView(subview)
        alertDialog.setCancelable(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editText = subview.findViewById(R.id.dialogEditText) as EditText
        val btnDone = subview.findViewById(R.id.btn_done) as Button
        var listFont: RecyclerView = subview.findViewById(R.id.list_font) as RecyclerView
        var listColor: RecyclerView = subview.findViewById(R.id.list_color) as RecyclerView

        listFont.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        var fontAdapter = FontAdapter(this)
        listFont.adapter = fontAdapter

        editText.typeface = Typeface.createFromAsset(assets, "font/anton_regular.ttf")

        fontAdapter.setOnFontClick(object : FontAdapter.FontClickListener {
            override fun onItemClick(view: View, fontName: String) {
                editText.typeface = Typeface.createFromAsset(assets, fontName)
            }
        })

        listColor.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val colorAdapter = ColorAdapter(this)

        listColor.adapter = colorAdapter
        colorAdapter.setOnColorClick(object : ColorAdapter.ColorClickListener {
            override fun onItemClick(view: View, colorName: String) {
                editText.setTextColor(Integer.valueOf(Color.parseColor(colorName)))
            }
        })

        btnDone.setOnClickListener {
            val tvSticker = StickerTextView(this@ImageEditActivity)
            tvSticker.tv_main!!.text = editText.text.toString()
            tvSticker.tv_main!!.typeface = editText.typeface
            tvSticker.tv_main!!.setTextColor(editText.textColors)
            binding.frameLayout.addView(tvSticker)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun opendialogSticker() {
        var dialog = android.app.AlertDialog.Builder(this)
        val factory = LayoutInflater.from(this)

        val subview: View = factory.inflate(R.layout.stickerdialog_layout, null)

        val alertDialog: android.app.AlertDialog = dialog.create()

        alertDialog.setView(subview)
        alertDialog.setCancelable(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var listSticker: RecyclerView = subview.findViewById(R.id.list_sticker) as RecyclerView
        var listStickerTab: RecyclerView =
            subview.findViewById(R.id.list_sticker_tab) as RecyclerView

        listStickerTab.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        var stickerTabAdapter = StickerTabAdapter(this)
        listStickerTab.adapter = stickerTabAdapter

        listSticker.layoutManager = GridLayoutManager(this, 7, GridLayoutManager.VERTICAL, false)
        var stickerAdapter = StickerAdapter(this, 0)
        listSticker.adapter = stickerAdapter

        stickerTabAdapter.setTabClickListener(object : StickerTabAdapter.StickerTabListener {
            override fun onTabSelected(view: View, position: Int) {
                stickerAdapter = StickerAdapter(this@ImageEditActivity, position)
                listSticker.adapter = stickerAdapter
                stickerAdapter.notifyDataSetChanged()

                stickerAdapter.setOnStickerClick(object : StickerAdapter.StickerListener {
                    override fun onStickerClick(view: View, drawable: Drawable) {
                        var iv_sticker = StickerImageView(this@ImageEditActivity)
                        iv_sticker.setImageDrawable(drawable)
                        binding.frameLayout.addView(iv_sticker)
                        alertDialog.dismiss()
                    }
                })
            }
        })

        stickerAdapter.setOnStickerClick(object : StickerAdapter.StickerListener {
            override fun onStickerClick(view: View, drawable: Drawable) {
                var ivSticker = StickerImageView(this@ImageEditActivity)
                ivSticker.setImageDrawable(drawable)
                binding.frameLayout.addView(ivSticker)
                alertDialog.dismiss()
            }
        })

        alertDialog.show()
    }

    private fun flip(src: Bitmap, type: Int): Bitmap {
        val matrix = Matrix()

        when (type) {
            LinearLayoutManager.VERTICAL -> {
                matrix.preScale(1.0f, -1.0f)
            }
            LinearLayoutManager.HORIZONTAL -> {
                matrix.preScale(-1.0f, 1.0f)
            }
            else -> {
                return src
            }
        }

        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    lateinit var display: DisplayMetrics
    var density: Float = 0.0f
    internal var dHeight: Int = 0
    internal var dWidth: Int = 0
    lateinit var originalBitmap1: Bitmap
    lateinit var imageBitmap: Bitmap
    lateinit var blurBitmap: Bitmap
    lateinit var hslBitmap: Bitmap


    var arrayImg: TypedArray? = null
    var arrayText: Array<String>? = null
    var stickerColor: Array<String>? = null
    var fontsSticker: Array<String>? = null

    var selectedIndex: Int = 1
    var imageUri: String? = null

    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null

    inner class borderListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            binding.imageFrame.setPadding(progress, progress, progress, progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    inner class adjustListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            filterAdjuster!!.adjust(progress)

            filter_apply(adjust_position)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    inner class hueListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            filteradjusterHue!!.adjust(progress)

            groupFilter(
                progress,
                binding.seekbarSaturation.progress,
                binding.seekbarBrightness.progress
            )
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    inner class saturationListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            filteradjusterSat!!.adjust(progress)
            groupFilter(binding.seekbarHue.progress, progress, binding.seekbarBrightness.progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    inner class brightnessListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            filteradjusterBright!!.adjust(progress)

            groupFilter(binding.seekbarHue.progress, binding.seekbarSaturation.progress, progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    inner class EffectLightListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            binding.overlayLight.imageAlpha = progress

//            binding.overlayLight.setImageBitmap((binding.overlayLight.drawable as BitmapDrawable).bitmap)

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    inner class EffectTextureListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            binding.overlayTexture.imageAlpha = progress
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    inner class EffectWeatherListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            binding.overlayWeather.imageAlpha = progress
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    private var filteradjusterHue: GPUImageFilterTools.FilterAdjuster? = null
    private var filteradjusterSat: GPUImageFilterTools.FilterAdjuster? = null
    private var filteradjusterBright: GPUImageFilterTools.FilterAdjuster? = null

    fun groupFilter(progress_hue: Int, progress_sat: Int, progress_bright: Int) {

        val gpuImage1 = GPUImage(this@ImageEditActivity)
        gpuImage1.setImage(originalBitmap1)

        val group = GPUImageFilterGroup()
        group.addFilter(GPUImageHueFilter())
        group.addFilter(GPUImageSaturationFilter())
        group.addFilter(GPUImageBrightnessFilter())

        val mergedFilters = group.mergedFilters
        filteradjusterHue = GPUImageFilterTools.FilterAdjuster(mergedFilters.get(0))
        filteradjusterHue!!.adjust(progress_hue)
        filteradjusterSat = GPUImageFilterTools.FilterAdjuster(mergedFilters.get(1))
        filteradjusterSat!!.adjust(progress_sat)
        filteradjusterBright = GPUImageFilterTools.FilterAdjuster(mergedFilters.get(2))
        filteradjusterBright!!.adjust(progress_bright)

        gpuImage1.setFilter(group)
        hslBitmap = gpuImage1.bitmapWithFilterApplied
        binding.imgMain.setImageBitmap(hslBitmap)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arrayImg = resources.obtainTypedArray(R.array.img_options)
        arrayText = resources.getStringArray(R.array.text_options)
        stickerColor = resources.getStringArray(R.array.sticker_color)
        fontsSticker = resources.getStringArray(R.array.fonts_sticker)

        imageUri = intent.getStringExtra("image_uri")

        display = resources.displayMetrics
        density = resources.displayMetrics.density
        dWidth = display.widthPixels
        dHeight = (display.heightPixels.toFloat() - density * 150.0f).toInt()


        var inputStream: InputStream?
        try {
            inputStream = contentResolver.openInputStream(Uri.parse(imageUri))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            inputStream = null
        }

        originalBitmap1 = BitmapFactory.decodeStream(inputStream)
        originalBitmap1 = AndroidUtils.resizeImageToNewSize(originalBitmap1, dWidth, dHeight)

        imageBitmap = originalBitmap1
        blurBitmap = originalBitmap1
        binding.imgMain.setImageBitmap(originalBitmap1)
        binding.thumbnailFilter.setImageBitmap(originalBitmap1)

        binding.listResize.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listResize.adapter = ResizeAdapter(this, this)

        binding.listOptions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listOptions.adapter = OptionAdapter()

        binding.listFilterstype.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.filterNames.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val filterNameAdapter = FilterNameAdapter(this, resources.getStringArray(R.array.filters))

        val filters = arrayListOf<FilterData>()
        filters.addAll(filter_clr1)
        filters.addAll(filter_clr2)
        filters.addAll(filter_duo)
        filters.addAll(filter_pink)
        filters.addAll(filter_fresh)
        filters.addAll(filter_euro)
        filters.addAll(filter_dark)
        filters.addAll(filter_ins)
        filters.addAll(filter_elegant)
        filters.addAll(filter_golden)
        filters.addAll(filter_tint)
        filters.addAll(filter_film)
        filters.addAll(filter_lomo)
        filters.addAll(filter_movie)
        filters.addAll(filter_retro)
        filters.addAll(filter_bw)

        var filterDetailAdapter = FilterDetailAdapter(filters)
        binding.listFilterstype.adapter = filterDetailAdapter

        binding.thumbnailFilter.setOnClickListener {

            binding.rlFilteritem.setBackgroundResource(R.drawable.selected_image_bg)
            binding.imgMain.setImageBitmap(originalBitmap1)

            filterDetailAdapter.clearSelection()
        }

        filterNameAdapter.setOnFilterNameClick(object : FilterNameAdapter.FilterNameClickListener {
            override fun onItemClick(view: View, position: Int) {
                binding.rlFilteritem.setBackgroundColor(resources.getColor(R.color.transparent))
                when (position) {
                    0 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_clr1.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    1 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_clr2.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    2 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_duo.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    3 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_pink.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    4 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_fresh.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    5 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_euro.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    6 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_dark.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    7 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_ins.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    8 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_elegant.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    9 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_golden.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    10 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_tint.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    11 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_film.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    12 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_lomo.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    13 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_movie.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    14 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_retro.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    15 -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_bw.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                    else -> {
                        filterDetailAdapter =
                            FilterDetailAdapter(filter_clr1.toMutableList() as ArrayList<FilterData>)
                        binding.listFilterstype.adapter = filterDetailAdapter
                    }
                }
                filterNameAdapter.notifyDataSetChanged()
                filterDetailAdapter.notifyDataSetChanged()
            }
        })

        binding.filterNames.adapter = filterNameAdapter

        binding.listBlend.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listBlend.adapter = BlendAdapter(img_blend)

        binding.listBlendType.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listBlendType.adapter = BlendTypeAdapter(img_blend)

        binding.listAdjust.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listAdjust.adapter = AdjustAdapter()

        binding.listBorder.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        var gpuImage = GPUImage(this@ImageEditActivity)
        gpuImage.setImage(blurBitmap)
        gpuImage.setFilter(GPUImageGaussianBlurFilter(5F))

        binding.borderBlur.setImageBitmap(gpuImage.bitmapWithFilterApplied)

        binding.borderBlur.setOnClickListener(this)

        val cAdapter = ColorAdapter(this)
        cAdapter.setOnColorClick(object : ColorAdapter.ColorClickListener {
            override fun onItemClick(view: View, colorName: String) {
                binding.frameLayout.setBackgroundColor(Integer.valueOf(Color.parseColor(colorName)))
            }
        })
        binding.listBorder.adapter = cAdapter

        binding.cropCancel.setOnClickListener(this)
        binding.cropConfirm.setOnClickListener(this)
        binding.filterConfirm.setOnClickListener(this)
        binding.filterCancel.setOnClickListener(this)
        binding.effectConfirm.setOnClickListener(this)
        binding.effectCancel.setOnClickListener(this)
        binding.effectBack.setOnClickListener(this)
        binding.adjustConfirm.setOnClickListener(this)
        binding.adjustCancel.setOnClickListener(this)
        binding.hslConfirm.setOnClickListener(this)
        binding.hslCancel.setOnClickListener(this)
        binding.layersConfirm.setOnClickListener(this)
        binding.layersCancel.setOnClickListener(this)
        binding.doneAdjust.setOnClickListener(this)
        binding.doneFilter.setOnClickListener(this)

        binding.txtResize.setOnClickListener(this)
        binding.txtRotate.setOnClickListener(this)
        binding.cropRotateLeft.setOnClickListener(this)
        binding.cropRotateRight.setOnClickListener(this)
        binding.flipHorizontal.setOnClickListener(this)
        binding.flipVertical.setOnClickListener(this)
        binding.imgSave.setOnClickListener(this)
        binding.imgReset.setOnClickListener(this)

        binding.llBlend.setOnClickListener(this)
        binding.llLight.setOnClickListener(this)
        binding.llTexture.setOnClickListener(this)
        binding.llWeather.setOnClickListener(this)
        binding.rlBack.setOnClickListener(this)

        binding.llText.setOnClickListener(this)
        binding.llSticker.setOnClickListener(this)
        binding.llBorder.setOnClickListener(this)
        binding.borderBack.setOnClickListener(this)
        binding.effectGallery.setOnClickListener(this)

        binding.seekbarBorder.setOnSeekBarChangeListener(borderListener())
        binding.seekbarAdjust1.setOnSeekBarChangeListener(adjustListener())

        binding.seekbarHue.setOnSeekBarChangeListener(hueListener())
        binding.seekbarSaturation.setOnSeekBarChangeListener(saturationListener())
        binding.seekbarBrightness.setOnSeekBarChangeListener(brightnessListener())

/*
        AdLoader.ads.ShowFBAds(this@ImageEditActivity)
*/

        binding.frameLayout.setOnTouchListener { v, event ->
            if (event!!.action == MotionEvent.ACTION_DOWN) {
                HideStickers()
            }
            true
        }
    }

    inner class FilterDetailAdapter(filters: ArrayList<FilterData>) :
        RecyclerView.Adapter<FilterDetailAdapter.FilterDetailHolder>() {
        var filterType = filters
        var selectedindex = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterDetailHolder {
            var view = LayoutInflater.from(this@ImageEditActivity)
                .inflate(R.layout.item_image_edit_filter, parent, false)
            return FilterDetailHolder(view)
        }

        override fun getItemCount(): Int {
            return filterType.size
        }

        override fun onBindViewHolder(holder: FilterDetailHolder, position: Int) {
            holder.thumbnail_filter.setImageResource(R.drawable.thumb_filter)

            red = filterType[holder.absoluteAdapterPosition].red
            green = filterType[holder.absoluteAdapterPosition].green
            blue = filterType[holder.absoluteAdapterPosition].blue
            saturation = filterType[holder.absoluteAdapterPosition].saturation

            var bitmap = Bitmap.createBitmap(
                originalBitmap1.width,
                originalBitmap1.height,
                Bitmap.Config.ARGB_8888
            )
            var canvas = Canvas(bitmap)

            var paint = Paint()
            var colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(saturation)

            var colorScale = ColorMatrix()
            colorScale.setScale(red, green, blue, 1F)
            colorMatrix.postConcat(colorScale)

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(originalBitmap1, 0F, 0F, paint)

            holder.thumbnail_filter.setImageBitmap(bitmap)

            holder.filterName.text = filterType[position].text

            holder.rl_filteritem.setBackgroundResource(R.drawable.selected_image_bg)


            if (selectedindex == position) {
                holder.rl_filteritem.setBackgroundResource(R.drawable.selected_image_bg)

                red = filterType[holder.absoluteAdapterPosition].red
                green = filterType[holder.absoluteAdapterPosition].green
                blue = filterType[holder.absoluteAdapterPosition].blue
                saturation = filterType[holder.absoluteAdapterPosition].saturation

                Async_Filter(
                    originalBitmap1,
                    binding.imgMain
                ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, red, green, blue)

            } else {
                holder.rl_filteritem.setBackgroundColor(resources.getColor(R.color.transparent))
            }

            holder.rl_filteritem.setOnClickListener {
                binding.rlFilteritem.setBackgroundResource(R.color.transparent)

                selectedindex = holder.absoluteAdapterPosition

                red = filterType[holder.absoluteAdapterPosition].red
                green = filterType[holder.absoluteAdapterPosition].green
                blue = filterType[holder.absoluteAdapterPosition].blue
                saturation = filterType[holder.absoluteAdapterPosition].saturation

                Async_Filter(
                    originalBitmap1,
                    binding.imgMain
                ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, red, green, blue)
                notifyDataSetChanged()
            }
        }

        fun clearSelection() {
            val previousSelectedItem: Int = selectedindex
            selectedindex = -1
            notifyItemChanged(previousSelectedItem)
        }

        inner class FilterDetailHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var thumbnail_filter: ImageView
            var filterName: TextView
            var rl_filteritem: RelativeLayout

            init {
                thumbnail_filter = itemView.findViewById(R.id.thumbnail_filter)
                filterName = itemView.findViewById(R.id.filterName)
                rl_filteritem = itemView.findViewById(R.id.rl_filteritem)
            }
        }
    }

    class Async_Filter() : AsyncTask<Float, Void, Bitmap>() {

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

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(this.originalBitmap, 0F, 0F, paint)

            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            this.imgMain.setImageBitmap(result)
        }
    }

    fun HideStickers() {
        var fm = binding.frameLayout
        var childcount: Int = binding.frameLayout.childCount

        if (childcount != 0) {
            for (i in 0 until childcount) {
                var v: View = fm.getChildAt(i)

                if (v is StickerView) {
                    v.setControlItemsHidden(true)
                }
            }
        }
    }

    inner class OptionAdapter : RecyclerView.Adapter<OptionAdapter.OptionHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionHolder {
            var view: View = LayoutInflater.from(this@ImageEditActivity)
                .inflate(R.layout.item_option, parent, false)

            return OptionHolder(view)
        }

        override fun getItemCount(): Int {
            return arrayText!!.size
        }

        override fun onBindViewHolder(holder: OptionHolder, position: Int) {
            holder.img_option.setImageResource(
                arrayImg!!.getResourceId(
                    holder.absoluteAdapterPosition,
                    0
                )
            )
            holder.txt_option.text = arrayText!![holder.absoluteAdapterPosition]

//            var lp: LinearLayout.LayoutParams =
//                LinearLayout.LayoutParams(D_width / 6, LinearLayout.LayoutParams.WRAP_CONTENT)
//            holder.ll_option.layoutParams = lp

            if (isGone) {
                holder.txt_option.setTextColor(resources.getColor(R.color.white))
                holder.tab_layout.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
            }

            if (selectedIndex == holder.absoluteAdapterPosition) {
                holder.txt_option.setTextColor(resources.getColor(R.color.colorAccent))
                holder.tab_layout.setBackgroundResource(R.drawable.bg_lightpurple_selected_bg)
            } else {
                holder.txt_option.setTextColor(resources.getColor(R.color.white))
                holder.tab_layout.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
            }

            holder.ll_option.setOnClickListener {
                selectedIndex = holder.absoluteAdapterPosition

                when (position) {
                    0 -> {
                        binding.llCrop.visibility = View.VISIBLE
                        binding.llFilter.visibility = View.GONE
                        binding.llEffect.visibility = View.GONE
                        binding.llAdjust.visibility = View.GONE
                        binding.llHsl.visibility = View.GONE
                        binding.llLayers.visibility = View.GONE
                        binding.cropImageView.imageBitmap = originalBitmap1

                    }

                    1 -> {
                        binding.llCrop.visibility = View.GONE
                        binding.llFilter.visibility = View.VISIBLE
                        binding.llEffect.visibility = View.GONE
                        binding.llAdjust.visibility = View.GONE
                        binding.llHsl.visibility = View.GONE
                        binding.llLayers.visibility = View.GONE
                    }

                    2 -> {
                        binding.llCrop.visibility = View.GONE
                        binding.llFilter.visibility = View.GONE
                        binding.llEffect.visibility = View.VISIBLE
                        binding.llAdjust.visibility = View.GONE
                        binding.llHsl.visibility = View.GONE
                        binding.llLayers.visibility = View.GONE
                    }

                    3 -> {
                        binding.llCrop.visibility = View.GONE
                        binding.llFilter.visibility = View.GONE
                        binding.llEffect.visibility = View.GONE
                        binding.llAdjust.visibility = View.VISIBLE
                        binding.llHsl.visibility = View.GONE
                        binding.llLayers.visibility = View.GONE

                        filterAdjuster = GPUImageFilterTools.FilterAdjuster(filter_adjust[0])
                        binding.seekbarAdjust1.progress = 90
                        filterAdjuster!!.adjust(binding.seekbarAdjust1.progress)

                        filter_apply(0)
                    }

                    4 -> {
                        binding.llCrop.visibility = View.GONE
                        binding.llFilter.visibility = View.GONE
                        binding.llEffect.visibility = View.GONE
                        binding.llAdjust.visibility = View.GONE
                        binding.llHsl.visibility = View.VISIBLE
                        binding.llLayers.visibility = View.GONE

                        hslBitmap = originalBitmap1
                        groupFilter(
                            binding.seekbarHue.progress,
                            binding.seekbarSaturation.progress,
                            binding.seekbarBrightness.progress
                        )
                    }

                    5 -> {
                        binding.llCrop.visibility = View.GONE
                        binding.llFilter.visibility = View.GONE
                        binding.llEffect.visibility = View.GONE
                        binding.llAdjust.visibility = View.GONE
                        binding.llHsl.visibility = View.GONE
                        binding.llLayers.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.llCrop.visibility = View.GONE
                        binding.llFilter.visibility = View.VISIBLE
                        binding.llEffect.visibility = View.GONE
                        binding.llAdjust.visibility = View.GONE
                        binding.llHsl.visibility = View.GONE
                        binding.llLayers.visibility = View.GONE
                    }
                }
                notifyDataSetChanged()
            }
        }

        inner class OptionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var img_option: ImageView = itemView.findViewById(R.id.img_option) as ImageView
            var txt_option: TextView = itemView.findViewById(R.id.txt_option) as TextView
            var ll_option: RelativeLayout = itemView.findViewById(R.id.ll_option) as RelativeLayout
            var tab_layout: RelativeLayout =
                itemView.findViewById(R.id.tab_layout) as RelativeLayout
        }
    }

    fun filter_apply(position: Int) {
        var gpuImage1 = GPUImage(this@ImageEditActivity)
        gpuImage1.setImage(originalBitmap1)
        gpuImage1.setFilter(filter_adjust[position])
        binding.imgMain.setImageBitmap(gpuImage1.bitmapWithFilterApplied)
    }

    inner class LightAdapter(effectList: Array<EffectData>, imageview: ImageView) :
        RecyclerView.Adapter<LightAdapter.LightHolder>() {
        var selectedindex = 0
        var effects: Array<EffectData>?
        var img_overlay = imageview

        init {
            effects = effectList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightHolder {
            var view: View = LayoutInflater.from(this@ImageEditActivity)
                .inflate(R.layout.item_filter, parent, false)

            return LightHolder(view)
        }

        override fun getItemCount(): Int {
            return effects!!.size
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onBindViewHolder(holder: LightHolder, position: Int) {
            holder.filterName.text = effects!![holder.absoluteAdapterPosition].name
            holder.thumbnailFilter.setImageResource(effects!![holder.absoluteAdapterPosition].icon)

            if (selectedindex == holder.absoluteAdapterPosition) {
                holder.rl_filteritem.setBackgroundResource(R.drawable.selected_image_bg)
            } else {
                holder.rl_filteritem.setBackgroundColor(resources.getColor(R.color.transparent))
            }

            holder.rl_filteritem.setOnClickListener {
                selectedindex = holder.absoluteAdapterPosition
                selectedPosition = holder.absoluteAdapterPosition
                img_overlay.visibility = View.VISIBLE

                val main_bitmap = (binding.imgMain.drawable as BitmapDrawable).bitmap
                var bitmap =
                    (resources.getDrawable(effects!![holder.absoluteAdapterPosition].icon) as BitmapDrawable).bitmap
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    main_bitmap.width,
                    main_bitmap.height,
                    true
                )

                /* var bmp: Bitmap

                             if (original_bitmap.getWidth() > original_bitmap.getHeight()) {

                                 bmp = ThumbnailUtils.extractThumbnail(
                                     bitmap,
                                     original_bitmap.getWidth(),
                                     original_bitmap.getHeight(),
                                     ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                                 )
                             } else if (original_bitmap.getWidth() < original_bitmap.getHeight()) {
                                 bmp = ThumbnailUtils.extractThumbnail(
                                     bitmap,
                                     original_bitmap.getWidth(),
                                     original_bitmap.getHeight(),
                                     ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                                 )
                             } else {
                                 bmp = bitmap
                             }*/

                img_overlay.setImageBitmap(bitmap)

                binding.seekbarBlend.progress = 90
                img_overlay.imageAlpha = binding.seekbarBlend.progress
                notifyDataSetChanged()
            }
        }

        inner class LightHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var thumbnailFilter: ImageView = itemView.findViewById(R.id.thumbnail_filter)
            var filterName: TextView = itemView.findViewById(R.id.filterName)
            var rl_filteritem: RelativeLayout = itemView.findViewById(R.id.rl_filteritem)
        }
    }

    inner class BlendTypeAdapter(images: Array<Int>) :
        RecyclerView.Adapter<BlendTypeAdapter.BlendTypeHolder>() {

        var selectedindex = 0
        var text_Blend_type: Array<String> = arrayOf(

            getString(R.string.effect_alpha),
            getString(R.string.effect_normal),
            getString(R.string.effect_lighten),
            getString(R.string.effect_screen),
            getString(R.string.effect_colorDodge),
            getString(R.string.effect_linearBurn),
            getString(R.string.effect_darken),
            getString(R.string.effect_multiply),
            getString(R.string.effect_overlay),
            getString(R.string.effect_hardLight),
            getString(R.string.effect_exclusion),
            getString(R.string.effect_difference),
            getString(R.string.effect_divide),
        )

        var img_effect = images

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlendTypeHolder {
            var view: View = LayoutInflater.from(this@ImageEditActivity)
                .inflate(R.layout.item_blend_type, parent, false)

            return BlendTypeHolder(view)
        }

        override fun getItemCount(): Int {
            return text_Blend_type.size
        }

        override fun onBindViewHolder(holder: BlendTypeHolder, position: Int) {
            holder.text_blend_type.text = text_Blend_type[holder.absoluteAdapterPosition]

            if (selectedindex == holder.absoluteAdapterPosition) {
                holder.item_adjust.setBackgroundResource(R.drawable.round_corner)
            } else {
                holder.item_adjust.setBackgroundColor(resources.getColor(R.color.transparent))
            }

            holder.item_adjust.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {

                    blendfilter_position = holder.absoluteAdapterPosition
                    selectedindex = holder.absoluteAdapterPosition

                    var gpuImage1 = GPUImage(this@ImageEditActivity)
                    gpuImage1.setImage(originalBitmap1)

                    var blendFilter: GPUImageFilter
                    if (!isFromGallery) {

                        var image: Bitmap = BitmapFactory.decodeResource(
                            resources, img_effect[bledImage_position]
                        )
                        if (originalBitmap1.width > originalBitmap1.height) {

                            image = ThumbnailUtils.extractThumbnail(
                                image,
                                originalBitmap1.width,
                                originalBitmap1.height,
                                ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                            )
                        } else if (originalBitmap1.width < originalBitmap1.height) {
                            image = ThumbnailUtils.extractThumbnail(
                                image,
                                originalBitmap1.width,
                                originalBitmap1.height,
                                ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                            )
                        } else {
                            image = image
                        }

                        blendFilter = createBlendFilter(
                            filters_blend[blendfilter_position],
                            image
                        )
                        gpuImage1.setFilter(blendFilter)
                        binding.imgMain.setImageBitmap(gpuImage1.bitmapWithFilterApplied)
                    } else {

                        CreateBitmap().executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            blend_bitmap
                        )

//                        blendFilter = createBlendFilter(
//                            filters_blend[blendfilter_position],
//                            blend_bitmap
//                        )
                    }


                    notifyDataSetChanged()
                }
            })
        }

        inner class BlendTypeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var text_blend_type: TextView = itemView.findViewById(R.id.text_blend_type) as TextView
            var item_adjust: RelativeLayout =
                itemView.findViewById(R.id.item_adjust) as RelativeLayout
        }
    }

    var isFromGallery: Boolean = false
    var blendfilter_position: Int = 0
    var bledImage_position: Int = 0
    var filters_blend: Array<Class<out GPUImageTwoInputFilter>> = arrayOf(
        GPUImageAlphaBlendFilter::class.java,
        GPUImageNormalBlendFilter::class.java,
        GPUImageLightenBlendFilter::class.java,
        GPUImageScreenBlendFilter::class.java,
        GPUImageColorDodgeBlendFilter::class.java,
        GPUImageLinearBurnBlendFilter::class.java,
        GPUImageDarkenBlendFilter::class.java,
        GPUImageMultiplyBlendFilter::class.java,
        GPUImageOverlayBlendFilter::class.java,
        GPUImageHardLightBlendFilter::class.java,
        GPUImageExclusionBlendFilter::class.java,
        GPUImageDifferenceBlendFilter::class.java,
        GPUImageDivideBlendFilter::class.java
    )

    var img_blend: Array<Int> = arrayOf(
        R.drawable.blend_1,
        R.drawable.blend_2,
        R.drawable.blend_3,
        R.drawable.blend_4,
        R.drawable.blend_5,
        R.drawable.blend_6,
        R.drawable.blend_7,
        R.drawable.blend_8,
        R.drawable.blend_9,
        R.drawable.blend_10,
        R.drawable.blend_11,
        R.drawable.blend_12,
        R.drawable.blend_13,
        R.drawable.blend_14,
        R.drawable.blend_15,
        R.drawable.blend_16,
        R.drawable.blend_17,
        R.drawable.blend_18,
        R.drawable.blend_19,
        R.drawable.blend_20
    )

    private fun createBlendFilter(
        filterClass: Class<out GPUImageTwoInputFilter>,
        image: Bitmap
    ): GPUImageFilter {
        return try {
            /*      var bmp: Bitmap = image
                  if (original_bitmap.getWidth() > original_bitmap.getHeight()) {

                      bmp = ThumbnailUtils.extractThumbnail(
                          image,
                          original_bitmap.getWidth(),
                          original_bitmap.getHeight(),
                          ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                      )
                  } else if (original_bitmap.getWidth() < original_bitmap.getHeight()) {
                      bmp = ThumbnailUtils.extractThumbnail(
                          image,
                          original_bitmap.getWidth(),
                          original_bitmap.getHeight(),
                          ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                      )
                  } else {
                      bmp = image
                  }
      */
            filterClass.newInstance().apply {
                bitmap = image
            }
        } catch (e: Exception) {
            e.printStackTrace()
            GPUImageFilter()
        }
    }

    inner class BlendAdapter(images: Array<Int>) :
        RecyclerView.Adapter<BlendAdapter.BlendHolder>() {
        var selectedindex = 0

        var img_effects = images
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlendHolder {
            var view: View = LayoutInflater.from(this@ImageEditActivity)
                .inflate(R.layout.item_blend, parent, false)

            return BlendHolder(view)
        }

        override fun getItemCount(): Int {
            return img_effects.size
        }

        override fun onBindViewHolder(holder: BlendHolder, position: Int) {
            holder.thumbnail_blend.setImageResource(img_effects[holder.absoluteAdapterPosition])

            if (selectedindex == holder.absoluteAdapterPosition) {
                holder.rl_blenditem.setBackgroundResource(R.drawable.selected_image_bg)
            } else {
                holder.rl_blenditem.setBackgroundColor(resources.getColor(R.color.transparent))
            }

            holder.thumbnail_blend.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    isFromGallery = false
                    bledImage_position = holder.absoluteAdapterPosition
                    selectedindex = holder.absoluteAdapterPosition

                    var image: Bitmap = BitmapFactory.decodeResource(
                        resources, img_effects[bledImage_position]
                    )
                    if (originalBitmap1.width > originalBitmap1.height) {

                        image = ThumbnailUtils.extractThumbnail(
                            image,
                            originalBitmap1.width,
                            originalBitmap1.height,
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                        )
                    } else if (originalBitmap1.width < originalBitmap1.height) {
                        image = ThumbnailUtils.extractThumbnail(
                            image,
                            originalBitmap1.width,
                            originalBitmap1.height,
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                        )
                    } else {
                        image = image
                    }

                    var gpuImage1 = GPUImage(this@ImageEditActivity)
                    gpuImage1.setImage(originalBitmap1)
                    gpuImage1.setFilter(
                        createBlendFilter(
                            filters_blend[blendfilter_position],
                            image
                        )
                    )
                    binding.imgMain.setImageBitmap(gpuImage1.bitmapWithFilterApplied)
                    notifyDataSetChanged()
                }

            })
        }

        inner class BlendHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var thumbnail_blend: ImageView =
                itemView.findViewById(R.id.thumbnail_blend) as ImageView
            var rl_blenditem: RelativeLayout =
                itemView.findViewById(R.id.rl_blenditem) as RelativeLayout
        }
    }

    var filter_adjust: Array<GPUImageFilter> = arrayOf(
        GPUImageContrastFilter(),
        GPUImageHighlightShadowFilter(0.0f, 1.0f),//fade
        GPUImageSepiaToneFilter(),
        GPUImageOpacityFilter(1.0f),//grain
        GPUImageBilateralBlurFilter(),//convex
        GPUImageExposureFilter(0.0f),
        GPUImageRGBFilter(1.0f, 1.0f, 1.0f),  //  ambiance
        GPUImageVignetteFilter(PointF(0.5f, 0.5f), floatArrayOf(0.0f, 0.0f, 0.0f), 0.3f, 0.75f),
        GPUImageSharpenFilter(),
        GPUImageWhiteBalanceFilter(),
        GPUImageVibranceFilter(),
        GPUImageSaturationFilter(1.0f),
        GPUImageColorBalanceFilter()//skintone
    )

    inner class AdjustAdapter : RecyclerView.Adapter<AdjustAdapter.AdjustHolder>() {
        var selectedindex = 0
        var imgs_adjust: Array<Int> = arrayOf(
            R.drawable.icon_adjust_contrast,
            R.drawable.icon_adjust_fade,
            R.drawable.icon_adjust_tone,
            R.drawable.icon_adjust_grain,
            R.drawable.icon_adjust_convex,
            R.drawable.icon_adjust_exposure,
            R.drawable.icon_adjust_ambiance,
            R.drawable.icon_adjust_vignette,
            R.drawable.icon_adjust_sharpen,
            R.drawable.icon_adjust_temp,
            R.drawable.icon_adjust_vibrance,
            R.drawable.icon_adjust_saturation,
            R.drawable.icon_adjust_skintone
        )

        var texts_adjust: Array<String> = arrayOf(
            resources.getString(R.string.contrast_lbl),
            resources.getString(R.string.fade_lbl),
            resources.getString(R.string.tone_lbl),
            resources.getString(R.string.grain_lbl),
            resources.getString(R.string.convex_lbl),
            resources.getString(R.string.exposure_lbl),
            resources.getString(R.string.ambiance_lbl),
            resources.getString(R.string.vig_lbl),
            resources.getString(R.string.sharpen_lbl),
            resources.getString(R.string.temp_lbl),
            resources.getString(R.string.vib_lbl),
            resources.getString(R.string.sat_lbl),
            resources.getString(R.string.skintone_lbl),
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdjustHolder {
            var view: View = LayoutInflater.from(this@ImageEditActivity)
                .inflate(R.layout.item_adjust, parent, false)

            return AdjustHolder(view)
        }

        override fun getItemCount(): Int {
            return imgs_adjust.size
        }

        override fun onBindViewHolder(holder: AdjustHolder, position: Int) {
            holder.img_adjust.setImageResource(imgs_adjust[holder.absoluteAdapterPosition])
            holder.text_adjust.text = texts_adjust[holder.absoluteAdapterPosition]

            if (selectedindex == holder.absoluteAdapterPosition) {
                holder.tabBg.setBackgroundResource(R.drawable.bg_lightpurple_selected_bg)
                holder.text_adjust.setTextColor(resources.getColor(R.color.light_purple))
            } else {
                holder.tabBg.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
                holder.text_adjust.setTextColor(resources.getColor(R.color.white))
            }

            holder.item_adjust.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {

                    adjust_position = holder.absoluteAdapterPosition

                    selectedindex = holder.absoluteAdapterPosition

                    filterAdjuster =
                        GPUImageFilterTools.FilterAdjuster(filter_adjust[holder.absoluteAdapterPosition])
                    binding.seekbarAdjust1.progress = 90
                    filterAdjuster!!.adjust(binding.seekbarAdjust1.progress)

                    filter_apply(adjust_position)
                    notifyDataSetChanged()
                }

            })

        }

        inner class AdjustHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var img_adjust: ImageView = itemView.findViewById(R.id.img_adjust) as ImageView
            var text_adjust: TextView = itemView.findViewById(R.id.text_adjust) as TextView
            var item_adjust: RelativeLayout =
                itemView.findViewById(R.id.item_adjust) as RelativeLayout
            var tabBg: RelativeLayout = itemView.findViewById(R.id.tab_layout) as RelativeLayout
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        DialogHandler.getInstance()
            .exitDialog(this@ImageEditActivity, true) { status, message, data, alertDialog ->
                if (status != null) {
                    when (status) {
                        "0" -> alertDialog.dismiss()
                        "1" -> {
                            alertDialog.dismiss()
                            startActivity(
                                Intent(
                                    this@ImageEditActivity,
                                    HomeActivity::class.java
                                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                        }
                    }
                }
            }
    }

}
