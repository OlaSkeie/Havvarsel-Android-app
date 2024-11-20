package no.uio.ifi.in2000.prosjekt

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.prosjekt.ui.InfoCards.InfoCards
import no.uio.ifi.in2000.prosjekt.ui.InfoScreen.InfoScreen
import no.uio.ifi.in2000.prosjekt.ui.InstructionManual.HomeScreenManuals
import no.uio.ifi.in2000.prosjekt.ui.InstructionManual.InstructionManualScreen
import no.uio.ifi.in2000.prosjekt.ui.InstructionManual.MapUserManualScreen
import no.uio.ifi.in2000.prosjekt.ui.Map.DetailedWeatherMapScreen
import no.uio.ifi.in2000.prosjekt.ui.Map.MapScreen
import no.uio.ifi.in2000.prosjekt.ui.home.HomeScreen
import no.uio.ifi.in2000.prosjekt.ui.infoSteder.InfoStederScreen
import no.uio.ifi.in2000.prosjekt.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HavvarselApp( this)
                }
            }
        }
    }
}




@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HavvarselApp(activity: Activity) {
    MyApplicationTheme {
        val navController = rememberNavController()

        NavHost(navController, startDestination = "hjem") {
            composable("hjem") {
                HomeScreen(activity, navController)
            }
            composable("infoStederScreen/{cordinates}/{sted}") { backStackEntry ->
                InfoStederScreen(
                    coordinate = backStackEntry.arguments?.getString("cordinates") ?: "59.15,10.75",
                    place = backStackEntry.arguments?.getString("sted") ?: "Oslo",
                    navController
                )
            }
            composable("Kart") {
                MapScreen(activity, navController)
            }
            composable("Lær") {
                InfoScreen(navController = navController)
            }
            composable("InfoCard/{id}") { backStackEntry ->
                InfoCards(id = backStackEntry.arguments?.getString("id"), navController)
            }
            composable("Manual"){
                InstructionManualScreen(navController)
            }
            composable("DetailedWeather/{coordinates}"){backStackEntry ->
                DetailedWeatherMapScreen(navController, coordinate = backStackEntry.arguments?.getString("coordinates") ?: "59.15,10.75",
                )
            }
            composable("InstructionCard/{id}"){backStackEntry ->
                     run {
                        val id = backStackEntry.arguments?.getString("id")
                        if (id == "0") {
                            HomeScreenManuals(navController = navController)
                        }
                         else{
                            MapUserManualScreen(navController)
                        }
                    }
        }
    }
    }
}
