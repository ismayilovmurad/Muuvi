package com.martiandeveloper.muuvi.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.databinding.FragmentSignUpUsernameBinding
import com.martiandeveloper.muuvi.viewmodel.SignUpUsernameViewModel
import kotlinx.android.synthetic.main.fragment_sign_up_username.*
import java.util.regex.Pattern

class SignUpUsernameFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSignUpUsernameBinding

    private lateinit var vm: SignUpUsernameViewModel

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val pattern = Pattern.compile("""^[_.A-Za-z0-9]*((\s)*[_.A-Za-z0-9])*${'$'}""")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = activity?.run {
            ViewModelProviders.of(this)[SignUpUsernameViewModel::class.java]
        } ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up_username, container, false)
        binding.signUpUsernameViewModel = vm
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
        binding.isErrorMTVGone = true
        setListeners()
        fragment_sign_up_username_usernameET.requestFocus()
        fragment_sign_up_username_usernamePB.visibility = View.GONE
    }

    private fun setProgress(alpha: Float, enable: Boolean, visible: Int) {
        fragment_sign_up_username_mainLL.alpha = alpha
        fragment_sign_up_username_bottomLL.alpha = alpha
        fragment_sign_up_username_usernameET.isEnabled = enable
        fragment_sign_up_username_nextMBTN.isEnabled = enable
        fragment_sign_up_username_privacyPolicyMTV.isEnabled = enable
        fragment_sign_up_username_termsAndConditionsMTV.isEnabled = enable
        fragment_sign_up_username_mainPB.visibility = visible
    }

    private fun setMBTN(alpha: Float, enable: Boolean) {
        fragment_sign_up_username_nextMBTN.alpha = alpha
        fragment_sign_up_username_nextMBTN.isEnabled = enable
    }

    private fun observeEditTextContent(
    ) {
        vm.usernameETContent.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                if (it.length < 15) {
                    if (pattern.matcher(it).matches()) {
                        checkUsername(it, false)
                    } else {
                        setError(resources.getString(R.string.usernames_can_only_use_letters_numbers_underscores_and_periods))
                    }
                } else {
                    setError(resources.getString(R.string.username_not_available))
                }
            } else {
                setMBTN(.5F, false)
            }
        })
    }

    private fun setError(s: String) {
        setMBTN(.5F, false)
        fragment_sign_up_username_usernameET.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_error,
            0
        )
        binding.isErrorMTVGone = false
        binding.error = s
    }

    private fun checkUsername(s: String?, save: Boolean) {
        if (s != null && s.isNotEmpty()) {
            if (s[0].isLetter()) {
                if (isInternetAvailable()) {
                    if (!save) {
                        setEditTextProgress()
                    }
                    if (s.isNullOrEmpty()) {
                        setError(resources.getString(R.string.username_cannot_be_empty))
                    } else {
                        val query = db.collection("users").whereEqualTo("username", s)
                        query.get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                for (i in it.result!!) {
                                    if (i.getString("username") == s) {
                                        try {
                                            fragment_sign_up_username_usernamePB.visibility =
                                                View.GONE
                                            setError(resources.getString(R.string.username_not_available))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }

                            if (it.result?.size() == 0) {
                                fragment_sign_up_username_usernamePB.visibility = View.GONE
                                setSuccess()

                                if (save) {
                                    saveUsername(s.toString())
                                }
                            }
                        }
                    }
                } else {
                    openNoInternetDialog()
                }
            } else {
                setError(resources.getString(R.string.username_cannot_start_with_a_number))
            }
        } else {
            setError(resources.getString(R.string.username_cannot_be_empty))
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

    private fun setEditTextProgress() {
        fragment_sign_up_username_usernamePB.visibility = View.VISIBLE
        fragment_sign_up_username_usernameET.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            0,
            0
        )
    }

    private fun setSuccess() {
        setMBTN(1F, true)
        fragment_sign_up_username_usernameET.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_success,
            0
        )
        binding.isErrorMTVGone = true
    }

    private fun saveUsername(s: String?) {
        setProgress(.5F, false, View.VISIBLE)

        val user = auth.currentUser

        if (user != null) {
            db.collection("users").document(user.uid).update("username", s).addOnCompleteListener {
                setProgress(1F, true, View.GONE)
                if (it.isSuccessful) {
                    hideErrorAndSuccessMessages()
                    navigateSignUpUserImageFragment()
                } else {
                    setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
                }
            }
        } else {
            setProgress(1F, true, View.GONE)
            setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
        }
    }

    private fun hideErrorAndSuccessMessages() {
        setMBTN(.5F, false)
        fragment_sign_up_username_usernameET.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            0,
            0
        )
        binding.isErrorMTVGone = true
    }

    private fun setToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun navigateSignUpUserImageFragment() {
        val action =
            SignUpUsernameFragmentDirections.actionSignUpUsernameFragmentToSignUpUserImageFragment()
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

    private fun setListeners() {
        fragment_sign_up_username_nextMBTN.setOnClickListener(this)
        fragment_sign_up_username_privacyPolicyMTV.setOnClickListener(this)
        fragment_sign_up_username_termsAndConditionsMTV.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.fragment_sign_up_username_nextMBTN -> {
                    checkUsername(fragment_sign_up_username_usernameET.text.toString(), true)
                }
                R.id.fragment_sign_up_username_privacyPolicyMTV -> openWeb("https://muuviapp.blogspot.com/p/privacy-policy.html")
                R.id.fragment_sign_up_username_termsAndConditionsMTV -> openWeb("https://muuviapp.blogspot.com/p/terms-conditions.html")
            }
        }
    }

    private fun openWeb(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
