<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Inicio - Tramite AE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="main-container">
        <h1 class="main-title">Bienvenido al Sistema de Tramites</h1>

        <%-- Mensajes de error --%>
        <% String error = request.getParameter("error"); %>
        <% if ("noauth".equals(error)) { %>
            <div class="error-message"><p>Debes autenticarte con tu certificado digital antes de continuar.</p></div>
        <% } else if ("auth".equals(error)) { %>
            <div class="error-message"><p>Error al iniciar la autenticacion. Comprueba que Viafirma Desktop esta instalado.</p></div>
        <% } else if ("certificado".equals(error)) { %>
            <div class="error-message"><p>No se pudieron obtener los datos del certificado. Intentalo de nuevo.</p></div>
        <% } %>

        <%-- Bienvenida + acceso a detalles del certificado (EXTRA 2 - EPD 1/5) --%>
        <% if (session.getAttribute("userCert") != null) { %>
            <div style="background:#ecfdf5;border:1px solid #6ee7b7;border-radius:8px;
                        padding:14px 18px;margin-bottom:20px;display:flex;
                        justify-content:space-between;align-items:center;">
                <span style="color:#065f46;font-size:14px;font-weight:600;">
                    &#10003; Autenticado correctamente
                </span>
                <%-- EXTRA: boton para ver detalles del certificado (EPD 1) --%>
                <a href="${pageContext.request.contextPath}/jsps/jspCertificado.jsp"
                   style="font-size:12px;color:#1e40af;text-decoration:underline;font-weight:600;">
                    Ver detalles del certificado &#8594;
                </a>
            </div>
        <% } %>

        <div class="access-buttons">
            <%-- Boton 1: inicia autenticacion con Viafirma Desktop --%>
            <form action="${pageContext.request.contextPath}/servlet1" method="POST">
                <button type="submit" class="button">Acceder con Certificado Digital</button>
            </form>

            <%-- Boton 2: bloqueado hasta autenticarse (EL ${empty sessionScope.userCert}) --%>
            <form action="${pageContext.request.contextPath}/jsps/jsp2.jsp" method="GET">
                <button type="submit" class="button"
                        ${empty sessionScope.userCert ? 'disabled' : ''}>
                    Continuar al Formulario
                </button>
            </form>
        </div>
    </div>
</body>
</html>
