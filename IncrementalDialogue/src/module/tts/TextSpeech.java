package module.tts;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

/**
 * @author Julian Hough
 *
 */
public class TextSpeech {

    protected Logger logger;
	private String voiceName;
	private Boolean speaking;
	private VoiceManager voiceManager;
    private Voice Talkvoice;
	
	
	public void initVoice() {
		logger.info("Using voice: " + voiceName);
		

		voiceManager = VoiceManager.getInstance();
		logger.debug("Voices:");
		Voice[] voices = voiceManager.getVoices();
        for (int v = 0; v<voices.length; v++){
        	logger.debug(voices[v].getName());
        }
        //System.out.println(voices[0].DATABASE_NAME);
		
		
        Talkvoice = voiceManager.getVoice(voiceName);

        if (Talkvoice == null)
        {
            System.err.println(
                "Cannot find a voice named "
                + voiceName + ".  Please specify a different voice.");
            System.exit(1);
        }

        /* Allocates the resources for the voice.
         */

        Talkvoice.allocate();
        /* Synthesize speech.
         */

        Talkvoice.setPitchShift(1.0f);
        Talkvoice.setRate(150.0f);
        Talkvoice.setPitch((float) 120.0);
        Talkvoice.setPitchRange((float) 12.0);
        Talkvoice.setVolume((float) 0.0);
		
	}

	public TextSpeech() {
		logger = Logger.getLogger(this.getClass());
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		//System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.AlanVoiceDirectory");
		
		//System.setProperty("mbrola.base", "de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory");
	
		voiceName = "kevin16";
		speaking = false;
		this.initVoice();
	
	}
		    
	public void read(String text) throws IOException{
		

        //while (speaking == true){
        //	//do nothing
        //}
		logger.info("Reading out:" + text);
        this.speaking = true;
        
        String command[] = {"say", text};
        Runtime.getRuntime().exec(command);
        
        Talkvoice.speak(text);
        this.speaking = false;
	   
	
	}
	
	public void deallocate() {

        /* Clean up and leave.
         */
        Talkvoice.deallocate();
	}
	
	public boolean speaking(){
		return speaking;
	}
		    
	public static void main(String args[]) throws IOException{
		
		VoiceManager voiceManager = VoiceManager.getInstance();
		//Voice[] voices = voiceManager.getVoices();
		//for (int i = 0; i < voices.length; i++) {
		//	logger.debug("    " + voices[i].getName() + " (" + voices[i].getDomain() + " domain)");
		//}
		
		String commandArray[] = {"say", "hello"};
		 
		
			TextSpeech reader = new TextSpeech();
			reader.read("hello, what is your name?");
			reader.read("uh hmm?");
			reader.read("yes?");
			reader.read("very good!");
			reader.read("bye!");
			reader.deallocate();
			
	}

}