package utiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.proyecto.slimefight.actores.Arma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AyudanteArmas {
    private final Map<String, Texture> cacheTexturas = new HashMap<>();

    public static class DatosArma {
        public String nombre;
        public String rutaTextura;
        public String rutaTexturaBala;
        public float offsetX;
        public float offsetY;
        public float width;
        public float height;
        public int dano;
        public float knockback;
        public float rango;
        public float dispersion;
        public float anchoBala;
        public float altoBala;
        public float cooldown;
        public int durabilidad;
        public int cantBalas;
        public int tipo;
    }

    public ArrayList<Arma> cargarArmasDesdeJson(String rutaJson, World mundo) {
        ArrayList<Arma> listaArmas = new ArrayList<>();

        JsonValue raiz = new JsonReader().parse(Utils.obtenerRutaInterna(rutaJson));
        Json json = new Json();

        for (JsonValue entrada : raiz) {
            DatosArma datos = json.readValue(DatosArma.class, entrada);
            String rutaBala = (datos.rutaTexturaBala == null || datos.rutaTexturaBala.isEmpty())
                ? "balas/default.png"
                : datos.rutaTexturaBala;

            Texture texArma = obtenerTextura(datos.rutaTextura);
            Texture texBala = obtenerTextura(rutaBala);

            Arma arma = new Arma(
                datos.nombre,
                mundo,
                datos.offsetX,
                datos.offsetY,
                datos.width,
                datos.height,
                texArma,
                datos.dano,
                datos.knockback,
                datos.rango,
                datos.dispersion,
                datos.anchoBala,
                datos.altoBala,
                texBala,
                datos.cooldown,
                datos.durabilidad,
                datos.cantBalas
            );

            arma.setTipo(datos.tipo);
            listaArmas.add(arma);
        }

        return listaArmas;
    }

    private Texture obtenerTextura(String ruta) {
        if (ruta == null || ruta.isEmpty()) return null;

        Texture tex = cacheTexturas.get(ruta);
        if (tex == null) {
            tex = new Texture(Utils.obtenerRutaInterna(ruta));
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            cacheTexturas.put(ruta, tex);
        }
        return tex;
    }

    public void dispose() {
        for (Texture tex : cacheTexturas.values()) {
            tex.dispose();
        }
        cacheTexturas.clear();
    }
}
