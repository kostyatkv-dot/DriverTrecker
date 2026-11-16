package ua.worktime.tracker.data

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Мінімальний генератор файлів Excel (.xlsx) без сторонніх бібліотек. */
object Xlsx {
    sealed class Cell {
        data class Text(val v: String, val bold: Boolean = false) : Cell()
        data class Num(val v: Double, val bold: Boolean = false) : Cell()
        object Empty : Cell()
    }

    class Sheet(val name: String, val widths: List<Double>, val rows: List<List<Cell>>)

    fun t(v: String) = Cell.Text(v)
    fun b(v: String) = Cell.Text(v, bold = true)
    fun n(v: Double) = Cell.Num(v)
    fun nb(v: Double) = Cell.Num(v, bold = true)

    fun write(out: OutputStream, sheets: List<Sheet>) {
        val zip = ZipOutputStream(out)
        fun put(path: String, content: String) {
            zip.putNextEntry(ZipEntry(path))
            zip.write(content.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        val head = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
        val ns = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
        val rns = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"

        put(
            "[Content_Types].xml",
            head + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
                "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
                "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
                sheets.indices.joinToString("") {
                    "<Override PartName=\"/xl/worksheets/sheet${it + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                } +
                "</Types>",
        )
        put(
            "_rels/.rels",
            head + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"$rns/officeDocument\" Target=\"xl/workbook.xml\"/>" +
                "</Relationships>",
        )
        put(
            "xl/workbook.xml",
            head + "<workbook xmlns=\"$ns\" xmlns:r=\"$rns\"><sheets>" +
                sheets.mapIndexed { i, s ->
                    "<sheet name=\"${esc(sheetName(s.name))}\" sheetId=\"${i + 1}\" r:id=\"rId${i + 1}\"/>"
                }.joinToString("") +
                "</sheets></workbook>",
        )
        put(
            "xl/_rels/workbook.xml.rels",
            head + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                sheets.indices.joinToString("") {
                    "<Relationship Id=\"rId${it + 1}\" Type=\"$rns/worksheet\" Target=\"worksheets/sheet${it + 1}.xml\"/>"
                } +
                "<Relationship Id=\"rId${sheets.size + 1}\" Type=\"$rns/styles\" Target=\"styles.xml\"/>" +
                "</Relationships>",
        )
        put(
            "xl/styles.xml",
            head + "<styleSheet xmlns=\"$ns\">" +
                "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
                "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>" +
                "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>" +
                "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
                "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
                "<cellXfs count=\"4\">" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
                "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>" +
                "<xf numFmtId=\"4\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>" +
                "<xf numFmtId=\"4\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\"/>" +
                "</cellXfs>" +
                "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
                "</styleSheet>",
        )
        sheets.forEachIndexed { i, s -> put("xl/worksheets/sheet${i + 1}.xml", sheetXml(s, head, ns)) }
        zip.finish()
        zip.flush()
    }

    private fun sheetXml(s: Sheet, head: String, ns: String): String {
        val sb = StringBuilder(head)
        sb.append("<worksheet xmlns=\"").append(ns).append("\">")
        if (s.widths.isNotEmpty()) {
            sb.append("<cols>")
            s.widths.forEachIndexed { i, w ->
                sb.append("<col min=\"${i + 1}\" max=\"${i + 1}\" width=\"$w\" customWidth=\"1\"/>")
            }
            sb.append("</cols>")
        }
        sb.append("<sheetData>")
        s.rows.forEachIndexed { ri, row ->
            val r = ri + 1
            sb.append("<row r=\"$r\">")
            row.forEachIndexed { ci, c ->
                val ref = col(ci) + r
                when (c) {
                    is Cell.Text -> sb.append("<c r=\"$ref\" t=\"inlineStr\"${if (c.bold) " s=\"1\"" else ""}><is><t xml:space=\"preserve\">")
                        .append(esc(c.v)).append("</t></is></c>")
                    is Cell.Num -> sb.append("<c r=\"$ref\" s=\"${if (c.bold) 3 else 2}\"><v>")
                        .append(java.math.BigDecimal(c.v).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString())
                        .append("</v></c>")
                    Cell.Empty -> {}
                }
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun col(i: Int): String {
        var n = i + 1
        val sb = StringBuilder()
        while (n > 0) {
            val m = (n - 1) % 26
            sb.insert(0, ('A' + m))
            n = (n - 1) / 26
        }
        return sb.toString()
    }

    private fun sheetName(s: String): String =
        s.replace(Regex("[\\[\\]:*?/\\\\]"), " ").take(31).ifBlank { "Sheet" }

    private fun esc(s: String): String {
        val sb = StringBuilder()
        for (ch in s) {
            when {
                ch == '&' -> sb.append("&amp;")
                ch == '<' -> sb.append("&lt;")
                ch == '>' -> sb.append("&gt;")
                ch == '"' -> sb.append("&quot;")
                ch < ' ' && ch != '\n' && ch != '\t' -> {}
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}
