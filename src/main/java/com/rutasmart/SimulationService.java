package com.rutasmart;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimulationService {
    private final RutaSmartController controller;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AsymmetricCryptography ac;
    private static final String RUTA_NFS = "/mnt/eData/";
    private final Map<String, Integer> busIndices = new HashMap<>();

    private final double[][] PATH_R01 = {{21.8818, -102.2916}, {21.8830, -102.2925}, {21.8850, -102.2940}, {21.8900, -102.2960}, {21.8850, -102.2940}};
    private final double[][] PATH_R20 = {{21.8800, -102.2700}, {21.8805, -102.2750}, {21.8810, -102.2800}, {21.8825, -102.3000}, {21.8810, -102.2800}};
    private final double[][] PATH_GENERICO = {{21.8818, -102.2916}, {21.8830, -102.2930}, {21.8840, -102.2950}, {21.8820, -102.2960}, {21.8818, -102.2916}};

    public SimulationService(RutaSmartController controller) throws Exception {
        this.controller = controller;
        this.ac = new AsymmetricCryptography();
    }

    @Scheduled(fixedRate = 3000)
    public void moverCombis() {
        try {
            File file = new File(RUTA_NFS + "buses.json");
            if (!file.exists()) return;
            var key = ac.getPublic("KeyPair/publicKey");
            byte[] bytes = Files.readAllBytes(file.toPath());
            String json = ac.decryptText(new String(bytes), key);
            List<Map<String, Object>> combis = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
            
            if (combis.isEmpty()) { controller.procesar("bus_live_location.json", "[]"); return; }
            List<Map<String, Object>> flota = new ArrayList<>();
            for (Map<String, Object> bus : combis) {
                if ("Inactivo".equalsIgnoreCase((String)bus.get("status"))) continue;
                String id = (String) bus.get("id");
                String routeId = bus.containsKey("routeId") ? (String)bus.get("routeId") : "R-01";
                double[][] ruta = "R-01".equals(routeId) ? PATH_R01 : ("R-20".equals(routeId) ? PATH_R20 : PATH_GENERICO);
                int idx = busIndices.getOrDefault(id, 0);
                idx = (idx + 1) % ruta.length;
                busIndices.put(id, idx);
                Map<String, Object> vivo = new HashMap<>();
                vivo.put("busId", id);
                vivo.put("lat", ruta[idx][0]);
                vivo.put("lon", ruta[idx][1]);
                vivo.put("estado", bus.get("status"));
                vivo.put("routeId", routeId);
                flota.add(vivo);
            }
            controller.procesar("bus_live_location.json", mapper.writeValueAsString(flota));
        } catch (Exception e) { System.err.println("Simulación error: " + e.getMessage()); }
    }
}