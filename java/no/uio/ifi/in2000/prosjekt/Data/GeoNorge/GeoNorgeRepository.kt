package no.uio.ifi.in2000.prosjekt.Data.GeoNorge

import no.uio.ifi.in2000.prosjekt.Model.GeoNorge

class GeoNorgeRepository {
    private val GeoNorgeDataSrc = GeoNorgeDataSource()
    suspend fun getGeoNorgeAPI(sted : String) : GeoNorge?{
        return GeoNorgeDataSrc.getSearchedLocation(sted)
    }
}