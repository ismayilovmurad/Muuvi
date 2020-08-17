package com.martiandeveloper.muuvi.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import com.martiandeveloper.muuvi.databinding.FragmentLogInHelpBinding
import com.martiandeveloper.muuvi.viewmodel.LogInHelpViewModel
import kotlinx.android.synthetic.main.fragment_log_in_help.*

class LogInHelpFragment : Fragment(), View.OnClickListener {

    private val callbackManager = CallbackManager.Factory.create()

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var vm: LogInHelpViewModel

    private var usernameEmailPhoneACTContent: String? = null

    private lateinit var binding: FragmentLogInHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = activity?.run {
            ViewModelProviders.of(this)[LogInHelpViewModel::class.java]
        } ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_log_in_help, container, false)
        binding.logInHelpViewModel = vm
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setProgress(1F, true, View.GONE)
        setTextViewToggle(fragment_log_in_help_logInWithFacebookMTV)
        setMBTN(.5F, false)
        setListeners()
        observeEditTextContent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun setProgress(alpha: Float, enable: Boolean, visible: Int) {
        fragment_log_in_help_mainLL.alpha = alpha
        fragment_log_in_help_facebookLL.alpha = alpha
        fragment_log_in_help_usernameEmailPhoneACT.isEnabled = enable
        fragment_log_in_help_nextMBTN.isEnabled = enable
        fragment_log_in_help_logInWithFacebookMTV.isEnabled = enable
        fragment_log_in_help_mainPB.visibility = visible
    }

    private fun setTextViewToggle(textView: MaterialTextView) {
        textView.setTextColor(
            context?.let {
                ContextCompat.getColorStateList(it, R.color.facebook_log_in_text_selector)
            }
        )
    }

    private fun setMBTN(alpha: Float, enable: Boolean) {
        fragment_log_in_help_nextMBTN.alpha = alpha
        fragment_log_in_help_nextMBTN.isEnabled = enable
    }

    private fun setListeners() {
        fragment_log_in_help_logInWithFacebookMTV.setOnClickListener(this)
        fragment_log_in_help_nextMBTN.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.fragment_log_in_help_logInWithFacebookMTV -> openFacebookLogIn()
                R.id.fragment_log_in_help_nextMBTN -> checkLogIn()
            }
        }
    }

    private fun openFacebookLogIn() {
        if (isInternetAvailable()) {
            setProgress(.5F, false, View.VISIBLE)
            fragment_log_in_help_usernameEmailPhoneACT.clearFocus()
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
                        setToast(
                            resources.getString(R.string.couldnt_log_in_please_try_again_later)
                        )
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
                setToast(
                    resources.getString(R.string.couldnt_log_in_please_try_again_later)
                )
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
                        setToast(
                            resources.getString(R.string.something_went_wrong_please_try_again_later)
                        )
                    }
                } else {
                    setProgress(1F, true, View.GONE)
                    setToast(
                        resources.getString(R.string.something_went_wrong_please_try_again_later)
                    )
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
                        setToast(
                            resources.getString(R.string.couldnt_log_in_please_try_again_later)
                        )
                    }
                } else {
                    setProgress(1F, true, View.GONE)
                    setToast(
                        resources.getString(R.string.couldnt_log_in_please_try_again_later)
                    )
                }
            } catch (e: Exception) {
                setProgress(1F, true, View.GONE)
                setToast(
                    resources.getString(R.string.couldnt_log_in_please_try_again_later)
                )
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
                navigate(LogInHelpFragmentDirections.actionLogInHelpFragmentToSignUpUsernameFragment())
            } else {
                setToast(
                    resources.getString(R.string.couldnt_log_in_please_try_again_later)
                )
            }
        }
    }

    private fun navigate(action: NavDirections) {
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

    private fun observeEditTextContent() {
        vm.usernameEmailPhoneACTContent.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                setMBTN(1F, true)
            } else {
                setMBTN(.5F, false)
            }
        })
    }

    private fun checkLogIn() {
        usernameEmailPhoneACTContent = vm.usernameEmailPhoneACTContent.value
        when {
            Patterns.EMAIL_ADDRESS.matcher(usernameEmailPhoneACTContent!!)
                .matches() -> {
                sendResetPassword()
            }
            usernameEmailPhoneACTContent!![0].isLetter() -> {
                checkUsername(usernameEmailPhoneACTContent!!)
            }
        }
    }

    private fun checkUsername(username: String) {
        if (isInternetAvailable()) {
            setProgress(.5F, false, View.VISIBLE)
            val query = db.collection("users").whereEqualTo(
                "username",
                username
            )
            query.get().addOnCompleteListener {
                setProgress(1F, true, View.GONE)
                if (it.isSuccessful) {
                    if (it.result != null) {
                        if (it.result!!.size() == 0) {
                            setToast(
                                resources.getString(R.string.theres_no_user_linked_to_this_username_try_to_sign_up_instead)
                            )
                        } else {
                            for (i in it.result!!) {
                                if (i.getString("username") == username) {
                                    if (i.getString("facebookId") == null) {
                                        when {
                                            i.getString("phoneNumber") != null -> {
                                                usernameEmailPhoneACTContent =
                                                    i.getString("phoneNumber")
                                                var text = ""
                                                for (j in usernameEmailPhoneACTContent!!.indices) {
                                                    when (j) {
                                                        0 -> {
                                                            text += usernameEmailPhoneACTContent!![j]
                                                        }
                                                        1 -> {
                                                            text += usernameEmailPhoneACTContent!![j]
                                                        }
                                                        2 -> {
                                                            text += usernameEmailPhoneACTContent!![j]
                                                        }
                                                        3 -> {
                                                            text += usernameEmailPhoneACTContent!![j]
                                                        }
                                                        4 -> {
                                                            text += usernameEmailPhoneACTContent!![j]
                                                        }
                                                        5 -> {
                                                            text += usernameEmailPhoneACTContent!![j]
                                                        }
                                                        6 -> {
                                                            text += usernameEmailPhoneACTContent!![j]
                                                        }
                                                        7 -> {
                                                            text += usernameEmailPhoneACTContent!![j]
                                                        }
                                                        text.lastIndex -> {
                                                            text += usernameEmailPhoneACTContent!![j]
                                                        }
                                                        else -> {
                                                            text += "*"
                                                        }
                                                    }
                                                }
                                                showDialog("${resources.getString(R.string.you_can_log_in_with_your_phone_number_linked_to_your_username)} ($text)\n")
                                            }
                                            i.getString("email") != null -> {
                                                usernameEmailPhoneACTContent =
                                                    i.getString("email")
                                                sendResetPassword()
                                            }
                                            else -> {
                                                setToast(
                                                    resources.getString(R.string.theres_no_user_linked_to_this_username_try_to_sign_up_instead)
                                                )
                                            }
                                        }
                                    } else {
                                        showDialog(resources.getString(R.string.you_can_log_in_with_your_facebook_account))
                                    }
                                }
                            }
                        }
                    } else {
                        setToast(
                            resources.getString(R.string.something_went_wrong_please_try_again_later)
                        )
                    }
                } else {
                    setToast(
                        resources.getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
            }
        } else {
            openNoInternetDialog()
        }
    }

    private fun sendResetPassword() {
        if (isInternetAvailable()) {
            setProgress(.5F, false, View.VISIBLE)
            val query = db.collection("users").whereEqualTo(
                "email",
                usernameEmailPhoneACTContent
            )
            query.get().addOnCompleteListener {
                setProgress(1F, true, View.GONE)
                if (it.isSuccessful) {
                    if (it.result != null) {
                        if (it.result!!.size() == 0) {
                            setToast(
                                resources.getString(R.string.theres_no_user_linked_to_this_username_try_to_sign_up_instead)
                            )
                        } else {
                            for (i in it.result!!) {
                                if (i.getString("email") == usernameEmailPhoneACTContent) {
                                    if (i.getString("facebookId") == null) {
                                        auth.sendPasswordResetEmail(usernameEmailPhoneACTContent!!)
                                            .addOnCompleteListener { it1 ->
                                                setProgress(1F, true, View.GONE)
                                                if (it1.isSuccessful) {
                                                    val email =
                                                        usernameEmailPhoneACTContent!!.split("@")[0]
                                                    var text = ""
                                                    for (j in email.indices) {
                                                        when (j) {
                                                            0 -> {
                                                                text += email[j]
                                                            }
                                                            text.lastIndex -> {
                                                                text += email[j]
                                                            }
                                                            else -> {
                                                                text += "*"
                                                            }
                                                        }
                                                    }
                                                    showDialog(
                                                        "${resources.getString(R.string.please_check_your_email_to_reset_your_password)} ($text${usernameEmailPhoneACTContent!!.split(
                                                            "@"
                                                        )[1]})"
                                                    )
                                                } else {
                                                    it.exception?.localizedMessage?.let { it2 ->
                                                        setToast(
                                                            it2
                                                        )
                                                    }
                                                }
                                            }
                                    } else {
                                        showDialog(resources.getString(R.string.you_can_log_in_with_your_facebook_account))
                                    }
                                }
                            }
                        }
                    } else {
                        setToast(
                            resources.getString(R.string.something_went_wrong_please_try_again_later)
                        )
                    }
                } else {
                    setToast(
                        resources.getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
            }
        } else {
            openNoInternetDialog()
        }
    }

    private fun showDialog(text: String) {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val view = layoutInflater.inflate(R.layout.layout_no_internet, null)
        val layoutNoInternetDismissMTV =
            view.findViewById<MaterialTextView>(R.id.layout_no_internet_dismissMTV)
        val layoutNoInternetMainMTV =
            view.findViewById<MaterialTextView>(R.id.layout_no_internet_mainMTV)
        setTextViewToggle(layoutNoInternetDismissMTV)
        layoutNoInternetMainMTV.text = text
        layoutNoInternetDismissMTV.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setView(view)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}