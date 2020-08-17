package com.martiandeveloper.muuvi.view

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.adapter.RecyclerViewCountryAdapter
import com.martiandeveloper.muuvi.databinding.FragmentSignUpWithPhoneOrEmailBinding
import com.martiandeveloper.muuvi.model.Country
import com.martiandeveloper.muuvi.viewmodel.SignUpWithPhoneOrEmailViewModel
import kotlinx.android.synthetic.main.fragment_sign_up_with_phone_or_email.*
import java.util.concurrent.TimeUnit

class SignUpWithPhoneOrEmailFragment : Fragment(), View.OnClickListener,
    RecyclerViewCountryAdapter.ItemClickListener {

    private lateinit var binding: FragmentSignUpWithPhoneOrEmailBinding

    private lateinit var vm: SignUpWithPhoneOrEmailViewModel

    private var selectedCountryCode: String = "+93"

    private lateinit var countryNameList: ArrayList<String>
    private lateinit var countryCodeList: ArrayList<String>
    private lateinit var countryNameShortageList: ArrayList<String>

    private lateinit var countryAdapter: RecyclerViewCountryAdapter

    private lateinit var phoneCodesDialog: AlertDialog

    private var phoneNumberETContent: String? = null
    private var emailETContent: String? = null
    private var confirmationCodeETContent: String? = null

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private lateinit var phoneNumberCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var myVerificationId: String? = null
    private var myToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = activity?.run {
            ViewModelProviders.of(this)[SignUpWithPhoneOrEmailViewModel::class.java]
        } ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_sign_up_with_phone_or_email,
                container,
                false
            )
        binding.signUpWithPhoneOrEmailViewModel = vm
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setProgress(1F, true, View.GONE)
        binding.isVerifyLLGone = true
        binding.isEmailLLGone = true
        binding.phoneCode = "AF +93"
        setListeners()
        setMBTN(.5F, false, fragment_sign_up_with_phone_or_email_phoneNextMBTN)
        observeEditTextContent(
            vm.phoneNumberETContent,
            "phone",
            fragment_sign_up_with_phone_or_email_phoneNextMBTN
        )
        initPhoneNumberCallback()
        setMBTN(.5F, false, fragment_sign_up_with_phone_or_email_emailNextMBTN)
        observeEditTextContent(
            vm.emailETContent,
            "email",
            fragment_sign_up_with_phone_or_email_emailNextMBTN
        )
    }

    private fun setProgress(alpha: Float, enable: Boolean, visible: Int) {
        fragment_sign_up_with_phone_or_email_verifyLL.alpha = alpha
        fragment_sign_up_with_phone_or_email_mainLL.alpha = alpha
        fragment_sign_up_with_phone_or_email_bottomLL.alpha = alpha
        fragment_sign_up_with_phone_or_email_requestNewOneMTV.isEnabled = enable
        fragment_sign_up_with_phone_or_email_confirmationCodeET.isEnabled = enable
        fragment_sign_up_with_phone_or_email_confirmationCodeNextMBTN.isEnabled = enable
        fragment_sign_up_with_phone_or_email_emailMTV.isEnabled = enable
        fragment_sign_up_with_phone_or_email_phoneCodeMTV.isEnabled = enable
        fragment_sign_up_with_phone_or_email_phoneNumberET.isEnabled = enable
        fragment_sign_up_with_phone_or_email_phoneNextMBTN.isEnabled = enable
        fragment_sign_up_with_phone_or_email_phoneMTV2.isEnabled = enable
        fragment_sign_up_with_phone_or_email_emailET.isEnabled = enable
        fragment_sign_up_with_phone_or_email_emailNextMBTN.isEnabled = enable
        fragment_sign_up_with_phone_or_email_bottomLL.isEnabled = enable
        fragment_sign_up_with_phone_or_email_mainPB.visibility = visible
    }

    private fun setListeners() {
        fragment_sign_up_with_phone_or_email_phoneCodeMTV.setOnClickListener(this)
        fragment_sign_up_with_phone_or_email_phoneNextMBTN.setOnClickListener(this)
        fragment_sign_up_with_phone_or_email_requestNewOneMTV.setOnClickListener(this)
        fragment_sign_up_with_phone_or_email_confirmationCodeNextMBTN.setOnClickListener(this)
        fragment_sign_up_with_phone_or_email_emailMTV.setOnClickListener(this)
        fragment_sign_up_with_phone_or_email_phoneMTV2.setOnClickListener(this)
        fragment_sign_up_with_phone_or_email_emailNextMBTN.setOnClickListener(this)
        fragment_sign_up_with_phone_or_email_bottomLL.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.fragment_sign_up_with_phone_or_email_phoneCodeMTV -> openPhoneCodesDialog()
                R.id.fragment_sign_up_with_phone_or_email_phoneNextMBTN -> checkPhoneNumber()
                R.id.fragment_sign_up_with_phone_or_email_requestNewOneMTV -> resendVerificationCode(
                    selectedCountryCode + phoneNumberETContent!!,
                    myToken
                )
                R.id.fragment_sign_up_with_phone_or_email_confirmationCodeNextMBTN -> checkCode()
                R.id.fragment_sign_up_with_phone_or_email_emailMTV -> showEmailLayout()
                R.id.fragment_sign_up_with_phone_or_email_phoneMTV2 -> showPhoneLayout()
                R.id.fragment_sign_up_with_phone_or_email_emailNextMBTN -> navigatePasswordFragment()
                R.id.fragment_sign_up_with_phone_or_email_bottomLL -> navigate(
                    SignUpWithPhoneOrEmailFragmentDirections.actionSignUpWithPhoneOrEmailFragmentToLoginFragment()
                )
            }
        }
    }

    private fun openPhoneCodesDialog() {
        fillTheLists()
        setCountryAdapter()

        phoneCodesDialog = AlertDialog.Builder(view?.context).create()
        val view = layoutInflater.inflate(R.layout.layout_country_codes, null)

        val layoutCountryCodesMainRV =
            view.findViewById<RecyclerView>(R.id.layout_country_codes_mainRV)

        layoutCountryCodesMainRV.layoutManager = LinearLayoutManager(context)
        layoutCountryCodesMainRV.adapter = countryAdapter

        val layoutCountryCodesMainSV =
            view.findViewById<SearchView>(R.id.layout_country_codes_mainSV)
        layoutCountryCodesMainSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                countryAdapter.filter.filter(newText)
                return false
            }

        })

        phoneCodesDialog.setView(view)
        phoneCodesDialog.show()
    }

    private fun fillTheLists() {
        countryNameList = ArrayList(listOf(*resources.getStringArray(R.array.country_name)))
        countryCodeList = ArrayList(listOf(*resources.getStringArray(R.array.country_code)))
        countryNameShortageList =
            ArrayList(listOf(*resources.getStringArray(R.array.country_name_shortage)))
    }

    private fun setCountryAdapter() {
        val countryList = ArrayList<Country>()
        for (i in 0 until countryNameList.size) {
            countryList.add(
                Country(
                    countryNameList[i],
                    countryCodeList[i],
                    countryNameShortageList[i]
                )
            )
        }
        countryAdapter = RecyclerViewCountryAdapter(countryList, requireContext(), this)
    }

    override fun onItemClick(country: Country) {
        phoneCodesDialog.dismiss()
        fragment_sign_up_with_phone_or_email_phoneNumberET.requestFocus()
        val text: String = if (country.countryCode.contains("–")) {
            "${country.countryNameShortage} +1"
        } else {
            val simplifiedCountryCode =
                country.countryCode.substring(1, country.countryCode.length - 1)
            "${country.countryNameShortage} $simplifiedCountryCode"
        }

        binding.phoneCode = text

        selectedCountryCode = if (country.countryCode.contains("–")) {
            val simple = country.countryCode.replace("–", "")
            simple.substring(1, simple.length - 1)
        } else {
            country.countryCode.substring(1, country.countryCode.length - 1)
        }
    }

    private fun observeEditTextContent(
        content: MutableLiveData<String>,
        type: String,
        btn: MaterialButton
    ) {
        content.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                if (type == "phone") {
                    if (Patterns.PHONE.matcher(it).matches()) {
                        setMBTN(1F, true, btn)
                    } else {
                        setMBTN(.5F, false, btn)
                    }
                } else if (type == "email") {
                    if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                        setMBTN(1F, true, btn)
                    } else {
                        setMBTN(.5F, false, btn)
                    }
                } else if (type == "confirm") {
                    if (it.length == 6) {
                        setMBTN(1F, true, btn)
                    } else {
                        setMBTN(.5F, false, btn)
                    }
                }
            } else {
                setMBTN(.5F, false, btn)
            }
        })
    }

    private fun setMBTN(alpha: Float, enable: Boolean, btn: MaterialButton) {
        btn.alpha = alpha
        btn.isEnabled = enable
    }

    private fun checkPhoneNumber() {
        phoneNumberETContent = vm.phoneNumberETContent.value?.replace(" ", "")
        if (!phoneNumberETContent!!.startsWith("+")) {
            if (isInternetAvailable()) {
                setProgress(.5F, false, View.VISIBLE)
                val query = db.collection("users").whereEqualTo(
                    "phoneNumber",
                    selectedCountryCode + phoneNumberETContent
                )
                query.get().addOnCompleteListener {
                    setProgress(1F, true, View.GONE)
                    if (it.isSuccessful) {
                        if (it.result != null) {
                            if (it.result!!.size() == 0) {
                                startPhoneNumberVerification(
                                    selectedCountryCode + phoneNumberETContent!!
                                )
                            } else {
                                setToast(resources.getString(R.string.phone_number_already_exists_try_to_log_in_instead))
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
            setToast(resources.getString(R.string.invalid_phone_number))
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

    private fun setToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
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
                            saveUserData(it.result!!.user!!)
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
        binding.isBottomLLGone = true
        binding.isVerifyLLGone = false
        binding.note =
            "${resources.getString(R.string.enter_the_six_digit_code_we_sent_to)} $selectedCountryCode$phoneNumberETContent"
        observeEditTextContent(
            vm.confirmationCodeETContent,
            "confirm",
            fragment_sign_up_with_phone_or_email_confirmationCodeNextMBTN
        )
        setMBTN(.5F, false, fragment_sign_up_with_phone_or_email_confirmationCodeNextMBTN)
    }

    private fun saveUserData(user: FirebaseUser) {
        setProgress(.5F, false, View.VISIBLE)
        val userMap = hashMapOf(
            "uid" to user.uid,
            "phoneNumber" to user.phoneNumber,
            "username" to ""
        )

        db.collection("users").document(user.uid).set(userMap).addOnCompleteListener {
            setProgress(1F, true, View.GONE)
            if (it.isSuccessful) {
                navigate(SignUpWithPhoneOrEmailFragmentDirections.actionSignUpWithPhoneOrEmailFragmentToSignUpUsernameFragment())
            } else {
                setToast(resources.getString(R.string.couldnt_log_in_please_try_again_later))
            }
        }
    }

    private fun navigate(action: NavDirections) {
        view?.let { Navigation.findNavController(it).navigate(action) }
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

    private fun showEmailLayout() {
        binding.isPhoneLLGone = true
        binding.isEmailLLGone = false
    }

    private fun showPhoneLayout() {
        binding.isEmailLLGone = true
        binding.isPhoneLLGone = false
    }

    private fun navigatePasswordFragment() {
        emailETContent = vm.emailETContent.value
        val action =
            SignUpWithPhoneOrEmailFragmentDirections.actionSignUpWithPhoneOrEmailFragmentToPasswordFragment(
                emailETContent!!
            )
        view?.let {
            Navigation.findNavController(it).navigate(action)
        }
    }
}
