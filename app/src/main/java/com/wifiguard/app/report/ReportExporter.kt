package com.wifiguard.app.report

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.wifiguard.app.model.HistoryEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ReportExporter(private val context: Context) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun exportPdf(history: List<HistoryEntity>): File {
        val file = reportFile("wifi_guard_history.pdf")
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 11f }
        canvas.drawText("WiFi Guard - Connection History", 32f, 44f, titlePaint)
        var y = 78f
        history.take(42).forEach {
            canvas.drawText("${dateFormat.format(Date(it.timestamp))}  ${it.eventType}  ${it.deviceName}  ${it.ipAddress}", 32f, y, bodyPaint)
            y += 18f
        }
        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    fun exportExcel(history: List<HistoryEntity>): File {
        val file = reportFile("wifi_guard_history.xlsx")
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.putText("[Content_Types].xml", contentTypes)
            zip.putText("_rels/.rels", rels)
            zip.putText("xl/workbook.xml", workbook)
            zip.putText("xl/_rels/workbook.xml.rels", workbookRels)
            zip.putText("xl/worksheets/sheet1.xml", sheetXml(history))
        }
        return file
    }

    private fun reportFile(name: String): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        return File(dir, name)
    }

    private fun ZipOutputStream.putText(name: String, text: String) {
        putNextEntry(ZipEntry(name))
        write(text.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun sheetXml(history: List<HistoryEntity>): String {
        val rows = buildString {
            append(row(1, listOf("Date", "Event", "Device", "IP", "MAC")))
            history.forEachIndexed { index, event ->
                append(row(index + 2, listOf(
                    dateFormat.format(Date(event.timestamp)),
                    event.eventType.name,
                    event.deviceName,
                    event.ipAddress,
                    event.macAddress
                )))
            }
        }
        return """<?xml version="1.0" encoding="UTF-8"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>$rows</sheetData></worksheet>"""
    }

    private fun row(index: Int, values: List<String>) = "<row r=\"$index\">" +
        values.mapIndexed { cell, value ->
            val letter = ('A'.code + cell).toChar()
            "<c r=\"$letter$index\" t=\"inlineStr\"><is><t>${escape(value)}</t></is></c>"
        }.joinToString("") + "</row>"

    private fun escape(value: String) = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    private val contentTypes = """<?xml version="1.0" encoding="UTF-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""
    private val rels = """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
    private val workbook = """<?xml version="1.0" encoding="UTF-8"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="History" sheetId="1" r:id="rId1"/></sheets></workbook>"""
    private val workbookRels = """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
}
