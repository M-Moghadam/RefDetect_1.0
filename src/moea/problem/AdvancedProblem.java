package moea.problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codeInformation.Entity;
import configuration.GetPropertyThresholds;
import simulation.CandidateRenameClassRefactoring.CandidateClassforRenameRefactoring;
import simulation.candidateRefactorings.CandidateEntity;
import simulation.candidateRefactorings.CandidateFieldRefactorings;
import simulation.candidateRefactorings.CandidateMethodRefactorings;
import simulation.candidateRefactorings.CandidateRefactorings;
import simulation.candidateRefactorings.CandidateRenameEntity;
import simulation.candidateRefactorings.DiffAlgorithm.CandidateBasedonDiffAlgorithm;
import simulation.fogsaa.BestAlignment;
import simulation.fogsaa.DisSimilarity;

public abstract class AdvancedProblem extends RefactoringProblem {

	//This variable saves the alignment for "initialSourceInformation" and "desiredSourceInformation".
	static BestAlignment initialAlignment;
	
	//This variable saves Rename Class refactoring candidates.
	static Map<String, String> renameClassCandidates;
	
	//This variable saves Delete Class refactoring candidates. 
	static List<String> deleteClassCandidates;
	
	//This variable saves New Class refactoring candidates. 
	static List<String> newClassCandidates;
	
	//This variable saves candidate fields for refactoring based on the initial and desired designs.
	static CandidateRefactorings initialCandidateFields;
	
	//This variable saves candidate methods for refactoring based on the initial and desired designs.
	static CandidateRefactorings initialCandidateMethods;
	
	public static Map<String, List<Entity>> fieldsMoveFromMap;
	public static Map<String, List<Entity>> fieldsMoveInMap;
	
	public static Map<String, List<Entity>> methodsMoveFromMap;
	public static Map<String, List<Entity>> methodsMoveInMap;
	
/*******************************************************************************************************
 * Description: extractInitialCandidateFieldsAndMethods()
 *    			This method has two rounds. In the second round we used the refactoring detected in the 
 * 				first round to improve the efficiency of the algorithm. 
********************************************************************************************************/
	void extractInitialCandidateFieldsAndMethods(BestAlignment initialAlignment) {
		
		extractInitialCandidateFieldsAndMethods(initialAlignment, this.getName() + "_FirstRound");
		
		CandidateBasedonDiffAlgorithm.secondRound = true;
		
		extractInitialCandidateFieldsAndMethods(initialAlignment, this.getName() + "_SecondRound");
		
		CandidateBasedonDiffAlgorithm.secondRound = false;
	}	

/*******************************************************************************************************
 * Description: extractInitialCandidateFieldsAndMethods()
********************************************************************************************************/
	private void extractInitialCandidateFieldsAndMethods(BestAlignment initialAlignment, String round){
		
		//First extract inconsistency based on diff results produced by alignment algorithm.
		extractInconsistence(initialAlignment);
		
		//See config.thresholds for more information.
		GetPropertyThresholds config = new GetPropertyThresholds(round);
		
		//It shows the importance of similar names when entities are compared.
		float nameSimilarityValue = config.thresholds.nameSimilarityValue;
				
		//Two entities are determined similar if their similarity is higher than this value.
		float entitySimilarityThreshold = config.thresholds.entitySimilarityThreshold;
				
		//Two classes are determined similar if their similarity is higher than this value.
		float classSimilarityThreshod = config.thresholds.classSimilarityThreshod;
		
		CandidateClassforRenameRefactoring candidateClassRename = new CandidateClassforRenameRefactoring();
		renameClassCandidates = candidateClassRename.extractRenameClassRefactoring(initialAlignment.disSimilarity, 
																				   nameSimilarityValue, 
																				   entitySimilarityThreshold, 
																				   classSimilarityThreshod);
	
		//The new and deleted are only determined in the second round.
		if (round.endsWith("_SecondRound")) {
			
			deleteClassCandidates = extractCandidateforDeleteClassRefactoring(initialAlignment.disSimilarity);
			
			newClassCandidates = extractCandidateforNewClassRefactoring(initialAlignment.disSimilarity);
		}
		
		initialCandidateFields.decideAboutEntities(renameClassCandidates, nameSimilarityValue, entitySimilarityThreshold);
		
		initialCandidateMethods.decideAboutEntities(renameClassCandidates, nameSimilarityValue, entitySimilarityThreshold);
		
		
		if (round.endsWith("_SecondRound")) {
		
			removeDuplicateRefactorings(initialCandidateFields, initialCandidateMethods);
			reviseReults(initialCandidateMethods);
		}
	}
	
/*******************************************************************************************************
 * Description: removeDuplicateRefactorings()
********************************************************************************************************/
	private void removeDuplicateRefactorings(CandidateRefactorings initialCandidateFields, 
											 CandidateRefactorings initialCandidateMethods){
		
		initialCandidateFields.getcandidateFieldRefactorings().removeDuplicate();
		initialCandidateMethods.getcandidateMthodRefactorings().removeDuplicate();
	}
	
	abstract List<String> extractCandidateforDeleteClassRefactoring(DisSimilarity disSimilarity);
	abstract List<String> extractCandidateforNewClassRefactoring(DisSimilarity disSimilarity);
	abstract void reviseReults(CandidateRefactorings initialCandidateMethods);
	
/*******************************************************************************************************
 * Description: setStaticFieldsNull()
********************************************************************************************************/
	public static void setStaticFieldsNull() {
		
		initialAlignment = null;
		renameClassCandidates = null;
		deleteClassCandidates = null;
		newClassCandidates = null;
		initialCandidateFields = null;
		initialCandidateMethods = null;
		fieldsMoveFromMap = null;
		fieldsMoveInMap = null;
		methodsMoveFromMap = null;
		methodsMoveInMap = null;
		CandidateRefactorings.matchedMethods = null;
		CandidateRefactorings.matchedFields  = null;
	}
	
/*******************************************************************************************************
 * Description: setInitialAlignment()
********************************************************************************************************/
	static void setInitialAlignment(BestAlignment alignment) {
		initialAlignment = alignment;
	}
	
/*******************************************************************************************************
 * Description: getCloneCopyofInitialAlignment()
********************************************************************************************************/
	public static BestAlignment getCloneCopyofInitialAlignment() {
		return new BestAlignment(initialAlignment);
	}
	
/*******************************************************************************************************
 * Description: extractInconsistence()
 * 				This method extracts inconsistency based on diff results produced by alignment algorithm.
********************************************************************************************************/
	 public void extractInconsistence(BestAlignment initialAlignment) {
		
		fieldsMoveFromMap = new HashMap<String, List<Entity>>();
		fieldsMoveInMap = new HashMap<String, List<Entity>>();
		
		methodsMoveFromMap = new HashMap<String, List<Entity>>();
		methodsMoveInMap = new HashMap<String, List<Entity>>();
		
		initialCandidateFields.extractMoveInMoveFormEntities(initialAlignment);
		
		initialCandidateMethods.extractMoveInMoveFormEntities(initialAlignment);
	}
	 
/*******************************************************************************************************
 * Description: getInitialCandidateFieldRefactorings()
********************************************************************************************************/
	public static CandidateFieldRefactorings getInitialCandidateFieldRefactorings() {
			return initialCandidateFields.getcandidateFieldRefactorings();
	}
	
/*******************************************************************************************************
 * Description: getInitialCandidateMethods()
********************************************************************************************************/
	public static CandidateMethodRefactorings getInitialCandidateMethodRefactorings() {
		return initialCandidateMethods.getcandidateMthodRefactorings();
	}
	
/*******************************************************************************************************
 * Description: getRenameClassCandidates()
********************************************************************************************************/
	public static Map<String, String> getRenameClassCandidates() {
		return renameClassCandidates;
	}
	
/*******************************************************************************************************
 * Description: getDeleteClassCandidates()
********************************************************************************************************/
	public static List<String> getDeleteClassCandidates() {
		return deleteClassCandidates;
	}
	
/*******************************************************************************************************
 * Description: getNewClassCandidates()
********************************************************************************************************/
	public static List<String> getNewClassCandidates() {
		return newClassCandidates;
	}
	
/*******************************************************************************************************
 * Description: filterFalsePositiveCases()
 * 				This method completed as son as I find new cases that should be filter.
 * 
 * 				Change method parameter in a class has more priority than other refactorings which move method to other class.
********************************************************************************************************/
	void filterFalsePositiveCases(CandidateRefactorings initialCandidateMethods) {
				
		List<CandidateRenameEntity> falsePositiveRefactoringList = new ArrayList<CandidateRenameEntity>();

		for (int i = 0; i < initialCandidateMethods.getcandidateMthodRefactorings().candidatesForChangeMethodParametersRef.size(); i++) {

			CandidateRenameEntity candidate1 = initialCandidateMethods.getcandidateMthodRefactorings().candidatesForChangeMethodParametersRef.get(i);

			if (!candidate1.originalClassFullName.equals(candidate1.targetClassFullName)) continue;

			//First Change Method Parameter refactoring.
			for (int j = 0; j < initialCandidateMethods.getcandidateMthodRefactorings().candidatesForChangeMethodParametersRef.size(); j++) {

				CandidateRenameEntity candidate2 = initialCandidateMethods.getcandidateMthodRefactorings().candidatesForChangeMethodParametersRef.get(j);

				//We delete Change Parameters in the original class which are then moved to other class.
				if (candidate1.entityName.equals(candidate2.entityName)	&& 
						candidate1.originalClassFullName.equals(candidate2.originalClassFullName) &&
						(!candidate1.originalClassFullName.equals(candidate2.targetClassFullName))) {

					initialCandidateMethods.getcandidateMthodRefactorings().candidatesForChangeMethodParametersRef.remove(candidate2);
					falsePositiveRefactoringList.add(candidate2);
					j--;
					if (i > j) i--;

					continue; //No need to check the next condition.
				}

				//We delete Change Parameters in other classes which are then moved to the original class.
				String candidate2NewName = candidate2.entityNewName;
				if ((candidate1.entityName.equals(candidate2NewName) || candidate1.entityNewName.equals(candidate2NewName)) && 
						candidate1.originalClassFullName.equals(candidate2.targetClassFullName) &&
						(!candidate1.originalClassFullName.equals(candidate2.originalClassFullName))) {

					initialCandidateMethods.getcandidateMthodRefactorings().candidatesForChangeMethodParametersRef.remove(candidate2);
					falsePositiveRefactoringList.add(candidate2);
					j--;
					if (i > j) i--;
				}
			}




			//We delete Rename Method which are then moved to other class.
			for (int j = 0; j < initialCandidateMethods.getcandidateMthodRefactorings().candidatesForRenameMethodRef.size(); j++) {

				CandidateRenameEntity candidate2 = initialCandidateMethods.getcandidateMthodRefactorings().candidatesForRenameMethodRef.get(j);

				if (candidate1.entityName.equals(candidate2.entityName)	&& 
						candidate1.originalClassFullName.equals(candidate2.originalClassFullName) && 
						(!candidate1.originalClassFullName.equals(candidate2.targetClassFullName))) {

					initialCandidateMethods.getcandidateMthodRefactorings().candidatesForRenameMethodRef.remove(candidate2);
					falsePositiveRefactoringList.add(candidate2);
					j--;

					continue; //No need to check the next condition.
				}

				//We delete Rename Method in other classes which are then moved to the original class.
				String candidate2NewName = candidate2.entityNewName;
				if ((candidate1.entityName.equals(candidate2NewName)|| candidate1.entityNewName.equals(candidate2NewName)) && 
						candidate1.originalClassFullName.equals(candidate2.targetClassFullName) && 
						(!candidate1.originalClassFullName.equals(candidate2.originalClassFullName))) {

					initialCandidateMethods.getcandidateMthodRefactorings().candidatesForRenameMethodRef.remove(candidate2);
					falsePositiveRefactoringList.add(candidate2);
					j--;
					if (i > j) i--;
				}
			}


			//Third Move Method refactorings.
			for (int j = 0; j < initialCandidateMethods.getcandidateMthodRefactorings().candidatesForMoveMethodRef.size(); j++) {

				CandidateEntity candidate2 = initialCandidateMethods.getcandidateMthodRefactorings().candidatesForMoveMethodRef.get(j);

				if (candidate1.entityName.equals(candidate2.entityName)	&& 
						candidate1.originalClassFullName.equals(candidate2.originalClassFullName) && i != j) {

					initialCandidateMethods.getcandidateMthodRefactorings().candidatesForMoveMethodRef.remove(candidate2);
					j--;

					continue; //No need to check the next condition.
				}

				//We delete Move Method in other classes which are moved to the original class.
				String candidate2Name = candidate2.entityName;
				if ((candidate1.entityName.equals(candidate2Name)|| candidate1.entityNewName.equals(candidate2Name)) && 
						candidate1.originalClassFullName.equals(candidate2.targetClassFullName)) {

					initialCandidateMethods.getcandidateMthodRefactorings().candidatesForMoveMethodRef.remove(candidate2);
					j--;
				}
			}
		}


		//Now remove false positive Change Method Parameters refactorings based on the detected false positive ones.
		for (int i = 0; i < falsePositiveRefactoringList.size(); i++) {

			CandidateRenameEntity FP_Candidate = falsePositiveRefactoringList.get(i);

			for (int j = 0; j < initialCandidateMethods.getcandidateMthodRefactorings().candidatesForChangeMethodParametersRef.size(); j++) {

				CandidateRenameEntity candidate = initialCandidateMethods.getcandidateMthodRefactorings().candidatesForChangeMethodParametersRef.get(j);

				if (FP_Candidate.entityNewName.equals(candidate.entityName)	&& 
						FP_Candidate.originalClassFullName.equals(candidate.originalClassFullName)) {

					initialCandidateMethods.getcandidateMthodRefactorings().candidatesForChangeMethodParametersRef.remove(candidate);
					falsePositiveRefactoringList.add(candidate);
					j--;
				}
			}
		}


		//Now remove false positive Move Method refactorings based on detected FP in previous loop.
		for (CandidateRenameEntity FP_Candidate : falsePositiveRefactoringList) {

			String FP_MethodNewName = FP_Candidate.entityNewName;
			String FP_OriginalClass = FP_Candidate.originalClassFullName;

			for (int j = 0; j < initialCandidateMethods.getcandidateMthodRefactorings().candidatesForMoveMethodRef.size(); j++) {

				CandidateEntity candidate = initialCandidateMethods.getcandidateMthodRefactorings().candidatesForMoveMethodRef.get(j);

				if (candidate.entityName.equals(FP_MethodNewName)	&& 
						candidate.originalClassFullName.equals(FP_OriginalClass)) {

					initialCandidateMethods.getcandidateMthodRefactorings().candidatesForMoveMethodRef.remove(candidate);
					j--;
				}
			}
		}
	}
}