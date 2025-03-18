package filter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import codeInformation.Call_Information.CallingMethods;
import moea.problem.AppliedRefactoringsInformation;
import codeInformation.ElementInformation;
import codeInformation.Field;
import codeInformation.SourceInformation;
import simulation.simulateRefactoring.Utility;

public class FilterMoveRenameEntities {

/*******************************************************************************************************
 * Description: isRefactoringValid()
********************************************************************************************************/
	public static boolean isRefactoringValid(AppliedRefactoringsInformation moveRenameInstance, 
						                     List<AppliedRefactoringsInformation> appliedRefactorings,
						                     SourceInformation initialSourceInformation, SourceInformation desiredSourceInformation) {
			
		String originalClassFullName = moveRenameInstance.originalClassFullName;
		String targetClassFullName = moveRenameInstance.targetClassFullName;
		
		String originalClassNewFullName = getNewClassFullName(originalClassFullName, appliedRefactorings);
		String targetClassOldFullName = getOldClassFullName(targetClassFullName, appliedRefactorings);
		
		// 1. Check inner classes: moving from outer class to inner class or vice versa is permitted.
		Map<String, Set<String>> nestedClasses = initialSourceInformation.getNestedClassMap();
		if (areNestedClasses(nestedClasses, originalClassFullName, targetClassOldFullName)) {
			return true;
		}
		
		nestedClasses = desiredSourceInformation.getNestedClassMap();
		if (areNestedClasses(nestedClasses, targetClassFullName, originalClassNewFullName)) {
			return true;
		}
		
		//2. Check filed type: if each class is defined as a type of a filed in another class, then moving is permitted.
		Map<String, ElementInformation> classElementMap = initialSourceInformation.getClassElementsMap();
		if (relatedThroughFieldDeclaration(classElementMap, originalClassFullName, targetClassOldFullName)) {
			return true;
		}
		
		classElementMap = desiredSourceInformation.getClassElementsMap();
		if (relatedThroughFieldDeclaration(classElementMap, targetClassFullName, originalClassNewFullName)) {
			return true;
		}
		
		//3. Check hierarchy structure: if classes belong to the same hierarchy structure, then moving is permitted.
		if (areInSimilarHierarchyStructure(initialSourceInformation, originalClassFullName, targetClassOldFullName)) {
			return true;
		}
		
		if (areInSimilarHierarchyStructure(desiredSourceInformation, originalClassNewFullName, targetClassFullName)) {
			return true;
		}
		
		//4. Check existing of the original class: if the original class is completely deleted, then moving is permitted.
		List<String> classes = desiredSourceInformation.getClassOrder();
		if (!(classes.contains(originalClassFullName) || classes.contains(originalClassNewFullName))) {
			return true;
		}
		
		//5. Check the entity usage before refactoring: if the entity is used in the target class before refactoring, then moving is permitted. We can extend that to classes in the hierarchy structure (super and subclass of the original class) but for now ignore that.
		try {
		    Map<String, Map<String, Map<String, CallingMethods>>> calledInformationMap = initialSourceInformation.get_CallInformation().getCalledInformationMap();
			String entityOldName = moveRenameInstance.originalEntityName;
			Map<String, Map<String, CallingMethods>> classInformationMap = calledInformationMap.get(originalClassFullName);
			Map<String, CallingMethods> callingClass = classInformationMap.get(entityOldName);
			CallingMethods callingMethods = callingClass.get(targetClassOldFullName);
			if (!callingMethods.nameList.isEmpty()) {
				return true;
			}
		} catch(NullPointerException e) {}
		

		//6. Check usage after refactoring: if the entity is used in a method in the original class, then moving is permitted. We can extend that to classes in the hierarchy structure (super and subclass of the original class) but for now ignore that.
		try {
		    Map<String, Map<String, Map<String, CallingMethods>>> calledInformationMap = desiredSourceInformation.get_CallInformation().getCalledInformationMap();
			String entityNewName = moveRenameInstance.targetEntityName;
			Map<String, Map<String, CallingMethods>> classInformationMap = calledInformationMap.get(targetClassFullName);
			Map<String, CallingMethods> callingClass = classInformationMap.get(entityNewName);
			CallingMethods callingMethods = callingClass.get(originalClassNewFullName);
			if (!callingMethods.nameList.isEmpty()) {
				return true;
			}
		} catch(NullPointerException e) {}
		
		
		//7. Check the entity usage after refactoring: if the entity is used in the target class in a method which is moved too, then moving is permitted. 
		try {
			Map<String, Map<String, Map<String, CallingMethods>>> calledInformationMap = desiredSourceInformation.get_CallInformation().getCalledInformationMap();
			String entityNewName = moveRenameInstance.targetEntityName;
			Map<String, Map<String, CallingMethods>> classInformationMap = calledInformationMap.get(targetClassFullName);
			Map<String, CallingMethods> callingClass = classInformationMap.get(entityNewName);
			CallingMethods callingMethods = callingClass.get(targetClassFullName);
			
			for (String callingMethod : callingMethods.nameList) {
				
				for (AppliedRefactoringsInformation refactoring : appliedRefactorings) {
					
					if (refactoring.originalClassFullName.equals(originalClassFullName)) {
						if (refactoring.targetClassFullName.equals(targetClassFullName)) {
							if (refactoring.originalEntityName.equals(callingMethod) || refactoring.targetEntityName.equals(callingMethod)) { 
								return true;
							}
						}
					}
				}
				
				
			}
		} catch(NullPointerException e) {}
		
		

		//8. Check the method parameters before refactoring: If the target class is defined as parameter of the method, then moving is permitted.  
		if (relatedThroughMethodParameter(moveRenameInstance.originalEntityName, targetClassOldFullName)) {
			return true;
		}
		
		//9. Check the method parameters after refactoring: If the original class is defined as parameter of the method, then moving is permitted. 
		if (relatedThroughMethodParameter(moveRenameInstance.targetEntityName, originalClassNewFullName)) {
			return true;
		}
		
		//10. if it is a new class
		//if (!initialSourceInformation.getClassOrder().contains(targetClassOldFullName)) {
		//	return true;
		//}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: relatedThroughMethodParameter()
********************************************************************************************************/
	private static boolean relatedThroughMethodParameter(String entityName, String classFullName) {
		
		int index = entityName.indexOf("(");
		if (index == -1) return false;
		
		String className = classFullName.substring(classFullName.lastIndexOf(".") + 1);
		
		String parameters = entityName.substring(index + 1, entityName.length() - 1);
		
		for (String parameter : parameters.split(",")) {
			if (parameter.equals(classFullName) || parameter.equals(className)) {
				return true;
			}
		}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: areInSimilarHierarchyStructure()
********************************************************************************************************/
	private static boolean areInSimilarHierarchyStructure(SourceInformation sourceInformation, 
														  String classFullName1, String classFullName2){
		
		Map<String, String> classParentMap = sourceInformation.getClassParentMap();
		Map<String, Set<String>> interfaceClassMap = sourceInformation.getInterfaceClassMap();
		
		if (isParent(classFullName1, classFullName2, classParentMap, interfaceClassMap)) return true;
		
		if (isParent(classFullName2, classFullName1, classParentMap, interfaceClassMap)) return true;
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: isParent()
********************************************************************************************************/
	private static boolean isParent(String child, String parent, 
							        Map<String, String> parentsMap, Map<String, Set<String>> interfaceClassMap) {
		
		if (Utility.getParent(child, parent, parentsMap)) return true;
			
		try {
			
			for (String classChild : interfaceClassMap.get(parent)) 
				if (classChild.equals(child)) 
					return true;
		}catch (NullPointerException e) {} 
			
		
		return false;
	}

/*******************************************************************************************************
 * Description: relatedThroughFieldDeclaration()
********************************************************************************************************/
	private static boolean relatedThroughFieldDeclaration(Map<String, ElementInformation> classElementMap, 
														  String classFullName1, String classFullName2) {
	
		ElementInformation elementInformation = classElementMap.get(classFullName1);
		for (Field field : elementInformation.fields) {

			if (field.getFieldTypeFullName().equals(classFullName2)) {
				return true;
			}
		}
	
		return false;
	}

/*******************************************************************************************************
 * Description: areNestedClasses()
********************************************************************************************************/
	private static boolean areNestedClasses(Map<String, Set<String>> nestedClasses, 
											String classFullName1, String classFullName2) {
		
		Set<String> set = nestedClasses.get(classFullName1);
		String className2 = classFullName2.substring(classFullName2.lastIndexOf(".") + 1);
		if (set != null && set.contains(className2)) return true;
		
		set = nestedClasses.get(classFullName2);
		String className1 = classFullName1.substring(classFullName1.lastIndexOf(".") + 1);
		if (set != null && set.contains(className1)) return true;
		
		return false;
	}

/*******************************************************************************************************
 * Description: getNewClassFullName()
********************************************************************************************************/
	private static String getNewClassFullName(String originalClassFullName, List<AppliedRefactoringsInformation> appliedRefactorings) {
		
		for (AppliedRefactoringsInformation refactoring : appliedRefactorings) {
			
			String refactoringType = refactoring.refactoringType;
			
			if (refactoringType.equals("RenameClass") || refactoringType.equals("MoveClass") || refactoringType.equals("MoveAndRenameClass")) {
				
				if (refactoring.originalClassFullName.equals(originalClassFullName)) {
					
					return refactoring.targetClassFullName;
				}
			}
		}
		
		return originalClassFullName;
	}

/*******************************************************************************************************
 * Description: getOldClassFullName()
********************************************************************************************************/
	private static String getOldClassFullName(String newClassFullName, List<AppliedRefactoringsInformation> appliedRefactorings) {
		
		for (AppliedRefactoringsInformation refactoring : appliedRefactorings) {
			
			String refactoringType = refactoring.refactoringType;
			
			if (refactoringType.equals("RenameClass") || refactoringType.equals("MoveClass") || refactoringType.equals("MoveAndRenameClass")) {
				
				if (refactoring.targetClassFullName.equals(newClassFullName)) { 
					
					return refactoring.originalClassFullName;
				}
			}
		}
		
		return newClassFullName;
	}
}
