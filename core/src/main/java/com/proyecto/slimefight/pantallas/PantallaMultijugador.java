
package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.proyecto.slimefight.Main;
import com.proyecto.slimefight.clases.Jugador;
import com.proyecto.slimefight.interfaz.IconoSlime;
import com.proyecto.slimefight.red.RedUDP;
import com.proyecto.slimefight.red.SesionRed;
import utiles.Config;
import utiles.Utils;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;


public class PantallaMultijugador extends Pantalla {

    private enum Estado { MENU, HOST, CLIENTE_CONECTANDO, CLIENTE_EN_LOBBY }

    private static final int PUERTO_DEFECTO = 7777;
    private static final int MAX_JUGADORES = 4;
    private static final int MIN_JUGADORES = 2;
    private static final long INTERVALO_MS = 1000;
    private static final long TIMEOUT_MS = 5000;
    private static final long CUENTA_MS = 3000;

    private static class Peer {
        final int id;
        Jugador jugador;
        final InetSocketAddress dir; // null = el host mismo
        long ultimo;

        Peer(int id, Jugador jugador, InetSocketAddress dir) {
            this.id = id;
            this.jugador = jugador;
            this.dir = dir;
            this.ultimo = System.currentTimeMillis();
        }
    }

    // UI general
    private Stage stage;
    private SpriteBatch batch;
    private ExtendViewport viewport;
    private OrthographicCamera camara;
    private final Texture fondo;
    private Texture pixel;
    private TextureRegionDrawable blanco;
    private BitmapFont fuente;
    private BitmapFont fuenteCampo;
    private TextButton.TextButtonStyle estiloBoton;
    private Label.LabelStyle estiloLabel;
    private TextField.TextFieldStyle estiloCampo;
    private Table tabla;
    private Label lblEstado;
    private TextField campoNombre, campoIp, campoPuerto;

    // Texturas de slime
    private Texture texCuerpo;
    private Texture[] texOjos;
    private Texture[] texBocas;

    // UI del lobby
    private final Table[] columnas = new Table[MAX_JUGADORES];
    private final String[] claves = new String[MAX_JUGADORES]; // para no reconstruir columnas sin cambios
    private float tamSlime = 96;
    private Label lblPie;
    private Container<Actor> contSlimeLocal;
    private Image imgColor, imgOjos, imgBoca;
    private TextField campoNombreLocal;
    private TextButton btnListo;

    // Red
    private Estado estado = Estado.MENU;
    private RedUDP red;
    private boolean esHost;
    private int idLocal = 0;
    private int siguienteId = 1;
    private InetSocketAddress servidor;
    private Jugador miJugador = crearJugadorInicial();
    private final Map<Integer, Peer> peers = new LinkedHashMap<>();        // solo host
    private final TreeMap<Integer, Jugador> lobby = new TreeMap<>();       // lo que se muestra
    private long ultimoEnvio = 0, ultimoRx = 0, inicioConexion = 0, cuentaInicio = 0;
    private int segundosInicio = -1;
    private boolean lobbyCambio = false;
    private boolean partidaIniciada = false;
    private boolean salir = false;

    // Valores recordados del menu
    private String nombreGuardado = "Jugador";
    private String ipGuardada = "192.168.0.";
    private String puertoGuardado = String.valueOf(PUERTO_DEFECTO);

    public PantallaMultijugador(Main main) {
        super(main);
        fondo = new Texture(Utils.obtenerRutaInterna("fondo_puntos.png"));
        fondo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    // ============================================== Helpers de Jugador / protocolo

    private static Jugador crearJugadorInicial() {
        Jugador j = new Jugador(0, "Jugador");
        aspectoAleatorio(j);
        return j;
    }

    private static void aspectoAleatorio(Jugador j) {
        j.setColorIdx(MathUtils.random(Jugador.PALETA.length - 1));
        j.setOjosIdx(MathUtils.random(Jugador.CANT_OJOS - 1));
        j.setBocaIdx(MathUtils.random(Jugador.CANT_BOCAS - 1));
    }

    private static void copiarAspecto(Jugador de, Jugador a) {
        a.setColorIdx(de.getColorIdx());
        a.setOjosIdx(de.getOjosIdx());
        a.setBocaIdx(de.getBocaIdx());
    }

    /** nombre:color:ojos:boca:listo  (el nombre nunca contiene ':' ';' '|') */
    private static String serializar(Jugador j) {
        String n = j.getNombre() == null || j.getNombre().trim().isEmpty() ? "Jugador" : j.getNombre();
        return n + ":" + j.getColorIdx() + ":" + j.getOjosIdx() + ":" + j.getBocaIdx() + ":" + (j.isListo() ? 1 : 0);
    }

    /** Devuelve null si el texto es invalido. Los indices se acotan a rangos validos. */
    private static Jugador parsear(int id, String s) {
        String[] t = s.split(":", -1);
        if (t.length < 5) return null;
        try {
            Jugador j = new Jugador(id, t[0].isEmpty() ? "Jugador" : t[0]);
            j.setColorIdx(MathUtils.clamp(Integer.parseInt(t[1]), 0, Jugador.PALETA.length - 1));
            j.setOjosIdx(MathUtils.clamp(Integer.parseInt(t[2]), 0, Jugador.CANT_OJOS - 1));
            j.setBocaIdx(MathUtils.clamp(Integer.parseInt(t[3]), 0, Jugador.CANT_BOCAS - 1));
            j.setListo(t[4].equals("1"));
            return j;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ================================================================== UI

    @Override
    public void show() {
        camara = new OrthographicCamera();
        viewport = new ExtendViewport(Config.ancho, Config.alto, camara);
        stage = new Stage(viewport);
        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(stage);

        FreeTypeFontGenerator generador = new FreeTypeFontGenerator(Utils.obtenerRutaInterna("fuentes/Matcha Boom.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 24;
        p.color = Color.LIME;
        p.borderWidth = 2;
        p.borderColor = Color.WHITE;
        p.minFilter = Texture.TextureFilter.Nearest;
        p.magFilter = Texture.TextureFilter.Nearest;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        fuente = generador.generateFont(p);
        generador.dispose();

        fuenteCampo = new BitmapFont();
        fuenteCampo.getData().setScale(1.6f);

        cargarTexturasSlime();

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
        blanco = new TextureRegionDrawable(new TextureRegion(pixel));

        estiloBoton = new TextButton.TextButtonStyle();
        estiloBoton.font = fuente;
        estiloBoton.fontColor = Color.WHITE;
        estiloBoton.overFontColor = Color.YELLOW;
        estiloBoton.downFontColor = Color.GRAY;

        estiloLabel = new Label.LabelStyle(fuente, Color.WHITE);

        SpriteDrawable cursor = (SpriteDrawable) blanco.tint(Color.WHITE);
        cursor.setMinWidth(2);
        SpriteDrawable seleccion = (SpriteDrawable) blanco.tint(new Color(0.3f, 0.5f, 1f, 0.5f));
        SpriteDrawable fondoCampo = (SpriteDrawable) blanco.tint(new Color(0f, 0f, 0f, 0.55f));
        fondoCampo.setLeftWidth(8);
        fondoCampo.setRightWidth(8);
        estiloCampo = new TextField.TextFieldStyle();
        estiloCampo.font = fuenteCampo;
        estiloCampo.fontColor = Color.WHITE;
        estiloCampo.cursor = cursor;
        estiloCampo.selection = seleccion;
        estiloCampo.background = fondoCampo;

        tabla = new Table();
        tabla.setFillParent(true);
        stage.addActor(tabla);

        construirMenu();
    }

    private void cargarTexturasSlime() {
        texCuerpo = new Texture(Utils.obtenerRutaInterna("slime.png"));
        texCuerpo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        texOjos = new Texture[Jugador.CANT_OJOS];
        for (int i = 0; i < texOjos.length; i++) {
            texOjos[i] = new Texture(Utils.obtenerRutaInterna("ojos/ojos" + (i + 1) + ".png"));
            texOjos[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
        texBocas = new Texture[Jugador.CANT_BOCAS];
        for (int i = 0; i < texBocas.length; i++) {
            texBocas[i] = new Texture(Utils.obtenerRutaInterna("bocas/boca" + (i + 1) + ".png"));
            texBocas[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

    // -------------------------------------------------------------- Menu

    private void construirMenu() {
        tabla.clearChildren();
        tabla.center();
        lblPie = null;

        campoNombre = new TextField(nombreGuardado, estiloCampo);
        campoNombre.setMaxLength(12);
        campoIp = new TextField(ipGuardada, estiloCampo);
        campoPuerto = new TextField(puertoGuardado, estiloCampo);
        campoPuerto.setMaxLength(5);
        lblEstado = new Label("", estiloLabel);

        tabla.add(new Label("MULTIJUGADOR", estiloLabel)).colspan(2).padBottom(25).row();
        tabla.add(new Label("Nombre:", estiloLabel)).right().padRight(10);
        tabla.add(campoNombre).width(260).padBottom(10).row();

        tabla.add(boton("CREAR PARTIDA", this::crearPartida)).colspan(2).padBottom(25).row();

        tabla.add(new Label("IP del host:", estiloLabel)).right().padRight(10);
        tabla.add(campoIp).width(260).padBottom(10).row();
        tabla.add(new Label("Puerto:", estiloLabel)).right().padRight(10);
        tabla.add(campoPuerto).width(260).padBottom(10).row();

        Table fila = new Table();
        fila.add(boton("UNIRSE", this::unirse)).padRight(30);
        fila.add(boton("BUSCAR EN LAN", this::buscarEnLan));
        tabla.add(fila).colspan(2).padBottom(25).row();

        tabla.add(boton("VOLVER", this::volverAlMenu)).colspan(2).padBottom(15).row();
        tabla.add(lblEstado).colspan(2);
    }

    // ------------------------------------------------------------- Lobby

    private void construirLobby() {
        tabla.clearChildren();
        tabla.center();

        float anchoCol = Config.ancho / (float) MAX_JUGADORES - 14f;
        tamSlime = Math.min(anchoCol * 0.6f, Config.alto * 0.25f);
        for (int i = 0; i < MAX_JUGADORES; i++) claves[i] = null;

        if (esHost) {
            lblEstado = new Label("Sos el host - Tu IP: " + RedUDP.ipLocal() + "   Puerto: " + red.getPuertoLocal(), estiloLabel);
        } else {
            lblEstado = new Label("Conectado a " + servidor.getAddress().getHostAddress() + ":" + servidor.getPort(), estiloLabel);
        }
        lblPie = new Label("", estiloLabel);

        int cols = MAX_JUGADORES * 2 - 1;
        tabla.add(new Label("LOBBY", estiloLabel)).colspan(cols).padBottom(4).row();
        tabla.add(lblEstado).colspan(cols).padBottom(14).row();

        for (int i = 0; i < MAX_JUGADORES; i++) {
            columnas[i] = new Table();
            columnas[i].top();
            tabla.add(columnas[i]).width(anchoCol).top();
            if (i < MAX_JUGADORES - 1) {
                tabla.add(new Image(blanco.tint(new Color(1f, 1f, 1f, 0.8f)))).width(3).fillY();
            }
        }
        tabla.row();

        tabla.add(lblPie).colspan(cols).padTop(18).row();
        tabla.add(boton("SALIR", this::salirDelLobby)).colspan(cols).padTop(10);

        construirColumnaLocal(anchoCol);
        refrescarLobbyUI();
    }

    /** Columna 0: SOS VOS. Se construye una sola vez y se actualiza "en el lugar". */
    private void construirColumnaLocal(float anchoCol) {
        Table t = columnas[0];

        campoNombreLocal = new TextField(miJugador.getNombre(), estiloCampo);
        campoNombreLocal.setMaxLength(12);
        campoNombreLocal.setDisabled(miJugador.isListo());
        campoNombreLocal.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                miJugador.setNombre(sanitizar(campoNombreLocal.getText()));
                perfilCambio();
            }
        });
        t.add(campoNombreLocal).width(anchoCol - 30).padBottom(10).row();

        contSlimeLocal = new Container<>(crearIcono(miJugador));
        contSlimeLocal.fill();
        t.add(contSlimeLocal).size(tamSlime).padBottom(15).row();

        // Selectores: click para pasar al siguiente
        imgColor = new Image(blanco.tint(miJugador.getColorSlime()));
        imgOjos = new Image(new TextureRegionDrawable(new TextureRegion(texOjos[miJugador.getOjosIdx()])));
        imgBoca = new Image(new TextureRegionDrawable(new TextureRegion(texBocas[miJugador.getBocaIdx()])));
        Color grisFondo = new Color(0.75f, 0.75f, 0.75f, 1f);
        Stack stOjos = new Stack(new Image(blanco.tint(grisFondo)), imgOjos);
        Stack stBoca = new Stack(new Image(blanco.tint(grisFondo)), imgBoca);

        imgColor.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { ciclarColor(); }
        });
        stOjos.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { ciclarOjos(); }
        });
        stBoca.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { ciclarBoca(); }
        });

        Table sel = new Table();
        filaSelector(sel, "COLOR", imgColor);
        filaSelector(sel, "OJOS", stOjos);
        filaSelector(sel, "BOCA", stBoca);
        t.add(sel).padBottom(10).row();

        btnListo = boton(miJugador.isListo() ? "CANCELAR" : "LISTO", this::alternarListo);
        t.add(btnListo);
    }

    private void filaSelector(Table sel, String texto, Actor widget) {
        sel.add(new Label(texto, estiloLabel)).left().padRight(15);
        sel.add(widget).size(38).padBottom(8).left().row();
    }

    private Actor crearIcono(Jugador j) {
        return new IconoSlime(texCuerpo, new Sprite(texOjos[j.getOjosIdx()]), new Sprite(texBocas[j.getBocaIdx()]),
            j.getColorSlime(), tamSlime);
    }

    // ---- acciones de personalizacion (solo el jugador local)

    private void ciclarColor() {
        if (miJugador.isListo()) return;
        miJugador.setColorIdx((miJugador.getColorIdx() + 1) % Jugador.PALETA.length);
        actualizarLocal();
    }

    private void ciclarOjos() {
        if (miJugador.isListo()) return;
        miJugador.setOjosIdx((miJugador.getOjosIdx() + 1) % Jugador.CANT_OJOS);
        actualizarLocal();
    }

    private void ciclarBoca() {
        if (miJugador.isListo()) return;
        miJugador.setBocaIdx((miJugador.getBocaIdx() + 1) % Jugador.CANT_BOCAS);
        actualizarLocal();
    }

    private void alternarListo() {
        miJugador.setListo(!miJugador.isListo());
        btnListo.setText(miJugador.isListo() ? "CANCELAR" : "LISTO");
        campoNombreLocal.setDisabled(miJugador.isListo());
        perfilCambio();
    }

    private void actualizarLocal() {
        imgColor.setDrawable(blanco.tint(miJugador.getColorSlime()));
        imgOjos.setDrawable(new TextureRegionDrawable(new TextureRegion(texOjos[miJugador.getOjosIdx()])));
        imgBoca.setDrawable(new TextureRegionDrawable(new TextureRegion(texBocas[miJugador.getBocaIdx()])));
        contSlimeLocal.setActor(crearIcono(miJugador));
        perfilCambio();
    }

    /** Avisa a la red que mi jugador cambio. */
    private void perfilCambio() {
        if (esHost) {
            lobbyCambio = true; // el jugador del host es el mismo objeto que miJugador
        } else if (red != null && servidor != null) {
            red.enviar("PERFIL|" + serializar(miJugador), servidor);
        }
    }

    // ---- columnas de los demas jugadores

    private void refrescarLobbyUI() {
        if (estado != Estado.HOST && estado != Estado.CLIENTE_EN_LOBBY) return;

        int col = 1;
        for (Jugador j : lobby.values()) {
            if (j.getId() == idLocal) continue;
            if (col >= MAX_JUGADORES) break;
            mostrarRemoto(col++, j);
        }
        for (; col < MAX_JUGADORES; col++) mostrarVacio(col);

        if (lblPie != null) {
            if (segundosInicio >= 0) lblPie.setText("¡Empieza en " + segundosInicio + "!");
            else if (lobby.size() < MIN_JUGADORES) lblPie.setText("Esperando mas jugadores...");
            else lblPie.setText("Esperando que todos esten listos...");
        }
    }

    private void mostrarRemoto(int i, Jugador j) {
        String clave = j.getId() + ":" + serializar(j);
        if (clave.equals(claves[i])) return;
        claves[i] = clave;

        Table t = columnas[i];
        t.clearChildren();
        t.add(new Label(j.getNombre(), estiloLabel)).padBottom(j.getId() == 0 ? 0 : 10).row();
        if (j.getId() == 0) t.add(new Label("(host)", estiloLabel)).padBottom(10).row();
        t.add(crearIcono(j)).size(tamSlime).padBottom(15).row();
        t.add(new Label(j.isListo() ? "¡LISTO!" : "...", estiloLabel));
    }

    private void mostrarVacio(int i) {
        if ("vacio".equals(claves[i])) return;
        claves[i] = "vacio";
        Table t = columnas[i];
        t.clearChildren();
        t.add(new Label("Nadie :c", estiloLabel)).padTop(tamSlime * 0.8f);
    }

    // ---- utilidades de UI

    private TextButton boton(String texto, Runnable accion) {
        TextButton b = new TextButton(texto, estiloBoton);
        b.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                accion.run();
            }
        });
        return b;
    }

    private void mensajeEstado(String txt) {
        if (lblEstado != null) lblEstado.setText(txt);
    }

    // ============================================================ Acciones

    private String leerNombre() {
        String n = sanitizar(campoNombre.getText().trim());
        return n.isEmpty() ? "Jugador" : n;
    }

    /** Saca los caracteres que usa el protocolo como separadores. */
    private static String sanitizar(String s) {
        return s.replaceAll("[|;:]", "_");
    }

    private int leerPuerto() {
        try {
            int p = Integer.parseInt(campoPuerto.getText().trim());
            if (p >= 1024 && p <= 65535) return p;
        } catch (NumberFormatException ignorado) { }
        return -1;
    }

    private void guardarCampos() {
        nombreGuardado = campoNombre.getText();
        ipGuardada = campoIp.getText();
        puertoGuardado = campoPuerto.getText();
    }

    private void crearPartida() {
        int puerto = leerPuerto();
        if (puerto < 0) { mensajeEstado("Puerto invalido (1024-65535)"); return; }
        guardarCampos();
        cerrarRed();

        try {
            red = new RedUDP();
            red.abrir(puerto);
        } catch (SocketException e) {
            red = null;
            mensajeEstado("No se pudo abrir el puerto " + puerto);
            return;
        }

        esHost = true;
        idLocal = 0;
        siguienteId = 1;
        Jugador nuevo = new Jugador(0, leerNombre()); // el host siempre es id 0
        copiarAspecto(miJugador, nuevo);
        miJugador = nuevo;
        peers.clear();
        peers.put(0, new Peer(0, miJugador, null));
        reconstruirLobbyDesdePeers();
        cuentaInicio = 0;
        segundosInicio = -1;
        lobbyCambio = true;
        estado = Estado.HOST;
        construirLobby();
    }

    private void unirse() {
        int puerto = leerPuerto();
        if (puerto < 0) { mensajeEstado("Puerto invalido (1024-65535)"); return; }
        String ip = campoIp.getText().trim();
        InetSocketAddress dir;
        try {
            dir = new InetSocketAddress(ip, puerto);
        } catch (IllegalArgumentException e) {
            mensajeEstado("Direccion invalida");
            return;
        }
        if (dir.isUnresolved()) { mensajeEstado("No se pudo resolver \"" + ip + "\""); return; }

        guardarCampos();
        if (!abrirRedCliente()) return;

        esHost = false;
        servidor = dir;
        miJugador.setNombre(leerNombre());
        miJugador.setListo(false);
        segundosInicio = -1;
        estado = Estado.CLIENTE_CONECTANDO;
        inicioConexion = System.currentTimeMillis();
        ultimoEnvio = 0; // fuerza HELLO inmediato
        mensajeEstado("Conectando...");
    }

    private void buscarEnLan() {
        int puerto = leerPuerto();
        if (puerto < 0) { mensajeEstado("Puerto invalido (1024-65535)"); return; }
        guardarCampos();
        if (!abrirRedCliente()) return;
        red.enviarBroadcast("BUSCAR", puerto);
        mensajeEstado("Buscando partidas en la red local...");
    }

    private boolean abrirRedCliente() {
        if (red != null && red.estaAbierta()) return true;
        try {
            red = new RedUDP();
            red.abrir(0);
            return true;
        } catch (SocketException e) {
            red = null;
            mensajeEstado("No se pudo abrir el socket");
            return false;
        }
    }

    private void iniciarComoHost() {
        reconstruirLobbyDesdePeers();
        for (int i = 0; i < 3; i++) { // x3 por si se pierde algun paquete
            difundir(armarLobby());
            difundir("START");
        }
        SesionRed.direcciones.clear();
        for (Peer p : peers.values()) if (p.dir != null) SesionRed.direcciones.put(p.id, p.dir);
        arrancarPartida();
    }

    private void arrancarPartida() {
        SesionRed.red = red;
        SesionRed.esHost = esHost;
        SesionRed.idLocal = idLocal;
        SesionRed.servidor = servidor;
        SesionRed.jugadores.clear();
        SesionRed.jugadores.putAll(lobby);
        SesionRed.jugadores.putIfAbsent(idLocal, miJugador);
        partidaIniciada = true;
        salir = true;
        // TODO: PantallaJuego debe crear los slimes desde SesionRed.jugadores y sincronizar inputs/estado
        main.setScreen(new PantallaJuego(main));
    }

    private void salirDelLobby() {
        if (esHost) difundir("CLOSE");
        else if (servidor != null) red.enviar("BYE", servidor);
        cerrarRed();
        estado = Estado.MENU;
        construirMenu();
    }

    private void volverAlMenu() {
        cerrarRed();
        salir = true;
        main.setScreen(new PantallaMenuPrincipal(main));
    }

    private void errorYMenu(String msg) {
        cerrarRed();
        estado = Estado.MENU;
        construirMenu();
        mensajeEstado(msg);
    }

    private void cerrarRed() {
        if (red != null) red.cerrar();
        red = null;
        servidor = null;
        peers.clear();
        lobby.clear();
        cuentaInicio = 0;
        segundosInicio = -1;
    }

    // ========================================================= Red (loop)

    private void actualizarRed() {
        if (red == null) return;
        long ahora = System.currentTimeMillis();

        RedUDP.Mensaje m;
        while (red != null && (m = red.recibir()) != null) {
            if (esHost && estado == Estado.HOST) procesarComoHost(m, ahora);
            else procesarComoCliente(m, ahora);
            if (salir) return;
        }
        if (red == null) return;

        switch (estado) {
            case HOST: {
                Iterator<Peer> it = peers.values().iterator();
                while (it.hasNext()) {
                    Peer p = it.next();
                    if (p.dir != null && ahora - p.ultimo > TIMEOUT_MS) { it.remove(); lobbyCambio = true; }
                }

                // Cuenta regresiva cuando todos estan listos
                boolean todos = peers.size() >= MIN_JUGADORES && todosListos();
                if (todos && cuentaInicio == 0) cuentaInicio = ahora;
                if (!todos && cuentaInicio != 0) cuentaInicio = 0;
                int seg = cuentaInicio == 0 ? -1 : (int) Math.ceil((CUENTA_MS - (ahora - cuentaInicio)) / 1000.0);
                if (seg != segundosInicio) { segundosInicio = seg; lobbyCambio = true; }
                if (cuentaInicio != 0 && ahora - cuentaInicio >= CUENTA_MS) { iniciarComoHost(); return; }

                if (lobbyCambio || ahora - ultimoEnvio > INTERVALO_MS) {
                    if (lobbyCambio) { reconstruirLobbyDesdePeers(); refrescarLobbyUI(); }
                    difundir(armarLobby());
                    ultimoEnvio = ahora;
                    lobbyCambio = false;
                }
                break;
            }

            case CLIENTE_CONECTANDO:
                if (ahora - ultimoEnvio > INTERVALO_MS) {
                    red.enviar("HELLO|" + serializar(miJugador), servidor);
                    ultimoEnvio = ahora;
                }
                if (ahora - inicioConexion > TIMEOUT_MS) errorYMenu("No se pudo conectar (sin respuesta)");
                break;

            case CLIENTE_EN_LOBBY:
                if (ahora - ultimoEnvio > INTERVALO_MS) {
                    red.enviar("PERFIL|" + serializar(miJugador), servidor); // keep-alive + sincroniza
                    ultimoEnvio = ahora;
                }
                if (ahora - ultimoRx > TIMEOUT_MS) errorYMenu("Se perdio la conexion con el host");
                break;

            default:
                break;
        }
    }

    // ---- Host

    private void procesarComoHost(RedUDP.Mensaje m, long ahora) {
        String[] p = m.texto.split("\\|");
        switch (p[0]) {
            case "HELLO": {
                Peer existente = buscarPeer(m.origen);
                if (existente == null) {
                    if (peers.size() >= MAX_JUGADORES) { red.enviar("FULL", m.origen); return; }
                    int nuevoId = siguienteId++;
                    Jugador jg = p.length > 1 ? parsear(nuevoId, p[1]) : null;
                    if (jg == null) {
                        jg = new Jugador(nuevoId, "Jugador");
                        aspectoAleatorio(jg);
                    }
                    existente = new Peer(nuevoId, jg, m.origen);
                    peers.put(nuevoId, existente);
                    lobbyCambio = true;
                }
                existente.ultimo = ahora;
                red.enviar("WELCOME|" + existente.id, m.origen);
                break;
            }
            case "PERFIL": {
                Peer pe = buscarPeer(m.origen);
                if (pe == null || p.length < 2) break;
                pe.ultimo = ahora;
                Jugador nuevo = parsear(pe.id, p[1]);
                if (nuevo != null && !serializar(nuevo).equals(serializar(pe.jugador))) {
                    pe.jugador = nuevo;
                    lobbyCambio = true;
                }
                break;
            }
            case "BYE": {
                Peer pe = buscarPeer(m.origen);
                if (pe != null) { peers.remove(pe.id); lobbyCambio = true; }
                break;
            }
            case "BUSCAR":
                red.enviar("ANUNCIO|" + peers.get(0).jugador.getNombre() + "|" + peers.size() + "|" + MAX_JUGADORES, m.origen);
                break;
            default:
                break;
        }
    }

    private Peer buscarPeer(InetSocketAddress dir) {
        for (Peer p : peers.values()) if (dir.equals(p.dir)) return p;
        return null;
    }

    private boolean todosListos() {
        for (Peer p : peers.values()) if (!p.jugador.isListo()) return false;
        return true;
    }

    private void reconstruirLobbyDesdePeers() {
        lobby.clear();
        for (Peer p : peers.values()) lobby.put(p.id, p.jugador);
    }

    private String armarLobby() {
        StringBuilder sb = new StringBuilder("LOBBY|");
        boolean primero = true;
        for (Peer p : peers.values()) {
            if (!primero) sb.append(';');
            sb.append(p.id).append(':').append(serializar(p.jugador));
            primero = false;
        }
        sb.append('|').append(segundosInicio);
        return sb.toString();
    }

    private void difundir(String msg) {
        for (Peer p : peers.values()) if (p.dir != null) red.enviar(msg, p.dir);
    }

    // ---- Cliente

    private void procesarComoCliente(RedUDP.Mensaje m, long ahora) {
        String[] p = m.texto.split("\\|");

        // Respuesta a la busqueda por broadcast: no hace falta estar conectado
        if (p[0].equals("ANUNCIO") && estado == Estado.MENU) {
            campoIp.setText(m.origen.getAddress().getHostAddress());
            String nombre = p.length > 1 ? p[1] : "?";
            String cupo = p.length > 3 ? " (" + p[2] + "/" + p[3] + ")" : "";
            mensajeEstado("Partida encontrada: " + nombre + cupo + "\nToca UNIRSE");
            return;
        }

        if (servidor == null || !servidor.equals(m.origen)) return; // ignorar paquetes ajenos
        ultimoRx = ahora;

        switch (p[0]) {
            case "WELCOME":
                if (estado == Estado.CLIENTE_CONECTANDO && p.length > 1) {
                    try { idLocal = Integer.parseInt(p[1]); } catch (NumberFormatException e) { return; }
                    // El id es final en Jugador: recreamos el local con el id que nos asigno el host
                    Jugador conId = new Jugador(idLocal, miJugador.getNombre());
                    copiarAspecto(miJugador, conId);
                    miJugador = conId;
                    estado = Estado.CLIENTE_EN_LOBBY;
                    ultimoEnvio = ahora;
                    construirLobby();
                }
                break;
            case "LOBBY":
                if (estado == Estado.CLIENTE_EN_LOBBY && p.length > 1) {
                    lobby.clear();
                    for (String entrada : p[1].split(";")) {
                        String[] kv = entrada.split(":", 2);
                        if (kv.length < 2) continue;
                        try {
                            int id = Integer.parseInt(kv[0]);
                            Jugador jg = parsear(id, kv[1]);
                            if (jg != null) lobby.put(id, jg);
                        } catch (NumberFormatException ignorado) { }
                    }
                    segundosInicio = -1;
                    if (p.length > 2) {
                        try { segundosInicio = Integer.parseInt(p[2]); } catch (NumberFormatException ignorado) { }
                    }
                    refrescarLobbyUI();
                }
                break;
            case "START":
                if (estado == Estado.CLIENTE_EN_LOBBY) arrancarPartida();
                break;
            case "FULL":
                errorYMenu("La partida esta llena");
                break;
            case "CLOSE":
                errorYMenu("El host cerro la partida");
                break;
            default:
                break;
        }
    }

    // ============================================================= libGDX

    @Override
    public void render(float delta) {
        actualizarRed();
        if (salir) return; // ya se cambio de pantalla

        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(fondo, 0, 0, Config.ancho, Config.alto);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (!partidaIniciada) cerrarRed(); // si arranco la partida, el socket sigue vivo en SesionRed
        if (fondo != null) fondo.dispose();
        if (batch != null) batch.dispose();
        if (stage != null) stage.dispose();
        if (fuente != null) fuente.dispose();
        if (pixel != null) pixel.dispose();
        if (texCuerpo != null) texCuerpo.dispose();
        if (texOjos != null) for (Texture t : texOjos) t.dispose();
        if (texBocas != null) for (Texture t : texBocas) t.dispose();
    }
}
