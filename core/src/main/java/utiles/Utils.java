package utiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.proyecto.slimefight.actores.Bala;
import com.proyecto.slimefight.actores.Slime;

public class Utils {

    public static boolean verificarColision(Slime jugador, Bala bala) {
        if (jugador.hitbox.overlaps(bala.hitbox)) {
           System.out.println("COLISON!");
           return true;
        }
        return false;
    }

    public static void dibujarSlime(Batch batch, TextureRegion region, Actor actor, float inclinacion, float scaleX, float scaleY) {
        float[] vertices = new float[20];

        float x = actor.getX();
        float y = actor.getY();
        float w = actor.getWidth() * actor.getScaleX() * scaleX;
        float h = actor.getHeight() * actor.getScaleY() * scaleY;

        // Base vertices stay strictly anchored to the floor
        float widthDifference = w - actor.getWidth();
        float baseLeftX = MathUtils.round(x - (widthDifference / 2f));
        float baseRightX = MathUtils.round(x + w);
        float baseY = MathUtils.round(y);
        float topY = MathUtils.round(y + h);

        // Top vertices slide together according to the smooth currentLean factor
        float xTopLeft  = baseLeftX + inclinacion;
        float xTopRight = baseRightX + inclinacion;

        // Asymmetric height expansion: Front side stretches up, back side squishes down
        float yTopLeft = topY;
        float yTopRight = topY;

        if (inclinacion > 0) {
            // derechai
            yTopRight += (inclinacion * 0.25f);
            yTopLeft  -= (inclinacion * 0.10f);
        } else if (inclinacion < 0) {
            // izquerda
            yTopLeft  += (Math.abs(inclinacion) * 0.25f);
            yTopRight -= (Math.abs(inclinacion) * 0.10f);
        }

        // Texture UV Mapping
        float u = region.getU();
        float v = region.getV2();
        float u2 = region.getU2();
        float v2 = region.getV();
        float color = actor.getColor().toFloatBits();

        int idx = 0;
        // Vertex 1: Bottom Left
        vertices[idx++] = baseLeftX;    vertices[idx++] = baseY;
        vertices[idx++] = color;         vertices[idx++] = u; vertices[idx++] = v;

        // Vertex 2: Top Left
        vertices[idx++] = xTopLeft;     vertices[idx++] = yTopLeft;
        vertices[idx++] = color;         vertices[idx++] = u; vertices[idx++] = v2;

        // Vertex 3: Top Right
        vertices[idx++] = xTopRight;    vertices[idx++] = yTopRight;
        vertices[idx++] = color;         vertices[idx++] = u2; vertices[idx++] = v2;

        // Vertex 4: Bottom Right
        vertices[idx++] = baseRightX;   vertices[idx++] = baseY;
        vertices[idx++] = color;         vertices[idx++] = u2; vertices[idx++] = v;

        batch.draw(region.getTexture(), vertices, 0, 20);
    }

    // Obtener rutas
    public static FileHandle obtenerRutaClase(String ruta) {
        return Gdx.files.classpath(ruta);
    }

    public static FileHandle obtenerRutaInterna(String ruta) {
        return Gdx.files.internal(ruta);
    }

    public static FileHandle obtenerRutaLocal(String ruta) {
        return Gdx.files.local(ruta);
    }

    public static FileHandle obtenerRutaExterna(String ruta) {
        return Gdx.files.external(ruta);
    }
}
