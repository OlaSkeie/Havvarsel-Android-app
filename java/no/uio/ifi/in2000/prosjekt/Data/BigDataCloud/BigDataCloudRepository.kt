package no.uio.ifi.in2000.prosjekt.Data.BigDataCloud

import no.uio.ifi.in2000.prosjekt.Model.BigDataCloud

class BigDataCloudRepository {
    private val bigDataCloudDataSrc = BigDataCloudDataSource()

    suspend fun getBigDataCloud(lat : String, lon : String) : BigDataCloud?{
        return bigDataCloudDataSrc.getPlaceFromCoordinates(lat, lon)
    }
}