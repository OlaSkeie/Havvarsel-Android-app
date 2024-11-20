package no.uio.ifi.in2000.prosjekt.ui.Map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.prosjekt.Data.Havvarsel.HavvarselRepository
import no.uio.ifi.in2000.prosjekt.Model.DataProjectionMain
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/* Data class to hold UI state from api */
data class DataUIStateHav(
    val dataProjectionMain: DataProjectionMain? = DataProjectionMain()
)
class MapScreenViewModel: ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val havvarselRepository: HavvarselRepository = HavvarselRepository()

    private val _HVUIState = MutableStateFlow(DataUIStateHav())
    val HVUIState: StateFlow<DataUIStateHav> = _HVUIState.asStateFlow()


    /* Calculate current time and +1 hour for API-request for this instant */
    private var currentDateTime = Instant.now()
    private val norwegianTimeZone = ZoneId.of("Europe/Oslo")
    private val norwegianDateTime = currentDateTime.atZone(norwegianTimeZone).toLocalDateTime()
    private val roundedDateTime = norwegianDateTime.withMinute(0).withSecond(0).withNano(0)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    private val norwegianNowDateTime = roundedDateTime
    private val formattedNowDateTime = formatter.format(norwegianNowDateTime)
    private val norwegianPlusOneDateTime = roundedDateTime.plusHours(1)
    private val formattedPlusOneDateTime = formatter.format(norwegianPlusOneDateTime)

    /* Get new data from API */
    fun getNewData(lat : String, lon : String){
        currentDateTime = Instant.now() // Oppdaterer nåværende tid
        toggleLoading()
        viewModelScope.launch {
            _HVUIState.update {
                it.copy(
                    dataProjectionMain = havvarselRepository.getHavvarselDataProjection(listOf("temperature", "salinity","wind_direction","wind_length","current_direction","current_length"), lon, lat, "0", formattedNowDateTime, formattedPlusOneDateTime),
                )
            }
            toggleLoading()
        }
    }
    fun toggleLoading() {
        _isLoading.value = !_isLoading.value
    }
}
