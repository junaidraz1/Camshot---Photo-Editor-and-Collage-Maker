package com.miczon.photoeditor.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.miczon.photoeditor.activity.HomeActivity
import com.miczon.photoeditor.adapter.GalleryAlbumAdapter
import com.miczon.photoeditor.adapter.GalleryAlbumRecyclerAdapter
import com.miczon.photoeditor.model.GalleryAlbum
import com.miczon.photoeditor.R
import com.miczon.photoeditor.databinding.FragmentGalleryAlbumBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class GalleryAlbumFragment() : Fragment() {

    lateinit var mAdapter: GalleryAlbumRecyclerAdapter
    private lateinit var binding: FragmentGalleryAlbumBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentGalleryAlbumBinding.inflate(inflater)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.VISIBLE
                }
                val loadPhotoAlbums = async {
                    loadPhotoAlbums()
                }
                val arrayList = loadPhotoAlbums.await()
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    mAdapter = GalleryAlbumRecyclerAdapter(
                        requireContext(),
                        arrayList,
                        object : GalleryAlbumAdapter.OnGalleryAlbumClickListener {
                            override fun onGalleryAlbumClick(galleryAlbum: GalleryAlbum?) {
                                val bundle = Bundle()
                                bundle.putStringArrayList(
                                    GalleryAlbumImageFragment.ALBUM_IMAGE_EXTRA,
                                    galleryAlbum!!.mImageList
                                )
                                bundle.putString(
                                    GalleryAlbumImageFragment.ALBUM_NAME_EXTRA,
                                    galleryAlbum.mAlbumName
                                )

                                val galleryAlbumImageFragment = GalleryAlbumImageFragment()
                                galleryAlbumImageFragment.arguments = bundle

                                val fragmentTransaction =
                                    activity!!.supportFragmentManager.beginTransaction()
                                fragmentTransaction.replace(
                                    R.id.frame_container,
                                    galleryAlbumImageFragment
                                )

                                fragmentTransaction.addToBackStack(null)
                                fragmentTransaction.commitAllowingStateLoss()
                            }
                        })

                    binding.listView.layoutManager =
                        GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
                    binding.listView.adapter = mAdapter
                }
            }
        }

        binding.rlBack.setOnClickListener {
            startActivity(
                Intent(requireActivity(), HomeActivity::class.java).setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            )
        }

        return binding.root
    }

    private fun loadPhotoAlbums(): ArrayList<GalleryAlbum> {

        val r0 = LinkedHashMap<Long, GalleryAlbum>()
        val r4: Array<String> =
            arrayOf("_id", "_data", "bucket_id", "bucket_display_name", "datetaken")
        val r3: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val arrayList: ArrayList<GalleryAlbum> = java.util.ArrayList<GalleryAlbum>()

        requireContext().contentResolver.query(r3, r4, null, null, "date_added DESC")
            .use { cursor ->
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        try {
                            do {
                                val r1: String =
                                    cursor.getString(cursor.getColumnIndexOrThrow("bucket_display_name"))
                                val r2: Long =
                                    cursor.getLong(cursor.getColumnIndexOrThrow("datetaken"))
                                val r5: String =
                                    cursor.getString(cursor.getColumnIndexOrThrow("_data"))
                                val r6: Long =
                                    cursor.getLong(cursor.getColumnIndexOrThrow("bucket_id"))

                                var r8: GalleryAlbum? = r0[r6]

                                if (r8 == null) {
                                    r8 = GalleryAlbum(r6, r1)
                                    r8.mTakenDate = SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss",
                                        Locale.getDefault()
                                    ).format(r2)
                                    r8.mImageList.add(r5)
                                    r0[r6] = r8
                                } else {
                                    r8.mImageList.add(r5)
                                }

                            } while (cursor.moveToNext())
                            arrayList.addAll(r0.values)
                        } catch (ex: RuntimeException) {
                            ex.printStackTrace()
                        }
                    }
                }
            }

        return arrayList
    }

}
