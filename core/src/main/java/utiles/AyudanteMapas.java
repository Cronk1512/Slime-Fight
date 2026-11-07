package utiles;


import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.proyecto.slimefight.pantallas.PantallaJuego;

import java.util.ArrayList;
import java.util.Objects;

public class AyudanteMapas {

    private TiledMap mapa;
    private PantallaJuego juego;
    private Vector2 posCamara;
    private ArrayList<Vector2> puntosDeSpawn = new ArrayList<>();

    public AyudanteMapas(TiledMap mapa, PantallaJuego juego) {
        this.mapa = mapa;
        this.juego = juego;
    }

    public OrthogonalTiledMapRenderer leerMapa() {
        obtenerCapas(mapa.getLayers());
        return new OrthogonalTiledMapRenderer(mapa);
    }

    private void obtenerCapas(MapLayers capas) {
        for (int i = 0; i < capas.size(); i++) {
            MapLayer capa = capas.get(i);
            MapObjects objetos = capa.getObjects();
            if (objetos != null) obtenerObjetos(objetos);
        }
    }

    private void obtenerObjetos(MapObjects objetos) {
        for (MapObject objeto : objetos) {
            if (objeto.getProperties().get("type", String.class).equals("Piso")) {
                crearCuerpoEstatico((PolygonMapObject) objeto);
            } else if (objeto.getProperties().get("type", String.class).equals("Camara")) {
                posCamara = new Vector2(objeto.getProperties().get("x", Float.class), objeto.getProperties().get("y", Float.class));
            } else if (objeto.getProperties().get("type", String.class).equals("Spawn")) {
                puntosDeSpawn.add(new Vector2(objeto.getProperties().get("x", Float.class), objeto.getProperties().get("y", Float.class)));
            }
        }
    }

    private void crearCuerpoEstatico(PolygonMapObject polygonMapObject) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = juego.getWorld().createBody(bodyDef);
        Shape shape = crearPoligono(polygonMapObject);
        body.createFixture(shape, 1000);
        shape.dispose();
    }

    private Shape crearPoligono(PolygonMapObject polygonMapObject) {
        float[] vertices = polygonMapObject.getPolygon().getTransformedVertices();
        Vector2[] verticesMapa = new Vector2[vertices.length / 2];

        for (int i = 0; i < vertices.length / 2; i++) {
            Vector2 verticeActual = new Vector2(vertices[i * 2] / Constantes.PPM    , vertices[i * 2 + 1] / Constantes.PPM);
            verticesMapa[i] = verticeActual;
        }

        PolygonShape figura = new PolygonShape();
        figura.set(verticesMapa);
        return figura;
    }

    public Vector2 getPosCamara() {
        return posCamara;
    }

    public ArrayList<Vector2> getPuntosDeSpawn() {
        return puntosDeSpawn;
    }
}
