# Certificado Electrónico — Administración Electrónica (AE 24/25)

Aplicación web Java EE que integra firma electrónica con certificado digital a través de la plataforma **Viafirma**, desarrollada para la asignatura de Administración Electrónica del Grado en Ingeniería Informática en Sistemas de Información (UPO).

## Tecnologías

- Java EE 7 · Servlets · JSP · JSTL
- Maven · Tomcat 7
- iText 7.2.5 (generación de PDF)
- BouncyCastle 1.70 (firma PKI)
- API REST Viafirma (autenticación + firma cliente/servidor)

## Flujo de la aplicación

1. **Autenticación** (`servlet1`) — El usuario se autentica con su certificado digital a través de Viafirma Desktop.
2. **Callback de autenticación** (`servlet2`) — Recibe los datos del certificado (nombre, apellidos, NIF) y los guarda en sesión.
3. **Formulario** (`jsp2.jsp`) — El usuario completa los datos del formulario.
4. **Generación de PDF + Firma cliente** (`FirmaDocumentoServlet`) — Se genera el PDF con iText y se envía a Viafirma para firma PAdES-B con sello QR.
5. **Firma de servidor** (`FirmaServidorServlet`) — Alternativa: firma directa en Viafirma sin certificado del usuario, con validación inmediata.
6. **Resultado** (`servlet3`, `jspResultadoServidor.jsp`) — Muestra el enlace de custodia del documento firmado.

## Estructura

```
src/main/java/
├── servlets/              # Lógica de negocio
│   ├── Constantes.java    # Credenciales Viafirma
│   ├── PdfCreator.java    # Generación PDF con iText
│   ├── servlet.java       # Inicio de autenticación
│   ├── servlet2.java      # Callback autenticación
│   ├── servlet3.java      # Callback firma cliente
│   ├── FirmaDocumentoServlet.java   # Firma PAdES-B cliente
│   ├── FirmaServidorServlet.java    # Firma de servidor + validación
│   └── CerrarSesion.java
└── org/openapitools/client/         # Cliente API Viafirma (generado)

src/main/webapp/
├── jsps/                  # Vistas JSP
├── css/styles.css
└── WEB-INF/web.xml
```

## Requisitos

- Java 8+
- Maven 3.x
- Servidor Tomcat 7/9 (o equivalente)
- Acceso a la plataforma Viafirma con credenciales válidas

## Compilar y desplegar

```bash
mvn clean package
# Desplegar el WAR generado en target/ en Tomcat
```

O con el plugin integrado:

```bash
mvn tomcat7:run
```

La aplicación arranca en `http://localhost:8080/CeballosRodriguezJoseAngel`.
