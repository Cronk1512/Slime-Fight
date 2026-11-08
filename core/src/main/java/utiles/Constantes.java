package utiles;

public class Constantes {

    public static final float PPM = 32.0f; // PIXEL POR METRO
    public static final float TPP = 1/60f; // TIEMPO POR PASO (para el world.step)
    public static final float GRAVEDAD = 20f;
    public static final float COYOTE_TIME = 0.2f;

    // Filtros de colisiones
    public static final short CATEGORIA_SLIME  = 0x0001;
    public static final short CATEGORIA_CAJAS  = 0x0002;
    public static final short CATEGORIA_TERRENO  = 0x0004;
    public static final short CATEGORIA_BALA  = 0x0008;
}
