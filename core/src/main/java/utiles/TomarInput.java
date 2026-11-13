package utiles;

import com.badlogic.gdx.InputAdapter;
import static com.badlogic.gdx.Input.*;
import static utiles.Config.*;

public class TomarInput extends InputAdapter {
    public boolean izquierda;
    public boolean derecha;
    public boolean saltar;
    public boolean abajo;
    public boolean disparar;
    public boolean lanzar_arma;

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Controles.MOVER_IZQ:
                izquierda = true;
                break;
            case Controles.MOVER_DER:
                derecha = true;
                break;
            case Controles.SALTAR:
                saltar = true;
                break;
            case Controles.MOVER_ABA:
                abajo = true;
                break;
            case Controles.DISPARAR:
                disparar = true;
                break;
            case Controles.LANZAR_ARMA:
                lanzar_arma = true;
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Controles.MOVER_IZQ:
                izquierda = false;
                break;
            case Controles.MOVER_DER:
                derecha = false;
                break;
            case Controles.SALTAR:
                saltar = false;
                break;
            case Controles.MOVER_ABA:
                abajo = false;
                break;
            case Controles.DISPARAR:
                disparar = false;
                break;
            case Controles.LANZAR_ARMA:
                lanzar_arma = false;
                break;
        }
        return true;
    }
}
