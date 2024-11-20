package no.uio.ifi.in2000.prosjekt.Data.EnTur

import no.uio.ifi.in2000.prosjekt.Model.EnTur

class EnTurRepository {
    private val enTurDataSource = EnTurDataSource()

    suspend fun getEnTurAPI(text : String, noOfResults : Int = 10) : EnTur?{
        return enTurDataSource.getEnTurAutoComplete(text, noOfResults)
    }
}