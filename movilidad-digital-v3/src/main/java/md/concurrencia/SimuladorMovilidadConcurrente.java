package md.concurrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import md.config.DAOLoader;
import md.datos.DAO.DAOFactory;
import md.datos.DAO.ServicioDAO;
import md.datos.DAO.UsuariosDAO;
import md.datos.DAO.VehiculosDAO;
import md.logica.GestorMovilidad;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;
import md.modelo.RegistroViaje;

public class SimuladorMovilidadConcurrente {

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

                if (u.getConductor() != null) {
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
                    + " | Servicios: " + servicios.size());
            System.out.println("-------------------------------------------------------------------------");

            int totalViajesASimular = 8;
            TableroServicios tablero = new TableroServicios(4);

            GeneradorDemandaTask generadorTask = new GeneradorDemandaTask(
                    tablero, gestor, clientes, servicios, totalViajesASimular, conductores.size()
            );

            Thread hiloGenerador = new Thread(generadorTask, "Hilo-Generador");
            List<Thread> hilosConductores = new ArrayList<>();

            for (Usuario conductor : conductores) {
                ConductorTask task = new ConductorTask(conductor, tablero, gestor);
                tareasConductores.add(task);
                Thread hilo = new Thread(task, "Hilo-Conductor-" + conductor.getId());
                hilosConductores.add(hilo);
            }

            hiloGenerador.start();
            for (Thread hilo : hilosConductores) {
                hilo.start();
            }

            hiloGenerador.join();
            for (Thread hilo : hilosConductores) {
                hilo.join();
            }

        } catch (Exception e) {
            System.err.println("Error durante la ejecución de la simulación: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=========================================================================");
        System.out.println("                        SIMULACIÓN FINALIZADA                            ");
        System.out.println("=========================================================================");

        // RECONTEO Y REPORTE FINAL DE LA JORNADA
        printReporteFinal(gestor, tareasConductores);
    }

    private static void printReporteFinal(GestorMovilidad gestor, List<ConductorTask> tareasConductores) {
        System.out.println("\n=========================================================================");
        System.out.println("                       REPORTE ESTADÍSTICO FINAL                         ");
        System.out.println("=========================================================================");

        // 1. Resumen por Conductor
        System.out.println("--- RECAUDACIÓN Y VIAJES POR CONDUCTOR ---");
        for (ConductorTask ct : tareasConductores) {
            Usuario u = ct.getUsuarioConductor();
            Vehiculo v = u.getConductor().getVehiculoActivo();
            
            String vehiculoStr = "Sin vehículo";
            if (v != null) {
                // Se agregan: el modelo, patente, TipoVehiculo y CategoriaVehiculo según enums de la clase
                vehiculoStr = String.format("%s (%s) [%s - %s]",
                        v.getModelo(),
                        v.getPatente(),
                        v.getTipoVehiculo(),      // AUTO o MOTO
                        v.getCategoriaVehiculo()  // ESTANDAR, CONFORT, PREMIUM
                );
            }
            
            System.out.println(String.format("• Conductor: %s | Vehículo: %s | Viajes realizados: %d | Recaudación: $%.2f",
                    u.getNombre(), vehiculoStr, ct.getViajesRealizados(), ct.getRecaudacionTotal()));
        }

        System.out.println("\n--- DETALLE GENERAL DE VIAJES REGISTRADOS ---");
        List<RegistroViaje> todosRegistros = gestor.obtenerRegistroViaje();
        
        if (todosRegistros == null || todosRegistros.isEmpty()) {
            // Si la bitácora general está vacía, se muestra un resumen directo sumando los viajes de los hilos conductores
            int totalViajesRealizados = tareasConductores.stream().mapToInt(ConductorTask::getViajesRealizados).sum();
            double recaudacionTotalGeneral = tareasConductores.stream().mapToDouble(ConductorTask::getRecaudacionTotal).sum();
            
            System.out.println("Total de viajes finalizados en la jornada: " + totalViajesRealizados);
            System.out.println("Recaudación total de la flota: $" + String.format("%.2f", recaudacionTotalGeneral));
        } else {
            System.out.println("Total de cambios de estado registrados en bitácora: " + todosRegistros.size());
            for (RegistroViaje reg : todosRegistros) {
                System.out.println(" - " + reg);
            }
        }
        System.out.println("=========================================================================");
    }
}