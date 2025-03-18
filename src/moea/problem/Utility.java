package moea.problem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import codeInformation.Call_Information;
import codeInformation.Call_Information.CalledInformation;
import codeInformation.Call_Information.Pair;
import codeInformation.Call_Information.VariablePair;
import codeInformation.ElementInformation;
import codeInformation.SourceInformation;
import filter.FilterMoveRenameEntities;
import simulation.candidateRefactorings.DiffAlgorithm.Service;
import simulation.simulateRefactoring.ChangeMethodParameters;
import simulation.simulateRefactoring.DeleteClass;
import simulation.simulateRefactoring.DeleteField;
import simulation.simulateRefactoring.DeleteMethod;
import simulation.simulateRefactoring.ExtractClass;
import simulation.simulateRefactoring.ExtractInterface;
import simulation.simulateRefactoring.ExtractSubClass;
import simulation.simulateRefactoring.InlineMethod;
import simulation.simulateRefactoring.MoveField;
import simulation.simulateRefactoring.MoveMethod;
import simulation.simulateRefactoring.PullUpField;
import simulation.simulateRefactoring.PullUpMethod;
import simulation.simulateRefactoring.PushDownField;
import simulation.simulateRefactoring.PushDownMethod;
import simulation.simulateRefactoring.Refactoring;
import simulation.simulateRefactoring.RenameField;
import simulation.simulateRefactoring.RenameMethod;
import simulation.simulateRefactoring_RefDetect.ExtractMethod;
import simulation.simulateRefactoring_RefDetect.ExtractSuperClass;
import simulation.simulateRefactoring_RefDetect.RenameClass;

public class Utility {

/*******************************************************************************************************
 * Description: getRefactringInformation()
********************************************************************************************************/
	public static List<AppliedRefactoringsInformation> getRefactringInformation(List<Refactoring> refactorings) {
		return getRefactringInformation(null, null, refactorings);
	}
	
/*******************************************************************************************************
 * Description: getRefactringInformation()
********************************************************************************************************/
	public static List<AppliedRefactoringsInformation> getRefactringInformation(SourceInformation initialSourceInformation, 
																			    SourceInformation resultedSourceInformation,
																			    List<Refactoring> refactorings) {
		
		List<AppliedRefactoringsInformation> returnList = new ArrayList<AppliedRefactoringsInformation>();
		
		//This variable contain Full name of classes that are deleted by this method. For example, an Extract Class that not enough entity is moved to that.
		Set<String> removedClasses = new HashSet<String>();
		
		Map<String, String> allRenameClassRefactorings = getAllRenameClassCandidates(refactorings);
		
		for (Refactoring refactoring : refactorings) {
			
			AppliedRefactoringsInformation appliedRef = new AppliedRefactoringsInformation();
			
			appliedRef.refactoringType = refactoring.getClass().getSimpleName();
			
			//We do not consider DeleteClassT DeleteMethod and DeleteField refactorings.
			if (appliedRef.refactoringType.equals("DeleteClass") || appliedRef.refactoringType.equals("DeleteMethod") || appliedRef.refactoringType.equals("DeleteField"))
				continue;
			
			returnList.add(appliedRef);
			switch (appliedRef.refactoringType) {

			case "DeleteClass":
				DeleteClass deleteClass = (DeleteClass)refactoring;
				appliedRef.originalClassFullName = deleteClass.getOriginalClassFullName();
				appliedRef.description = "DeleteClass: Class " + getSimpleName(appliedRef.originalClassFullName) + " is deleted";
				break;
				
			case "DeleteMethod":
				DeleteMethod deleteMethod = (DeleteMethod)refactoring;
				appliedRef.originalClassFullName = deleteMethod.originalClassFullName;
				appliedRef.originalEntityName = getMethodName(deleteMethod.methodName);
				appliedRef.description = "DeleteMethod: Method " + appliedRef.originalEntityName + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is deleted";
				break;
				

			case "DeleteField":
				DeleteField deleteField = (DeleteField)refactoring;
				appliedRef.originalClassFullName = deleteField.originalClassFullName;
				appliedRef.originalEntityName = deleteField.fieldName;
				appliedRef.description = "DeleteField: Field " + getSimpleName(appliedRef.originalEntityName) + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is deleted";
				break;
			
			case "InlineMethod": 
				InlineMethod inlineMethod = (InlineMethod)refactoring;
				appliedRef.originalClassFullName = inlineMethod.originalClassFullName;
				appliedRef.originalEntityName = getMethodName(inlineMethod.methodName);
				appliedRef.targetClassFullName = inlineMethod.getTargetClassFullName();
				appliedRef.targetEntityName = getMethodName(inlineMethod.getTargetMethodName());
				if (!appliedRef.originalClassFullName.equals(appliedRef.targetClassFullName))
					appliedRef.refactoringType = "MoveAndInlineMethod";
				appliedRef.description = appliedRef.refactoringType + ": Method " + appliedRef.originalEntityName + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is inlined to method " + appliedRef.targetEntityName + " in class " + getSimpleName(appliedRef.targetClassFullName) + "";
				break;
				

			case "ExtractMethod": 
				ExtractMethod extractMethod = (ExtractMethod)refactoring;
				appliedRef.originalClassFullName = extractMethod.originalClassFullName;
				appliedRef.originalEntityName = getMethodName(extractMethod.getOriginalMethodName());
				appliedRef.targetClassFullName = extractMethod.getTargetClassFullName();
				appliedRef.targetEntityName = getMethodName(extractMethod.methodName);
				if (!appliedRef.originalClassFullName.equals(appliedRef.targetClassFullName))
					appliedRef.refactoringType = "ExtractAndMoveMethod";
				appliedRef.description = appliedRef.refactoringType + ": Method " + appliedRef.targetEntityName + " in class " + getSimpleName(appliedRef.targetClassFullName) + " is extracted from method " + appliedRef.originalEntityName + " in class " + getSimpleName(appliedRef.originalClassFullName) + "";
				
				//It is possible that the refactoring is an Encapsulate Field. So, we check this and if it is an Encapsulate Field, we change its description.
				//updateRefactoringTypeIfEncapsulateField(appliedRef, extractMethod);
				
				break;


			case "ChangeMethodParameters":
				ChangeMethodParameters changeMethodParameters = (ChangeMethodParameters)refactoring;
				appliedRef.originalClassFullName = changeMethodParameters.originalClassFullName;
				appliedRef.originalEntityName = getMethodName(changeMethodParameters.methodName);
				appliedRef.targetEntityName = getMethodName(changeMethodParameters.getNewMethodName());
				appliedRef.targetClassFullName = changeMethodParameters.targetClassFullName;
				appliedRef.description = "ChangeMethodParameters: Parameters of method " + appliedRef.originalEntityName + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is changed to " + appliedRef.targetEntityName + "";
				break;
			

			case "RenameMethod":
				RenameMethod renameMethod = (RenameMethod)refactoring;
				appliedRef.originalClassFullName = renameMethod.originalClassFullName;
				appliedRef.originalEntityName = getMethodName(renameMethod.methodName);
				appliedRef.targetEntityName = getMethodName(renameMethod.getNewMethodName());
				appliedRef.targetClassFullName = renameMethod.targetClassFullName;
				appliedRef.description = "RenameMethod: Method " + appliedRef.originalEntityName + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is renamed to " + appliedRef.targetEntityName + "";
				break;
			

			case "RenameField":
				RenameField renameField = (RenameField)refactoring;
				appliedRef.originalClassFullName = renameField.originalClassFullName;
				appliedRef.originalEntityName = renameField.fieldName;
				appliedRef.targetEntityName = renameField.getNewFieldName();
				appliedRef.description = "RenameField: Field " + getSimpleName(appliedRef.originalEntityName) + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is rename to " + getSimpleName(appliedRef.targetEntityName) + "";
				break;
				

			case "RenameClass":
				
				RenameClass renameClass = (RenameClass)refactoring;
				appliedRef.originalClassFullName = renameClass.getOriginalClassFullName();
				appliedRef.targetClassFullName = renameClass.getNewClassFullName();
				String type = DecideAboutRefactoringType(appliedRef.originalClassFullName, renameClass.getNewClassFullName());
				appliedRef.refactoringType = type + "Class";
				
				//If an inner class is moved, we ignore that if its outer class is moved as well.
				if (innerClassIsEffectedButItIsNotRefactoring(appliedRef.originalClassFullName, appliedRef.targetClassFullName, allRenameClassRefactorings)) { 
					returnList.remove(appliedRef); break; 
				}
				
				appliedRef.description = appliedRef.refactoringType + ": Class " + appliedRef.originalClassFullName + " is " + type + " to " + appliedRef.targetClassFullName + "";
				break;
				


			case "ExtractSuperClass": 
				ExtractSuperClass extractSuperClass = (ExtractSuperClass)refactoring;
				appliedRef.originalClassFullName = extractSuperClass.getNewClassFullName();
				
				//We only accept Extract SuperClass that at least one of its child class belongs to the initial design. It is done to have similar results as RMiner
				List<String> childrenClassesSuperClass = new ArrayList<String>();
				List<String> classOrder = null;
				try{classOrder = initialSourceInformation.getClassOrder();}catch(NullPointerException e) {}
				for (String childClass : extractSuperClass.getChildrenClassesFullName()) {
					if (classOrder == null || classOrder.contains(childClass)) 
						childrenClassesSuperClass.add(childClass);
				}
				if (childrenClassesSuperClass.isEmpty()) { returnList.remove(appliedRef); break; }
				
				Collections.sort(childrenClassesSuperClass);
				
				String subclasses1 = "";
				for (String subClassName : childrenClassesSuperClass) {
					subclasses1 += getSimpleName(subClassName) + ",";
					appliedRef.targetClassFullName +=  subClassName + ",";
				}
				subclasses1 = subclasses1.substring(0, subclasses1.length() - 1);;
				appliedRef.targetClassFullName = appliedRef.targetClassFullName.substring(0, appliedRef.targetClassFullName.length() - 1);;
				appliedRef.description = "ExtractSuperClass: Superclass " + getSimpleName(appliedRef.originalClassFullName) + " is extracted from children classes " + subclasses1 + "";
				break;
			
			
				
			case "ExtractSubClass":
				ExtractSubClass extractSubClass = (ExtractSubClass)refactoring;
				appliedRef.originalClassFullName = extractSubClass.getNewClassFullName();
				appliedRef.targetClassFullName = extractSubClass.getParentClassFullName();
				appliedRef.description = "Extract SubClass: Subclass " + getSimpleName(appliedRef.originalClassFullName) + " is extracted from parent class " + getSimpleName(appliedRef.targetClassFullName) + "";
				break;
				
			
			case "ExtractClass": 
				ExtractClass extractClass = (ExtractClass)refactoring;
				appliedRef.originalClassFullName = extractClass.getNewClassFullName();
				appliedRef.targetClassFullName = extractClass.getOriginalClassFullName();
				
				/** We only accept Extract Class that at least two entities (fields and methods) are moved to the newly created class, except
				    it is an inner class. In this case, we accept moving at least one entity if the entity is moved from the outer class.*/
				if (resultedSourceInformation != null) {
					
					Map<String, ElementInformation> elementMap = resultedSourceInformation.getClassElementsMap();
					ElementInformation elementInformation = elementMap.get(appliedRef.originalClassFullName);
					
					if (elementInformation == null) { returnList.remove(appliedRef); removedClasses.add(appliedRef.originalClassFullName); break; }

					int size = elementInformation.fields.size() + elementInformation.methods.size();
						
					if (size == 0) { returnList.remove(appliedRef);	removedClasses.add(appliedRef.originalClassFullName); break; }
					
					//Exception for inner class - moving at least one entity to an inner class is enough.
					if (size == 1 && (!appliedRef.originalClassFullName.startsWith(appliedRef.targetClassFullName))) { returnList.remove(appliedRef); removedClasses.add(appliedRef.originalClassFullName);	break;}
				}
				
				appliedRef.description = "ExtractClass: Class " + getSimpleName(appliedRef.originalClassFullName) + " is extracted from class " + getSimpleName(appliedRef.targetClassFullName) + "";
				break;
				

			case "ExtractInterface": 
				ExtractInterface extractInterface = (ExtractInterface)refactoring;
				appliedRef.originalClassFullName = extractInterface.getNewClassFullName();
				
				//We only accept Extract Interface that at least one of its child class belongs to the initial design. It is done to have similar results as RMiner
				List<String> childrenClassesInterface = new ArrayList<String>();
				classOrder = null;
				try{classOrder = initialSourceInformation.getClassOrder();}catch(NullPointerException e) {}
				for (String childClass : extractInterface.getChildrenClassesFullName()) {
					if (classOrder == null || classOrder.contains(childClass)) 
						childrenClassesInterface.add(childClass);
				}
				if (childrenClassesInterface.isEmpty()) { returnList.remove(appliedRef); removedClasses.add(appliedRef.originalClassFullName); break; }
				
				Collections.sort(childrenClassesInterface);
				
				String subclasses2 = "";
				for (String subClassName : childrenClassesInterface) {
					subclasses2 += getSimpleName(subClassName) + ",";
					appliedRef.targetClassFullName +=  subClassName + ",";
				}
				subclasses2 = subclasses2.substring(0, subclasses2.length() - 1);;
				appliedRef.targetClassFullName = appliedRef.targetClassFullName.substring(0, appliedRef.targetClassFullName.length() - 1);;
				appliedRef.description = "ExtractInterface: Interface " + getSimpleName(appliedRef.originalClassFullName) + " is extracted from classes " + subclasses2 + "";
				break;


			case "PullUpField": 
				PullUpField pullUpField = (PullUpField)refactoring;
				appliedRef.originalClassFullName = pullUpField.originalClassFullName;
				appliedRef.originalEntityName = pullUpField.fieldName;
				appliedRef.targetClassFullName = pullUpField.getParentClassFullName();
				appliedRef.description = "PullUpField: Field " + getSimpleName(appliedRef.originalEntityName) + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is pulled up to class " + getSimpleName(appliedRef.targetClassFullName) + "";
				break;
				

			case "PullUpMethod": 
				PullUpMethod pullUpMethod = (PullUpMethod)refactoring;
				appliedRef.originalClassFullName = pullUpMethod.originalClassFullName;
				appliedRef.originalEntityName = getMethodName(pullUpMethod.methodName);
				appliedRef.targetClassFullName = pullUpMethod.getParentClassFullName();
				appliedRef.description = "PullUpMethod: Method " + appliedRef.originalEntityName + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is pulled up to class " + getSimpleName(appliedRef.targetClassFullName) + "";
				break;
				

			case "PushDownField": 
				PushDownField pushDownField = (PushDownField)refactoring;
				appliedRef.originalClassFullName = pushDownField.originalClassFullName;
				appliedRef.originalEntityName = pushDownField.fieldName;
				
				List<String> childrenClassesField = pushDownField.getChildClassesFullName();
				appliedRef.targetClassFullName = getSimpleName(childrenClassesField.get(0));
				appliedRef.description = "PushDownField: Field " + appliedRef.originalEntityName + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is pushed down to class " + getSimpleName(appliedRef.targetClassFullName) + "";

				for (int i = 1; i < childrenClassesField.size(); i++) {
					AppliedRefactoringsInformation newAppliedRef = new AppliedRefactoringsInformation();
					returnList.add(newAppliedRef);
					newAppliedRef.refactoringType = appliedRef.refactoringType;
					newAppliedRef.originalClassFullName = pushDownField.originalClassFullName;
					newAppliedRef.originalEntityName = pushDownField.fieldName;
					newAppliedRef.targetClassFullName = getSimpleName(childrenClassesField.get(i));
					newAppliedRef.description = "PushDownField: Field " + getSimpleName(newAppliedRef.originalEntityName) + " in class " + getSimpleName(newAppliedRef.originalClassFullName) + " is pushed down to class " + newAppliedRef.targetClassFullName + "";
				}
				break;
				

			case "PushDownMethod": 
				PushDownMethod pushDownMethod = (PushDownMethod)refactoring;
				appliedRef.originalClassFullName = pushDownMethod.originalClassFullName;
				appliedRef.originalEntityName = getMethodName(pushDownMethod.methodName);
				
				List<String> childrenClassesMethod = pushDownMethod.getChildClassesFullName();
				appliedRef.targetClassFullName = getSimpleName(childrenClassesMethod.get(0));
				appliedRef.description = "PushDownMethod: Method " + appliedRef.originalEntityName + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is pushed down to class " + appliedRef.targetClassFullName + "";
				
				for (int i = 1; i < childrenClassesMethod.size(); i++) {
					AppliedRefactoringsInformation newAppliedRef = new AppliedRefactoringsInformation();
					returnList.add(newAppliedRef);
					newAppliedRef.refactoringType = appliedRef.refactoringType;
					newAppliedRef.originalClassFullName = pushDownMethod.originalClassFullName;
					newAppliedRef.originalEntityName = getMethodName(pushDownMethod.methodName);
					newAppliedRef.targetClassFullName = getSimpleName(childrenClassesMethod.get(i));
					newAppliedRef.description = "PushDownMethod: Method " + newAppliedRef.originalEntityName + " in class " + getSimpleName(newAppliedRef.originalClassFullName) + " is pushed down to class " + newAppliedRef.targetClassFullName + "";
				}
				break;


			case "MoveField": 
				MoveField moveField = (MoveField)refactoring;
				appliedRef.originalClassFullName = moveField.originalClassFullName;
				appliedRef.originalEntityName = moveField.fieldName;
				appliedRef.targetClassFullName = moveField.getTargetClassFullName();
				appliedRef.description = "MoveField: Field " + getSimpleName(appliedRef.originalEntityName) + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is moved to class " + getSimpleName(appliedRef.targetClassFullName) + "";
				break;
							

			case "MoveMethod":
				MoveMethod moveMethod = (MoveMethod)refactoring;
				appliedRef.originalClassFullName = moveMethod.originalClassFullName;
				appliedRef.originalEntityName = getMethodName(moveMethod.methodName);
				appliedRef.targetClassFullName = moveMethod.getTargetClassFullName();
				appliedRef.description = "MoveMethod: Method " + appliedRef.originalEntityName + " in class " + getSimpleName(appliedRef.originalClassFullName) + " is moved to class " + getSimpleName(appliedRef.targetClassFullName) + "";
				break;
							
				
			default:
				System.out.println("Error: Refactoring Type " + appliedRef.refactoringType + " Is Not Supported.");
				System.exit(0);
			}
		}
		
		reviseList(returnList, removedClasses);
		
		return returnList;
	}

/*******************************************************************************************************
 * Description: getAllRenameClassCandidates
********************************************************************************************************/
	private static Map<String, String> getAllRenameClassCandidates(List<Refactoring> refactorings) {
		
		Map<String, String> result = new HashMap<String, String>();
		
		for (Refactoring refactoring : refactorings) {
			
			if (refactoring instanceof RenameClass) {
				result.put(((RenameClass) refactoring).getOriginalClassFullName(), ((RenameClass) refactoring).getNewClassFullName());
			}
		}
		
		return result;
	}

/*******************************************************************************************************
 * Description: innerClassIsEffectedButItIsNotRefactoring
********************************************************************************************************/
	private static boolean innerClassIsEffectedButItIsNotRefactoring(String originalClassFullName, String targetClassFullName, 
																	 Map<String, String> allRenameClassRefactorings) {	
		
		int index1 = originalClassFullName.lastIndexOf(".");
		 
		 String outerClassFullName1;
		 String className1 = "";
		 
		 if (index1 != -1) {
			 outerClassFullName1 = originalClassFullName.substring(0, index1);
			 className1 = originalClassFullName.substring(index1 + 1);
		 }
		 else {outerClassFullName1 = originalClassFullName;}
		
		
		 int index2 = targetClassFullName.lastIndexOf(".");
		 
		 if (index1 == index2 && index1 == -1) return false;
		 
		 String outerClassFullName2;
		 String className2 = "";
		 
		 if (index2 != -1) {
			 outerClassFullName2 = targetClassFullName.substring(0, index2);
			 className2 = targetClassFullName.substring(index2 + 1);
		 }
		 else {outerClassFullName2 = targetClassFullName;}

		if (!className1.equals(className2)) return false;
		
		//We know both classes have similar name.
		
		try{ if (allRenameClassRefactorings.get(outerClassFullName1).equals(outerClassFullName2)) return true;} catch (NullPointerException e) {}
		
		return false;
	}
		
/*******************************************************************************************************
 * Description: reviseList
********************************************************************************************************/
	private static void reviseList(List<AppliedRefactoringsInformation> appliedRefactorings, Set<String> removedClasses) {
		
		removeUnCompletedRefactorings(appliedRefactorings, removedClasses);
		
		CreateRefactoringsRelated2MoveField(appliedRefactorings);

		CreateRefactoringsRelated2MoveMethod(appliedRefactorings);
		
		Mix_Rename_Move_ChangeMethodParameters(appliedRefactorings);
		
		updateEncapsulateFieldRefactoring(appliedRefactorings);
	}
	
/*******************************************************************************************************
 * Description: updateEncapsulateFieldRefactoring()
 * 				If Encapsulate Field refactoring happens, and it results in both getter and setter, then 
 * 				we will have two Encapsulate Field refactorings. If that is the case then we need to mix 
 * 				them as one refactoring.
********************************************************************************************************/
	private static void updateEncapsulateFieldRefactoring(List<AppliedRefactoringsInformation> appliedRefactorings) {
		
		for (int i = 0; i < appliedRefactorings.size(); i++) {
			
			AppliedRefactoringsInformation ref1 = appliedRefactorings.get(i);
			
			if (!ref1.refactoringType.startsWith("EncapsulateField")) continue;
			
			int index = ref1.description.indexOf(",");
			String des1 = ref1.description.substring(0, index);
			
			String getterMethodName = (ref1.description.contains(", getterMethodName: ") ? ref1.description.substring(index + 20) : "null");
			String setterMethodName = (ref1.description.contains(", setterMethodName: ") ? ref1.description.substring(index + 20) : "null");
			
			for (int j = i + 1; j < appliedRefactorings.size(); j++) {
				
				AppliedRefactoringsInformation ref2 = appliedRefactorings.get(j);
				
				//If reference to field are replaced in more than one place with getter or setter then we will have more than one Encapsulate Field. Therefore, we need to delete the extra ones.
				if (ref1.description.equals(ref2.description)) {
					
					appliedRefactorings.remove(j);
					j--;
					continue;
				}
				
				if (!ref2.refactoringType.startsWith("EncapsulateField")) continue;
				
				String des2 = ref2.description.substring(0, ref2.description.indexOf(","));

				if (!des1.equals(des2)) continue;
				
				if (ref2.description.contains(", getterMethodName: ")) getterMethodName = ref2.description.substring(index + 20);
				else if (ref2.description.contains(", setterMethodName: ")) setterMethodName = ref2.description.substring(index + 20);
				
				appliedRefactorings.remove(j);
				j--;
			}
			
			ref1.description = des1 + ", getterMethodName: " + getterMethodName + ", setterMethodName: " + setterMethodName + "";
		}
	}
	
/*******************************************************************************************************
 * Description: updateRefactoringTypeIfEncapsulateField()
 * 				For Extract Method refactorings if we have the below conditions then we conclude the 
 * 				applied refactoring is Encapsulate Field and not Extract Method. Note that RMiner also 
 * 				checks the similar conditions:
 * 				1. Both the original class and the target class are the same				
 * 				2. A field should be accessed inside the method				
 * 				3. The method should return void (for setter) or should return field type for getter.
 * 				4. The method has no parameter for getter or has one parameter of type field for setter.
 * 				5. Only the field should be accessed inside the method body. No other fields, variables 
 * 				   or method should be called inside the method. 
 * 				   For setter it have access to one variable that is its input parameter.
 * 				
********************************************************************************************************/
	private static void updateRefactoringTypeIfEncapsulateField(AppliedRefactoringsInformation appliedRef, 
																ExtractMethod extractMethod) {
		
		String originalClassFullName = appliedRef.originalClassFullName;
		String targetClassFullName = appliedRef.targetClassFullName;
		
		//Condition 1
		if (!originalClassFullName.equals(targetClassFullName)) return;
		
		SourceInformation desiredSourceInformation = RefactoringProblem.getDesiredSourceInformation();
		
		Call_Information call_Information = desiredSourceInformation.get_CallInformation();
		
		Map<String, Map<String, CalledInformation>> callingInformationMap = call_Information.getCallingInformationMap();
		
		//Condition 2
		if (callingInformationMap == null) return;
		
		Map<String, CalledInformation> callingMethod = callingInformationMap.get(targetClassFullName);
		
		if (callingMethod == null) return;
		
		CalledInformation calledInformation = callingMethod.get(extractMethod.methodName);
		
		if (calledInformation == null) return;
		
		List<Pair> calledFields = calledInformation.calledFields;
		
		if (calledFields == null || calledFields.size() != 1) return;
		
		Pair calledField = calledFields.get(0);
		
		if (!calledField.entityClassFullName.equals(originalClassFullName)) return;
		
		//Condition 3 and 4
		String methodReturnType = extractMethod.methodReturnType;
		String methodSigniture = extractMethod.methodSignature;

		if (methodReturnType.equals("void") && (!methodSigniture.equals("MP"))) return;
				
		if ((!methodReturnType.equals("void")) && (!methodSigniture.equals("M"))) return;
		
		//condition 5
		if (!isVariableAccessValid(call_Information.getCalledVariableInformationMap(), extractMethod, targetClassFullName, methodReturnType)) return;
		
		//It is probably an Encapsulate Field refcatoring.
		appliedRef.refactoringType = "EncapsulateField";
		appliedRef.originalEntityName = calledField.calledEntityName;
		appliedRef.targetEntityName = calledField.calledEntityName;
		
		String description = "EncapsulateField: The Encapsulate Field refactoring has been applied to the Field " + calledField.calledEntityName + " in class " + getSimpleName(targetClassFullName);
		if (methodReturnType.equals("void")) appliedRef.description = description + ", setterMethodName: " + getMethodName(extractMethod.methodName) + "";
		else appliedRef.description = description + ", getterMethodName: " + getMethodName(extractMethod.methodName) + "";
	}
	
/*******************************************************************************************************
 * Description: isVariableAccessValid()
********************************************************************************************************/
	private static boolean isVariableAccessValid(Map<String, Map<String, Map<String, Set<VariablePair>>>> calledVariableInformationMap, 
										         ExtractMethod extractMethod, String classFullName, String methodReturnType){
		
		int numberofCalledVariables = getNumberofCalledVariables(calledVariableInformationMap, extractMethod, classFullName);
		
		if (numberofCalledVariables == 0 && (!methodReturnType.equals("void"))) return true;
		
		if (numberofCalledVariables == 1 && methodReturnType.equals("void")) return true;
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: callAnyVariable()
********************************************************************************************************/
	private static int getNumberofCalledVariables(Map<String, Map<String, Map<String, Set<VariablePair>>>> calledVariableInformationMap, 
										   			  ExtractMethod extractMethod, String classFullName){
			
		if (calledVariableInformationMap == null) return 0;
			
		Map<String, Map<String, Set<VariablePair>>> calledVariablesMap = calledVariableInformationMap.get(classFullName);
			
		if (calledVariablesMap == null) return 0;
			
		Map<String, Set<VariablePair>> calledVariables = calledVariablesMap.get(extractMethod.methodName);
			
		if (calledVariables == null) return 0;
			
		//At least one variable is called.
		
		return calledVariables.values().size();
	}
	
	private static void removeUnCompletedRefactorings(List<AppliedRefactoringsInformation> appliedRefactorings, Set<String> removedClasses) {
		
		//To cover a case that the refactoring that creates the class is removed from the applied refactoring list, and so the refactorings dependent on that will not be applied in the program any more.
		for (int i = 0; i < appliedRefactorings.size(); i++) {
			
			AppliedRefactoringsInformation appliedRefactoring = appliedRefactorings.get(i);
			
			if (removedClasses.contains(appliedRefactoring.targetClassFullName)) { 
				appliedRefactorings.remove(i); 
				i--; 
			}
		}
		
		
		//The order for ChangeMethodParameters and RenameMethod is important.
		String[] refactoringTypes = {"ChangeMethodParameters", "RenameMethod", "RenameField"}; 
		
		for (int k = 0; k < refactoringTypes.length; k++) {
			
			String mainRefType = refactoringTypes[k];
			
			label: for (int i = 0; i < appliedRefactorings.size(); i++) {
				
				AppliedRefactoringsInformation ref1 = appliedRefactorings.get(i);
			
				if (ref1.refactoringType.equals(mainRefType)) {
			
					String ref1_originalClass = ref1.originalClassFullName;
					String ref1_targetClass = ref1.targetClassFullName;
					String ref1_targetEntityName = ref1.targetEntityName;
					
					if (ref1_targetClass.isEmpty() || ref1_originalClass.equals(ref1_targetClass)) continue;
					
					//Then the entity is moved too or maybe its class is renamed.
					for (int j = 0; j < appliedRefactorings.size(); j++) {
						
						if (j == i) continue;
						
						AppliedRefactoringsInformation ref2 = appliedRefactorings.get(j);
						
						String ref2_originalClass = ref2.originalClassFullName;
						String ref2_targetClass = ref2.targetClassFullName; 
						
						if (ref1_originalClass.equals(ref2_originalClass) && ref1_targetClass.equals(ref2_targetClass) ) {
							
							if (ref2.refactoringType.equals("RenameClass") || ref2.refactoringType.equals("MoveClass")) continue label;
							
							if (ref1_targetEntityName.equals(ref2.originalEntityName)) continue label;
						}
					}
					
					//This means only part of a composite refactoring is applied, so we can remove that.
					appliedRefactorings.remove(i);
					i--;
				}
			}
		}
	}

	private static void Mix_Rename_Move_ChangeMethodParameters(List<AppliedRefactoringsInformation> appliedRefactorings){
			
		for (int i = 0; i < appliedRefactorings.size(); i++) {

			AppliedRefactoringsInformation ref1 = appliedRefactorings.get(i);

			if (!ref1.refactoringType.equals("RenameMethod")) continue;

			for (AppliedRefactoringsInformation ref2 : appliedRefactorings) {

				if (!ref2.refactoringType.equals("MoveAndChangeMethodParameters")) continue;

				if (ref1.originalClassFullName.equals(ref2.originalClassFullName) && 
					ref1.targetEntityName.equals(ref2.originalEntityName)) {


					//We change ref2 and remove ref1
					ref2.originalEntityName = ref1.originalEntityName;
					ref2.refactoringType = "MoveAndRenameAndChangeMethodParameters";
					ref2.description = "MoveAndRenameAndChangeMethodParameters: Method " + ref1.originalEntityName + " in class " + getSimpleName(ref1.originalClassFullName) + " is renamed and its parameters changed to " + ref2.targetEntityName + " and moved to class " + getSimpleName(ref2.targetClassFullName) +"";

					appliedRefactorings.remove(ref1);
					i--;
					break;
				}
			}
		}
	}	

/*******************************************************************************************************
 * Description: CreateRefactoringsRelated2MoveField
********************************************************************************************************/
	private static void CreateRefactoringsRelated2MoveField(List<AppliedRefactoringsInformation> appliedRefactorings){
			
		for (int i = 0; i < appliedRefactorings.size(); i++) {

			if (appliedRefactorings.get(i).refactoringType.equals("MoveField")) {

				AppliedRefactoringsInformation moveRefactoring = appliedRefactorings.get(i);

				for (int j = 0; j < appliedRefactorings.size(); j++) {

					AppliedRefactoringsInformation appliedRef2 = appliedRefactorings.get(j);

					if (appliedRef2.refactoringType.equals("RenameField") &&
					    moveRefactoring.originalClassFullName.equals(appliedRef2.originalClassFullName) &&
						moveRefactoring.originalEntityName.equals(appliedRef2.targetEntityName)) {

						//Create Move and Rename Field.
						moveRefactoring.refactoringType = "MoveAndRenameField";
						moveRefactoring.originalEntityName = appliedRef2.originalEntityName;
						moveRefactoring.targetEntityName = appliedRef2.targetEntityName;
						moveRefactoring.description = "MoveAndRenameField: Field " + appliedRef2.originalEntityName + " in class " + getSimpleName(appliedRef2.originalClassFullName) + " is renamed to " + appliedRef2.targetEntityName + " and moved to class " + getSimpleName(moveRefactoring.targetClassFullName) +"";
						appliedRefactorings.remove(j);
						if (i > j) i--;
						
						//Delete those which do not follow preconditions
						SourceInformation initialSourceInformation = RefactoringProblem.getInitialSourceInformation();
						SourceInformation desiredSourceInformation = RefactoringProblem.getDesiredSourceInformation();
						
						//At least one of preconditions should happen. Otherwise, the refactoring is removed.
						if (!FilterMoveRenameEntities.isRefactoringValid(moveRefactoring, appliedRefactorings, initialSourceInformation, desiredSourceInformation)) {
							
							appliedRefactorings.remove(i);
							
							i--;
						}
						
						break;
					}
				}
			}
		}
	}

/*******************************************************************************************************
 * Description: CreateRefactoringsRelated2MoveMethod
********************************************************************************************************/
	private static void CreateRefactoringsRelated2MoveMethod(List<AppliedRefactoringsInformation> appliedRefactorings){
		
		//Create Move and Change Parameters.
		for (int i = 0; i < appliedRefactorings.size(); i++) {
				
			if (appliedRefactorings.get(i).refactoringType.equals("ChangeMethodParameters")) {
				
				AppliedRefactoringsInformation changeParameterRefactoring = appliedRefactorings.get(i);
				
				for (int j = 0; j < appliedRefactorings.size(); j++) {
						
					AppliedRefactoringsInformation appliedRef2 = appliedRefactorings.get(j);
						
					if (appliedRef2.refactoringType.equals("MoveMethod") && 
						changeParameterRefactoring.originalClassFullName.equals(appliedRef2.originalClassFullName) &&
						changeParameterRefactoring.targetEntityName.equals(appliedRef2.originalEntityName)) {
						
						if (changeParameterRefactoring.targetClassFullName.equals(appliedRef2.targetClassFullName)) {
						
							changeParameterRefactoring.refactoringType = "MoveAndChangeMethodParameters";
							changeParameterRefactoring.targetClassFullName = appliedRef2.targetClassFullName;
							changeParameterRefactoring.description = "MoveAndChangeMethodParameters: Method " + changeParameterRefactoring.originalEntityName + " in class " + getSimpleName(changeParameterRefactoring.originalClassFullName) + " is changed to " + changeParameterRefactoring.targetEntityName + " and moved to class " + getSimpleName(changeParameterRefactoring.targetClassFullName) +"";
							appliedRefactorings.remove(j);
							if (i > j) i--;
							break;
						}
					}
				}
			}	
		}
		
		//Create Move and Rename Method.
		for (int i = 0; i < appliedRefactorings.size(); i++) {
			
			if (appliedRefactorings.get(i).refactoringType.equals("RenameMethod")) {
				
				AppliedRefactoringsInformation renameMethodRefactoring = appliedRefactorings.get(i);
				
				for (int j = 0; j < appliedRefactorings.size(); j++) {
					
					AppliedRefactoringsInformation appliedRef2 = appliedRefactorings.get(j);
					
					if (appliedRef2.refactoringType.equals("MoveMethod") &&
						renameMethodRefactoring.originalClassFullName.equals(appliedRef2.originalClassFullName) &&
						renameMethodRefactoring.targetEntityName.equals(appliedRef2.originalEntityName)) {
					
						if (renameMethodRefactoring.targetClassFullName.equals(appliedRef2.targetClassFullName)) {
						
							renameMethodRefactoring.refactoringType = "MoveAndRenameMethod";
							renameMethodRefactoring.targetClassFullName = appliedRef2.targetClassFullName;
							renameMethodRefactoring.description = "MoveAndRenameMethod: Method " + renameMethodRefactoring.originalEntityName + " in class " + getSimpleName(renameMethodRefactoring.originalClassFullName) + " is renamed to " + renameMethodRefactoring.targetEntityName + " and moved to class " + getSimpleName(renameMethodRefactoring.targetClassFullName) +"";
							appliedRefactorings.remove(j);
							if (i > j) i--;
							
							//Delete those which do not follow preconditions
							SourceInformation initialSourceInformation = RefactoringProblem.getInitialSourceInformation();
							SourceInformation desiredSourceInformation = RefactoringProblem.getDesiredSourceInformation();
							
							//At least one of preconditions should happen. Otherwise, the refactoring is removed.
							if (!FilterMoveRenameEntities.isRefactoringValid(renameMethodRefactoring, appliedRefactorings, initialSourceInformation, desiredSourceInformation)) {
								
								appliedRefactorings.remove(i);
								
								i--;
							}
							
							break;
						}
					}
				}
			}
		}
	}
	
/*******************************************************************************************************
 * Description: getSimpleName
********************************************************************************************************/
	private static String getSimpleName(String entityFullName) {
		
		return entityFullName.substring(entityFullName.lastIndexOf(".") + 1);
	}
	
/*******************************************************************************************************
 * Description: getMethodName
********************************************************************************************************/
	private static String getMethodName(String methodName) {
		
		String result = "";
		
		String[] parts1 = methodName.split("\\(");
		result = getSimpleName(parts1[0]) + "(";
		
		if (parts1.length == 1) 
			return result.substring(0, result.length() - 1);
		
		parts1[1] = Service.removeSpecialCharachters(parts1[1]);
		
		String[] parts2 = parts1[1].split(",");
		
		for (String part : parts2) {
			
			int index = part.lastIndexOf(".");
			if (index != -1) {
				part = part.substring(index + 1);
			}
			result += getSimpleName(part) + ",";
		}
		
		return result.substring(0, result.length() - 1);
	}

/*******************************************************************************************************
 * Description: DecideAboutRefactoringType()
********************************************************************************************************/
	 private static String DecideAboutRefactoringType(String oldFullClassName, String newFullClassName){
		 
		 int index = oldFullClassName.lastIndexOf(".");
		 
		 String part1_1 = "";
		 String part1_2;
		 
		 if (index != -1) {
			 part1_1 = oldFullClassName.substring(0, index);
			 part1_2 = oldFullClassName.substring(index + 1);
		 }
		 else {part1_2 = oldFullClassName;}
		 
		 index = newFullClassName.lastIndexOf(".");
		 
		 String part2_1 = "";
		 String part2_2;
		 
		 if (index != -1) {
			 part2_1 = newFullClassName.substring(0, index);
			 part2_2 = newFullClassName.substring(index + 1);
		 }
		 else {part2_2 = newFullClassName;}
		 
		 String refactoringType = "";
		 
		 if (!part1_1.equals(part2_1)) {
			 refactoringType = "Move";
			 if (!part1_2.equals(part2_2))
				 refactoringType += "AndRename";
		 }
		 
		 else if (!part1_2.equals(part2_2)) refactoringType = "Rename";
		 
		 return refactoringType;
	 }
}