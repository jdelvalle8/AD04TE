package dam.config;

import java.util.*;

import jakarta.persistence.*;
public class JpaUtil {

    public static EntityManagerFactory crearFactory() {
        //cogemos la clave de una variabel de entorno
        String clave = System.getenv("DB_PASSWORD");
        if (clave == null || clave.isBlank()) {
            throw new IllegalStateException("Define DB_PASSWORD antes de ejecutar.");
        }

        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.password", clave);

        return Persistence.createEntityManagerFactory("activa", propiedades);
    }

}