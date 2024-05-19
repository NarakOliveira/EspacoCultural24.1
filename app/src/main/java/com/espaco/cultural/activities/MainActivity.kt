package com.espaco.cultural.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.espaco.cultural.R
import com.espaco.cultural.activities.fragments.FullArtWorkFragment
import com.espaco.cultural.activities.fragments.HomeFragment
import com.espaco.cultural.activities.fragments.SettingsFragment
import com.espaco.cultural.activities.fragments.VisitsFragment
import com.espaco.cultural.database.ArtWorkDB
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.database.preferences.SettingsPreferences
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var settingsPreferences: SettingsPreferences
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestNotificationPermissions()


        setSupportActionBar(binding.materialToolbar)
        binding.navigationView.setNavigationItemSelectedListener(this)

        userPreferences = UserPreferences(this)
        settingsPreferences = SettingsPreferences(this)
        if (settingsPreferences.newExposition) Firebase.messaging.subscribeToTopic("exposition")
        else Firebase.messaging.unsubscribeFromTopic("exposition")

        updateCacheData()

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.materialToolbar, R.string.nav_open, R.string.nav_close)

        toggle.drawerArrowDrawable.color = Color.WHITE
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        updateUserInfo()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment()).commit()

            binding.navigationView.setCheckedItem(R.id.nav_home)
        }

        if (intent.action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            val rawMessages = getNfcMessage()
            if (rawMessages != null) {
                val value = String((rawMessages[0] as NdefMessage).records[0].payload)
                ArtWorkDB.findArtWork(value) {
                    if (it != null) {
                        val f = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                        if (f is FullArtWorkFragment) return@findArtWork
                        val ldf = FullArtWorkFragment()
                        val args = Bundle()
                        args.putString("title", it.title)
                        args.putString("autor", it.autor)
                        args.putString("description", it.description)
                        args.putString("image", it.image)
                        ldf.setArguments(args)

                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                            .replace(R.id.fragmentContainer, ldf)
                            .commit()
                    } else {
                        Toast.makeText(this, "Obra não encontrada!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment()).commit()
            }

            R.id.nav_visits -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, VisitsFragment()).commit()
            }

            R.id.nav_settings-> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, SettingsFragment()).commit()
            }

            R.id.nav_logout -> {
                AlertDialog.Builder(this)
                    .setTitle("Deconectar conta")
                    .setMessage("Tem certeza que deseja se desconectar deste dispositivo?")
                    .setPositiveButton("Desconectar") { _, _ ->
                        userPreferences.clear()
                        settingsPreferences.clear()

                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val f = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (f is HomeFragment) {
                if (System.currentTimeMillis() - lastClickTime >= 2000) {
                    Toast.makeText(this, "Pressione novamente para sair", Toast.LENGTH_SHORT).show()
                    lastClickTime = System.currentTimeMillis();
                } else {
                    finishAffinity()
                }
            } else {
                binding.navigationView.setCheckedItem(R.id.nav_home)
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()
            }
        }
    }

    private fun updateCacheData() {
        Thread {
            UserDB.findUser(userPreferences.registration) {
                userPreferences.name = it.name
                userPreferences.picture = it.picture
                userPreferences.isAdmin = it.isAdmin
                updateUserInfo()
            }
        }.start()
    }

    private fun updateUserInfo() {
        val header = binding.navigationView.getHeaderView(0)
        val nameText = header.findViewById<TextView>(R.id.nameText)
        val registrationText = header.findViewById<TextView>(R.id.registrationText)
        val userImage = header.findViewById<ImageView>(R.id.userImage)

        nameText.text = userPreferences.name
        registrationText.text = userPreferences.registration

        Glide.with(this)
            .load(userPreferences.picture)
            .error(R.drawable.no_user_picture)
            .into(userImage)
    }

    private fun getNfcMessage(): Array<out Parcelable>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, Parcelable::class.java)
        } else {
            return intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        }
    }

    private fun requestNotificationPermissions() {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)  {
           val hasPermission = ContextCompat.checkSelfPermission(
               this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

           if (!hasPermission) {
               ActivityCompat.requestPermissions(
                   this,
                   arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                   0)
           }
       }
    }
}