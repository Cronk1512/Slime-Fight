package utiles;

import com.badlogic.gdx.InputAdapter;
import static com.badlogic.gdx.Input.*;

public class TomarInput extends InputAdapter {
    public boolean izquierda;
    public boolean derecha;
    public boolean arriba;
    public boolean abajo;
    public boolean espacio;

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Keys.LEFT:
                izquierda = true;
                break;
            case Keys.RIGHT:
                derecha = true;
                break;
            case Keys.UP:
                arriba = true;
                break;
            case Keys.DOWN:
                abajo = true;
                break;
            case Keys.SPACE:
                espacio = true;
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.LEFT:
                izquierda = false;
                break;
            case Keys.RIGHT:
                derecha = false;
                break;
            case Keys.UP:
                arriba = false;
                break;
            case Keys.DOWN:
                abajo = false;
                break;
            case Keys.SPACE:
                espacio = false;
                break;
        }
        return true;
    }
}
