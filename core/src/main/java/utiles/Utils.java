package utiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.proyecto.slimefight.actores.Bala;
import com.proyecto.slimefight.actores.Slime;

public class Utils {

    public static Polygon rectanguloAPoligono(Rectangle rect) {
        // Convierte rectangulo en poligono para verificar colision
        float[] verticesRect = new float[] {
            rect.x, rect.y,
            rect.x + rect.width, rect.y,
            rect.x + rect.width, rect.y + rect.height,
            rect.x, rect.y + rect.height
        };
        return new Polygon(verticesRect);
    }

    public static void contactoPies(Contact c, int d) {
        Fixture a = c.getFixtureA(), b = c.getFixtureB();
        if ("pies".equals(a.getUserData()) && !b.isSensor()) ((Slime) a.getBody().getUserData()).sumarContactoPiso(d);
        if ("pies".equals(b.getUserData()) && !a.isSensor()) ((Slime) b.getBody().getUserData()).sumarContactoPiso(d);
    }

    public static Body crearCuerpo(float x, float y, float width, float height, BodyDef.BodyType tipo, World mundo) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = tipo;
        bodyDef.position.set(x / Constantes.PPM, y / Constantes.PPM);
        bodyDef.fixedRotation = true;
        Body cuerpo = mundo.createBody(bodyDef);

        PolygonShape figura = new PolygonShape();
        PolygonShape pies = new PolygonShape();
        figura.setAsBox(width / 2 / Constantes.PPM, height / 2 / Constantes.PPM);
        pies.setAsBox(10f / Constantes.PPM, 2f / Constantes.PPM,
            new Vector2(0, -16f / Constantes.PPM), 0);

        FixtureDef fdprincipal = new FixtureDef();
        FixtureDef fdpies = new FixtureDef();
        fdprincipal.shape = figura;
        fdprincipal.friction = 0;
        fdpies.shape = pies;
        fdpies.isSensor = true;
        cuerpo.createFixture(fdprincipal).setUserData("principal");
        cuerpo.createFixture(fdpies).setUserData("pies");
        figura.dispose();
        pies.dispose();
        return cuerpo;
    }

    public static boolean verificarColisionBala(Slime jugador, Bala bala) {
        if (jugador.hitbox.overlaps(bala.hitbox)) {
           System.out.println("COLISON!");
           return true;
        }
        return false;
    }

    public static void dibujarSlime(Batch batch, TextureRegion region, Actor actor, Body cuerpo, float inclinacion, float scaleX, float scaleY) {
        float[] vertices = new float[20];

        // 1. Convert Box2D center-position (meters) to screen-coordinates (pixels)
        float centerX = cuerpo.getPosition().x * Constantes.PPM;
        float centerY = cuerpo.getPosition().y * Constantes.PPM;

        // 2. Base width and height calculations on the visual Actor dimensions
        float actorW = actor.getWidth() * actor.getScaleX();
        float actorH = actor.getHeight() * actor.getScaleY();

        float w = actorW * scaleX;
        float h = actorH * scaleY;

        // 3. Shift calculations from the physical center to the graphical bottom-left corner
        // This anchors the base perfectly to where the physics body sits on the ground
        float x = centerX - (w / 2f);
        float y = centerY - (actorH / 2f); // Shifts down by half the standard height

        // Base vertices stay strictly anchored to the floor
        float baseLeftX = MathUtils.round(x);
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
            yTopRight += (inclinacion * 0.25f);
            yTopLeft  -= (inclinacion * 0.10f);
        } else if (inclinacion < 0) {
            yTopLeft  += (Math.abs(inclinacion) * 0.25f);
            yTopRight -= (Math.abs(inclinacion) * 0.10f);
        }

        // Texture UV Mapping & Color Setup
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
