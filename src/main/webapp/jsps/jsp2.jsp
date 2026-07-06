<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Formulario de Participacion</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .firma-opciones { display:flex;gap:16px;flex-wrap:wrap;margin-top:8px; }
        .btn-cliente { background-color:#1e40af; }
        .btn-servidor { background-color:#065f46; }
        .info-firma { background:#f0fdf4;border:1px solid #bbf7d0;border-radius:8px;
                      padding:10px 14px;margin-bottom:16px;font-size:13px;color:#065f46; }
    </style>
</head>
<body>
    <div class="form-container">
        <c:choose>
            <c:when test="${empty sessionScope.userCert}">
                <div class="error-message">
                    <p>Error: No estas autenticado.</p>
                    <a href="${pageContext.request.contextPath}/jsps/jsp1.jsp" class="button">Volver al inicio</a>
                </div>
            </c:when>

            <c:otherwise>
                <h1 class="main-title">Formulario de Solicitud</h1>

                <c:if test="${param.error == 'pdf'}">
                    <div class="error-message"><p>Error al generar el PDF. Intentalo de nuevo.</p></div>
                </c:if>
                <c:if test="${param.error == 'firma'}">
                    <div class="error-message"><p>Error al conectar con Viafirma. Comprueba Viafirma Desktop.</p></div>
                </c:if>

                <%-- Datos del certificado (solo lectura desde sesion) --%>
                <div class="user-data">
                    <h3>Datos del Usuario Autenticado</h3>
                    <p><strong>Nombre:</strong>
                        <input type="text" value="${userCert.name}" readonly
                               style="border:1px solid #e5e7eb;background:#f9fafb;color:#6b7280;
                                      padding:6px 10px;border-radius:6px;width:auto;">
                    </p>
                    <p><strong>Apellidos:</strong>
                        <input type="text" value="${userCert.surname1} ${userCert.surname2}" readonly
                               style="border:1px solid #e5e7eb;background:#f9fafb;color:#6b7280;
                                      padding:6px 10px;border-radius:6px;width:auto;">
                    </p>
                    <p><strong>NIF:</strong>
                        <input type="text" value="${userCert.numberUserId}" readonly
                               style="border:1px solid #e5e7eb;background:#f9fafb;color:#6b7280;
                                      padding:6px 10px;border-radius:6px;width:auto;">
                    </p>
                    <small style="color:#9ca3af;">
                        Datos del certificado digital — no modificables.
                        <a href="${pageContext.request.contextPath}/jsps/jspCertificado.jsp"
                           style="color:#1e40af;margin-left:8px;">Ver detalles completos &#8594;</a>
                    </small>
                </div>

                <%-- Campos del formulario — se envian al servlet elegido --%>
                <%-- Usamos id para que ambos formularios compartan los campos --%>
                <div id="campos-formulario">
                    <div class="form-group">
                        <label for="estacionInfo">Estacion o punto de referencia (opcional):</label>
                        <input type="text" id="estacionInfo" name="estacionInfo"
                               placeholder="Indica una estacion o referencia geografica..."
                               style="width:100%;padding:10px 12px;border:1px solid #d1d5db;
                                      border-radius:6px;font-size:14px;">
                    </div>

                    <div class="form-group">
                        <label for="motivacion">Motivacion para participar: *</label>
                        <textarea id="motivacion" name="motivacion" rows="4" required
                                  placeholder="Describa el motivo de su participacion..."></textarea>
                    </div>

                    <div class="form-group">
                        <label>
                            <input type="checkbox" id="consentimiento" name="consentimiento" required>
                            Consiento la verificacion de mi identidad. Deseo participar en el
                            procedimiento de PRUEBA, a tal efecto firmo este documento.
                        </label>
                    </div>
                </div>

                <%-- EXTRA: info sobre las dos modalidades de firma --%>
                <div class="info-firma">
                    <strong>Modalidades de firma disponibles:</strong><br>
                    <strong>Firma con certificado digital</strong> — requiere Viafirma Desktop y tu certificado personal.<br>
                    <strong>Firma de servidor (EPD 7)</strong> — Viafirma firma en el servidor, sin necesidad de certificado personal.
                </div>

                <%-- Dos formularios — uno por modalidad, comparten los datos mediante JS --%>
                <div class="firma-opciones">

                    <%-- Boton 1: Firma con certificado del usuario (flujo original) --%>
                    <form id="form-cliente" action="${pageContext.request.contextPath}/FirmaDocumentoServlet" method="POST">
                        <input type="hidden" name="motivacion"    id="h-motivacion">
                        <input type="hidden" name="consentimiento" id="h-consentimiento">
                        <input type="hidden" name="estacionInfo"  id="h-estacion">
                        <button type="submit" class="submit-button btn-cliente"
                                onclick="return copiarDatos()">
                            Firmar con Certificado Digital
                        </button>
                    </form>

                    <%-- Boton 2: Firma de servidor — EPD 7 (EXTRA) --%>
                    <form id="form-servidor" action="${pageContext.request.contextPath}/FirmaServidorServlet" method="POST">
                        <input type="hidden" name="motivacion"    id="s-motivacion">
                        <input type="hidden" name="consentimiento" id="s-consentimiento">
                        <input type="hidden" name="estacionInfo"  id="s-estacion">
                        <button type="submit" class="submit-button btn-servidor"
                                onclick="return copiarDatosServidor()">
                            Firmar con Servidor (EPD 7)
                        </button>
                    </form>

                </div>

                <script>
                    // Copia los valores de los campos comunes a los hidden inputs del formulario del cliente
                    function copiarDatos() {
                        var mot = document.getElementById('motivacion').value;
                        var con = document.getElementById('consentimiento').checked;
                        var est = document.getElementById('estacionInfo').value;
                        if (!mot.trim()) { alert('Rellena el campo de motivacion.'); return false; }
                        if (!con) { alert('Debes marcar el checkbox de consentimiento.'); return false; }
                        document.getElementById('h-motivacion').value    = mot;
                        document.getElementById('h-consentimiento').value = con ? 'si' : '';
                        document.getElementById('h-estacion').value      = est;
                        return true;
                    }
                    // Mismo proceso para el formulario del servidor
                    function copiarDatosServidor() {
                        var mot = document.getElementById('motivacion').value;
                        var con = document.getElementById('consentimiento').checked;
                        var est = document.getElementById('estacionInfo').value;
                        if (!mot.trim()) { alert('Rellena el campo de motivacion.'); return false; }
                        if (!con) { alert('Debes marcar el checkbox de consentimiento.'); return false; }
                        document.getElementById('s-motivacion').value    = mot;
                        document.getElementById('s-consentimiento').value = con ? 'si' : '';
                        document.getElementById('s-estacion').value      = est;
                        return true;
                    }
                </script>

            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
