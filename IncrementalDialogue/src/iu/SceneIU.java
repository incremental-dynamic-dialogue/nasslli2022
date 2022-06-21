package iu;

import sium.nlu.context.Context;
import module.vision.WorldBelief;
import inpro.incremental.unit.IU;

/**
 * Simple class to hold a Context object mapping object ID to properties of the objects in the scene.
 * 
 * @author jhough
 */
public class SceneIU extends IU {
	
	private Context<String, String> context;
	private WorldBelief worldBelief;
	
	public SceneIU(Context<String, String> a_context){
		this.context = a_context;
	}
	
	public Context<String, String> getContext(){
		return this.context;
	}
	
	public WorldBelief getWorldBelief() {
		return this.worldBelief;
	}
		
	public SceneIU(WorldBelief wb){
		this.worldBelief = wb;
	}

	@Override
	public String toPayLoad() {
		if (this.context!=null)
			return this.context.toString();
		if (this.worldBelief!=null)
			return this.worldBelief.toString();
		return "";
	}
	

}
