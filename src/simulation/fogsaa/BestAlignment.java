package simulation.fogsaa;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

/***********************************************************************************************************
 * Description : BestAlignment()
**************************************************************************************************************/
public class BestAlignment {
	
	public List<Similarity> similarityList;
	public DisSimilarity disSimilarity;
	
/***********************************************************************************************************
 * Description : BestAlignment()
**************************************************************************************************************/
	BestAlignment() {
		 similarityList = new ArrayList<Similarity>();
	}
	
/***********************************************************************************************************
 * Description : BestAlignment()
 * 				 This constructor create a deep copy and prevent problem with shadow copy.
**************************************************************************************************************/
	public BestAlignment(BestAlignment inputAlignment) {
		
		this();
		
		for (Similarity sim : inputAlignment.similarityList) {
			
			similarityList.add(new Similarity(sim.classFullName, 
										      Utility.cloneList(sim.resultedAlignment),
										      Utility.cloneList(sim.resultedAlignmentFullName), 
										      Utility.cloneList(sim.desiredAlignment),
										      Utility.cloneList(sim.desiredAlignmentFullName),
										      Utility.cloneList(sim.types),
										      sim.currentScore,
										      sim.possibleMaximumScore));
		}
		
		
		//It supports deep copy. Therefore, no extra action needed.
		disSimilarity = new DisSimilarity(inputAlignment.disSimilarity);
	}
		
/***********************************************************************************************************
 * Description : saveBestLeaf()
**************************************************************************************************************/
	void saveBestLeaf(DefaultMutableTreeNode bestLeaf, String classFullName){
	
		List<Character> resultedAlignment = new ArrayList<Character>();
		List<String> resultedAlignmentFullName = new ArrayList<String>();
		
		List<Character> desiredAlignment = new ArrayList<Character>();
		List<String> desiredAlignmentFullName = new ArrayList<String>();

		List<Integer> types = new ArrayList<Integer>();
		
		double possibleMaximumScore = 0;
		
		DefaultMutableTreeNode currentNode = bestLeaf;
		
		NodeInformation nodeInformation = (NodeInformation) currentNode.getUserObject();
		double currentScore = nodeInformation.presentScore;
		
		while (!currentNode.isRoot()) {
			
			nodeInformation = (NodeInformation) currentNode.getUserObject();
			
			String[] chars = nodeInformation.nodeName.split(",");
			
			resultedAlignment.add(0, chars[0].charAt(0));
			resultedAlignmentFullName.add(0, nodeInformation.Part1Name);
			
			desiredAlignment.add(0, chars[1].charAt(0));
			desiredAlignmentFullName.add(0, nodeInformation.Part2Name);
			
			types.add(0, nodeInformation.type);
			
			if (possibleMaximumScore < nodeInformation.fitnessScore.fMax) 
				possibleMaximumScore = nodeInformation.fitnessScore.fMax;
			
			/** Go up until root.*/
			currentNode = (DefaultMutableTreeNode) currentNode.getParent();
		}
		
		Similarity sim = new Similarity(classFullName, 
									    resultedAlignment, resultedAlignmentFullName, 
									    desiredAlignment, desiredAlignmentFullName,
									    types, currentScore, possibleMaximumScore);
		
		similarityList.add(sim);
	}
}