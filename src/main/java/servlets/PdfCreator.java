package servlets;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfCreator {

    private static final DeviceRgb AZUL = new DeviceRgb(30, 64, 175);
    private static final DeviceRgb GRIS_C = new DeviceRgb(243, 244, 246);
    private static final DeviceRgb GRIS_B = new DeviceRgb(209, 213, 219);

    public static byte[] createFormPdfBytes(String nombre, String apellidos, String nif,
            String motivacion, String consentimiento, String estacionInfo, String logoPath) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc, PageSize.A4);
        doc.setMargins(40, 50, 40, 50);

        // Metadatos (EPD 3, experimento 4a)
        PdfDocumentInfo info = pdfDoc.getDocumentInfo();
        info.setTitle("Formulario de Firma Electronica - AE");
        info.setAuthor((nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : ""));
        info.setSubject("Procedimiento de prueba - Administracion Electronica UPO");
        info.setKeywords("firma, certificado digital, UPO, AE");
        info.setCreator("Proyecto AE 24/25 - iText 7.2.5");

        // Cabecera con logo (EPD 4, experimento 1) [I3]
        doc.add(cabecera(logoPath));
        doc.add(new Paragraph("\n"));

        // Titulo
        doc.add(new Paragraph("SOLICITUD DE PARTICIPACION EN PROCEDIMIENTO")
                .setFontSize(14).setBold().setFontColor(AZUL)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));
        doc.add(new Paragraph("Firma Electronica con Certificado Digital")
                .setFontSize(11).setFontColor(new DeviceRgb(107, 114, 128))
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

        // Secciones
        doc.add(seccion("1. DATOS DE IDENTIDAD"));
        Table tId = new Table(new float[]{2, 5}).useAllAvailableWidth().setMarginBottom(8);
        fila(tId, "Nombre", nombre != null ? nombre : "-");
        fila(tId, "Apellidos", apellidos != null ? apellidos : "-");
        fila(tId, "NIF / NIE", nif != null ? nif : "-");
        doc.add(tId);

        // Estacion de referencia (opcional, campo de jsp2)
        int secDeclaracion;
        if (estacionInfo != null && !estacionInfo.trim().isEmpty()) {
            doc.add(seccion("2. REFERENCIA DE LOCALIZACION"));
            doc.add(new Paragraph(estacionInfo).setFontSize(10).setBackgroundColor(GRIS_C)
                    .setPadding(10).setMarginBottom(8));
            doc.add(seccion("3. MOTIVACION"));
            secDeclaracion = 4;
        } else {
            doc.add(seccion("2. MOTIVACION"));
            secDeclaracion = 3;
        }
        String mot = (motivacion != null && !motivacion.trim().isEmpty()) ? motivacion : "(No especificado)";
        doc.add(new Paragraph(mot).setFontSize(10).setBackgroundColor(GRIS_C)
                .setPadding(10).setMarginBottom(8));

        doc.add(seccion(secDeclaracion + ". DECLARACION"));
        doc.add(new Paragraph("D./Dna. " + (nombre != null ? nombre : "")
                + " " + (apellidos != null ? apellidos : "")
                + ", con NIF/NIE " + (nif != null ? nif : "-")
                + ", declara que consiente la verificacion de su identidad "
                + "y desea participar en el presente procedimiento de PRUEBA, "
                + "firmando este documento con su certificado digital.")
                .setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED).setMarginBottom(8));

        doc.add(new Paragraph("Consentimiento: ")
                .add(new Text(consentimiento != null ? "Si" : "No").setBold())
                .setFontSize(10).setMarginBottom(8));

        // Espacio para el sello QR de Viafirma
        doc.add(new Paragraph("\n\n"));
        Table tFirma = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth().setBorder(new DashedBorder(GRIS_B, 1));
        tFirma.addCell(new Cell()
                .add(new Paragraph("Firma electronica").setBold().setFontSize(10).setFontColor(AZUL))
                .add(new Paragraph("Este espacio sera completado por Viafirma Platform con el sello QR.")
                        .setFontSize(9).setFontColor(new DeviceRgb(107, 114, 128)))
                .setPadding(16).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        doc.add(tFirma);

        // Pie
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        doc.add(new Paragraph("Documento generado el " + ts + " - AE - UPO")
                .setFontSize(8).setFontColor(new DeviceRgb(156, 163, 175))
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(24));

        doc.close();
        return baos.toByteArray();
    }

    // ─── Métodos de maquetacion ───────────────────────────────────────────────
    private static Table cabecera(String logoPath) {
        Table t = new Table(new float[12]).useAllAvailableWidth();

        // Celda izquierda: logo (3 columnas)
        Cell cLogo = new Cell(1, 3)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(new SolidBorder(GRIS_B, 0.5f));
        if (logoPath != null && new File(logoPath).exists()) {
            try {
                cLogo.add(new Image(ImageDataFactory.create(logoPath)).scaleToFit(80, 50));
            } catch (Exception e) {
                cLogo.add(new Paragraph("UPO").setBold().setFontSize(12));
            }
        } else {
            cLogo.add(new Paragraph("UPO").setBold().setFontSize(12));
        }
        t.addCell(cLogo);

        // Celda central: titulo (6 columnas)
        t.addCell(new Cell(1, 6)
                .add(new Paragraph("Administracion Electronica")
                        .setBold().setFontSize(11).setFontColor(AZUL))
                .add(new Paragraph("Grado en Ingenieria Informatica en Sistemas de Informacion")
                        .setFontSize(8).setFontColor(new DeviceRgb(107, 114, 128)))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(new SolidBorder(GRIS_B, 0.5f)));

        // Celda derecha: codigo (3 columnas)
        t.addCell(new Cell(1, 3)
                .add(new Paragraph("AE").setBold().setFontSize(14).setFontColor(AZUL))
                .add(new Paragraph("24/25")
                        .setFontSize(9).setFontColor(new DeviceRgb(107, 114, 128)))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(new SolidBorder(GRIS_B, 0.5f)));

        return t;
    }

    private static Paragraph seccion(String txt) {
        return new Paragraph(txt).setFontSize(10).setBold().setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(AZUL).setPaddingLeft(8).setPaddingTop(4).setPaddingBottom(4)
                .setMarginTop(16).setMarginBottom(6);
    }

    private static void fila(Table t, String etiq, String val) {
        t.addCell(new Cell().add(new Paragraph(etiq).setBold().setFontSize(10))
                .setBackgroundColor(GRIS_C).setPadding(6).setBorder(new SolidBorder(GRIS_B, 0.5f)));
        t.addCell(new Cell().add(new Paragraph(val).setFontSize(10))
                .setPadding(6).setBorder(new SolidBorder(GRIS_B, 0.5f)));
    }
}
