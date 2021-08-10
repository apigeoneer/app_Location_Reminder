package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    private val _viewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _viewModel.authenticationState.observe(this, {

            when (it) {
                AuthenticationState.AUTHENTICATED -> {
                    // If the user was authenticated, send her to RemindersActivity
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                AuthenticationState.UNAUTHENTICATED -> {
                    binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

                    // Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
                    binding.loginBtn.setOnClickListener {
                        Log.d(TAG, "::::::: Launching signin flow :::::::")
                        launchSignInFlow()
                    }
                }

                else -> {
                    Log.e(TAG,"::::::: $it doesn't require any UI change :::::::" )
                }
            }

        })

        // A bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()

            // This is where you can provide more ways for users to register and
            // sign in.
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE
        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build(),
            SIGN_IN_REQUEST_CODE
        )
        Log.d(TAG, "::::::: launched signin intent :::::::")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(TAG, "::::::: Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}! :::::::")
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "::::::: Sign in unsuccessful ${response?.error?.errorCode} :::::::")
                return
            }
        }
    }

    companion object {
        private const val TAG="AuthenticationActivity"
        private const val SIGN_IN_REQUEST_CODE = 222
    }
}
