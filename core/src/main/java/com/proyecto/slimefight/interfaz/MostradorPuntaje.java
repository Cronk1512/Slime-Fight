package com.proyecto.slimefight.interfaz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.proyecto.slimefight.actores.Slime;
import com.proyecto.slimefight.clases.Jugador;
import com.proyecto.slimefight.clases.Punto;
import utiles.Config;
import utiles.Utils;

import java.util.ArrayList;

public class MostradorPuntaje {
    private static final float ALTO_BARRA = 64f;
    private static final float SEPARACION = 96f; // distancia entre filas
    private static final float TAM_ICONO = 56f;
    private static final float MARGEN = 130f; // espacio a izquierda y derecha
    private static final float ESC_PATRON = 3f; // cada píxel del patrón se ve de 3x3
    private static final float PAUSA_INICIAL = 0.8f; // se explica solo dale bot no tengo que dejar un comentario por variable
    private static final float DUR_POR_PUNTO = 0.6f; // segundos que tarda en expandirse el segmento por punto
    private static final float PAUSA_FINAL = 1.5f;
    private static final float TIEMPO_FADE_IN = 0.15f; // del texto
    private static final float TIEMPO_FADE_OUT = 0.4f; // del texto

    private final Stage stage = new Stage(new ScreenViewport());
    private final Texture pixel = new Texture(Utils.obtenerRutaInterna("pixel_blanco.png"));
    private final Texture patron = new Texture(Utils.obtenerRutaInterna("fondo_puntos.png"));
    private final BitmapFont fuente = new BitmapFont();
    private final BitmapFont fuenteTexto = new BitmapFont();

    private boolean visible = false;
    private boolean terminado = false;
    private float tiempo = 0;
    private float duracion = 0;

    public MostradorPuntaje() {
        patron.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        patron.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fuente.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fuente.getData().setScale(2f);
        fuenteTexto.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fuenteTexto.getData().setScale(1.5f);
    }

    private static class Segmento {
        float valor, inicio, dur;
        Color color;
        boolean nuevo;
        String texto;   // "+3 Kill"
    }

    private static class Fila {
        IconoSlime icono;
        ArrayList<Segmento> segmentos = new ArrayList<>();
    }

    /** puntosAntes[i] = cantidad de entradas en jugadores.get(i).getPuntos() al empezar la ronda. */
    public void mostrar(ArrayList<Jugador> jugadores, int[] puntosAntes) {
        stage.clear();
        ArrayList<Fila> filas = new ArrayList<>();
        float fin = PAUSA_INICIAL;

        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            Slime s = j.getSlime();
            Fila fila = new Fila();
            fila.icono = new IconoSlime(s.getTexturaCuerpo(), s.getOjos(), s.getBoca(), s.getColor(), TAM_ICONO);

            float t = PAUSA_INICIAL;
            for (int idx = 0; idx < j.getPuntos().size(); idx++) {
                Punto p = j.getPuntos().get(idx);
                Segmento segmento = new Segmento();
                segmento.valor = p.getValor();
                segmento.color = new Color(p.getColor());
                segmento.texto = "+" + p.getValor() + " " + p.getNombre();
                segmento.nuevo = idx >= puntosAntes[i];
                if (segmento.nuevo) {
                    segmento.inicio = t;
                    segmento.dur = DUR_POR_PUNTO * Math.max(1, p.getValor());
                    t += segmento.dur + 0.1f;
                }
                fila.segmentos.add(segmento);
            }
            fin = Math.max(fin, t);
            filas.add(fila);
        }

        Tablero tablero = new Tablero(filas);
        for (Fila f : filas) tablero.addActor(f.icono);
        stage.addActor(tablero);

        duracion = fin + PAUSA_FINAL;
        tiempo = 0;
        terminado = false;
        visible = true;
    }

    public void update(float delta) {
        if (!visible) return;
        stage.act(delta);
        tiempo += delta;
        if (tiempo >= duracion) terminado = true;
    }

    public void draw() {
        if (!visible) return;
        stage.getViewport().apply();
        stage.draw();
    }

    public void ocultar() {
        visible = false;
    }

    public boolean isTerminado() {
        return terminado;
    }

    private class Tablero extends Group {
        private final ArrayList<Fila> filas;
        private float t = 0;

        Tablero(ArrayList<Fila> filas) { this.filas = filas; }

        @Override public void act(float delta) { super.act(delta); t += delta; }

        @Override public void draw(Batch b, float parentAlpha) {
            float w = stage.getWidth(), h = stage.getHeight();
            Color viejo = new Color(b.getColor());

            // Fondo oscuro
            b.setColor(0f, 0f, 0f, 0.8f);
            b.draw(pixel, 0, 0, w, h);

            float izq = MARGEN;
            float der = w - MARGEN;
            float unidad = (der - izq) / Config.puntosParaGanar;
            float altoTotal = filas.size() * SEPARACION;
            float arriba = (h + altoTotal) / 2f;
            float yMin = arriba - altoTotal;

            // Líneas punteadas cada punto
            b.setColor(1f, 1f, 1f, 0.25f);
            for (int k = 1; k < Config.puntosParaGanar; k++) {
                float x = izq + k * unidad;
                for (float y = yMin - 10; y < arriba + 10; y += 14) b.draw(pixel, x - 1, y, 2, 7);
            }
            // Eje izquierdo y línea de WIN
            b.setColor(1f, 1f, 1f, 0.9f);
            b.draw(pixel, izq - 2, yMin - 10, 4, altoTotal + 20);
            b.draw(pixel, der - 2, yMin - 10, 4, altoTotal + 20);
            fuente.setColor(1f, 1f, 1f, 1f);
            fuente.draw(b, "WIN!", der - 60, arriba + 50, 120, Align.center, false);

            // Barras
            for (int i = 0; i < filas.size(); i++) {
                Fila f = filas.get(i);
                float y = arriba - (i + 1) * SEPARACION + (SEPARACION - ALTO_BARRA) / 2f;
                f.icono.setPosition(izq - TAM_ICONO - 24, y + (ALTO_BARRA - TAM_ICONO) / 2f);

                float x = izq;
                for (Segmento s : f.segmentos) {
                    float prog = 1f;
                    if (s.nuevo) prog = Interpolation.pow2Out.apply(MathUtils.clamp((t - s.inicio) / s.dur, 0f, 1f));
                    float ancho = s.valor * unidad * prog;
                    if (ancho > 0.5f) dibujarSegmento(b, x, y, ancho, ALTO_BARRA, s.color);

                    // Etiqueta "+X Nombre": solo para puntos nuevos, aparece al terminar de expandirse
                    if (s.nuevo) {
                        float desdeInicio = t - s.inicio;            // tiempo desde que empezó a expandirse
                        float desdeFin = t - (s.inicio + s.dur);     // tiempo desde que terminó (negativo mientras crece)

                        if (desdeInicio > 0 && desdeFin < TIEMPO_FADE_OUT) {
                            float aEntrada = MathUtils.clamp(desdeInicio / TIEMPO_FADE_IN, 0f, 1f);
                            float aSalida = 1f - MathUtils.clamp(desdeFin / TIEMPO_FADE_OUT, 0f, 1f);
                            float a = Math.min(aEntrada, aSalida);

                            // Sube suavemente durante toda la vida de la etiqueta
                            float vida = desdeInicio / (s.dur + TIEMPO_FADE_OUT);
                            float subida = Interpolation.pow2Out.apply(MathUtils.clamp(vida, 0f, 1f)) * 18f;

                            // Sigue al borde derecho del segmento mientras crece
                            float anchoFinal = s.valor * unidad;
                            float yTexto = y + ALTO_BARRA + 22 + subida;

                            fuenteTexto.setColor(0f, 0f, 0f, a);
                            fuenteTexto.draw(b, s.texto, x + 2, yTexto - 2, anchoFinal, Align.center, false);
                            fuenteTexto.setColor(s.color.r, s.color.g, s.color.b, a);
                            fuenteTexto.draw(b, s.texto, x, yTexto, anchoFinal, Align.center, false);
                        }
                    }
                    x += ancho;
                }
            }

            b.setColor(viejo);
            super.draw(b, parentAlpha);   // dibuja los iconos de los slimes
        }

        private void dibujarSegmento(Batch b, float x, float y, float w, float h, Color c) {
            // base translúcida
            b.setColor(c.r, c.g, c.b, 0.35f);
            b.draw(pixel, x, y, w, h);

            // patrón teñido; el origen del patrón queda anclado a la izquierda del segmento,
            // así se "revela" al expandirse en vez de estirarse
            b.setColor(c.r, c.g, c.b, 1f);
            b.draw(patron, x, y, w, h, 0, 0, (int) (w / ESC_PATRON), (int) (h / ESC_PATRON), false, false);

            // borde
            float g = Math.min(3f, w / 2f);
            b.setColor(c);
            b.draw(pixel, x, y, w, g);
            b.draw(pixel, x, y + h - g, w, g);
            b.draw(pixel, x, y, g, h);
            b.draw(pixel, x + w - g, y, g, h);
        }
    }



    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    public void dispose() {
        stage.dispose();
        pixel.dispose();
        patron.dispose();
        fuente.dispose();
        fuenteTexto.dispose();
    }
}
