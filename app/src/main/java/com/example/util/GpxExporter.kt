package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.Trek
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GpxExporter {

    fun generateGpxString(trek: Trek): String {
        val points = GpsPointJsonHelper.jsonToPoints(trek.pointsJson)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"Arolock Trekking App\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
        sb.append("  <metadata>\n")
        sb.append("    <name>").append(escapeXml(trek.name)).append("</name>\n")
        sb.append("    <desc>").append(escapeXml(trek.description)).append("</desc>\n")
        sb.append("    <time>").append(isoFormat.format(Date(trek.startTimeMillis))).append("</time>\n")
        sb.append("  </metadata>\n")
        sb.append("  <trk>\n")
        sb.append("    <name>").append(escapeXml(trek.name)).append("</name>\n")
        sb.append("    <type>HIKING</type>\n")
        sb.append("    <trkseg>\n")

        for (p in points) {
            sb.append("      <trkpt lat=\"").append(p.latitude).append("\" lon=\"").append(p.longitude).append("\">\n")
            sb.append("        <ele>").append(String.format(Locale.US, "%.1f", p.altitude)).append("</ele>\n")
            sb.append("        <time>").append(isoFormat.format(Date(p.timestamp))).append("</time>\n")
            sb.append("      </trkpt>\n")
        }

        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>")
        return sb.toString()
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    fun shareGpxFile(context: Context, trek: Trek) {
        try {
            val gpxContent = generateGpxString(trek)
            val fileName = "arolock_${trek.id}_${trek.name.replace("\\s+".toRegex(), "_")}.gpx"
            val file = File(context.cacheDir, fileName)
            FileWriter(file).use { it.write(gpxContent) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/gpx+xml"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Trek Route: ${trek.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Trek Route GPX"))
        } catch (_: Exception) {
            // Error sharing
        }
    }
}
