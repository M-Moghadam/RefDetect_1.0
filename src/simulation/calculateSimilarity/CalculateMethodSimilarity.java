package simulation.calculateSimilarity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import codeInformation.Method;
import codeInformation.SourceInformation;
import simulation.CandidateRenameClassRefactoring.CandidateClassforRenameRefactoring;
import simulation.candidateRefactorings.DiffAlgorithm.Service;
import codeInformation.Call_Information.CallingMethods;
import codeInformation.Call_Information.VariablePair;
import moea.problem.RefactoringProblem;
import codeInformation.Entity;

/*******************************************************************************************************
 * Description: CalculateMethodSimilarity
 * 			    This class is responsible for calculating similarity between two methods.
********************************************************************************************************/
public class CalculateMethodSimilarity extends CalculateEntitySimilarity {

	private Map<String, Map<String, Set<VariablePair>>> originalCalledVariableMap;
	private Map<String, Map<String, Set<VariablePair>>> desiredCalledVariableMap;
	
/*******************************************************************************************************
 * Description: CalculateMethodSimilarity()
********************************************************************************************************/
	public CalculateMethodSimilarity(String originalClassFullName, String desiredClassFullName) {
		
		super(originalClassFullName, desiredClassFullName);
		
		originalCalledVariableMap = originalCallInformation.getCalledVariableInformationMap().get(originalClassFullName);
		desiredCalledVariableMap = desiredCallInformation.getCalledVariableInformationMap().get(desiredClassFullName);
	}
	
/*******************************************************************************************************
 * Description: calculateSimilarity()
********************************************************************************************************/
	void calculateSimilarity(Entity originalEntity, Entity desiredEntity, 
							 Map<String, CallingMethods> originalCallingClass,
							 Map<String, CallingMethods> desiredCallingClass,
							 TemporarySimilarityClass temporarySimilarity,
							 DeletedNewEntities delete_And_NewEntities) {
		
		Method originalMethod = (Method) originalEntity;
		String originalMethodName = originalMethod.getName();
		Method desiredMethod = (Method) desiredEntity;
		String desiredMethodName = desiredMethod.getName();
		
		temporarySimilarity.desiredEntities.add(desiredMethod);
		
		//If methods have different return type, then no need to continue.
		if (Service.hasSimilarType(originalMethod.getMethodReturnTypeFullName(), desiredMethod.getMethodReturnTypeFullName()) == 0) {
			if (!ignoreConstructorsForRenameClassRefactoring(originalMethodName, desiredMethodName)) {
				temporarySimilarity.hasSimilarName.add(false);
				temporarySimilarity.relationshipSimilarityValues.add(0f);
				return;
			}
		}
		
		/** We checks that methods have similar names.*/ 
		if (similarSimpleName(originalMethodName, originalClassFullName, desiredMethodName, desiredClassFullName)) { 
			temporarySimilarity.hasSimilarName.add(true);
		}
		else temporarySimilarity.hasSimilarName.add(false);

		//Now consider callInformationMap
		
		//This variable saves number of similar methods that call both original and desired methods.
		SimilarityInformation similarEntitiesThatCallBothMethods = similar_Fields_Methods_That_CallBothEntities(originalMethodName, desiredMethodName,
																					            			    originalCallingClass, desiredCallingClass, 
																					            			    delete_And_NewEntities);

		SimilarityInformation similarEntitiesThatBothMethodsCalled = similar_Fields_Methods_That_BothEntitiesCall(originalMethodName, originalCallingEntityMap,
																												  desiredMethodName, desiredCallingEntityMap,
																				     	  						  delete_And_NewEntities);
		
		SimilarityInformation similarVariablesThatBothMethodAccess = similarVariablesBothMethodsAccess(originalMethodName, originalCalledVariableMap, 
																									   desiredMethodName, desiredCalledVariableMap);
		
		/** This means method does not call any entity and it is not called by any method and has no variable. 
		 *  In this case we assume they are the same. This case happens for abstract methods.*/
		boolean hasMinumumSimilarity = false;
		if (similarEntitiesThatCallBothMethods.similarityValue == -100 && similarEntitiesThatBothMethodsCalled.similarityValue == -100 && similarVariablesThatBothMethodAccess.similarityValue == -100) {
			
			if (hasSimilarSimpleName(originalMethodName, desiredMethodName)) {
				
				similarEntitiesThatCallBothMethods.similarityValue = 1;
				similarEntitiesThatBothMethodsCalled.similarityValue = 1;
				similarVariablesThatBothMethodAccess.similarityValue = 1;
				hasMinumumSimilarity = true;
			}
		}
		
		if (similarEntitiesThatCallBothMethods.similarityValue == -100) similarEntitiesThatCallBothMethods.similarityValue = 0;
		if (similarEntitiesThatBothMethodsCalled.similarityValue == -100) similarEntitiesThatBothMethodsCalled.similarityValue = 0;
		if (similarVariablesThatBothMethodAccess.similarityValue == -100) similarVariablesThatBothMethodAccess.similarityValue = 0;
		
		
		float relationshipSimilarityValue = 0;
		if (hasMinumumSimilarity || hasMinimumSimilarity(similarEntitiesThatCallBothMethods, similarEntitiesThatBothMethodsCalled, similarVariablesThatBothMethodAccess)) {
			
			relationshipSimilarityValue = similarEntitiesThatCallBothMethods.similarityValue + 
									      similarEntitiesThatBothMethodsCalled.similarityValue + 
									      similarVariablesThatBothMethodAccess.similarityValue;
			
			//To give more chance to a desired method in the original class to be matched with the original method
			if (originalClassFullName.equals(desiredClassFullName) &&
			    hasSimilarSimpleName(originalMethodName, desiredMethodName) && 
			    hasStrongSimilarity(similarEntitiesThatCallBothMethods, similarEntitiesThatBothMethodsCalled, similarVariablesThatBothMethodAccess)) {
				
				relationshipSimilarityValue += (relationshipSimilarityValue * 0.1);
			}
			
			else if(relationshipSimilarityValue < 1 && 
					weakRelationship(similarEntitiesThatCallBothMethods, similarEntitiesThatBothMethodsCalled, similarVariablesThatBothMethodAccess))  
				
				relationshipSimilarityValue = 0;
		}
		
		/** Also measure string literal similarity. However, it seems including this feature in test cases makes the result waker, and resulting 
		 *  some FP cases as there are many text similarity in test cases. Therefore, we do not consider this feature for test classes.*/
		relationshipSimilarityValue += measureStringLiteralsSimilarity(originalClassFullName, originalMethodName, desiredClassFullName, desiredMethodName);
		
		temporarySimilarity.relationshipSimilarityValues.add(relationshipSimilarityValue);
	}
	
/*******************************************************************************************************
 * Description: measureStringLiteralsSimilarity()
********************************************************************************************************/
	private int measureStringLiteralsSimilarity(String originalClassFullName, String originalMethodName, 
												String desiredClassFullName, String desiredMethodName) {
	
		SourceInformation initialSourceInformation = RefactoringProblem.getInitialSourceInformation();
		SourceInformation desiredSourceInformation = RefactoringProblem.getDesiredSourceInformation();
		
		if (initialSourceInformation.isTestClass(originalClassFullName) || desiredSourceInformation.isTestClass(desiredClassFullName)) return 0;
		
		List<String> initialStringLiterals = initialSourceInformation.getStringLiterals(originalClassFullName, originalMethodName);
		List<String> desiredStringLiterals = desiredSourceInformation.getStringLiterals(desiredClassFullName, desiredMethodName);
		
		return measureStringLiteralsSimilarity(initialStringLiterals, desiredStringLiterals);
	}
	
/*******************************************************************************************************
 * Description: measureStringLiteralsSimilarity
 * 				Currently we set maximum value of 5 for string literal similarity.
********************************************************************************************************/
	private int measureStringLiteralsSimilarity(List<String> list1, List<String> list2) {
		
		if (list1 == null || list2 == null) return 0;
		
		if (list1.isEmpty() || list2.isEmpty()) return 0;
		
		long correct1 = list1.stream()
		            		 .filter(ingredient -> list2.contains(ingredient))
		            		 .count();
		
		long correct2 = list2.stream()
	    					 .filter(ingredient -> list1.contains(ingredient))
	    					 .count();
		    
		double correct = correct1 < correct2 ? correct1 : correct2;
		
		double size = list1.size() > list2.size() ? list1.size() : list2.size();
		
		return (int) Math.round((correct / size) * 5);
	}
	
/*******************************************************************************************************
 * Description: weakRelationship
********************************************************************************************************/
	private boolean weakRelationship(SimilarityInformation similarityInfo1, SimilarityInformation similarityInfo2, SimilarityInformation similarityInfo3) {
		
			if (similarityInfo1.similarEntities == 0) {
				
				if (similarityInfo2.similarEntities == 0 || similarityInfo3.similarEntities == 0) return true;
			}
			
			else if (similarityInfo2.similarEntities == 0 && similarityInfo3.similarEntities == 0) return true;
					
		return false;
	}

/*******************************************************************************************************
 * Description: ignoreConstructorsForRenameClassRefactoring()
 * 				If we are comparing two classes for Rename Class refactoring, then when comparing 
 * 				their constructors we ignore their return type as they are surely different.
********************************************************************************************************/
	private boolean ignoreConstructorsForRenameClassRefactoring(String originalMethodName, String desiredMethodName) {
		
		if (CandidateClassforRenameRefactoring.renameClassRefactoringIsChecking) {
			
			if (Service.isConstructor(originalClassFullName, originalMethodName) && Service.isConstructor(desiredClassFullName, desiredMethodName))
				return true;
		}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: hasSimilarSimpleName
 * 			    This method only checks simple methods' name without checking their parameters.
********************************************************************************************************/
	private boolean hasSimilarSimpleName(String methodName1, String methodName2){
		
		methodName1 = methodName1.substring(0, methodName1.indexOf("(") + 1); 
		if (methodName2.startsWith(methodName1)) return true;
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: similarVariablesBothMethodsAccess
 * 			    This method checks calledVariableInformationMap and returns shared variables 
 * 				(it considers both positive and negative variables).
********************************************************************************************************/
	private SimilarityInformation similarVariablesBothMethodsAccess(String originalMethod, 
																	Map<String, Map<String, Set<VariablePair>>> originalCallingVariableMap,
		     									    				String desiredMethod, 
		     									    				Map<String, Map<String, Set<VariablePair>>> desiredCallingVariableMap) {
		
		SimilarityInformation result = new SimilarityInformation();
		
		Map<String, Set<VariablePair>> originalAccessVariable = null;
		Map<String, Set<VariablePair>> desiredAccessVariable = null;
		
		try {originalAccessVariable = originalCallingVariableMap.get(originalMethod);} catch (NullPointerException e) {}
		try {desiredAccessVariable = desiredCallingVariableMap.get(desiredMethod);} catch (NullPointerException e) {}
		
		if (originalAccessVariable == null && desiredAccessVariable == null) { 
			result.similarityValue = -100;
			return result;
		}
		
		if (originalAccessVariable == null || desiredAccessVariable == null) { 
			return result;
		}
		
		Set<VariablePair> originalVariableRead = originalAccessVariable.get("Read");
		Set<VariablePair> desiredVariableRead = desiredAccessVariable.get("Read");
		try { result.allEntities += originalVariableRead.size();} catch (NullPointerException e) {}
		try { result.allEntities += desiredVariableRead.size();} catch (NullPointerException e) {}
		
		if (originalVariableRead != null && desiredVariableRead != null) {
			
			Set<CloneVariablePair> desiredVariableReadClone = clone(desiredAccessVariable.get("Read"));
		
			//Find similar variables that both methods read.
			for (VariablePair readVariableOriginal : originalVariableRead) {
	
				for (CloneVariablePair readVariabledDesired : desiredVariableReadClone) {
					
					if (haveSimilarName_AND_Type(readVariableOriginal, readVariabledDesired)) {
						
						result.similarEntities += 2;
						desiredVariableReadClone.remove(readVariabledDesired);
						break;
					}
				}
			}
		}
		
		
		
		Set<VariablePair> originalVariableWrite = originalAccessVariable.get("Write");
		Set<VariablePair> desiredVariableWrite = desiredAccessVariable.get("Write");
		try { result.allEntities += originalVariableWrite.size();} catch (NullPointerException e) {}
		try { result.allEntities += desiredVariableWrite.size();} catch (NullPointerException e) {}
		
		if (originalVariableWrite != null && desiredVariableWrite != null) {
		
			Set<CloneVariablePair> desiredVariableWriteClone = clone(desiredAccessVariable.get("Write"));
			
			//Find similar variables that both methods write.
			for (VariablePair writeVariableOriginal : originalVariableWrite) {
	
				for (CloneVariablePair writeVariabledDesired : desiredVariableWriteClone) {
					
					if (haveSimilarName_AND_Type(writeVariableOriginal, writeVariabledDesired)) {
						
						result.similarEntities += 2;
						desiredVariableWriteClone.remove(writeVariabledDesired);
						break;
					}
				}
			}
		}
			
		int numberofDifferentVariables = result.allEntities - result.similarEntities;
		
		result.similarityValue = (result.similarEntities - numberofDifferentVariables) /2.0f;
		
		return result;
	}

/*******************************************************************************************************
 * Description: haveSimilarName_AND_Type()
********************************************************************************************************/
	private boolean haveSimilarName_AND_Type(VariablePair variablePair1, CloneVariablePair variablePair2){
		
		if (!variablePair1.variableName.equals(variablePair2.variableName)) return false;
		
		if (Service.hasSimilarType(variablePair1.variableType, variablePair2.variableType) == 1) return true;
		
		return false;
	}

/*******************************************************************************************************
 * Description: clone()
********************************************************************************************************/
	private Set<CloneVariablePair> clone(Set<VariablePair> accessedVariables){
			
		Set<CloneVariablePair> cloneSet =  new HashSet<CloneVariablePair>();
			
		try {
			for (VariablePair variablePair : accessedVariables) {
				cloneSet.add(new CloneVariablePair(variablePair.variableName, variablePair.variableType));
			}
		} catch(NullPointerException e) {}
			
		return cloneSet;
	}
	
/*******************************************************************************************************
 * Description: VariablePair
********************************************************************************************************/
	public class CloneVariablePair {
							
		public String variableName;
		public String variableType;
							
		public CloneVariablePair(String variableName, String variableType){ 
			this.variableName = variableName;
			this.variableType = variableType;
		}
	}
}