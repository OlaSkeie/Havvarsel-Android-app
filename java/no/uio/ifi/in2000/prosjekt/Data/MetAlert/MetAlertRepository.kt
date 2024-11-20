package no.uio.ifi.in2000.prosjekt.Data.MetAlert

import no.uio.ifi.in2000.prosjekt.Model.MetAlerts

class MetAlertRepository {
    private val metAlertDataSrc = MetAlertDataSource()
    suspend fun getMetAlert(): MetAlerts? {
        return metAlertDataSrc.getMetAlerts()
    }

    suspend fun getMetAlertCoordinates(lat:String, lon:String): MetAlerts? {
        return metAlertDataSrc.getMetAlertsCoordinates(lat, lon)
    }
}