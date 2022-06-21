package module.vision;

import java.util.HashMap;

@SuppressWarnings("serial")
public class WorldBelief extends HashMap<String, HashMap<String, Float>> {

	public HashMap<String, String> xmlStrings;
	
	public WorldBelief(HashMap<String, String> xmlstrings) {
		super();
		this.xmlStrings = xmlstrings;
	}
	
	public WorldBelief() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getXMLStringForObj(String objID) {
		return xmlStrings.get(objID.replace("object", ""));
	}
	
	/**
	 * Returns the XML-text of the object
	 * @param objID
	 * @return
	 */
	public String getXMLStringForObj(int objID) {
		return xmlStrings.get(Integer.toString(objID));
	}
	
	// Returns the scores of the properties of the WorldBelief-HashMap,
	// but only If the demanded property exists in this "object"-HashMap
	public HashMap<String, Float> getObjectScoresForProperty(String propertyName){
		
		// Initialize the HashMap which will be returned
		HashMap<String, Float> objectScores = new HashMap<String, Float>();
		
		// Iterate through the objects in wb HashMap (object4; object0; ...)
		for (java.util.Map.Entry<String, HashMap<String, Float>> e : this.entrySet()){
			// Extract the ID of the current object
			String objectName = e.getKey();
			// Extract the "object"-HashMap from the "wb"-Hash-Map
			HashMap<String, Float> currentObject = new HashMap<String, Float>();
			currentObject= e.getValue();
			
			// Iterate through the current object HashMap (label_apple_prob; color_red_prob; ...)
			for (String k : currentObject.keySet()) {
				// Select the demanded key and its value
				if (k.equals(propertyName) == true) {
					// Add the objectName and the 
					objectScores.put(objectName, currentObject.get(k));
				}
				
			}	
		}
		return objectScores;
	}

}