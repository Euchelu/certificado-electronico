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
import org.openapitools.client.api.ClientAuthResourceApi;
import org.openapitools.client.model.ClientAccessResponseDTO;
import org.openapitools.client.model.PrepareAuthDTO;
import org.openapitools.client.model.RequestResultDTO;

@WebServlet(name = "servlet1", urlPatterns = {"/servlet1"})
public class servlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(servlet.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ApiClient api = new ApiClient();
        api.setUsername(Constantes.USERNAME);
        api.setPassword(Constantes.PASSWORD);

        ClientAuthResourceApi cara = new ClientAuthResourceApi(api);
        PrepareAuthDTO auth = new PrepareAuthDTO();

        String callbackURL = request.getScheme() + "://"
                + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath() + "/servlet2";
        auth.callbackURL(callbackURL);

        try {
            RequestResultDTO rrd = cara.requestAuthentication(auth);
            request.getSession().setAttribute("token", rrd.getCode());
            ClientAccessResponseDTO clientAccess = rrd.getClientAccess();
            String desktop = clientAccess.getDesktopProtocol();
            LOG.info("Redirigiendo a Viafirma Desktop para autenticacion");
            response.sendRedirect(desktop);
        } catch (ApiException ex) {
            LOG.log(Level.SEVERE, "Error al solicitar autenticacion a Viafirma", ex);
            response.sendRedirect(request.getContextPath() + "/jsps/jsp1.jsp?error=auth");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { processRequest(req, res); }

    @Override
    public String getServletInfo() { return "Servlet de inicio de autenticacion"; }
}
