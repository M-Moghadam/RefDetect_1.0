package simulation.fogsaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import codeInformation.ElementInformation;
import codeInformation.Entity;
import codeInformation.PairComparator;
import codeInformation.SourceInformation;

/*******************************************************************************************************
 * Description: FOGSAA_Algorithm
******************************************************************************************************/
public class FOGSAA_Algorithm {

	/** This list keeps all classes (in both designs) and contain their best alignment.*/
	private BestAlignment bestAlignment = new BestAlignment();
	
	/** Contains 4 strings (generalString1, 2, 3 and 4) - see details in the class "GeneralStringInformation"*/
	private GeneralStringInformation generalStringInformation;
	
	private String localStr1 = "";
	private String localStr2 = "";
	
	/** Store the nodes of FOGSAA tree for further expansion based on their fitness score(Fmax, Fmin).*/
	private List<DefaultMutableTreeNode> priorityQueue = new ArrayList<DefaultMutableTreeNode>();
	
	/** It keeps the value for best obtained alignment so far.*/
	private double optimalSimilarityValue = Integer.MIN_VALUE;
	
	/** This matrix keeps the best present value for each point (p1, p2).*/
	private double matrix[][];
	
	/** The best leaf achieved so far.*/
	private DefaultMutableTreeNode bestLeaf;
	
	/** They contains methods and fields for each class. Class full name is key and fields and methods are values.*/
	private Map<String, ElementInformation> resultedClassElementsMap;
	private Map<String, ElementInformation> desiredClassElementsMap;
	
	private Map<String, String> resultedClassParentMap;
	private Map<String, String> desiredClassParentMap;
	
	private List<String> resultedClassOrder;
	private List<String> desiredClassOrder;
	
	private Map<String, List<String>> resulted_R_Information;
	private Map<String, List<String>> desired_R_Information;
	
	/** It keeps the name of class which is considering.*/
	private String currentClassFullName;
	
	/** It keeps the list of classes which are changed.*/
	private Set<String> changedClasses;
	
/***********************************************************************************************************
 * Description : FOGSAA_Algorithm()
**************************************************************************************************************/
	public FOGSAA_Algorithm(SourceInformation resultedSourceInformation, 
							SourceInformation desiredSourceInformation, 
						    Set<String> changedClassesParameter) {
		
		//We clone list to changes have no effect on initial ones
		resultedClassOrder = Utility.cloneList(resultedSourceInformation.getClassOrder()); 
		List<String> resultedStr = Utility.cloneList(resultedSourceInformation.getCodeAsString());
		resultedClassElementsMap = resultedSourceInformation.getClassElementsMap();
		resultedClassParentMap = resultedSourceInformation.getClassParentMap();
		
		List<Set<String>> resultedRInformation = resultedSourceInformation.get_R_Information();
		resulted_R_Information = new HashMap<String, List<String>>();
		for (int i = 0; i < resultedRInformation.size(); i++) {
			List<String> cls = new ArrayList<String>(resultedRInformation.get(i));
			Collections.sort(cls);
			resulted_R_Information.put(resultedClassOrder.get(i), cls);
		}
		
		
		desiredClassOrder = Utility.cloneList(desiredSourceInformation.getClassOrder());
		List<String> desiredStr = Utility.cloneList(desiredSourceInformation.getCodeAsString());
		desiredClassElementsMap = desiredSourceInformation.getClassElementsMap();
		desiredClassParentMap = desiredSourceInformation.getClassParentMap();
		
		List<Set<String>> desiredRInformation = desiredSourceInformation.get_R_Information();
		desired_R_Information = new HashMap<String, List<String>>();
		for (int i = 0; i < desiredRInformation.size(); i++) {
			List<String> cls = new ArrayList<String>(desiredRInformation.get(i));
			Collections.sort(cls);
			desired_R_Information.put(desiredClassOrder.get(i),cls);
		}
		
		
		
		/** It is necessary to sort fields and methods in "resultedClassElementsMAP". Note that no need 
		to do this step for "desiredClassElementsMAP" as it is sorted when it is created and it is not changed at all.*/
		sortClassElementsMap(this.resultedClassElementsMap);
		
		changedClasses = changedClassesParameter;
		
		/** Same classes in two versions sit in a same index.*/
		Utility.orderStrings(resultedStr, resultedClassOrder, desiredStr, desiredClassOrder);
		
		generalStringInformation = new GeneralStringInformation();
		generalStringInformation.getStrings(resultedStr, resultedClassOrder, desiredStr, desiredClassOrder, changedClasses);
		
		/** To give methods with different number of parameters the same chance, we change methods based on the number of parameters to single character.*/
		 Utility.convertMethods(generalStringInformation);

		/** Note that "R" is deleted as they are not determined because of which class, it can negatively effect the result.*/
		//Utility.removeRs(generalStringInformation);
	}
		
/***********************************************************************************************************
 * Description : sortClassElementsMap
**************************************************************************************************************/	
	private void sortClassElementsMap(Map<String, ElementInformation> resultedClassElementsMAP){
		
		/** Sort fields based on their name.*/
		Comparator<Entity> cpField = PairComparator.getComparator(PairComparator.SortParameter.Name_ASCENDING);
		for (String classKey : resultedClassElementsMAP.keySet()) {
			Collections.sort(resultedClassElementsMAP.get(classKey).fields, cpField);
		}
		
		/** Sort methods based on their signature and name.*/
		Comparator<Entity> cpMethod = PairComparator.getComparator(PairComparator.SortParameter.SIGNATURE_ASCENDING, PairComparator.SortParameter.Name_ASCENDING);
		for (String classKey : resultedClassElementsMAP.keySet()) {
			Collections.sort(resultedClassElementsMAP.get(classKey).methods, cpMethod);
		}
	}
		
/***********************************************************************************************************
 * Description : run()
**************************************************************************************************************/
	public double run(){
		
		double finalOptimalSimilarityValue = 0;
		int generalPosition1 = 0;
		int generalPosition2 = 0;
		
		/** This variable shows which class is considering now.*/
		int classIndex = -1;
		
		while(generalPosition1 < generalStringInformation.str1.length() || generalPosition2 < generalStringInformation.str2.length()){ 
		
			/** First it is necessary to reset all share data structures.*/
			optimalSimilarityValue = Integer.MIN_VALUE;
			priorityQueue.clear();
			bestLeaf = null;
			
			classIndex++;
			
			/** In a case that nothing is changed no need to more process.*/
			if (changedClasses == null)
				break;

			/** Extract the name of class is considered.*/
			currentClassFullName = resultedClassOrder.get(classIndex);
			
			/** No need to consider classes which are not changed.*/
			if (!changedClasses.contains(currentClassFullName))
				continue;
			
			localStr1 = Utility.getLocalStr(generalStringInformation.str1, generalPosition1);
			localStr2 = Utility.getLocalStr(generalStringInformation.str2, generalPosition2);
			
			generalPosition1 += localStr1.length();
			generalPosition2 += localStr2.length();
			
			if (!currentClassFullName.equals(desiredClassOrder.get(classIndex))){
				System.out.println("Bug: Classes are not aligned correctly.");
				System.exit(0);
			}
			
			/** The "matrix" keeps the best present value for each point (p1, p2).
			    Note that in the beginning we can have for example (a, -). In this case we have a value in matrix(1,0). 
			    In addition, as positions in strings start from 1, we need to get the size of array one more than size of strings.*/
			matrix = new double[localStr1.length() + 1][localStr2.length() + 1]; 
			for (double[] row : matrix)
		        Arrays.fill(row, Integer.MIN_VALUE);

		
			/** Create first node as root.*/
			DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode("root");

			/** The loop is repeated until it finish first branch.*/
			do {
				
				/** The node can have three children (a1,b1), (a1,-) or (-,b1), where the first child indicates 
				a match or mismatch, the second child indicates a gap in S2 and the third one indicates a gap in S1.*/
				List<DefaultMutableTreeNode> children = createChildren(currentNode);
				
				/** It means first branch is finished.*/
				if (currentNode.isLeaf()) {
					bestLeaf = currentNode;
					if (optimalSimilarityValue <= ((NodeInformation)bestLeaf.getUserObject()).fitnessScore.fMax)
						optimalSimilarityValue = ((NodeInformation)bestLeaf.getUserObject()).fitnessScore.fMax;
					break; 
				}
				
				DefaultMutableTreeNode theBestChild = getBestChild(children);
				NodeInformation theBestnodeInformation = (NodeInformation)theBestChild.getUserObject();
				
				Position position = theBestnodeInformation.position;
				double presentScore = theBestnodeInformation.presentScore;
				
				/** Save the best present value for p1, and p2.*/
				matrix[position.position1][position.position2] = presentScore;
				
				currentNode = theBestChild;
	
			} while(true);
			
			/** Expand other branches to find a better expansion if there is any.*/
			ExpandOtherBranches();
			
			/** The value of current string is added to the previous ones.*/
			finalOptimalSimilarityValue += optimalSimilarityValue;
			
			/** Also save best leaf in "bestAlignmentList".*/
			bestAlignment.saveBestLeaf(bestLeaf, currentClassFullName);
		}
		
		/** At this line we have the value for all classes. Then it is necessary the 
		value for classes which are in one design, but not in another design.*/
		
		/** In a case that classes in two strings are not equal.*/
		double dissimilarity = 0;
		if (!(generalStringInformation.str3.isEmpty() && generalStringInformation.str4.isEmpty())){ 
		
			/** "temp2" is equal or greater than "temp1".*/
			int temp1 = generalStringInformation.str3.length();
			int temp2 = generalStringInformation.str4.length();
			if (temp1 > temp2){
				temp2 = generalStringInformation.str3.length();
				temp1 = generalStringInformation.str4.length();
			}
			
			/** Again in this case we have different weight for method depends on its parameters. Therefore,
			if we have CAMPMP its dissimilarity should be the same as CAMM. To cope with this case first it is
			necessary to set gap = 0. In this case we have dissimilarity -4 while a real value for first case 
			CAMPMP is -3.5. In this case we have a unreal stronger dissimilarity. However, I think it does not
			probably a strong negative effect on results.*/
			
			/** To measure dissimilarity the smallest length is measured as mismatch 
			and the difference between two string is measured as gap.*/
			dissimilarity = (temp2 - temp1) *  FOGSAA_Parameters.gap; 
			dissimilarity += temp1 * FOGSAA_Parameters.mismatch;
			
			/**To increase negative effect of dissimilarity in extra or miss classes.*/
			dissimilarity *= FOGSAA_Parameters.dissimilarityEffect;
		}
		
		/** Also save classes which are not in both design.*/
		All_Dissimilarity_Information disSimilarityInformation = 
								      new All_Dissimilarity_Information(generalStringInformation.str3, generalStringInformation.classOrderFullName3,
													 			        resultedClassElementsMap, resultedClassParentMap, resulted_R_Information,
													 			        generalStringInformation.str4, generalStringInformation.classOrderFullName4,
													 			        desiredClassElementsMap, desiredClassParentMap, desired_R_Information);
		
		bestAlignment.disSimilarity = new DisSimilarity(disSimilarityInformation);
				
		return finalOptimalSimilarityValue + dissimilarity;
	}
	
/***********************************************************************************************************
 * Description : All_Dissimilarity_Information
 * 				 We create this class to prevent long method parameter when extract dissimilarity information.
**************************************************************************************************************/
	public class All_Dissimilarity_Information {
		
		List<String> resultedDissimilarList;
		List<String> resultedClassOrder;
		Map<String, ElementInformation> resultedClassElementsMap;
		Map<String, String> resultedClassParentMap;
		Map<String, List<String>> resulted_R_Information;
			
		List<String> desiredDissimilarList;
		List<String> desiredClassOrder;
		Map<String, ElementInformation> desiredClassElementsMap;
		Map<String, String> desiredClassParentMap;
		Map<String, List<String>> desired_R_Information;
		
/***********************************************************************************************************
 * Description : All_Dissimilarity_Information
**************************************************************************************************************/
		All_Dissimilarity_Information(String resultedDissimilarString, List<String> resultedClassOrder,
									  Map<String, ElementInformation> resultedClassElementsMap,
									  Map<String, String> resultedClassParentMap,
									  Map<String, List<String>> resulted_R_Information,
									  String desiredDissimilarString, List<String> desiredClassOrder,
									  Map<String, ElementInformation> desiredClassElementsMap,
									  Map<String, String> desiredClassParentMap,
									  Map<String, List<String>> desired_R_Information){
			
			this.resultedDissimilarList = getasList(resultedDissimilarString);
			this.resultedClassOrder = resultedClassOrder;
			this.resultedClassElementsMap = resultedClassElementsMap;
			this.resultedClassParentMap = resultedClassParentMap;
			this.resulted_R_Information = resulted_R_Information;
			
			this.desiredDissimilarList = getasList(desiredDissimilarString);
			this.desiredClassOrder = desiredClassOrder;
			this.desiredClassElementsMap = desiredClassElementsMap;
			this.desiredClassParentMap = desiredClassParentMap;
			this.desired_R_Information = desired_R_Information;
		}
		
/***********************************************************************************************************
 * Description : getasList()
**************************************************************************************************************/
		private List<String> getasList(String input) {
						
			List<String> result = new ArrayList<String>();
			String temp = "";
						
			for (int i = 0; i < input.length(); i++) {
							
				char ch = input.charAt(i);
					
				if (ch != 'C') temp += ch;
				else {
					if (!temp.isEmpty()) result.add(temp);
					temp = "C";
				}
			}
						
			if (!temp.isEmpty()) result.add(temp);
				
			return result;
		}
	}
	
/***********************************************************************************************************
 * Description : getBestChild()
**************************************************************************************************************/
	private DefaultMutableTreeNode getBestChild(List<DefaultMutableTreeNode> children){
	
		if (children.size() == 1) {
			return children.get(0);
		}
		
		int firstIndex  = 0;
		int secondIndex = 1;
		FutureScore firstFitnessPair  = ((NodeInformation)children.get(0).getUserObject()).fitnessScore;
		FutureScore secondFitnessPair = ((NodeInformation)children.get(1).getUserObject()).fitnessScore;
		FutureScore thirdFitnessPair  = ((NodeInformation)children.get(2).getUserObject()).fitnessScore;
		
		if (secondFitnessPair.fMax > firstFitnessPair.fMax){
			firstIndex  = 1;
			secondIndex = 0;
		}
		
		else if (equalsDouble(secondFitnessPair.fMax, firstFitnessPair.fMax)) {
			if (secondFitnessPair.fMin > firstFitnessPair.fMin){
				firstIndex  = 1;
				secondIndex = 0;
			}
		}
		
		if (thirdFitnessPair.fMax > ((NodeInformation)children.get(secondIndex).getUserObject()).fitnessScore.fMax){
			secondIndex = 2;
		}
		
		else if (equalsDouble(thirdFitnessPair.fMax, ((NodeInformation)children.get(secondIndex).getUserObject()).fitnessScore.fMax)) {
			if (thirdFitnessPair.fMin > ((NodeInformation)children.get(secondIndex).getUserObject()).fitnessScore.fMin){
				secondIndex = 2;
			}
		}
		
		if (thirdFitnessPair.fMax > ((NodeInformation)children.get(firstIndex).getUserObject()).fitnessScore.fMax){
			secondIndex = firstIndex;
			firstIndex  = 2;
		}
		
		else if (equalsDouble(thirdFitnessPair.fMax, ((NodeInformation)children.get(firstIndex).getUserObject()).fitnessScore.fMax)) {
			if (thirdFitnessPair.fMin > ((NodeInformation)children.get(firstIndex).getUserObject()).fitnessScore.fMin){
				secondIndex = firstIndex;
				firstIndex  = 2;
			}
		}
		
		/** Here we know the best and second best child. However, if the third child is equal the second one add both to the queue.*/

		addtoPriorityQueue(children.get(secondIndex));
		
		/** Decide if two reaming children have the same merit.*/
		Set<Integer> numbers = new HashSet<Integer>(Arrays.asList(0, 1, 2)); 
		numbers.remove(firstIndex);
		numbers.remove(secondIndex);
		Integer thirdIndex = numbers.iterator().next();
		if (twoRemainingChildrenAreTheSame(children, secondIndex, thirdIndex)) 
			addtoPriorityQueue(children.get(thirdIndex));	
			
		return children.get(firstIndex);
	}

/***********************************************************************************************************
 * Description : twoRemainingChildrenAreTheSame()
**************************************************************************************************************/
	private boolean twoRemainingChildrenAreTheSame(List<DefaultMutableTreeNode> children, int secondIndex, int thirdindex){
		
		FutureScore secondFitnessPair  = ((NodeInformation)children.get(secondIndex).getUserObject()).fitnessScore;
		FutureScore thirdFitnessPair  = ((NodeInformation)children.get(thirdindex).getUserObject()).fitnessScore;
		
		if (equalsDouble(secondFitnessPair.fMax, thirdFitnessPair.fMax) && equalsDouble(secondFitnessPair.fMin, thirdFitnessPair.fMin))
			return true;
		
		return false;
	}
	
/***********************************************************************************************************
 * Description : equalsDouble()
**************************************************************************************************************/
	private boolean equalsDouble(Double value1, Double value2) {
	    return (Math.abs(value1 - value2) == 0);
	}
	
/***********************************************************************************************************
 * Description : addtoPriorityQueue()
**************************************************************************************************************/
	private void addtoPriorityQueue(DefaultMutableTreeNode node){
		
		FutureScore fitnessPair = ((NodeInformation)node.getUserObject()).fitnessScore;
		
		double fMax = fitnessPair.fMax;
		double fMin = fitnessPair.fMin;
		
		int i = 0;
		for (; i < priorityQueue.size(); i++) {
			
			FutureScore pair = ((NodeInformation)priorityQueue.get(i).getUserObject()).fitnessScore;
		
			if (fMax > pair.fMax){
				priorityQueue.add(i, node);
				break;
			}
			
			if (equalsDouble(fMax, pair.fMax))
				if (fMin > pair.fMin){
					priorityQueue.add(i, node);
					break;
				}
		}
		
		/** If it is the last element.*/ 
		if (priorityQueue.size() == i)
			priorityQueue.add(i, node);
	}
	
/***********************************************************************************************************
 * Description : ExpandOtherBranches()
**************************************************************************************************************/
	private double ExpandOtherBranches(){
	
		DefaultMutableTreeNode currentNode = null;
		while(true){

			if (currentNode == null)
				currentNode = getFirstPairofPriorityQueue();
		
			if (currentNode == null) break;
		
			NodeInformation currentnodeInformation = (NodeInformation)currentNode.getUserObject();
		
			Position position = currentnodeInformation.position;
			double presentScore = currentnodeInformation.presentScore;
			FutureScore fitnessPair = currentnodeInformation.fitnessScore;
		
			/** Prune the current branch as it has already been traversed in a better way.*/
			if (presentScore <= matrix[position.position1][position.position2]) {
				currentNode = null;
				continue; 
			}
		
			/** Save the best present value for p1, and p2.*/
			matrix[position.position1][position.position2] = presentScore;
			
			/** Prune the current branch if The Tmax of the current node is not grater than the optimal 
			branch score obtained so far, then it can not ever lead to the optimal solution.*/
			if (fitnessPair.fMax <= optimalSimilarityValue) {
				currentNode = null;
				continue; 
			}
		
			ArrayList<DefaultMutableTreeNode> children = createChildren(currentNode);
		
			if (currentNode.isLeaf()) {
				getBestLeaf(currentNode);
				currentNode = null;
				continue;
			}
		
			/** Two best child are added to the queue.*/
			currentNode = getBestChild(children);
		}
	
		return optimalSimilarityValue;
	}
		
/***********************************************************************************************************
 * Description : getBestLeaf()
**************************************************************************************************************/
	private void getBestLeaf(DefaultMutableTreeNode newLeaf){
		
		double newLeafScore = ((NodeInformation)((DefaultMutableTreeNode)newLeaf).getUserObject()).fitnessScore.fMax;
		double bestLeafScore = ((NodeInformation)((DefaultMutableTreeNode)bestLeaf).getUserObject()).fitnessScore.fMax;
		
		if (newLeafScore > bestLeafScore){
			bestLeaf = newLeaf;
			if (optimalSimilarityValue <= ((NodeInformation)bestLeaf.getUserObject()).fitnessScore.fMax)
				optimalSimilarityValue = ((NodeInformation)bestLeaf.getUserObject()).fitnessScore.fMax;
		}
	}
	
/***********************************************************************************************************
 * Description : createChildren()
**************************************************************************************************************/
	private ArrayList<DefaultMutableTreeNode> createChildren(DefaultMutableTreeNode parent){
	
		ArrayList<DefaultMutableTreeNode> children = new ArrayList<DefaultMutableTreeNode>();
		int p1 = 0;
		int p2 = 0;
		
		Object nodeInformation = parent.getUserObject();
		if (nodeInformation != "root") {
			Position position = ((NodeInformation)nodeInformation).position;
			p1 = position.position1;
			p2 = position.position2;
		}

		String c1 = (localStr1.length() > p1 ? Character.toString(localStr1.charAt(p1)) : null); 
		String c2 = (localStr2.length() > p2 ? Character.toString(localStr2.charAt(p2)) : null); 
		
		/** Create first child (a1, b1).*/
		if (c1 != null && c2 != null)
			children.add(createNode(parent, c1 + "," + c2));
		
		/** Create second child (a1, -).*/
		if (c1 != null) 
			children.add(createNode(parent, c1 + ",-"));
		
		/** Create third child (-, b1).*/
		if (c2 != null)
			children.add(createNode(parent, "-," + c2));
		
		return children;
	}
	
/***********************************************************************************************************
 * Description : createNode()
 * 				 The method creates a new node depends on input parameters.
**************************************************************************************************************/
	private DefaultMutableTreeNode createNode(DefaultMutableTreeNode parent, String nodesCharachter){
		
		//The information for element1 is in index 0, and the information for the second element is in index 1
		List<ElementDetails> elements_Details = getElements_Information(parent, nodesCharachter);
		ElementDetails element1 = elements_Details.get(0);
		ElementDetails element2 = elements_Details.get(1);
		
		/** "presentScore" is the addition of scores for each node, from root to
		the current node of the current branch, gives the present score.*/				 
		double presentScore = getHowMatch(element1, element2);
		if (parent.getUserObject() != "root") 
			presentScore += ((NodeInformation)parent.getUserObject()).presentScore;
		
		/** Increase the pointer depends on how entities are matched to each other.*/
		Position positionPair = increasePointer(nodesCharachter, parent);
		
		int type = getType(element1, element2);  
		
		NodeInformation nodeInformation = new NodeInformation(nodesCharachter, presentScore, measureFitnessScore(positionPair, presentScore), positionPair, type, element1.name, element2.name);
		
		DefaultMutableTreeNode node	= new DefaultMutableTreeNode(nodeInformation);
		parent.add(node);
		
		return node;
	}
	
/***********************************************************************************************************
 * Description : getElements_Information
**************************************************************************************************************/
	private List<ElementDetails> getElements_Information(DefaultMutableTreeNode parent, String nodeName) {
		
		List<ElementDetails> elements_Details = new ArrayList<ElementDetails>();
		
		char char1 = nodeName.charAt(0);
		char char2 = nodeName.charAt(2); 
		
		//Position of the elements
		int position1 = getPosition(char1, "part1", parent);
		int position2 = getPosition(char2, "part2", parent);
		
		String element1_Name = Utility.getElementFullName(char1, position1, currentClassFullName, 
			     										 resultedClassElementsMap, resultedClassParentMap, resulted_R_Information);

		boolean isAbstract1 = isAbstractMethod(char1, position1, resultedClassElementsMap);


		String element2_Name = Utility.getElementFullName(char2, position2, currentClassFullName,
			     										 desiredClassElementsMap, desiredClassParentMap, desired_R_Information);
		
		boolean isAbstract2 = isAbstractMethod(char2, position2, desiredClassElementsMap);
		
		elements_Details.add(0, new ElementDetails(char1, position1, element1_Name, isAbstract1));
		elements_Details.add(1, new ElementDetails(char2, position2, element2_Name, isAbstract2));
		
		return elements_Details;
	}
	
/***********************************************************************************************************
 * Description : ElementDetails
**************************************************************************************************************/
	class ElementDetails {
		
		char character;
		int position;
		String name;
		boolean isAbstract;
		
		ElementDetails(char character, int position, String name, boolean isAbstract) {
		
			this.character = character; 
			this.position =  position;
			this.name = 	name;
			this.isAbstract = isAbstract;
		}
	}

/***********************************************************************************************************
 * Description : getHowMatch()
 * 				 There are three possibilities: match, mismatch, and gap. The function returns 
 * 				 a value depends on which case happens.
**************************************************************************************************************/
	private double getHowMatch(ElementDetails element1, ElementDetails element2){
		
		char char1 = element1.character;
		char char2 = element2.character;
		
		double score = 0;
		
		/** If it is a gap.*/
 		if (char1 == '-' || char2 == '-') 
			score = FOGSAA_Parameters.gap;

		/** If it is a match.*/
 		else if (char1 == char2 && element1.name.equals(element2.name) && (element1.isAbstract == element2.isAbstract))
			score = FOGSAA_Parameters.match;
				
		/** Otherwise, it is a mismatch.*/
 		else score = FOGSAA_Parameters.mismatch;
		
		return score;
	}
	
/***********************************************************************************************************
 * Description : isAbstractMethod()
**************************************************************************************************************/
	private boolean isAbstractMethod(char character, int position, Map<String, ElementInformation> classElementsMap){
		
		switch(character) {
			case '-':	
			case 'C':
			case 'G':
			case 'A':
			case 'R': return false;
		}	
				
		/** In this case it is a method.*/
		return classElementsMap.get(currentClassFullName).methods.get(position).isAbstract();
	}
	
/***********************************************************************************************************
 * Description : getPosition()
**************************************************************************************************************/
	private int getPosition(char character, String part, DefaultMutableTreeNode parent){

		if (character == '-') return -1;
		
		int position = 0;
		
		DefaultMutableTreeNode currentNode = parent; 
		
		while (!currentNode.isRoot()) {
			
			NodeInformation nodeInformation = (NodeInformation) currentNode.getUserObject();
			
			String nodeName = nodeInformation.nodeName;
			
			char ch;
			if (part.equals("part1"))
				ch = nodeName.charAt(0);
			else ch = nodeName.charAt(2);
			
			if (ch != '-') {
			
				if (character == 'C' || character == 'G' || character == 'A' || character == 'R') {
					if (ch != character) break;
				}
				
				//It is a method (Z, X, Y, ...)
				else {
					if (ch == 'C' || ch == 'G' || ch == 'A') break;
				}
				
				position++;
			}
			
			currentNode = (DefaultMutableTreeNode) currentNode.getParent();
		}
		
		return position;
	}
	
/***********************************************************************************************************
 * Description : getType()
 * 				 The type of alignment: 
 * 				 Type = 1: indicates a match. Type = 2: indicates a mismatch.
 * 		 		 Type = 3: indicates a gap in s1. Type = 4: indicates a gap in s2.
**************************************************************************************************************/
	private int getType(ElementDetails element1, ElementDetails element2){
			
		char char1 = element1.character;
		char char2 = element2.character;
		
		String element1_Name = element1.name;
		String element2_Name = element2.name;
		
		/** If there is a gap in part1.*/
		if (char1 == '-') return 3; 
		
		/** If there is a gap in part2.*/
		if (char2 == '-') return 4;
		
		/** If it is a Mismatch.*/
		if ((char1 != char2) || (!element1_Name.equals(element2_Name) || (element1.isAbstract != element2.isAbstract))) 
			return 2;
		
		/** It is a match.*/
		return 1;
	}

/***********************************************************************************************************
 * Description : increasePointer()
**************************************************************************************************************/
	private Position increasePointer(String nodeName, DefaultMutableTreeNode parent){ 
		
		int p1 = 0;
		int p2 = 0;
		
		Object nodeInformation = parent.getUserObject();
		if (nodeInformation != "root") {
			Position parentPosition = ((NodeInformation)nodeInformation).position;
			p1 = parentPosition.position1;
			p2 = parentPosition.position2;
		}
		
		String part1 = nodeName.substring(0, nodeName.indexOf(','));
		String part2 = nodeName.substring(nodeName.indexOf(',') + 1);
		
		/** If it is a gap in first string.*/
		if (part1.contains("-")) p2++;
		
		/** If it is a gap in second string.*/
		if(part2.contains("-")) p1++;
		
		/** At this stage it is a match or mismatch, so counts both p1 and p2.*/
		if (!(part1.contains("-") || part2.contains("-"))) {
			p1++;
			p2++;
		}
		
		return new Position(p1, p2);
	}
		
/***********************************************************************************************************
 * Description : measureFitnessScore()
**************************************************************************************************************/
	private FutureScore measureFitnessScore(Position positionPair, double prs){
		
		double fMax, fMin;
		int x1 = this.localStr2.length() - positionPair.position2;
		int x2 = this.localStr1.length() - positionPair.position1;  
		
		if (x2 < x1){
			fMin = x2 * FOGSAA_Parameters.mismatch + FOGSAA_Parameters.gap * (x1 - x2);
			fMax = x2 * FOGSAA_Parameters.match + FOGSAA_Parameters.gap * (x1 - x2);
		}
		else {
			fMin = x1 * FOGSAA_Parameters.mismatch + FOGSAA_Parameters.gap * (x2 - x1);
			fMax = x1 * FOGSAA_Parameters.match + FOGSAA_Parameters.gap * (x2 - x1);
		}
		
		return new FutureScore(fMax + prs, fMin + prs);
	}
	
/***********************************************************************************************************
 * Description : getFirstPairofPriorityQueue()
 * 				 The method remove and returns the first element in "priorityQueue".  
**************************************************************************************************************/
	private DefaultMutableTreeNode getFirstPairofPriorityQueue(){
		
		if (priorityQueue.isEmpty()) return null; 
		
		DefaultMutableTreeNode node = priorityQueue.get(0);
		priorityQueue.remove(0);
		
		return node;
	}
	
/*******************************************************************************************************
 * Description: getBestAlignment()
********************************************************************************************************/
	public BestAlignment getBestAlignment() {
		return bestAlignment;
	}
}