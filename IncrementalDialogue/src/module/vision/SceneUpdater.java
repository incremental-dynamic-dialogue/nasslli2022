package module.vision;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import iu.SceneIU;

import org.xml.sax.SAXException;

import sium.nlu.context.Context;
import sium.nlu.context.Entity;
import sium.nlu.context.Property;
import module.vision.WorldBelief;
import module.vision.WorldBeliefParser;
import module.vision.XmlContextUtil;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Integer;
import edu.cmu.sphinx.util.props.S4String;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import inpro.io.SensorIU;

/**
 * A module which takes SensorIU inputs with an XML-based payload which represents
 * a scene of objects.
 *
 * It doesn't create IUs for internal consumption
 * (and so is a leaf node in the IU module topology),
 * but acts as a mutually accessible object to other
 * modules.
 * 
 * It can create SensorIUs to be sent back to the vision system if needs be.
 * 
 * @author jhough
 * @author casey
 *
 */
public class SceneUpdater extends IUModule {
	
	@S4String(defaultValue = "")
	public final static String SCENE_REPRESENTATION_FORMAT = "scene_representation_format";
	
	@S4Integer(defaultValue = -1)
	public final static String TOP_LEFT_X = "top_left_x";
	
	@S4Integer(defaultValue = -1)
	public final static String TOP_LEFT_Y = "top_left_y";
	
	@S4Integer(defaultValue = -1)
	public final static String BOTTOM_RIGHT_X = "bottom_right_x";
	
	@S4Integer(defaultValue = -1)
	public final static String BOTTOM_RIGHT_Y = "bottom_right_y";
	
	@S4Integer(defaultValue = -1)
	public final static String TIME_OUT = "timeout";
	
	@S4Integer(defaultValue = -1)
	public final static String NUMBER_OF_OBJECTS = "number_of_objects"; //TODO only for games with fixed numbers of pieces

	
	private Context<String,String> scene;
	private WorldBelief worldBelief;
	private int timeOut;
	private Thread timeOutThread;
	private boolean updating;
	private int numberOfObjects;
	private Integer [] bottomRightCornerXY;
	private Integer [] topLeftCornerXY;
	private String sceneRepresentationFormat;
	
	@Override
	public void newProperties(PropertySheet ps) {
		super.newProperties(ps);
		sceneRepresentationFormat = ps.getString(SCENE_REPRESENTATION_FORMAT);
		numberOfObjects = ps.getInt(NUMBER_OF_OBJECTS);
		timeOut = ps.getInt(TIME_OUT);
		bottomRightCornerXY = new Integer[2];
		bottomRightCornerXY[0] = ps.getInt(BOTTOM_RIGHT_X);
		bottomRightCornerXY[1] = ps.getInt(BOTTOM_RIGHT_Y);
		topLeftCornerXY = new Integer[2];
		topLeftCornerXY[0] = ps.getInt(TOP_LEFT_X);
		topLeftCornerXY[1] = ps.getInt(TOP_LEFT_Y);
		scene = new Context<String,String>();
		worldBelief = null;
		updating = true;
	}
	
	public Integer[] getBottomRightCornerOfScene() {
		return bottomRightCornerXY; 
	}
	
	public Integer[] getTopLeftCornerOfScene() {
		return topLeftCornerXY; 
	}
	
	public Integer[] getCentreOfTopOfScene() {
		int length = this.getBottomRightCornerOfScene()[0] - this.getTopLeftCornerOfScene()[0];
		Integer [] i = { this.getTopLeftCornerOfScene()[0] + (length/2), this.getTopLeftCornerOfScene()[1] }; 
		return i;
	}
	
	public void resetScene(){
		scene = new Context<String,String>();
		SensorIU sensor_iu = new SensorIU("<reset>","/inprotk/rr/");
		this.rightBuffer.addToBuffer(sensor_iu); //also resets the scene to get all new object. Not a traditional left-buffer triggered update.
	}
	
	public String getObjectCentroidXY(String ID){
		String x = "";
		String y = "";
		ID = ID.replace("object", "");
		if (scene.containsEntity(ID)) {
			for (Property<String> p : scene.getPropertiesForEntity(ID)){
				String[] propstring = p.getProperty().toString().split(":");
				if (propstring[0].equals("x"))
					x = propstring[1];
				if (propstring[0].equals("y"))
					y = propstring[1];
					
			}
		} else {
			logger.warn("No object in scene with ID: " + ID);
			return null;
		}
		logger.debug("x: " + x  + ", y: "+y);
		return x.toString()+":"+y.toString();
	}
	
	public Map<String,String> getObjectPositions(){
		Map<String,String> map = new HashMap<String,String>();
		for (Entity<String> e : scene.getEntities()){
			map.put(e.toString(), getObjectCentroidXY(e.toString()));
		}
		return map;
			
	}

	
	public boolean isUpdating(){
		return this.updating;
	}

	public void setUpdating(boolean updating) {
		this.updating = updating;
	}

	private void checkTimeout() {
		
		if (timeOutThread != null) 
			timeOutThread.interrupt();
//		create a timeout ... if nothing happens after <timeout> seconds, then reset the context
		if (timeOut != -1) {
			timeOutThread = new Thread(new Runnable() {
			    public void run() {
			        try {
						Thread.sleep(timeOut * 1000);
					} 
			        catch (InterruptedException e) {
//			        	if something happened within the timeout, then just return (i.e., don't reset the context)
			        	return;
					}
			        resetScene();
			    }
			});
			timeOutThread.start();
		}		
	}
	
	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius,
			List<? extends EditMessage<? extends IU>> edits) {
		
		List<EditMessage<? extends IU>> newEdits = new ArrayList<EditMessage<? extends IU>>();
		for (EditMessage<? extends IU> edit : edits) {
			IU iu = edit.getIU();
			if (iu instanceof SensorIU){
				switch (edit.getType()) {
				case ADD:
					try {
						//if (edit.getIU().toPayLoad().equals("<reset>")) {
						//	logger.debug("RESOLUTION RESET!");
						//	resolution.reset();
						//	continue;
						//}
						if (updating){
							logger.debug("getting scene info");
							logger.debug(edit.getIU().toPayLoad());
							SceneIU newIU = null;
							WorldBelief wb = null;
							switch (this.sceneRepresentationFormat){
								case("OPENCV_DSG"):
									// updates objects one by one
								    XmlContextUtil.updateContextWorldBeliefStyle(edit.getIU().toPayLoad(), scene);
								    wb = new WorldBeliefParser().convertWorldBeliefFromContext(scene);
								    worldBelief = wb;
									newIU = new SceneIU(worldBelief);
									newEdits.add(new EditMessage<SceneIU>(EditType.ADD, newIU));
									break;
								case ("FAMULA_VISION_SFB"):
									// updates all objects in one go
									scene = null;
									wb = new WorldBeliefParser().parseWorldBeliefFromXML(edit.getIU().toPayLoad());
									worldBelief = wb;
									newIU = new SceneIU(worldBelief);
									newEdits.add(new EditMessage<SceneIU>(EditType.ADD, newIU));
									break;
								default:
									break;
								
							}
						
							
							//resolution.setContext(scene);
							
							if (scene.getEntities().size()>=this.numberOfObjects){
								updating = false; //TODO this just freezes WAC's context at the start
								logger.debug("Got maximal number of objects in scene.");
							}
							
							
							
							//checkTimeout();
						}
						
						
					} 
					catch (ParserConfigurationException e) {
						logger.error("XML parser error.");
						e.printStackTrace();
					} 
					catch (SAXException e) {
						logger.error("SAX parser error.");
						e.printStackTrace();
					} 
					catch (IOException e) {
						logger.error("IO error.");
						e.printStackTrace();
					} catch (Exception e) {
						logger.error("World Belief parsing error");
						e.printStackTrace();
					}
					break;
				case COMMIT:
					break;
				case REVOKE:
					break;
				default:
					break;
				
				}
			}
		}
		
		rightBuffer.setBuffer(newEdits);
	}


}
