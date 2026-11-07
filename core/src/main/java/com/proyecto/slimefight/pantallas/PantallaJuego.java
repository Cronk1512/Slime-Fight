package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.proyecto.slimefight.Main;
import com.proyecto.slimefight.actores.Arma;
import com.proyecto.slimefight.actores.Bala;
import com.proyecto.slimefight.actores.Slime;
import com.proyecto.slimefight.actores.Terreno;
import utiles.Config;
import utiles.TomarInput;
import utiles.Utils;

import java.util.ArrayList;

public class PantallaJuego extends Pantalla {
    private Stage stage;
    private TomarInput inputs = new TomarInput();
    private Slime jugador;
    private Slime botardo;
    private OrthographicCamera camara;
    private ExtendViewport viewport;
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer renderer;
    float mapaAnchoPx;
    float mapaAltoPx;

    ArrayList<Slime> slimes;
    ArrayList<Terreno> terrenos;

    public PantallaJuego(Main main) {
        super(main);
    }

    @Override
    public void show() {
        // Inicializar cosas
        camara = new OrthographicCamera(Config.ancho , Config.alto);
        viewport = new ExtendViewport(Config.ancho, Config.alto);
        viewport.setCamera(camara);
        stage = new Stage(viewport);
        mapa = new TmxMapLoader().load("mapa/mapaTest.tmx");
        renderer = new OrthogonalTiledMapRenderer(mapa);
        slimes = new ArrayList<>();
        terrenos = new ArrayList<>();

        int tileWidth = mapa.getProperties().get("tilewidth", Integer.class);
        int tileHeight = mapa.getProperties().get("tileheight", Integer.class);
        int mapWidthTiles = mapa.getProperties().get("width", Integer.class);
        int mapHeightTiles = mapa.getProperties().get("height", Integer.class);

        mapaAnchoPx = mapWidthTiles * tileWidth;
        mapaAltoPx = mapHeightTiles * tileHeight;

        viewport.getCamera().position.set(mapaAnchoPx / 2f, mapaAltoPx / 2f, 0);
        viewport.getCamera().update();

        Gdx.input.setInputProcessor(inputs);

        Texture tojo = new Texture(Utils.obtenerRutaInterna("ojos/ojos3.png"));
        Texture tboca = new Texture(Utils.obtenerRutaInterna("bocas/boca3.png"));
        Texture bojo = new Texture(Utils.obtenerRutaInterna("ojos/ojos1.png"));
        Texture bboca = new Texture(Utils.obtenerRutaInterna("bocas/boca1.png"));
        tojo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        tboca.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bojo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bboca.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // debug slimes
        jugador = new Slime(
            new Sprite(tojo), new Sprite(tboca), Color.WHITE, false);
        Arma armatest = new Arma("AK47", 0, -10,76, 22, new Texture(Utils.obtenerRutaInterna("armas/arma1.png")),
            3, 16, 25f, 0f, 7, 2f, 10, 1);
        jugador.setArma(armatest);

        botardo = new Slime(
            new Sprite(bojo), new Sprite(bboca), Color.SKY, false);

        // TODO: sacar lista de slimes de pantalla lobby multiplayer
        slimes.add(jugador);
        slimes.add(botardo);

        leerMapa();

        // Añade todos los jugadores al stage
        for (Slime slime : slimes) {
            stage.addActor(slime);
        }

        // debug añade el armatest al jugador
        stage.addActor(armatest);
    }

    private void leerMapa() {
        // Terrenos
        MapLayer capaFisica = mapa.getLayers().get("Fisico");
        for (MapObject objeto : capaFisica.getObjects()) {
            if (objeto.getProperties().get("type", String.class).equals("Piso")) {
                terrenos.add(new Terreno((PolygonMapObject) objeto));
            }
        }

        // Entidades
        MapLayer capaEntidades = mapa.getLayers().get("Entidades");
        ArrayList<Vector2> puntosDeSpawn = new ArrayList<>();
        for (MapObject objeto : capaEntidades.getObjects()) {
            if (objeto.getProperties().get("type", String.class).equals("Spawn")) {
                puntosDeSpawn.add(new Vector2(objeto.getProperties().get("x", Float.class), objeto.getProperties().get("y", Float.class)));
            }
        }

        int posID = 0;
        for (Slime slime : slimes) {
            if (posID > puntosDeSpawn.size() - 1) posID = 0;
            slime.setPosition(puntosDeSpawn.get(posID).x, puntosDeSpawn.get(posID).y);
            posID++;
        }

        MapObject posCamara = capaEntidades.getObjects().get("camaraPos");
        stage.getViewport().getCamera().position.set(posCamara.getProperties().get("x",Float.class), posCamara.getProperties().get("y",Float.class), 0);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //camara.zoom += (float) (0.1 * delta);
        //stage.getViewport().getCamera().position.set(mapaAnchoPx / 2f, mapaAltoPx / 2f, 0);
        stage.getViewport().getCamera().update();

        renderer.setView(camara);
        renderer.render();

        jugador.update(delta, inputs);
        botardo.update(delta, inputs);
        stage.act(delta);

        // añadir balas a array
        ArrayList<Bala> balas = new ArrayList<>();
        for (Actor actor : stage.getActors()) {
            if (actor.getClass() == Bala.class) {
                balas.add((Bala) actor);
            }
        }

        // Chequear colisiones
        for (Slime jugador : slimes) {
            for (Terreno terreno : terrenos) {
                System.out.println(Utils.verificarTocandoTerreno(jugador.pisoCollider, terreno));
                jugador.setTocandoTecho(Utils.verificarTocandoTerreno(jugador.techoCollider, terreno));
                jugador.setTocandoPiso(Utils.verificarTocandoTerreno(jugador.pisoCollider, terreno));
                jugador.setTocandoParedD(Utils.verificarTocandoTerreno(jugador.paredderCollider, terreno));
                jugador.setTocandoParedI(Utils.verificarTocandoTerreno(jugador.paredizqCollider, terreno));
            }

            for (Bala bala : balas) {
                if (Utils.verificarColisionBala(jugador, bala)) {
                    jugador.recibirDano(bala);
                    bala.remove();
                }
            }
        }

        // Dibujar el stage encima del mapa
        stage.getViewport().apply();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width,height);
        stage.getViewport().update(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        mapa.dispose();
        renderer.dispose();
    }
}
