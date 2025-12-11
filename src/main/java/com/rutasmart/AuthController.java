package com.rutasmart;

import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final String RUTA_NFS = "/mnt/eData/";
    private final AsymmetricCryptography crypto; 
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthController() throws Exception {
        this.crypto = new AsymmetricCryptography();
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credenciales) {
        String userIngresado = credenciales.get("username");
        String passIngresado = credenciales.get("password");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            File file = new File(RUTA_NFS + "users.json");
            if (!file.exists()) {
                respuesta.put("error", "DB no encontrada");
                return respuesta;
            }

            var publicKey = crypto.getPublic("KeyPair/publicKey"); 
            byte[] bytesCifrados = Files.readAllBytes(file.toPath());
            String jsonLimpio = crypto.decryptText(new String(bytesCifrados), publicKey);

            List<Map<String, Object>> usuarios = mapper.readValue(jsonLimpio, new TypeReference<List<Map<String, Object>>>(){});

            for (Map<String, Object> u : usuarios) {
                String uName = String.valueOf(u.get("username"));
                String uPass = String.valueOf(u.get("password"));

                if (uName.equals(userIngresado) && uPass.equals(passIngresado)) {
                    respuesta.put("success", true);
                    respuesta.put("user", u);
                    respuesta.put("token", "jwt-token-simulado-123");
                    return respuesta;
                }
            }
            respuesta.put("success", false);
            respuesta.put("message", "Credenciales incorrectas");
        } catch (Exception e) {
            respuesta.put("error", "Error interno: " + e.getMessage());
        }
        return respuesta;
    }
}