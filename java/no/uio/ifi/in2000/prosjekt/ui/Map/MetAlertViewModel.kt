package no.uio.ifi.in2000.prosjekt.ui.Map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.prosjekt.Data.MetAlert.MetAlertRepository
import no.uio.ifi.in2000.prosjekt.Model.MetAlerts

data class MetAlersUiState(
    val MetAlerts: MetAlerts? = MetAlerts(),
    val MetAlertsCoordinate: MetAlerts? = MetAlerts()
)
class MetAlertViewModel: ViewModel() {
    private val metRepo : MetAlertRepository = MetAlertRepository()
    private val _metAUiState = MutableStateFlow(MetAlersUiState())
    val metAUiState: StateFlow<MetAlersUiState> = _metAUiState.asStateFlow()

    private val _isPopupVisible = MutableStateFlow(false)
    val isPopupVisible: StateFlow<Boolean> = _isPopupVisible.asStateFlow()


    init {
        getNewData()
    }

    /* Gets new alert data for all the marine alerts along the norwegian coast */
    fun getNewData(){
        viewModelScope.launch {
            _metAUiState.update {
                it.copy(
                    MetAlerts = metRepo.getMetAlert()
                )
            }
        }
    }
    /* Gets new alert data for a given place in Norway based on coordinates, both marine and land */
    fun getNewDataCoords(lat : String, lon: String){
        viewModelScope.launch {
            _metAUiState.update {
                it.copy(
                    MetAlertsCoordinate = metRepo.getMetAlertCoordinates(lat, lon)
                )
            }
        }
    }

    fun togglePopupVisibility() {
        _isPopupVisible.value = !_isPopupVisible.value
    }
}