package com.proyecto.slimefight.actores;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import utiles.Config;
import utiles.Constantes;
import utiles.TomarInput;
import com.badlogic.gdx.math.Rectangle;
import utiles.Utils;

public class Slime extends Actor {

    private int id;

    // Cuerpo
    private Body cuerpo;
    private int contactosPiso = 0;
    private int contactosParedD = 0;
    private int contactosParedI = 0;

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
    private float coyote_time = 0f;

    // vars de movimiento
    private float moveX;
    private float velYPrevia;
    private float impulsoX = 0f;

    // flags
    boolean bloqueado = false; // Hace que no pueda tomar ninguna accion (para el inicio del juego)
    boolean puedeSaltar = true;
    boolean puedeDisparar = false;
    boolean tocandoPiso = false;
    boolean tocandoParedD = false;
    boolean tocandoParedI = false;
    boolean saltando = false;
    boolean soltoBotonSalto = true; // Inicializado en true para evitar auto-saltos al iniciar
    boolean estaMuerto = false;

    // stats
    private float porcentajeVida = 0;
    private float velocidad = 6.5f; // Metros por segundo (1m = 32p)
    private float poder_salto = 12.0f;
    private int limiteSaltoPared = 2;
    private int saltoParedActual = 0;
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

    public void update(float delta, TomarInput input) {
        if (!bloqueado) moverPersonaje(delta, input);
        sincronizarConCuerpo();
        actualizarColliders();
        if (arma != null && mano != null) arma.update(delta, mano.getX(), mano.getY(), volteado, this);
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
        tocandoParedD = contactosParedD > 0;
        tocandoParedI = contactosParedI > 0;

        // Detectar si el usuario acaba de presionar el botón de salto en este frame específico
        boolean saltoRecienPresionado = input.saltar && soltoBotonSalto;

        // Salto normal
        if (puedeSaltar && saltoRecienPresionado) {
            cuerpo.setLinearVelocity(cuerpo.getLinearVelocity().x, poder_salto);
            timerAplastar = 0; // cancela animacion de aterrizar
            puedeSaltar = false;
            saltando = true;
            animScaleY = 1.3f;
            animScaleX = 0.7f;
            soltoBotonSalto = false; // Consumimos el input para que no se repita
        }

        // Salto en la misma pared (Rebote sutil para re-enganchar)
        if (saltoRecienPresionado && !tocandoPiso && saltoParedActual < limiteSaltoPared) {
            if (tocandoParedD) {
                impulsoX = -1.2f;
                saltoParedActual += 1;
                cuerpo.setLinearVelocity(impulsoX, poder_salto / 1.15f);
                puedeSaltar = false;
                saltando = true;
                animScaleY = 1.3f;
                animScaleX = 0.7f;
                soltoBotonSalto = false; // Consumimos el input
            } else if (tocandoParedI) {
                impulsoX = 1.2f;
                saltoParedActual += 1;
                cuerpo.setLinearVelocity(impulsoX, poder_salto / 1.15f);
                puedeSaltar = false;
                saltando = true;
                animScaleY = 1.3f;
                animScaleX = 0.7f;
                soltoBotonSalto = false; // Consumimos el input
            }
        }

        // Actualizamos el flag de soltar el botón de salto
        if (!input.saltar) {
            soltoBotonSalto = true;
        }

        if (tocandoPiso && !estabaEnPiso) { // Aterrizo
            puedeSaltar = true;
            saltoParedActual = 0;
            coyote_time = 0;
            float fuerzaImpacto = MathUtils.clamp(Math.abs(velYPrevia) / poder_salto, 0.2f, 1.0f);
            animScaleX = 1.5f * fuerzaImpacto;
            animScaleY = 0.4f / fuerzaImpacto;
            timerAplastar = 1f;
        }

        // corta el salto para mas precision
        if (saltando && !input.saltar && cuerpo.getLinearVelocity().y > 0) {
            cuerpo.setLinearVelocity(cuerpo.getLinearVelocity().x, 0);
        }

        // en caso de knockback al aire para que el if de arriba no deje cortar el salto
        if (cuerpo.getLinearVelocity().y <= 0) saltando = false;

        // si te caes como un bot no te deja saltar (PERO CON COYOTE TIME)
        if (cuerpo.getLinearVelocity().y < 0 && puedeSaltar) {
            coyote_time += delta * 2f;
            if (coyote_time > Constantes.COYOTE_TIME) {
                puedeSaltar = false;
            }
        }

        velYPrevia = cuerpo.getLinearVelocity().y;
        cuerpo.setLinearVelocity(moveX * velocidad + impulsoX, cuerpo.getLinearVelocity().y);

        // Impulsos
        if (impulsoX != 0) {
            impulsoX = MathUtils.lerp(impulsoX, 0, delta * 5f);
            if (Math.abs(impulsoX) < 0.5f) impulsoX = 0;
        }

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
    }

    public void morir() {
        if (estaMuerto) return;
        estaMuerto = true;
        if (arma != null) {
            arma.destruir();
            arma = null;
        }
        cuerpo.getWorld().destroyBody(cuerpo);
        remove();
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
        aplicarImpulso(dir * bala.knockback, 2);
    }

    public void aplicarImpulso(float x, float y) {
        impulsoX += x * (porcentajeVida/100);
        cuerpo.setLinearVelocity(cuerpo.getLinearVelocity().x, cuerpo.getLinearVelocity().y + y);
        saltando = false;
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

    public void sumarContactoPared(int d, boolean paredDerecha) {
        if (paredDerecha) {
            contactosParedD += d;
            return;
        }
        contactosParedI += d;
    }

    public void setArma(Arma arma) {
        this.arma = arma;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public float getPorcentajeVida() {
        return porcentajeVida;
    }

    public boolean isEstaMuerto() {
        return estaMuerto;
    }

    public Texture getTexturaCuerpo() { return textura; }

    public Sprite getOjos() { return ojos; }

    public Sprite getBoca() { return boca; }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }
}
