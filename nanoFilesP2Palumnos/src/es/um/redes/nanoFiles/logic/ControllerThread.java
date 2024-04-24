package es.um.redes.nanoFiles.logic;

public class ControllerThread extends Thread {
	
	NFControllerLogicDir contDir = null;
	NFControllerLogicP2P contPeer = null;
	
	public ControllerThread(NFControllerLogicDir nfld, NFControllerLogicP2P  nflp) {
		this.contDir = nfld;
		this.contPeer = nflp;
	}
	
	public void run() {
		if (this.contPeer.getFgStatus()) {
			contDir.registerFileServer(contPeer.getFgServerPort());
		}
	}
	
	
	
}
