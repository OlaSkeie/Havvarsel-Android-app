package no.uio.ifi.in2000.prosjekt.ui.home

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import no.uio.ifi.in2000.prosjekt.Data.BigDataCloud.BigDataCloudRepository
import no.uio.ifi.in2000.prosjekt.Data.EnTur.EnTurRepository
import no.uio.ifi.in2000.prosjekt.Data.Havvarsel.HavvarselRepository
import no.uio.ifi.in2000.prosjekt.Data.WeatherForecast.LocationForecastRepository
import no.uio.ifi.in2000.prosjekt.Model.BigDataCloud
import no.uio.ifi.in2000.prosjekt.Model.DataProjectionMain
import no.uio.ifi.in2000.prosjekt.Model.FeaturesEnTur
import no.uio.ifi.in2000.prosjekt.Model.WeatherData
import no.uio.ifi.in2000.prosjekt.ui.home.DataStoreManager.LOCATION_UI_STATE_KEY
import no.uio.ifi.in2000.prosjekt.ui.home.DataStoreManager.dataStore
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object DataStoreManager {
    val LOCATION_UI_STATE_KEY = stringPreferencesKey("location_ui_state")

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Cities")
}
@Serializable
data class CombinedWeatherData(
    val weatherData: WeatherData?,
    val dataProjectionMain: DataProjectionMain?,
    val bigDataCloud: BigDataCloud?,
    val enTurLocationName: String? = null  // Legg til et felt for stedsnavnet fra EnTur API

)
@Serializable
data class LocationUIState(
    //val locationDataMap: Map<String, WeatherData?> = emptyMap(),
    //val seaTempratureMap: Map<String, Temperature?> = emptyMap(),
    val combinedDataMap: Map<String, CombinedWeatherData> = emptyMap(),
    val locationCombined : Pair<String, CombinedWeatherData?>? = null,
    val suggestion: List<FeaturesEnTur>? = emptyList()
)
val json = Json {
    serializersModule = SerializersModule {
        // If you have custom serializers, you can include them here
    }
    ignoreUnknownKeys = true // Useful for backward compatibility
    encodeDefaults = true
}
/*
I am serializing the data class into a JSON string for storing it in datastore
 and then deserializing it back into the same data class.
 */
fun serializeLocationUIState(state: LocationUIState): String {
    return json.encodeToString(LocationUIState.serializer(), state)
}

fun deserializeLocationUIState(serializedData: String): LocationUIState {
    return json.decodeFromString(LocationUIState.serializer(), serializedData)
}
/*
This extension function is used to save the location UI state to DataStore
 */
suspend fun Context.saveLocationUIState(state: LocationUIState) {
    val serializedState = serializeLocationUIState(state)
    dataStore.edit { preferences ->
        preferences[LOCATION_UI_STATE_KEY] = serializedState
    }
}
/*
Loading the location UI state from DataStore
 */
suspend fun Context.loadLocationUIState(): LocationUIState? {
    // This assumes you have an extension function or a utility to read from DataStore and deserialize
    val preferences = dataStore.data.first() // Use .first() to get the current value, be mindful of the thread it's called on
    val serializedState = preferences[LOCATION_UI_STATE_KEY]
    return if (serializedState != null) deserializeLocationUIState(serializedState) else null
}

@RequiresApi(Build.VERSION_CODES.O)
class HomeScreenViewModel(application: Application): AndroidViewModel(application){
    private val locationForecastrepository: LocationForecastRepository = LocationForecastRepository()
    private val havvarselRepository: HavvarselRepository = HavvarselRepository()
    private  val bigDataCloudDataRepository: BigDataCloudRepository = BigDataCloudRepository()
    private val enTurRepository: EnTurRepository = EnTurRepository()
    private val _isPopupVisible = MutableStateFlow(false)
    val isPopupVisible: StateFlow<Boolean> = _isPopupVisible.asStateFlow()


    private val _locationUIstate = MutableStateFlow(LocationUIState())
    val locationUIState: StateFlow<LocationUIState> = _locationUIstate.asStateFlow()

    init {
        viewModelScope.launch {
            val appContext = getApplication<Application>()
            val storedState = appContext.loadLocationUIState()
            if(storedState?.combinedDataMap == null) {
                val locMap = hashMapOf<Pair<String, String>, String?>(
                    Pair("59.911075", "10.748128") to "Oslo",
                    Pair("60.391789", "5.326067") to "Bergen"
                )
                fetchWeatherData(locMap)
            }
            else{
                /*
                Checking if the user have allowed to use their location and if so calling the function
                fetchLocationWeatherData and getting the updated location data
                 */
                getLocationViewModel(appContext) { location ->
                    val lat = location.latitude.toString()
                    val lon = location.longitude.toString()
                    fetchLocationWeatherData(Pair(lat, lon))
                }
                /*
                Going through the stored data and updating all the data from the api
                 */
                val locationDataMap: Map<Pair<String, String>, String?> = storedState.combinedDataMap.map { (key, value) ->
                    val coordinates = key.split(",").let {
                        Pair(it[0].trim(), it[1].trim()) // Ensuring there is no extra whitespace
                    }
                    coordinates to value.enTurLocationName
                }.toMap()
                fetchWeatherData(locationDataMap)


            }
        }
    }
    fun toggleVisibility(){
        _isPopupVisible.value = !_isPopupVisible.value
    }


    fun triggerSaveState(context: Context, state: LocationUIState) {
        viewModelScope.launch {
            context.saveLocationUIState(state)
        }
    }

    fun fetchSuggestions(locationName: String){
        viewModelScope.launch {
            val suggestionsTemp = enTurRepository.getEnTurAPI(locationName)?.features
            Log.d("Suggestions", "Fetched ${suggestionsTemp?.size} suggestions")
            //_suggestionsUIstate.value = suggestionsTemp
            _locationUIstate.update {currenState ->
                currenState.copy(suggestion = suggestionsTemp)
            }

        }
    }

    fun clearSuggestions(){
        viewModelScope.launch {
            _locationUIstate.update {currenState ->
                currenState.copy(suggestion = emptyList())
            }
        }
    }
    /*
    I want the if check to have an empty body here because i dont want to ask for location permissions every time
    they open the app.
     */
    private fun getLocationViewModel(
        context: Context,

        locationCallback: (Location) -> Unit // Callback function to receive location
    ) {
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


        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        /*
                        using a callback here because it is an asyncrounous operation and we dont want to block the main thread
                        If i would return here i will not get the data the same way
                         */
                        locationCallback(location) // Call the callback with location
                    }
                }
            return
        }
    }

    private fun fetchWeatherData(locations: Map<Pair<String,String>, String?>){
        locations.forEach{(location, name) ->
            viewModelScope.launch {
                val weatherData = locationForecastrepository.getLocationForecast(location.first, location.second, null)
                val currentDateTime = Instant.now()
                val norwegianTimeZone = ZoneId.of("Europe/Oslo")
                val norwegianDateTime = currentDateTime.atZone(norwegianTimeZone).toLocalDateTime()
                val roundedDateTime = norwegianDateTime.withMinute(0).withSecond(0).withNano(0)
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

                val norwegianNowDateTime = roundedDateTime
                val formattedNowDateTime = formatter.format(norwegianNowDateTime)
                val norwegianPlusOneDateTime = roundedDateTime.plusHours(1)
                val formattedPlusOneDateTime = formatter.format(norwegianPlusOneDateTime)

                val seaTemperatureAndSalinity = havvarselRepository.getHavvarselDataProjection(listOf("temperature", "salinity"),location.second,location.first,null,formattedPlusOneDateTime,formattedNowDateTime)
                val bigDataCloudData = bigDataCloudDataRepository.getBigDataCloud(location.first, location.second)


                _locationUIstate.update {currentState ->
                    val updatedMap = currentState.combinedDataMap.toMutableMap()
                    val locationKey = "${location.first}, ${location.second}"

                    updatedMap[locationKey] = CombinedWeatherData(weatherData, seaTemperatureAndSalinity, bigDataCloudData, name)
                    currentState.copy(combinedDataMap = updatedMap)
                }
            }
        }
    }
    /*
    I am only fetching the city that you are in for your location.
     */
    fun fetchLocationWeatherData(location: Pair<String,String>) {
        viewModelScope.launch {
            val weatherData = locationForecastrepository.getLocationForecast(
                location.first,
                location.second,
                null
            )
            val currentDateTime = Instant.now()
            val norwegianTimeZone = ZoneId.of("Europe/Oslo")
            val norwegianDateTime = currentDateTime.atZone(norwegianTimeZone).toLocalDateTime()
            val roundedDateTime = norwegianDateTime.withMinute(0).withSecond(0).withNano(0)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

            val norwegianNowDateTime = roundedDateTime
            val formattedNowDateTime = formatter.format(norwegianNowDateTime)
            val norwegianPlusOneDateTime = roundedDateTime.plusHours(1)
            val formattedPlusOneDateTime = formatter.format(norwegianPlusOneDateTime)


            val seaTemperatureAndSalinity = havvarselRepository.getHavvarselDataProjection(
                listOf("temperature", "salinity"),
                location.second,
                location.first,
                null,
                formattedPlusOneDateTime,
                formattedNowDateTime
            )
            val bigDataCloudData =
                bigDataCloudDataRepository.getBigDataCloud(location.first, location.second)
            _locationUIstate.update {currentState ->
                val locationName = bigDataCloudDataRepository.getBigDataCloud(location.first, location.second)
                val locationKey = "${location.first}, ${location.second}"

                val myLoc = Pair(locationKey,CombinedWeatherData(weatherData, seaTemperatureAndSalinity, bigDataCloudData, locationName?.city))

                currentState.copy(locationCombined = myLoc)

            }
        }
    }
    private fun addWeatherData(locationKey: String, weatherData: CombinedWeatherData){
        _locationUIstate.update {currenState->
            val updatedMap = currenState.combinedDataMap.toMutableMap().apply {
                put(locationKey, weatherData)
            }
            currenState.copy(combinedDataMap = updatedMap)
        }
    }

    fun addLocationByName(locationName: String, context: Context){
        viewModelScope.launch {

            val findSuggestion = _locationUIstate.value.suggestion?.find {
                (it.properties.label == locationName)
            }
            val cordinates = findSuggestion?.geometry?.coordinates
            val weatherData = locationForecastrepository.getLocationForecast(cordinates?.get(1).toString(), cordinates?.get(0).toString(), null)

            val nowWithOffset = ZonedDateTime.now(ZoneOffset.of("+01:00"))
            val oneHourLater = nowWithOffset.plusHours(1)
            val formattedOneHourLater = oneHourLater.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val formattedNow = nowWithOffset.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            val seaTemperatureAndSalinity = havvarselRepository.getHavvarselDataProjection(listOf("temperature", "salinity"),cordinates?.get(0).toString(),cordinates?.get(1).toString(),null,formattedOneHourLater,formattedNow)
            val bigDataCloudData = bigDataCloudDataRepository.getBigDataCloud(cordinates?.get(1).toString(), cordinates?.get(0).toString())

            addWeatherData("${cordinates?.get(1)}, ${cordinates?.get(0)}", CombinedWeatherData(weatherData, seaTemperatureAndSalinity, bigDataCloudData, findSuggestion?.properties?.label))
            triggerSaveState(context, locationUIState.value)
        }
    }
    fun deleteLocation(locationCordinate: String){
        _locationUIstate.update {currenState ->
            val updatedMap = currenState.combinedDataMap.toMutableMap().apply {
                remove(locationCordinate)
            }
            currenState.copy(combinedDataMap = updatedMap)
        }
    }
}
