package servlets;

import java.io.IOException;
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
import org.openapitools.client.api.ClientAuthResourceApi;
import org.openapitools.client.model.CertificateResponseDTO;

@WebServlet(name = "servlet2", urlPatterns = {"/servlet2"})
public class servlet2 extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(servlet2.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String token = (String) session.getAttribute("token");

        if (token == null || token.isEmpty()) {
            LOG.warning("servlet2 llamado sin token en sesion");
            response.sendRedirect(request.getContextPath() + "/jsps/jsp1.jsp?error=noauth");
            return;
        }

        ApiClient api = new ApiClient();
        api.setUsername(Constantes.USERNAME);
        api.setPassword(Constantes.PASSWORD);
        ClientAuthResourceApi cara = new ClientAuthResourceApi(api);

        try {
            CertificateResponseDTO certificado = cara.getCertificate(token);

            // [CF3] Objeto completo para EL en JSPs: ${userCert.name}, ${userCert.numberUserId}
            session.setAttribute("userCert", certificado);

            // ── Campos individuales con proteccion contra null ────────────────
            // Los certificados autofirmados (keytool) pueden devolver null en
            // getName(), getSurname1() y getSurname2() porque no tienen los
            // OIDs especificos de los certificados espanoles (FNMT, DNIe).
            // Sin este control, el PDF mostraria el literal "null null".

            String nombre = certificado.getName() != null
                    ? certificado.getName().trim() : "";

            String s1 = certificado.getSurname1() != null
                    ? certificado.getSurname1().trim() : "";
            String s2 = certificado.getSurname2() != null
                    ? certificado.getSurname2().trim() : "";
            String apellidos = (s1 + " " + s2).trim();

            // Se guarda null si el campo esta vacio — PdfCreator lo muestra como "-"
            session.setAttribute("nombre",    nombre.isEmpty()    ? null : nombre);
            session.setAttribute("apellidos", apellidos.isEmpty() ? null : apellidos);
            session.setAttribute("nif",       certificado.getNumberUserId());
            session.setAttribute("certEmail", certificado.getEmail());

            // Flag de control de acceso — lo comprueba FirmaDocumentoServlet
            session.setAttribute("autenticado", Boolean.TRUE);

            LOG.info("Autenticacion completada: "
                    + (nombre.isEmpty() ? "(sin nombre)" : nombre)
                    + " | NIF: " + certificado.getNumberUserId());

        } catch (ApiException ex) {
            LOG.log(Level.SEVERE, "Error al obtener certificado de Viafirma", ex);
            response.sendRedirect(request.getContextPath() + "/jsps/jsp1.jsp?error=certificado");
            return;
        }

        // Redirigir a jsp1 para que el usuario pulse "Continuar al Formulario"
        response.sendRedirect(request.getContextPath() + "/jsps/jsp1.jsp");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }

    @Override
    public String getServletInfo() { return "Servlet callback de autenticacion Viafirma"; }
}