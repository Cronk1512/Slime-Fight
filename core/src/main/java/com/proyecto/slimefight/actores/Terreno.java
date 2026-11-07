package com.proyecto.slimefight.actores;

import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Terreno extends Actor {
    public Polygon figura;

    public Terreno(PolygonMapObject objeto) {
        figura = objeto.getPolygon();
    }
}
