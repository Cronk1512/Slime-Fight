package utiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.*;
import com.badlogic.gdx.Preferences;
import com.proyecto.slimefight.actores.Arma;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;

public abstract class Config {
    public static int ancho = 640;
    public static int alto = 480;

    public static class Controles {
        // TODO controles configurables
        public static final int MOVER_DER = Keys.RIGHT;
        public static final int MOVER_IZQ = Keys.LEFT;
        public static final int MOVER_ABA = Keys.DOWN;
        public static final int SALTAR = Keys.UP;
        public static final int DISPARAR = Keys.J;
        public static final int LANZAR_ARMA = Keys.K;
    }


}
