package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ConstantesTest {

    private static final String CLAVE = "VIAFIRMA_TEST_SOLO_PARA_TESTS";

    @AfterEach
    void limpiar() {
        System.clearProperty(CLAVE);
    }

    @Test
    void leeLaPropiedadDeSistemaSiNoHayVariableDeEntorno() {
        System.setProperty(CLAVE, "  usuario-demo  ");
        assertEquals("usuario-demo", Constantes.leer(CLAVE));
    }

    @Test
    void devuelveCadenaVaciaSiNoEstaDefinida() {
        assertEquals("", Constantes.leer(CLAVE));
    }
}
