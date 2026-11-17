package com.proyecto.slimefight.interfaz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import utiles.Utils;

public class Botones extends TextButton {

    public Botones(String texto, BitmapFont fuente) {
        super(texto, crearEstilo(fuente));
    }

    private static TextButtonStyle crearEstilo(BitmapFont fuente) {
        // Puedes cargar texturas para estado normal y presionado (aquí usamos un placeholder o texturas tuyas)
        Texture texNormal = new Texture(Utils.obtenerRutaInterna("BotonJugar.png")); // Ajusta tu ruta
        Texture texPresionado = new Texture(Utils.obtenerRutaInterna("BotonJugarPre.png"));

        texNormal.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        texPresionado.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextButtonStyle estilo = new TextButtonStyle();
        estilo.font = fuente;
        estilo.fontColor = Color.WHITE;
        estilo.downFontColor = Color.LIGHT_GRAY;

        estilo.up = new TextureRegionDrawable(new TextureRegion(texNormal));
        estilo.down = new TextureRegionDrawable(new TextureRegion(texPresionado));
        estilo.over = estilo.up; // Opcional para hover si es escritorio

        return estilo;
    }
}
