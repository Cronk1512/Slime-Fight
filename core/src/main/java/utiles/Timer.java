package utiles;

import com.badlogic.gdx.Gdx;

public class Timer {
    private float tiempoRestante;
    private boolean estaActivo;

    public Timer(float duracion) {
        tiempoRestante = duracion;
        estaActivo = false;
    }

    public void iniciar() {
        estaActivo = true;
    }

    public void pausar() {
        estaActivo = false;
    }

    public void update() {
        if (!estaActivo) return;

        tiempoRestante -= Gdx.graphics.getRawDeltaTime();

        if (tiempoRestante <= 0) {
            tiempoRestante = 0;
            estaActivo = false;
            alCompletarTimer();
        }
    }

    private void alCompletarTimer() {
        System.out.println("Timer Completo!");
    }

    public int getSegundosRestantes() {
        return (int) Math.ceil(tiempoRestante);
    }

    public float getTiempoRestante() {
        return tiempoRestante;
    }
}
