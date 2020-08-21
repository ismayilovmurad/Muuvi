package com.martiandeveloper.muuvi.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.databinding.FragmentPasswordBinding
import com.martiandeveloper.muuvi.viewmodel.PasswordViewModel
import kotlinx.android.synthetic.main.fragment_password.*

class PasswordFragment : Fragment(), View.OnClickListener {

    private var isPasswordVisible = false

    private lateinit var vm: PasswordViewModel

    private var passwordETContent: String? = null

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var binding: FragmentPasswordBinding

    private var emailAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = activity?.run {
            ViewModelProviders.of(this)[PasswordViewModel::class.java]
        } ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_password, container, false)
        binding.passwordViewModel = vm
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setProgress(1F, true, View.GONE)
        setMBTN(.5F, false)
        observeEditTextContent()
        setPasswordToggle(fragment_password_passwordET)
        setPasswordToggle(fragment_password_confirmPasswordET)
        setListeners()
        emailAddress = arguments?.getString("emailAddress")
        binding.isVerifyLLGone = true
    }

    private fun setProgress(alpha: Float, enable: Boolean, visible: Int) {
        fragment_password_mainLL.alpha = alpha
        fragment_password_passwordET.isEnabled = enable
        fragment_password_confirmPasswordET.isEnabled = enable
        fragment_password_nextMBTN.isEnabled = enable
        fragment_password_mainPB.visibility = visible
    }

    private fun setMBTN(alpha: Float, enable: Boolean) {
        fragment_password_nextMBTN.alpha = alpha
        fragment_password_nextMBTN.isEnabled = enable
    }

    private fun observeEditTextContent(
    ) {
        vm.passwordETContent.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                if (it.length >= 4) {
                    if (it == vm.confirmPasswordET.value) {
                        setMBTN(1F, true)
                    } else {
                        setMBTN(.5F, false)
                    }
                } else {
                    setMBTN(.5F, false)
                }
            } else {
                setMBTN(.5F, false)
            }
        })

        vm.confirmPasswordET.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                if (it.length >= 4) {
                    if (it == vm.passwordETContent.value) {
                        setMBTN(1F, true)
                    } else {
                        setMBTN(.5F, false)
                    }
                } else {
                    setMBTN(.5F, false)
                }
            } else {
                setMBTN(.5F, false)
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setPasswordToggle(editText: EditText) {
        editText.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= editText.right - editText.compoundDrawables[2].bounds.width()
                ) {
                    val selection: Int = editText.selectionEnd
                    isPasswordVisible = if (isPasswordVisible) {
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_visibility_off,
                            0
                        )

                        editText.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        false
                    } else {
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_visibility,
                            0
                        )

                        editText.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        true
                    }
                    editText.setSelection(selection)
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun setListeners() {
        fragment_password_nextMBTN.setOnClickListener(this)
        fragment_password_verifyNextMBTN.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.fragment_password_nextMBTN -> signUp()
                R.id.fragment_password_verifyNextMBTN -> checkEmailVerification()
            }
        }
    }

    private fun signUp() {
        passwordETContent = vm.passwordETContent.value
        if (isInternetAvailable()) {
            setProgress(.5F, false, View.VISIBLE)
            auth.createUserWithEmailAndPassword(emailAddress!!, passwordETContent!!)
                .addOnCompleteListener {
                    setProgress(1F, true, View.GONE)
                    if (it.isSuccessful) {
                        if (it.result != null) {
                            if (it.result!!.user != null) {
                                sendVerification(it.result!!.user!!)
                            } else {
                                setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                            }
                        } else {
                            setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                        }
                    } else {
                        it.exception?.localizedMessage?.let { it1 -> setToast(it1) }
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

    private fun setTextViewToggle(textView: MaterialTextView) {
        textView.setTextColor(
            context?.let {
                ContextCompat.getColorStateList(it, R.color.facebook_log_in_text_selector)
            }
        )
    }

    private fun sendVerification(user: FirebaseUser) {
        setProgress(.5F, false, View.VISIBLE)
        user.sendEmailVerification()
            .addOnCompleteListener {
                setProgress(1F, true, View.GONE)
                if (it.isSuccessful) {
                    saveUserData(user)
                } else {
                    setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                }
            }
    }

    private fun saveUserData(user: FirebaseUser) {
        setProgress(.5F, false, View.VISIBLE)
        val userMap = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "username" to ""
        )

        db.collection("users").document(user.uid).set(userMap).addOnCompleteListener {
            setProgress(1F, true, View.GONE)
            if (it.isSuccessful) {
                showVerifyLayout()
            } else {
                setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
            }
        }
    }

    private fun showVerifyLayout() {
        binding.isMainLLGone = true
        binding.isVerifyLLGone = false
    }

    private fun setToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun checkEmailVerification() {
        if (auth.currentUser != null) {
            auth.currentUser!!.reload()
            if (auth.currentUser!!.isEmailVerified) {
                navigateSignUpUsernameFragment(view)
            } else {
                setToast(resources.getString(R.string.please_verify_your_email_address_to_continue))
            }
        }
    }

    private fun navigateSignUpUsernameFragment(v: View?) {
        val action =
            PasswordFragmentDirections.actionPasswordFragmentToSignUpUsernameFragment()
        v?.let {
            Navigation.findNavController(it).navigate(action)
        }
    }
}
