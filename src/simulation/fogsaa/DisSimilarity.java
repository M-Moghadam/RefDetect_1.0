package simulation.fogsaa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import codeInformation.ElementInformation;
import simulation.fogsaa.FOGSAA_Algorithm.All_Dissimilarity_Information;

/***********************************************************************************************************
 * Description : DisSimilarity()
 * 				 "resultedDissimilarList" and "desiredDissimilarString" contains classes for resulted and 
 * 				 desired design respectively. Note that these strings can have more than one class, and one 
 * 				 can be empty while the other have some elements.
 * 				 Classes in "resultedDissimilarList" are those which are in resulted design, but not exist in desired design. 
 * 				 Classes in "desiredDissimilarString" are those which are in desired design, but not exist in resulted design.
**************************************************************************************************************/
public class DisSimilarity {
			
	public List<String> resultedDissimilarList;
	public List<String> resultedClassOrder;
	public List<ArrayList<String>> resultedListOfFullNames;
	
	public static List<String> desiredDissimilarList;
	public static List<String> desiredClassOrder;
	public static List<ArrayList<String>> desiredListOfFullNames;
	
/***********************************************************************************************************
 * Description : DisSimilarity()
 * 				 This constructor create a deep copy of input dissimilarity parameter.
**************************************************************************************************************/
	DisSimilarity(DisSimilarity inputDissimilarity){
				
		this.resultedDissimilarList = Utility.cloneList(inputDissimilarity.resultedDissimilarList);
		this.resultedClassOrder = Utility.cloneList(inputDissimilarity.resultedClassOrder);
		this.resultedListOfFullNames = Utility.cloneList(inputDissimilarity.resultedListOfFullNames);
	}
	
/***********************************************************************************************************
 * Description : DisSimilarity()
**************************************************************************************************************/
	DisSimilarity(All_Dissimilarity_Information dissimilarityInformatin){
		
		this.resultedDissimilarList = dissimilarityInformatin.resultedDissimilarList;
		this.resultedClassOrder = Utility.cloneList(dissimilarityInformatin.resultedClassOrder);
		this.resultedListOfFullNames = extractEntityNames(resultedDissimilarList, resultedClassOrder,
													      dissimilarityInformatin.resultedClassElementsMap, 
													      dissimilarityInformatin.resultedClassParentMap, 
													      dissimilarityInformatin.resulted_R_Information);
		
		
		//Note that as desired design is not changed, we run the below instruction once.
		//In RunRefactoringProblem we set this variable to false.
		if (desiredListOfFullNames == null) {
			
			desiredDissimilarList = Utility.cloneList(dissimilarityInformatin.desiredDissimilarList); 
			desiredClassOrder = Utility.cloneList(dissimilarityInformatin.desiredClassOrder);
			
			desiredListOfFullNames = extractEntityNames(desiredDissimilarList, desiredClassOrder,
														dissimilarityInformatin.desiredClassElementsMap, 
														dissimilarityInformatin.desiredClassParentMap, 
														dissimilarityInformatin.desired_R_Information);
		}
	}
	
/***********************************************************************************************************
 * Description : extractEntityNames()
**************************************************************************************************************/
	private List<ArrayList<String>> extractEntityNames(List<String> dissimilarList, List<String> classOrder,
													   Map<String, ElementInformation> classElementsMap,
													   Map<String, String> classParentMap,
													   Map<String, List<String>> R_Information) {
					
		List<ArrayList<String>> listOfFullNames = new ArrayList<ArrayList<String>>();
			
		int[] positions;
			
		for (int i = 0; i < dissimilarList.size(); i++) {
				
			ArrayList<String> entityFullNames = new ArrayList<String>();
					
			//It contains field, method and relation positions respectively.
			positions = new int[3];
					
			String classInformation = dissimilarList.get(i);
					
			for (int j = 0; j < classInformation.length(); j++) {
						
				char entity = classInformation.charAt(j);
						
				int position = getPosition(entity, positions);
						
				String entityFullName = Utility.getElementFullName(entity, position, classOrder.get(i),
																   classElementsMap, classParentMap, R_Information);
						
				entityFullNames.add(entityFullName);
			}
					
			listOfFullNames.add(entityFullNames);
		}
			
		return listOfFullNames;	
	}
		
/***********************************************************************************************************
 * Description : getPosition()
**************************************************************************************************************/
	private int getPosition(char entity, int[] positions){
			
		int position = -1;
			
		switch (entity) {
				
			case 'C': 
			case 'G': position = -1; break;
				
			case 'A': position = positions[0]++; break;
				
			case 'R': position = positions[2]++; break;
				
			default: position = positions[1]++;
		}
			
		return position;
	}
	
/***********************************************************************************************************
 * Description : setDesiredInformationNull()
**************************************************************************************************************/
	public static void setDesiredInformationNull() {
		desiredDissimilarList = null;
		desiredClassOrder = null;
		desiredListOfFullNames = null;
	}
}