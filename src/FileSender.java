import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class FileSender implements Runnable {

	private File subor;
	private InetAddress address;
	private int port;
	private DatagramSocket s;
	private RandomAccessFile raf;
	
	
	public FileSender(File subor, InetAddress address, int port) {
		this.subor=subor;
		this.address = address;
		this.port=port;
	}

	private void posli(int offset) throws IOException{
		byte[] pole = new byte[1000];
		raf.seek(offset);
		int dlzka = raf.read(pole);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1008);
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeInt(dlzka);
		oos.writeInt(offset); //cislo offsetu
		oos.write(pole);
		oos.flush();
		oos.close();
		byte[] sprava = baos.toByteArray();
		DatagramPacket paket = new DatagramPacket(sprava, sprava.length, address, port);
		s.send(paket);
	}
	
	@Override
	public void run() {
		try {
			raf = new RandomAccessFile(subor, "r");
			s = new DatagramSocket();
			s.setSoTimeout(30000);

			for (int offset = 0; offset < subor.length(); offset+=1000) {
				posli(offset);
			}

			while(true){
                            try{
				DatagramPacket paket = new DatagramPacket(
						new byte[s.getReceiveBufferSize()] , s.getReceiveBufferSize());
				s.receive(paket);
				byte[] sprava = paket.getData();
				ByteArrayInputStream bais=new ByteArrayInputStream(sprava);
				ObjectInputStream ois = new ObjectInputStream(bais);
				int pocet = ois.readInt();
//                                System.out.println("");
				if(pocet==0){
					break;
				}
				for (int i = 0; i < pocet; i++) {
					int offset = ois.readInt();
					posli(offset);
				}
                            }catch (java.net.SocketTimeoutException e) {
                                System.out.println("Server: Ziadne dalsie poziadavky na pakety");
                            }
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			s.close();
		}
	}

}
