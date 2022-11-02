package module.vision;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sium.nlu.context.Context;
import sium.nlu.context.Entity;
import sium.nlu.context.Property;

public class WorldBeliefParser {

	public WorldBelief parseWorldBeliefFromXML(String xml) throws Exception {

		// Store the xml text of every single object in a HashMap
		String[] lines = xml.split(System.lineSeparator());
		HashMap<String, String> objectTexts = new HashMap<String, String>();
		int count = 0;
		boolean start = false;
		boolean objectStart = false;
		String xmlObjectText = "";
		// Iterate through all the lines
		for (String line : lines) {
			// Start storing the text when the first object begins
			if (line.contains("<object ")) {
				objectStart = true;
			}
			if (objectStart == true) {
				xmlObjectText += line;
				xmlObjectText += System.lineSeparator();
				// If one object is finished, store the text in the HashMap
				// and give every object an ID
				if (line.contains("</object>") & start) {
					objectTexts.put(Integer.toString(count), xmlObjectText);
					xmlObjectText = "";
					count++;
				}
				start = true;
			}
		}

		// Initialize the WorldBelief HashMap
		WorldBelief wb = new WorldBelief(objectTexts);

		// Parse the XML input
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		// Get the input as a string
		Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
		doc.getDocumentElement().normalize();

		// Select all "object" nodes
		NodeList objectList = doc.getElementsByTagName("object");
		for (int i = 0; i < objectList.getLength(); i++) {
			Node objectNode = objectList.item(i);
			String objectID = objectNode.getNodeName() + i;
			// Create a new HashMap for the current object
			HashMap<String, Float> object = new HashMap<String, Float>();
			// Get the names and values of the attributes of "object" and add
			// them to the HashMap "object"
			if (objectNode.getNodeType() == Node.ELEMENT_NODE) {
				Element objectElement = (Element) objectNode;
				NamedNodeMap objectAttributeList = objectElement.getAttributes();
				for (int a = 0; a < objectAttributeList.getLength(); a++) {
					Node objectAttribute = objectAttributeList.item(a);
					// Convert the names and values into suitable keys and
					// datatypes
					String objectAttributeName = objectAttribute.getNodeName();
					float objectAttributeValue = Float.parseFloat(objectAttribute.getNodeValue());
					// Add the key and the (float) value to the HashMap "object"
					object.put(objectAttributeName, objectAttributeValue);
				}

				// Select all child nodes of "object"
				NodeList objectChildList = objectElement.getChildNodes();
				for (int j = 0; j < objectChildList.getLength(); j++) {
					Node objectChildNode = objectChildList.item(j);

					// If it only has an attribute of the child node: Get the
					// names and
					// values and add them to the HashMap "object"
					if (objectChildNode.hasChildNodes() == false
							&& objectChildNode.getNodeType() == Node.ELEMENT_NODE) {
						Element objectChildElement = (Element) objectChildNode;
						NamedNodeMap objectChildAttributeList = objectChildElement.getAttributes();
						for (int a = 0; a < objectChildAttributeList.getLength(); a++) {
							Node objectChildAttribute = objectChildAttributeList.item(a);
							// Convert the names and values into suitable keys
							// and datatypes
							String objectChildAttributeName = objectChildElement.getNodeName() + "_"
									+ objectChildAttribute.getNodeName();
							float objectChildAttributeValue = Float.parseFloat(objectChildAttribute.getNodeValue());
							// Add the key and the (float) value to the HashMap
							// "object"
							object.put(objectChildAttributeName, objectChildAttributeValue);
						}
					}

					// If it contains gradchild nodes of "object": Select it and
					// get name and value of its attributes. Then add them to
					// the HashMap "object"
					if (objectChildNode.hasChildNodes() == true && objectChildNode.getNodeType() == Node.ELEMENT_NODE) {
						Element objectChildElement = (Element) objectChildNode;
						NodeList objectGrandChildList = objectChildElement.getChildNodes();
						// Select all grandchild nodes
						for (int k = 0; k < objectGrandChildList.getLength(); k++) {
							Node objectGrandChildNode = objectGrandChildList.item(k);
							if (objectGrandChildNode.getNodeType() == Node.ELEMENT_NODE) {
								Element objectGrandChildElement = (Element) objectGrandChildNode;
								NamedNodeMap objectGrandChildAttributeList = objectGrandChildElement.getAttributes();
								// Get names and values of the attributes
								for (int a = 0; a < objectGrandChildAttributeList.getLength(); a++) {
									Node objectGrandChildAttribute = objectGrandChildAttributeList.item(a);
									// Convert the names and values into
									// suitable keys and datatypes
									String objGrandChildAttributeName = objectChildElement.getNodeName() + "_"
											+ objectGrandChildElement.getNodeName() + "_"
											+ objectGrandChildAttribute.getNodeName();
									float objectGrandChildAttributeValue = Float
											.parseFloat(objectGrandChildAttribute.getNodeValue());
									// Add the key and the (float) value to the
									// HashMap "object"
									object.put(objGrandChildAttributeName, objectGrandChildAttributeValue);
								}
							}
						}
					}
				}
			}
			// Add the "object"- HashMap of the current object "i" to the
			// WorldBelief-HashMap
			wb.put(objectID, object);
		}
		return wb;
	}
	
	public WorldBelief convertWorldBeliefFromContext(Context<String, String> scene){
		
		WorldBelief wb = new WorldBelief();
		for (Entity<String> ID : scene.getEntities()){
			String stringID = "object" + ID.toString();
			HashMap<String, Float> object = new HashMap<String, Float>();
			for (Property<String> p : scene.getPropertiesForEntity(ID.toString())){
				//System.out.println(p.getProperty().toString());
				String[] propstring = p.getProperty().toString().split(":");
				//System.out.println(propstring[0]);
				//System.out.println(propstring[1]);
				float value = 0;
				try {
					value = Float.valueOf(propstring[1]);
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}
				object.put(propstring[0], value);
				
			}
			wb.put(stringID, object);
		}
		return wb;
		
	}

	public static void main(String[] args) throws Exception {
		// Initialize new WorldBeliefParser
		WorldBeliefParser parser = new WorldBeliefParser();
		// Get the XML input from a file
		BufferedReader br = new BufferedReader(new FileReader("resources/sample-images/famula/XMLWorldBelief_2017-07-25_15-23-38.xml"));
		List<String> objectTexts = new ArrayList<String>();
		try {
			String input = "";
			String line = br.readLine();
			while (line != null) {
				// System.out.println(line);
				input += line;
				input += System.lineSeparator();
				line = br.readLine();
			}
			String xmlInput = input.toString();
			// Parse the xmlInput
			WorldBelief wb = parser.parseWorldBeliefFromXML(xmlInput);
			// Print the wb
			/*
			 * for (String k : wb.keySet()){ System.out.println(k);
			 * System.out.println(wb.get(k)); }
			 */

			// Get the scores for a property.
			// Only objects, which have this property, are part of the HashMap.
			System.out.println(wb.get("object0"));
			System.out.println(wb.getObjectScoresForProperty("color_red_prob"));
			System.out.println(wb.getObjectScoresForProperty("label_apple_prob"));
			System.out.println(wb.getObjectScoresForProperty("elongated_short_prob").get("object0"));
			// Get the xml-text of the object ID
			System.out.println(wb.getXMLStringForObj(0));

		} finally {
			br.close();
		}

	}
}