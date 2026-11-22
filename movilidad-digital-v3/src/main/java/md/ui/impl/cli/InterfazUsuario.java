package md.ui.impl.cli;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import md.logica.GestorMovilidad;
import md.modelo.*;
import md.rutas.api.IRutasService;
import md.rutas.impl.RutasServiceImpl;
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
        precargarViajesSiNoHayPendientes();

        System.out.println("====================================");
        System.out.println("         MOVILIDAD DIGITAL");
        System.out.println("====================================");
        System.out.println("Ingresá tu nombre o email para comenzar.");
        System.out.println("Tip: escribí 'listar' para ver usuarios o '0' para salir.\n");

        usuarioActual = null;
        while (usuarioActual == null) {
            System.out.print("Nombre o email: ");
            String entrada = scanner.nextLine().trim();

            if (entrada.equals("0")) {
                detener();
                return;
            }

            if (entrada.equalsIgnoreCase("listar")) {
                mostrarUsuariosDisponibles();
                continue;
            }

            if (entrada.isEmpty()) {
                System.out.println("Por favor, ingresá tu nombre o email (o '0' para salir).");
                continue;
            }

            usuarioActual = buscarUsuario(entrada);

            if (usuarioActual == null) {
                System.out.println("No encontramos ningún usuario con ese nombre o email. Probá de nuevo o escribí 'listar'.");
            }
        }

        System.out.println("\n¡Hola, " + usuarioActual.getNombre() + "! Qué bueno tenerte por acá.");

        boolean tieneCliente = usuarioActual.getCliente() != null;
        boolean tieneConductor = usuarioActual.getConductor() != null;

        if (tieneCliente && tieneConductor) {
            System.out.println("\n¿Con qué rol desea iniciar sesión?");
            System.out.println("1. Cliente");
            System.out.println("2. Conductor");
            System.out.print("Seleccione una opción: ");
            String opcionRol = scanner.nextLine().trim();

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

    private void precargarViajesSiNoHayPendientes() {
        if (!gestor.obtenerViajesPendientes().isEmpty()) {
            return;
        }

        List<Usuario> clientesDisponibles = new ArrayList<>();
        for (Usuario u : gestor.obtenerUsuarios()) {
            if (u.getCliente() != null) {
                clientesDisponibles.add(u);
            }
        }

        if (clientesDisponibles.isEmpty()) {
            return;
        }

        Servicio servicioEstandar = new Servicio(
            "Standard",
            TipoServicio.PASAJEROS,
            TipoVehiculo.AUTO,
            CategoriaVehiculo.ESTANDAR,
            100.0,
            50.0,
            10.0
        );

        String[][] rutasEjemplo = {
            {"Plaza San Martín", "Universidad (UNPSJB)"},
            {"Terminal de Ómnibus", "Ecocentro"},
            {"Muelle Luis Piedra Buena", "Universidad Tecnológica Nacional (UTN)"}
        };

        for (int i = 0; i < rutasEjemplo.length; i++) {
            Usuario cliente = clientesDisponibles.get(i % clientesDisponibles.size());
            String origenNombre = rutasEjemplo[i][0];
            String destinoNombre = rutasEjemplo[i][1];

            double[] cOrigen = obtenerCoordenadas(origenNombre);
            double[] cDestino = obtenerCoordenadas(destinoNombre);

            Ubicacion origen = new Ubicacion(origenNombre, cOrigen[0], cOrigen[1]);
            Ubicacion destino = new Ubicacion(destinoNombre, cDestino[0], cDestino[1]);

            gestor.solicitarViaje(cliente, servicioEstandar, origen, destino, LocalDateTime.now().minusMinutes(5L * (3 - i)));
        }
    }

    private void mostrarUsuariosDisponibles() {
        System.out.println("\n--- Usuarios disponibles ---");
        for (Usuario u : gestor.obtenerUsuarios()) {
            System.out.println("- " + u.getNombre() + " | " + u.getEmail());
        }
        System.out.println();
    }

    @Override
    public void detener() {
        System.out.println("Cerrando sesión y finalizando interfaz de consola...");
    }

    private Usuario buscarUsuario(String dato) {
        for (Usuario usuario : gestor.obtenerUsuarios()) {
            boolean coincideNombre = usuario.getNombre() != null && usuario.getNombre().equalsIgnoreCase(dato);
            boolean coincideEmail = usuario.getEmail() != null && usuario.getEmail().equalsIgnoreCase(dato);
            boolean coincideId = usuario.getId() != null && usuario.getId().equalsIgnoreCase(dato);

            if (coincideNombre || coincideEmail || coincideId) {
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
            String opcion = scanner.nextLine().trim();

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

        System.out.println("--- SELECCIONE EL ORIGEN ---");
        for (int i = 0; i < ubicacionesDisponibles.length; i++) {
            System.out.println((i + 1) + ". " + ubicacionesDisponibles[i]);
        }
        System.out.print("Elija el número de origen: ");
        int indexOrigen = -1;
        try {
            indexOrigen = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
            return;
        }

        if (indexOrigen < 0 || indexOrigen >= ubicacionesDisponibles.length) {
            System.out.println("Opción de origen fuera de rango.");
            return;
        }
        String nombreOrigen = ubicacionesDisponibles[indexOrigen];

        System.out.println("\n--- SELECCIONE EL DESTINO ---");
        for (int i = 0; i < ubicacionesDisponibles.length; i++) {
            System.out.println((i + 1) + ". " + ubicacionesDisponibles[i]);
        }
        System.out.print("Elija el número de destino: ");
        int indexDestino = -1;
        try {
            indexDestino = Integer.parseInt(scanner.nextLine().trim()) - 1;
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

        System.out.println("\n--- SELECCIONE EL TIPO DE SERVICIO ---");
        String[] serviciosDisponibles = {"Económico", "Standard", "Confort", "VIP"};
        for (int i = 0; i < serviciosDisponibles.length; i++) {
            System.out.println((i + 1) + ". " + serviciosDisponibles[i]);
        }
        System.out.print("Elija el número de servicio: ");
        int indexServicio = 1;
        try {
            indexServicio = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (Exception e) {
            indexServicio = 1;
        }
        if (indexServicio < 0 || indexServicio >= serviciosDisponibles.length) {
            indexServicio = 1;
        }
        String tipoServicioElegido = serviciosDisponibles[indexServicio];

        try {
            double[] coordsOrigen = obtenerCoordenadas(nombreOrigen);
            double[] coordsDestino = obtenerCoordenadas(nombreDestino);

            IRutasService rutasService = new RutasServiceImpl();
            double distanciaKm = rutasService.calcularDistanciaKm(coordsOrigen[0], coordsOrigen[1], coordsDestino[0], coordsDestino[1]);
            String detalleRuta = rutasService.calcularRuta(coordsOrigen[0], coordsOrigen[1], coordsDestino[0], coordsDestino[1]);

            double tiempoMinutos = (distanciaKm / 30.0) * 60.0;

            Servicio servicioObj = new Servicio(
                tipoServicioElegido,
                TipoServicio.PASAJEROS,
                TipoVehiculo.AUTO,
                CategoriaVehiculo.ESTANDAR,
                100.0,
                50.0,
                10.0
            );

            double costoTotal = servicioObj.calcularCosto(distanciaKm, tiempoMinutos);

            Ubicacion origenObj = new Ubicacion(nombreOrigen, coordsOrigen[0], coordsOrigen[1]);
            Ubicacion destinoObj = new Ubicacion(nombreDestino, coordsDestino[0], coordsDestino[1]);

            Viaje nuevoViaje = gestor.solicitarViaje(usuarioActual, servicioObj, origenObj, destinoObj, LocalDateTime.now());

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
        String id = scanner.nextLine().trim();
        try {
            UUID idViaje = UUID.fromString(id);
            System.out.println("Viaje seleccionado: " + idViaje);
        } catch (IllegalArgumentException e) {
            System.out.println("El ID del viaje no es válido.");
        }
    }

    public void verViajesPendientes() {
        System.out.println("\n----- VIAJES PENDIENTES -----");
        List<Viaje> pendientes = gestor.obtenerViajesPendientes();

        if (pendientes.isEmpty()) {
            System.out.println("No hay viajes pendientes en este momento.");
            return;
        }

        for (int i = 0; i < pendientes.size(); i++) {
            Viaje v = pendientes.get(i);
            String cliente = (v.getCliente() != null) ? v.getCliente().getNombre() : "Sin datos";
            String origen = (v.getOrigen() != null) ? v.getOrigen().getDireccion() : "Origen desconocido";
            String destino = (v.getDestino() != null) ? v.getDestino().getDireccion() : "Destino desconocido";

            System.out.printf(Locale.US, "%d. [%s] %s -> %s | Pasajero: %s | $%.2f%n",
                    (i + 1), v.getEstadoActual(), origen, destino, cliente, v.getCostoCalculado());
        }
    }

    public void aceptarViaje() {
        System.out.println("\n----- ACEPTAR VIAJE -----");
        List<Viaje> pendientes = gestor.obtenerViajesPendientes();

        if (pendientes.isEmpty()) {
            System.out.println("No hay viajes pendientes para aceptar.");
            return;
        }

        verViajesPendientes();
        System.out.print("\nSeleccione el número de viaje a aceptar (o '0' para cancelar): ");
        String entrada = scanner.nextLine().trim();

        if (entrada.equals("0")) {
            return;
        }

        try {
            int indice = Integer.parseInt(entrada) - 1;
            if (indice < 0 || indice >= pendientes.size()) {
                System.out.println("Número de viaje fuera de rango.");
                return;
            }

            Viaje seleccionado = pendientes.get(indice);
            gestor.aceptarViaje(seleccionado.getId(), usuarioActual, LocalDateTime.now());
            System.out.println("¡Viaje aceptado con éxito! Estado actual: " + seleccionado.getEstadoActual());

        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Ingresá un número de la lista.");
        } catch (Exception e) {
            System.out.println("No se pudo aceptar el viaje: " + e.getMessage());
        }
    }

    public void actualizarUbicacion() {
        System.out.println("\n----- ACTUALIZAR UBICACIÓN -----");
        try {
            System.out.print("Ingrese latitud: ");
            double latitud = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Ingrese longitud: ");
            double longitud = Double.parseDouble(scanner.nextLine().trim());
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