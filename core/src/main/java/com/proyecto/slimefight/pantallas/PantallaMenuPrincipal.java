package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.proyecto.slimefight.Main;
import utiles.Config;
import utiles.Utils;

public class PantallaMenuPrincipal extends Pantalla {
    private final Texture fondo;
    private Stage stage;
    private SpriteBatch batch;

    public PantallaMenuPrincipal(Main main) {
        super(main);
        fondo = new Texture(Utils.obtenerRutaInterna("fondo_puntos.png")); // TODO fondo temporal, cambiar esto
        fondo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public void show() {
        stage = new Stage();
        batch = new SpriteBatch();
    }

    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(fondo, 0, 0, Config.ancho, Config.alto);
        batch.end();

        // Aca va lo del menu principal (botones, fondo, etc)
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width,height);
    }

    public void dispose() {
        fondo.dispose();
        batch.dispose();
    }
}
