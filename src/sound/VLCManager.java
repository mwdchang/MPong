package sound;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;

////////////////////////////////////////////////////////////////////////////////
// SoundManager based on the VLC player runtime
////////////////////////////////////////////////////////////////////////////////
public class VLCManager extends SoundManager {

   @Override
   public void init() {
      new NativeDiscovery().discover();
      Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
      
      factory = new MediaPlayerFactory();
      mediaPlayer = factory.newHeadlessMediaPlayer();
      mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
        public void finished(MediaPlayer mediaPlayer) {
          //System.exit(0);
        }
        public void error(MediaPlayer mediaPlayer) {
          //System.exit(1);
        }
      });
  }

   
   
   @Override
   public void play(String mediaPath) throws Exception {
      mediaPlayer.playMedia( mediaPath );
      //Thread.currentThread().join();          
   }

   
   public MediaPlayerFactory factory = null;
   public MediaPlayer mediaPlayer = null;
   
   
}
