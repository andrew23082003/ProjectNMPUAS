package com.blinc.healingapps

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.blinc.healingapps.databinding.ActivityMainBinding
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)

        if (sharedPref.getInt("user_id", -1) == -1) {
            val intent = Intent(this, ActivityLogin::class.java)
            startActivity(intent)
            finish()
            return
        }

        setupDrawer()
        setupViewPager()
        setupNavigation()
    }

    private fun setupDrawer() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Yuk, Healing!"

        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )

        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val headerView = binding.navigationView.getHeaderView(0)
        val tvWelcomeUser = headerView.findViewById<TextView>(R.id.tvWelcomeUser)
        tvWelcomeUser.text = "Welcome, ${sharedPref.getString("user_name", "User")}"
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewPager() {
        val fragments = listOf(
            ExploreFragment.newInstance(),
            FavoriteFragment.newInstance(),
            ProfileFragment.newInstance()
        )

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNavigation.menu.getItem(position).isChecked = true
            }
        })

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.nav_favourite -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.nav_profile -> {
                    binding.viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigation() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_change_password -> {
                    navigateToChangePassword()
                    true
                }
                R.id.nav_sign_out -> {
                    handleSignOut()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToChangePassword() {
        val intent = Intent(this, ActivityChangePassword::class.java)
        startActivity(intent)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun handleSignOut() {
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        val intent = Intent(this, ActivityLogin::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}