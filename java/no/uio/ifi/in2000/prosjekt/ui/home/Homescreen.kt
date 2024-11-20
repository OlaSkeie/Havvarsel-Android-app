package no.uio.ifi.in2000.prosjekt.ui.home


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import kotlinx.coroutines.delay
import no.uio.ifi.in2000.prosjekt.R
import no.uio.ifi.in2000.prosjekt.ui.CommonUIUtils.DecideWeatherIcon
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun Logo(){
    Column(
        modifier = Modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo3),
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),

            contentScale = ContentScale.FillWidth
        )
    }
}
@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
fun HomeScreen(activity: Activity, navController: NavController, homeScreenViewModel: HomeScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var text by remember { mutableStateOf("") }
    val weatherState = homeScreenViewModel.locationUIState.collectAsState()
    var showSuggestions by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { RunBottomBar(navController = navController)} // Setter bottomBar i Scaffold
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()) // Gir plass for BottomBar
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF171729)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Logo()
                }

                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF171729).copy(alpha = 0.9f)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        TextField(
                            value = text,
                            onValueChange = { newValue ->
                                text = newValue
                                showSuggestions = newValue.isNotEmpty()
                                if (newValue.isNotEmpty()) {
                                    homeScreenViewModel.fetchSuggestions(newValue)
                                }
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Location",
                                    modifier = Modifier.clickable {
                                        getLocation(context, activity, homeScreenViewModel)
                                    }
                                )
                            },
                            placeholder = { Text("Skriv her") },
                            modifier = Modifier
                                .fillMaxWidth(0.9f),
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                            colors = TextFieldDefaults.colors( // Få bort rar linje under søkebar
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
                item{
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item{
                    if(weatherState.value.locationCombined != null){
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                                //.background(Color.Red),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Spacer(
                                modifier = Modifier.width(16.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.mylocation),
                                contentDescription = "minLokasjon",
                            )
                            Spacer(
                                modifier = Modifier.width(16.dp)
                            )
                            Text(
                                text = "Min posisjon",
                                color = Color.White,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ))
                        }

                        WeatherBox(
                            location = weatherState.value.locationCombined?.first ?: "",
                            combinedWeatherData = weatherState.value.locationCombined?.second,
                            navController = navController,true)
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            //.background(Color.Blue)
                            .height(40.dp),

                        verticalAlignment = Alignment.Bottom
                    ){
                        Spacer(
                            modifier = Modifier.width(16.dp)
                        )
                            Image(
                                modifier = Modifier
                                    .padding(bottom = 6.dp),
                                painter = painterResource(id = R.drawable.star2),
                                contentDescription = "minLokasjon",
                            )

                            Spacer(
                                modifier = Modifier.width(16.dp)
                            )

                            Text(
                                text = "Favoritter",
                                modifier = Modifier
                                    .padding(bottom = 6.dp),
                                color = Color.White,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ))
                            Spacer(modifier = Modifier.weight(1f))  // Dette presser alt annet til venstre
                            AddIconButton(homeScreenViewModel)
                            Spacer(modifier = Modifier.width(16.dp))

                    }
                }

                items(weatherState.value.combinedDataMap.entries.toList()) { (locationKey, weatherData) ->
                    key(locationKey) {
                        CustomSwipeToDeleteContainer(
                            item = locationKey,
                            onDelete = { location ->
                                homeScreenViewModel.deleteLocation(location)
                                homeScreenViewModel.triggerSaveState(context, homeScreenViewModel.locationUIState.value)
                            },
                            content = { location ->
                                WeatherBox(
                                    location = location,
                                    combinedWeatherData = weatherData,
                                    navController = navController,
                                    false)
                            }
                        )
                    }
                }
            }
            myPopup(homeScreenViewModel = homeScreenViewModel)

            if (showSuggestions) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF171729).copy(alpha = 1f))
                        .clickable {
                            showSuggestions = false
                            text = ""
                            keyboardController?.hide()
                            homeScreenViewModel.clearSuggestions()
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = text,
                            onValueChange = { newValue ->
                                text = newValue
                                showSuggestions = newValue.isNotEmpty()
                                if (newValue.isNotEmpty()) {
                                    homeScreenViewModel.fetchSuggestions(newValue)
                                }
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Location",
                                    modifier = Modifier.clickable {
                                        getLocation(context, activity, homeScreenViewModel)
                                    }
                                )
                            },
                            label = { Text("Skriv her") },
                            modifier = Modifier
                                .fillMaxWidth(0.9f),
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 80.dp)
                            .fillMaxWidth(0.8f)
                            .background(Color(0xFFCFE3F3))
                            .heightIn(max = 250.dp)
                            .align(Alignment.TopCenter)
                    ) {

                        items(weatherState.value.suggestion ?: emptyList()) { suggestion ->
                            Text(
                                text = suggestion.properties.label,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        text = suggestion.properties.label
                                        showSuggestions = false
                                        homeScreenViewModel.clearSuggestions()
                                        val coordinates = suggestion.geometry.coordinates
                                        val coordinateString =
                                            "${coordinates[1]}, ${coordinates[0]}"
                                        navController.navigate("infoStederScreen/$coordinateString/$text")
                                    }
                                    .padding(8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
private const val REQUEST_LOCATION_PERMISSION_CODE = 0

fun getLocation(
    context: Context,
    activity: Activity,
    homeScreenViewModel: HomeScreenViewModel) {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    if ((ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            100
        )
    } else {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                homeScreenViewModel.fetchLocationWeatherData(Pair(location?.latitude.toString(), location?.longitude.toString()), )
            }
        return
    }
}

@Composable
@ExperimentalMaterial3Api
fun CustomSwipeToDismiss(
    onSwiped: () -> Unit,
    onDeleted: () -> Unit,
    background: @Composable RowScope.() -> Unit,
    dismissContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSwiped by remember {
        mutableStateOf(false)
    }

    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "")
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenWidthPx = with(LocalDensity.current) { screenWidth.dp.toPx() }

    Box(
        modifier.pointerInput(Unit) {

            detectHorizontalDragGestures(
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()

                    // If the background is revealed, allow the user to swipe it back out to the right
                    if (dragAmount > 0f) {
                        if (isSwiped && offsetX < 0f) {
                            if ((offsetX + dragAmount) > 0f) {
                                offsetX = 0f
                            } else {
                                offsetX += dragAmount
                            }
                        }
                        return@detectHorizontalDragGestures
                    }

                    // Store swipe offset from right to left
                    offsetX += dragAmount
                },
                onDragEnd = {
                    val absOffsetX = abs(offsetX) // compare using positive value

                    // If the user has dragged the item more than half of the screen width, delete it
                    if (absOffsetX >= screenWidthPx / 2 && absOffsetX < screenWidthPx) {
                        offsetX = -(screenWidthPx)
                        onDeleted()
                    }

                    // If the user has dragged the item more than a fifth of the screen width, swipe it to reveal background
                    else if (absOffsetX >= screenWidthPx / 5 && absOffsetX < screenWidthPx / 2) {
                        offsetX = -(60.dp.toPx())
                        isSwiped = true
                        onSwiped()
                    } else {
                        offsetX = 0f
                    }
                }
            )
        }
    ) {
        Row(
            content = background, modifier = Modifier
                .matchParentSize()
                .clickable {
                    if (isSwiped) {
                        offsetX = -(screenWidthPx)
                        onDeleted()
                    }
                }
        )
        Row(content = dismissContent,
            modifier = Modifier.offset {
                IntOffset(animatedOffset.roundToInt(), 0)
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomSwipeToDeleteContainer(
    item: T, onDelete: (T) -> Unit, animationDuration: Int = 500, content: @Composable (T) -> Unit
) {

    var isRemoved by remember {
        mutableStateOf(false)
    }
    var isActive by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            delay(animationDuration.toLong())
            onDelete(item)
        }
    }

    AnimatedVisibility(
        visible = !isRemoved, exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration), shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        CustomSwipeToDismiss(
            onSwiped = {
                isActive = true
            },
            onDeleted = {
                isRemoved = true
            },
            background = {
                CustomDeleteBackground()
            },
            dismissContent = { content(item) },
        )
    }
}
@Composable
fun CustomDeleteBackground(
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(1f)
            .height(80.dp), // Setter akkurat likt som WeatherBox, slik at den røde bakgrunnsfargen ikke er synlig når brukeren ikke swiper.
        colors = CardDefaults.cardColors(Color.Red)
    ) {
        // Lager en Box for å plassere ikonet på høyre siden.
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .fillMaxSize() // Fyll hele Card
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Slett",
                modifier = Modifier
                    .padding(16.dp), // Padding inne i Box
                tint = Color.White
            )
        }
    }
}

@Composable
fun WeatherBox(location: String?,
               combinedWeatherData: CombinedWeatherData?,
               navController: NavController,
               sjekkMinPosisjon:Boolean) {

    val sted = combinedWeatherData?.bigDataCloud?.city
    val placeName = combinedWeatherData?.enTurLocationName
    val placeNameSplit = placeName?.split(",")

    val containerColor = if (sjekkMinPosisjon && placeNameSplit != null) Color.Transparent else Color(0xFFCFE3F3).copy(alpha = 0.9f)
    Box( modifier = Modifier
        .fillMaxWidth(1f)
        .background(Color(0xFF171729))
    ){
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(1f)
                .heightIn(min = 80.dp)
                .clickable {
                    navController.navigate("infoStederScreen/${location}/${placeName}")
                },
            colors = CardDefaults.cardColors(containerColor = containerColor),
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = 80.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DecideWeatherIcon(icon = combinedWeatherData?.weatherData?.properties?.timeseries?.get(2)?.data?.next_1_hours?.summary?.get("symbol_code") ?: "",
                    size = 50,
                    padding = 8)
                Column(modifier = Modifier
                    .weight(0.27f)
                    .padding(10.dp)) {
                    if (sjekkMinPosisjon && placeNameSplit != null ){
                        Text(
                            text = "Min posisjon",
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }else{
                        if (placeNameSplit != null){
                            Text(
                                text = if(placeNameSplit.size > 1)"" + (placeNameSplit[1]) else "Sted",
                                color = Color.Black,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (sjekkMinPosisjon && placeNameSplit != null){
                        Text(
                            text = placeNameSplit[0],
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                        )
                    }else{
                        Text(
                            text = placeNameSplit?.get(0) ?: "",
                            color = Color.Black,
                            style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                        )
                    }
                }

                val airTemperature = combinedWeatherData?.weatherData?.properties?.timeseries?.get(2)?.data?.instant?.details?.get("air_temperature")
                // Spacer(modifier = Modifier.weight(1f)) // Legger til horisontalt mellomrom
                Column(modifier = Modifier
                    .weight(0.21f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (sjekkMinPosisjon){
                        Text(
                            text = "Lufttemp:",
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${airTemperature}°C",
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                            )
                        )
                    }else{
                        Text(
                            text = "Lufttemp:",
                            color = Color.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${airTemperature}°C",
                            color = Color.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                            )
                        )
                    }

                }

                val seaTemperature = combinedWeatherData?.dataProjectionMain?.data?.get(0)?.data?.find{it.key == "temperature"}?.value
                val doubleSeaTemperature = seaTemperature?.toDouble()
                val roundSeaTemperature = doubleSeaTemperature?.let{ round(it)}
                Column(modifier = Modifier
                    .weight(0.21f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if(sjekkMinPosisjon){
                        Text(
                            text = "Sjøtemp:",
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${roundSeaTemperature}°C",
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                        )
                    }else{
                        Text(
                            text = "Sjøtemp:",
                            color = Color.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${roundSeaTemperature}°C",
                            color = Color.Black,
                            style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                        )
                    }

                }

                val wind = combinedWeatherData?.weatherData?.properties?.timeseries?.get(2)?.data?.instant?.details?.get("wind_speed")
                val doubleWind = wind?.toDouble()
                val roundWind = doubleWind?.let{ round(it)}
                Column(modifier = Modifier
                    .weight(0.21f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (sjekkMinPosisjon){
                        Text(
                            text = "Vind:",
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            )
                        Text(
                            text = "${roundWind} m/s",
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                        )
                    }else{
                        Text(
                            text = "Vind:",
                            color = Color.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),

                            )
                        Text(
                            text = "${roundWind} m/s",
                            color = Color.Black,
                            style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddIconButton(homeScreenViewModel: HomeScreenViewModel){

        IconButton(
            onClick = { homeScreenViewModel.toggleVisibility() },
            modifier = Modifier
                .size(30.dp)
                //.background(Color.Black)
        ){
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Legg til",
                tint = Color.White
            )
        }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun myPopup(homeScreenViewModel: HomeScreenViewModel){
    val context = LocalContext.current
    val visible by homeScreenViewModel.isPopupVisible.collectAsState()
    var newLocationName by remember{ mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val locationUIState by homeScreenViewModel.locationUIState.collectAsState()

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Card (
                shape = RectangleShape,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171729))
            ){
                Row(
                    modifier = Modifier
                ) {
                    IconButton(
                        onClick = {
                            homeScreenViewModel.clearSuggestions()
                            newLocationName = ""
                            homeScreenViewModel.toggleVisibility()
                        }
                    )
                    {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Tilbake",
                            modifier = Modifier.size(40.dp), // Juster størrelsen etter behov
                            tint = Color.White // Juster fargen etter ønske
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center ,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    Column(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TextField(
                            value = newLocationName,
                            onValueChange = {newLocationName = it
                                homeScreenViewModel.fetchSuggestions(it)
                                if (newLocationName.isNotEmpty()){
                                    homeScreenViewModel.clearSuggestions()
                                }

                            },
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newLocationName.isNotEmpty()){
                                    keyboardController?.hide()
                                }
                            }
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.9f),
                            label = { Text("Legg til favorittsted!")},
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )

                        )
                        Column(
                            modifier = Modifier
                                .verticalScroll(scrollState)
                                .padding(3.dp)


                        ) {
                            locationUIState.suggestion?.forEach{suggestion->
                                Text(
                                    text = suggestion.properties.label,
                                    modifier = Modifier
                                        .background(Color(0xFFCFE3F3))
                                        .fillMaxWidth(0.8f)
                                        .clickable {
                                            newLocationName = suggestion.properties.label
                                            homeScreenViewModel.addLocationByName(
                                                newLocationName, context
                                            )
                                            homeScreenViewModel.clearSuggestions()
                                            homeScreenViewModel.toggleVisibility()
                                            newLocationName = ""
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}