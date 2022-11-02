package module.comms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4String;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import inpro.io.ListenerModule;

/**
 * A module under development for receiving ROS messages.
 * @author julian
 *
 */
public class ROSSubscriber extends ListenerModule {
	
	
	static Logger log = Logger.getLogger(ROSSubscriber.class.getName());
 	
 	@S4String(defaultValue = "")
	public final static String SCOPE = "scope";	 
 	
	
	
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		String scope = ps.getString(SCOPE);
		logger.info("Starting dummy ROSSubscriber on scope " + scope);
		BufferedReader br;
		String xmlInput = "";
		try {
			br = new BufferedReader(new FileReader("resources/sample-images/famula/XMLWorldBelief_2017-07-25_15-23-38.xml"));
			String input = "";
			String line = br.readLine();
			while (line != null) {
				// System.out.println(line);
				input += line;
				input += System.lineSeparator();
				line = br.readLine();
			}
			xmlInput = input.toString();
		} catch (Exception e) {
			logger.error("Error reading from file");
		}
		logger.info("Sending + " + xmlInput);
		System.out.println("Sending + " + xmlInput);
		this.process(xmlInput);
		
	}
	

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		// TODO Auto-generated method stub
		
	}

}
