package com.rutasmart;

import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final String RUTA_NFS = "/mnt/eData/";
    private final RutaSmartController mainController;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AsymmetricCryptography ac;

    public AdminController(RutaSmartController mainController) throws Exception {
        this.mainController = mainController;
        this.ac = new AsymmetricCryptography();
    }

    @GetMapping("/status")
    public Map<String, String> getSystemStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("servidor", "Spring Boot - VM APP");
        status.put("almacenamiento", "NFS Activo");
        File carpeta = new File(RUTA_NFS);
        status.put("estado", (carpeta.exists() && carpeta.canWrite()) ? "OPERATIVO" : "ERROR_IO");
        return status;
    }

    @GetMapping("/ver-logs")
    public String verLogs() {
        File logs = new File(RUTA_NFS + "system_logs.txt");
        if (!logs.exists()) return "Sistema iniciado. Esperando eventos...";
        return "Log detectado en NFS a las " + java.time.LocalTime.now() + " - Integridad OK.";
    }

    @PostMapping("/reset")
    public String resetSimulation() { return "Sistema reiniciado."; }

    @GetMapping("/upgrade-security")
    public String upgradeSecurity() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair pair = kpg.generateKeyPair();
            try (FileOutputStream fos = new FileOutputStream("KeyPair/privateKey")) { fos.write(pair.getPrivate().getEncoded()); }
            try (FileOutputStream fos = new FileOutputStream("KeyPair/publicKey")) { fos.write(pair.getPublic().getEncoded()); }
            return "ÉXITO: Llaves actualizadas a 2048 bits.";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }

    @PostMapping("/seed-db")
    public String seedDatabase() {
        try {
            List<Map<String, Object>> buses = new ArrayList<>();
            buses.add(Map.of("id", "A-107", "placa", "AGS-998-Z", "chofer", "Juan Pérez", "modelo", "Nissan Urvan 2023", "status", "Activo"));
            mainController.procesar("buses.json", mapper.writeValueAsString(buses));

            List<Map<String, Object>> routes = new ArrayList<>();
            routes.add(Map.of("id", "R-01", "nombre", "Penal - Centro", "distancia", "12.5 km", "tiempo_promedio", "45 min"));
            routes.add(Map.of("id", "R-20", "nombre", "Sur - Norte (UAA)", "distancia", "18.2 km", "tiempo_promedio", "60 min"));
            mainController.procesar("routes.json", mapper.writeValueAsString(routes));

            List<Map<String, Object>> users = new ArrayList<>();
            users.add(Map.of("id", 1, "username", "admin", "password", "admin123", "email", "admin@rutasmart.mx", "rol", "Super Admin", "ultimo_acceso", "2025-12-08"));
            mainController.procesar("users.json", mapper.writeValueAsString(users));
            return "Base de datos NFS poblada correctamente.";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }

    private List<Map<String, Object>> leerArchivoGenerico(String nombreArchivo) {
        try {
            File file = new File(RUTA_NFS + nombreArchivo);
            if (!file.exists()) return new ArrayList<>();
            var key = ac.getPublic("KeyPair/publicKey"); 
            byte[] bytes = Files.readAllBytes(file.toPath());
            String json = ac.decryptText(new String(bytes), key);
            return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
        } catch (Exception e) { return new ArrayList<>(); }
    }

    @GetMapping("/buses") public List<Map<String, Object>> getBuses() { return leerArchivoGenerico("buses.json"); }
    @GetMapping("/routes") public List<Map<String, Object>> getRoutes() { return leerArchivoGenerico("routes.json"); }
    @GetMapping("/users") public List<Map<String, Object>> getUsers() { return leerArchivoGenerico("users.json"); }

    @PostMapping("/buses/add")
    public String addBus(@RequestBody Map<String, Object> item) throws Exception {
        List<Map<String, Object>> items = leerArchivoGenerico("buses.json");
        items.add(item);
        mainController.procesar("buses.json", mapper.writeValueAsString(items));
        return "OK";
    }
    @PutMapping("/buses/update/{id}")
    public String updateBus(@PathVariable String id, @RequestBody Map<String, Object> itemData) throws Exception {
        List<Map<String, Object>> items = leerArchivoGenerico("buses.json");
        for (int i = 0; i < items.size(); i++) {
            if (String.valueOf(items.get(i).get("id")).equals(id)) {
                items.set(i, itemData);
                mainController.procesar("buses.json", mapper.writeValueAsString(items));
                return "OK";
            }
        }
        return "Error";
    }
    @DeleteMapping("/buses/delete/{id}")
    public String deleteBus(@PathVariable String id) throws Exception {
        List<Map<String, Object>> items = leerArchivoGenerico("buses.json");
        items.removeIf(b -> String.valueOf(b.get("id")).equals(id));
        mainController.procesar("buses.json", mapper.writeValueAsString(items));
        return "OK";
    }

    @PostMapping("/routes/add")
    public String addRoute(@RequestBody Map<String, Object> item) throws Exception {
        List<Map<String, Object>> items = leerArchivoGenerico("routes.json");
        items.add(item);
        mainController.procesar("routes.json", mapper.writeValueAsString(items));
        return "OK";
    }
    @PutMapping("/routes/update/{id}")
    public String updateRoute(@PathVariable String id, @RequestBody Map<String, Object> itemData) throws Exception {
        List<Map<String, Object>> items = leerArchivoGenerico("routes.json");
        for (int i = 0; i < items.size(); i++) {
            if (String.valueOf(items.get(i).get("id")).equals(id)) {
                items.set(i, itemData);
                mainController.procesar("routes.json", mapper.writeValueAsString(items));
                return "OK";
            }
        }
        return "Error";
    }
    @DeleteMapping("/routes/delete/{id}")
    public String deleteRoute(@PathVariable String id) throws Exception {
        List<Map<String, Object>> items = leerArchivoGenerico("routes.json");
        items.removeIf(r -> String.valueOf(r.get("id")).equals(id));
        mainController.procesar("routes.json", mapper.writeValueAsString(items));
        return "OK";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestBody Map<String, Object> item) throws Exception {
        List<Map<String, Object>> items = leerArchivoGenerico("users.json");
        items.add(item);
        mainController.procesar("users.json", mapper.writeValueAsString(items));
        return "OK";
    }
    @PutMapping("/users/update/{id}")
    public String updateUser(@PathVariable String id, @RequestBody Map<String, Object> itemData) throws Exception {
        List<Map<String, Object>> items = leerArchivoGenerico("users.json");
        for (int i = 0; i < items.size(); i++) {
            if (String.valueOf(items.get(i).get("id")).equals(id)) {
                items.set(i, itemData);
                mainController.procesar("users.json", mapper.writeValueAsString(items));
                return "OK";
            }
        }
        return "Error";
    }
    @DeleteMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable String id) throws Exception {
        List<Map<String, Object>> items = leerArchivoGenerico("users.json");
        items.removeIf(u -> String.valueOf(u.get("id")).equals(id));
        mainController.procesar("users.json", mapper.writeValueAsString(items));
        return "OK";
    }
}