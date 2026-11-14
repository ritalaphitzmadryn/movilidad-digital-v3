package md.concurrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import md.config.DAOLoader;
import md.datos.DAO.DAOFactory;
import md.datos.DAO.ServicioDAO;
import md.datos.DAO.UsuariosDAO;
import md.datos.DAO.VehiculosDAO;
import md.logica.GestorMovilidad;
//import md.modelo.RegistroViaje;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;

public class SimuladorMovilidadConcurrente {

    // =========================================================================
    // PARÁMETROS CONFIGURABLES DE LA SIMULACIÓN
    // =========================================================================
    private static final int DURACION_EMISION_SEGUNDOS = 12;
    private static final int INTERVALO_MIN_DEMANDA_MS = 600;
    private static final int INTERVALO_MAX_DEMANDA_MS = 1300;
    private static final int CAPACIDAD_TABLERO = 50;
    private static final int TIMEOUT_CIERRE_SEGUNDOS = 30;

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("                 SIMULACIÓN CONCURRENTE DE SERVICIOS                     ");
        System.out.println("=========================================================================");

        List<ConductorTask> tareasConductores = new ArrayList<>();
        GestorMovilidad gestor = null;

        try {
            DAOFactory factory = DAOLoader.getFactory();
            VehiculosDAO vehiculoDAO = factory.getVehiculoDAO();
            UsuariosDAO usuarioDAO = factory.getUsuarioDAO();
            ServicioDAO servicioDAO = factory.getServicioDAO();

            Map<String, Vehiculo> vehiculos = vehiculoDAO.cargarTodos();
            Map<String, Usuario> usuarios = usuarioDAO.cargarTodos();
            List<Servicio> servicios = servicioDAO.cargarTodos();

            gestor = new GestorMovilidad();

            for (Vehiculo v : vehiculos.values()) {
                gestor.registrarVehiculo(v);
            }

            for (Servicio s : servicios) {
                gestor.agregarServicio(s);
            }

            List<Usuario> clientes = new ArrayList<>();
            List<Usuario> conductores = new ArrayList<>();

            for (Usuario u : usuarios.values()) {
                gestor.registrarCliente(u);
                clientes.add(u);

                // FILTRO LÓGICO DE FLOTA OPERATIVA: 
                // Solo registramos y creamos hilo para el conductor si tiene perfil Conductor Y un vehículo activo asignado.
                if (u.getConductor() != null && u.getConductor().getVehiculoActivo() != null) {
                    gestor.registrarConductor(u);
                    conductores.add(u);
                }
            }

            if (conductores.isEmpty() || clientes.isEmpty() || servicios.isEmpty()) {
                System.out.println("Error: Datos insuficientes en almacenamiento para iniciar la simulación.");
                return;
            }

            System.out.println("Recursos inicializados -> Clientes: " + clientes.size()
                    + " | Conductores: " + conductores.size()
                    + " | Servicios: " + servicios.size()
                    + " | Duración: " + DURACION_EMISION_SEGUNDOS + "s");
            System.out.println("-------------------------------------------------------------------------");

            TableroServicios tablero = new TableroServicios(CAPACIDAD_TABLERO);

            // Pool de hilos: 1 hilo productor + 1 hilo por cada conductor registrado
            ExecutorService executor = Executors.newFixedThreadPool(conductores.size() + 1);

            GeneradorDemandaTask generadorTask = new GeneradorDemandaTask(
                    tablero, gestor, clientes, servicios,
                    DURACION_EMISION_SEGUNDOS, INTERVALO_MIN_DEMANDA_MS, INTERVALO_MAX_DEMANDA_MS,
                    conductores.size()
            );
            executor.submit(generadorTask);

            for (Usuario conductor : conductores) {
                ConductorTask task = new ConductorTask(conductor, tablero, gestor);
                tareasConductores.add(task);
                executor.submit(task);
            }

            executor.shutdown();
            if (!executor.awaitTermination(TIMEOUT_CIERRE_SEGUNDOS, TimeUnit.SECONDS)) {
                System.out.println("[Sistema] Tiempo máximo excedido, forzando cierre de hilos restantes...");
                executor.shutdownNow();
            }

        } catch (Exception e) {
            System.err.println("Error durante la ejecución de la simulación: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=========================================================================");
        System.out.println("                        SIMULACIÓN FINALIZADA                            ");
        System.out.println("=========================================================================");

        printReporteFinal(gestor, tareasConductores);
    }

    private static void printReporteFinal(GestorMovilidad gestor, List<ConductorTask> tareasConductores) {
        if (gestor == null) return;

        System.out.println("\n=================================================================================================================================================");
        System.out.println("                                         REPORTE ESTADÍSTICO Y DETALLE FINAL                                                     ");
        System.out.println("=================================================================================================================================================");

        // 1. TABLA / LISTADO DETALLADO DE VIAJES REALIZADOS (Orden: Vehículo -> Servicio -> Origen->Destino)
        System.out.println("--- 1. DETALLE DE VIAJES REALIZADOS EN LA JORNADA ---");
        System.out.println(String.format("%-10s | %-18s | %-18s | %-28s | %-22s | %-38s | %-10s",
                "ID Viaje", "Cliente", "Conductor", "Vehículo", "Servicio", "Origen -> Destino", "Costo"));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");

        int totalViajesGeneral = 0;
        double recaudacionGeneral = 0.0;

        for (ConductorTask ct : tareasConductores) {
            Usuario uConductor = ct.getUsuarioConductor();
            md.modelo.Conductor perfilConductor = uConductor.getConductor();
            if (perfilConductor == null) continue;

            md.modelo.Vehiculo v = perfilConductor.getVehiculoActivo();
            String vehiculoStr = (v != null) ? v.getModelo() + " (" + v.getPatente() + ")" : "Sin vehíc.";

            for (md.modelo.Viaje viaje : perfilConductor.getViajes()) {
                if (viaje.getEstadoActual() != md.modelo.EstadoViaje.FINALIZADO) {
                    continue;
                }

                String idCorto = viaje.getId().toString().substring(0, 8);
                String clienteNombre = viaje.getCliente() != null ? viaje.getCliente().getNombre() : "N/D";
                String servicioNombre = viaje.getServicio() != null ? viaje.getServicio().getNombre() + " (" + viaje.getServicio().getTipoServicio() + ")" : "N/D";
                String ruta = (viaje.getOrigen() != null ? viaje.getOrigen().getDireccion() : "Origen") 
                        + " -> " + (viaje.getDestino() != null ? viaje.getDestino().getDireccion() : "Destino");
                String costoStr = "$" + String.format("%.2f", viaje.getCostoCalculado());

                System.out.println(String.format("%-10s | %-18s | %-18s | %-28s | %-22s | %-38s | %-10s",
                        "#" + idCorto,
                        clienteNombre.length() > 18 ? clienteNombre.substring(0, 15) + "..." : clienteNombre,
                        uConductor.getNombre().length() > 18 ? uConductor.getNombre().substring(0, 15) + "..." : uConductor.getNombre(),
                        vehiculoStr.length() > 28 ? vehiculoStr.substring(0, 25) + "..." : vehiculoStr,
                        servicioNombre.length() > 22 ? servicioNombre.substring(0, 19) + "..." : servicioNombre,
                        ruta.length() > 38 ? ruta.substring(0, 35) + "..." : ruta,
                        costoStr
                ));

                totalViajesGeneral++;
                recaudacionGeneral += viaje.getCostoCalculado();
            }
        }

        if (totalViajesGeneral == 0) {
            System.out.println("No se completaron viajes durante la ejecución.");
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");

        // 2. TABLA / RESUMEN AGRUPADO POR CONDUCTOR (Con mayor espacio para Vehículo Activo)
        System.out.println("\n--- 2. RESUMEN ESTADÍSTICO POR CONDUCTOR ---");
        System.out.println(String.format("%-22s | %-45s | %-25s | %-16s | %-15s",
                "Conductor", "Vehículo Activo", "Tipos de Servicio", "Viajes Realizados", "Recaudación Total"));
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");

        for (ConductorTask ct : tareasConductores) {
            Usuario u = ct.getUsuarioConductor();
            md.modelo.Vehiculo v = u.getConductor().getVehiculoActivo();

            String vehiculoStr = "Sin vehículo asignado";
            String tiposServiciosStr = "N/D";

            if (v != null) {
                vehiculoStr = String.format("%s (%s) [%s - %s]", v.getModelo(), v.getPatente(), v.getTipoVehiculo(), v.getCategoriaVehiculo());
                tiposServiciosStr = v.getTiposServicios().toString();
            }

            System.out.println(String.format("%-22s | %-45s | %-25s | %-16d | $%14.2f",
                    u.getNombre(),
                    vehiculoStr.length() > 45 ? vehiculoStr.substring(0, 42) + "..." : vehiculoStr,
                    tiposServiciosStr.length() > 25 ? tiposServiciosStr.substring(0, 22) + "..." : tiposServiciosStr,
                    ct.getViajesRealizados(),
                    ct.getRecaudacionTotal()
            ));
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println(String.format("TOTALES GENERALES -> Viajes Finalizados: %d | Recaudación Flota: $%.2f",
                totalViajesGeneral, recaudacionGeneral));
        System.out.println("=================================================================================================================================================");
    }
}