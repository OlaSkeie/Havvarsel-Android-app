package no.uio.ifi.in2000.prosjekt.Model

import kotlinx.serialization.Serializable

@Serializable
data class InfoScreen(
    val info : List<InfoObjects>
)

@Serializable
data class InfoObjects(
    val id : String,
    val name : String,
    val icon : Int,
    val img : String,
    val description : String
)