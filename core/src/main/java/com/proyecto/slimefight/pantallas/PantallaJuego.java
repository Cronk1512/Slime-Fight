package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.proyecto.slimefight.Main;
import com.proyecto.slimefight.actores.Arma;
import com.proyecto.slimefight.actores.Slime;
import com.proyecto.slimefight.interfaz.Hud;
import utiles.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PantallaJuego extends Pantalla {
    private Stage stage;
    private TomarInput inputs = new TomarInput();
    private Slime jugador;
    private Slime botardo;
    private OrthographicCamera camara;
    private ExtendViewport viewport;
    float acumulador = 0;

    private World world;
    private Box2DDebugRenderer box2DDebugRenderer;
    private SpriteBatch batch;
    private OrthogonalTiledMapRenderer renderer;
    private TiledMap mapa;
    private AyudanteMapas ayudanteMapas;
    private final Matrix4 matrizDebug = new Matrix4();
    private Hud hud;
    private final Map<Integer, TomarInput> inputsPorSlime = new HashMap<>();
    private int idLocal = 0;   // el slime que controla esta PC

    ArrayList<Slime> slimes;

    public PantallaJuego(Main main) {
        super(main);
    }



    @Override
    public void show() {
        // Inicializar cosas
        camara = new OrthographicCamera(Config.ancho , Config.alto);
        viewport = new ExtendViewport(Config.ancho, Config.alto, camara);
        world = new World(new Vector2(0, -Constantes.GRAVEDAD), false);
        box2DDebugRenderer = new Box2DDebugRenderer();
        batch = new SpriteBatch();
        mapa = new TmxMapLoader().load("mapa/mapaTest.tmx");
        ayudanteMapas = new AyudanteMapas(mapa, this);
        renderer = ayudanteMapas.leerMapa();
        stage = new Stage(viewport);
        slimes = new ArrayList<>();
        Gdx.input.setInputProcessor(inputs);

        // contact listener bullshit
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact c) { Utils.contactoPies(c, 1); Utils.contactoParedes(c, 1); Utils.contactoBala(c); }
            @Override public void endContact(Contact c) { Utils.contactoPies(c, -1);  Utils.contactoParedes(c, -1); }
            @Override public void preSolve(Contact c, Manifold m) {}
            @Override public void postSolve(Contact c, ContactImpulse i) {}
        });

        // debug slimes
        Texture tojo = new Texture(Utils.obtenerRutaInterna("ojos/ojos3.png"));
        Texture tboca = new Texture(Utils.obtenerRutaInterna("bocas/boca3.png"));
        tojo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        tboca.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        Texture bojo = new Texture(Utils.obtenerRutaInterna("ojos/ojos1.png"));
        Texture bboca = new Texture(Utils.obtenerRutaInterna("bocas/boca1.png"));
        bojo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bboca.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jugador = new Slime(Utils.crearCuerpo(32, 32, 26, 32, BodyDef.BodyType.DynamicBody, world), new Sprite(tojo), new Sprite(tboca), Color.WHITE, false);
        botardo = new Slime(Utils.crearCuerpo(32, 32, 26, 32, BodyDef.BodyType.DynamicBody, world), new Sprite(bojo), new Sprite(bboca), Color.SKY, false);
        slimes.add(jugador);
        slimes.add(botardo);
        // TODO: sacar lista de slimes de pantalla lobby multiplayer

        // Añade slimes
        for (int i = 0; i < slimes.size(); i++) {
            Slime s = slimes.get(i);
            s.setId(i);
            // el local usa el input del teclado, el resto uno propio (por ahora vacío)
            inputsPorSlime.put(i, i == idLocal ? inputs : new TomarInput());
        }

        // Spawns iniciales de slimes
        int posID = 0;
        for (Slime slime : slimes) {
            stage.addActor(slime);
            if (posID > ayudanteMapas.getPuntosDeSpawn().size() - 1) posID = 0;
            Vector2 spawn = ayudanteMapas.getPuntosDeSpawn().get(posID);
            slime.setPosicionPx(spawn.x, spawn.y);
            posID++;
        }

        // debug añade el armatest al jugador
        Texture tarma = new Texture(Utils.obtenerRutaInterna("armas/arma1.png"));
        tarma.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        Arma armatest = new Arma("AK47", world, 0, -5, 65, 21, tarma, 3, 23, 25, 0.1f, 7, 2.0f, 25, 1);
        jugador.setArma(armatest);
        stage.addActor(armatest);

        hud = new Hud(slimes);
    }

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        renderer.render();

        stage.act(delta);
        stage.draw();

        matrizDebug.set(camara.combined).scl(Constantes.PPM);
        box2DDebugRenderer.render(world, matrizDebug);

        // UI
        hud.update(delta);
        hud.draw();

        /*
        //camara.zoom += (float) (0.1 * delta);
        //stage.getViewport().getCamera().position.set(mapaAnchoPx / 2f, mapaAltoPx / 2f, 0);
        stage.getViewport().getCamera().update();

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
            // Terrenos
            boolean piso = false, techo = false, paredD = false, paredI = false;
            for (Terreno terreno : terrenos) {
                Polygon t = terreno.figura;
                Polygon rectPoligono = Utils.rectanguloAPoligono(new Rectangle(jugador.getX(), jugador.getY(),
                    jugador.getWidth(), jugador.getHeight()));

                if (Intersector.overlapConvexPolygons(rectPoligono, t)) {
                    float penIzq = (rectPoligono.getX() + rectPoligono.getScaleX()) - t.getX();     // slime entra por la izquierda del terreno
                    float penDer = (t.getX() + t.getScaleY()) - rectPoligono.getX();                // entra por la derecha
                    float penAbajo = (rectPoligono.getY() + rectPoligono.getScaleY()) - t.getY();   // entra por debajo (golpea techo)
                    float penArriba = (t.getY() + t.getScaleY()) - rectPoligono.getY();             // entra por arriba (aterriza)

                    float min = Math.min(Math.min(penIzq, penDer), Math.min(penAbajo, penArriba));

                    if (min == penArriba && jugador.getMoveY() <= 0) {
                        jugador.setY(t.getY() + t.getScaleY());   // lo apoya encima
                        jugador.setMoveY(0);
                    } else if (min == penAbajo && jugador.getMoveY() > 0) {
                        jugador.setY(t.getY() - jugador.getHeight());
                        jugador.cancelarMovimientoY();
                    } else if (min == penIzq) {
                        jugador.setX(t.getX() - jugador.getWidth());
                    } else if (min == penDer) {
                        jugador.setX(t.getX() + t.getScaleX());
                    }
                }

                jugador.actualizarColliders();

                techo |= Utils.verificarTocandoTerreno(jugador.techoCollider, terreno);
                paredD |= Utils.verificarTocandoTerreno(jugador.paredderCollider, terreno);
                piso |= Utils.verificarTocandoTerreno(jugador.pisoCollider, terreno);
                paredI |= Utils.verificarTocandoTerreno(jugador.paredizqCollider, terreno);
                if (piso && jugador.getMoveY() <= 0) {
                    jugador.setY(terreno.figura.getY() + terreno.figura.getScaleY());
                    jugador.setMoveY(0);
                }
            }

            jugador.setTocandoTecho(techo);
            jugador.setTocandoParedD(paredD);
            jugador.setTocandoPiso(piso);
            jugador.setTocandoParedI(paredI);

            // Balas
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

         */
    }

    private void update(float delta) {
        for (Slime slime : slimes) {
            if (slime.isEstaMuerto()) continue;
            slime.setBloqueado(!hud.isCuentaAtrasFinalizada());
            if (slime.getY() < ayudanteMapas.getyLetal()) {
                slime.morir();
                continue;
            }
            TomarInput input = inputsPorSlime.get(slime.getId());
            slime.update(delta, input != null ? input : new TomarInput());
        }

        float tiempoFrame = Math.min(delta, 0.25f); // Limite a delta en caso de lag spike (como si fuera a pasar xdd)
        acumulador += tiempoFrame; // libgdx te amo estas re bien documentado
        while (acumulador >= Constantes.TPP) {
            acumulador -= Constantes.TPP;
            world.step(Constantes.TPP, 6 ,2);
        }

        actualizarCamara();

        batch.setProjectionMatrix(camara.combined);
        renderer.setView(camara);
    }

    private void actualizarCamara() {
        Vector2 posCamara = ayudanteMapas.getPosCamara();
        camara.position.set(new Vector3(posCamara.x, posCamara.y, 0));
        camara.update();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width,height);
        viewport.update(width, height);
        hud.resize(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        mapa.dispose();
        renderer.dispose();
        batch.dispose();
        hud.dispose();
    }

    public World getWorld() {
        return world;
    }
}
