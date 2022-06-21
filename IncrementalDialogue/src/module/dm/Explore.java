package module.dm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semantics.GoalLocationRelation;
import util.Stats;
import module.vision.SceneUpdater;

/**
 * A simple exploration game for objects where the user can ask for the
 * system to GRAB and SHOW them the object, it can DROP it in a specified location
 * or GIVE the user the object.
 * This can be ongoing until the user wants to stop.
 * 
 * @author jhough
 */
public class Explore extends Game {
	
	public Explore(){
		super();
	}
	
	
	/**
	 * From the target object compute the nearest centroid at which to 
	 * place it, given the location relation (e.g. BEHIND etc.) to be computed relative to 
	 * the target destination IDs
	 * 
	 * @param scene
	 * @param objectID
	 * @param relation
	 * @param destinationIDs
	 * @return
	 */
	public Integer[] getFreeSpaceForObjectFromRelationAndReferenceObjects(
			SceneUpdater scene,
			String objectID,
			GoalLocationRelation relation,
			ArrayList<String> destinationIDs) {
		// TODO Auto-generated method stub
		//String coordString = scene.getObjectCentroidXY(objectID);
		//String[] coordsStrings = coordString.split(":");
		//Double[] coords = new Double[]{Double.parseDouble(coordsStrings[0]), Double.parseDouble(coordsStrings[1])};
		
		//Double[] objectCoords = objectCoordString.split(":")
		Set<Integer[]> referenceCoords = new HashSet<Integer[]>();
		for (String ID : destinationIDs){
			String coordString = scene.getObjectCentroidXY(ID);
			String[] coordsStrings = coordString.split(":");
			//for (String )
			referenceCoords.add(new Integer[]{Integer.parseInt(coordsStrings[0]), Integer.parseInt(coordsStrings[1])});
		}
		int x = 0;
		int y = 0;
		double[] all_x = new double[referenceCoords.size()];
		double[] all_y = new double[referenceCoords.size()];
		int count = 0;
		switch (relation){
			case BEHIND:
				y = scene.getBottomRightCornerOfScene()[1];
				for (Integer[] xy : referenceCoords){
					if (xy[1]>y){
						y = xy[1];
					}
					all_x[count] = xy[0];
					count+=1;
				}
				x = (int) new Stats().mean(all_x);
				y = y - 100;
				break;
			case BETWEEN:
				logger.debug("No BETWEEN calculation");
				break;
			case FRONT:
				y = scene.getTopLeftCornerOfScene()[1];
				for (Integer[] xy : referenceCoords){
					if (xy[1]<y){
						y = xy[1];
					}
					all_x[count] = xy[0];
					count+=1;
				}
				x = (int) new Stats().mean(all_x);
				y = y - 100;
				break;
			case INTO:
				logger.debug("No INTO calculation");
				break;
			case LEFT:
				x = scene.getBottomRightCornerOfScene()[0];
				for (Integer[] xy : referenceCoords){
					if (xy[0]<x){
						x = xy[0];
					}
					all_y[count] = xy[1];
					count+=1;
				}
				x = x - 100;
				y = (int) new Stats().mean(all_y);
				break;
			case ONTO:
				logger.debug("No ONTO calculation");
				break;
			case RIGHT:
				x = scene.getTopLeftCornerOfScene()[0];
				for (Integer[] xy : referenceCoords){
					if (xy[0]>x){
						x = xy[0];
					}
					all_y[count] = xy[1];
					count+=1;
				}
				x = x + 100;
				y = (int) new Stats().mean(all_y);
				break;
			default:
				break;

		}
		return new Integer[]{x,y};
	}

	@Override
	public Integer[] getCentroidOfCentroidsOfAvailableTargetDestinations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean completed() {
		// always returns false
		return false;
	}


	@Override
	public Integer[] geCenroidOfCurrentTargetLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public static void main(String[] args) {
		Explore game = new Explore();
		game.start();
	}
}
