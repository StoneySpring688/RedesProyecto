package es.um.redes.nanoFiles.tcp.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NFConnectorThread extends Thread{
	private String Hash;
	private long init;
	private long fin;
	private int id;
	NFConnector nfc;
	public static volatile int idAEscr  =  0;
	public static volatile int npeers;
	public static volatile File f;
	private static Lock l = new ReentrantLock();
	private static Condition c = l.newCondition();
	
	public NFConnectorThread(String  h, long i, long f, int id,InetSocketAddress isa) {
		this.Hash = h;
		this.init = i;
		this.fin = f;
		this.id = id;
		try {
			this.nfc = new NFConnector(isa);
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public void run() {
		try {
			byte[] data =this.nfc.downloadChunk(Hash, init, fin);
			l.lock();
			try {
				while(idAEscr != this.id) {
					c.await();
				}
				RandomAccessFile file = new RandomAccessFile(f, "rw");
				file.seek(this.init);
				file.write(data);
				file.close();
				idAEscr = (idAEscr + 1) % npeers;
				c.signalAll();
			}finally {
				l.unlock();
			}		
		}  catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
	}

}
