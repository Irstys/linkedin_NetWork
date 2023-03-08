package ru.netology.linkedin_network.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.auth.AppAuth
import ru.netology.linkedin_network.viewmodel.AuthViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.linkedin_network.BuildConfig.MAPS_API_KEY
import ru.netology.linkedin_network.databinding.ActivityMainBinding
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AppActivity : AppCompatActivity(){

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    private lateinit var binding: ActivityMainBinding

    private val viewModel: AuthViewModel by viewModels()


    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(MAPS_API_KEY)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.color.blue))

        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.postFeedFragment,
                R.id.eventFeedFragment,
                R.id.contactsFragment,
                R.id.userProfileFragment -> navView.visibility =
                    View.VISIBLE
                else -> navView.visibility = View.GONE
            }
        }

        val appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController,appBarConfiguration)
        navView.setupWithNavController(navController)



        navView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.postFeedFragment  -> {
                    navController.navigate(R.id.postFeedFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.eventFeedFragment  -> {
                    navController.navigate(R.id.eventFeedFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.contactsFragment  -> {
                    navController.navigate(R.id.contactsFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.userProfileFragment -> {
                    if (!viewModel.authenticated) {
                        navController.navigate(R.id.signInFragment)
                    } else {
                        navController.navigate(R.id.userProfileFragment)
                    }
                }
            }
            return@setOnItemSelectedListener false
        }
        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }


        checkGoogleApiAvailability()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sign, menu)

        menu.let {
            it.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
            it.setGroupVisible(R.id.authenticated, viewModel.authenticated)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.navHost).navigate(R.id.postFeedFragment)
                true
            }
            R.id.signin -> {
                findNavController(R.id.navHost).navigate(R.id.signInFragment)
                true
            }
            R.id.signup -> {
                findNavController(R.id.navHost).navigate(R.id.registerFragment)
                true
            }
            R.id.signout -> {
                appAuth.removeAuth()
                findNavController(R.id.navHost).navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(
                this@AppActivity,
                R.string.google_play_unavailable,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHost)
        return navController.navigateUp(appBarConfiguration)
    }
}
