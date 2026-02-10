/**
 * Punto de entrada principal del proyecto Tablón de Avisos.
 * Lanza la interfaz gráfica elegida (Swing o JavaFX).
 */
public class Main {
    public static void main(String[] args) {
        // Descomenta la interfaz que deseas ejecutar

        // Opción 1: Interfaz con Swing
        ui.SwingMain.main(args);

        // Opción 2: Interfaz con JavaFX (recomendada por estilo y experiencia previa)
        //ui.JavaFXMain.main(args);
    }
}