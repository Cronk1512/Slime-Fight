package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.proyecto.slimefight.Main;
import com.proyecto.slimefight.actores.Jugador;
import utiles.Config;
import utiles.TomarInput;

public class PantallaJuego extends Pantalla {
    private Stage stage;
    private TomarInput inputs = new TomarInput();;
    private Jugador jugador;
    private OrthographicCamera camara;

    public PantallaJuego(Main main) {
        super(main);
    }

    public void show() {
        camara = new OrthographicCamera(Config.ancho, (float) Config.alto / 2);
        stage = new Stage();

        Gdx.input.setInputProcessor(inputs);

        jugador = new Jugador();
        jugador.setPosition(50, 50);
        jugador.setDebug(true);

        stage.addActor(jugador);
    }

    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        jugador.update(delta, 0, inputs);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width,height);
    }

    public void dispose() {
        stage.dispose();
    }
}
