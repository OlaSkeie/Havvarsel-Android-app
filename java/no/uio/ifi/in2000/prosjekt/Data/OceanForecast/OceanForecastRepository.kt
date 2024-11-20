package no.uio.ifi.in2000.prosjekt.Data.OceanForecast

import no.uio.ifi.in2000.prosjekt.Model.OceanForecastData

class OceanForecastRepository{
    private val oceanForecastDataSrc = OceanForecastDataSource()

    suspend fun getOceanForecast(lat : String, lon : String): OceanForecastData? {
        return oceanForecastDataSrc.getOceanForecast(lat, lon)
    }
}