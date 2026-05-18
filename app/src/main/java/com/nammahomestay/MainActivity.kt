package com.nammahomestay

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.nammahomestay.databinding.ActivityMainBinding
import com.nammahomestay.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemSelectedListener { item ->
            handleBottomNav(item, bottomNav)
        }

        handleAutoNavigate()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.otpFragment,
                R.id.roleSelectionFragment -> {
                    bottomNav?.visibility = View.GONE
                    supportActionBar?.hide()
                }
                else -> {
                    val role = sessionManager.getUserRole()
                    if (role == "host") {
                        bottomNav?.visibility = View.VISIBLE
                        bottomNav?.menu?.clear()
                        bottomNav?.inflateMenu(R.menu.bottom_nav_host)
                    } else if (role == "guest") {
                        bottomNav?.visibility = View.VISIBLE
                        bottomNav?.menu?.clear()
                        bottomNav?.inflateMenu(R.menu.bottom_nav_guest)
                    } else if (role == "admin") {
                        bottomNav?.visibility = View.VISIBLE
                        bottomNav?.menu?.clear()
                        bottomNav?.inflateMenu(R.menu.bottom_nav_admin)
                    } else {
                        bottomNav?.visibility = View.GONE
                    }
                    supportActionBar?.show()
                }
            }
        }
    }

    private fun handleAutoNavigate() {
        val role = intent?.getStringExtra("auto_role")
        if (!role.isNullOrBlank()) {
            val dest = when (role) {
                "guest" -> R.id.guestHomeFragment
                "host" -> R.id.hostDashboardFragment
                "admin" -> R.id.adminPanelFragment
                else -> return
            }
            navController.navigate(dest)
        }
    }

    private fun handleBottomNav(item: MenuItem, bottomNav: BottomNavigationView): Boolean {
        return when (item.itemId) {
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                sessionManager.clearSession()
                navController.navigate(R.id.splashFragment)
                true
            }
            R.id.guestHomeFragment -> {
                navController.navigate(R.id.guestHomeFragment)
                true
            }
            R.id.hostDashboardFragment -> {
                navController.navigate(R.id.hostDashboardFragment)
                true
            }
            R.id.adminPanelFragment -> {
                navController.navigate(R.id.adminPanelFragment)
                true
            }
            R.id.inquiriesFragment -> {
                navController.navigate(R.id.inquiriesFragment)
                true
            }
            R.id.dailyMenuFragment -> {
                navController.navigate(R.id.dailyMenuFragment)
                true
            }
            else -> {
                navController.navigate(item.itemId)
                true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
