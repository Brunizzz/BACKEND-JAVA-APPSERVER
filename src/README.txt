# RutaSmart - Backend Distribuido (Spring Boot)

Sistema de gestión de transporte público distribuido con soporte para NFS, Encriptación RSA y Simulación GPS.

## 1. Requisitos Previos
- Java JDK 17 o superior.
- Maven 3.x.
- Acceso a un servidor NFS en la red.

## 2. Configuración de Infraestructura
Este backend asume que existe un montaje NFS en la siguiente ruta local:
- Ruta: `/mnt/eData/`
- Permisos: Lectura y Escritura.

Para montar el NFS (en Linux):
sudo mount <IP_SERVIDOR_NFS>:/var/nfs/eData /mnt/eData

## 3. Configuración de Seguridad
El sistema utiliza criptografía asimétrica RSA.
Antes de ejecutar, se debe crear una carpeta llamada `KeyPair` en la raíz del proyecto para almacenar las llaves generadas.

## 4. Cómo Ejecutar
1. Navegar a la carpeta del proyecto.
2. Ejecutar el comando:
   mvn clean spring-boot:run

## 5. Endpoints Principales
- Web Panel: http://<IP_SERVIDOR>:8080
- API Android: http://<IP_SERVIDOR>:8080/api/obtener-ubicacion
- Admin API: http://<IP_SERVIDOR>:8080/admin

## 6. Primeros Pasos (Reset de Fábrica)
Si es la primera vez que se corre el sistema o los datos están corruptos:
1. Asegurarse que el servidor está corriendo.
2. Hacer una petición POST a:
   http://<IP_SERVIDOR>:8080/admin/upgrade-security (Genera llaves)
3. Reiniciar servidor.
4. Hacer una petición POST a:
   http://<IP_SERVIDOR>:8080/admin/seed-db (Pobla la BD inicial)

## 7. Credenciales por Defecto
- Admin: admin / admin123
- Gerente: gerente / gerente123