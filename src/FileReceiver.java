import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FileReceiver implements Runnable {

	private File subor;
	private int dlzkaSuboru;
        private Set<Integer> chybajuceOffsety;
        private InetAddress ip;
        private int port;
        private int poslednyZiadany=-1;
	
	public FileReceiver(File subor, Integer dlzka, InetAddress ip, int port) {
		this.subor=subor;
		this.dlzkaSuboru = dlzka;
                this.chybajuceOffsety=new HashSet<Integer>(this.dlzkaSuboru/500);
                for (int i = 0; i < dlzkaSuboru; i+=1000) {
                    this.chybajuceOffsety.add(i);
                }
                this.ip=ip;
                this.port=port;
	}

        public DatagramPacket chybajuce() throws IOException{
            byte[] missing = this.getChybajuce();
            return new DatagramPacket(missing,
                                    missing.length,
                                    this.ip,
                                    this.port);
        }

        public byte[] getChybajuce() throws IOException {

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(1008);
                    ObjectOutputStream oos = new ObjectOutputStream(baos);

                    oos.writeInt(Math.min(255, this.chybajuceOffsety.size()));
                    int i=0;
                    for (int offset : this.chybajuceOffsety) {
                        
                        i++;
                        oos.writeInt(offset);
                        this.poslednyZiadany=offset;
                        if(i==255){
                            break;
                        }
                    }
                    oos.flush();
                    oos.close();
                    return baos.toByteArray();
        }

	@Override
	public void run() {
		try {
			byte[] poleSuboru = new byte[dlzkaSuboru];
			DatagramSocket s = new DatagramSocket();
                        s.setSoTimeout(2000);

                        String ss = "chcem subor";
                        DatagramPacket ziadost = new DatagramPacket(ss.getBytes(), ss.getBytes().length, this.ip, this.port);
                        s.send(ziadost);

			DatagramPacket paket = new DatagramPacket(
					new byte[s.getReceiveBufferSize()] , s.getReceiveBufferSize());
			ExecutorService ex = Executors.newCachedThreadPool();

                        while(true){
                            while (true) {
                                try{
                                        s.receive(paket);
                                        byte[] sprava = paket.getData();
                                        ByteArrayInputStream bais = new ByteArrayInputStream(sprava);
                                        ObjectInputStream ois = new ObjectInputStream(bais);
                                        int dlzka = ois.readInt();
                                        int offset = ois.readInt();
                                        byte[] data = new byte[dlzka];
                                        ois.read(data);
                                        System.arraycopy(data, 0, poleSuboru, offset, dlzka);
                                        this.chybajuceOffsety.remove(offset);
                                        
                                        if(offset == this.poslednyZiadany){
//                                            System.out.println("mam vsetky co som chcel");
                                            System.out.println("Este treba "+this.chybajuceOffsety.size());
                                            break;
                                        }

                                    }catch (java.net.SocketTimeoutException e){
                                        System.out.println("Este treba "+this.chybajuceOffsety.size());
                                        break;

                                    }
                            }
                            s.send(this.chybajuce());
                            s.send(this.chybajuce());
                            s.send(this.chybajuce());

                            if (this.chybajuceOffsety.isEmpty()) {
                                FileOutputStream fos=new FileOutputStream(subor);
                                fos.write(poleSuboru);
                                fos.close();
                                System.out.println("Subor "+subor.getAbsolutePath()+" ulozeny");
                                return;
                            }
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}