package com.rutasmart;

import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RutaSmartController {

    private static final String RUTA_NFS = "/mnt/eData/";
    private final AsymmetricCryptography ac;

    public RutaSmartController() throws Exception {
        this.ac = new AsymmetricCryptography();
    }

    // Endpoint para Android (Consulta Ubicaciones en Vivo)
    @GetMapping("/obtener-ubicacion")
    public String obtenerUbicacion() {
        try {
            File file = new File(RUTA_NFS + "bus_live_location.json");
            if (!file.exists()) return "[]";

            // Android lee público, por lo tanto el Servidor escribió con PrivateKey
            // Para leer usamos PublicKey
            var key = ac.getPublic("KeyPair/publicKey");
            byte[] bytes = Files.readAllBytes(file.toPath());
            return ac.decryptText(new String(bytes), key);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // Método auxiliar usado por AdminController y SimulationService
    // Cifra con llave PRIVADA y escribe en NFS
    public void procesar(String nombreArchivo, String contenido) throws Exception {
        var key = ac.getPrivate("KeyPair/privateKey");
        String cifrado = ac.encryptText(contenido, key);
        ac.writeEncryptedFile(RUTA_NFS + nombreArchivo, cifrado);
    }
}