package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import utiles.Config;
import utiles.TomarInput;
import com.badlogic.gdx.math.Rectangle;
import utiles.Utils;

public class Slime extends Actor {


    // Sprites
    private Sprite ojos;
    private Sprite boca;
    private Rectangle hitbox;
    private TextureRegion texturaSlime;
    private boolean volteado;

    // cosas de animacion de inclinacion
    float walkTimer = 0;
    float animScaleX = 1.0f;
    float animScaleY = 1.0f;
    float timerAplastar = 0;
    private float inclinacion = 0f;
    private static final float MAX_INCLINACION = 16f;
    private static final float LERP_SPEED = 4.0f;
    private static final float REBOTE_IDLE = 0.03f;

    // con getScaleX() o Y podes obtener la escala actual, pero width y height determinan ancho y alto default.
    private float width = 50;
    private float height = 50;
    private float moveX;
    private float moveY;
    boolean puedeSaltar = true; // TODO: en vez de 0 sería el piso (aunque tambien deberia poder saltar al tocar una pared)
    float gravedad;

    private final float VELOCIDAD = 300;
    private final float PODER_SALTO = 900;

    public Slime(Sprite ojos, Sprite boca, boolean dadoVuelta) {
        this.setSize(width, height);
        hitbox = new Rectangle(getX(), getY(), getScaleX(), getScaleY());

        this.ojos = ojos;
        this.boca = boca;
        Texture textura = new Texture(Utils.obtenerRutaInterna("slime.png"));
        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        texturaSlime = new TextureRegion(textura);


        if (dadoVuelta) {
            darVuelta();
        }

        setColor(Color.RED);
    }

    /*
    @Override
    public void act (float delta) {

    }
    */

    public void update(float delta, TomarInput input) {
        moverPersonaje(delta, input);
    }

    private void moverPersonaje(float delta, TomarInput input) {
        // Movimiento horizontal
        moveX = ((input.derecha ? 1 : 0) - (input.izquierda ? 1 : 0));
        setX(getX() + moveX * VELOCIDAD * delta);

        // Movimiento vertical
        gravedad = (moveY > 0) ? Config.gravedad : Config.gravedad * 1.5f;

        if (puedeSaltar && input.arriba) {
            moveY = PODER_SALTO;
            timerAplastar = 0; // cancela animacion de aterrizar
            puedeSaltar = false;
            animScaleY = 1.3f;
            animScaleX = 0.7f;
        }
        if (!input.arriba && moveY > 0) moveY = 0;
        if (getY() > 0) {
            moveY -= gravedad * delta;
        }
        setY(getY() + moveY * delta);
        if (getY() < 0) {
            puedeSaltar = true;
            setY(0);
            float fuerzaImpacto = MathUtils.clamp(Math.abs(moveY) / PODER_SALTO, 0.2f, 1.0f);

            animScaleX = 1.5f * fuerzaImpacto;
            animScaleY = 0.4f / fuerzaImpacto;
            timerAplastar = 1f;
            moveY = 0;
        }

        // Voltear si va para atras
        if (moveX < 0 && !volteado) {
            darVuelta();
        }
        if (moveX > 0 && volteado) {
            darVuelta();
        }

        // Animacion procedural
        float objetivoInclinacion = 0f;
        if (moveX != 0) {
            objetivoInclinacion = moveX == 1 ? MAX_INCLINACION : -MAX_INCLINACION;
        }

        inclinacion = MathUtils.lerp(inclinacion, objetivoInclinacion, delta * LERP_SPEED);

        if (!puedeSaltar) {
            // estirar al saltar
            if (moveY > 0) {
                // mientras va para arriba
                animScaleY = MathUtils.lerp(animScaleY, 1.0f, delta * 2.5f);
                animScaleX = MathUtils.lerp(animScaleX, 1.0f, delta * 2.5f);
            } else {
                // mientras cae
                animScaleY = MathUtils.lerp(animScaleY, 1.75f, delta * 1.2f);
                animScaleX = MathUtils.lerp(animScaleX, 0.65f, delta * 1.2f);
            }
        } else if (timerAplastar > 0) {
            // AL CAER AL PISO
            timerAplastar -= delta; // velocidad en recuperarse

            float wobbleWave = MathUtils.cos(timerAplastar * MathUtils.PI * 4f) * timerAplastar; // magia matematica
            animScaleX = 1.0f + (wobbleWave * 0.4f);
            animScaleY = 1.0f - (wobbleWave * 0.3f);
        } else {
            // default ass walk
            if (moveX == 0 && moveY == 0) {
                // si no se mueve rebota como animacion idle
                walkTimer += delta * 12f;
                animScaleY = 1.0f - (MathUtils.sin(walkTimer) * REBOTE_IDLE);
                animScaleX = 1.0f + (MathUtils.sin(walkTimer) * REBOTE_IDLE);
            } else {
                // si se esta moviendo no rebota
                walkTimer = 0;
                animScaleX = MathUtils.lerp(animScaleX, 1.0f, delta * 8f);
                animScaleY = MathUtils.lerp(animScaleY, 1.0f, delta * 8f);
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (volteado) {
            ojos.setPosition(getX()-5 + inclinacion/2, getY());
            ojos.setSize(getWidth(), getHeight() * animScaleY);
            boca.setPosition(getX()-5 + inclinacion/2, getY());
            boca.setSize(getWidth(), getHeight() * animScaleY);
        } else {
            ojos.setPosition(getX()+5 + inclinacion/2, getY());
            ojos.setSize(getWidth(), getHeight() * animScaleY);
            boca.setPosition(getX()+5 + inclinacion/2, getY());
            boca.setSize(getWidth(), getHeight() * animScaleY);
        }

        Utils.dibujarSlime(batch, texturaSlime, this, inclinacion, animScaleX, animScaleY);

        ojos.draw(batch);
        boca.draw(batch);
    }

    public void darVuelta() {
        volteado = !volteado;
        ojos.flip(true, false);
        boca.flip(true, false);
    }

}
