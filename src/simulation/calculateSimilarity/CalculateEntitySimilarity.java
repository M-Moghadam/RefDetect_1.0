package simulation.calculateSimilarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moea.problem.AdvancedProblem;
import moea.problem.RefactoringProblem;
import codeInformation.Call_Information;
import codeInformation.DesiredSourceInformation;
import codeInformation.Call_Information.CalledInformation;
import codeInformation.Call_Information.CallingMethods;
import codeInformation.Call_Information.Pair;
import simulation.CandidateRenameClassRefactoring.CandidateClassforRenameRefactoring;
import simulation.candidateRefactorings.CandidateFieldRefactorings;
import simulation.candidateRefactorings.CandidateMethodRefactorings;
import simulation.candidateRefactorings.CandidateRefactorings;
import simulation.candidateRefactorings.MatchEntities;
import simulation.candidateRefactorings.DiffAlgorithm.Service;
import simulation.simulateRefactoring.Utility;
import codeInformation.Entity;

public abstract class CalculateEntitySimilarity {
	
	final Call_Information originalCallInformation;
	final Call_Information desiredCallInformation;
	
	private final Map<String, Map<String, CallingMethods>> originalCalledEntityMap;
	private final Map<String, Map<String, CallingMethods>> desiredCalledEntityMap;
	
	final Map<String, CalledInformation> originalCallingEntityMap;
	final Map<String, CalledInformation> desiredCallingEntityMap;
	
	final String originalClassFullName;
	final String desiredClassFullName;
	
/*******************************************************************************************************
 * Description: CalculateEntitySimilarity()
********************************************************************************************************/
	public CalculateEntitySimilarity(String originalClassFullName, String desiredClassFullName) {
		
		this.originalClassFullName = originalClassFullName;
		this.desiredClassFullName = desiredClassFullName;
		
		originalCallInformation = RefactoringProblem.getInitialSourceInformation().get_CallInformation();
		desiredCallInformation = RefactoringProblem.getDesiredSourceInformation().get_CallInformation();
		
		originalCallingEntityMap = originalCallInformation.getCallingInformationMap().get(originalClassFullName);
		desiredCallingEntityMap = desiredCallInformation.getCallingInformationMap().get(desiredClassFullName);
		
		Map<String, Map<String, Map<String, CallingMethods>>> originalCallInformationMap = originalCallInformation.getCalledInformationMap();
		originalCalledEntityMap = originalCallInformationMap.get(originalClassFullName);

		Map<String, Map<String, Map<String, CallingMethods>>> desiredCallInformationMap = desiredCallInformation.getCalledInformationMap();
		desiredCalledEntityMap = desiredCallInformationMap.get(desiredClassFullName);
	}
	
/*******************************************************************************************************
 * Description: calculateSimilarity()
********************************************************************************************************/
	abstract void calculateSimilarity(Entity originalEntity, Entity desiredEntity, 
								      Map<String, CallingMethods> originalCallingClass,
		    						  Map<String, CallingMethods> desiredCallingClass,
		    						  TemporarySimilarityClass temporarySimilarity,
		    						  DeletedNewEntities delete_And_NewEntities);
	
/*******************************************************************************************************
 * Description: calculateSimilarity()
 * 				
********************************************************************************************************/
	public List<MatchEntities> calculateSimilarity(List<Entity> originalEntities, List<Entity> desiredEntities,
												   String originalClassFullName, String desiredClassFullName,								       
												   float nameSimilarityImportance, float entitySimilarityThreshold) {
			
		DeletedNewEntities delete_And_NewEntities = new DeletedNewEntities();
		
		SimilarityClass similarityClass = new SimilarityClass();
		
		for (Entity originalEntity : originalEntities) {
 			
			TemporarySimilarityClass temporarySimilarity = new TemporarySimilarityClass();

			Map<String, CallingMethods> originalCallingClass = null;
			if (originalCalledEntityMap != null)
				originalCallingClass = originalCalledEntityMap.get(originalEntity.getName());
			
			for (Entity desiredEntity : desiredEntities) {
				
				Map<String, CallingMethods> desiredCallingClass = null;
				if (desiredCalledEntityMap != null)
					desiredCallingClass = desiredCalledEntityMap.get(desiredEntity.getName());
				
				calculateSimilarity(originalEntity, desiredEntity, originalCallingClass, 
									desiredCallingClass, temporarySimilarity, delete_And_NewEntities);
				
				if (originalEntity.getName().equals(desiredEntity.getName())) {
					
					Map<String, String> renameClassCandidates = AdvancedProblem.getRenameClassCandidates();
					
					if (renameClassCandidates == null) continue; 
					
					String renamedClassFullName = renameClassCandidates.get(originalClassFullName);
					
					if (renamedClassFullName != null && renamedClassFullName.equals(desiredClassFullName)) break;
				}
			}
			
			//Now we remove those which have similarity less than "entitySimilarityThreshold".
			removeExtraEntities(temporarySimilarity, originalEntity, similarityClass,
								nameSimilarityImportance, entitySimilarityThreshold);
		}
		
		List<MatchEntities> matchedEntitiesList = new ArrayList<MatchEntities>();
		
		//Now create the match list
		for (int i = 0; i < similarityClass.originalEntities.size(); i++) {
			
			matchedEntitiesList.add(new MatchEntities(originalClassFullName, 
												      desiredClassFullName, 
												      similarityClass.originalEntities.get(i), 
												      similarityClass.desiredEntities.get(i), 
												      similarityClass.normalizedSimilarityValues.get(i),
												      similarityClass.realRelationshipSimilarityValue.get(i)));
		}	
		
		return matchedEntitiesList;
	}

/*******************************************************************************************************
 * Description: removeExtraEntities()
********************************************************************************************************/
	private static void removeExtraEntities(TemporarySimilarityClass temporarySimilarity, 
								    		Entity originalEntity, SimilarityClass similarityClass,
								    		float nameSimilarityImportance, float entitySimilarityThreshold){
		
		if (temporarySimilarity.desiredEntities.isEmpty()) return;
		
		//First find maximum value for call relationship.
		float max = Collections.max(temporarySimilarity.relationshipSimilarityValues);
		
		if (max < 0) max = 0;
		
		float callSimilarityImportance = 1 - nameSimilarityImportance;
		
		float sum = 0; int counter = 0;
		for (int i = 0; i < temporarySimilarity.hasSimilarName.size(); i++) {
			
			int myInt = temporarySimilarity.hasSimilarName.get(i) ? 1 : 0;
			float callSimilarity = 0;
			if (max != 0) callSimilarity = temporarySimilarity.relationshipSimilarityValues.get(i) / max;
			
			float entitySimilarity = (nameSimilarityImportance * myInt) + (callSimilarityImportance * callSimilarity); 
			
			temporarySimilarity.finalSimilarityValue.add(i, entitySimilarity);
			
			if (entitySimilarity > entitySimilarityThreshold) {
				counter++;
				sum += temporarySimilarity.finalSimilarityValue.get(i);
			}
		}
		
		if (counter != 0) {
			entitySimilarityThreshold = sum / counter;
		}
		
		for (int i = 0; i < temporarySimilarity.hasSimilarName.size(); i++) {
		
			float entitySimilarity = temporarySimilarity.finalSimilarityValue.get(i);
			
			if (entitySimilarity >= entitySimilarityThreshold) {
				
				similarityClass.originalEntities.add(originalEntity);
				similarityClass.desiredEntities.add(temporarySimilarity.desiredEntities.get(i));
				similarityClass.normalizedSimilarityValues.add(entitySimilarity);
				similarityClass.realRelationshipSimilarityValue.add(temporarySimilarity.relationshipSimilarityValues.get(i));
			}
		}
	}	

/*******************************************************************************************************
 * Description: similar_Fields_Methods_That_CallBothEntities
********************************************************************************************************/
	SimilarityInformation similar_Fields_Methods_That_CallBothEntities(String entityName1, String entityName2,
										               				   Map<String, CallingMethods> originalCallingClass, 
										               				   Map<String, CallingMethods> desiredCallingClass,
										               				   DeletedNewEntities delete_And_NewEntities){
		
		SimilarityInformation result = new SimilarityInformation();
		
		//If no methods call entities, then we assume they are the same.
		if (originalCallingClass == null && desiredCallingClass == null) {
			
			result.similarityValue = -100;
			return result;
		}
		
		if (originalCallingClass == null) { 
			originalCallingClass = new HashMap<String, CallingMethods>();
		}
		if (desiredCallingClass == null) {
			desiredCallingClass = new HashMap<String, CallingMethods>();
		}
		
		boolean countOnlyPositiveRelationship = isThereSimilarcandidateInHierarchyStructure(entityName1, originalClassFullName, 
																			                entityName2, desiredClassFullName);
		
		int numberofEntities = 0;
		int numberofDeletedEntities = 0;
		int numberofNewEntities = 0;
		
		//As first step counts number of methods that called the desired entity.
		List<String> movedMethods = new ArrayList<>();
		for (String desiredCallingClassFullName : desiredCallingClass.keySet()) {
			
			numberofEntities += desiredCallingClass.get(desiredCallingClassFullName).nameList.size();
			result.allEntities += desiredCallingClass.get(desiredCallingClassFullName).nameList.size();
			
			//Also if we are in the first round, we save methods that called the desired entity, but themselves are in MoveInList
			if (!CandidateRefactorings.secondRound) {
				
				for (String callingMethod : desiredCallingClass.get(desiredCallingClassFullName).nameList) {
				
					if (entityIsMovedIn(callingMethod, desiredCallingClassFullName))
						movedMethods.add(callingMethod);
				} 
			}
		}
		
		
		Map<String, List<String>> cloneDesiredCallingClass = clone(desiredCallingClass);
		
		//Find similar methods which call both entities.
		for (String originalCallingClassFullName : originalCallingClass.keySet()) {
			
			for (String originalCallingMethod : originalCallingClass.get(originalCallingClassFullName).nameList) {
				
				numberofEntities++;
				result.allEntities++;
				
				boolean find = false;
				
				label: for (String desiredCallingClassFullName : cloneDesiredCallingClass.keySet()) {
					
					List<String> desiredCallingMethods = cloneDesiredCallingClass.get(desiredCallingClassFullName);
					
					for (int i = 0; i < desiredCallingMethods.size(); i++) {
						
						String desiredCallingMethod = desiredCallingMethods.get(i);
		
						if (Service.similarCalled_or_CallingEntities(originalCallingMethod, originalCallingClassFullName, 
										 		    				 desiredCallingMethod, desiredCallingClassFullName)) {
							
							result.similarEntities += 2;
							desiredCallingMethods.remove(i);
							find = true;
							
							movedMethods.remove(desiredCallingMethod);
							
							break label;
						}
					}
				}
				
				/** If no similar entity is found and we are in the first round, and also if 
				 * 	"originalCallingMethod" is itself in MoveFromList, then do not count its negative effect.*/
				if (!(CandidateRefactorings.secondRound || find)) {
					
					if (entityIsMovedFrom(originalCallingMethod, originalCallingClassFullName))
						numberofEntities--;
					
					continue;
				}
				
				//We do not check relationship with methods which are deleted from the original design. Note in the first round it is empty
				else if (!countOnlyPositiveRelationship && !find && entityIsDeleted(originalCallingMethod, originalCallingClassFullName, delete_And_NewEntities.deletedMethods)) {
					numberofDeletedEntities++;
				}
			}
		}
		
		if (CandidateRefactorings.secondRound && (!countOnlyPositiveRelationship) && delete_And_NewEntities.newMethods != null && (!delete_And_NewEntities.newMethods.isEmpty())) {

			for (String desiredCallingClassFullName : cloneDesiredCallingClass.keySet()) {
				
				List<String> desiredCallingMethods = cloneDesiredCallingClass.get(desiredCallingClassFullName);
				
				for (int i = 0; i < desiredCallingMethods.size(); i++) {
					
					//We do not check relationship with methods which are new in the desired design.
					if (entityIsNew(desiredCallingMethods.get(i), desiredCallingClassFullName, delete_And_NewEntities.newMethods)) {
						numberofNewEntities++;
					}
				}
			}
		}
		

		//Note in the second round movedMethod is empty and its size is zero.
		numberofEntities -= movedMethods.size();
		
		int numberofDifferentEntities = 0;
		
		/** If methods in a hierarchy structure are compared, then no need to count different entities. It helps to detect a
		 *  case when a method is pushed down to more than one class, or more than one method are pulled up to a superclass.*/
		if (!countOnlyPositiveRelationship)
			numberofDifferentEntities = numberofEntities - result.similarEntities - numberofDeletedEntities - numberofNewEntities;
		
		result.similarityValue = (result.similarEntities - numberofDifferentEntities) / 2.0f;
		
		return result;
	}
	
/*******************************************************************************************************
 * Description: hasMinimumSimilarity()
 * 				Two fields should have at least 25% similar entities.
 * 				
 * 				This method is not check when entities of two class are checked in a Rename Class refactoring.
********************************************************************************************************/
	boolean hasMinimumSimilarity(SimilarityInformation ...similarityInfo){
			
		if (CandidateClassforRenameRefactoring.renameClassRefactoringIsChecking == true) return true;
			
		int allEntities = 0;
		int similarEntities = 0;
			
		for (SimilarityInformation info : similarityInfo) {
			allEntities += info.allEntities;
			similarEntities += info.similarEntities;
		}
			
		if (((similarEntities * 100.0) / allEntities) > 35 ) return true;
			
		return false;
	}
	
/*******************************************************************************************************
 * Description: hasStrongSimilarity()
********************************************************************************************************/
	boolean hasStrongSimilarity(SimilarityInformation ...similarityInfo){
						
		int allEntities = 0;
		int similarEntities = 0;
						
		for (SimilarityInformation info : similarityInfo) {
			allEntities += info.allEntities;
			similarEntities += info.similarEntities;
		}
						
		if (((similarEntities * 100.0) / allEntities) > 60) return true;
			
		return false;
	}
	
/*******************************************************************************************************
 * Description: similar_Fields_Methods_That_BothEntitiesCall
********************************************************************************************************/
	SimilarityInformation similar_Fields_Methods_That_BothEntitiesCall(String originalEntity, 
																	   Map<String, CalledInformation> originalCallingEntityMap,
																	   String desiredMethod, 
																	   Map<String, CalledInformation> desiredCallingEntityMap,
																	   DeletedNewEntities delete_And_NewEntities) {
		SimilarityInformation result = new SimilarityInformation();
		
		CalledInformation originalCalledEntities = null;
		CalledInformation desiredCalledEntities = null;
		
		try {originalCalledEntities = originalCallingEntityMap.get(originalEntity);} catch (NullPointerException e) {}
		try {desiredCalledEntities = desiredCallingEntityMap.get(desiredMethod);} catch (NullPointerException e) {}
		
		if (originalCalledEntities == null && desiredCalledEntities == null) { 
			result.similarityValue = -100;
			return result;
		}
		
		if (originalCalledEntities == null || desiredCalledEntities == null) { 
			return result;
		}
		
		int numberofDeletedEntities = 0;
		int numberofNewEntities = 0;
		
		int numberofEntities = 0;
		
		//Find similar fields which both entities call.
		
		//First if we are in the first round, we save fields in the desired entity which are called, but themselves are in MoveInList
		List<String> movedFields = new ArrayList<>();
		if (!CandidateRefactorings.secondRound) {
						
			for (Pair calledField : desiredCalledEntities.calledFields) {
				if (entityIsMovedIn(calledField.calledEntityName, calledField.entityClassFullName))
					movedFields.add(calledField.calledEntityName);
			}
		}
		
		List<ClonePair> calledFieldsDesired = cloneEntity(desiredCalledEntities.calledFields);
		for (Pair calledFieldOriginal : originalCalledEntities.calledFields) {

			numberofEntities++;
			result.allEntities++;
			boolean find = false;
			
			for (ClonePair calledFieldDesired : calledFieldsDesired) {
				
				if (Service.similarCalled_or_CallingEntities(calledFieldOriginal.calledEntityName, calledFieldOriginal.entityClassFullName, 
										    				 calledFieldDesired.calledEntityName, calledFieldDesired.entityClassFullName)) {
					
					result.similarEntities += 2;
					calledFieldsDesired.remove(calledFieldDesired);
					find = true;
					
					movedFields.remove(calledFieldDesired.calledEntityName);
					
					break;
				}
			}
			
			/** If no similar entity is found and we are in the first round, and also if 
			 * 	"calledFieldOriginal" is itself in MoveFromList, then do not count its negative effect.*/
			if (!(CandidateRefactorings.secondRound || find)) {
				
				if (entityIsMovedFrom(calledFieldOriginal.calledEntityName, calledFieldOriginal.entityClassFullName))
					numberofEntities--;
				
				continue;
			}
			
			//We do not check relationship with fields which are deleted from the original design.
			else if (!find && entityIsDeleted(calledFieldOriginal.calledEntityName, calledFieldOriginal.entityClassFullName, delete_And_NewEntities.deletedFields)) {
				numberofDeletedEntities++;
			}
		}
			
		if (CandidateRefactorings.secondRound && delete_And_NewEntities.newFields != null && (!delete_And_NewEntities.newFields.isEmpty())) {
			
			for (ClonePair calledFieldDesired : calledFieldsDesired) {
			
				//We do not check relationship with fields which are new in the desired design.
				if (entityIsNew(calledFieldDesired.calledEntityName, calledFieldDesired.entityClassFullName, delete_And_NewEntities.newFields)) {
					numberofNewEntities++;
				}
			}
		}
		
		
		//Find similar methods which both entities call.
		
		//First if we are in the first round, we save methods in the desired entity which are called, but themselves are in MoveInList
		List<String> movedMethods = new ArrayList<>();
		if (!CandidateRefactorings.secondRound) {
				
			for (Pair calledMethod : desiredCalledEntities.calledMethods) {
				if (entityIsMovedIn(calledMethod.calledEntityName,calledMethod.entityClassFullName))
					movedMethods.add(calledMethod.calledEntityName);
			}
		}
		
		List<ClonePair> calledMethodsDesired = cloneEntity(desiredCalledEntities.calledMethods);
		for (Pair calledMethodOriginal : originalCalledEntities.calledMethods) {
			
			numberofEntities++;
			result.allEntities++;
			boolean find = false;
			
			for (ClonePair calledMethodDesired : calledMethodsDesired) {
				
				if (Service.similarCalled_or_CallingEntities(calledMethodOriginal.calledEntityName, calledMethodOriginal.entityClassFullName, 
								 		    				 calledMethodDesired.calledEntityName, calledMethodDesired.entityClassFullName)) {
					
					result.similarEntities += 2;
					calledMethodsDesired.remove(calledMethodDesired);
					find = true;
					
					movedMethods.remove(calledMethodDesired.calledEntityName);
					
					break;
				}
			}
			
			/** If no similar entity is found and we are in the first round, and also if 
			 * 	"calledMethodOriginal" is itself in MoveFromList, then do not count its negative effect.*/
			if (!(CandidateRefactorings.secondRound || find)) {
				
				if (entityIsMovedFrom(calledMethodOriginal.calledEntityName, calledMethodOriginal.entityClassFullName))
					numberofEntities--;
				
				continue;
			}

			//We do not check relationship with methods which are deleted from the original design.
			else if (!find && entityIsDeleted(calledMethodOriginal.calledEntityName, calledMethodOriginal.entityClassFullName, delete_And_NewEntities.deletedMethods)) {
				numberofDeletedEntities++;
			}
		}
			
		if (CandidateRefactorings.secondRound && delete_And_NewEntities.newMethods != null && (!delete_And_NewEntities.newMethods.isEmpty())) {
			
			for (ClonePair calledMethodDesired : calledMethodsDesired) {
				
				//We do not check relationship with methods which are new in the desired design.
				if (entityIsNew(calledMethodDesired.calledEntityName, calledMethodDesired.entityClassFullName, delete_And_NewEntities.newMethods)) {
					numberofNewEntities++;
				}
			}
		}
			
		//Note in the second round movedMethods and movedFields is empty and its size is zero.
		numberofEntities -= movedMethods.size() + movedFields.size();	
		numberofEntities += desiredCalledEntities.calledFields.size() + desiredCalledEntities.calledMethods.size();
		result.allEntities += desiredCalledEntities.calledFields.size() + desiredCalledEntities.calledMethods.size();
		
		int numberofDifferentEntities = numberofEntities - result.similarEntities - numberofDeletedEntities - numberofNewEntities;
		
		result.similarityValue = ((result.similarEntities - numberofDifferentEntities) / 2.0f);
		
		return result;
	}
	
/*******************************************************************************************************
 * Description: cloneEntity()
********************************************************************************************************/
	private List<ClonePair> cloneEntity(List<Pair> calledEntities){
			
		List<ClonePair> cloneList =  new ArrayList<ClonePair>();
			
		for (Pair calledEntity : calledEntities) {
			cloneList.add(new ClonePair(calledEntity.calledEntityName, calledEntity.entityClassFullName));
		}
			
		return cloneList;
	}
	
/*******************************************************************************************************
 * Description: ClonePair
********************************************************************************************************/
	public class ClonePair {
						
		public String calledEntityName;
		public String entityClassFullName;
						
		public ClonePair(String calledEntityName, String entityClassFullName){ 
			this.calledEntityName = calledEntityName;
			this.entityClassFullName = entityClassFullName;
		}
	}
	
/*******************************************************************************************************
 * Description: entityIsMovedFrom()
*********************************************************************************************************/
	private boolean entityIsMovedFrom(String entityName, String classFullName) {
		
		//To ignore a case that entity is used by constructor, but it is moved and is not any more called by the constructor
		if (Service.isConstructor(classFullName, entityName)) return true;
		
		List<Entity> movedEntities = null;
		if (entityName.indexOf(")") != -1)
			 movedEntities = AdvancedProblem.methodsMoveFromMap.get(classFullName);
		else movedEntities = AdvancedProblem.fieldsMoveFromMap.get(classFullName);
		
		try {
			for (Entity entity : movedEntities) {
				if (entity.getName().equals(entityName)) return true;
			}
		}catch(NullPointerException e) { }	
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: entityIsMovedIn()
*********************************************************************************************************/
	private boolean entityIsMovedIn(String entityName, String classFullName) {
		
		//To ignore a case that entity is used by constructor, but it is moved and is not any more called by the constructor
		if (Service.isConstructor(classFullName, entityName)) return true;
		
		List<Entity> movedEntities = null;
			
		if (entityName.indexOf(")") != -1)
			movedEntities = AdvancedProblem.methodsMoveInMap.get(classFullName);
		else movedEntities = AdvancedProblem.fieldsMoveInMap.get(classFullName);
			
		try {
			for (Entity entity : movedEntities) {
				if (entity.getName().equals(entityName)) return true;
			}
		}catch(NullPointerException e) { }	
			
		return false;
	}
	
/*******************************************************************************************************
 * Description: isThereSimilarcandidateInHierarchyStructure()
 * 				If class1 and class2 belong to a hierarchy structure and there is another similar candidate
 * 				for pushdown or pull up then return true.
*********************************************************************************************************/
	private boolean isThereSimilarcandidateInHierarchyStructure(String entityName1, String classFullName1, 
												  				String entityName2, String classFullName2) {

		Map<String, String> initialClassParentMap = RefactoringProblem.getInitialSourceInformation().getClassParentMap();
		
		//If classFullName2 is parent of classFullName1 in the initial or desired design
		if (Utility.getParent(classFullName1, classFullName2, initialClassParentMap) || Utility.getParent(classFullName1, classFullName2, DesiredSourceInformation.classParentMap)) {
			
			int index = entityName1.lastIndexOf("(");
			if (index != -1) {
				
				entityName1 = entityName1.substring(0, index + 1);
			
				return isThereSimilarcandidateInHierarchyStructure(classFullName2, entityName1, classFullName1, 
															       AdvancedProblem.methodsMoveFromMap);
			}
			
			return isThereSimilarcandidateInHierarchyStructure(classFullName2, entityName1, classFullName1, 
					       									   AdvancedProblem.fieldsMoveFromMap);
		}
		
		
		
		//If classFullName1 is parent of classFullName2 in the initial or desired design
		if (Utility.getParent(classFullName2, classFullName1, initialClassParentMap) || Utility.getParent(classFullName2, classFullName1, DesiredSourceInformation.classParentMap)) {
			
			int index = entityName1.lastIndexOf("(");
			if (index != -1) {
						
				entityName1 = entityName1.substring(0, index + 1);
					
				return isThereSimilarcandidateInHierarchyStructure(classFullName1, entityName1, classFullName2, 
																   AdvancedProblem.methodsMoveInMap);
			}
					
			return isThereSimilarcandidateInHierarchyStructure(classFullName1, entityName1, classFullName2, 
							       							   AdvancedProblem.fieldsMoveInMap);
		}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: isThereSimilarcandidateInHierarchyStructure()
********************************************************************************************************/
	private boolean isThereSimilarcandidateInHierarchyStructure(String superClass, String entityName, String ignoreClassFullName,
																Map<String, List<Entity>> candidateEntitiess){
		
		Map<String, String> initialClassParentMap = RefactoringProblem.getInitialSourceInformation().getClassParentMap();
		
		for (String clazz : candidateEntitiess.keySet()) {
			
			//We do not consider target class.
			if (clazz.equals(ignoreClassFullName)) continue;
			
			if (Utility.getParent(clazz, superClass, initialClassParentMap) || Utility.getParent(clazz, superClass, DesiredSourceInformation.classParentMap)) {
				
				for (Entity entity : candidateEntitiess.get(clazz)) {
					
					String candidateEntityName = entity.getName();
					
					int index = candidateEntityName.lastIndexOf("(");
					if (index != -1) 
						candidateEntityName = candidateEntityName.substring(0, candidateEntityName.lastIndexOf("(") + 1);
						
					//We ignore method parameters and only consider method name
					if (entityName.equals(candidateEntityName))
						return true;
				}
			}
		}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: clone()
********************************************************************************************************/
	private Map<String, List<String>> clone(Map<String, CallingMethods> desiredCallingClass){
		
		Map<String, List<String>> clone = new HashMap<String, List<String>>();
		
		for (String callingClass : desiredCallingClass.keySet()) {
			
			List<String> cloneList = new ArrayList<String>();
			for (String callingMethod : desiredCallingClass.get(callingClass).nameList) {
				cloneList.add(callingMethod);
			}
			clone.put(callingClass, cloneList);	
		}
		
		return clone;		
	}

/*******************************************************************************************************
 * Description: similarSimpleName()
 * 				This method only checks entities' names. For method is checks its input parameters as well.
********************************************************************************************************/
	boolean similarSimpleName(String originalEntityName, String originalClassFullName, 
							  String desiredEntityName, String desiredClassFullName) {
		
		char type = 'F';
		if (originalEntityName.indexOf(")") != -1) 
			type = 'M'; 
		
		if (originalEntityName.equals(desiredEntityName)) return true;
		
		if (type == 'M' && Service.similarNames(originalEntityName, desiredEntityName)) return true; 
		
		return Service.matchedBefore(originalEntityName, originalClassFullName, desiredEntityName, desiredClassFullName, type);
	}
	
/*******************************************************************************************************
 * Description: entityIsDeleted()
********************************************************************************************************/
	private boolean entityIsDeleted(String entityName, String entityClassFullName, Map<String, List<Entity>> deletedEntities) {
		
		//To ignore a case that entity is used by constructor, but it is moved and is not any more called by the constructor
		if (Service.isConstructor(entityClassFullName, entityName)) return true;
		
		try{for (Entity deleteEntity : deletedEntities.get(entityClassFullName)) {
					
			if (deleteEntity.getName().equals(entityName)) return true;
			
		}}catch(NullPointerException e) {
			//We cover null pointer exception.
		}
			
		return false;
	}
	
/*******************************************************************************************************
 * Description: entityIsNew()
********************************************************************************************************/
	private boolean entityIsNew(String entityName, String entityClassFullName, Map<String, List<Entity>> newEntities) {
		
		//To ignore a case that entity is used by constructor, but it is moved and is not any more called by the constructor
		if (Service.isConstructor(entityClassFullName, entityName)) return true;
		
		try{for (Entity newEntity : newEntities.get(entityClassFullName)) {
				
			if (newEntity.getName().equals(entityName)) return true;
					
		}}catch(NullPointerException e) {
				//We cover null pointer exception.
		}
				
		return false;
	}
}

/*******************************************************************************************************
 * Description: SimilarityClass
********************************************************************************************************/
	class SimilarityClass {
		
		List<Entity> originalEntities;
		List<Entity> desiredEntities;
		List<Float> normalizedSimilarityValues;
		List<Float> realRelationshipSimilarityValue;
		
		public SimilarityClass(){
			originalEntities = new ArrayList<Entity>();
			desiredEntities = new ArrayList<Entity>();
			normalizedSimilarityValues = new ArrayList<Float>();
			realRelationshipSimilarityValue = new ArrayList<Float>();
		}
	}
	
/*******************************************************************************************************
 * Description: TemporarySimilarityClass
********************************************************************************************************/
	class TemporarySimilarityClass {
			
		List<Entity> desiredEntities;
		List<Boolean> hasSimilarName;
		List<Float> relationshipSimilarityValues;
		List<Float> finalSimilarityValue;
			
		public TemporarySimilarityClass(){
			desiredEntities = new ArrayList<Entity>();
			hasSimilarName = new ArrayList<Boolean>();
			relationshipSimilarityValues = new ArrayList<Float>();
			finalSimilarityValue = new ArrayList<Float>();
		}
	}

/*******************************************************************************************************
* Description: DeletedNewEntities
********************************************************************************************************/
	class DeletedNewEntities {
		
		private CandidateFieldRefactorings candidateFields;
		Map<String, List<Entity>> deletedFields = null, newFields = null;
		
		private CandidateMethodRefactorings candidateMethods;
		Map<String, List<Entity>> deletedMethods = null, newMethods = null;
		
/*******************************************************************************************************
 * Description: DeletedNewEntities()
********************************************************************************************************/
		DeletedNewEntities() {
			
			try{
				candidateFields = AdvancedProblem.getInitialCandidateFieldRefactorings();
				deletedFields = candidateFields.deletedFields;
				newFields = candidateFields.newFields;
			}catch(NullPointerException e){}
			
			try{
				candidateMethods = AdvancedProblem.getInitialCandidateMethodRefactorings();
				deletedMethods = candidateMethods.deletedMethods;
				newMethods = candidateMethods.newMethods;
			}catch(NullPointerException e){}
		}
	}

/*******************************************************************************************************
 * Description: SimilarityInformation
********************************************************************************************************/
	class SimilarityInformation {
		
		int allEntities;
		int similarEntities;
		float similarityValue;
		
		public SimilarityInformation() {
			allEntities = 0;
			similarEntities = 0;
			similarityValue = 0;
		}
	}
	