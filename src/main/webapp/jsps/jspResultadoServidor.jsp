<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="org.openapitools.client.model.SignatureResponseDTO"%>
<%@page import="org.openapitools.client.model.ValidationFileResponseDTO"%>
<%
    /*
     * jspResultadoServidor.jsp — Resultado de la firma de servidor (EPD 7)
     * con validacion inmediata (EPD 6).
     *
     * Muestra:
     *  - Enlace al PDF firmado (signedLink de Viafirma)
     *  - Resultado de la validacion: firma valida, numero de firmas, timestamp
     *  - Codigo de operacion de Viafirma
     */
    SignatureResponseDTO    firma     = (SignatureResponseDTO)    request.getAttribute("firmaDTO");
    ValidationFileResponseDTO valid   = (ValidationFileResponseDTO) request.getAttribute("validacionDTO");
    String nombreFirmante             = (String) request.getAttribute("nombreFirmante");
    String nifFirmante                = (String) request.getAttribute("nifFirmante");
    String errorFirma                 = (String) request.getAttribute("errorFirma");

    String signedLink    = (firma != null) ? firma.getSignedLink()    : null;
    String operationCode = (firma != null) ? firma.getOperationCode() : null;

    boolean firmaValida    = (valid != null && Boolean.TRUE.equals(valid.getSignatureValid()));
    Integer numFirmas      = (valid != null) ? valid.getSignaturesCount()      : null;
    Integer firmasValidas  = (valid != null) ? valid.getValidSignaturesCount() : null;
    String  errorValidacion = (valid != null) ? valid.getErrorMessage()        : null;
    String  timestampValid  = (valid != null && valid.getValidationTime() != null)
                              ? valid.getValidationTime().toString() : null;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Documento Firmado - Firma de Servidor</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .badge-ok  { background:#d1fae5;color:#065f46;padding:4px 12px;border-radius:20px;font-weight:700;font-size:13px; }
        .badge-err { background:#fee2e2;color:#991b1b;padding:4px 12px;border-radius:20px;font-weight:700;font-size:13px; }
        .seccion   { background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:20px;margin:16px 0; }
        .seccion h3{ color:#1e40af;font-size:15px;margin-bottom:12px;border-bottom:1px solid #e2e8f0;padding-bottom:8px; }
        .fila      { display:flex;justify-content:space-between;margin-bottom:8px;font-size:14px; }
        .fila span:first-child { color:#6b7280;font-weight:500; }
        .fila span:last-child  { color:#1a1a2e;font-weight:600; }
        .badge-mode { background:#dbeafe;color:#1e40af;padding:3px 10px;border-radius:12px;font-size:12px;font-weight:700; }
    </style>
</head>
<body>
<div class="main-container">

    <% if (errorFirma != null) { %>
        <%-- Error en la firma de servidor --%>
        <div style="text-align:center;">
            <div style="font-size:48px;margin-bottom:16px;">&#9888;</div>
            <h1 class="main-title" style="color:#991b1b;">Error en la firma de servidor</h1>
            <div class="error-message"><p><%= errorFirma %></p></div>
            <a href="${pageContext.request.contextPath}/jsps/jsp2.jsp" class="button">Volver al formulario</a>
        </div>

    <% } else { %>
        <%-- Resultado exitoso --%>
        <div style="text-align:center;margin-bottom:24px;">
            <div style="font-size:48px;">&#10003;</div>
            <h1 class="main-title">Documento firmado con servidor</h1>
            <span class="badge-mode">Firma de servidor &mdash; EPD 7</span>
            <p style="color:#6b7280;font-size:14px;margin-top:12px;">
                El documento ha sido firmado directamente por Viafirma Platform
                sin necesidad de certificado personal del usuario.
            </p>
        </div>

        <%-- Datos del firmante --%>
        <div class="seccion">
            <h3>Datos del firmante</h3>
            <div class="fila"><span>Nombre:</span><span><%= nombreFirmante != null ? nombreFirmante : "-" %></span></div>
            <div class="fila"><span>NIF / NIE:</span><span><%= nifFirmante != null ? nifFirmante : "-" %></span></div>
            <div class="fila"><span>Modalidad:</span><span>Firma de servidor (EPD 7)</span></div>
            <% if (operationCode != null) { %>
            <div class="fila"><span>Codigo de operacion:</span><span><%= operationCode %></span></div>
            <% } %>
        </div>

        <%-- Resultado de la validacion (EPD 6) --%>
        <% if (valid != null) { %>
        <div class="seccion">
            <h3>Resultado de la validacion &mdash; EPD 6</h3>
            <div class="fila">
                <span>Estado de la firma:</span>
                <span>
                    <% if (firmaValida) { %>
                        <span class="badge-ok">&#10003; Firma VALIDA</span>
                    <% } else { %>
                        <span class="badge-err">&#10007; Firma NO valida</span>
                    <% } %>
                </span>
            </div>
            <% if (numFirmas != null) { %>
            <div class="fila"><span>Total de firmas:</span><span><%= numFirmas %></span></div>
            <% } %>
            <% if (firmasValidas != null) { %>
            <div class="fila"><span>Firmas validas:</span><span><%= firmasValidas %></span></div>
            <% } %>
            <% if (timestampValid != null) { %>
            <div class="fila"><span>Fecha de validacion:</span><span><%= timestampValid %></span></div>
            <% } %>
            <% if (errorValidacion != null && !errorValidacion.trim().isEmpty()) { %>
            <div class="fila"><span>Detalle:</span><span style="color:#991b1b;"><%= errorValidacion %></span></div>
            <% } %>
        </div>
        <% } %>

        <%-- Enlace al documento firmado --%>
        <div class="seccion" style="text-align:center;">
            <h3>Documento firmado</h3>
            <% if (signedLink != null && !signedLink.trim().isEmpty()) { %>
                <a href="<%= signedLink %>" target="_blank" class="button">
                    Ver documento firmado (PDF con firma electronica)
                </a>
                <p style="color:#6b7280;font-size:12px;margin-top:10px;">
                    El documento incluye el sello de firma electronica de Viafirma Platform.
                </p>
            <% } else { %>
                <p style="color:#991b1b;">
                    No se obtuvo enlace al documento. Puede descargarlo desde el
                    panel de administracion de Viafirma Platform.
                </p>
            <% } %>
        </div>

        <%-- Navegacion --%>
        <div style="display:flex;gap:12px;justify-content:center;margin-top:24px;flex-wrap:wrap;">
            <a href="${pageContext.request.contextPath}/jsps/jsp1.jsp" class="button"
               style="background-color:#6b7280;">Volver al inicio</a>
            <a href="${pageContext.request.contextPath}/CerrarSesion" class="button"
               style="background-color:#374151;">Cerrar sesion</a>
        </div>

    <% } %>

</div>
</body>
</html>
