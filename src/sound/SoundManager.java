package sound;

////////////////////////////////////////////////////////////////////////////////
// A contract for handling high-level sound management
// TODO: Should probably support media caching
// 
// init : Initialize the sound drivers and whatnots
// play : Play the media file
// 
////////////////////////////////////////////////////////////////////////////////
public abstract class SoundManager {
   public abstract void init();
   public abstract void play(String mediaPath) throws Exception;
   
   
}
