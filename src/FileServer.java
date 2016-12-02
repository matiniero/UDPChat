import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;


public class FileServer implements Runnable {

	InetAddress adresa;
	int port;
	
	public FileServer(InetAddress adresa, int port){
		this.adresa=adresa;
		this.port=port;
	}
	
	public void run() {
		try {
			JFileChooser jfc = new JFileChooser();
			int akcia = jfc.showDialog(null, "Send");
			if(akcia==JFileChooser.APPROVE_OPTION){
				File subor = jfc.getSelectedFile();
				String sprava = "@subor@"+subor.getName()+"@"+subor.length();
				DatagramSocket s = new DatagramSocket();
				DatagramPacket paket = new DatagramPacket(sprava.getBytes(), 
                                                                          sprava.getBytes().length,
                                                                          adresa,
                                                                          port);
				s.send(paket);
				
				paket = new DatagramPacket(
						new byte[s.getReceiveBufferSize()] , s.getReceiveBufferSize());
				ExecutorService ex = Executors.newCachedThreadPool();
				while(true){
					s.receive(paket);
					String prislo = new String(paket.getData()).trim();
					if(prislo.equals("chcem subor")){
						ex.submit(new FileSender(subor,paket.getAddress(),paket.getPort()));
					}
					System.out.println(paket.getAddress() + ": " + prislo);
					paket = new DatagramPacket(
							new byte[s.getReceiveBufferSize()] , s.getReceiveBufferSize());
				}
//				s.close();
			}
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
