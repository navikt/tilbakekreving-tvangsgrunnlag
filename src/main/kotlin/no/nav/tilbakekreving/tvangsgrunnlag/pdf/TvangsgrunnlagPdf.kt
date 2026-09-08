package no.nav.tilbakekreving.tvangsgrunnlag.pdf

import no.nav.tilbakekreving.tvangsgrunnlag.modell.Tvangsgrunnlag
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Genererer en dummy-PDF med noen linjer tekst. Simulerer et dokument som senere
 * skal hentes fra et fagsystem (f.eks. SAF) - innholdet her er kun placeholder.
 */
fun genererDummyPdf(
    tittel: String,
    tekst: String,
): ByteArray {
    PDDocument().use { document ->
        val page = PDPage()
        document.addPage(page)
        PDPageContentStream(document, page).use { content ->
            content.beginText()
            content.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
            content.newLineAtOffset(50f, 700f)
            content.showText(tittel)
            content.newLineAtOffset(0f, -20f)
            content.showText(tekst)
            content.endText()
        }
        val output = ByteArrayOutputStream()
        document.save(output)
        return output.toByteArray()
    }
}

/**
 * Pakker én dummy-PDF per tvangsgrunnlag i en zip-fil.
 */
fun genererTvangsgrunnlagZip(tvangsgrunnlag: List<Tvangsgrunnlag>): ByteArray {
    val zipOutput = ByteArrayOutputStream()
    ZipOutputStream(zipOutput).use { zip ->
        tvangsgrunnlag.forEach { grunnlag ->
            val pdfBytes =
                genererDummyPdf(
                    tittel = "Tvangsgrunnlag ${grunnlag.id}",
                    tekst = "Dummy-tekst for ${grunnlag.description} (prioritet: ${grunnlag.priority})",
                )
            zip.putNextEntry(ZipEntry("tvangsgrunnlag-${grunnlag.id}.pdf"))
            zip.write(pdfBytes)
            zip.closeEntry()
        }
    }
    return zipOutput.toByteArray()
}
