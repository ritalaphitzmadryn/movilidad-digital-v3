package md.impl.cli;

import java.util.Scanner;
import java.util.UUID;

import md.logica.GestorMovilidad;
import md.modelo.RolUsuario;
import md.modelo.Usuario;
import md.modelo.Viaje;

public class InterfazUsuario {

    private GestorMovilidad gestor;
    private Scanner scanner;
    private Usuario usuarioActual;

    public InterfazUsuario(GestorMovilidad gestor) {
        this.gestor = gestor;
        this.scanner = new Scanner(System.in);
    }

    // =====================================================
    // INICIO
    // =====================================================

    public void iniciar() {

        System.out.println("================================");
        System.out.println("       MOVILIDAD DIGITAL");
        System.out.println("================================");

        System.out.print("Ingrese su usuario: ");
        String idUsuario = scanner.nextLine();

        usuarioActual = buscarUsuario(idUsuario);

        if (usuarioActual == null) {
            System.out.println("Usuario no encontrado.");
            return;
        }

        mostrarMenu();
    }

    // =====================================================
    // BUSCAR USUARIO
    // =====================================================

    private Usuario buscarUsuario(String idUsuario) {

        for (Usuario usuario : gestor.obtenerUsuarios()) {

            if (usuario.getId().equals(idUsuario)) {
                return usuario;
            }
        }

        return null;
    }

    // =====================================================
    // MENÚ PRINCIPAL
    // =====================================================

    private void mostrarMenu() {

        boolean salir = false;

        while (!salir) {

            System.out.println();
            System.out.println("================================");
            System.out.println("Usuario: " + usuarioActual.getNombre());
            System.out.println(
                    "Rol activo: " + usuarioActual.getRolActivo()
            );
            System.out.println("================================");

            if (usuarioActual.getRolActivo() == RolUsuario.CLIENTE) {

                mostrarMenuCliente();

            } else if (usuarioActual.getRolActivo() == RolUsuario.CONDUCTOR) {

                mostrarMenuConductor();
            }

            System.out.println("0. Salir");

            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            if (usuarioActual.getRolActivo() == RolUsuario.CLIENTE) {

                salir = procesarOpcionCliente(opcion);

            } else {

                salir = procesarOpcionConductor(opcion);
            }
        }
    }

    // =====================================================
    // MENÚ CLIENTE
    // =====================================================

    private void mostrarMenuCliente() {

        System.out.println();
        System.out.println("----- MENÚ CLIENTE -----");
        System.out.println("1. Solicitar viaje");
        System.out.println("2. Consultar historial");
        System.out.println("3. Calificar servicio");

        if (usuarioActual.getConductor() != null) {
            System.out.println("4. Cambiar a modo conductor");
        }
    }

    // =====================================================
    // MENÚ CONDUCTOR
    // =====================================================

    private void mostrarMenuConductor() {

        System.out.println();
        System.out.println("----- MENÚ CONDUCTOR -----");
        System.out.println("1. Ver viajes pendientes");
        System.out.println("2. Aceptar viaje");
        System.out.println("3. Actualizar ubicación");

        if (usuarioActual.getCliente() != null) {
            System.out.println("4. Cambiar a modo cliente");
        }
    }

    // =====================================================
    // OPCIONES CLIENTE
    // =====================================================

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
                return true;

            default:
                System.out.println("Opción no válida.");
        }

        return false;
    }

    // =====================================================
    // OPCIONES CONDUCTOR
    // =====================================================

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
                cambiarACliente();
                break;

            case "0":
                return true;

            default:
                System.out.println("Opción no válida.");
        }

        return false;
    }

    // =====================================================
    // OPERACIONES CLIENTE
    // =====================================================

    public void solicitarViaje() {

        System.out.println();
        System.out.println("----- SOLICITAR VIAJE -----");

        System.out.println(
                "Funcionalidad de solicitar viaje."
        );
    }

    public void consultarHistorial() {

        System.out.println();
        System.out.println("----- HISTORIAL DE VIAJES -----");

        for (Viaje viaje :
                usuarioActual.getCliente().getViajes()) {

            System.out.println(
                    "Viaje: " + viaje.getId()
                    + " | Estado: "
                    + viaje.getEstadoActual()
            );
        }
    }

    public void calificarServicio() {

        System.out.println();
        System.out.println("----- CALIFICAR SERVICIO -----");

        System.out.print("Ingrese el ID del viaje: ");

        String id = scanner.nextLine();

        try {

            UUID idViaje = UUID.fromString(id);

            System.out.println(
                    "Viaje seleccionado: " + idViaje
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "El ID del viaje no es válido."
            );
        }
    }

    // =====================================================
    // OPERACIONES CONDUCTOR
    // =====================================================

    public void verViajesPendientes() {

        System.out.println();
        System.out.println("----- VIAJES PENDIENTES -----");

        System.out.println(
                "Funcionalidad de consulta de viajes pendientes."
        );
    }

    public void aceptarViaje() {

        System.out.println();
        System.out.println("----- ACEPTAR VIAJE -----");

        System.out.print("Ingrese el ID del viaje: ");

        String id = scanner.nextLine();

        try {

            UUID idViaje = UUID.fromString(id);

            System.out.println(
                    "Viaje seleccionado: " + idViaje
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "El ID del viaje no es válido."
            );
        }
    }

    public void actualizarUbicacion() {

        System.out.println();
        System.out.println("----- ACTUALIZAR UBICACIÓN -----");

        System.out.print("Ingrese latitud: ");
        double latitud =
                Double.parseDouble(scanner.nextLine());

        System.out.print("Ingrese longitud: ");
        double longitud =
                Double.parseDouble(scanner.nextLine());

        System.out.println(
                "Nueva ubicación: "
                + latitud + ", " + longitud
        );
    }

    // =====================================================
    // CAMBIO DE ROL
    // =====================================================

    private void cambiarAConductor() {

        usuarioActual.cambiarRolActivo(
                RolUsuario.CONDUCTOR
        );

        System.out.println(
                "Ahora está utilizando el sistema como conductor."
        );
    }

    private void cambiarACliente() {

        usuarioActual.cambiarRolActivo(
                RolUsuario.CLIENTE
        );

        System.out.println(
                "Ahora está utilizando el sistema como cliente."
        );
    }
}