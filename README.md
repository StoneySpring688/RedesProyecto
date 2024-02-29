# RedesProyecto 2023-2024



Este proyecto consiste en un sistema de compartición y transferencia de
ficheros

## Features

- login
- help
- logout
- userlist

## Documentation

en la terminal introducir uno de los comandos:
#### login
- contacta con el servidor udp para enviar el nombre de usuario introducido con el comando 
- si el nickname es valido, el directorio lo registra en la lista de usuarios y devuelve un identificador(sessionKey)
- el identificador actuará como una contraseña en el servidor que el cliente deberá incluir en los mensajes sucesivos que se envien al directorio
-el directorio comprobará que el identificador es valido en cada mensaje que reciba(excepto en las solicitudes de login)
### help
- muestra una guia de los distintos comandos
### logout
- cierra la sesion activa en el servidor
### userlist
- imprime una lista con los usuarios loggeados en el servidor

## Autores

- [@StoneySpring688](https://github.com/StoneySpring688)
- [@pabletel](https://github.com/pabletel)
