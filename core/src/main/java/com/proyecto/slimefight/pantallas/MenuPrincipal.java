package com.proyecto.slimefight.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.proyecto.slimefight.Main;

public class MenuPrincipal extends Pantalla {
    private Stage stage;

    public MenuPrincipal(Main main) {
        super(main);
    }

    public void show() {
        stage = new Stage();
    }

    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Aca va lo del menu principal (botones, fondo, etc)
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width,height);
    }

    public void dispose() {

    }
}
