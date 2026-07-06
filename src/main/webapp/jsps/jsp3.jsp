<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    /*
     * jsp3.jsp — Resultado de la firma.
     * Muestra el enlace al PDF firmado con cajetin QR (requisito 4 del enunciado).
     *
     * [IC3 CORREGIDO] El boton "Cerrar sesion" ahora apunta a /servlet1 (logout manual)
     * o a jsp1.jsp con invalidacion de sesion. Se ha eliminado el enlace a /logout
     * que no existia en el web.xml y causaba 404.
     */
    String urlFirmado     = (String) request.getAttribute("urlFirmado");
    String nombreFirmante = (String) request.getAttribute("nombreFirmante");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Documento Firmado</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="main-container" style="text-align:center;">

        <div style="font-size:48px;margin-bottom:16px;">&#10003;</div>

        <h1 class="main-title">Documento firmado correctamente</h1>

        <div class="user-data" style="text-align:left;margin:24px 0;">
            <p><strong>Firmante:</strong> <%= nombreFirmante != null ? nombreFirmante : "-" %></p>
            <p><strong>Plataforma:</strong> Viafirma Platform (sandbox)</p>
            <p style="color:#6b7280;font-size:13px;margin-top:8px;">
                El documento incluye un codigo QR que permite verificar la autenticidad
                de la firma escaneandolo con cualquier lector QR.
            </p>
        </div>

        <% if (urlFirmado != null && !urlFirmado.isEmpty()) { %>
            <a href="<%= urlFirmado %>" target="_blank" class="button">
                Ver documento firmado (PDF + sello QR)
            </a>
        <% } else { %>
            <div class="error-message">
                <p>No se pudo obtener el enlace al documento firmado.</p>
            </div>
        <% } %>

        <div style="margin-top:24px;display:flex;gap:12px;justify-content:center;flex-wrap:wrap;">
            <a href="${pageContext.request.contextPath}/jsps/jsp1.jsp" class="button"
               style="background-color:#6b7280;">
                Volver al inicio
            </a>
            <%--
                Cerrar sesion: invalida la sesion desde la JSP
                y redirige al inicio limpio.
            --%>
            <a href="${pageContext.request.contextPath}/CerrarSesion" class="button"
               style="background-color:#374151;">
                Cerrar sesion
            </a>
        </div>

    </div>
</body>
</html>
