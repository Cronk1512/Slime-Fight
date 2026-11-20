package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.proyecto.slimefight.Main;
import utiles.Config;
import utiles.Utils;

public class PantallaMenuPrincipal extends Pantalla {
    private final Texture fondo;
    private Stage stage;
    private SpriteBatch batch;
    private ExtendViewport viewport;
    private OrthographicCamera camara;
    private BitmapFont fuente;

    public PantallaMenuPrincipal(Main main) {
        super(main);
        fondo = new Texture(Utils.obtenerRutaInterna("fondo_puntos.png"));
        fondo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    @Override
    public void show() {
        camara = new OrthographicCamera();
        viewport = new ExtendViewport(Config.ancho, Config.alto, camara);
        stage = new Stage(viewport);
        batch = new SpriteBatch();

        // asignamos el input processor al stage para que detecte clics en los botones
        Gdx.input.setInputProcessor(stage);

        // fuente temporal
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

        fuente.getData().setScale(2f); // Escalar si se ve muy chica

        //tabla para organizar la interfaz
        Table tabla = new Table();
        tabla.setFillParent(true);
        stage.addActor(tabla);

        // Estilo básico de texto para los botones nativos de LibGDX (puedes cambiarlo por tu clase de botones)
        TextButton.TextButtonStyle estiloBotón = new TextButton.TextButtonStyle();
        estiloBotón.font = fuente;
        estiloBotón.fontColor = com.badlogic.gdx.graphics.Color.WHITE;

        // Botón Jugar
        TextButton btnJugar = new TextButton("JUGAR", estiloBotón);
        btnJugar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                main.setScreen(new PantallaJuego(main));
            }
        });

        TextButton btnMulti = new TextButton("MULTIJUGADOR", estiloBotón);
        btnMulti.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                main.setScreen(new PantallaMultijugador(main));
            }
        });

        // Botón Salir
        TextButton btnSalir = new TextButton("SALIR", estiloBotón);
        btnSalir.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        // Añadir elementos a la tabla con espaciados
        tabla.center();
        tabla.add(btnJugar).padBottom(20).row();
        tabla.add(btnMulti).padBottom(20).row();
        tabla.add(btnSalir);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.draw(fondo, 0, 0, Config.ancho, Config.alto);
        batch.end();

        // Actualizar y dibujar la interfaz (Stage)
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (fondo != null) fondo.dispose();
        if (batch != null) batch.dispose();
        if (stage != null) stage.dispose();
        if (fuente != null) fuente.dispose();
    }
}
