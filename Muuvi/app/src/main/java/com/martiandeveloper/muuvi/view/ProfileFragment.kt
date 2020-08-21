package com.martiandeveloper.muuvi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.martiandeveloper.muuvi.R
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        checkFacebookProfileImage()
        setToolbar()
        getUsername()
    }

    private fun setToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(activity_rate_mainMT)
        (activity as AppCompatActivity).title = ""
    }

    private fun checkFacebookProfileImage() {
        auth.currentUser?.uid?.let {
            db.collection("users").document(it).get().addOnCompleteListener { it2 ->
                if (it2.isSuccessful) {
                    if (it2.result?.get("facebookId") != null) {
                        Glide.with(this)
                            .load("https://graph.facebook.com/${it2.result?.get("facebookId")}/picture?type=normal")
                            .placeholder(R.drawable.logo1)
                            .centerCrop()
                            .into(fragment_profile_userImageIV)
                    } else {
                        checkProfileImage()
                    }
                }
            }
        }
    }

    private fun checkProfileImage() {
        val storageRef = FirebaseStorage.getInstance().reference
        val filepath: StorageReference =
            storageRef.child("profile_images").child(auth.currentUser?.uid!!)

        filepath.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Glide.with(this)
                    .load(it.result.toString())
                    .placeholder(R.drawable.logo1)
                    .centerCrop()
                    .into(fragment_profile_userImageIV)
            }
        }
    }

    private fun getUsername() {
        db.collection("users").document(auth.currentUser?.uid!!).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.getString("username") != null) {
                    (activity as AppCompatActivity).title = it.result?.getString("username")
                }
            }
        }
    }

}