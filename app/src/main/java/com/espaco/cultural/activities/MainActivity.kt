package com.espaco.cultural.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.espaco.cultural.R
import com.espaco.cultural.activities.fragments.HomeFragment
import com.espaco.cultural.activities.fragments.SettingsFragment
import com.espaco.cultural.activities.fragments.VisitsFragment
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.database.preferences.SettingsPreferences
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var settingsPreferences: SettingsPreferences
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)
        binding.navigationView.setNavigationItemSelectedListener(this)

        userPreferences = UserPreferences(this)
        settingsPreferences = SettingsPreferences(this)
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
}