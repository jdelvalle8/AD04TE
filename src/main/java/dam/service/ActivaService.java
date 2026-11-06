package dam.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dam.domain.*;
import jakarta.persistence.*;

public class ActivaService {

    private EntityManagerFactory emf;

    public ActivaService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Long crearMonitor(String nombre, String especialidad) {
        if (nombre == null || nombre.isBlank() || especialidad == null || especialidad.isBlank()) {
            throw new IllegalArgumentException("El nombre y la especialidad no pueden estar vacios");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Monitor monitor = new Monitor(nombre, especialidad);
            em.persist(monitor);
            tx.commit();
            return monitor.getId();
        } catch (RuntimeException e) {
            deshacer(tx, e);
            throw e;
        } finally {
            em.close();
        }
    }

    public Long crearActividad(String nombre, String localidad, LocalDateTime fechaHora, int plazas, Long monitorId) {
        if (plazas <= 0) {
            throw new IllegalArgumentException("Las plazas tienen que ser un numero positivo");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Monitor monitor = em.find(Monitor.class, monitorId);
            if (monitor == null) {
                throw new IllegalArgumentException("No existe el monitor con ID " + monitorId);
            }
            Actividad actividad = new Actividad(nombre, localidad, fechaHora, plazas);
            actividad.setMonitor(monitor);
            em.persist(actividad);
            tx.commit();
            return actividad.getId();
        } catch (RuntimeException e) {
            deshacer(tx, e);
            throw e;
        } finally {
            em.close();
        }
    }

    public Long crearCliente(String nombre, String email) {
        //quito espacios y paso a minusculas para comparar los emails
        String emailLimpio = email.trim().toLowerCase();
        if (emailLimpio.isEmpty() || !emailLimpio.contains("@") || !emailLimpio.contains(".")) {
            throw new IllegalArgumentException("El email no es válido");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Long repetidos = em.createQuery(
                    "SELECT COUNT(c) FROM Cliente c WHERE LOWER(c.email) = :email", Long.class)
                    .setParameter("email", emailLimpio)
                    .getSingleResult();
            if (repetidos > 0) {
                throw new IllegalArgumentException("Ya existe un cliente con ese email");
            }
            Cliente cliente = new Cliente(nombre, emailLimpio);
            em.persist(cliente);
            tx.commit();
            return cliente.getId();
        } catch (RuntimeException e) {
            deshacer(tx, e);
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Actividad> buscarDisponibles(String localidad, LocalDate dia, int plazas) {
        if (plazas <= 0) {
            throw new IllegalArgumentException("La cantidad tiene que ser positiva");
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Actividad a JOIN FETCH a.monitor "
                    + "WHERE LOWER(a.localidad) = :localidad "
                    + "AND a.fechaHora >= :inicio AND a.fechaHora < :fin "
                    + "AND a.plazasDisponibles >= :plazas "
                    + "ORDER BY a.fechaHora", Actividad.class)
                    .setParameter("localidad", localidad.toLowerCase())
                    .setParameter("inicio", dia.atStartOfDay())
                    .setParameter("fin", dia.plusDays(1).atStartOfDay())
                    .setParameter("plazas", plazas)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Actividad> listarActividades() {
        EntityManager em = emf.createEntityManager();
        try {
            //JOIN FETCH para tener el monitor cargado al cerrar el em
            return em.createQuery(
                    "SELECT a FROM Actividad a JOIN FETCH a.monitor ORDER BY a.fechaHora", Actividad.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
    public Reserva crearReserva(Long clienteId, Long actividadId, int plazas) {
        if (plazas <= 0) {
            throw new IllegalArgumentException("La cantidad tiene que ser positiva");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Cliente cliente = em.find(Cliente.class, clienteId);
            if (cliente == null) {
                throw new IllegalArgumentException("No existe el cliente con ID " + clienteId);
            }
            Actividad actividad = em.find(Actividad.class, actividadId);
            if (actividad == null) {
                throw new IllegalArgumentException("No existe la actividad con ID " + actividadId);
            }
            if (actividad.getPlazasDisponibles() < plazas) {
                throw new IllegalArgumentException("No hay plazas suficientes, quedan " + actividad.getPlazasDisponibles());
            }

            //se guarda la reserva y se descuentan las plazas en la misma tx
            actividad.setPlazasDisponibles(actividad.getPlazasDisponibles() - plazas);
            if (actividad.getPlazasDisponibles() < 0) {
                throw new IllegalArgumentException("No hay plazas suficientes");
            }
            Reserva reserva = new Reserva(cliente, actividad, plazas, LocalDateTime.now());
            em.persist(reserva);
            tx.commit();
            return reserva;
        } catch (RuntimeException e) {
            deshacer(tx, e);
            throw e;
        } finally {
            em.close();
        }
    }

    public Reserva cancelarReserva(Long reservaId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reserva reserva = em.find(Reserva.class, reservaId);
            if (reserva == null) {
                throw new IllegalArgumentException("No existe la reserva con ID " + reservaId);
            }

            //devuelvo las plazas y borro la reserva en la misma tx
            Actividad actividad = reserva.getActividad();
            actividad.setPlazasDisponibles(actividad.getPlazasDisponibles() + reserva.getNumeroPlazas());
            em.remove(reserva);
            tx.commit();
            return reserva;
        } catch (RuntimeException e) {
            deshacer(tx, e);
            throw e;
        } finally {
            em.close();
        }
    }
    
    //deshace la transaccion si sigue abierta
    private static void deshacer(EntityTransaction tx, RuntimeException original) {
        if (tx.isActive()) {
            try {
                tx.rollback();
            } catch (RuntimeException errorRollback) {
                original.addSuppressed(errorRollback);
            }
        }
    }

}