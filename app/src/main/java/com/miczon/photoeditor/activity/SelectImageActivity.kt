package com.miczon.photoeditor.activity


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.miczon.photoeditor.adapter.SelectedPhotoAdapter
import com.miczon.photoeditor.BaseActivity
import com.miczon.photoeditor.fragments.GalleryAlbumFragment
import com.miczon.photoeditor.fragments.GalleryAlbumImageFragment
import com.miczon.photoeditor.R
import com.miczon.photoeditor.databinding.ActivitySelectImageBinding
import java.io.File

class SelectImageActivity : BaseActivity(), GalleryAlbumImageFragment.OnSelectImageListener,
    SelectedPhotoAdapter.OnDeleteButtonClickListener {

    private lateinit var binding: ActivitySelectImageBinding
    var TAG = "SelectImageActivity"

    override fun onDeleteButtonClick(str: String) {

        mSelectedImages.remove(str)
        mSelectedPhotoAdapter.notifyDataSetChanged()
        val textView = binding.textImgcount
        /* val str2 = getString(R.string.select_photos_label)*/
        val sb = StringBuilder()
        sb.append("(")
        sb.append(this.mSelectedImages.size)
        sb.append(")")
        textView.text = sb.toString()
    }

    private val mSelectedImages = ArrayList<String>()
    private var maxIamgeCount = 10
    private lateinit var mSelectedPhotoAdapter: SelectedPhotoAdapter
    private var mLastClickTime: Long = 0

    private fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }


    override fun onSelectImage(str: String) {
        if (str != null) {
            if (this.mSelectedImages.size == this.maxIamgeCount) {
                Toast.makeText(
                    this,
                    String.format(getString(R.string.photos_warning_lbl), maxIamgeCount),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                var uri = Uri.fromFile(File(str))
                this.mSelectedImages.add(str)
                this.mSelectedPhotoAdapter.notifyDataSetChanged()
                val textView = binding.textImgcount
                /*val str2 = "Select upto 10 photo(s)"*/
                val sb = StringBuilder()
                sb.append("(")
                sb.append(this.mSelectedImages.size)
                sb.append(")")
                textView.text = sb.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mSelectedPhotoAdapter = SelectedPhotoAdapter(mSelectedImages, this)

        binding.listImages.hasFixedSize()
        binding.listImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listImages.adapter = mSelectedPhotoAdapter

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, GalleryAlbumFragment()).commit()

        binding.btnNext.setOnClickListener {
            checkClick()
            createCollage()
        }
    }

    private fun createCollage() {
        if (mSelectedImages.size == 0) {
            Toast.makeText(this, getString(R.string.collage_error_msg), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            Log.e(TAG, "createCollage: selected image size: " + mSelectedImages.size)
            val intent = Intent(this, CollageActivity::class.java)
            intent.putExtra("imageCount", mSelectedImages.size)
            intent.putExtra("selectedImages", mSelectedImages)
            intent.putExtra("imagesinTemplate", mSelectedImages.size)

            startActivityForResult(intent, 111)

        } catch (e: Exception) {
            Log.e(TAG, "createCollage: exception: " + e.localizedMessage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == 111) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
