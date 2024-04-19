# RedesProyecto 2023-2024



Este proyecto consiste en un sistema de compartición y transferencia de
ficheros

## Features

- login
- help
- logout
- userlist
- downloadfrom
- fgserve & bgserve

## Documentation

en la terminal introducir uno de los comandos:
#### login
- contacta con el servidor udp para enviar el nombre de usuario introducido con el comando 
- si el nickname es valido, el directorio lo registra en la lista de usuarios y devuelve un identificador(sessionKey)
- el identificador actuará como una contraseña en el servidor que el cliente deberá incluir en los mensajes sucesivos que se envien al directorio
- el directorio comprobará que el identificador es valido en cada mensaje que reciba(excepto en las solicitudes de login)
- uso : login DIRECCIÓN NICKNAME

### help
- muestra una guia de los distintos comandos

### logout
- cierra la sesion activa en el servidor

### userlist
- imprime una lista con los usuarios loggeados en el servidor

### downloadfrom
- primera forma de uso : DIRECCION:PUERTO
  si se conoce la dirección y el puerto de el servidor de ficheros se puede efectuar la descarga con ellos
- uso : downloadfrom DIRECCION:PUERTO HASH FILENAME

### fgserve & bgserve
- hay dos comandos paara establecer un servidor de ficheros :
#### fgserve
- establece un servidor de ficheros en primer plano, tras eso no podrá utilizarse la consola de comandos
#### bgserve
- establece un servidor de ficheros en segundo plano, lo que permite seguir utilizando la consola de comandos


## Software empleado

- [JFLAP](https://www.jflap.org). 

## Autores

- [@StoneySpring688](https://github.com/StoneySpring688)
- [@pabletel](https://github.com/pabletel)
