package utiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.*;
import com.badlogic.gdx.Preferences;

import java.util.Dictionary;
import java.util.Enumeration;

public abstract class Config {
    public static int ancho = 640;
    public static int alto = 480;

    public static class Controles {
        // TODO controles configurables
        public static final int MOVER_DER = Keys.D;
        public static final int MOVER_IZQ = Keys.A;
        public static final int MOVER_ABA = Keys.S;
        public static final int SALTAR = Keys.W;
        public static final int DISPARAR = Keys.J;
    }

    public static float tamañoTile = 32f;
    public static float escalaTile = 1f / tamañoTile;


}
