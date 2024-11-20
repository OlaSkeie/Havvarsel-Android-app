package no.uio.ifi.in2000.prosjekt.ui.CommonUIUtils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AlertTriangle(modifier : Modifier, color : String) {
    val colorMap = hashMapOf<String, Color>( //fargekart for de ulike gradene
        "Yellow" to Color(0xFFFFFF00),
        "Orange" to Color(0xFFFF9800),
        "Red" to Color(0xFF880808)
        )
    Canvas(modifier = Modifier.fillMaxSize()) {
        //tegning av selve trekanten
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            lineTo(size.width / 2, 0f)
            close()
        }
        //fargen for outlinen
        drawPath(path, color = colorMap[color]!!, style = Stroke(width = 5.dp.toPx()))
        //det hvite inni
        drawPath(path, color = Color(0xFFFFFFFF))
        val dotY = size.height * 3 / 4
        //Tegning av streken på utropstegnet
        drawLine(
            start = Offset(size.width / 2, size.height * 1 / 3), // Start of the vertical line
            end = Offset(size.width / 2, dotY), // End of the vertical line
            color = Color(0xFF000000),
            strokeWidth = 3.dp.toPx()
        )
        //tegning av sirkelen under utropstegnet
        drawCircle(
            color = Color(0xFF000000),
            center = Offset(size.width / 2, dotY + 3.2.dp.toPx()), // Center of the dot
            radius = 2.dp.toPx()
        )
    }

}
//for alertmessage kan man sende inn description og evt instruction i json filen
//for color kan man sende inn riskMatrixColor i json filen
@Composable
fun AlertTriangleWithDialog(color : String, alertMessage : String) {
    var showDialog by remember { mutableStateOf(false) }

    //kan gjøre den mindre her ved å sette ned size
    Box(modifier = Modifier.size(24.dp).clickable { showDialog = true }){

        AlertTriangle(modifier = Modifier
            .clickable { showDialog = true }.size(20.dp),
            color
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Varsel!") },
                text = { Text(text = alertMessage) },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text(text = "OK")
                    }
                }
            )
        }
    }
    }
