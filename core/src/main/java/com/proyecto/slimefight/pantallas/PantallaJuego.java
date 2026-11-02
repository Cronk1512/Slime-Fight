package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.proyecto.slimefight.Main;
import utiles.TomarInput;

public class PantallaJuego extends Pantalla {
    private Stage stage;
    private TomarInput inputs = new TomarInput();;

    // Test inputs, esto debería ir al jugador
    private float moveX;
    private float moveY;

    public PantallaJuego(Main main) {
        super(main);
    }

    public void show() {
        stage = new Stage();
        Gdx.input.setInputProcessor(inputs);
    }

    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // mover solo si no se tocan las dos teclas (pq 1 - 1 = 0)
        moveX = (inputs.derecha ? 1 : 0) - (inputs.izquierda ? 1 : 0);
        moveY = (inputs.arriba ? 1 : 0) - (inputs.abajo ? 1 : 0);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width,height);
    }

    public void dispose() {

    }
}
