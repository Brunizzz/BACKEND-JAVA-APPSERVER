package com.rutasmart;

import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.nio.file.Files;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RutaSmartController {

    private static final String RUTA_NFS = "/mnt/eData/";
    private final AsymmetricCryptography ac;

    public RutaSmartController() throws Exception {
        this.ac = new AsymmetricCryptography();
    }

    @GetMapping("/obtener-ubicacion")
    public String obtenerUbicacion() {
        try {
            File file = new File(RUTA_NFS + "bus_live_location.json");
            if (!file.exists()) return "[]";
            var key = ac.getPublic("KeyPair/publicKey");
            byte[] bytes = Files.readAllBytes(file.toPath());
            return ac.decryptText(new String(bytes), key);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public void procesar(String nombreArchivo, String contenido) throws Exception {
        var key = ac.getPrivate("KeyPair/privateKey");
        String cifrado = ac.encryptText(contenido, key);
        ac.writeEncryptedFile(RUTA_NFS + nombreArchivo, cifrado);
    }
}