package com.martiandeveloper.muuvi.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.martiandeveloper.muuvi.R
import kotlinx.android.synthetic.main.fragment_sign_up.*

class SignUpFragment : Fragment(), View.OnClickListener {

    private val callbackManager = CallbackManager.Factory.create()
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setProgress(1F, true, View.GONE)
        setTextViewToggle(fragment_sign_up_signUpWithEmailOrPhoneNumberMTV)
        setListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun setTextViewToggle(textView: MaterialTextView) {
        textView.setTextColor(
            context?.let {
                ContextCompat.getColorStateList(it, R.color.facebook_log_in_text_selector)
            }
        )
    }

    private fun setProgress(alpha: Float, enable: Boolean, visible: Int) {
        fragment_sign_up_mainIV.alpha = alpha
        fragment_sign_up_mainLL.alpha = alpha
        fragment_sign_up_bottomLL.alpha = alpha
        fragment_sign_up_facebookMCV.isEnabled = enable
        fragment_sign_up_signUpWithEmailOrPhoneNumberMTV.isEnabled = enable
        fragment_sign_up_bottomLL.isEnabled = enable
        fragment_sign_up_mainPB.visibility = visible
    }

    private fun setListeners() {
        fragment_sign_up_facebookMCV.setOnClickListener(this)
        fragment_sign_up_signUpWithEmailOrPhoneNumberMTV.setOnClickListener(this)
        fragment_sign_up_bottomLL.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.fragment_sign_up_facebookMCV -> openFacebookLogIn()
                R.id.fragment_sign_up_signUpWithEmailOrPhoneNumberMTV -> navigate(
                    SignUpFragmentDirections.actionSignUpFragmentToSignUpWithPhoneOrEmailFragment()
                )
                R.id.fragment_sign_up_bottomLL -> navigate(SignUpFragmentDirections.actionSignUpFragmentToLoginFragment())
            }
        }
    }

    private fun openFacebookLogIn() {
        if (isInternetAvailable()) {
            setProgress(.5F, false, View.VISIBLE)
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("email", "public_profile"))
            LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        handleFacebookAccessToken(loginResult.accessToken)
                    }

                    override fun onCancel() {
                        setProgress(1F, true, View.GONE)
                    }

                    override fun onError(error: FacebookException) {
                        setProgress(1F, true, View.GONE)
                        setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                    }
                })
        } else {
            openNoInternetDialog()
        }
    }

    @Suppress("DEPRECATION")
    private fun isInternetAvailable(): Boolean {
        var result = false
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    private fun openNoInternetDialog() {
        val dialogNoInternet = AlertDialog.Builder(requireContext()).create()
        val view = layoutInflater.inflate(R.layout.layout_no_internet, null)
        val layoutNoInternetDismissMTV =
            view.findViewById<MaterialTextView>(R.id.layout_no_internet_dismissMTV)
        setTextViewToggle(layoutNoInternetDismissMTV)
        layoutNoInternetDismissMTV.setOnClickListener {
            dialogNoInternet.dismiss()
        }
        dialogNoInternet.setView(view)
        dialogNoInternet.setCanceledOnTouchOutside(false)
        dialogNoInternet.show()
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                checkUser(auth.currentUser, token)
            } else {
                setProgress(1F, true, View.GONE)
                setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
            }
        }
    }

    private fun setToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun checkUser(user: FirebaseUser?, token: AccessToken) {
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result != null) {
                        if (it.result!!.exists()) {
                            setProgress(1F, true, View.GONE)
                            goToFeed()
                        } else {
                            getUserProfileImage(user, token)
                        }
                    } else {
                        setProgress(1F, true, View.GONE)
                        setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
                    }
                } else {
                    setProgress(1F, true, View.GONE)
                    setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
                }
            }
        }
    }

    private fun goToFeed() {
        val intent = Intent(activity, FeedActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun getUserProfileImage(user: FirebaseUser?, token: AccessToken) {
        val request = GraphRequest.newMeRequest(token) { `object`, response ->
            try {
                if (response != null) {
                    val facebookId = `object`.getString("id")
                    if (user != null) {
                        saveUserData(user, facebookId)
                    } else {
                        setProgress(1F, true, View.GONE)
                        setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                    }
                } else {
                    setProgress(1F, true, View.GONE)
                    setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                }
            } catch (e: Exception) {
                setProgress(1F, true, View.GONE)
                setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "id")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun saveUserData(user: FirebaseUser, facebookId: String) {
        val userMap = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "username" to "",
            "facebookId" to facebookId
        )

        db.collection("users").document(user.uid).set(userMap).addOnCompleteListener {
            setProgress(.1F, true, View.GONE)
            if (it.isSuccessful) {
                navigate(SignUpFragmentDirections.actionSignUpFragmentToSignUpUsernameFragment())
            } else {
                setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
            }
        }
    }

    private fun navigate(action: NavDirections) {
        view?.let { Navigation.findNavController(it).navigate(action) }
    }
}
