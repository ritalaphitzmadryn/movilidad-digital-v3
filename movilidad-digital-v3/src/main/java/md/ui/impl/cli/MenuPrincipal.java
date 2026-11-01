package md.ui.impl.cli;

import java.util.Scanner;

import md.logica.GestorMovilidad;

public class MenuPrincipal {

    private Scanner teclado;
    private GestorMovilidad gestor;

    public MenuPrincipal(GestorMovilidad gestor) {
        this.gestor = gestor;
        this.teclado = new Scanner(System.in);
    }

    public void mostrarMenu() {

        int opcion;

        do {

            System.out.println();
            System.out.println("================================");
            System.out.println("       MOVILIDAD DIGITAL");
            System.out.println("================================");
            System.out.println("1. Ingresar");
            System.out.println("0. Salir");
            System.out.println("--------------------------------");
            System.out.print("Seleccione una opción: ");

            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {

                case 1:
                    ingresarUsuario();
                    break;

                case 0:
                    System.out.println(
                        "Gracias por utilizar Movilidad Digital."
                    );
                    break;

                default:
                    System.out.println(
                        "Opción incorrecta."
                    );
            }

        } while (opcion != 0);
    }

    private void ingresarUsuario() {

        System.out.println();
        System.out.println("--- INGRESO DE USUARIO ---");

        InterfazUsuario interfaz =
                new InterfazUsuario(gestor);

        interfaz.iniciar();
    }
}