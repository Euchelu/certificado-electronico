package servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.ClientSignatureResourceApi;
import org.openapitools.client.model.OperationStatusDTO;
import org.openapitools.client.model.RequestStatusResponseDTO;

@WebServlet(name = "servlet3", urlPatterns = {"/servlet3"})
public class servlet3 extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(servlet3.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = (String) request.getSession().getAttribute("tokenFirma");

        if (token == null) {
            LOG.warning("servlet3 llamado sin tokenFirma en sesion");
            response.sendRedirect(request.getContextPath() + "/jsps/jsp1.jsp?error=token");
            return;
        }

        ApiClient api = new ApiClient();
        api.setUsername(Constantes.USERNAME);   
        api.setPassword(Constantes.PASSWORD);

        ClientSignatureResourceApi csra = new ClientSignatureResourceApi(api);

        try {
            RequestStatusResponseDTO statusDTO = csra.getSignatureStatus(token);
            OperationStatusDTO opDTO = statusDTO.getOperations().get(0);
            String urlFirmado = opDTO.getSignedLink();

            LOG.info("Firma completada. URL custodia: " + urlFirmado);

            request.setAttribute("urlFirmado", urlFirmado);
            request.setAttribute("nombreFirmante",
                    request.getSession().getAttribute("nombre") + " "
                  + request.getSession().getAttribute("apellidos"));

            // Limpiar token de firma
            request.getSession().removeAttribute("tokenFirma");

            request.getRequestDispatcher("/jsps/jsp3.jsp").forward(request, response);

        } catch (ApiException ex) {
            LOG.log(Level.SEVERE, "Error al consultar estado de la firma", ex);
            response.sendRedirect(request.getContextPath() + "/jsps/jsp2.jsp?error=estadofirma");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }

    @Override
    public String getServletInfo() { return "Servlet callback de firma"; }
}
