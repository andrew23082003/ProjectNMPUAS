package com.blinc.healingapps

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.blinc.healingapps.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    companion object {
        private const val SPLASH_DELAY = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        setupAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToLogin()
        }, SPLASH_DELAY)
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.ivLogo.startAnimation(fadeIn)

        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.tvAppName.startAnimation(slideUp)

        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        binding.progressBar.startAnimation(rotateAnimation)
    }


    private fun navigateToLogin() {
        val intent = Intent(this, ActivityLogin::class.java)
        startActivity(intent)

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}