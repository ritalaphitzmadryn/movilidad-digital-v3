package md.ui.impl.cli;

import java.util.InputMismatchException;
import java.util.Scanner;
import md.logica.GestorMovilidad;
import md.modelo.RolUsuario;
import md.modelo.Usuario;

public class MenuPrincipal {

    private Scanner teclado;
    private GestorMovilidad gestor;

    public MenuPrincipal(GestorMovilidad gestor) {
        this.gestor = gestor;
        this.teclado = new Scanner(System.in);
    }

    public void mostrarMenu() {
        int opcion = -1;

        do {
            try {
                System.out.println();
                System.out.println("================================");
                System.out.println("        MOVILIDAD DIGITAL");
                System.out.println("================================");
                System.out.println("1. Ingresar al Sistema");
                System.out.println("0. Salir");
                System.out.println("--------------------------------");
                System.out.print("Seleccione una opción: ");

                opcion = teclado.nextInt();
                teclado.nextLine(); // Limpiar el buffer

                switch (opcion) {
                    case 1:
                        ingresarUsuario();
                        break;

                    case 0:
                        System.out.println();
                        System.out.println("Gracias por utilizar Movilidad Digital. ¡Hasta luego!");
                        break;

                    default:
                        System.out.println("Opción incorrecta. Por favor, elija 1 o 0.");
                }

            } catch (InputMismatchException e) {
                System.out.println("Error: Debe ingresar un número válido.");
                teclado.nextLine(); // Limpiar la entrada errónea
                opcion = -1;
            }

        } while (opcion != 0);
    }

    private void ingresarUsuario() {
        System.out.println();
        System.out.println("--- INGRESO DE USUARIO ---");
        System.out.print("Ingrese su ID de usuario: ");
        String idUsuario = teclado.nextLine();

        // Buscamos el usuario directamente a través del gestor
        Usuario usuarioEncontrado = null;
        for (Usuario u : gestor.obtenerUsuarios()) {
            if (u.getId().equals(idUsuario)) {
                usuarioEncontrado = u;
                break;
            }
        }

        if (usuarioEncontrado == null) {
            System.out.println("Usuario no encontrado en el sistema.");
            return;
        }

        System.out.println("¡Bienvenido/a, " + usuarioEncontrado.getNombre() + "!");

        // Validamos qué roles tiene disponibles
        boolean tieneCliente = usuarioEncontrado.getCliente() != null;
        boolean tieneConductor = usuarioEncontrado.getConductor() != null;

        if (tieneCliente && tieneConductor) {
            System.out.println("\n¿Con qué rol desea iniciar sesión?");
            System.out.println("1. Cliente");
            System.out.println("2. Conductor");
            System.out.print("Seleccione una opción de rol: ");
            String opcionRol = teclado.nextLine();

            if (opcionRol.equals("2")) {
                usuarioEncontrado.cambiarRolActivo(RolUsuario.CONDUCTOR);
                System.out.println("Rol asignado: CONDUCTOR");
            } else {
                usuarioEncontrado.cambiarRolActivo(RolUsuario.CLIENTE);
                System.out.println("Rol asignado: CLIENTE");
            }
        } else if (tieneConductor) {
            usuarioEncontrado.cambiarRolActivo(RolUsuario.CONDUCTOR);
            System.out.println("Iniciando automáticamente como Conductor...");
        } else {
            usuarioEncontrado.cambiarRolActivo(RolUsuario.CLIENTE);
            System.out.println("Iniciando automáticamente como Cliente...");
        }

        // Una vez configurado el usuario y su rol, abrimos la interfaz de consola pasándole el gestor
        InterfazUsuario interfaz = new InterfazUsuario(gestor);
        
        // Opcional: si querés que la interfaz ya reciba el usuario logueado, 
        // podés agregarle un método setter o pasárselo por constructor.
        interfaz.iniciar();
    }
    
}