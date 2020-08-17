package com.martiandeveloper.muuvi.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.martiandeveloper.muuvi.R
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.fragment_sign_up_user_image.*
import java.io.File
import java.util.*

class SignUpUserImageFragment : Fragment(), View.OnClickListener {

    private var fileName = ""
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var buttonIsNext = false
    private var imagePath: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up_user_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        hideProgress()
        setListeners()
        setTextViewToggle(fragment_sign_up_user_image_skipMTV)
    }

    private fun setListeners() {
        fragment_sign_up_user_image_addAPhotoMBTN.setOnClickListener(this)
        fragment_sign_up_user_image_skipMTV.setOnClickListener(this)
        fragment_sign_up_user_image_userImageIV.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.fragment_sign_up_user_image_addAPhotoMBTN -> {
                    if (buttonIsNext) {
                        if (imagePath != null) {
                            saveProfilePicture()
                        } else {
                            goToFeed()
                        }
                    } else {
                        openChangeProfilePhotoDialog(v)
                    }
                }
                R.id.fragment_sign_up_user_image_skipMTV -> goToFeed()
                R.id.fragment_sign_up_user_image_userImageIV -> openChangeProfilePhotoDialog(v)
            }
        }
    }

    private fun goToFeed() {
        val intent = Intent(activity, FeedActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun openChangeProfilePhotoDialog(v: View?) {
        val dialog = AlertDialog.Builder(v?.context).create()
        val view = layoutInflater.inflate(R.layout.layout_change_profile_photo, null)

        val layoutChangeProfilePhotoImportFromFacebookMTV =
            view.findViewById<MaterialTextView>(R.id.layout_change_profile_photo_importFromFacebookMTV)
        val layoutChangeProfilePhotoTakePhotoMTV =
            view.findViewById<MaterialTextView>(R.id.layout_change_profile_photo_takePhotoMTV)
        val layoutChangeProfilePhotoChooseFromLibraryMTV =
            view.findViewById<MaterialTextView>(R.id.layout_change_profile_photo_chooseFromLibraryMTV)

        layoutChangeProfilePhotoImportFromFacebookMTV.setOnClickListener {
            dialog.dismiss()
            importFromFacebook()
        }

        layoutChangeProfilePhotoTakePhotoMTV.setOnClickListener {
            dialog.dismiss()
            openCamera()
        }

        layoutChangeProfilePhotoChooseFromLibraryMTV.setOnClickListener {
            dialog.dismiss()
            openGallery()
        }

        dialog.setView(view)
        dialog.show()
    }

    private fun importFromFacebook() {
        if (isInternetAvailable(requireContext())) {
            showProgress()

            val user = auth.currentUser

            if (user != null) {

                db.collection("users").document(user.uid).get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result != null) {
                            val facebookId = it.result!!.getString("facebookId")
                            if (facebookId != null) {
                                hideProgress()
                                Glide.with(this)
                                    .load("https://graph.facebook.com/$facebookId/picture?type=normal")
                                    .into(fragment_sign_up_user_image_userImageIV)
                                buttonIsNext = true
                                fragment_sign_up_user_image_addAPhotoMBTN.text =
                                    resources.getString(R.string.next)
                            } else {
                                hideProgress()
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.couldnt_get_profile_picture_from_facebook),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            hideProgress()
                            Toast.makeText(
                                context,
                                resources.getString(R.string.couldnt_get_profile_picture_from_facebook),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        hideProgress()
                        Toast.makeText(
                            context,
                            resources.getString(R.string.couldnt_get_profile_picture_from_facebook),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                hideProgress()
                Toast.makeText(
                    context,
                    resources.getString(R.string.something_went_wrong_please_try_again_later),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            openNoInternetDialog(view)
        }
    }

    private fun showProgress() {
        fragment_sign_up_user_image_mainPB.visibility = View.VISIBLE
        fragment_sign_up_user_image_addAPhotoMBTN.isClickable = false
        fragment_sign_up_user_image_skipMTV.isClickable = false
        fragment_sing_up_user_image_mainLL.alpha = .5F
    }

    private fun hideProgress() {
        fragment_sign_up_user_image_mainPB.visibility = View.GONE
        fragment_sign_up_user_image_addAPhotoMBTN.isClickable = true
        fragment_sign_up_user_image_skipMTV.isClickable = true
        fragment_sing_up_user_image_mainLL.alpha = 1F
    }

    @Suppress("DEPRECATION")
    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }

    private fun openNoInternetDialog(v: View?) {
        val dialogNoInternet = AlertDialog.Builder(v?.context).create()
        val view = layoutInflater.inflate(R.layout.layout_no_internet, null)

        val layoutNoInternetDismissMTV =
            view.findViewById<MaterialTextView>(R.id.layout_no_internet_dismissMTV)

        layoutNoInternetDismissMTV.setOnClickListener {
            dialogNoInternet.dismiss()
        }

        dialogNoInternet.setView(view)
        dialogNoInternet.setCanceledOnTouchOutside(false)
        dialogNoInternet.show()
    }

    private fun openCamera() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                )
            } != PackageManager.PERMISSION_GRANTED
            || context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
            || context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                201
            )
        } else {
            fileName = System.currentTimeMillis().toString() + ".jpg"
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName))
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, 202)
            }
        }
    }

    private fun getCacheImagePath(fileName: String): Uri {
        val path = File(requireActivity().externalCacheDir, "camera")
        if (!path.exists()) path.mkdirs()
        val image = File(path, fileName)
        return FileProvider.getUriForFile(
            requireContext(),
            requireActivity().packageName + ".provider",
            image
        )
    }

    private fun setTextViewToggle(textView: MaterialTextView) {
        textView.setTextColor(
            context?.let {
                ContextCompat.getColorStateList(it, R.color.facebook_log_in_text_selector)
            }
        )
    }

    private fun openGallery() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 102)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 102)
            }
        } else if (requestCode == 201) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fileName = System.currentTimeMillis().toString() + ".jpg"
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName))
                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivityForResult(intent, 202)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 102 && resultCode == Activity.RESULT_OK && data != null) {
            startCrop(data.data)
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK && data != null) {
            handleCropResult(data)
        } else if (requestCode == UCrop.RESULT_ERROR) {
            handleCropError(data)
        } else if (requestCode == 202 && resultCode == Activity.RESULT_OK) {
            startCrop(getCacheImagePath(fileName))
        }
    }

    private fun startCrop(imageUri: Uri?) {
        val destinationUri = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()
        imageUri?.let {
            UCrop.of(it, Uri.fromFile(File(context?.cacheDir, destinationUri))).start(
                context as Activity
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun handleCropResult(data: Intent?) {
        val resultUri = data?.let { UCrop.getOutput(it) }
        try {
            if (resultUri != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source =
                        ImageDecoder.createSource(requireActivity().contentResolver, resultUri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    fragment_sign_up_user_image_userImageIV.setImageBitmap(bitmap)
                    imagePath = resultUri
                    buttonIsNext = true
                    fragment_sign_up_user_image_addAPhotoMBTN.text =
                        resources.getString(R.string.next)
                } else {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver,
                        resultUri
                    )
                    fragment_sign_up_user_image_userImageIV.setImageBitmap(bitmap)
                    imagePath = resultUri
                    buttonIsNext = true
                    fragment_sign_up_user_image_addAPhotoMBTN.text =
                        resources.getString(R.string.next)
                }
            } else {
                Toast.makeText(
                    context,
                    resources.getString(R.string.cannot_retrieve_cropped_image),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleCropError(data: Intent?) {
        val cropError = data?.let { UCrop.getError(it) }
        if (cropError != null) {
            Toast.makeText(context, "Error: ${cropError.message}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error: Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfilePicture() {
        showProgress()
        val user = auth.currentUser
        if (user != null) {
            if (imagePath != null) {
                val reference = FirebaseStorage.getInstance().reference
                reference
                    .child("profile_images")
                    .child(user.uid)
                    .putFile(imagePath!!).addOnCompleteListener {
                        if (it.isSuccessful) {
                            hideProgress()
                            goToFeed()
                        } else {
                            hideProgress()
                            Toast.makeText(
                                context,
                                resources.getString(R.string.couldnt_save_profile_picture),
                                Toast.LENGTH_SHORT
                            ).show()
                            goToFeed()
                        }
                    }
            } else {
                hideProgress()
                Toast.makeText(
                    context,
                    resources.getString(R.string.couldnt_save_profile_picture),
                    Toast.LENGTH_SHORT
                ).show()
                goToFeed()
            }
        } else {
            hideProgress()
            Toast.makeText(
                context,
                resources.getString(R.string.something_went_wrong_please_try_again_later),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
