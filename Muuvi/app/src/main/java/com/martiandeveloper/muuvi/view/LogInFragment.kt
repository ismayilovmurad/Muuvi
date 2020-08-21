package com.martiandeveloper.muuvi.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.databinding.FragmentLogInBinding
import com.martiandeveloper.muuvi.viewmodel.LogInViewModel
import kotlinx.android.synthetic.main.fragment_log_in.*
import java.util.concurrent.TimeUnit

class LogInFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentLogInBinding

    private var isPasswordVisible = false

    private val callbackManager = CallbackManager.Factory.create()

    private lateinit var vm: LogInViewModel

    private var phoneNumberEmailUsernameACTContent: String? = null
    private var passwordETContent: String? = null
    private var confirmationCodeETContent: String? = null

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var phoneNumberCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var myVerificationId: String? = null
    private var myToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = activity?.run {
            ViewModelProviders.of(this)[LogInViewModel::class.java]
        } ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_log_in, container, false)
        binding.logInViewModel = vm
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setProgress(1F, true, View.GONE)
        binding.isVerifyLLGone = true
        binding.isPasswordETGone = true
        setPasswordToggle()
        setTextViewToggle(fragment_log_in_logInWithFacebookMTV)
        observeEditTextContent(vm.phoneNumberEmailUsernameACTContent, 0, fragment_log_in_logInMBTN)
        setMBTN(.5F, false, fragment_log_in_logInMBTN)
        setListeners()
        initPhoneNumberCallback()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setPasswordToggle() {
        fragment_log_in_passwordET.setOnTouchListener(OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= fragment_log_in_passwordET.right - fragment_log_in_passwordET.compoundDrawables[2].bounds.width()
                ) {
                    val selection: Int = fragment_log_in_passwordET.selectionEnd
                    isPasswordVisible = if (isPasswordVisible) {
                        fragment_log_in_passwordET.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_visibility_off,
                            0
                        )

                        fragment_log_in_passwordET.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        false
                    } else {
                        fragment_log_in_passwordET.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_visibility,
                            0
                        )

                        fragment_log_in_passwordET.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        true
                    }
                    fragment_log_in_passwordET.setSelection(selection)
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun setTextViewToggle(textView: MaterialTextView) {
        textView.setTextColor(
            context?.let {
                ContextCompat.getColorStateList(it, R.color.facebook_log_in_text_selector)
            }
        )
    }

    private fun setProgress(alpha: Float, enable: Boolean, visible: Int) {
        fragment_log_in_mainLL.alpha = alpha
        fragment_log_in_facebookLL.alpha = alpha
        fragment_log_in_bottomLL.alpha = alpha
        fragment_log_in_verifyLL.alpha = alpha
        fragment_log_in_phoneNumberEmailUsernameACT.isEnabled = enable
        fragment_log_in_passwordET.isEnabled = enable
        fragment_log_in_logInMBTN.isEnabled = enable
        fragment_log_in_getHelpSigningInLL.isEnabled = enable
        fragment_log_in_logInWithFacebookMTV.isEnabled = enable
        fragment_log_in_bottomLL.isEnabled = enable
        fragment_log_in_requestNewOneMTV.isEnabled = enable
        fragment_log_in_confirmationCodeET.isEnabled = enable
        fragment_log_in_confirmationCodeNextMBTN.isEnabled = enable
        fragment_log_in_mainPB.visibility = visible
    }

    private fun observeEditTextContent(
        content: MutableLiveData<String>,
        textLength: Int,
        btn: MaterialButton
    ) {
        content.observe(viewLifecycleOwner, {
            if (textLength == 0) {
                if (it.isNotEmpty()) {
                    setMBTN(1F, true, btn)
                } else {
                    setMBTN(.5F, false, btn)
                }
            } else {
                if (it.length == textLength) {
                    setMBTN(1F, true, btn)
                } else {
                    setMBTN(.5F, false, btn)
                }
            }
        })
    }

    private fun setMBTN(alpha: Float, enable: Boolean, btn: MaterialButton) {
        btn.alpha = alpha
        btn.isEnabled = enable
    }

    private fun setListeners() {
        fragment_log_in_logInMBTN.setOnClickListener(this)
        fragment_log_in_requestNewOneMTV.setOnClickListener(this)
        fragment_log_in_confirmationCodeNextMBTN.setOnClickListener(this)
        fragment_log_in_getHelpSigningInLL.setOnClickListener(this)
        fragment_log_in_logInWithFacebookMTV.setOnClickListener(this)
        fragment_log_in_bottomLL.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.fragment_log_in_logInMBTN -> checkLogIn()
                R.id.fragment_log_in_requestNewOneMTV -> resendVerificationCode(
                    phoneNumberEmailUsernameACTContent!!,
                    myToken
                )
                R.id.fragment_log_in_confirmationCodeNextMBTN -> checkCode()
                R.id.fragment_log_in_getHelpSigningInLL -> navigate(LogInFragmentDirections.actionLoginFragmentToLogInHelpFragment())
                R.id.fragment_log_in_logInWithFacebookMTV -> openFacebookLogIn()
                R.id.fragment_log_in_bottomLL -> navigate(LogInFragmentDirections.actionLoginFragmentToSignUpFragment())
            }
        }
    }

    private fun checkLogIn() {
        if (fragment_log_in_passwordET.visibility == View.VISIBLE) {
            passwordETContent = vm.passwordETContent.value
            logInWithEmailAndPassword(phoneNumberEmailUsernameACTContent!!, passwordETContent!!)
        } else {
            phoneNumberEmailUsernameACTContent = vm.phoneNumberEmailUsernameACTContent.value
            when {
                Patterns.EMAIL_ADDRESS.matcher(phoneNumberEmailUsernameACTContent!!)
                    .matches() -> {
                    setPasswordET()
                }
                Patterns.PHONE.matcher(phoneNumberEmailUsernameACTContent!!)
                    .matches() -> {
                    checkPhoneNumber(phoneNumberEmailUsernameACTContent!!.replace(" ", ""))
                }
                phoneNumberEmailUsernameACTContent!![0].isLetter() -> {
                    checkUsername(phoneNumberEmailUsernameACTContent!!)
                }
            }
        }
    }

    private fun logInWithEmailAndPassword(email: String, password: String) {
        if (isInternetAvailable()) {
            setProgress(.5F, false, View.VISIBLE)
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                setProgress(1F, true, View.GONE)
                if (it.isSuccessful) {
                    goToFeed()
                } else {
                    it.exception?.localizedMessage?.let { it1 -> setToast(it1) }
                    binding.isPhoneNumberEmailUsernameACTGone = false
                    binding.isPasswordETGone = true
                }
            }
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

    private fun goToFeed() {
        val intent = Intent(activity, FeedActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun checkPhoneNumber(phoneNumber: String) {
        if (phoneNumber.startsWith("+")) {
            if (isInternetAvailable()) {
                setProgress(.5F, false, View.VISIBLE)
                val query = db.collection("users").whereEqualTo(
                    "phoneNumber",
                    phoneNumber
                )
                query.get().addOnCompleteListener {
                    setProgress(1F, true, View.GONE)
                    if (it.isSuccessful) {
                        if (it.result != null) {
                            if (it.result!!.size() == 0) {
                                setToast(resources.getString(R.string.theres_no_user_linked_to_this_phone_number_try_to_sign_up_instead))
                            } else {
                                for (i in it.result!!) {
                                    if (i.getString("phoneNumber") == phoneNumber) {
                                        startPhoneNumberVerification(
                                            phoneNumber
                                        )
                                    }
                                }
                            }
                        } else {
                            setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
                        }
                    } else {
                        setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
                    }
                }
            } else {
                openNoInternetDialog()
            }
        } else {
            setToast(resources.getString(R.string.please_add_phone_number_code_of_your_country))
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        if (isInternetAvailable()) {
            setProgress(.5F, false, View.VISIBLE)
            PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    requireActivity(),
                    phoneNumberCallbacks
                )
        } else {
            openNoInternetDialog()
        }
    }

    private fun initPhoneNumberCallback() {
        phoneNumberCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                setProgress(1F, true, View.GONE)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                setProgress(1F, true, View.GONE)
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.invalid_phone_number),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is FirebaseTooManyRequestsException -> {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.quota_exceeded),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.something_went_wrong_please_try_again_later),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                setProgress(1F, true, View.GONE)
                myVerificationId = verificationId
                myToken = token
                showVerifyLayout()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        if (isInternetAvailable()) {
            setProgress(.5F, false, View.VISIBLE)

            auth.signInWithCredential(credential).addOnCompleteListener {
                setProgress(1F, true, View.GONE)
                if (it.isSuccessful) {
                    if (it.result != null) {
                        if (it.result!!.user != null) {
                            goToFeed()
                        } else {
                            setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                        }
                    } else {
                        setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                    }
                } else {
                    setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                }
            }
        } else {
            openNoInternetDialog()
        }
    }

    private fun showVerifyLayout() {
        binding.isMainLLGone = true
        binding.isFacebookLLGone = true
        binding.isBottomLLGone = true
        binding.isVerifyLLGone = false
        var text = ""
        for (j in phoneNumberEmailUsernameACTContent!!.indices) {
            when (j) {
                0 -> {
                    text += phoneNumberEmailUsernameACTContent!![j]
                }
                1 -> {
                    text += phoneNumberEmailUsernameACTContent!![j]
                }
                2 -> {
                    text += phoneNumberEmailUsernameACTContent!![j]
                }
                3 -> {
                    text += phoneNumberEmailUsernameACTContent!![j]
                }
                4 -> {
                    text += phoneNumberEmailUsernameACTContent!![j]
                }
                5 -> {
                    text += phoneNumberEmailUsernameACTContent!![j]
                }
                6 -> {
                    text += phoneNumberEmailUsernameACTContent!![j]
                }
                7 -> {
                    text += phoneNumberEmailUsernameACTContent!![j]
                }
                text.lastIndex -> {
                    text += phoneNumberEmailUsernameACTContent!![j]
                }
                else -> {
                    text += "*"
                }
            }
        }
        binding.note =
            "${resources.getString(R.string.enter_the_six_digit_code_we_sent_to)} $text"
        observeEditTextContent(
            vm.confirmationCodeETContent,
            6,
            fragment_log_in_confirmationCodeNextMBTN
        )
        setMBTN(.5F, false, fragment_log_in_confirmationCodeNextMBTN)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        if (myToken != null) {
            if (isInternetAvailable()) {
                setProgress(.5F, false, View.VISIBLE)
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    requireActivity(),
                    phoneNumberCallbacks,
                    token
                )
            } else {
                openNoInternetDialog()
            }
        } else {
            setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
        }
    }

    private fun checkCode() {
        confirmationCodeETContent = vm.confirmationCodeETContent.value
        setProgress(.5F, false, View.VISIBLE)
        if (myVerificationId != null) {
            val credential = PhoneAuthProvider.getCredential(
                myVerificationId!!,
                confirmationCodeETContent!!
            )

            if (isInternetAvailable()) {
                if (credential.smsCode != null) {
                    setProgress(1F, true, View.GONE)
                    signInWithPhoneAuthCredential(credential)
                } else {
                    setProgress(1F, true, View.GONE)
                    setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
                }
            } else {
                setProgress(1F, true, View.GONE)
                openNoInternetDialog()
            }
        } else {
            setProgress(1F, true, View.GONE)
            setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
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
                            setToast(resources.getString(R.string.theres_no_user_linked_to_this_username_try_to_sign_up_instead))
                        } else {
                            for (i in it.result!!) {
                                if (i.getString("username") == username) {
                                    if (i.getString("facebookId") == null) {
                                        when {
                                            i.getString("phoneNumber") != null -> {
                                                phoneNumberEmailUsernameACTContent =
                                                    i.getString("phoneNumber")
                                                startPhoneNumberVerification(i.getString("phoneNumber")!!)
                                            }
                                            i.getString("email") != null -> {
                                                phoneNumberEmailUsernameACTContent =
                                                    i.getString("email")
                                                setPasswordET()
                                            }
                                            else -> {
                                                setToast(resources.getString(R.string.theres_no_user_linked_to_this_username_try_to_sign_up_instead))
                                            }
                                        }
                                    } else {
                                        setToast(resources.getString(R.string.you_can_log_in_with_your_facebook_account))
                                    }
                                }
                            }
                        }
                    } else {
                        setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
                    }
                } else {
                    setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
                }
            }
        } else {
            openNoInternetDialog()
        }
    }

    private fun setPasswordET() {
        binding.isPhoneNumberEmailUsernameACTGone = true
        binding.isPasswordETGone = false
        setMBTN(.5F, false, fragment_log_in_logInMBTN)
        if (!vm.passwordETContent.hasActiveObservers()) {
            observeEditTextContent(vm.passwordETContent, 0, fragment_log_in_logInMBTN)
        }
    }

    private fun navigate(action: NavDirections) {
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

    private fun openFacebookLogIn() {
        if (isInternetAvailable()) {
            setProgress(.5F, false, View.VISIBLE)
            fragment_log_in_phoneNumberEmailUsernameACT.clearFocus()
            fragment_log_in_passwordET.clearFocus()
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
                navigate(LogInFragmentDirections.actionLoginFragmentToSignUpUsernameFragment())
            } else {
                setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
            }
        }
    }
}
