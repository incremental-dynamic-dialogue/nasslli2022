package module.tts;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import iu.DialogueActIU;


public class Synthesizer extends IUModule {
	
	TextSpeech tts;
	
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		logger.info("started synthesizer");
		tts = new TextSpeech();
	}
	
	private void processDialogueActIU(DialogueActIU iu) throws IOException {
		// simply goes word by word through content and vocalises
		String fullspeech = ((DialogueActIU) iu).getWords();
		//for (String word : (fullspeech.split(" ")) {
		//	tts.read(word);
		//}
		tts.read(fullspeech);
		
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		// TODO Auto-generated method stub
		for (EditMessage<? extends IU> edit: edits) {
			IU iu = edit.getIU();
			if (iu instanceof DialogueActIU) {
	
				try {
					this.processDialogueActIU((DialogueActIU) iu);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
	}

}
