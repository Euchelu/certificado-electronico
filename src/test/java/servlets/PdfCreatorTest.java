package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class PdfCreatorTest {

    private static String textoDe(byte[] pdf) throws IOException {
        try (PdfDocument doc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdf)))) {
            StringBuilder texto = new StringBuilder();
            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                texto.append(PdfTextExtractor.getTextFromPage(doc.getPage(i))).append('\n');
            }
            return texto.toString();
        }
    }

    private static String autorDe(byte[] pdf) throws IOException {
        try (PdfDocument doc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdf)))) {
            return doc.getDocumentInfo().getAuthor();
        }
    }

    @Test
    void generaUnPdfValido() throws IOException {
        byte[] pdf = PdfCreator.createFormPdfBytes(
                "Ana", "Garcia Lopez", "12345678Z", "Motivo", "on", null, null);

        String cabecera = new String(pdf, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", cabecera);
    }

    @Test
    void incluyeLosDatosDelSolicitante() throws IOException {
        byte[] pdf = PdfCreator.createFormPdfBytes(
                "Ana", "Garcia Lopez", "12345678Z", "Quiero participar", "on", null, null);

        String texto = textoDe(pdf);
        assertTrue(texto.contains("Ana"));
        assertTrue(texto.contains("Garcia Lopez"));
        assertTrue(texto.contains("12345678Z"));
        assertTrue(texto.contains("Quiero participar"));
        assertEquals("Ana Garcia Lopez", autorDe(pdf));
    }

    @Test
    void reflejaSiHayConsentimiento() throws IOException {
        String conConsentimiento = textoDe(PdfCreator.createFormPdfBytes(
                "Ana", "Garcia", "1Z", "x", "on", null, null));
        String sinConsentimiento = textoDe(PdfCreator.createFormPdfBytes(
                "Ana", "Garcia", "1Z", "x", null, null, null));

        assertTrue(conConsentimiento.matches("(?s).*Consentimiento:\\s*Si.*"));
        assertTrue(sinConsentimiento.matches("(?s).*Consentimiento:\\s*No.*"));
    }

    @Test
    void anadeLaSeccionDeLocalizacionSoloSiSeIndica() throws IOException {
        String sinEstacion = textoDe(PdfCreator.createFormPdfBytes(
                "Ana", "Garcia", "1Z", "x", "on", null, null));
        String conEstacion = textoDe(PdfCreator.createFormPdfBytes(
                "Ana", "Garcia", "1Z", "x", "on", "Estacion Sevilla Aeropuerto", null));

        assertFalse(sinEstacion.contains("REFERENCIA DE LOCALIZACION"));
        assertTrue(sinEstacion.contains("3. DECLARACION"));
        assertTrue(conEstacion.contains("2. REFERENCIA DE LOCALIZACION"));
        assertTrue(conEstacion.contains("Estacion Sevilla Aeropuerto"));
        assertTrue(conEstacion.contains("4. DECLARACION"));
    }

    @Test
    void usaValoresPorDefectoSiFaltanDatos() throws IOException {
        String texto = textoDe(PdfCreator.createFormPdfBytes(
                null, null, null, "  ", null, null, "/ruta/que/no/existe.png"));

        assertTrue(texto.contains("(No especificado)"));
        assertTrue(texto.contains("UPO"));
    }
}
