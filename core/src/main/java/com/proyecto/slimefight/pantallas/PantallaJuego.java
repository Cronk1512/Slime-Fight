package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.proyecto.slimefight.Main;
import com.proyecto.slimefight.actores.Bala;
import com.proyecto.slimefight.actores.Slime;
import utiles.Config;
import utiles.TomarInput;
import utiles.Utils;

import java.util.ArrayList;

public class PantallaJuego extends Pantalla {
    private Stage stage;
    private TomarInput inputs = new TomarInput();;
    private Slime jugador;
    private OrthographicCamera camara;

    public PantallaJuego(Main main) {
        super(main);
    }

    public void show() {
        camara = new OrthographicCamera(Config.ancho, (float) Config.alto / 2);
        stage = new Stage();

        Gdx.input.setInputProcessor(inputs);


        Texture tojo = new Texture(Utils.obtenerRutaInterna("ojos/ojos3.png"));
        Texture tboca = new Texture(Utils.obtenerRutaInterna("bocas/boca3.png"));
        tojo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        tboca.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jugador = new Slime(new Sprite(tojo), new Sprite(tboca), false);
        jugador.setPosition(50, 50);
        jugador.setDebug(false);

        stage.addActor(jugador);
    }

    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        jugador.update(delta, inputs);
        stage.act(delta);

        // Separar actores en listas
        ArrayList<Slime> jugadores = new ArrayList<>();
        ArrayList<Bala> balas = new ArrayList<>();;
        for (Actor actor : stage.getActors()) {
            if (actor.getClass() == Slime.class) {
                jugadores.add((Slime) actor);
            } else if (actor.getClass() == Bala.class) {
                balas.add((Bala) actor);
            }
        }

        // Chequear colisiones
        for (Slime jugador : jugadores) {
            for (Bala bala : balas) {
                if (Utils.verificarColision(jugador, bala)) {
                    jugador.recibirDano(bala);
                    bala.remove();
                }
            }
        }

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
