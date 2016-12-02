import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Play implements Runnable{
   
   public static void playAudioFile( String fileName ) {
      File soundFile = new File( fileName );

      try {
         
         AudioInputStream audioInputStream = AudioSystem.getAudioInputStream( soundFile );
         
         playAudioStream( audioInputStream );
      } catch ( Exception e ) {
         System.out.println( "Problem with highlight" + fileName + ":" );
         e.printStackTrace();
      }
   }

   public static void playAudioStream( AudioInputStream audioInputStream ) {

      AudioFormat audioFormat = audioInputStream.getFormat();

      DataLine.Info info = new DataLine.Info( SourceDataLine.class, audioFormat );
      if ( !AudioSystem.isLineSupported( info ) ) {
         System.out.println( "Play.playAudioStream does not handle this type of audio on this system." );
         return;
      }

      try {

         SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine( info );

         dataLine.open( audioFormat );

         if( dataLine.isControlSupported( FloatControl.Type.MASTER_GAIN ) ) {
            FloatControl volume = (FloatControl) dataLine.getControl( FloatControl.Type.MASTER_GAIN );
            volume.setValue( 100.0F );
         }

         dataLine.start();

         int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
         byte [] buffer = new byte[ bufferSize ];

         try {
            int bytesRead = 0;
            while ( bytesRead >= 0 ) {
               bytesRead = audioInputStream.read( buffer, 0, buffer.length );
               if ( bytesRead >= 0 ) {
                  int framesWritten = dataLine.write( buffer, 0, bytesRead );
               }
            } 
         } catch ( IOException e ) {
            e.printStackTrace();
         }
         dataLine.drain();
         dataLine.close();
      } catch ( LineUnavailableException e ) {
         e.printStackTrace();
      }
   }

    @Override
    public void run() {
        playAudioFile("./sound/beep-7.wav");
    }
}