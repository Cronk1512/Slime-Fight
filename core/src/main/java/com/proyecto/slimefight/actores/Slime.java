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
    boolean puedeLanzarArma = false;
    boolean tocandoPiso = false;
    boolean tocandoParedD = false;
    boolean tocandoParedI = false;
    boolean saltando = false;
    boolean soltoBotonSalto = true; // Inicializado en true para evitar auto-saltos al iniciar
    boolean estaMuerto = false;
    boolean agachado = false;

    // stats
    private float porcentajeVida = 0;
    private float velocidad = 6.5f; // Metros por segundo (1m = 32p)
    private float poder_salto = 12.0f;
    private int limiteSaltoPared = 2;
    private int saltoParedActual = 0;
    private Arma arma;

    // detectar ultimo atacante (para cosas de puntos)
    private int ultimoAtacanteId = -1;
    private long msUltimoGolpe;
    private static final long VENTANA_ATRIBUCION_MS = 4000; // Si se muere fuera de esta ventana entonce no points for u (rip bozo + L + skill issue)
    private java.util.function.Consumer<Slime> oyenteMuerte; // esto es re piola te deja guardarte una funcion de otro archivo basicamente
    //Emotes
    private Sprite spriteEmote;
    private float timerEmote = 0f;
    private static final float DURACION_EMOTE = 1.8f; // segundos visible

    public void mostrarEmote(Texture texturaEmote) {
        if (texturaEmote == null) return;
        if (spriteEmote == null) {
            spriteEmote = new Sprite(texturaEmote);
            spriteEmote.setSize(20, 20); // ajusta el tamaño según tu escala en px
        } else {
            spriteEmote.setTexture(texturaEmote);
        }
        timerEmote = DURACION_EMOTE;
    }
    public Slime(Body cuerpo, Sprite ojos, Sprite boca, Color color, boolean dadoVuelta) {
        this.setSize(width, height);
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
        if (timerEmote > 0f) {
            timerEmote -= delta;
            if (timerEmote < 0f) timerEmote = 0f;
        }
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

        // Agarrar arma del piso
        agachado = input.abajo && moveX == 0 && Math.abs(cuerpo.getLinearVelocity().y) < 0.01f;
        if (agachado) {
            // TODO agregar logica de agarrar armas tiradas en el piso
        }

        // Impulsos
        if (impulsoX != 0) {
            impulsoX = MathUtils.lerp(impulsoX, 0, delta * 5f);
            if (Math.abs(impulsoX) < 0.5f) impulsoX = 0;
        }

        // Shoot ze gun
        if (!input.disparar) puedeDisparar = true;
        if (input.disparar && puedeDisparar) {
            if (arma != null) arma.disparar();
            puedeDisparar = false;
        }

        if (!input.lanzar_arma) puedeLanzarArma = true;
        if (input.lanzar_arma && puedeLanzarArma) {
            if (arma != null)  arma.lanzarArma();
            puedeDisparar = false;
            arma = null;
            puedeLanzarArma = false;
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
            if (moveX == 0 && Math.abs(cuerpo.getLinearVelocity().y) < 0.01f && !agachado) {
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
    }

    public void morir() {
        if (estaMuerto) return;
        estaMuerto = true;
        if (oyenteMuerte != null) oyenteMuerte.accept(this);
        if (arma != null) {
            arma.destruir();
            arma = null;
        }
        cuerpo.getWorld().destroyBody(cuerpo);
        remove();
    }

    /** Solo sube el impulso si el host reporta uno nuevo (knockback); si no, el decay local sigue. */
    public void sincronizarImpulso(float v) {
        if (Math.abs(v) > Math.abs(impulsoX) + 0.3f) impulsoX = v;
    }

    /** El cliente no ve las balas reales: el host le dice quien lo mato. */
    public void forzarAtacante(int id) {
        ultimoAtacanteId = id;
        msUltimoGolpe = System.currentTimeMillis();
    }

    /** Correccion suave contra el estado autoritativo del host. */
    public void corregirDesdeRed(float cx, float cy, float vx, float vy, float zonaMuerta, float factor) {
        if (estaMuerto) return;
        float px = getCentroXPx(), py = getCentroYPx();
        float dx = cx - px, dy = cy - py;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 96f) {                       // desincronizado de verdad: teletransporte
            cuerpo.setTransform(cx / Constantes.PPM, cy / Constantes.PPM, 0);
            cuerpo.setLinearVelocity(vx, vy);
        } else if (dist > zonaMuerta) {         // solo se corrige el exceso sobre la zona muerta
            float k = (dist - zonaMuerta) / dist * factor;
            cuerpo.setTransform((px + dx * k) / Constantes.PPM, (py + dy * k) / Constantes.PPM, 0);
        }
        sincronizarConCuerpo();
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
        if (timerEmote > 0f && spriteEmote != null) {
            // Pequeño rebote/flotación arriba de la cabeza del slime
            float flotarY = MathUtils.sin((DURACION_EMOTE - timerEmote) * 8f) * 2f;
            float ex = getX() + (getWidth() - spriteEmote.getWidth()) / 2f;
            float ey = getY() + getHeight() + 6f + flotarY;

            // Desvanece suavemente en los últimos 0.4s
            float alpha = Math.min(1f, timerEmote / 0.4f);
            spriteEmote.setPosition(ex, ey);
            spriteEmote.setAlpha(alpha);
            spriteEmote.draw(batch);
        }
    }

    public void recibirDano(Bala bala) {
        if (estaMuerto) return;
        ultimoAtacanteId = bala.getDuenoId();
        msUltimoGolpe = System.currentTimeMillis();
        porcentajeVida += bala.dano;
        int dir = bala.dirDerecha() ? 1 : -1;
        aplicarImpulso(dir * bala.knockback, 2);
    }

    public int getAtacanteValido() {
        boolean valido = System.currentTimeMillis() - msUltimoGolpe < VENTANA_ATRIBUCION_MS;
        return valido ? ultimoAtacanteId : -1;
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

    public void setOyenteMuerte(java.util.function.Consumer<Slime> oyente) {
        this.oyenteMuerte = oyente;
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

    public Arma getArma() {
        return arma;
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

    public float getCentroXPx() { return cuerpo.getPosition().x * Constantes.PPM; }

    public float getCentroYPx() { return cuerpo.getPosition().y * Constantes.PPM; }

    public float getVelX() { return cuerpo.getLinearVelocity().x; }

    public float getVelY() { return cuerpo.getLinearVelocity().y; }

    public boolean isVolteado() { return volteado; }

    public float getImpulsoX() { return impulsoX; }

    public void setPorcentajeVida(float v) { porcentajeVida = v; }

    public Texture getTexturaCuerpo() { return textura; }

    public Sprite getOjos() { return ojos; }

    public Sprite getBoca() { return boca; }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public boolean isAgachado() {
        return agachado;
    }
}
