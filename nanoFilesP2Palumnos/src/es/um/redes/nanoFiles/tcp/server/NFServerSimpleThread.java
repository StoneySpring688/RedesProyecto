package es.um.redes.nanoFiles.tcp.server;

import java.util.Scanner;

public class NFServerSimpleThread extends Thread{
	
	public Scanner scanner;
	public  static boolean continu3;
	public String stopcommand;
	
	public NFServerSimpleThread(String s) {
		this.scanner = new Scanner(System.in);
		continu3 = true;
		this.stopcommand = s;
	}
	
	public void run() {
		while(continu3) {
			String input = scanner.nextLine();
			if(input.matches(this.stopcommand)) {
				continu3 = false;
			}
			
		}
	}
	
}
