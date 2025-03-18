package moea.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import moea.variable.RefactoringVariable;
import simulation.candidateRefactorings.CandidateEntity;
import simulation.candidateRefactorings.CandidateRefactorings;
import simulation.candidateRefactorings.DiffAlgorithm.CandidateFieldsForRefactoring_DiffAlgorithm;
import simulation.candidateRefactorings.DiffAlgorithm.CandidateMethodsForRefactoring_DiffAlgorithm;
import simulation.fogsaa.BestAlignment;
import simulation.fogsaa.DisSimilarity;

public class DiffRefactoringProblem extends AdvancedProblem {

/*******************************************************************************************************
 * Description: DiffRefactoringProblem()
 * 				This constructor is important. It should be also public. MOEA needs this constructor. 
********************************************************************************************************/
	public DiffRefactoringProblem(){}
	
/*******************************************************************************************************
 * Description: DiffRefactoringProblem()
********************************************************************************************************/
	public DiffRefactoringProblem(int i){
		
		initialCandidateFields = new CandidateFieldsForRefactoring_DiffAlgorithm();
		initialCandidateMethods = new CandidateMethodsForRefactoring_DiffAlgorithm();
	}
	
/*******************************************************************************************************
 * Description: reviseReults()
********************************************************************************************************/
	@Override
	void reviseReults(CandidateRefactorings initialCandidateMethods){
		
		filterFalsePositiveCases(initialCandidateMethods);
		
		findNotDetectedMoveClassRefactorings();
	}
	
/*******************************************************************************************************
 * Description: findNotDetectedMoveClassRefactorings()
 *				If two classes have similar names, but they are not detected as rename candidate, but 
 *				at least three entities is moved from the original class to the desired one, we detect
 *				this as Move Class candidate refactoring, and delete all Move Entity candidate refactorings
 *				from the original class to the desired one. 
 *				As an example below classes in Elastic f77804dad35c13d9ff96456e85737883cf7ddd99.
 *				Original Class: org.elasticsearch.index.shard.MergePolicySettingsTest
 *				Desired Class: org.elasticsearch.test.ElasticsearchTestCase
********************************************************************************************************/
	private void findNotDetectedMoveClassRefactorings() {
		
		for (int i = 0; i < newClassCandidates.size(); i++) {
			
			String newClassFullName = newClassCandidates.get(i);
			
			String newClassName = newClassFullName.substring(newClassFullName.lastIndexOf(".") + 1);
			
			for (String deletedClassFullName : deleteClassCandidates) {
				
				String deletedClassName = deletedClassFullName.substring(deletedClassFullName.lastIndexOf(".") + 1);

				//If classes have different name no need to continue;
				if (!newClassName.equals(deletedClassName)) continue;
				
				//If at least three entity is moved from deleted class to the new one, we assume they are the same.
		
				List<CandidateEntity> temp1 = new ArrayList<CandidateEntity>();
				for (CandidateEntity candidateEntity : initialCandidateMethods.getcandidateMthodRefactorings().candidatesForMoveMethodRef) {
					
					if (candidateEntity.originalClassFullName.equals(deletedClassFullName) && 
						candidateEntity.targetClassFullName.equals(newClassFullName)) {
						
							temp1.add(candidateEntity);
					}
				}
				
				List<CandidateEntity> temp2 = new ArrayList<CandidateEntity>();
				for (CandidateEntity candidateEntity : initialCandidateFields.getcandidateFieldRefactorings().candidatesForMoveFieldRef) {
					
					if (candidateEntity.originalClassFullName.equals(deletedClassFullName) && 
						candidateEntity.targetClassFullName.equals(newClassFullName)) {
						
						temp2.add(candidateEntity);
					}
				}
				
				if (temp2.size() + temp1.size() < 3) continue;
				
				//Detected Move Entity should be deleted.
				for (CandidateEntity candidateEntity : temp1) {
					initialCandidateMethods.getcandidateMthodRefactorings().candidatesForMoveMethodRef.remove(candidateEntity);
				}
				
				for (CandidateEntity candidateEntity : temp2) {
					initialCandidateFields.getcandidateFieldRefactorings().candidatesForMoveFieldRef.remove(candidateEntity);
				}
				
				//classes should be deleted from the new and deleted lists.
				deleteClassCandidates.remove(deletedClassFullName);
				newClassCandidates.remove(newClassFullName); i--;
				
				//Move class refactoring should be created.
				renameClassCandidates.put(deletedClassFullName, newClassFullName);
				
				break;
			}
		}
	}
	
/*******************************************************************************************************
 * Description: extractCandidateforDeleteClassRefactoring()
********************************************************************************************************/
	@Override
	List<String> extractCandidateforDeleteClassRefactoring(DisSimilarity disSimilarity) {
		
		List<String> deleteClassCandidates = new ArrayList<String>();
		
		for (String cls : disSimilarity.resultedClassOrder) {
			
			if (renameClassCandidates.get(cls) == null) deleteClassCandidates.add(cls);
		}

		return deleteClassCandidates;
	}
	
/*******************************************************************************************************
 * Description: extractCandidateforNewClassRefactoring()
********************************************************************************************************/
	@Override
	List<String> extractCandidateforNewClassRefactoring(DisSimilarity disSimilarity) {
			
		List<String> newClassCandidates = new ArrayList<String>();
			
		Collection<String> renamedClasses = renameClassCandidates.values();
		
		for (String cls : DisSimilarity.desiredClassOrder) {
			
			if (!renamedClasses.contains(cls)) newClassCandidates.add(cls);
		}

		return newClassCandidates;
	}
	
/*******************************************************************************************************
 * Description: createRefactoringVariable()
********************************************************************************************************/	
	@Override
	RefactoringVariable createRefactoringVariable() {

		//Note that we clone the initial alignment.
		return new RefactoringVariable(0, initialSourceInformation.clone(), 
								       new BestAlignment(initialAlignment), this);
	}
}