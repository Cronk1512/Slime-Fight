package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.proyecto.slimefight.Main;
import com.proyecto.slimefight.actores.Arma;
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
    private Slime botardo;
    private OrthographicCamera camara;

    public PantallaJuego(Main main) {
        super(main);
    }

    public void show() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        camara = new OrthographicCamera(Config.ancho, Config.alto * height/width);
        ExtendViewport viewport = new ExtendViewport(Config.ancho, Config.alto);

        stage = new Stage(viewport);

        Gdx.input.setInputProcessor(inputs);

        // debug slimes
        Texture tojo = new Texture(Utils.obtenerRutaInterna("ojos/ojos3.png"));
        Texture tboca = new Texture(Utils.obtenerRutaInterna("bocas/boca3.png"));
        Texture bojo = new Texture(Utils.obtenerRutaInterna("ojos/ojos1.png"));
        Texture bboca = new Texture(Utils.obtenerRutaInterna("bocas/boca1.png"));
        tojo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        tboca.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bojo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bboca.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jugador = new Slime(new Sprite(tojo), new Sprite(tboca), Color.WHITE, false);
        jugador.setPosition(50, 50);
        jugador.setDebug(false);
        Arma armatest = new Arma("AK47", 0, -10,76, 22, new Texture(Utils.obtenerRutaInterna("armas/arma1.png")),
            3, 16, 25f, 0f, 7, 2f, 10, 1);
        jugador.setArma(armatest);


        botardo = new Slime(new Sprite(bojo), new Sprite(bboca), Color.SKY, false);
        botardo.setPosition(150, 50);

        stage.addActor(jugador);
        stage.addActor(botardo);
        stage.addActor(armatest);
    }

    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        jugador.update(delta, inputs);
        botardo.update(delta, inputs);
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
                if (Utils.verificarColisionBala(jugador, bala)) {
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
