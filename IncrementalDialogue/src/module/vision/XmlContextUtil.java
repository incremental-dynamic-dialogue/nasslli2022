package module.vision;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sium.nlu.context.Context;

public class XmlContextUtil {

	public static void updateContext(String xmlString, Context<String,String> scene) throws ParserConfigurationException, SAXException, IOException {
		
		synchronized (scene) { 
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		    Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
		    
		    doc.getDocumentElement().normalize();
		    
		    String id = doc.getDocumentElement().getAttribute("id");
		    
		    scene.removeEntityAndProperties(id);
		    
		    String x = getValue(doc, "position", "x");
		    String y = getValue(doc, "position", "y");
		    String nEdges = getValue(doc, "nbEdges", "value");
		    
		    String h = getValue(doc, "hsvValue", "H");
		    String s = getValue(doc, "hsvValue", "S");
		    String v = getValue(doc, "hsvValue", "V");
		    
		    String r = getValue(doc, "rgbValue", "R");
		    String g = getValue(doc, "rgbValue", "G");
		    String b = getValue(doc, "rgbValue", "B");
		    
		    scene.addPropertyToEntity(id, prop("x", x));
		    scene.addPropertyToEntity(id, prop("y", y));
		    scene.addPropertyToEntity(id, prop("ne", nEdges));
		    scene.addPropertyToEntity(id, prop("h", h));
		    scene.addPropertyToEntity(id, prop("s", s));
		    scene.addPropertyToEntity(id, prop("v", v));
		    scene.addPropertyToEntity(id, prop("r", r));
		    scene.addPropertyToEntity(id, prop("g", g));
		    scene.addPropertyToEntity(id, prop("b", b));
		}
	    
	}
	
	public static void updateContextWorldBeliefStyle(String xmlString, Context<String,String> scene)
			throws ParserConfigurationException, SAXException, IOException {
	  
		synchronized (scene) { 
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		    Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
		    
		    doc.getDocumentElement().normalize();
		    
		    String id = doc.getDocumentElement().getAttribute("id");
		    
		    scene.removeEntityAndProperties(id);
		    
		    String x = getValue(doc, "position", "x");
		    String y = getValue(doc, "position", "y");
		    
		    String shape_F = getValue(doc, "distribution", "F");
		    String shape_I = getValue(doc, "distribution", "I");
		    String shape_L = getValue(doc, "distribution", "L");
		    String shape_N = getValue(doc, "distribution", "N");
		    String shape_P = getValue(doc, "distribution", "P");
		    String shape_T = getValue(doc, "distribution", "T");
		    String shape_U = getValue(doc, "distribution", "U");
		    String shape_V = getValue(doc, "distribution", "V");
		    String shape_W = getValue(doc, "distribution", "W");
		    String shape_X = getValue(doc, "distribution", "X");
		    String shape_Y = getValue(doc, "distribution", "Y");
		    String shape_Z = getValue(doc, "distribution", "Z");
		    
		    String color_blue = getValue(doc, "distribution", "Blue");
		    String color_brown = getValue(doc, "distribution", "Brown");
		    String color_gray = getValue(doc, "distribution", "Gray");
		    String color_green = getValue(doc, "distribution", "Green");
		    String color_orange = getValue(doc, "distribution", "Orange");
		    String color_pink = getValue(doc, "distribution", "Pink");
		    String color_purple = getValue(doc, "distribution", "Purple");
		    String color_red = getValue(doc, "distribution", "Red");
		    String color_yellow = getValue(doc, "distribution", "Yellow");
		    
		    
		    String nEdges = getValue(doc, "nbEdges", "value");
		    
		    String h = getValue(doc, "hsvValue", "H");
		    String s = getValue(doc, "hsvValue", "S");
		    String v = getValue(doc, "hsvValue", "V");
		    
		    String r = getValue(doc, "rgbValue", "R");
		    String g = getValue(doc, "rgbValue", "G");
		    String b = getValue(doc, "rgbValue", "B");
		    
		    scene.addPropertyToEntity(id, prop("x", x));
		    scene.addPropertyToEntity(id, prop("y", y));
		    
		    scene.addPropertyToEntity(id, prop("label_f_prob", shape_F));
		    scene.addPropertyToEntity(id, prop("label_i_prob", shape_I));
		    scene.addPropertyToEntity(id, prop("label_l_prob", shape_L));
		    scene.addPropertyToEntity(id, prop("label_n_prob", shape_N));
		    scene.addPropertyToEntity(id, prop("label_p_prob", shape_P));
		    scene.addPropertyToEntity(id, prop("label_t_prob", shape_T));
		    scene.addPropertyToEntity(id, prop("label_u_prob", shape_U));
		    scene.addPropertyToEntity(id, prop("label_v_prob", shape_V));
		    scene.addPropertyToEntity(id, prop("label_w_prob", shape_W));
		    scene.addPropertyToEntity(id, prop("label_x_prob", shape_X));
		    scene.addPropertyToEntity(id, prop("label_y_prob", shape_Y));
		    scene.addPropertyToEntity(id, prop("label_z_prob", shape_Z));
		    
		    scene.addPropertyToEntity(id, prop("color_blue_prob", color_blue));
		    scene.addPropertyToEntity(id, prop("color_brown_prob", color_brown));
		    scene.addPropertyToEntity(id, prop("color_grey_prob", color_gray));  // NB spelling
		    scene.addPropertyToEntity(id, prop("color_green_prob", color_green));
		    scene.addPropertyToEntity(id, prop("color_orange_prob", color_orange));
		    scene.addPropertyToEntity(id, prop("color_pink_prob", color_pink));
		    scene.addPropertyToEntity(id, prop("color_purple_prob", color_purple));
		    scene.addPropertyToEntity(id, prop("color_red_prob", color_red));
		    scene.addPropertyToEntity(id, prop("color_yellow_prob", color_yellow));
		    
		    
		    
		    
		    //scene.addPropertyToEntity(id, prop("ne", nEdges));
		    //scene.addPropertyToEntity(id, prop("h", h));
		    //scene.addPropertyToEntity(id, prop("s", s));
		    //scene.addPropertyToEntity(id, prop("v", v));
		    //scene.addPropertyToEntity(id, prop("r", r));
		    //scene.addPropertyToEntity(id, prop("g", g));
		    //scene.addPropertyToEntity(id, prop("b", b));
		}
	    
	}
	
	
	private static String prop(String string, String x) {
		return string + ":" + x;
	}


	private static String getValue(Document doc, String type, String key) {
		NodeList list = doc.getElementsByTagName(type);
		//System.out.println(list.getLength());
		Node target = null;
		for (int i=0; i<list.getLength(); i++){
			if (list.item(i).getAttributes().getNamedItem(key) != null){
				target = list.item(i);
				break;
			}
		}
		
	    return target.getAttributes().getNamedItem(key).getNodeValue();
	}
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
	
	
	Context<String,String> context = new Context<String,String>();
	
	String xmlString = "	<obj id=\"3\"> "+
"<position global=\"left top\" x=\"58\" y=\"36\"/>"+
"<shape BestResponse=\"L\">"+
"	<distribution F=\"0.211178226419\" I=\"0.13178111597\" L=\"0.221920055768\" N=\"0.0913010419543\" P=\"0.0587420489\" T=\"0.0\" U=\"0.013947137604\" V=\"0.164712905676\" W=\"0.0854967613028\" X=\"0.0\" Y=\"0.006973568802\" Z=\"0.013947137604\"/>"+
"	<orientation value=\"-28.0061243848\"/>"+
"	<skewness horizontal=\"right-skewed\" vertical=\"bottom-skewed\"/>"+
"	<nbEdges value=\"9\"/>"+
"</shape>"+
"<colour BestResponse=\"Gray\">"+
	"<distribution Blue=\"2.71610413117e-08\" Brown=\"0.162080697696\" Gray=\"0.307031853875\" Green=\"0.0233555470581\" Orange=\"0.13709689745\" Pink=\"0.133059522439\" Purple=\"0.076566009398\" Red=\"0.0435255782886\" Yellow=\"0.117283866634\"/>"+
	"<hsvValue H=\"55.5093930636\" S=\"22.9873554913\" V=\"44.0036127168\"/>"+
	"<rgbValue B=\"45.6907514451\" G=\"45.0252890173\" R=\"46.7478323699\"/>"+
"</colour>"+
"</obj>";
	
	XmlContextUtil.updateContextWorldBeliefStyle(xmlString, context);
	
	WorldBelief wb = new WorldBeliefParser().convertWorldBeliefFromContext(context);
	System.out.println(wb);
	
	
	
	
	}
}
