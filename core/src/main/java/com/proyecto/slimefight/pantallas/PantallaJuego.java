package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.proyecto.slimefight.clases.Jugador;
import com.proyecto.slimefight.Main;
import com.proyecto.slimefight.ManagerJuego;
import com.proyecto.slimefight.actores.Arma;
import com.proyecto.slimefight.actores.Caja;
import com.proyecto.slimefight.actores.Slime;
import com.proyecto.slimefight.interfaz.Hud;
import com.proyecto.slimefight.interfaz.MostradorPuntaje;
import com.proyecto.slimefight.red.RedUDP;
import com.proyecto.slimefight.red.SesionRed;
import utiles.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PantallaJuego extends Pantalla {
    private static final long TIMEOUT_MS = 6000;
    private static final float INTERVALO_SNAP = 1f / 30f;
    private static final Random RNG = new Random();

    private AyudanteArmas ayudanteArmas;
    private Stage stage;
    private TomarInput inputs = new TomarInput();
    private OrthographicCamera camara;
    private ExtendViewport viewport;
    float acumulador = 0;
    private int[] puntosAntes = new int[0];
    private boolean mostrandoPuntaje = false;

    private World world;
    private SpriteBatch batch;
    private OrthogonalTiledMapRenderer renderer;
    private TiledMap mapa;
    private AyudanteMapas ayudanteMapas;
    private Hud hud;
    private MostradorPuntaje mostradorPuntaje;
    private final Map<Integer, TomarInput> inputsPorSlime = new HashMap<>();
    private int idLocal = 0;   // el slime que controla esta PC
    private final String[] mapas = {"mapa/MapaTuberias1.tmx" /*, ... , ...*/};
    private int mapaActual = 0;
    private ManagerJuego manager = new ManagerJuego();
    private float timerArmas;
    private ArrayList<Arma> armas;
    private ArrayList<Caja> cajas;

    ArrayList<Slime> slimes;

    // Red
    private final boolean enRed = SesionRed.activa();
    private final boolean esCliente = enRed && !SesionRed.esHost;
    private final boolean esHost = !esCliente;        // sin red tambien es host
    private boolean salido = false;

    private int ronda = 0;
    private int rondaHost = 1;          // cliente: ultima ronda vista en un snapshot
    private boolean goHost = false;     // cliente: el host ya termino la cuenta atras
    private int tickSnap = 0;           // host: contador / cliente: ultimo tick aplicado
    private float acumSnap = 0;
    private int seqEnvio = 0;
    private long ultimoRxServidor = 0;
    private int siguienteIdCaja = 1;

    // host
    private final Map<InetSocketAddress, Integer> idPorDir = new HashMap<>();
    private final Map<Integer, Integer> ultSeq = new HashMap<>();
    private final Map<Integer, Integer> maskUltima = new HashMap<>();
    private final Map<Integer, Integer> maskPend = new HashMap<>();
    private final Map<Integer, Long> ultimoRxPeer = new HashMap<>();
    private final Set<Integer> desconectados = new HashSet<>();
    private final Map<Integer, Integer> serialArma = new HashMap<>();     // host: veces que le dieron arma / cliente: ultima vista
    private final Map<Integer, Integer> atacanteMuerte = new HashMap<>();

    private final Map<String, Texture> texturas = new HashMap<>();

    public PantallaJuego(Main main) {
        super(main);
    }

    private Texture textura(String ruta) {
        return texturas.computeIfAbsent(ruta, r -> {
            Texture t = new Texture(Utils.obtenerRutaInterna(r));
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return t;
        });
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        if (enRed) {
            idLocal = SesionRed.idLocal;
            crearJugadoresDeRed();
            long ahora = System.currentTimeMillis();
            ultimoRxServidor = ahora;
            for (Map.Entry<Integer, InetSocketAddress> e : SesionRed.direcciones.entrySet()) {
                idPorDir.put(e.getValue(), e.getKey());
                ultimoRxPeer.put(e.getKey(), ahora);
            }
        } else {
            crearJugadoresPlaceholder();
        }

        cargarRonda(mapas[mapaActual]);
        hud = new Hud(manager.getJugadores());
        mostradorPuntaje = new MostradorPuntaje();
    }

    private void crearJugadoresDeRed() {
        for (Jugador j : SesionRed.jugadores.values()) { // TreeMap: mismo orden en todas las PCs
            j.setOjosSlime(new Sprite(textura(j.getRutaOjos())));
            j.setBocaSlime(new Sprite(textura(j.getRutaBoca())));
            j.setColorSlime(Jugador.PALETA[j.getColorIdx()]);
            manager.añadirJugador(j);
        }
    }

    private void crearJugadoresPlaceholder() {
        Jugador jugador1 = new Jugador(0, "Luxor89");
        Jugador jugador2 = new Jugador(1, "Botardo");
        Jugador jugador3 = new Jugador(3, "Viscous");
        jugador1.setOjosSlime(new Sprite(textura("ojos/ojos3.png")));
        jugador1.setBocaSlime(new Sprite(textura("bocas/boca3.png")));
        jugador1.setColorSlime(Color.WHITE);
        jugador2.setOjosSlime(new Sprite(textura("ojos/ojos1.png")));
        jugador2.setBocaSlime(new Sprite(textura("bocas/boca1.png")));
        jugador2.setColorSlime(Color.SKY);
        jugador3.setOjosSlime(new Sprite(textura("ojos/ojos2.png")));
        jugador3.setBocaSlime(new Sprite(textura("bocas/boca2.png")));
        jugador3.setColorSlime(Color.GREEN);
        manager.añadirJugador(jugador1);
        manager.añadirJugador(jugador2);
        manager.añadirJugador(jugador3);
    }

    private void cargarRonda(String ruta) {
        descartarRonda(); // descarta cosas mapa previo

        ronda = esCliente ? Math.max(ronda + 1, rondaHost) : ronda + 1;
        goHost = false;

        // Inicializar cosas
        camara = new OrthographicCamera(Config.ancho, Config.alto);
        viewport = new ExtendViewport(Config.ancho, Config.alto, camara);
        world = new World(new Vector2(0, -Constantes.GRAVEDAD), false);
        mapa = new TmxMapLoader().load(ruta);

        ayudanteMapas = new AyudanteMapas(mapa, this);
        renderer = ayudanteMapas.leerMapa();
        stage = new Stage(viewport);
        slimes = new ArrayList<>();
        cajas = new ArrayList<>();
        Gdx.input.setInputProcessor(inputs);
        timerArmas = 0;

        // Cargar armas desde JSON (despues de crear world y ayudanteMapas)
        ayudanteArmas = new AyudanteArmas();
        armas = ayudanteArmas.cargarArmasDesdeJson("armas/armas.json", world);
        for (int i = 0; i < armas.size(); i++) {
            armas.get(i).setTipo(i); // indice en la lista "armas" (se manda por red)
        }

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
            // local: teclado. Resto: input propio (host: lo llena la red / cliente: lo llena el snapshot)
            inputsPorSlime.put(j.getId(), j.getId() == idLocal ? inputs : new TomarInput());
            s.setOyenteMuerte(this::alMorirSlime);
            slimes.add(s);
        }

        // Spawns de cajas
        if (!esCliente) generarArmas(ayudanteMapas.getPuntosDeArmas().size());

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

        puntosAntes = new int[manager.getJugadores().size()];
        for (int i = 0; i < puntosAntes.length; i++)
            puntosAntes[i] = manager.getJugadores().get(i).getPuntos().size(); // cantidad de entradas
    }

    private void descartarRonda() {
        if (ayudanteArmas != null) {
            ayudanteArmas.dispose();
            ayudanteArmas = null;
        }
        timerCamara = 0;
        inputsPorSlime.clear();
        serialArma.clear();
        atacanteMuerte.clear();
        siguienteIdCaja = 1;
        if (stage != null) stage.dispose();
        if (mapa != null) mapa.dispose();
        if (renderer != null) renderer.dispose();
        if (world != null) world.dispose();
    }

    private void alMorirSlime(Slime muerto) {
        atacanteMuerte.put(muerto.getId(), muerto.getAtacanteValido()); // el host se lo informa a los clientes
        Jugador victima = manager.getJugador(muerto.getId());
        Jugador asesino = manager.getJugador(muerto.getAtacanteValido());
        manager.alMorir(victima, asesino);
    }

    private Slime crearSlime(Jugador jugador, World mundo) {
        return new Slime(Utils.crearCuerpo(32, 32, 26, 32, BodyDef.BodyType.DynamicBody, mundo),
            new Sprite(jugador.getOjosSlime()), new Sprite(jugador.getBocaSlime()), jugador.getColorSlime(), false);
    }

    @Override
    public void render(float delta) {
        update(delta);
        if (salido) return;

        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        renderer.render();

        stage.act(delta);
        stage.draw();

        // UI
        hud.update(delta);
        hud.draw();
        mostradorPuntaje.update(delta);
        mostradorPuntaje.draw();
    }

    private boolean cuentaFinalizada() {
        return esCliente ? goHost : hud.isCuentaAtrasFinalizada();
    }

    private void siguienteRonda() {
        mapaActual = esCliente ? (rondaHost - 1) % mapas.length : (mapaActual + 1) % mapas.length;
        cargarRonda(mapas[mapaActual]);
    }

    private void update(float delta) {
        if (enRed) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { salirDePartida(); return; }
            procesarRed(delta);
            if (salido) return;
        }

        if (mostrandoPuntaje) {                       // juego congelado mientras se ven los puntos
            if (mostradorPuntaje.isTerminado()) {
                if (esCliente && rondaHost <= ronda) return; // espera a que el host cargue la ronda nueva
                mostradorPuntaje.ocultar();
                mostrandoPuntaje = false;
                siguienteRonda();
            }
            return;
        }

        // el host ya paso de ronda y este cliente no se enteró del fin por su cuenta
        if (esCliente && rondaHost > ronda) { siguienteRonda(); return; }

        boolean go = cuentaFinalizada();
        manager.update(delta, go);

        if (manager.consumirCambioDeMapa()) {
            mostradorPuntaje.mostrar(manager.getJugadores(), puntosAntes);
            mostrandoPuntaje = true;
            return;
        }

        if (!esCliente) {
            for (Caja caja : cajas) {
                if (caja.getY() < ayudanteMapas.getyLetal()) {
                    caja.destruir();
                    continue;
                }
                Slime s = caja.getRecogidaPor();
                if (s != null && !s.isEstaMuerto() && !caja.isDestruida()) {
                    caja.generarArma(s);
                    serialArma.merge(s.getId(), 1, Integer::sum);
                }
            }
        }
        cajas.removeIf(Caja::isDestruida);

        // Spawnear cajas cada ~12s
        if (!esCliente) {
            timerArmas += delta;
            if (timerArmas >= 12f && cajas.size() < 6) {
                generarArmas(2);
                timerArmas = 0;
            }
        }

        if (esHost && enRed) aplicarInputsRemotos();

        for (Slime slime : slimes) {
            if (slime.isEstaMuerto()) continue;
            slime.setBloqueado(!go);
            // la muerte por caida la decide el host
            if (!esCliente && slime.getY() < ayudanteMapas.getyLetal()) {
                slime.morir();
                continue;
            }
            TomarInput input = inputsPorSlime.get(slime.getId());
            slime.update(delta, input != null ? input : new TomarInput());
        }

        float tiempoFrame = Math.min(delta, 0.25f); // Limite a delta en caso de lag spike
        acumulador += tiempoFrame;
        while (acumulador >= Constantes.TPP) {
            acumulador -= Constantes.TPP;
            world.step(Constantes.TPP, 6, 2);
        }

        actualizarCamara(delta);

        batch.setProjectionMatrix(camara.combined);
        renderer.setView(camara);
    }

    private void generarArmas(int cant) {
        if (ayudanteMapas == null || ayudanteMapas.getPuntosDeArmas() == null
            || ayudanteMapas.getPuntosDeArmas().isEmpty() || armas == null || armas.isEmpty()) {
            return;
        }

        ArrayList<Vector2> puntos = ayudanteMapas.getPuntosDeArmas();
        ArrayList<Vector2> puntosExcluidos = new ArrayList<>();
        for (int i = 0; i < cant; i++) {
            Vector2 v = puntos.get(RNG.nextInt(puntos.size()));
            if (puntosExcluidos.contains(v)) { // para que no se generen 2 cajas en el mismo spot
                i--;
                continue;
            }
            puntosExcluidos.add(v);
            Caja caja = new Caja(v.x, v.y, world, stage, armas);
            caja.setId(siguienteIdCaja++);
            cajas.add(caja);
            stage.addActor(caja);

            if (puntosExcluidos.size() == puntos.size()) puntosExcluidos.clear();
        }
    }

    // ===================================================================== RED

    private static int empaquetar(TomarInput i) {
        return (i.izquierda ? 1 : 0) | (i.derecha ? 2 : 0) | (i.saltar ? 4 : 0)
            | (i.abajo ? 8 : 0) | (i.disparar ? 16 : 0) | (i.lanzar_arma ? 32 : 0);
    }

    private static void desempaquetar(int m, TomarInput i) {
        i.izquierda = (m & 1) != 0;
        i.derecha = (m & 2) != 0;
        i.saltar = (m & 4) != 0;
        i.abajo = (m & 8) != 0;
        i.disparar = (m & 16) != 0;
        i.lanzar_arma = (m & 32) != 0;
    }

    private static int toInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    private void procesarRed(float delta) {
        long ahora = System.currentTimeMillis();

        RedUDP.Mensaje m;
        while (!salido && SesionRed.activa() && (m = SesionRed.red.recibir()) != null) {
            procesarMensaje(m, ahora);
        }
        if (salido) return;
        if (!SesionRed.activa()) { salirDePartida(); return; }

        if (esCliente) {
            // el input se manda cada frame (es chiquito); el host acumula con OR para no perder taps
            SesionRed.red.enviar("IN|" + (seqEnvio++) + "|" + empaquetar(inputs), SesionRed.servidor);
            if (ahora - ultimoRxServidor > TIMEOUT_MS) salirDePartida();
        } else {
            for (Map.Entry<Integer, Long> e : ultimoRxPeer.entrySet())
                if (ahora - e.getValue() > TIMEOUT_MS) desconectados.add(e.getKey());
            for (Slime s : slimes)
                if (desconectados.contains(s.getId()) && !s.isEstaMuerto()) s.morir();
            enviarSnapshot(delta);
        }
    }

    private void procesarMensaje(RedUDP.Mensaje m, long ahora) {
        String[] p = m.texto.split("\\|", -1);

        if (esHost) {
            Integer id = idPorDir.get(m.origen);
            if (id == null) return;
            ultimoRxPeer.put(id, ahora);
            switch (p[0]) {
                case "IN": {
                    if (p.length < 3 || desconectados.contains(id)) return;
                    int seq = toInt(p[1]);
                    Integer last = ultSeq.get(id);
                    if (last != null && seq <= last) return; // llego desordenado
                    ultSeq.put(id, seq);
                    int mask = toInt(p[2]);
                    maskUltima.put(id, mask);
                    maskPend.merge(id, mask, (a, b) -> a | b);
                    break;
                }
                case "PERFIL":
                case "HELLO":
                    SesionRed.red.enviar("START", m.origen);
                    break;
                case "BYE":
                    desconectados.add(id);
                    break;
                default:
                    break;
            }
        } else {
            if (!m.origen.equals(SesionRed.servidor)) return;
            ultimoRxServidor = ahora;
            switch (p[0]) {
                case "ST":
                    aplicarSnapshot(p);
                    break;
                case "CLOSE":
                    salirDePartida();
                    break;
                default:
                    break;
            }
        }
    }

    // ---- Host

    private void aplicarInputsRemotos() {
        for (Map.Entry<Integer, TomarInput> e : inputsPorSlime.entrySet()) {
            int id = e.getKey();
            if (id == idLocal) continue;
            int mask = maskUltima.getOrDefault(id, 0) | maskPend.getOrDefault(id, 0);
            if (desconectados.contains(id)) mask = 0;
            desempaquetar(mask, e.getValue());
            maskPend.put(id, 0);
        }
    }

    private void enviarSnapshot(float delta) {
        if (!enRed || SesionRed.direcciones.isEmpty()) return;
        acumSnap += delta;
        if (acumSnap < INTERVALO_SNAP) return;
        acumSnap = 0;

        StringBuilder sb = new StringBuilder("ST|");
        sb.append(ronda).append('|').append(++tickSnap).append('|')
            .append(hud.isCuentaAtrasFinalizada() ? 1 : 0).append('|');

        boolean primero = true;
        for (Slime s : slimes) {
            if (!primero) sb.append(';');
            primero = false;
            agregarSlime(sb, s);
        }
        sb.append('|');
        primero = true;
        for (Caja c : cajas) {
            if (c.isDestruida()) continue;
            if (!primero) sb.append(';');
            primero = false;
            sb.append(c.getId()).append(',').append(Math.round(c.getX() * 10)).append(',').append(Math.round(c.getY() * 10));
        }

        String msg = sb.toString();
        for (InetSocketAddress d : SesionRed.direcciones.values()) SesionRed.red.enviar(msg, d);
    }

    private void agregarSlime(StringBuilder sb, Slime s) {
        int id = s.getId();
        if (s.isEstaMuerto()) {
            sb.append(id).append(",0,0,0,0,1,0,0,0,-1,0,").append(atacanteMuerte.getOrDefault(id, -1));
            return;
        }
        TomarInput in = inputsPorSlime.get(id);
        int mask = in == null ? 0 : empaquetar(in);
        Arma a = s.getArma();
        sb.append(id).append(',')
            .append(Math.round(s.getCentroXPx() * 10)).append(',')
            .append(Math.round(s.getCentroYPx() * 10)).append(',')
            .append(Math.round(s.getVelX() * 100)).append(',')
            .append(Math.round(s.getVelY() * 100)).append(',')
            .append(s.isVolteado() ? 2 : 0).append(',')
            .append(mask).append(',')
            .append(Math.round(s.getImpulsoX() * 100)).append(',')
            .append(Math.round(s.getPorcentajeVida())).append(',')
            .append(a != null ? a.getTipo() : -1).append(',')
            .append(serialArma.getOrDefault(id, 0)).append(',')
            .append(-1);
    }

    // ---- Cliente

    private void aplicarSnapshot(String[] p) {
        if (p.length < 6) return;
        int rondaS = toInt(p[1]);
        if (rondaS > rondaHost) rondaHost = rondaS;
        if (rondaS != ronda) return;       // snapshot de otra ronda
        int tick = toInt(p[2]);
        if (tick <= tickSnap) return;      // viejo / desordenado
        tickSnap = tick;
        goHost = (toInt(p[3]) & 1) != 0;

        if (!p[4].isEmpty()) {
            for (String e : p[4].split(";")) {
                String[] f = e.split(",");
                if (f.length < 12) continue;
                int[] v = new int[12];
                for (int i = 0; i < 12; i++) v[i] = toInt(f[i]);
                Slime s = buscarSlime(v[0]);
                if (s != null) aplicarSlimeRed(s, v);
            }
        }

        Set<Integer> vistas = new HashSet<>();
        if (!p[5].isEmpty()) {
            for (String e : p[5].split(";")) {
                String[] f = e.split(",");
                if (f.length < 3) continue;
                int id = toInt(f[0]);
                float x = toInt(f[1]) / 10f, y = toInt(f[2]) / 10f;
                vistas.add(id);
                Caja caja = buscarCaja(id);
                if (caja == null) {
                    caja = new Caja(x, y, world, stage, armas);
                    caja.setId(id);
                    cajas.add(caja);
                    stage.addActor(caja);
                } else {
                    caja.sincronizar(x, y, 10f);
                }
            }
        }
        for (Caja c : cajas) if (!vistas.contains(c.getId())) c.destruir();
    }

    private void aplicarSlimeRed(Slime s, int[] v) {
        boolean muerto = (v[5] & 1) != 0;
        if (muerto) {
            if (!s.isEstaMuerto()) { s.forzarAtacante(v[11]); s.morir(); } // morir() avisa al manager local
            return;
        }
        if (s.isEstaMuerto()) return;

        int id = s.getId();
        boolean local = id == idLocal;

        s.setPorcentajeVida(v[8]);
        s.sincronizarImpulso(v[7] / 100f); // knockback nuevo

        float cx = v[1] / 10f, cy = v[2] / 10f, vx = v[3] / 100f, vy = v[4] / 100f;
        if (local) {
            s.corregirDesdeRed(cx, cy, vx, vy, 16f, 0.2f);   // zona muerta grande: la prediccion manda
        } else {
            TomarInput in = inputsPorSlime.get(id);
            if (in != null) desempaquetar(v[6], in);          // el remoto se simula con su input real
            if (((v[5] & 2) != 0) != s.isVolteado()) s.darVuelta();
            s.corregirDesdeRed(cx, cy, vx, vy, 4f, 0.35f);
        }

        sincronizarArma(s, local, v[9], v[10]);
    }

    private void sincronizarArma(Slime s, boolean local, int tipo, int serial) {
        int id = s.getId();
        Arma actual = s.getArma();
        if (tipo >= 0 && tipo < armas.size()) {
            if (serial != serialArma.getOrDefault(id, 0)) {  // le dieron un arma nueva
                serialArma.put(id, serial);
                darArma(s, tipo);
            } else if (actual == null && !local) {
                darArma(s, tipo);
            }
        } else if (actual != null && !local) {
            actual.destruir();
            s.setArma(null);
        }
        // el local que tira su arma lo predice solo; no se le "devuelve" hasta que haya serial nuevo
    }

    private void darArma(Slime s, int tipo) {
        if (s.getArma() != null) s.getArma().destruir();
        Arma nueva = new Arma(armas.get(tipo));
        stage.addActor(nueva);
        s.setArma(nueva);
    }

    private Slime buscarSlime(int id) {
        for (Slime s : slimes) if (s.getId() == id) return s;
        return null;
    }

    private Caja buscarCaja(int id) {
        for (Caja c : cajas) if (!c.isDestruida() && c.getId() == id) return c;
        return null;
    }

    private void salirDePartida() {
        if (salido) return;
        salido = true;
        if (enRed && SesionRed.activa()) {
            if (esHost) {
                for (InetSocketAddress d : SesionRed.direcciones.values()) SesionRed.red.enviar("CLOSE", d);
            } else {
                SesionRed.red.enviar("BYE", SesionRed.servidor);
            }
        }
        SesionRed.cerrar();
        main.setScreen(new PantallaMenuPrincipal(main));
    }

    // ================================================================== Camara

    private float timerCamara = 0;
    private void actualizarCamara(float delta) {
        if (manager.getEstado() == ManagerJuego.Estado.FIN_RONDA) {
            // Hacer zoom en el slime ganador
            timerCamara += delta * 2;
            float frac = Math.min(timerCamara / 3f, 1f);
            for (Slime s : slimes) {
                if (s.isEstaMuerto()) continue;
                float posx = MathUtils.lerp(camara.position.x, (s.getX() + s.getWidth() / 2), delta * 1.4f);
                float posy = MathUtils.lerp(camara.position.y, (s.getY() + s.getHeight() / 2), delta * 1.4f);
                camara.position.set(new Vector3(posx, posy, 0));
                camara.zoom = Math.max(1 - Interpolation.exp10Out.apply(frac) * 0.6f, 0.4f);
            }
        } else {
            Vector2 posCamara = ayudanteMapas.getPosCamara();
            camara.position.set(new Vector3(posCamara.x, posCamara.y, 0));
            camara.zoom = 1;
        }
        camara.update();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
        hud.resize(width, height);
        mostradorPuntaje.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hud.dispose();
        for (Texture t : texturas.values()) t.dispose();
        texturas.clear();
    }

    public World getWorld() {
        return world;
    }
}
