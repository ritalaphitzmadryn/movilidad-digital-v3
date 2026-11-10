package md.ui.impl.cli;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import md.logica.GestorMovilidad;
import md.modelo.*;
import md.rutas.api.IRutasService;
import md.rutas.impl.IRutasServiceImpl;
import md.ui.api.IInterfazUsuario;

public class InterfazUsuario implements IInterfazUsuario {

    private GestorMovilidad gestor;
    private Scanner scanner;
    private Usuario usuarioActual;

    public InterfazUsuario(GestorMovilidad gestor) {
        this.gestor = gestor;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void iniciar() {
        System.out.println("================================");
        System.out.println("        MOVILIDAD DIGITAL");
        System.out.println("================================");

        System.out.print("Ingrese su ID de usuario: ");
        String idUsuario = scanner.nextLine();

        usuarioActual = buscarUsuario(idUsuario);

        if (usuarioActual == null) {
            System.out.println("Usuario no encontrado.");
            return;
        }

        System.out.println("¡Bienvenido/a, " + usuarioActual.getNombre() + "!");

        // Selector de rol inteligente si tiene ambos perfiles
        boolean tieneCliente = usuarioActual.getCliente() != null;
        boolean tieneConductor = usuarioActual.getConductor() != null;

        if (tieneCliente && tieneConductor) {
            System.out.println("\n¿Con qué rol desea iniciar sesión?");
            System.out.println("1. Cliente");
            System.out.println("2. Conductor");
            System.out.print("Seleccione una opción: ");
            String opcionRol = scanner.nextLine();

            if (opcionRol.equals("2")) {
                usuarioActual.cambiarRolActivo(RolUsuario.CONDUCTOR);
            } else {
                usuarioActual.cambiarRolActivo(RolUsuario.CLIENTE);
            }
        } else if (tieneConductor) {
            usuarioActual.cambiarRolActivo(RolUsuario.CONDUCTOR);
            System.out.println("Iniciando como Conductor...");
        } else {
            usuarioActual.cambiarRolActivo(RolUsuario.CLIENTE);
            System.out.println("Iniciando como Cliente...");
        }

        mostrarMenu();
    }

    @Override
    public void detener() {
        System.out.println("Cerrando sesión y finalizando interfaz de consola...");
    }

    private Usuario buscarUsuario(String idUsuario) {
        for (Usuario usuario : gestor.obtenerUsuarios()) {
            if (usuario.getId().equals(idUsuario)) {
                return usuario;
            }
        }
        return null;
    }

    private void mostrarMenu() {
        boolean salir = false;

        while (!salir) {
            System.out.println();
            System.out.println("================================");
            System.out.println("Usuario: " + usuarioActual.getNombre());
            System.out.println("Rol activo: " + usuarioActual.getRolActivo());
            System.out.println("================================");

            if (usuarioActual.getRolActivo() == RolUsuario.CLIENTE) {
                mostrarMenuCliente();
            } else if (usuarioActual.getRolActivo() == RolUsuario.CONDUCTOR) {
                mostrarMenuConductor();
            }

            System.out.println("0. Salir de la sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            if (usuarioActual.getRolActivo() == RolUsuario.CLIENTE) {
                salir = procesarOpcionCliente(opcion);
            } else {
                salir = procesarOpcionConductor(opcion);
            }
        }
    }

    private void mostrarMenuCliente() {
        System.out.println("\n----- MENÚ CLIENTE -----");
        System.out.println("1. Solicitar viaje");
        System.out.println("2. Consultar historial");
        System.out.println("3. Calificar servicio");
        if (usuarioActual.getConductor() != null) {
            System.out.println("4. Cambiar a modo conductor");
        }
    }

    private void mostrarMenuConductor() {
        System.out.println("\n----- MENÚ CONDUCTOR -----");
        System.out.println("1. Ver viajes pendientes");
        System.out.println("2. Aceptar viaje");
        System.out.println("3. Actualizar ubicación");
        if (usuarioActual.getCliente() != null) {
            System.out.println("4. Cambiar a modo cliente");
        }
    }

    private boolean procesarOpcionCliente(String opcion) {
        switch (opcion) {
            case "1":
                solicitarViaje();
                break;
            case "2":
                consultarHistorial();
                break;
            case "3":
                calificarServicio();
                break;
            case "4":
                if (usuarioActual.getConductor() != null) {
                    cambiarAConductor();
                } else {
                    System.out.println("Opción no válida.");
                }
                break;
            case "0":
                detener();
                return true;
            default:
                System.out.println("Opción no válida.");
        }
        return false;
    }

    private boolean procesarOpcionConductor(String opcion) {
        switch (opcion) {
            case "1":
                verViajesPendientes();
                break;
            case "2":
                aceptarViaje();
                break;
            case "3":
                actualizarUbicacion();
                break;
            case "4":
                if (usuarioActual.getCliente() != null) {
                    cambiarACliente();
                } else {
                    System.out.println("Opción no válida.");
                }
                break;
            case "0":
                detener();
                return true;
            default:
                System.out.println("Opción no válida.");
        }
        return false;
    }

    // =====================================================
    // MÉTODO SOLICITAR VIAJE (Con ubicaciones de Puerto Madryn)
    // =====================================================
    public void solicitarViaje() {
        System.out.println();
        System.out.println("================================");
        System.out.println("       SOLICITAR NUEVO VIAJE");
        System.out.println("================================");

        String[] ubicacionesDisponibles = {
            "Municipalidad de Puerto Madryn",
            "Puerto Almirante Brown / Storni",
            "Universidad del Chubut (UDC)",
            "Universidad Tecnológica Nacional (UTN)",
            "Plaza San Martín",
            "Terminal de Ómnibus",
            "Ecocentro",
            "Muelle Luis Piedra Buena",
            "Universidad (UNPSJB)",
            "Parque Industrial",
            "Supermercado La Anónima",
            "Hipermercado Changomás",
            "Cine Teatro Auditorium",
            "Escuela N° 728",
            "Escuela N° 7707",
            "Polideportivo Municipal"
        };

        // Mostrar Orígenes
        System.out.println("--- SELECCIONE EL ORIGEN ---");
        for (int i = 0; i < ubicacionesDisponibles.length; i++) {
            System.out.println((i + 1) + ". " + ubicacionesDisponibles[i]);
        }
        System.out.print("Elija el número de origen: ");
        int indexOrigen = -1;
        try {
            indexOrigen = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
            return;
        }

        if (indexOrigen < 0 || indexOrigen >= ubicacionesDisponibles.length) {
            System.out.println("Opción de origen fuera de rango.");
            return;
        }
        String nombreOrigen = ubicacionesDisponibles[indexOrigen];

        // Mostrar Destinos
        System.out.println("\n--- SELECCIONE EL DESTINO ---");
        for (int i = 0; i < ubicacionesDisponibles.length; i++) {
            System.out.println((i + 1) + ". " + ubicacionesDisponibles[i]);
        }
        System.out.print("Elija el número de destino: ");
        int indexDestino = -1;
        try {
            indexDestino = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
            return;
        }

        if (indexDestino < 0 || indexDestino >= ubicacionesDisponibles.length) {
            System.out.println("Opción de destino fuera de rango.");
            return;
        }
        String nombreDestino = ubicacionesDisponibles[indexDestino];

        if (nombreOrigen.equals(nombreDestino)) {
            System.out.println("Error: El origen y el destino no pueden ser el mismo lugar.");
            return;
        }

        // Seleccionar Tipo de Servicio
        System.out.println("\n--- SELECCIONE EL TIPO DE SERVICIO ---");
        String[] serviciosDisponibles = {"Económico", "Standard", "Confort", "VIP"};
        for (int i = 0; i < serviciosDisponibles.length; i++) {
            System.out.println((i + 1) + ". " + serviciosDisponibles[i]);
        }
        System.out.print("Elija el número de servicio: ");
        int indexServicio = 1;
        try {
            indexServicio = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (Exception e) {
            indexServicio = 1; // Por defecto Standard
        }
        if (indexServicio < 0 || indexServicio >= serviciosDisponibles.length) {
            indexServicio = 1;
        }
        String tipoServicioElegido = serviciosDisponibles[indexServicio];

        try {
            // Obtener coordenadas geográficas
            double[] coordsOrigen = obtenerCoordenadas(nombreOrigen);
            double[] coordsDestino = obtenerCoordenadas(nombreDestino);

            // Calcular distancia y ruta usando el servicio de rutas
            IRutasService rutasService = new IRutasServiceImpl();
            double distanciaKm = rutasService.calcularDistanciaKm(coordsOrigen[0], coordsOrigen[1], coordsDestino[0], coordsDestino[1]);
            String detalleRuta = rutasService.calcularRuta(coordsOrigen[0], coordsOrigen[1], coordsDestino[0], coordsDestino[1]);

            // Estimación de tiempo (velocidad urbana de 30 km/h)
            double tiempoMinutos = (distanciaKm / 30.0) * 60.0;

            // Instanciar el Servicio con los parámetros de dominio
            Servicio servicioObj = new Servicio(
                tipoServicioElegido,
                TipoServicio.PASAJEROS,
                TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR,
                100.0, // Tarifa base
                50.0,  // Precio por km
                10.0   // Precio por minuto
            );

            double costoTotal = servicioObj.calcularCosto(distanciaKm, tiempoMinutos);

            // Crear objetos de Ubicación y registrar el viaje en el gestor
            Ubicacion origenObj = new Ubicacion(nombreOrigen, coordsOrigen[0], coordsOrigen[1]);
            Ubicacion destinoObj = new Ubicacion(nombreDestino, coordsDestino[0], coordsDestino[1]);

            Viaje nuevoViaje = gestor.solicitarViaje(usuarioActual, servicioObj, origenObj, destinoObj, LocalDateTime.now());

            // Mostrar resumen en consola
            System.out.println();
            System.out.println("================================");
            System.out.println("   ¡VIAJE SOLICITADO CON ÉXITO!");
            System.out.println("================================");
            System.out.println("ID de Viaje: " + nuevoViaje.getId());
            System.out.println("Origen: " + nombreOrigen);
            System.out.println("Destino: " + nombreDestino);
            System.out.println("Servicio: " + tipoServicioElegido);
            System.out.printf(Locale.US, "Distancia: %.2f km\n", distanciaKm);
            System.out.printf(Locale.US, "Tiempo aproximado: %.0f min\n", tiempoMinutos);
            System.out.printf(Locale.US, "Costo Total: $%.2f\n", costoTotal);
            System.out.println("Detalle de ruta: " + detalleRuta);
            System.out.println("================================");

        } catch (Exception e) {
            System.err.println("Error al procesar el viaje: " + e.getMessage());
        }
    }

    public void consultarHistorial() {
        System.out.println("\n----- HISTORIAL DE VIAJES -----");
        if (usuarioActual.getCliente() != null) {
            List<Viaje> viajesCliente = gestor.obtenerViajes(usuarioActual.getCliente());
            if (viajesCliente.isEmpty()) {
                System.out.println("No registra viajes anteriores.");
            } else {
                for (Viaje viaje : viajesCliente) {
                    System.out.println("Viaje ID: " + viaje.getId() + " | Estado: " + viaje.getEstadoActual());
                }
            }
        }
    }

    public void calificarServicio() {
        System.out.println("\n----- CALIFICAR SERVICIO -----");
        System.out.print("Ingrese el ID del viaje: ");
        String id = scanner.nextLine();
        try {
            UUID idViaje = UUID.fromString(id);
            System.out.println("Viaje seleccionado: " + idViaje);
        } catch (IllegalArgumentException e) {
            System.out.println("El ID del viaje no es válido.");
        }
    }

    public void verViajesPendientes() {
        System.out.println("\n----- VIAJES PENDIENTES -----");
        System.out.println("Funcionalidad de consulta de viajes pendientes.");
    }

    public void aceptarViaje() {
        System.out.println("\n----- ACEPTAR VIAJE -----");
        System.out.print("Ingrese el ID del viaje: ");
        String id = scanner.nextLine();
        try {
            UUID idViaje = UUID.fromString(id);
            System.out.println("Viaje seleccionado: " + idViaje);
        } catch (IllegalArgumentException e) {
            System.out.println("El ID del viaje no es válido.");
        }
    }

    public void actualizarUbicacion() {
        System.out.println("\n----- ACTUALIZAR UBICACIÓN -----");
        try {
            System.out.print("Ingrese latitud: ");
            double latitud = Double.parseDouble(scanner.nextLine());
            System.out.print("Ingrese longitud: ");
            double longitud = Double.parseDouble(scanner.nextLine());
            System.out.println("Nueva ubicación: " + latitud + ", " + longitud);
        } catch (NumberFormatException e) {
            System.out.println("Coordenadas inválidas.");
        }
    }

    private void cambiarAConductor() {
        usuarioActual.cambiarRolActivo(RolUsuario.CONDUCTOR);
        System.out.println("Ahora está utilizando el sistema como conductor.");
    }

    private void cambiarACliente() {
        usuarioActual.cambiarRolActivo(RolUsuario.CLIENTE);
        System.out.println("Ahora está utilizando el sistema como cliente.");
    }

    // Método auxiliar de coordenadas idéntico al de JavaFX
    private double[] obtenerCoordenadas(String lugar) {
        switch (lugar) {
            case "Municipalidad de Puerto Madryn": 
                return new double[]{-42.7692, -65.0385};
            case "Puerto Almirante Brown / Storni": 
                return new double[]{-42.7480, -65.0310};
            case "Universidad del Chubut (UDC)": 
                return new double[]{-42.7820, -65.0420};
            case "Universidad Tecnológica Nacional (UTN)": 
                return new double[]{-42.7735, -65.0610};
            case "Plaza San Martín": 
                return new double[]{-42.7685, -65.0350};
            case "Terminal de Ómnibus": 
                return new double[]{-42.7710, -65.0700};
            case "Ecocentro": 
                return new double[]{-42.7915, -65.0220};
            case "Muelle Luis Piedra Buena": 
                return new double[]{-42.7575, -65.0350};
            case "Universidad (UNPSJB)": 
                return new double[]{-42.7750, -65.0500};
            case "Parque Industrial": 
                return new double[]{-42.7950, -65.0800};
            case "Supermercado La Anónima": 
                return new double[]{-42.7640, -65.0430};
            case "Hipermercado Changomás": 
                return new double[]{-42.7720, -65.0650};
            case "Cine Teatro Auditorium": 
                return new double[]{-42.7670, -65.0360};
            case "Escuela N° 728": 
                return new double[]{-42.7660, -65.0500};
            case "Escuela N° 7707": 
                return new double[]{-42.7780, -65.0550};
            case "Polideportivo Municipal": 
                return new double[]{-42.7610, -65.0720};
            default: 
                return new double[]{-42.7692, -65.0385}; 
        }
    }
}