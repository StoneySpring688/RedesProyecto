package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: Añadir aquí todas las constantes que definen los diferentes tipos de
	 * mensajes(values) del protocolo de comunicación con el directorio.
	 */
	
	//hay muchos mensajes  iguales, pero  los que van al servidor, por la forma de contruirlos es necesarios hacerlos, aunque puedan  ser iguales
	
	//operations
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_LOGIN = "login";
	public static final String OPERATION_ERROR = "error";
	public static final String OPERATION_CONFIRMATION = "confirmation";
	public static final String OPERATION_LOGOUT = "logout";
	public static final String OPERATION_USERLIST = "user_list";
	public static final String OPERATION_REGISTER_FILESERVER = "registerfileserver";
	public static final String OPERATION_LOOKUP_SERVADR = "lookupservadr";
	public static final String OPERATION_PUBLISH = "publish";
	public static final String OPERATION_FILELIST = "filelist";
	public static final String OPERATION_SEARCH = "search";
	public static final String OPERATION_STOPSERVER = "stopserver";
	public static final String OPERATION_DOWNLOADASKINFO = "downloadAskInfo";
	
	
	//codes
	public static final String OPERATION_LOGINFAILED = "login_failed";
	public static final String OPERATION_LOGOUTFAILED = "logout_failed";
	public static final String OPERATION_LISTFAILED = "list_failed";
	public static final String OPERATION_LOOKUPSERVADRFAILED = "lookupservadr_failed"; 
	public static final String OPERATION_LOGINOK = "loginok";
	public static final String OPERATION_LOGOUTOK = "logoutok";
	public static final String OPERATION_LISTOK = "listok";
	public static final String OPERATION_REGISTERFILESERVEROK = "registerfileserverok";
	public static final String OPERATION_LOOKUPSERVADROK = "lookupservadrok";
	public static final String OPERATION_PUBLISHOK = "publishok";
	public static final String OPERATION_PUBLISH_FAILED = "publish_failed";
	public static final String OPERATION_FILELISTOK = "filelistok";
	public static final String OPERATION_FILELIST_FAILED = "filelist_failed";
	public static final String OPERATION_SEARCHOK = "searchok";
	public static final String OPERATION_SEARCH_FAILED = "search_failed";
	public static final String OPERATION_STOPSERVEROK = "stopserverok";
	public static final String OPERATION_DOWNLOADASKINFOOK = "downloadAskInfook";





}
