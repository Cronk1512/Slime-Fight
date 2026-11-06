package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
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
    private TomarInput inputs = new TomarInput();
    private Slime jugador;
    private Slime botardo;
    private OrthographicCamera camara;

    // Variables para el mapa de Tiled
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    public PantallaJuego(Main main) {
        super(main);
    }

    @Override
    public void show() {
        camara = new OrthographicCamera(Config.ancho , Config.alto);
        stage = new Stage();

        Gdx.input.setInputProcessor(inputs);

        // 1. Cargar el mapa desde la carpeta assets (ajusta la ruta si está dentro de una subcarpeta como "mapa/MapaGame.tmx")
        map = new TmxMapLoader().load("mapa/MapaGame.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);

        // 2. Centrar la cámara estáticamente en el medio del mapa
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        int mapWidthTiles = map.getProperties().get("width", Integer.class);
        int mapHeightTiles = map.getProperties().get("height", Integer.class);

        float mapaAnchoPx = mapWidthTiles * tileWidth;
        float mapaAltoPx = mapHeightTiles * tileHeight;

        camara.position.set(mapaAnchoPx / 2f, mapaAltoPx / 2f, 0);
        camara.update();

        // 3. Configuración de texturas y actores
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
        Arma armatest = new Arma(0, -10,76, 22, new Texture(Utils.obtenerRutaInterna("armas/arma1.png")), 3, 16, 25, 1.5f, 5);
        jugador.setArma(armatest);


        botardo = new Slime(new Sprite(bojo), new Sprite(bboca), Color.SKY, false);
        botardo.setPosition(150, 50);

        stage.addActor(jugador);
        stage.addActor(botardo);
        stage.addActor(armatest);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar y renderizar el mapa con la cámara fija en el centro
        camara.update();
        renderer.setView(camara);
        renderer.render();

        // Lógica de tus slimes y actores
        jugador.update(delta, inputs);
        botardo.update(delta, inputs);
        stage.act(delta);

        // Separar actores en listas
        ArrayList<Slime> jugadores = new ArrayList<>();
        ArrayList<Bala> balas = new ArrayList<>();
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

        // Dibujar el stage encima del mapa
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width,height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        map.dispose();
        renderer.dispose();
    }
}
