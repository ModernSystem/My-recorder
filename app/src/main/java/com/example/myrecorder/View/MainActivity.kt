package com.example.myrecorder.View
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myrecorder.Controler.RecordModifierListDialogFragment
import com.example.myrecorder.R
import kotlinx.android.synthetic.main.main_activity.*
class MainActivity : AppCompatActivity(), RecordModifierListDialogFragment.Listener {

    private lateinit var appBarConfiguration : AppBarConfiguration



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment? ?: return

        appBarConfiguration= AppBarConfiguration(setOf(R.id.mainFragment),drawer_layout)
        appBarConfiguration = AppBarConfiguration(host.navController.graph)
        setupActionBarWithNavController(host.navController,appBarConfiguration)


    }

    override fun onSupportNavigateUp() =
        findNavController(this, R.id.navHostFragment).navigateUp()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.navHostFragment))
                || super.onOptionsItemSelected(item)

    }

    override fun onRecordModifierClicked(position: Int) {
    }
}



