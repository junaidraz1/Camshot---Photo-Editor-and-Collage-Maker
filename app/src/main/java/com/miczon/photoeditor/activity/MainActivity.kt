package com.miczon.photoeditor.activity



/*import com.google.firebase.auth.FirebaseAuth*/
import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.miczon.photoeditor.BaseActivity
import com.miczon.photoeditor.R
import com.miczon.photoeditor.databinding.ActivityMainBinding
import com.vorlonsoft.android.rate.AppRate
import java.io.File
import java.util.*

class MainActivity : BaseActivity(), View.OnClickListener {

    var PICK_IMAGE: Int = 111
    var CAMERA_REQUEST: Int = 123

    private lateinit var toolbar: Toolbar
    private lateinit var galleryImages: ArrayList<String>
    private lateinit var adapter: ImageAdapter
    private lateinit var binding: ActivityMainBinding

    companion object {
        var isFromSaved: Boolean = true
    }

    private fun imagesPath(): ArrayList<String> {
        val listOfAllImages = ArrayList<String>()
        var absolutePathOfImage: String? = null
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(MediaStore.MediaColumns.DATA)

        val cursor: Cursor =
            contentResolver.query(
                uri,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC"
            )!!

        val column_index_data: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data)
            listOfAllImages.add(absolutePathOfImage)
        }



        cursor.close()
        return listOfAllImages
    }

    lateinit var timer: Timer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppRate.with(this)
            .setInstallDays(2.toByte())
            .setLaunchTimes(2.toByte())
            .setRemindInterval(1.toByte())
            .setRemindLaunchesNumber(1.toByte())
            .monitor() // Monitors the app launch times
/*
        AdLoader.ads.loadFullAdFacebook(this)
        AdLoader.ads.loadfullAdAdmob(this)*/


        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                setAdapter()
            } else {
                requestPermission()
            }
        } else {
            setAdapter()
        }

        binding.btnSelect.setOnClickListener(this)
        binding.btnCollage.setOnClickListener(this)
        binding.btnCamera.setOnClickListener(this)
        binding.imgShare.setOnClickListener(this)
        binding.imgRate.setOnClickListener(this)
        binding.imgCreation.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.imgNext.setOnClickListener(this)

//        toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.logout -> {
//                // Handle settings click
//                AlertDialog.Builder(this).create().apply {
//                    setTitle("Logout")
//                    setMessage("Are you sure to logout from the application")
//                    setButton(
//                        AlertDialog.BUTTON_NEGATIVE, "Cancel"
//                    ) { dialog, _ -> dialog?.dismiss() }
//                    setButton(AlertDialog.BUTTON_POSITIVE, "Logout") { dialog, _ ->
//                        //FirebaseAuth.getInstance().signOut()
//                        dialog?.dismiss()
//                        val intent = Intent(this@MainActivity, Login_Activity::class.java)
//                        startActivity(intent)
//                    }
//                    show()
//                }
//                return true
//            }
//
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }


    private fun setAdapter() {

        galleryImages = ArrayList<String>()
        galleryImages.clear()

        galleryImages = imagesPath()
        adapter = ImageAdapter()
        binding.pagerImages.adapter = adapter

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                binding.pagerImages.post {
                    binding.pagerImages.setCurrentItem(
                        (binding.pagerImages.currentItem + 1) % galleryImages.size,
                        true
                    )
                }
            }
        }, 4000, 4000)
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ),
            100
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission", "Granted")
                setAdapter()
            } else {
                Log.e("Permission", "Denied")

                requestPermission()
            }
        }
    }

    private var mLastClickTime: Long = 0
    fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    private var mCapturedImageUri: Uri? = null

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_select -> {
                checkClick()
                var intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_PICK
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
            }

            R.id.btn_collage -> {
                checkClick()
                var intent = Intent(this, SelectImageActivity::class.java)
                startActivity(intent)
            }

            R.id.btn_camera -> {
                checkClick()
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(packageManager) != null) {
                    var photofile: File? = null

                    try {
                        val capturedPath = "image_" + System.currentTimeMillis() + ".jpg"
                        photofile = File(
                            Environment.getExternalStorageDirectory().absolutePath + "/DCIM",
                            capturedPath
                        )
                        photofile.parentFile.mkdirs()
                        mCapturedImageUri = Uri.fromFile(photofile)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    if (photofile != null) {
                        mCapturedImageUri = FileProvider.getUriForFile(
                            this,
                            "$packageName.provider",
                            photofile
                        )
                    }
                }

                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageUri)
                startActivityForResult(intent, CAMERA_REQUEST)
            }

            R.id.img_share -> {
                checkClick()
                try {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "text/plain"
                    i.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
                    val shareMessage = getString(R.string.txt_share)
                    i.putExtra(Intent.EXTRA_TEXT, shareMessage)
                    startActivity(Intent.createChooser(i, "Share App Via"))
                } catch (e: Exception) {
                    //e.toString();
                }
            }

            R.id.img_rate -> {
                checkClick()
                try {
                    val uri = Uri.parse("market://details?id=$packageName")
                    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    startActivity(goToMarket)
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                        )
                    )
                }
            }

            R.id.img_creation -> {
                checkClick()
                val intent = Intent(this, MyCreationActivity::class.java)
                startActivity(intent)
            }

            R.id.img_back -> {
                checkClick()
                timer.cancel()
                if (binding.pagerImages.currentItem <= 0) {
                    binding.pagerImages.setCurrentItem(
                        galleryImages.size,
                        true
                    )
                } else {
                    binding.pagerImages.setCurrentItem(
                        (binding.pagerImages.currentItem - 1) % galleryImages.size,
                        true
                    )
                }
            }

            R.id.img_next -> {
                checkClick()
                timer.cancel()

                binding.pagerImages.setCurrentItem(
                    (binding.pagerImages.currentItem + 1) % galleryImages.size,
                    true
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            if (data != null) {
                try {
                    val uri: Uri = data.data!!

                    val intent = Intent(this, ImageEditActivity::class.java)
                    intent.putExtra("image_uri", uri.toString())
                    startActivity(intent)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST) {
            if (mCapturedImageUri != null) {
                var intent = Intent(this, ImageEditActivity::class.java)
                intent.putExtra("image_uri", mCapturedImageUri.toString())
                startActivity(intent)
            }
        }
    }

    inner class ImageAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var view: View =
                LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.item_slider, container, false)
            var img_slider: ImageView = view.findViewById(R.id.img_slider) as ImageView

            /*     Picasso.with(this@MainActivity)
                     .load("http://i.imgur.com/DvpvklR.png")
                     .fit()
                     .into(img_slider)
     */

            Glide.with(this@MainActivity)
                .asBitmap()
                .apply(RequestOptions.circleCropTransform())
                .load(galleryImages[position])
                .into(img_slider)

            (container as ViewPager).addView(view)

            img_slider.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    checkClick()
                    var uri = Uri.fromFile(File(galleryImages[position]))
                    var intent = Intent(this@MainActivity, ImageEditActivity::class.java)
                    intent.putExtra("image_uri", uri.toString())
                    startActivity(intent)
                }
            })
            return view
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return (view == `object` as View)
        }

        override fun getCount(): Int {
            return galleryImages.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            (container as ViewPager).removeView(`object` as View)
        }
    }

    override fun onBackPressed() {
        var intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}
