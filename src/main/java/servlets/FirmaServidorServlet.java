package servlets;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.ServerSignatureResourceApi;
import org.openapitools.client.api.ValidationResourceApi;
import org.openapitools.client.model.ConfigSignatureDTO;
import org.openapitools.client.model.SignatureRequestDTO;
import org.openapitools.client.model.SignatureResponseDTO;
import org.openapitools.client.model.ValidationFileRequestDTO;
import org.openapitools.client.model.ValidationFileResponseDTO;

@WebServlet(name = "FirmaServidorServlet", urlPatterns = {"/FirmaServidorServlet"})
public class FirmaServidorServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(FirmaServidorServlet.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Control de acceso
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("autenticado"))) {
            response.sendRedirect(request.getContextPath() + "/jsps/jsp1.jsp?error=noauth");
            return;
        }

        // Datos del certificado desde sesion
        String nombre     = (String) session.getAttribute("nombre");
        String apellidos  = (String) session.getAttribute("apellidos");
        String nif        = (String) session.getAttribute("nif");

        // Datos del formulario
        request.setCharacterEncoding("UTF-8");
        String motivacion     = request.getParameter("motivacion");
        String consentimiento = request.getParameter("consentimiento");
        String estacionInfo   = request.getParameter("estacionInfo");
        String logoPath       = getServletContext().getRealPath("/img/logo_upo.png");

        // ── PASO 1: Generar PDF en memoria (EPD 3 y 4) ───────────────────────
        byte[] pdfBytes;
        try {
            pdfBytes = PdfCreator.createFormPdfBytes(
                    nombre, apellidos, nif, motivacion, consentimiento, estacionInfo, logoPath);
            LOG.info("PDF generado en memoria para firma de servidor (" + pdfBytes.length + " bytes)");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error al generar el PDF", ex);
            response.sendRedirect(request.getContextPath() + "/jsps/jsp2.jsp?error=pdf");
            return;
        }

        String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

        ApiClient api = new ApiClient();
        api.setUsername(Constantes.USERNAME);
        api.setPassword(Constantes.PASSWORD);

        // ── PASO 2: Firma de servidor (EPD 7) ─────────────────────────────────
        // ServerSignatureResourceApi firma el documento directamente en Viafirma
        // sin necesidad de que el usuario disponga de certificado propio.
        SignatureResponseDTO firmaDTO;
        try {
            ServerSignatureResourceApi ssra = new ServerSignatureResourceApi(api);

            // Configuracion PAdES-B (firma electronica avanzada en PDF)
            ConfigSignatureDTO config = new ConfigSignatureDTO();
            config.setSignatureType(ConfigSignatureDTO.SignatureTypeEnum.PADES_B);
            config.setPackaging(ConfigSignatureDTO.PackagingEnum.ENVELOPED);

            // Peticion de firma de servidor
            SignatureRequestDTO peticion = new SignatureRequestDTO();
            peticion.setFileBase64(pdfBase64);           // PDF en Base64
            peticion.setConfiguration(config);            // Configuracion PAdES-B
            peticion.setReason("Proyecto AE 24/25 - Firma de servidor");

            firmaDTO = ssra.signDocument(peticion);
            LOG.info("Firma de servidor completada. OperationCode: " + firmaDTO.getOperationCode());

        } catch (ApiException ex) {
            LOG.log(Level.SEVERE, "Error en firma de servidor: " + ex.getMessage(), ex);
            request.setAttribute("errorFirma", "Error al firmar con el servidor de Viafirma: "
                    + ex.getMessage());
            request.getRequestDispatcher("/jsps/jspResultadoServidor.jsp")
                   .forward(request, response);
            return;
        }

        // ── PASO 3: Validacion inmediata del documento firmado (EPD 6) ─────────
        // Tras la firma de servidor, validamos el resultado usando
        // ValidationResourceApi.verifySignature() con el fichero firmado en Base64.
        ValidationFileResponseDTO validacionDTO = null;
        try {
            ValidationResourceApi vra = new ValidationResourceApi(api);

            ValidationFileRequestDTO peticionValidacion = new ValidationFileRequestDTO();
            // Usamos el documento firmado que devolvio Viafirma en Base64
            peticionValidacion.setSignedFileBase64(firmaDTO.getSignedFileBase64());
            peticionValidacion.setFilename("formulario_firmado.pdf");
            peticionValidacion.setShowExtendedInfo(true);

            validacionDTO = vra.verifySignature(peticionValidacion);
            LOG.info("Validacion completada. Firma valida: " + validacionDTO.getSignatureValid()
                    + " | Firmas: " + validacionDTO.getSignaturesCount());

        } catch (ApiException ex) {
            // La validacion es un extra — si falla, continuamos sin ella
            LOG.log(Level.WARNING, "No se pudo validar el documento firmado: " + ex.getMessage());
        }

        // ── PASO 4: Pasar datos a la vista de resultado ────────────────────────
        request.setAttribute("firmaDTO",      firmaDTO);
        request.setAttribute("validacionDTO", validacionDTO);
        request.setAttribute("nombreFirmante",
                (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : ""));
        request.setAttribute("nifFirmante",   nif);

        request.getRequestDispatcher("/jsps/jspResultadoServidor.jsp")
               .forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }

    @Override
    public String getServletInfo() { return "Servlet de firma de servidor (EPD 7) + validacion (EPD 6)"; }
}
