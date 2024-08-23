package com.miczon.photoeditor.activity


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.ads.LoadAdError
import com.miczon.photoeditor.BaseActivity
import com.miczon.photoeditor.R
import com.miczon.photoeditor.eventListeners.BannerAdLoadListener
import com.miczon.photoeditor.helper.BannerAdManager
import com.miczon.photoeditor.helper.DialogHandler
import com.miczon.photoeditor.utils.Utility
import java.io.File

class ShowImageActivity : BaseActivity(), View.OnClickListener, BannerAdLoadListener {

    var TAG = "ShowImageActivity"

    private var imageUri: String? = null
    private var from: String? = null
    private var imgShow: ImageView? = null
    private var savedFile: File? = null
    private var density: Float = 0.toFloat()
    private var dHeight: Int = 0
    private var dWidth: Int = 0
    private var display: DisplayMetrics? = null
    private lateinit var bannerAdManager: BannerAdManager

    /*private var mInterstitialAd: InterstitialAd? = null*/
    private var mLastClickTime: Long = 0
    private fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_show_image)

        bannerAdManager = BannerAdManager(this, this)

        imgShow = findViewById<View>(R.id.img_show) as ImageView

        if (!Utility.getInstance().isPremiumActive(this@ShowImageActivity)) {
            findViewById<View>(R.id.rl_adContainer).visibility = View.VISIBLE
            findViewById<View>(R.id.fl_adContainer).visibility = View.VISIBLE
            bannerAdManager.loadBannerAd(findViewById(R.id.fl_adContainer))
        } else {
            findViewById<View>(R.id.rl_adContainer).visibility = View.GONE
            findViewById<View>(R.id.fl_adContainer).visibility = View.GONE
        }

        imageUri = intent.getStringExtra("image_uri")
        from = intent.getStringExtra("from")

        if (from?.equals("saved") == true || from?.equals("RemoveBackgroundActivity") == true) {
            findViewById<View>(R.id.rl_back).visibility = View.VISIBLE
        } else if (from?.equals("ApplyEffectActivity", ignoreCase = true) == true ||
            from?.equals("IphoneCameraActivity", ignoreCase = true) == true ||
            from?.equals("SamsungCameraActivity", ignoreCase = true) == true ||
            from?.equals("NikonCameraActivity", ignoreCase = true) == true ||
            from?.equals("SonyCameraActivity", ignoreCase = true) == true
        ) {
            findViewById<View>(R.id.rl_back).visibility = View.VISIBLE
            Glide.with(this@ShowImageActivity)
                .load(imageUri)
                .into(imgShow!!)
        }

        Log.e(TAG, "shareImageSocialApp: saved image uri: $imageUri")

        savedFile = File(imageUri!!)

        display = resources.displayMetrics
        density = resources.displayMetrics.density
        dWidth = display!!.widthPixels
        dHeight = (display!!.heightPixels.toFloat() - density * 150.0f).toInt()

        /* val layoutParams = RelativeLayout.LayoutParams(D_width, RelativeLayout.LayoutParams.WRAP_CONTENT)
         layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
         img_show!!.layoutParams = layoutParams*/

        imgShow!!.setImageURI(Uri.parse(imageUri))

        findViewById<View>(R.id.whatsapp_share).setOnClickListener(this)
        findViewById<View>(R.id.facebook_share).setOnClickListener(this)
        findViewById<View>(R.id.instagram_share).setOnClickListener(this)
        findViewById<View>(R.id.share_more).setOnClickListener(this)
        findViewById<View>(R.id.img_folder).setOnClickListener(this)
        findViewById<View>(R.id.rl_back).setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.whatsapp_share -> {
                checkClick()
                DialogHandler.getInstance().shareImagePermissionDialog(
                    this, getString(R.string.share_whatsapp), true
                ) { status, _, _, alertDialog ->
                    if (status.equals("0", ignoreCase = true)) {
                        alertDialog.dismiss()
                    } else {
                        alertDialog.dismiss()
                        shareImageSocialApp(imageUri!!, "com.instagram.android", "Whatsapp")
                    }
                }
            }

            R.id.instagram_share -> {
                checkClick()
                DialogHandler.getInstance().shareImagePermissionDialog(
                    this, getString(R.string.share_instagram), true
                ) { status, _, _, alertDialog ->
                    if (status.equals("0", ignoreCase = true)) {
                        alertDialog.dismiss()
                    } else {
                        alertDialog.dismiss()
                        shareImageSocialApp(imageUri!!, "com.instagram.android", "Instagram")
                    }
                }
            }

            R.id.facebook_share -> {
                checkClick()
                DialogHandler.getInstance().shareImagePermissionDialog(
                    this, getString(R.string.share_facebook), true
                ) { status, _, _, alertDialog ->
                    if (status.equals("0", ignoreCase = true)) {
                        alertDialog.dismiss()
                    } else {
                        alertDialog.dismiss()
                        shareImageSocialApp(imageUri!!, "com.facebook.katana", "Facebook")
                    }
                }
            }

            R.id.share_more -> {
                checkClick()
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUri))
                shareIntent.type = "image/*"
                shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startActivity(shareIntent)
                } else {
                    startActivity(
                        Intent.createChooser(
                            shareIntent,
                            getString(R.string.share_header)
                        )
                    )
                }
            }

            R.id.img_folder -> {
                checkClick()
                var intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.rl_back -> {
                checkClick()
                onBackPressed()
            }
        }
    }

    private fun shareImageSocialApp(savedImagePath: String, packageName: String, from: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(savedImagePath))
        intent.setPackage(packageName)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Not installed or no handler for the intent
            if (from.equals("instagram", ignoreCase = true)) {
                Toast.makeText(this@ShowImageActivity, R.string.insta_msg, Toast.LENGTH_SHORT)
                    .show()
            } else if (from.equals("whatsapp", ignoreCase = true)) {
                Toast.makeText(this@ShowImageActivity, R.string.whatsapp_msg, Toast.LENGTH_SHORT)
                    .show()
            } else if (from.equals("facebook", ignoreCase = true)) {
                Toast.makeText(this@ShowImageActivity, R.string.facebook_msg, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /*fun loadfullAdAdmob(context: Context){

        mInterstitialAd = com.google.android.gms.ads.InterstitialAd(context)
        mInterstitialAd!!.adUnitId = context.getString(R.string.Admob_adUnitId)
        mInterstitialAd!!.loadAd(AdRequest.Builder().build())

    }*/

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (from?.equals("saved") == true) {
            val intent = Intent(this, MyCreationActivity::class.java)
            startActivity(intent)
            finish()

        } else if (from?.equals("ApplyEffectActivity", ignoreCase = true) == true) {
            val intent = Intent(this, CartoonFiltersActivity::class.java)
            startActivity(intent)
            finish()

        } else if (from?.equals("RemoveBackgroundActivity", ignoreCase = true) == true) {
            val intent = Intent(this, RemoveBackgroundActivity::class.java)
            startActivity(intent)
            finish()

        } else if (from?.equals("IphoneCameraActivity", ignoreCase = true) == true) {
            val intent = Intent(this, IphoneCameraActivity::class.java)
            startActivity(intent)
            finish()

        } else if (from?.equals("SamsungCameraActivity", ignoreCase = true) == true) {
            val intent = Intent(this, SamsungCameraActivity::class.java)
            startActivity(intent)
            finish()

        } else if (from?.equals("NikonCameraActivity", ignoreCase = true) == true) {
            val intent = Intent(this, NikonCameraActivity::class.java)
            startActivity(intent)
            finish()

        } else if (from?.equals("SonyCameraActivity", ignoreCase = true) == true) {
            val intent = Intent(this, SonyCameraActivity::class.java)
            startActivity(intent)
            finish()

        } else if (from?.equals("FilterCollageActivity", ignoreCase = true) == true) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onAdClicked() {
        TODO("Not yet implemented")
    }

    override fun onAdLoaded() {
        TODO("Not yet implemented")
        findViewById<View>(R.id.tv_loadingAd).visibility = View.GONE
    }

    override fun onAdFailedToLoad(loadAdError: LoadAdError?) {
        TODO("Not yet implemented")
    }

    /*fun showInterstitial(context: Context) {
        Log.e("FunCalled", "From ShowInterstitial")

        if (mInterstitialAd!!.isLoaded) {
            Log.e("Google in show", "From ShowInterstitial")
            mInterstitialAd!!.show()
        }
        loadfullAdAdmob(context)
    }*/


}