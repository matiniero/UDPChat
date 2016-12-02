import java.awt.HeadlessException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

import javax.swing.JFileChooser;


public class MyFirstOwnFileSender implements Runnable {

	@Override
	public void run() {
		try {
			JFileChooser jfc = new JFileChooser();
			int akcia = jfc.showDialog(null, "Send");
			if(akcia==JFileChooser.APPROVE_OPTION){
				File subor = jfc.getSelectedFile();
				RandomAccessFile raf = new RandomAccessFile(subor, "r");
//				raf.seek(1000);
				byte[] pole = new byte[1000];
				int dlzka = raf.read(pole);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(1008);
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeInt(dlzka);
				oos.writeInt(0); //cislo offsetu
				oos.write(pole);
				oos.flush();
				oos.close();
				byte[] sprava = baos.toByteArray();
			}		
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
