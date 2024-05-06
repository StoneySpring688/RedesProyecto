# RedesProyecto 2023-2024



Este proyecto consiste en un sistema de compartición y transferencia de
ficheros

## Features

- login
- help
- logout
- userlist
- downloadfrom
- download
- fgserve & bgserve
- stopServer
- publish
- filelist
- search

## Documentation

Ejecutar uno o ambos archivos JAR. En el caso de ejecutar NanoFiles : 
- introducir los archivos que se deseen compartir en la carpeta nf-shared, la cual se creará en el directorio donde se encuantre el archivo JAR
- en la terminal introducir uno de los comandos:
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
  uso : downloadfrom NICK HASH FILENAME

### download
- descarga un fichero, de forma concurrente, de todos los peers que lo compartan 
- uso : download HASH FNAME

### fgserve & bgserve
- hay dos comandos para establecer un servidor de ficheros :
#### fgserve
- establece un servidor de ficheros en primer plano, tras eso no podrá utilizarse la consola de comandos
#### bgserve
- establece un servidor de ficheros en segundo plano, lo que permite seguir utilizando la consola de comandos

### stopServer
- finaliza la ejecución del servidor, dá de baja el peer y en caso de ser necesario sus ficheros de el servidor de directorios 

### publish
- publica los ficheros compartidos y su metainformación en  el servidor de directorios

### filelist
- muestra una lista con información de todos los ficheros publicados en el directorio

### search
- muestra una lista de los peers que comparten un determinado fichero
- si el hash no se encuentra se envia un error
- si se encuentran varios hash iguales, imprime un mensaje con información de los ficheros encontrados
- uso : search HASH

### quit

- finaliza la ejecución

## Software empleado

- [JFLAP](https://www.jflap.org).
- [WIRESHARK](https://www.wireshark.org/download.html). recomendado para ver el intercambio de paquetes

## Autores

- [@StoneySpring688](https://github.com/StoneySpring688)
- [@pabletel](https://github.com/pabletel)
