import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JCheckBox;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;

public class UDPChatServer implements Runnable{

        private JTextArea txt;
        private JCheckBox soundEnabled;

        public UDPChatServer(JTextArea t, JCheckBox sound){
            this.txt=t;
            this.soundEnabled=sound;
        }

        public UDPChatServer(){

        }

	@Override
	public void run() {
		 try {

			DatagramSocket s = new DatagramSocket(7777);
			DatagramPacket paket = new DatagramPacket(
					new byte[s.getReceiveBufferSize()] , s.getReceiveBufferSize());
			ExecutorService ex = Executors.newCachedThreadPool();
			while(true){
				s.receive(paket);
				String prislo = new String(paket.getData()).trim();
				if(prislo.startsWith("@subor@")){
					//upozornenie na moznost tahania
					JFileChooser jfc = new JFileChooser();
					String[] casti = prislo.substring(7).split("@");
					jfc.setSelectedFile(new File(casti[0]));
					int stlacil = jfc.showDialog(null, "save");
					if(stlacil==JFileChooser.APPROVE_OPTION){
						File subor = jfc.getSelectedFile();
						ex.submit(new FileReceiver(subor, new Integer(casti[1]), paket.getAddress(), paket.getPort()));
					}
				}
                                if(txt!=null){
                                    txt.setText(txt.getText() + paket.getAddress() + ": " + prislo + "\n");
                                    txt.setCaretPosition(txt.getText().length());
                                    if(this.soundEnabled.isSelected()){
                                        ExecutorService es = Executors.newCachedThreadPool();
                                        es.submit(new Play());
                                    }
                                }

				paket = new DatagramPacket(
						new byte[s.getReceiveBufferSize()] , s.getReceiveBufferSize());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
