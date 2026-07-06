<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="org.openapitools.client.model.CertificateResponseDTO"%>
<%
    /*
     * jspCertificado.jsp — Detalles completos del certificado digital (EPD 1 y 5).
     *
     * Muestra toda la informacion disponible en el CertificateResponseDTO
     * que devuelve Viafirma tras la autenticacion:
     *  - Datos de identidad: nombre, apellidos, NIF, email
     *  - Autoridad de Certificacion (CA) emisora
     *  - Numero de serie y otros metadatos
     *
     * Los datos proceden de la sesion — no se hace ninguna llamada nueva a Viafirma.
     */
    CertificateResponseDTO cert = (CertificateResponseDTO) session.getAttribute("userCert");
    if (cert == null) {
        response.sendRedirect(request.getContextPath() + "/jsps/jsp1.jsp?error=noauth");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Detalles del Certificado</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .cert-header {
            background: linear-gradient(135deg,#1e40af,#1e3a5f);
            color:#fff;border-radius:12px;padding:24px;margin-bottom:24px;text-align:center;
        }
        .cert-header h2 { margin:0 0 6px;font-size:20px; }
        .cert-header p  { margin:0;font-size:13px;opacity:0.8; }
        .cert-icon { font-size:40px;margin-bottom:10px; }
        .seccion { background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:20px;margin-bottom:16px; }
        .seccion h3 { color:#1e40af;font-size:14px;font-weight:700;text-transform:uppercase;
                      letter-spacing:0.05em;margin-bottom:14px;border-bottom:1px solid #e2e8f0;padding-bottom:8px; }
        table { width:100%;border-collapse:collapse; }
        td { padding:9px 12px;font-size:14px;border-bottom:1px solid #f1f5f9; }
        td:first-child { color:#6b7280;font-weight:600;width:45%; }
        td:last-child  { color:#1a1a2e; }
        tr:last-child td { border-bottom:none; }
        .badge-activo { background:#d1fae5;color:#065f46;padding:2px 10px;
                        border-radius:12px;font-size:12px;font-weight:700; }
        .null-val { color:#d1d5db;font-style:italic;font-size:13px; }
    </style>
</head>
<body>
<div class="form-container" style="max-width:720px;">

    <div class="cert-header">
        <div class="cert-icon">&#128196;</div>
        <h2>Certificado Digital</h2>
        <p>Informacion completa del certificado con el que te has autenticado</p>
    </div>

    <%-- Seccion 1: Datos de identidad --%>
    <div class="seccion">
        <h3>1. Datos de identidad (EPD 1 — Certificado X.509)</h3>
        <table>
            <tr>
                <td>Nombre</td>
                <td><%= cert.getName() != null ? cert.getName() : "<span class='null-val'>No disponible</span>" %></td>
            </tr>
            <tr>
                <td>Primer apellido</td>
                <td><%= cert.getSurname1() != null ? cert.getSurname1() : "<span class='null-val'>No disponible</span>" %></td>
            </tr>
            <tr>
                <td>Segundo apellido</td>
                <td><%= cert.getSurname2() != null ? cert.getSurname2() : "<span class='null-val'>No disponible</span>" %></td>
            </tr>
            <tr>
                <td>NIF / NIE</td>
                <td><strong><%= cert.getNumberUserId() != null ? cert.getNumberUserId() : "<span class='null-val'>No disponible</span>" %></strong></td>
            </tr>
            <tr>
                <td>Correo electronico</td>
                <td><%= cert.getEmail() != null ? cert.getEmail() : "<span class='null-val'>No disponible</span>" %></td>
            </tr>
        </table>
    </div>

    <%-- Seccion 2: Datos de la CA emisora --%>
    <div class="seccion">
        <h3>2. Autoridad de Certificacion (EPD 1 — PKI)</h3>
        <table>
            <tr>
                <td>CA emisora</td>
                <td><%= cert.getCa() != null ? cert.getCa() : "<span class='null-val'>No disponible</span>" %></td>
            </tr>
            <tr>
                <td>Estado</td>
                <td><span class="badge-activo">&#10003; Activo</span></td>
            </tr>
        </table>
        <p style="font-size:12px;color:#9ca3af;margin-top:12px;">
            La Autoridad de Certificacion (CA) es la entidad que emitio y garantiza
            la autenticidad de este certificado. Solo los certificados emitidos por
            CAs reconocidas (FNMT, ACCV, Camerfirma...) son aceptados por Viafirma.
        </p>
    </div>

    <%-- Seccion 3: Explicacion tecnica para el examen --%>
    <div class="seccion" style="background:#eff6ff;border-color:#bfdbfe;">
        <h3 style="color:#1e40af;">3. Conceptos tecnicos — EPD 1</h3>
        <table>
            <tr>
                <td>Estandar</td>
                <td>X.509 v3 (RFC 5280)</td>
            </tr>
            <tr>
                <td>Formato del fichero</td>
                <td>PKCS#12 (.p12 / .pfx) — contiene clave publica + privada</td>
            </tr>
            <tr>
                <td>Tipo de firma</td>
                <td>PAdES-B (PDF Advanced Electronic Signature — Baseline)</td>
            </tr>
            <tr>
                <td>Infraestructura</td>
                <td>PKI (Public Key Infrastructure)</td>
            </tr>
            <tr>
                <td>Servicio de firma</td>
                <td>Viafirma Platform — sandbox.viafirma.com</td>
            </tr>
        </table>
    </div>

    <div style="display:flex;gap:12px;justify-content:center;margin-top:8px;flex-wrap:wrap;">
        <a href="${pageContext.request.contextPath}/jsps/jsp1.jsp" class="button"
           style="background-color:#6b7280;">Volver al inicio</a>
        <a href="${pageContext.request.contextPath}/jsps/jsp2.jsp" class="button">
            Continuar al formulario</a>
    </div>

</div>
</body>
</html>
