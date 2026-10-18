package servlets;

/**
 * Credenciales de acceso a la API de Viafirma.
 *
 * <p>Los valores no se guardan en el código: se leen de las variables de entorno
 * {@code VIAFIRMA_USERNAME} y {@code VIAFIRMA_PASSWORD} o, si no existen, de las
 * propiedades de sistema de la JVM con el mismo nombre (útil en Tomcat con
 * {@code -DVIAFIRMA_USERNAME=...}).</p>
 */
public final class Constantes {

    static final String VAR_USUARIO = "VIAFIRMA_USERNAME";
    static final String VAR_PASSWORD = "VIAFIRMA_PASSWORD";

    private Constantes() {}

    public static final String USERNAME = leer(VAR_USUARIO);
    public static final String PASSWORD = leer(VAR_PASSWORD);

    /**
     * Devuelve el valor de la variable de entorno indicada, o de la propiedad de
     * sistema con el mismo nombre, o una cadena vacía si no está definida.
     */
    static String leer(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.trim().isEmpty()) {
            valor = System.getProperty(nombre);
        }
        return valor == null ? "" : valor.trim();
    }
}
