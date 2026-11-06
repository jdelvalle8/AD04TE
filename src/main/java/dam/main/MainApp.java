package dam.main;

import java.time.*;
import java.time.format.*;
import java.util.*;

import dam.config.JpaUtil;
import dam.domain.*;
import dam.service.ActivaService;
import jakarta.persistence.*;

public class MainApp {

    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try (EntityManagerFactory emf = JpaUtil.crearFactory()) {
            ActivaService servicio = new ActivaService(emf);
            int opcion = 0;

            while (opcion != 8) {
                System.out.println("=== ACTIVA · Gestión de actividades ===");
                System.out.println();
                System.out.println("1. Crear monitor");
                System.out.println("2. Crear actividad");
                System.out.println("3. Buscar actividades disponibles");
                System.out.println("4. Crear cliente");
                System.out.println("5. Crear reserva");
                System.out.println("6. Cancelar reserva");
                System.out.println("7. Listar actividades");
                System.out.println("8. Salir");

                try {
                    opcion = leerEntero("Opción: ");
                    switch (opcion) {
                    case 1:
                        crearMonitor(servicio);
                        break;
                    case 2:
                        crearActividad(servicio);
                        break;
                    case 3:
                        buscarDisponibles(servicio);
                        break;
                    case 4:
                        crearCliente(servicio);
                        break;
                    case 5:
                        crearReserva(servicio);
                        break;
                    case 6:
                        cancelarReserva(servicio);
                        break;
                    case 7:
                        listarActividades(servicio);
                        break;
                    case 8:
                        break;
                    default:
                        System.out.println("Opción no válida, elige de 1 a 8");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                } catch (PersistenceException e) {
                    System.out.println("No se ha podido completar la operación, no se ha guardado nada");
                }
            }
        }
    }

    private static void crearMonitor(ActivaService servicio) {
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Especialidad: ");
        String especialidad = sc.nextLine();

        Long id = servicio.crearMonitor(nombre, especialidad);
        System.out.println("Monitor creado con ID " + id);
    }

    private static void crearActividad(ActivaService servicio) {
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Localidad: ");
        String localidad = sc.nextLine();
        LocalDateTime fechaHora = leerFechaHora("Fecha y hora (formato dd/MM/yyyy HH:mm): ");
        int plazas = leerEntero("Plazas totales: ");
        Long monitorId = leerLong("ID del monitor: ");

        servicio.crearActividad(nombre, localidad, fechaHora, plazas, monitorId);
        System.out.println("Actividad creada");
    }

    private static void buscarDisponibles(ActivaService servicio) {
        System.out.print("Localidad: ");
        String localidad = sc.nextLine();
        LocalDate dia = leerFecha("Día (formato dd/MM/yyyy): ");
        int plazas = leerEntero("Plazas necesarias: ");

        List<Actividad> actividades = servicio.buscarDisponibles(localidad, dia, plazas);
        if (actividades.isEmpty()) {
            System.out.println("No hay actividades con esos datos");
        } else {
            for (Actividad a : actividades) {
                System.out.println("ID " + a.getId() + " | " + a.getNombre() + " | " + a.getFechaHora()
                        + " | Monitor: " + a.getMonitor().getNombre()
                        + " | Plazas disponibles: " + a.getPlazasDisponibles());
            }
        }
    }

    private static void crearCliente(ActivaService servicio) {
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();

        Long id = servicio.crearCliente(nombre, email);
        System.out.println("Cliente creado con ID " + id);
    }

    private static void crearReserva(ActivaService servicio) {
        Long clienteId = leerLong("ID del cliente: ");
        Long actividadId = leerLong("ID de la actividad: ");
        int plazas = leerEntero("Número de plazas: ");

        Reserva reserva = servicio.crearReserva(clienteId, actividadId, plazas);
        System.out.println("Reserva creada con ID " + reserva.getId() 
        + ". Plazas restantes: " + reserva.getActividad().getPlazasDisponibles());
    }

    private static void cancelarReserva(ActivaService servicio) {
        Long reservaId = leerLong("ID de la reserva: ");

        Reserva reserva = servicio.cancelarReserva(reservaId);
        System.out.println("Reserva cancelada.");
        System.out.println("Plazas recuperadas: " + reserva.getNumeroPlazas());
        System.out.println("Plazas disponibles: " + reserva.getActividad().getPlazasDisponibles());
    }

    private static void listarActividades(ActivaService servicio) {
        List<Actividad> actividades = servicio.listarActividades();
        if (actividades.isEmpty()) {
            System.out.println("No hay actividades");
        } else {
            for (Actividad a : actividades) {
                System.out.println("ID " + a.getId() + " | " + a.getNombre() + " | " + a.getLocalidad() + " | "
                        + a.getFechaHora() + " | Monitor: " + a.getMonitor().getNombre()
                        + " | Totales: " + a.getPlazasTotales() + " | Disponibles: " + a.getPlazasDisponibles());
            }
        }
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);
        try {
            return Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tienes que ser un número entero");
        }
    }

    private static Long leerLong(String mensaje) {
        System.out.print(mensaje);
        try {
            return Long.parseLong(sc.nextLine());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID tiene que ser numérico");
        }
    }

    private static LocalDateTime leerFechaHora(String mensaje) {
        System.out.print(mensaje);
        try {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
            return LocalDateTime.parse(sc.nextLine(), formato);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha y hora no válidas");
        }
    }

    private static LocalDate leerFecha(String mensaje) {
        System.out.print(mensaje);
        try {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            return LocalDate.parse(sc.nextLine(), formato);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha no válida");
        }
    }

}