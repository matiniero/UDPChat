import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UDPChatUI {


	public static void main(String[] args) throws Exception {
		ExecutorService ex = Executors.newCachedThreadPool();
		ex.submit(new UDPChatServer());
		DatagramSocket s = new DatagramSocket();
		
		Scanner scanner = new Scanner(System.in);
		while(true){
			String sprava = scanner.nextLine();
			if(sprava.equals("subor")){
				ex.submit(new FileServer(InetAddress.getByName("127.0.0.1"),7777));
				continue;
			}
			DatagramPacket paket = new DatagramPacket(
					sprava.getBytes(), 
					sprava.getBytes().length, 
					InetAddress.getByName("127.0.0.1"),
					7777);
			s.send(paket);
		}
	}
}
