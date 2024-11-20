package no.uio.ifi.in2000.prosjekt.Data.Havvarsel

import no.uio.ifi.in2000.prosjekt.Model.DataProjectionMain
import no.uio.ifi.in2000.prosjekt.Model.Temperature
import no.uio.ifi.in2000.prosjekt.Model.TimeValues
import no.uio.ifi.in2000.prosjekt.Model.WindCurrentProjection

class HavvarselRepository {
    private val havvarselDataSrc = HavvarselDataSource()

    suspend fun getHavarselTemperature(longitude : String, latitude : String, depth: String? = null, before : String? = null, after: String? = null) : Temperature?{
        return havvarselDataSrc.getHavvarselTemp(longitude, latitude, depth, before, after)
    }

    suspend fun getHavarselWindCurrent(lonCorner1 : String, latCorner1 : String, lonCorner2 : String, latCorner2 : String, time : String) : WindCurrentProjection?{
        return havvarselDataSrc.getHavvarselWindCurrent(lonCorner1, latCorner1, lonCorner2, latCorner2, time)
    }

    suspend fun getHavvarselDataProjection(variables: List<String>, lon: String, lat: String, depth: String? = null, before : String? = null, after: String? = null) : DataProjectionMain?{
        return havvarselDataSrc.getHavvarselDataProjection(variables, lon, lat, depth, before)
    }

    suspend fun getHavvarselTimes() : TimeValues?{
        return havvarselDataSrc.getHavvarselTimeValues()
    }
}