package simulation.calculateSimilarity;

import java.util.Map;

import codeInformation.Call_Information.CallingMethods;
import codeInformation.Entity;
import codeInformation.Field;
import simulation.CandidateRenameClassRefactoring.CandidateClassforRenameRefactoring;
import simulation.candidateRefactorings.DiffAlgorithm.Service;

/*******************************************************************************************************
* Description: CalculateFieldSimilarity
* 			   This class is responsible for calculating similarity between two fields.
********************************************************************************************************/
public class CalculateFieldSimilarity extends CalculateEntitySimilarity {
	
/*******************************************************************************************************
 * Description: CalculateFieldSimilarity()
********************************************************************************************************/
	public CalculateFieldSimilarity(String originalClassFullName, String desiredClassFullName) {
		super(originalClassFullName, desiredClassFullName);
	}
	
/*******************************************************************************************************
 * Description: calculateSimilarity()
********************************************************************************************************/
	void calculateSimilarity(Entity originalEntity, Entity desiredEntity, 
							 Map<String, CallingMethods> originalCallingClass,
							 Map<String, CallingMethods> desiredCallingClass,
							 TemporarySimilarityClass temporarySimilarity,
							 DeletedNewEntities delete_And_NewEntities) {
	
		Field originalField = (Field) originalEntity;
		String originalFieldName = originalField.getName();
		Field desiredField = (Field) desiredEntity;
		String desiredFieldName = desiredField.getName();
		
		temporarySimilarity.desiredEntities.add(desiredField);
		
		//If fields have different type, then no need to continue.
		if (Service.hasSimilarType(originalField.getFieldTypeFullName(), desiredField.getFieldTypeFullName()) == 0) {
			temporarySimilarity.hasSimilarName.add(false);
			temporarySimilarity.relationshipSimilarityValues.add(0f);
			return;
		}
		
		/** We checks that fields have similar names.*/ 
		boolean similarName = false;
		if (similarSimpleName(originalFieldName, originalClassFullName, desiredFieldName, desiredClassFullName)) {
			temporarySimilarity.hasSimilarName.add(true);
			similarName = true;
		}
		else temporarySimilarity.hasSimilarName.add(false);
		
		
		//Now consider callInformationMap
		SimilarityInformation similarEntitiesThatCallBothFields = similar_Fields_Methods_That_CallBothEntities(originalFieldName, desiredFieldName,
																					           				   originalCallingClass, desiredCallingClass,
																					           				   delete_And_NewEntities);
		
		SimilarityInformation similarEntitiesThatBothFieldsCalled = similar_Fields_Methods_That_BothEntitiesCall(originalFieldName, originalCallingEntityMap,
																												 desiredFieldName, desiredCallingEntityMap,
																					             				 delete_And_NewEntities);
		
		boolean hasMinumumSimilarity = true;
		if (similarEntitiesThatCallBothFields.similarityValue == -100 && similarEntitiesThatBothFieldsCalled.similarityValue == -100) {
			hasMinumumSimilarity = false;
			if (CandidateClassforRenameRefactoring.renameClassRefactoringIsChecking && similarName) {
				similarEntitiesThatCallBothFields.similarityValue = 1;
				similarEntitiesThatBothFieldsCalled.similarityValue = 1;
				hasMinumumSimilarity = true;
			}
		}
		if (similarEntitiesThatCallBothFields.similarityValue == -100) similarEntitiesThatCallBothFields.similarityValue = 0;
		if (similarEntitiesThatBothFieldsCalled.similarityValue == -100) similarEntitiesThatBothFieldsCalled.similarityValue = 0;
		

		float relationshipSimilarityValue = 0;
		if (hasMinumumSimilarity || hasMinimumSimilarity(similarEntitiesThatCallBothFields, similarEntitiesThatBothFieldsCalled)) {
		
			relationshipSimilarityValue = similarEntitiesThatCallBothFields.similarityValue + 
										  similarEntitiesThatBothFieldsCalled.similarityValue;
		
		}
		
		if(Service.hasSimilarType(originalField.getFieldTypeFullName(), desiredField.getFieldTypeFullName()) == 0) {
		   
			if (!hasStrongSimilarity(similarEntitiesThatCallBothFields, similarEntitiesThatBothFieldsCalled)) {
			
				relationshipSimilarityValue = 0;
		   }
		}
		
		temporarySimilarity.relationshipSimilarityValues.add(relationshipSimilarityValue);
	}
}