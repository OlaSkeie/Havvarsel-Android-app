package no.uio.ifi.in2000.prosjekt.ui.CommonUIUtils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.prosjekt.R

@Composable
fun DecideWeatherIcon(icon: String, size: Int, padding: Int){
// Android studio discourages use of get R.drawable by string-name, so we choose to do it manually
    val drawableResourceId = when (icon){
        "clearsky_day" -> R.drawable.clearskyd
        "clearsky_night" -> R.drawable.clearskyn
        "fair_day" -> R.drawable.lightcloudd
        "fair_night" -> R.drawable.lightcloudn
        "partlycloudy_day" -> R.drawable.partlycloudyd
        "partlycloudy_night" -> R.drawable.partlycloudyn
        "cloudy" -> R.drawable.cloudy
        "rainshowers_day" -> R.drawable.rainshowersd
        "rainshowers_night" -> R.drawable.rainshowersn
        "rainshowersandthunder_day" -> R.drawable.rainshowersandthunderd
        "rainshowersandthunder_night" -> R.drawable.rainshowersandthundern
        "rain" -> R.drawable.rain
        "heavyrain" -> R.drawable.heavyrain
        "heavyrainandthunder" -> R.drawable.heavyrainandthunder
        "fog" -> R.drawable.fog
        "rainandthunder" -> R.drawable.rainandthunder
        "lightrainshowersandthunder_day" -> R.drawable.lightrainshowersandthunderd
        "lightrainshowersandthunder_night" -> R.drawable.lightrainshowersandthundern
        "heavyrainshowersandthunder_day" -> R.drawable.heavyrainshowersandthunderd
        "heavyrainshowersandthunder_night" -> R.drawable.heavyrainshowersandthundern
        "lightrainandthunder" -> R.drawable.lightrainandthunder
        "lightrainshowers_day" -> R.drawable.lightrainshowersd
        "lightrainshowers_night" -> R.drawable.lightrainshowersn
        "heavyrainshowers_day" -> R.drawable.heavyrainshowersd
        "heavyrainshowers_night" -> R.drawable.heavyrainshowersn
        "lightrain" -> R.drawable.lightrain
        else -> R.drawable.cloudy
        // Set the rest to cloud since there are no more snow/sleet we dont include them, however they can be implemented in the future
    }
    Image(
        painter = painterResource(drawableResourceId),
        contentDescription = "Thunderstorm Icon",
        modifier = Modifier
            .size(size.dp)
            .padding(top = padding.dp)
    )
}