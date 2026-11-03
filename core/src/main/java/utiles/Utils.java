package utiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Utils {
    // ------------ Matematica *miedo* ------------
    // valor absoluto
    public static float Abs(float valor) {
        if (valor < 0) return valor * -1;
        return valor;
    }

    // Obtener rutas
    public static FileHandle obtenerRutaClase(String ruta) {
        return Gdx.files.classpath(ruta);
    }

    public static FileHandle obtenerRutaInterna(String ruta) {
        return Gdx.files.internal(ruta);
    }

    public static FileHandle obtenerRutaLocal(String ruta) {
        return Gdx.files.local(ruta);
    }

    public static FileHandle obtenerRutaExterna(String ruta) {
        return Gdx.files.external(ruta);
    }
}
