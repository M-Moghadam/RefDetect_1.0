package simulation.candidateRefactorings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import codeInformation.DesiredSourceInformation;
import codeInformation.Entity;
import codeInformation.Field;
import codeInformation.Method;
import codeInformation.SourceInformation;
import moea.problem.AdvancedProblem;
import moea.problem.RefactoringProblem;
import simulation.candidateRefactorings.DiffAlgorithm.Service;
import simulation.candidateRefactorings.ExtractInlineMethod.CandidateForExtractMethodRefactoring;
import simulation.candidateRefactorings.ExtractInlineMethod.CandidateForInlineMethodRefactoring;

public interface Facility {

/*******************************************************************************************************
 * Description: FieldIsValid()
********************************************************************************************************/
	default boolean FieldIsValid(Character entity) {
			
		return Service.fieldIsValid(entity);
	}
	
/*******************************************************************************************************
 * Description: createField()
********************************************************************************************************/
	default Entity createField(String entityName, Character entityAsCharacter, 
					           String classFullName, String designType) {

		String signature = simulation.fogsaa.Utility.getSignature(entityAsCharacter);
			
		Field field = getField(classFullName, entityName, designType);
		String fieldTypeFullName = field.getFieldTypeFullName();
			
		return new Field(entityName, signature, fieldTypeFullName, field.Clone_Modifier());
	}

/*******************************************************************************************************
 * Description: addFieldToMoveInMap()
********************************************************************************************************/
	default void addFieldToMoveInMap(String classFullName, List<Entity> entityList,
									 Map<String, List<Entity>> entitiesMoveInMap){
				
		entitiesMoveInMap.put(classFullName, entityList);
		
		AdvancedProblem.fieldsMoveInMap.put(classFullName, cloneFieldList(entityList));
	}
	
/*******************************************************************************************************
 * Description: addFieldToMoveFromMap()
********************************************************************************************************/
	default void addFieldToMoveFromMap(String classFullName, List<Entity> entityList, 
						  			   Map<String, List<Entity>> entitiesMoveFromMap){
				
		entitiesMoveFromMap.put(classFullName, entityList);
		
		AdvancedProblem.fieldsMoveFromMap.put(classFullName, cloneFieldList(entityList));
	}
	
/*******************************************************************************************************
 * Description: cloneFieldList()
********************************************************************************************************/
	private List<Entity> cloneFieldList(List<Entity> fieldList){
		
		List<Entity> temp = new ArrayList<Entity>();
		for (Entity entity : fieldList) {
			temp.add(new Field(entity.getName(), entity.getSignature(), ((Field)entity).getFieldTypeFullName(), entity.Clone_Modifier()));
		} 
		
		return temp;
	}
	
/*******************************************************************************************************
 * Description: getFieldTypeFullName()
********************************************************************************************************/
	private Field getField(String classFullName, String fieldName, String designType){
		
		SourceInformation sourceInformation = null;
		
		if (designType.equals("originalDesign")) 
			sourceInformation = RefactoringProblem.getInitialSourceInformation();
		
		else if (designType.equals("desiredDesign")) 
			sourceInformation = RefactoringProblem.getDesiredSourceInformation();
		
		if (sourceInformation == null) {
			System.out.println("Bug: " + designType + "is not exist");
			System.exit(0);
		}
		
		List<Field> fields = sourceInformation.getClassElementsMap().get(classFullName).fields;
		
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) return field;
		}
		
		System.out.println("Bug: field "+ fieldName + " is not exist");
		System.exit(0);
		
		return null;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////
	
/*******************************************************************************************************
 * Description: methodIsValid()
********************************************************************************************************/
	default boolean methodIsValid(Character entity) {
		
		return Service.methodIsValid(entity);
	}
	
/*******************************************************************************************************
 * Description: createMethod()
********************************************************************************************************/
	default Entity createMethod(String entityName, Character entityAsCharacter, 
								String classFullName, String designType) {

		String signature = simulation.fogsaa.Utility.getSignature(entityAsCharacter);
			
		Method method = getMethod(classFullName, entityName, designType);
		String methodReturnTypeFullName = method.getMethodReturnTypeFullName();

		return new Method(entityName, signature, methodReturnTypeFullName, method.Clone_Modifier());
	}
	
/*******************************************************************************************************
 * Description: addMethodToMoveInMap()
********************************************************************************************************/
	default void addMethodToMoveInMap(String classFullName, List<Entity> entityList,
								      Map<String, List<Entity>> entitiesMoveInMap){
					
		entitiesMoveInMap.put(classFullName, entityList);
		
		AdvancedProblem.methodsMoveInMap.put(classFullName, cloneMethodList(entityList));
	}
	
/*******************************************************************************************************
 * Description: addMethodToMoveFromMap()
********************************************************************************************************/
	default void addMethodToMoveFromMap(String classFullName, List<Entity> entityList, 
						  			    Map<String, List<Entity>> entitiesMoveFromMap){
			
		entitiesMoveFromMap.put(classFullName, entityList);
		
		AdvancedProblem.methodsMoveFromMap.put(classFullName, cloneMethodList(entityList));
	}
	
/*******************************************************************************************************
 * Description: cloneEntityList()
********************************************************************************************************/
	private List<Entity> cloneMethodList(List<Entity> methodList){
			
		List<Entity> temp = new ArrayList<Entity>();
		for (Entity entity : methodList) {
			temp.add(new Method(entity.getName(), entity.getSignature(), ((Method)entity).getMethodReturnTypeFullName(), entity.Clone_Modifier()));
		} 
			
		return temp;
	}
	
/*******************************************************************************************************
 * Description: getMethodReturnTypeFullName()
********************************************************************************************************/
	private Method getMethod(String classFullName, String methodName, String designType){
		
		SourceInformation sourceInformation = null;
		
		if (designType.equals("originalDesign")) 
			sourceInformation = RefactoringProblem.getInitialSourceInformation();
		
		else if (designType.equals("desiredDesign")) 
			sourceInformation = RefactoringProblem.getDesiredSourceInformation();
		
		if (sourceInformation == null) {
			System.out.println("Bug: " + designType + "is not exist");
			System.exit(0);
		}
		
		List<Method> methods = sourceInformation.getClassElementsMap().get(classFullName).methods;
		
		for (Method method : methods) {
			if (method.getName().equals(methodName)) return method;
		}
		
		System.out.println("Bug: method "+ methodName + " is not exist");
		System.exit(0);
		
		return null;
	}
	
/*******************************************************************************************************
 * Description: decideAboutChangeMethodSignature
********************************************************************************************************/
	default List<CandidateRenameEntity> decideAboutChangeMethodSignature(CandidateMethodRefactorings candidateMethodRefactorings, 
																		 Map<String, String> renameClassCandidates) {
		
		List<CandidateRenameEntity> newCandidateMethodsForRename = new ArrayList<CandidateRenameEntity>();
		
		List<CandidateRenameEntity> ChangeMethodParameter_FalsePositiveList = new ArrayList<CandidateRenameEntity>();
		
		for (int i = 0; i < candidateMethodRefactorings.candidatesForRenameMethodRef.size(); i++) {
			
			CandidateRenameEntity renameMethodCandidate = candidateMethodRefactorings.candidatesForRenameMethodRef.get(i);
		
			String oldEntityFullName = renameMethodCandidate.entityName;
			int index = oldEntityFullName.indexOf("(");
			String oldEntityName = oldEntityFullName.substring(0, index);
			String oldEntityParameters = oldEntityFullName.substring(index + 1, oldEntityFullName.lastIndexOf(")"));
			
			String newEntityFullName = renameMethodCandidate.entityNewName;
			index = newEntityFullName.indexOf("(");
			String newEntityName = newEntityFullName.substring(0, index);
			String newEntityParameters = newEntityFullName.substring(index + 1, newEntityFullName.lastIndexOf(")"));
			
			//It is a Rename Method candidate, and no need to process more.
			if (oldEntityParameters.equals(newEntityParameters)) continue;
			
			//Certainly it is a Change Method Signature candidate, but may be a Rename Method candidate as well.
			candidateMethodRefactorings.candidatesForRenameMethodRef.remove(i);
			i--;
			
			//It is also a Rename Method Candidate. Therefore, create a new Rename Method Candidate, but with different newMethod name
			if (!oldEntityName.equals(newEntityName)) {
			
				//Create a Rename Method candidate.
				CandidateRenameEntity candidateRenameMethod = new CandidateRenameEntity(renameMethodCandidate.originalClassFullName, 
																						renameMethodCandidate.targetClassFullName,											  		
																						oldEntityFullName, 
																						renameMethodCandidate.entitySignature,
																						renameMethodCandidate.entityTypeFullName,
																						newEntityName + "(" + oldEntityParameters + ")");
				addToList(newCandidateMethodsForRename, candidateRenameMethod);
			
				//We prefer first Rename and then Change Signature refactorings. Therefore, we passed newEntityName as oldEntityName 
				oldEntityFullName = newEntityName + "(" + oldEntityParameters + ")";
			}
			
			
			//Create a Change Method Signature candidate.
			CandidateRenameEntity candidateChangeMethodSignature = new CandidateRenameEntity(renameMethodCandidate.originalClassFullName, 
																						     renameMethodCandidate.targetClassFullName,											  		
																						     oldEntityFullName, 
																						     renameMethodCandidate.entitySignature,
																						     renameMethodCandidate.entityTypeFullName,
																						     newEntityFullName);

			//If the change is due to a Rename Class refactoring, then it is not a real change method parameter refactoring. 
			if (changeIsDueToRenameClass(candidateChangeMethodSignature, renameClassCandidates)) {
				
				ChangeMethodParameter_FalsePositiveList.add(candidateChangeMethodSignature);
				continue;
			}
			
			//Otherwise, create Change Method Parameter refactoring.
			
			candidateMethodRefactorings.candidatesForChangeMethodParametersRef.add(candidateChangeMethodSignature);
		}
		
		if (!newCandidateMethodsForRename.isEmpty())
			candidateMethodRefactorings.candidatesForRenameMethodRef.addAll(newCandidateMethodsForRename);
		
		return ChangeMethodParameter_FalsePositiveList;
	}
	
/*******************************************************************************************************
 * Description: changeIsDueToRenameClass()
********************************************************************************************************/
	private boolean changeIsDueToRenameClass(CandidateRenameEntity candidateChangeMethodSignature, 
											 Map<String, String> renameClassCandidates) {
	
		String currentName = candidateChangeMethodSignature.entityName;
		String newName = candidateChangeMethodSignature.entityNewName;
		
		String parts1 = currentName.substring(currentName.lastIndexOf("(") + 1, currentName.length() - 1);
		String parts2 = newName.substring(newName.lastIndexOf("(") + 1, newName.length() - 1);
		
		String[] split1 = parts1.split(",");
		String[] split2 = parts2.split(",");
		
		if (split1.length != split2.length) return false;
		
		for (int i = 0; i < split1.length; i++) {
			
			String par1 = split1[i];
			String par2 = split2[i];
			
			if (par1.equals(par2)) continue;
			
			if (par2.equals(renameClassCandidates.get(par1))) continue;
			
			if (ignoreDueToABugInSpoon(par1, par2)) continue;
						
			return false;
		}
		
		return true;
	}
	
/*******************************************************************************************************
 * Description: ignoreDueToABugInSpoon
********************************************************************************************************/
	private boolean ignoreDueToABugInSpoon(String oldEntityParameter, String newEntityParameter) {
		
		/** If at least one of classes belong to the program, then we check full name type.*/
		if (DesiredSourceInformation.classOrder.contains(newEntityParameter)) {
			if (!oldEntityParameter.equals(newEntityParameter)) return false;
		}
			
		int index1 = oldEntityParameter.lastIndexOf(".");
		if (index1 != -1) oldEntityParameter = oldEntityParameter.substring(index1 + 1);
			
		int index2 = newEntityParameter.lastIndexOf(".");
		if (index2 != -1) newEntityParameter = newEntityParameter.substring(index2 + 1);
			
		if (!oldEntityParameter.equals(newEntityParameter)) return false;
		
		return true;
	}

/*******************************************************************************************************
 * Description: addToList
 * 				If the refactoring is not in the list, add it to the list.
********************************************************************************************************/
	private void addToList(List<CandidateRenameEntity> candidatesForRenameMethod, CandidateRenameEntity newRenameMethodCandidate) {
		
		for (CandidateRenameEntity can : candidatesForRenameMethod) {
			
			if (can.entityName.equals(newRenameMethodCandidate.entityName) &&
			    can.entityNewName.equals(newRenameMethodCandidate.entityNewName) &&
			    can.entityTypeFullName.equals(newRenameMethodCandidate.entityTypeFullName) &&
			    can.originalClassFullName.equals(newRenameMethodCandidate.originalClassFullName) &&
			    can.targetClassFullName.equals(newRenameMethodCandidate.targetClassFullName)) {
				
				return;
			}
		}
		
		candidatesForRenameMethod.add(newRenameMethodCandidate);
	}
	
/*******************************************************************************************************
 * Description: decideAboutExtractInlineMethodRefactorings
********************************************************************************************************/
	default void decideAboutExtractInlineMethodRefactorings(CandidateMethodRefactorings candidateMethodRefactorings) {
		
		//As first step we filter false positive new and deleted entities.
		filterFalsePositiveNew_and_DeleteEntities(candidateMethodRefactorings);
		
		if (!candidateMethodRefactorings.newMethods.isEmpty()) {
				
			CandidateForExtractMethodRefactoring extractMethod = new CandidateForExtractMethodRefactoring();
			candidateMethodRefactorings.candidatesForExtractMethodRef = extractMethod.extractCandidaeForExtractMethod(candidateMethodRefactorings.newMethods);
		}
				
		if (!candidateMethodRefactorings.deletedMethods.isEmpty()) {

			CandidateForInlineMethodRefactoring inlineMethod = new CandidateForInlineMethodRefactoring();
			candidateMethodRefactorings.candidatesForInlineMethodRef = inlineMethod.extractCandidaeForInlineMethod(candidateMethodRefactorings.deletedMethods);

			//If a method is candidate for Inline Method refactoring, then remove it from deletedMethods list.
			for (CandidateExtractInlineMethod inlineMethodCandidate : candidateMethodRefactorings.candidatesForInlineMethodRef) {

				List<Entity> entities = candidateMethodRefactorings.deletedMethods.get(inlineMethodCandidate.originalClassFullName);

				try{for (Entity entity : entities) {

					if (entity.getName().equals(inlineMethodCandidate.entityName) && 
							entity.getSignature().equals(inlineMethodCandidate.entitySignature)) {

						entities.remove(entity);
						if (candidateMethodRefactorings.deletedMethods.get(inlineMethodCandidate.originalClassFullName).isEmpty())
							candidateMethodRefactorings.deletedMethods.remove(inlineMethodCandidate.originalClassFullName);
						break;
					}
				}}catch(NullPointerException e) {}
			}
		}
	}

/*******************************************************************************************************
 * Description: filterFalsePositiveNew_and_DeleteEntities
********************************************************************************************************/
	private void filterFalsePositiveNew_and_DeleteEntities(CandidateMethodRefactorings candidateMethodRefactorings) {
	
		for (CandidateRenameEntity candidateRef : candidateMethodRefactorings.candidatesForChangeMethodParametersRef) {
			
			if (!candidateRef.originalClassFullName.equals(candidateRef.targetClassFullName)) continue;
			
			String entityName = candidateRef.entityName.substring(0, candidateRef.entityName.indexOf("("));
			String entityNewName = candidateRef.entityNewName.substring(0, candidateRef.entityNewName.indexOf("("));
			
			if (!entityName.equals(entityNewName)) continue;
			
			List<Entity> newMethodCandidates = candidateMethodRefactorings.newMethods.get(candidateRef.originalClassFullName);
			if (newMethodCandidates != null) {
				for (Entity entity : newMethodCandidates) {
					if (entity.getName().equals(candidateRef.entityNewName)) {
						newMethodCandidates.remove(entity); 
						break;
					}
				}
			}
			
			List<Entity> deletedMethodCandidates = candidateMethodRefactorings.deletedMethods.get(candidateRef.originalClassFullName);
			if (deletedMethodCandidates != null) {
				for (Entity entity : deletedMethodCandidates) {
					if (entity.getName().equals(candidateRef.entityName)) {
						deletedMethodCandidates.remove(entity); 
						break;
					}
				}
			}
		}
	}
	
/*******************************************************************************************************
 * Description: updateRefactorings()
********************************************************************************************************/
	default void updateRefactorings(List<CandidateRenameEntity> ChangeMethodParameter_FalsePositiveList, 
									CandidateMethodRefactorings candidateMethodRefactorings) {
			
		for (CandidateRenameEntity flasePositiveInstance : ChangeMethodParameter_FalsePositiveList) {
				
			if (updateRefactorings(flasePositiveInstance, candidateMethodRefactorings.candidatesForMoveMethodRef)) continue;
				
			if (updateRefactorings(flasePositiveInstance, candidateMethodRefactorings.candidatesForPullUpMethodRef)) continue;
				
			if (updateRefactorings(flasePositiveInstance, candidateMethodRefactorings.candidatesForPushDownMethodRef)) continue;
		}
	}
		
/*******************************************************************************************************
 * Description: updateRefactorings()
 ********************************************************************************************************/
	default boolean updateRefactorings(CandidateRenameEntity flasePositiveInstance, List<CandidateEntity> candidatesForMoveMethodRef){
			
		String originalClassFullName = flasePositiveInstance.originalClassFullName;
		String targetClassFullName = flasePositiveInstance.targetClassFullName;
		String entityName = flasePositiveInstance.entityName;
		String entityNewName = flasePositiveInstance.entityNewName;
			
		for (CandidateEntity candidateRefactoring : candidatesForMoveMethodRef) {
				
			if (candidateRefactoring.originalClassFullName.equals(originalClassFullName) && 
				candidateRefactoring.targetClassFullName.equals(targetClassFullName) && 
				candidateRefactoring.entityName.equals(entityNewName)) {
					
				candidateRefactoring.entityName = entityName;
				return true;
			}
		}
			
		return false;
	}
}