package module.dm;

import java.util.ArrayList;

/**
 * A simple putting game where the aim is to put objects
 * into numbered boxes on the playing area.
 * 
 * @author jhough
 */
public class PutInNumberedSlot extends Game {

	
	private String[] targetPositionSlots; //will have a finite number of final target positions
	private int currentTargetSlot; //keeps a record of which the next target slot is
	private Integer[][] boundingBoxes;// the bounding box positions of the target areas
	private ArrayList<String> piecesInPlay;

	public void init(Integer[][] finalpositionsBB){
		setTargetPositionSlots(new String[finalpositionsBB.length]);
		setCurrentTargetSlot(-1);
	}

	
	public static int meanRounded(Integer[] m) {
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    return (int) (sum / m.length);
	}
	
	//constructor
	public PutInNumberedSlot(Integer[][] finalpositionsBB){
		//uses the bounding box regions of the target areas
		super();
		setBoundingBoxes(finalpositionsBB);
		init(finalpositionsBB);
		
	}
	
	public PutInNumberedSlot(int numberPieces, Integer[] topLeftCornerOfScene, Integer[] bottomRightCornerOfScene) {
		//create the bounding boxes at the bottom of the scene (TODO for now beyond the bottom)
		super();
		int numberSlots = 6; //TODO hard coded based on our current board
		int width = bottomRightCornerOfScene[0] - topLeftCornerOfScene[0];
		int slotwidth = width / numberSlots;
		int slotlength = 100; //TODO hard coded, about length of piece
		Integer[][] bbs = new Integer[numberSlots][4]; //number of pieces, dimension
		int currentLeftX = topLeftCornerOfScene[0];
		for (int i=0; i<numberSlots; i++){
			bbs[i][0] = currentLeftX; //topleft x
			bbs[i][1] = bottomRightCornerOfScene[1]- slotlength; //y
			bbs[i][2] = currentLeftX + slotwidth; //bottom right x
			bbs[i][3] = bottomRightCornerOfScene[1]; //y
			currentLeftX = currentLeftX + slotwidth;
		}
		//add the pieces to the game
		ArrayList<String> pieces = new ArrayList<String>();
		for (int j=0; j<numberPieces; j++){ //TODO hard coding number of slots based on our current board
			pieces.add(String.valueOf(j));
		}
		
		setBoundingBoxes(bbs);
		setPiecesInPlay(pieces);
		init(bbs);
	
	}

	@Override
	public void start(){
		super.start();
		//this.setNextTargetSlot();	//when decided automatically
		this.setCurrentTargetSlot(-1); //default when chosen by user/system online
	}
	
	/*
	public void setNextTargetSlot(){
		 //Method to generate the next target slot automatically.
		 //Can be a simple incrementer or something more complex
		 
		this.nexttargetslot = this.nexttargetslot + 1;
	}
    */
	
	public void piecePlacedAtSlotNumber(String pieceID, int slot){
		//when a piece is placed at a target slot
		boolean newslot = true;
		
		if (this.targetPositionSlots[slot]!=null){
			java.awt.Toolkit.getDefaultToolkit().beep();
			int readable  = slot+1;
			System.out.println("GAME WARNING: PIECE ALREADY AT POSITION " + readable);
			newslot = false;
		}
		this.targetPositionSlots[slot] = pieceID;
		this.removeFromPlay(pieceID); //for simple game removing it from play
		if (completed()||newslot==false){
			return;
		}
		this.setCurrentTargetSlot(-1);
	}
	
	@Override
	public boolean completed(){
		
		if (this.getPiecesInPlay().isEmpty()||this.getPositionsInPlay().isEmpty()){
			
			return true;
		}
		return false;
		
	}
	
	public ArrayList<Integer> getPositionsInPlayAsInts(){
		ArrayList<Integer> positions = new ArrayList<Integer>();
		for (int i=0; i<getTargetPositionSlots().length; i++){
			if (getTargetPositionSlots()[i]==null){
				positions.add(i);
			}
		}
		return positions;
	}
	
	/***
	 * Returns the possible final positions yet to have a piece in them yet (still in play).
	 * This is in player-friendly form (idx + 1) and as a string rather than int.
	 */
	public ArrayList<String> getPositionsInPlay() {
		ArrayList<String> positions = new ArrayList<String>();
		for (int i : this.getPositionsInPlayAsInts()){
			positions.add(String.valueOf(i+1));
		}
		return positions;
	}
	
	public Integer[] getBoundingBox(int slot){
		return this.boundingBoxes[slot];
	}
	
	public Integer[] getBoundingBoxOfCurrentTarget(){
		return this.getBoundingBox(this.currentTargetSlot);
	}
	
	public Integer[] getCentroidOfSlot(int slot){
		Integer[] bb = getBoundingBox(slot);
		Integer[] centroid = new Integer[2];
		int x = (int) (long) Math.round(bb[2] - ((bb[2] - bb[0])/2.0));
		int y = (int) (long) Math.round(bb[3] - ((bb[3] - bb[1])/2.0));
		centroid[0] = x;
		centroid[1] = y;
		return centroid;
	}
	
	public Integer[] geCenroidOfCurrentTargetLocation(){
		return getCentroidOfSlot(this.currentTargetSlot);
	}
	
	public Integer[][] getCentroidsOfPositionsInPlay(){
		ArrayList<Integer> posInPlay = this.getPositionsInPlayAsInts();
		Integer[][] centroids = new Integer[posInPlay.size()][2];
		int count = 0;
		for (int i : posInPlay){
			centroids[count] = this.getCentroidOfSlot(i);
			count++;
		}
		return centroids;
	}
	
	public Integer[] getCentroidOfCentroidsOfPositionsInPlay() {
		Integer[][] centroids = this.getCentroidsOfPositionsInPlay();
		Integer[] x = new Integer[centroids.length];
		Integer[] y = new Integer[centroids.length];
		for (int i=0; i<centroids.length; i++){
			x[i] = centroids[i][0];
			y[i] = centroids[i][1];
		}
		return new Integer[] {meanRounded(x), meanRounded(y)};
	}
	
	@Override
	public Integer[] getCentroidOfCentroidsOfAvailableTargetDestinations() {
		// TODO Auto-generated method stub
		return this.getCentroidOfCentroidsOfPositionsInPlay();
	}
	
	public void removeFromPlay(String ID){
		this.piecesInPlay.remove(ID);
	}
	
	public boolean inPlay(String ID){
		if (this.piecesInPlay.contains(ID)){
			return true;
		}
		return false;
	}
	
	
	//standard getters and setters
	
	/**
	 * Setting the next target based on either user move
	 * or through robot's initiative.
	 */
	public void setCurrentTargetSlot(int i){
		this.currentTargetSlot = i;
	}
	
	public Integer[][] getBoundingBoxes() {
		return boundingBoxes;
	}

	public void setBoundingBoxes(Integer[][] boundingboxes) {
		this.boundingBoxes = boundingboxes;
	}
	
	public String[] getTargetPositionSlots() {
		return targetPositionSlots;
	}

	public void setTargetPositionSlots(String[] targetpositionslots) {
		this.targetPositionSlots = targetpositionslots;
	}

	public int getCurrentTargetSlot() {
		return currentTargetSlot;
	}
	
	public ArrayList<String> getPiecesInPlay() {
		return piecesInPlay;
	}

	public void setPiecesInPlay(ArrayList<String> piecesinplay) {
		this.piecesInPlay = piecesinplay;
	}
	
	
	
	
	
	public static void main(String[] args) {
		
		Integer[][] bbs = new Integer[3][4]; //number of pieces, dimensions
		bbs[0][0] = 40; //top left
		bbs[0][1] = 200;
		bbs[0][2] = 60; //bottom right
		bbs[0][3] = 220;
		bbs[1][0] = 60; //top left
		bbs[1][1] = 200;
		bbs[1][2] = 80; //bottom right
		bbs[1][3] = 220;
		bbs[2][0] = 80; //top left
		bbs[2][1] = 200;
		bbs[2][2] = 100; //bottom right
		bbs[2][3] = 220;
		
		PutInNumberedSlot put = new PutInNumberedSlot(bbs);
		System.out.println(put.getName());
		System.out.println(put.targetPositionSlots.length);
		put.start();
		//piece 1 at position 0
		//pause(1);
		System.out.println(put.getCurrentTargetSlot());
		System.out.println(put.getBoundingBoxOfCurrentTarget()[0]);
		System.out.println(put.getBoundingBoxOfCurrentTarget()[2]);
		put.piecePlacedAtSlotNumber("1", 0);
		System.out.println(put.completed());
		//piece 2 at position 0 (error)
		//pause(1);
		System.out.println();
		System.out.println(put.getCurrentTargetSlot());
		System.out.println(put.getBoundingBoxOfCurrentTarget()[0]);
		System.out.println(put.getBoundingBoxOfCurrentTarget()[2]);
		put.piecePlacedAtSlotNumber("2", 0);
		System.out.println(put.completed());
		//piece 2 at position 1
		//pause(1);
		System.out.println();
		System.out.println(put.getCurrentTargetSlot());
		System.out.println(put.getBoundingBoxOfCurrentTarget()[0]);
		System.out.println(put.getBoundingBoxOfCurrentTarget()[2]);
		put.piecePlacedAtSlotNumber("2", 1);
		System.out.println(put.completed());
		//piece 3 at position 2
		//pause(1);
		System.out.println();
		System.out.println(put.getCurrentTargetSlot());
		System.out.println(put.getBoundingBoxOfCurrentTarget()[0]);
		System.out.println(put.getBoundingBoxOfCurrentTarget()[2]);
		put.piecePlacedAtSlotNumber("3", 2);
		System.out.println(put.completed());
	
	
	}




}
