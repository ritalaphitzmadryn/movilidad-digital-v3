package md.ui.impl.javafx.map;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.util.PointList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class RutaGraphHopperService {

    private static GraphHopper hopper;

    public static synchronized GraphHopper obtenerInstancia() {
        if (hopper != null) {
            return hopper;
        }

        Path osmPath = Paths.get("mapas", "puerto_madryn_ampliado(1).osm");
        if (!Files.exists(osmPath)) {
            throw new IllegalStateException("No se encontró el archivo OSM en: " + osmPath.toAbsolutePath());
        }

        Path cacheDir = Paths.get("target", "graphhopper-cache");

        System.out.println("🗺️ [GraphHopper] Inicializando desde OSM: " + osmPath.toAbsolutePath());
        System.out.flush();

        GraphHopper gh = new GraphHopper();
        gh.setOSMFile(osmPath.toString());
        gh.setGraphHopperLocation(cacheDir.toString());
        gh.setEncodedValuesString("car_access, car_average_speed");
        gh.setProfiles(new Profile("car").setWeighting("fastest"));
        gh.importOrLoad();

        hopper = gh;
        System.out.println("✅ [GraphHopper] Grafo listo en caché: " + cacheDir.toAbsolutePath());
        System.out.flush();
        return hopper;
    }

    public static ResponsePath calcularRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        GraphHopper gh = obtenerInstancia();
        GHRequest req = new GHRequest(latOrigen, lonOrigen, latDestino, lonDestino)
                .setProfile("car")
                .setLocale(Locale.getDefault());

        GHResponse res = gh.route(req);
        if (res.hasErrors()) {
            throw new RuntimeException("Error calculando ruta con GraphHopper: " + res.getErrors());
        }

        ResponsePath best = res.getBest();
        if (best == null || best.getPoints() == null || best.getPoints().isEmpty()) {
            throw new RuntimeException("GraphHopper no devolvió geometría para el trayecto solicitado.");
        }
        return best;
    }

    public static PointList obtenerPuntosRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        return calcularRuta(latOrigen, lonOrigen, latDestino, lonDestino).getPoints();
    }

    public static synchronized void limpiarInstancia() {
        if (hopper != null) {
            hopper.close();
            hopper = null;
        }
    }
}