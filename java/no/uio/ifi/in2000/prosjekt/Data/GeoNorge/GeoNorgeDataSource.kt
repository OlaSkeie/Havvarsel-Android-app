package no.uio.ifi.in2000.prosjekt.Data.GeoNorge

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.prosjekt.Model.GeoNorge

class GeoNorgeDataSource {
    private val client = HttpClient(CIO){
        defaultRequest {
            url("https://ws.geonorge.no/adresser/v1/sok?sok=")
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true

            })
        }
    }
    suspend fun getSearchedLocation(sted : String) : GeoNorge?{ //sted kan være navn på by eller en adresse

        val stedList = sted.trim().split(" ")
        if(stedList.size > 1){ //Hvis det er mer enn et element vil det ikke være en kommune, men adresse
            val adresse = stedList.joinToString(separator = "%20")


        return try {
            client.get("https://ws.geonorge.no/adresser/v1/sok?sok=$adresse").body()
        } catch (e: Exception) {
            println("Error during HTTP request for metAlerts: $e")
            return null
        }}

        //Hvis det er en kommune (søket er annerledes)
        return try {
            client.get("https://ws.geonorge.no/adresser/v1/sok?kommunenavn=$sted").body()
        } catch (e: Exception) {
            println("Error during HTTP request for metAlerts: $e")
            return null
        }

    }
}