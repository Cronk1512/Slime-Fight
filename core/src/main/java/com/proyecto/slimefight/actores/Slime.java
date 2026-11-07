package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import utiles.Config;
import utiles.Constantes;
import utiles.TomarInput;
import com.badlogic.gdx.math.Rectangle;
import utiles.Utils;

public class Slime extends Actor {

    // Cuerpo
    private Body cuerpo;
    private int contactosPiso = 0;

    // Sprites
    private Sprite ojos;
    private Sprite boca;
    public Rectangle hitbox;
    private Rectangle mano;
    public Rectangle pisoCollider;
    public Rectangle paredizqCollider;
    public Rectangle paredderCollider;
    public Rectangle techoCollider;
    Texture textura = new Texture(Utils.obtenerRutaInterna("slime.png"));
    private TextureRegion texturaSlime;
    private boolean volteado;
    private float width = 32;
    private float height = 32;

    // cosas de animacion de inclinacion
    float walkTimer = 0;
    float animScaleX = 1.0f;
    float animScaleY = 1.0f;
    float timerAplastar = 0;
    private float inclinacion = 0f;
    private static final float MAX_INCLINACION = 16f;
    private static final float LERP_SPEED = 4.0f;
    private static final float REBOTE_IDLE = 0.03f;

    // vars de movimiento
    private float moveX;
    private float velYPrevia;

    // flags
    boolean puedeSaltar = true;
    boolean puedeDisparar = false;
    boolean tocandoPiso = false;
    boolean tocandoTecho = false;
    boolean tocandoParedD = false;
    boolean tocandoParedI = false;
    boolean debeAterrizar = false;

    // stats
    private float porcentajeVida = 0;
    private float vidas = 3;
    private float velocidad = 5.0f; // Metros por segundo (1m = 32p)
    private float poder_salto = 12.0f;
    private Arma arma;

    public Slime(Body cuerpo, Sprite ojos, Sprite boca, Color color, boolean dadoVuelta) {
        this.setSize(width, height);
        hitbox = new Rectangle(getX(), getY(), getScaleX(), getScaleY());
        pisoCollider = new Rectangle(getX(), getY()-2, getWidth(), getHeight());
        paredizqCollider = new Rectangle(getX()-2, getY(), getWidth(), getHeight());
        paredderCollider = new Rectangle(getX()+2, getY(), getWidth(), getHeight());
        techoCollider = new Rectangle(getX(), getY()+2, getWidth(), getHeight());

        this.cuerpo = cuerpo;
        cuerpo.setUserData(this);
        this.ojos = ojos;
        this.boca = boca;
        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        texturaSlime = new TextureRegion(textura);

        if (dadoVuelta) {
            darVuelta();
        }

        setColor(color);
    }

    /*
    @Override
    public void act (float delta) {

    }
    */

    public void update(float delta, TomarInput input) {
        moverPersonaje(delta, input);
        sincronizarConCuerpo();
        if (arma != null && mano != null) arma.update(delta, mano.getX(), mano.getY(), volteado);
    }

    private void sincronizarConCuerpo() {
        setPosition(
            cuerpo.getPosition().x * Constantes.PPM - getWidth() / 2f,
            cuerpo.getPosition().y * Constantes.PPM - getHeight() / 2f
        );
    }

    private void moverPersonaje(float delta, TomarInput input) {
        // Movimiento horizontal
        moveX = ((input.derecha ? 1 : 0) - (input.izquierda ? 1 : 0));

        // Movimiento vertical
        boolean estabaEnPiso = tocandoPiso;
        tocandoPiso = contactosPiso > 0;
        if (puedeSaltar && input.saltar) {
            cuerpo.setLinearVelocity(cuerpo.getLinearVelocity().x, 0);
            cuerpo.applyLinearImpulse(0, cuerpo.getMass() * poder_salto, cuerpo.getWorldCenter().x, cuerpo.getWorldCenter().y, true);
            timerAplastar = 0; // cancela animacion de aterrizar
            puedeSaltar = false;
            debeAterrizar = true;
            animScaleY = 1.3f;
            animScaleX = 0.7f;
        }


        if (tocandoPiso && !estabaEnPiso) { // Aterrizo
            puedeSaltar = true;
            float fuerzaImpacto = MathUtils.clamp(Math.abs(velYPrevia) / poder_salto, 0.2f, 1.0f);
            animScaleX = 1.5f * fuerzaImpacto;
            animScaleY = 0.4f / fuerzaImpacto;
            timerAplastar = 1f;
        }

        if (!input.saltar && cuerpo.getLinearVelocity().y > 0) {
            cuerpo.setLinearVelocity(cuerpo.getLinearVelocity().x, 0);
        }

        velYPrevia = cuerpo.getLinearVelocity().y;
        cuerpo.setLinearVelocity(moveX * velocidad, cuerpo.getLinearVelocity().y);

        // Shoot ze gun
        if (!input.disparar) puedeDisparar = true;
        if (input.disparar && puedeDisparar && arma != null) {
            arma.disparar();
            puedeDisparar = false;
        }

        // Voltear si va para atras
        if (moveX < 0 && !volteado) {
            darVuelta();
        }
        if (moveX > 0 && volteado) {
            darVuelta();
        }

        // Animacion
        float objetivoInclinacion = 0f;
        if (moveX != 0) {
            objetivoInclinacion = moveX == 1 ? MAX_INCLINACION : -MAX_INCLINACION;
        }

        inclinacion = MathUtils.lerp(inclinacion, objetivoInclinacion, delta * LERP_SPEED);

        if (!tocandoPiso) {
            // estirar al saltar
            if (cuerpo.getLinearVelocity().y > 0) {
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
            if (moveX == 0 && Math.abs(cuerpo.getLinearVelocity().y) < 0.01f) {
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

    public void actualizarColliders() {
        if (volteado) {
            mano = new Rectangle(getX() + 5, getY() + getHeight()/2, 1, 1);
        } else {
            mano = new Rectangle(getX() + getWidth() - 5, getY() + getHeight()/2, 1, 1);
        }
        hitbox = new Rectangle(getX() + 5, getY() + 5, getWidth() -10, getHeight() -10);

        float grosor = 2f;
        float margen = 4f;
        pisoCollider = new Rectangle(getX() + margen, getY()-grosor, getWidth()-margen*2, grosor);
        techoCollider = new Rectangle(getX() + margen, getY()+getHeight(), getWidth()-margen*2, grosor);
        paredizqCollider = new Rectangle(getX()-grosor, getY() + margen, grosor, getHeight()-margen*2);
        paredderCollider = new Rectangle(getX()+getWidth(), getY() + margen, grosor, getHeight()-margen*2);
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

        Utils.dibujarSlime(batch, texturaSlime, this, cuerpo, inclinacion, animScaleX, animScaleY);

        ojos.draw(batch);
        boca.draw(batch);
    }

    public void recibirDano(Bala bala) {
        porcentajeVida += bala.dano;
        int dir = bala.dirDerecha() ? 1 : -1;
        System.out.println(porcentajeVida);
        aplicarImpulso(dir * bala.knockback, 15);
    }

    public void aplicarImpulso(float x, float y) {
        cuerpo.applyLinearImpulse(cuerpo.getMass() * x, cuerpo.getMass() * y, cuerpo.getWorldCenter().x, cuerpo.getWorldCenter().y, true);
    }

    public void darVuelta() {
        volteado = !volteado;
        ojos.flip(true, false);
        boca.flip(true, false);
    }

    public void setPosicionPx(float x, float y) {
        cuerpo.setTransform(x / Constantes.PPM, (y + getHeight() / 2f) / Constantes.PPM, 0);
        cuerpo.setLinearVelocity(0, 0);
        sincronizarConCuerpo();
    }

    public void sumarContactoPiso(int d) {
        contactosPiso += d;
    }

}
