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
import com.proyecto.slimefight.Jugador;
import com.proyecto.slimefight.Main;
import com.proyecto.slimefight.ManagerJuego;
import com.proyecto.slimefight.actores.Arma;
import com.proyecto.slimefight.actores.Caja;
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
    private final String[] mapas = {"mapa/mapaTest.tmx" /*, ... , ...*/};
    private int mapaActual = 0;
    private ManagerJuego manager = new ManagerJuego();
    private float timerArmas = 0;
    private ArrayList<Arma> armas;
    private ArrayList<Caja> cajas;

    ArrayList<Slime> slimes;

    public PantallaJuego(Main main) {
        super(main);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        Texture tojo = new Texture(Utils.obtenerRutaInterna("ojos/ojos3.png"));
        Texture tboca = new Texture(Utils.obtenerRutaInterna("bocas/boca3.png"));
        tojo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        tboca.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        Texture bojo = new Texture(Utils.obtenerRutaInterna("ojos/ojos1.png"));
        Texture bboca = new Texture(Utils.obtenerRutaInterna("bocas/boca1.png"));
        bojo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bboca.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Jugadores placeholder
        Jugador jugador1 = new Jugador(0, "Luxor89");
        Jugador jugador2 = new Jugador(1, "Cronk1512");
        jugador1.setOjosSlime(new Sprite(tojo));
        jugador1.setBocaSlime(new Sprite(tboca));
        jugador1.setColorSlime(Color.WHITE);
        jugador2.setOjosSlime(new Sprite(bojo));
        jugador2.setBocaSlime(new Sprite(bboca));
        jugador2.setColorSlime(Color.SKY);

        manager.añadirJugador(jugador1);
        manager.añadirJugador(jugador2);

        cargarRonda(mapas[mapaActual]);        // <- primero: crea world, stage y slimes
        hud = new Hud(manager.getJugadores()); // <- ahora getSlime() ya no es null
    }

    private void cargarRonda(String ruta) {
        descartarRonda(); // descarta cosas mapa previo

        // Inicializar cosas
        camara = new OrthographicCamera(Config.ancho , Config.alto);
        viewport = new ExtendViewport(Config.ancho, Config.alto, camara);
        world = new World(new Vector2(0, -Constantes.GRAVEDAD), false);
        box2DDebugRenderer = new Box2DDebugRenderer();
        mapa = new TmxMapLoader().load(ruta);

        ayudanteMapas = new AyudanteMapas(mapa, this);
        renderer = ayudanteMapas.leerMapa();
        stage = new Stage(viewport);
        slimes = new ArrayList<>();
        cajas = new ArrayList<>();
        armas = new ArrayList<>();
        Gdx.input.setInputProcessor(inputs);

        // contact listener bullshit
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact c) { Utils.contactoPies(c, 1); Utils.contactoParedes(c, 1); Utils.contactoCaja(c); Utils.contactoBala(c); }
            @Override public void endContact(Contact c) { Utils.contactoPies(c, -1);  Utils.contactoParedes(c, -1); }
            @Override public void preSolve(Contact c, Manifold m) {}
            @Override public void postSolve(Contact c, ContactImpulse i) {}
        });

        for (Jugador j : manager.getJugadores()) {
            Slime s = crearSlime(j, world);
            j.setSlime(s);
            s.setId(j.getId());
            inputsPorSlime.put(j.getId(), j.getId() == idLocal ? inputs : new TomarInput()); // el local usa el input del teclado, el resto uno propio (por ahora vacío)
            s.setOyenteMuerte(this::alMorirSlime);
            slimes.add(s);
        }

        // Spawns de armas
        for (int i = 0; i < ayudanteMapas.getPuntosDeArmas().size(); i++) {
            Vector2 v = ayudanteMapas.getPuntosDeArmas().get(i);
            Caja caja = new Caja(v.x, v.y, world, stage, armas);
            cajas.add(caja);
            stage.addActor(caja);
        }

        Texture tarma = new Texture(Utils.obtenerRutaInterna("armas/arma1.png"));
        tarma.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        armas.add(new Arma("AK47", world, 0, -5, 65, 27, tarma, 3, 27, 25, 0.1f, 7,5, 1.0f, 25, 1));


        // Spawns iniciales de slimes
        int posID = 0;
        for (Slime slime : slimes) {
            stage.addActor(slime);
            if (posID > ayudanteMapas.getPuntosDeSpawn().size() - 1) posID = 0;
            Vector2 spawn = ayudanteMapas.getPuntosDeSpawn().get(posID);
            slime.setPosicionPx(spawn.x, spawn.y);
            posID++;
        }

        if (hud != null) hud.reiniciarCuentaAtras();

        if (hud != null) hud.actualizarSlimes();
    }

    private void descartarRonda() {
        inputsPorSlime.clear();
        if (stage != null) stage.dispose();
        if (mapa != null) mapa.dispose();
        if (renderer != null) renderer.dispose();
        if (world != null) world.dispose();
        if (box2DDebugRenderer != null) box2DDebugRenderer.dispose();
    }

    private void alMorirSlime(Slime muerto) {
        Jugador victima = manager.getJugador(muerto.getId());
        Jugador asesino = manager.getJugador(muerto.getAtacanteValido());
        manager.alMorir(victima, asesino);
    }

    private Slime crearSlime(Jugador jugador, World mundo) {
        Slime s = new Slime(Utils.crearCuerpo(32, 32, 26, 32, BodyDef.BodyType.DynamicBody, mundo),
            new Sprite(jugador.getOjosSlime()), new Sprite(jugador.getBocaSlime()), jugador.getColorSlime(), false);
        return s;
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
    }

    private void update(float delta) {
        manager.update(delta, hud.isCuentaAtrasFinalizada());

        if (manager.consumirCambioDeMapa()) {
            mapaActual = (mapaActual + 1) % mapas.length;   // o sorteado
            cargarRonda(mapas[mapaActual]);
            return;   // el resto del frame usaría objetos recién destruidos
        }

        for (Caja caja : cajas) {
            Slime s = caja.getRecogidaPor();
            if (s != null && !s.isEstaMuerto() && !caja.isDestruida()) caja.generarArma(s);
        }
        cajas.removeIf(Caja::isDestruida);

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
        batch.dispose();
        hud.dispose();
    }

    public World getWorld() {
        return world;
    }
}
