package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.android.ext.android.inject
import kotlin.contracts.contract

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val viewModel: AuthenticationViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity
        viewModel.authenticationState.observe(this) { state ->
            state?.let {
                handleAuthenticationState(it)
            }
        }
//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        observeAuthenticationState()
        setUpAction()
    }

    private fun setUpAction() {
        binding.btnLogin.setOnClickListener {
            signIn()
        }
    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(this) { authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
//                    open RemindersActivity
                    openRemindersActivity()
                }
                else -> {

                }
            }
        }
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            val responder = result.idpResponse
            if (result.resultCode == RESULT_OK) {
                handleAuthenticationState(AuthenticationViewModel.AuthenticationState.AUTHENTICATED)
            } else {

            }
        }

    private fun signIn() {
        val loginProviders = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(loginProviders)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun openRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
    }

    private fun handleAuthenticationState(state: AuthenticationViewModel.AuthenticationState) {
        when (state) {
            AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                openRemindersActivity()
            }
            AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {

            }
            else -> {

            }
        }
    }

}
