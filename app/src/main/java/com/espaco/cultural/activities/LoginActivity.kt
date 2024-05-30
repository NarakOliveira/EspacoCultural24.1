package com.espaco.cultural.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.espaco.cultural.R
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.database.exceptions.DatabaseException
import com.espaco.cultural.database.exceptions.InvalidPasswordException
import com.espaco.cultural.database.exceptions.UserNotFoundException
import com.espaco.cultural.database.preferences.SettingsPreferences
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.ActivityLoginBinding
import com.espaco.cultural.services.NotificationService
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPreferences = UserPreferences(this)
        val settingsPreferences = SettingsPreferences(this)

        if (userPreferences.isLogged()) {
            if (settingsPreferences.needPassword) binding.registrationEditText.setText(userPreferences.registration)
            else nextScreen()
        }

        binding.registrationEditText.addTextChangedListener {
            binding.registrationInputLayout.error = null
        }

        binding.passwordEditText.addTextChangedListener {
            binding.passwordInputLayout.error = null
        }

        binding.signInButton.setOnClickListener {
            val registration = binding.registrationEditText.text.toString()
            val password= binding.passwordEditText.text.toString()

            if (registration.isEmpty() || registration.isBlank()) {
                binding.registrationInputLayout.error = "Informe sua matrícula"
                binding.registrationEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.isBlank()) {
                binding.passwordInputLayout.error = "Informe sua senha"
                binding.passwordEditText.requestFocus()
                return@setOnClickListener
            }

            val progressBar = ProgressBar(this)
            progressBar.isIndeterminate = true

            val alertDialog = AlertDialog.Builder(this, R.style.TransparentDialog)
                .setView(progressBar)
                .create()

            alertDialog.show()

            UserDB.login(registration, password) { user, error->
                alertDialog.dismiss()

                if (error != null) {
                    try {
                        throw error
                    } catch (ignore: UserNotFoundException) {
                        binding.registrationInputLayout.error = "Matrícula invalida"
                        binding.registrationEditText.requestFocus()
                    } catch (ignore: InvalidPasswordException) {
                        binding.passwordInputLayout.error = "Senha invalida"
                        binding.passwordEditText.requestFocus()
                    } catch (ignore: DatabaseException) {
                        showErrorMessage(it, "Erro ao tentar realizar o login")
                    }

                    return@login
                }

                if (user != null) {
                    userPreferences.registration = user.registration
                    userPreferences.name = user.name
                    userPreferences.picture = user.picture
                    userPreferences.isAdmin = user.isAdmin
                    nextScreen()
                } else {
                    showErrorMessage(it, "Ocorreu um erro inexperado")
                }
            }
        }
    }

    private fun showErrorMessage(view: View, message: String) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

        Snackbar.make(binding.signInButton, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.red_500))
            .show()
    }

    private fun nextScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !notificationServiceRunning()) {
            val service = Intent(this, NotificationService::class.java)
            startForegroundService(service)
        }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun notificationServiceRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getRunningServices(Int.MAX_VALUE).forEach {
            if (NotificationService::class.java.name.equals(it.service.className)) {
                return true
            }
        }
        return false
    }
}
