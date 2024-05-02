package es.um.redes.nanoFiles.logic;

import java.io.IOException;
import java.net.InetSocketAddress;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.shell.NFCommands;
import es.um.redes.nanoFiles.shell.NFShell;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFController {
	/**
	 * Diferentes estados del cliente de acuerdo con el autómata
	 */
	private static final byte LOGGED_OUT = 0;
	private static final byte LOGGED_IN = 1;
	private static final byte IS_SERVER = 2;

	/**
	 * Shell para leer comandos de usuario de la entrada estándar
	 */
	private NFShell shell;
	/**
	 * Último comando proporcionado por el usuario
	 */
	private byte currentCommand;

	/**
	 * Objeto controlador encargado de la comunicación con el directorio
	 */
	private NFControllerLogicDir controllerDir;
	/**
	 * Objeto controlador encargado de la comunicación con otros peers (como
	 * servidor o cliente)
	 */
	private NFControllerLogicP2P controllerPeer;

	/**
	 * El estado en que se encuentra este peer (según el autómata). El estado debe
	 * actualizarse cuando se produce un evento (comando) que supone un cambio en el
	 * autómata.
	 */
	private byte currentState;
	/**
	 * Atributos donde se establecen los argumentos pasados a los distintos comandos
	 * del shell. Estos atributos se establecen automáticamente según la orden y se
	 * deben usar para pasar los valores de los parámetros a las funciones invocadas
	 * desde este controlador.
	 */
	private String nickname; // Nick del usuario (register)
	private String directory; // Nombre/IP del host donde está el directorio (login)
	private String downloadTargetFileHash; // Hash del fichero a descargar (download)
	private String downloadLocalFileName; // Nombre con el que se guardará el fichero descargado
	private String downloadTargetServer; // nombre o IP:puerto del sevidor del que se descargará el fichero

	// Constructor
	public NFController() {
		shell = new NFShell();
		controllerDir = new NFControllerLogicDir();
		controllerPeer = new NFControllerLogicP2P();
		// Estado inicial del autómata
		currentState = LOGGED_OUT;
	}

	/**
	 * Método que procesa los comandos introducidos por un usuario. Se encarga
	 * principalmente de invocar los métodos adecuados de NFControllerLogicDir y
	 * NFControllerLogicP2P según el comando.
	 */
	public void processCommand() {

		if (!canProcessCommandInCurrentState()) {
			return;
		}		
		
		boolean commandSucceeded = false;
		switch (currentCommand) {
		case NFCommands.COM_MYFILES:
			showMyLocalFiles(); // Muestra los ficheros en el directorio local compartido
			break;
		case NFCommands.COM_LOGIN:
			if (NanoFiles.testMode) {
				try {
					controllerDir.testCommunicationWithDirectory(directory);
					return;
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("[testMode] An error occurred, failed to communicate with directory");
					System.exit(-1);
				}
			}
			
			commandSucceeded = controllerDir.doLogin(directory, nickname);
			break;
		case NFCommands.COM_LOGOUT:
			
			commandSucceeded = controllerDir.doLogout();
			break;
		case NFCommands.COM_USERLIST:
			
			commandSucceeded = controllerDir.getAndPrintUserList();
			break;
		case NFCommands.COM_FILELIST:
			
			commandSucceeded = controllerDir.getAndPrintFileList();
			break;
		case NFCommands.COM_FGSERVE:
			
			controllerPeer.foregroundServeFiles(this.controllerDir,this.controllerPeer);
			commandSucceeded = true; // si devuelve la terminal porque se ha desbloqueado, ha sido  un exito
			
			break;
		case NFCommands.COM_PUBLISH:
			
			commandSucceeded = controllerDir.publishLocalFiles();
			break;
		case NFCommands.COM_BGSERVE:
			
			boolean serverRunning = controllerPeer.backgroundServeFiles();
			if (serverRunning) {
				commandSucceeded = controllerDir.registerFileServer(controllerPeer.getServerPort());
			}
			
			break;
		case NFCommands.COM_STOP_SERVER:
			
			controllerPeer.stopBackgroundFileServer();
			commandSucceeded = controllerDir.unregisterFileServer();
			break;
		case NFCommands.COM_DOWNLOADFROM:
																																
			InetSocketAddress serverAddr = controllerDir.getServerAddress(downloadTargetServer);
			commandSucceeded = controllerPeer.downloadFileFromSingleServer(serverAddr, downloadTargetFileHash, downloadLocalFileName);
			break;
		case NFCommands.COM_SEARCH:
			
			commandSucceeded = controllerDir.getAndPrintServersNicknamesSharingThisFile(downloadTargetFileHash);
			break;
		case NFCommands.COM_DOWNLOAD:
			
			DirMessage serverAddressList = controllerDir.getServerAddressesSharingThisFile(downloadTargetFileHash);
			commandSucceeded = controllerPeer.downloadFileFromMultipleServers(serverAddressList, downloadTargetFileHash,downloadLocalFileName);
			break;
		case NFCommands.COM_QUIT:
		default:
		}

		updateCurrentState(commandSucceeded);
	}

	/**
	 * Método que comprueba si se puede procesar un comando introducidos por un
	 * usuario, en función del estado del autómata en el que nos encontramos.
	 */
	public boolean canProcessCommandInCurrentState() {
		
		boolean commandAllowed = true;
		switch (currentCommand) {
		case NFCommands.COM_MYFILES: {
			commandAllowed = true;
			break;
		}
		case NFCommands.COM_LOGIN:
			if (currentState != LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("[warning] you must logout first");
			}
			break;
		case NFCommands.COM_LOGOUT : {
			if(currentState == IS_SERVER) {
				commandAllowed = false;
				System.err.println("[warning] stop the server first");
			}else if(currentState != LOGGED_IN) {
				commandAllowed = false;
				System.err.println("[warning] you must login first");
			}
			break;
		}
		case NFCommands.COM_BGSERVE : {
			if(currentState != LOGGED_IN) {
				commandAllowed = false;
				System.err.println("[warning] you must login first");
			}
			break;
		}
		case NFCommands.COM_FGSERVE : {
			if(currentState != LOGGED_IN) {
				commandAllowed = false;
				System.err.println("[warning] you must login first");
			}
			break;
		}
		case NFCommands.COM_STOP_SERVER : {
			if(currentState != IS_SERVER) {
				commandAllowed = false;
				System.err.println("[warning] you must run a server first");
			}
			break;
		}
		case NFCommands.COM_PUBLISH : {
			if(currentState != IS_SERVER) {
				commandAllowed = false;
				System.err.println("[warning] you must run a server first");
			}
			break;
		}
		case NFCommands.COM_FILELIST : {
			if(currentState != LOGGED_IN && currentState != IS_SERVER) {
				commandAllowed =  false;
				System.err.println("[warning] you must login first");
			}
			break;
		}
		case NFCommands.COM_SEARCH : {
			if(currentState != LOGGED_IN && currentState != IS_SERVER) {
				commandAllowed =  false;
				System.err.println("[warning] you must login first");
			}
			break;
		}
		case NFCommands.COM_USERLIST : {
			if(currentState != LOGGED_IN && currentState != IS_SERVER) {
				commandAllowed =  false;
				System.err.println("[warning] you must login first");
			}
			break;
		}
		case NFCommands.COM_DOWNLOAD : {
			if(currentState != LOGGED_IN && currentState != IS_SERVER) {
				commandAllowed =  false;
				System.err.println("[warning] you must login first");
			}
			break;
		}
		case NFCommands.COM_DOWNLOADFROM : {
			if(currentState != LOGGED_IN && currentState != IS_SERVER) {
				commandAllowed =  false;
				System.err.println("[warning] you must login first");
			}
			break;
		}
		case  NFCommands.COM_QUIT : {
			if(currentState == IS_SERVER) {
				commandAllowed = false;
				System.err.println("[warning] stop the server first");
			}else if (currentState != LOGGED_OUT) {
				commandAllowed = false;
				System.err.println("[warning] you must logout first");
			}
			break;
		}


		default:
			System.err.println("ERROR: undefined behaviour for " + currentCommand + "command!");
		}
		return commandAllowed;
	}

	private void updateCurrentState(boolean success) {
		
		if (!success) {
			return;
		}
		switch (currentCommand) {
		case NFCommands.COM_LOGIN: {
			currentState = LOGGED_IN;
			break;
		}
		case NFCommands.COM_LOGOUT: {
			currentState = LOGGED_OUT;
			break;
		}
		case NFCommands.COM_BGSERVE : {
			currentState = IS_SERVER;
			break;
		}
		case NFCommands.COM_FGSERVE : {
			currentState = LOGGED_IN; //como ya ha dejado de ser un servidor el estado pasa a ser logged_in, el stopserver de fgserve se gestiona en un hilo
			break;
		}
		case NFCommands.COM_STOP_SERVER : {
			currentState = LOGGED_IN;
			break;
		}



		default:
		}

	}

	private void showMyLocalFiles() {
		System.out.println("List of files in local folder:");
		FileInfo.printToSysout(NanoFiles.db.getFiles());
	}

	/**
	 * Método que comprueba si el usuario ha introducido el comando para salir de la
	 * aplicación
	 */
	public boolean shouldQuit() {
		return currentCommand == NFCommands.COM_QUIT && currentState == LOGGED_OUT;
	}

	/**
	 * Establece el comando actual
	 * 
	 * @param command el comando tecleado en el shell
	 */
	private void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	/**
	 * Registra en atributos internos los posibles parámetros del comando tecleado
	 * por el usuario.
	 */
	private void setCurrentCommandArguments(String[] args) {
		switch (currentCommand) {
		case NFCommands.COM_LOGIN:
			directory = args[0];
			nickname = args[1];
			break;
		case NFCommands.COM_SEARCH:
			downloadTargetFileHash = args[0];
			break;
		case NFCommands.COM_DOWNLOADFROM:
			downloadTargetServer = args[0];
			downloadTargetFileHash = args[1];
			downloadLocalFileName = args[2];
			break;
		case NFCommands.COM_DOWNLOAD:
			downloadTargetFileHash = args[0];
			downloadLocalFileName = args[1];
			break;
		default:
		}
	}

	/**
	 * Método para leer un comando general
	 */
	public void readGeneralCommandFromShell() {
		// Pedimos el comando al shell
		shell.readGeneralCommand();
		// Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		// Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

}
