package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
import org.openapitools.client.api.ClientSignatureResourceApi;
import org.openapitools.client.model.CertificateFilterDTO;
import org.openapitools.client.model.ClientAccessResponseDTO;
import org.openapitools.client.model.ConfigCertificateRequestDTO;
import org.openapitools.client.model.ConfigPadesDTO;
import org.openapitools.client.model.ConfigSignatureDTO;
import org.openapitools.client.model.PrepareRequestDTO;
import org.openapitools.client.model.PrepareSignatureRequestDTO;
import org.openapitools.client.model.RequestResultDTO;
import org.openapitools.client.model.StamperDTO;

@WebServlet(name = "FirmaDocumentoServlet", urlPatterns = {"/FirmaDocumentoServlet"})
public class FirmaDocumentoServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(FirmaDocumentoServlet.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Control de acceso: solo usuarios autenticados
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("autenticado"))) {
            response.sendRedirect(request.getContextPath() + "/jsps/jsp1.jsp?error=noauth");
            return;
        }

        // Datos del certificado leidos de SESION (no de hidden inputs del formulario)
        String nombre = (String) session.getAttribute("nombre");
        String apellidos = (String) session.getAttribute("apellidos");
        String nif = (String) session.getAttribute("nif");

        // Datos editables del formulario
        request.setCharacterEncoding("UTF-8");
        String motivacion = request.getParameter("motivacion");
        String consentimiento = request.getParameter("consentimiento");
        String estacionInfo = request.getParameter("estacionInfo");
        String logoPath = getServletContext().getRealPath("/img/logo_upo.png");

        // ── PASO 1: Generar PDF en memoria con iText (EPD 3 y 4) ─────────────
        byte[] pdfBytes;
        try {
            pdfBytes = PdfCreator.createFormPdfBytes(
                    nombre, apellidos, nif, motivacion, consentimiento, estacionInfo, logoPath);
            LOG.info("PDF generado en memoria (" + pdfBytes.length + " bytes)");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error al generar el PDF", ex);
            response.sendRedirect(request.getContextPath() + "/jsps/jsp2.jsp?error=pdf");
            return;
        }

        String encodedString = Base64.getEncoder().encodeToString(pdfBytes);

        ApiClient api = new ApiClient();
        api.setUsername(Constantes.USERNAME);
        api.setPassword(Constantes.PASSWORD);

        try {
            ClientSignatureResourceApi csra = new ClientSignatureResourceApi(api);

            // ── Configuracion de firma PAdES-B (EPD 6) ───────────────────────
            ConfigSignatureDTO config = new ConfigSignatureDTO();
            config.setSignatureType(ConfigSignatureDTO.SignatureTypeEnum.PADES_B);
            config.setPackaging(ConfigSignatureDTO.PackagingEnum.ENVELOPED);
            config.setSignatureReason("Proyecto AE 24/25 - Firma de formulario");

            // Sello QR de Viafirma (EPD 6, ejercicio 2)
            // FIX: usar setPadesConfig() — el metodo real implementado.
            // setPades() es un stub que lanza UnsupportedOperationException.
            StamperDTO stamper = new StamperDTO();
            stamper.setType(StamperDTO.TypeEnum.QR);
            stamper.setHeight(36);
            stamper.setWidth(324);
            stamper.setxAxis(5);
            stamper.setyAxis(680);

            ConfigPadesDTO padesConfig = new ConfigPadesDTO();
            padesConfig.setStamper(stamper);
            config.setPadesConfig(padesConfig);  // metodo correcto

            // ── Preparar peticion de firma ────────────────────────────────────
            PrepareSignatureRequestDTO petFirmaDTO = new PrepareSignatureRequestDTO();
            petFirmaDTO.setFile(encodedString);
            petFirmaDTO.setFileName("formulario_firmado.pdf");
            petFirmaDTO.setConfiguration(config);

            PrepareRequestDTO prepareRequestDTO = new PrepareRequestDTO();
            List<PrepareSignatureRequestDTO> lfirma = new ArrayList<>();
            lfirma.add(petFirmaDTO);
            prepareRequestDTO.setSignatures(lfirma);
            prepareRequestDTO.setCount(1);

            // callbackURL dinamico a servlet3 (EPD 6)
            String callbackURL = request.getScheme() + "://"
                    + request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath() + "/servlet3";
            prepareRequestDTO.callbackURL(callbackURL);

            // Filtro por NIF del usuario autenticado (EPD 6, ejercicio 3)
            if (nif != null && !nif.trim().isEmpty()) {
                CertificateFilterDTO certificateFilter = new CertificateFilterDTO();
                certificateFilter.addFilterValuesItem(nif);
                ConfigCertificateRequestDTO configCertificate = new ConfigCertificateRequestDTO();
                configCertificate.setAutoSend(true);
                configCertificate.setNationalIdFilter(certificateFilter);
                prepareRequestDTO.setCertificateFilter(configCertificate);
            }

            RequestResultDTO resultDTO = csra.requestSignature(prepareRequestDTO);
            request.getSession().setAttribute("tokenFirma", resultDTO.getCode());
            ClientAccessResponseDTO clientAccess = resultDTO.getClientAccess();
            LOG.info("Redirigiendo a Viafirma Desktop para firma");
            response.sendRedirect(clientAccess.getDesktopProtocol());

        } catch (ApiException ex) {
            LOG.log(Level.SEVERE, "Error al solicitar firma a Viafirma", ex);
            response.sendRedirect(request.getContextPath() + "/jsps/jsp2.jsp?error=firma");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    public String getServletInfo() {
        return "Servlet de generacion PDF y firma PAdES-B con sello QR";
    }
}
