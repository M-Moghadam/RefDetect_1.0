package moea.variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import moea.problem.AdvancedProblem;
import codeInformation.DesiredSourceInformation;
import codeInformation.Entity;
import codeInformation.SourceInformation;
import simulation.candidateRefactorings.CandidateEntity;
import simulation.candidateRefactorings.CandidateExtractInlineMethod;
import simulation.candidateRefactorings.CandidateFieldRefactorings;
import simulation.candidateRefactorings.CandidateMethodRefactorings;
import simulation.candidateRefactorings.CandidateRenameEntity;
import simulation.simulateRefactoring.ChangeMethodParameters;
import simulation.simulateRefactoring.DeleteClass;
import simulation.simulateRefactoring.DeleteField;
import simulation.simulateRefactoring.DeleteMethod;
import simulation.simulateRefactoring.ExtractClass;
import simulation.simulateRefactoring.ExtractInterface;
import simulation.simulateRefactoring_RefDetect.ExtractMethod;
import simulation.simulateRefactoring.ExtractSubClass;
import simulation.simulateRefactoring_RefDetect.ExtractSuperClass;
import simulation.simulateRefactoring_RefDetect.InlineMethod;
import simulation.simulateRefactoring_RefDetect.RenameClass;
import simulation.simulateRefactoring.MoveField;
import simulation.simulateRefactoring.MoveMethod;
import simulation.simulateRefactoring.PullUpField;
import simulation.simulateRefactoring.PullUpMethod;
import simulation.simulateRefactoring.PushDownField;
import simulation.simulateRefactoring.PushDownMethod;
import simulation.simulateRefactoring.Refactoring;
import simulation.simulateRefactoring.RenameField;
import simulation.simulateRefactoring.RenameMethod;

public abstract class AdvancedCreateChromosome extends CreateChromosome {

	private final CandidateFieldRefactorings candidateFieldRefactorings;
	private final CandidateMethodRefactorings candidateMethodRefactorings;
	
	/** This field contains all candidate refactorings: 
	    Key is refactoring type and values are candidates for that type of refactoring.*/
	static Map<String, List<Refactoring>> candidateRefactorings;
	
/*******************************************************************************************************
 * Description: AdvancedCreateChromosome()
********************************************************************************************************/
	public AdvancedCreateChromosome(SourceInformation sourceInformation) {
		
		super(sourceInformation);
		
		//This field contains all candidate fields for refactoring.
		this.candidateFieldRefactorings = AdvancedProblem.getInitialCandidateFieldRefactorings();
		
		//This field contains all candidate methods for refactoring.
		this.candidateMethodRefactorings = AdvancedProblem.getInitialCandidateMethodRefactorings();
		
		if (candidateRefactorings == null) {
			candidateRefactorings = new HashMap<String, List<Refactoring>>();
			createAllCandidateRefactorings();
		}
	}
	
/*******************************************************************************************************
 * Description: createAllCandidateRefactorings()
********************************************************************************************************/
	private void createAllCandidateRefactorings() {
		
		createAllInlineMethodRefactorings();
		createAllExtractMethodRefactorings();
		createAllDeleteClassRefactoring();
		createAllChangeMethodParametersRefactoring();
		createAllRenameMethodRefactoring();
		createAllRenameFieldRefactoring();
		createAllDeleteMethodRefactoring();
		createAllDeleteFieldRefactoring();
		createAllRenameClassRefactoring();
		createAllExtractSuperClassRefactorings();
		createAllExtractSubClassRefactorings();
		createAllExtractClassRefactorings();
		createAllExtractInterfaceRefactorings();
		createAllPullUpFieldRefactorings();
		createAllPullUpMethodRefactorings();
		createAllPushDownFieldRefactorings();
		createAllPushDownMethodRefactorings();
		createAllMoveFieldRefactorings();
		createAllMoveMethodRefactorings();
	}

/*******************************************************************************************************
 * Description: createAllInlineMethodRefactorings()
********************************************************************************************************/
	private void createAllInlineMethodRefactorings() {

		List<Refactoring> InlineMethodCandidates = new ArrayList<>();
				
		for (CandidateExtractInlineMethod candidateMethod : candidateMethodRefactorings.candidatesForInlineMethodRef) {
			InlineMethodCandidates.add(new InlineMethod(candidateMethod.originalClassFullName, candidateMethod.entityName, candidateMethod.entitySignature, candidateMethod.entityTypeFullName, candidateMethod.targetClassFullName, candidateMethod.original_or_target_MethodName));
		}
				
		candidateRefactorings.put("InlineMethod", InlineMethodCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllExtractMethodRefactorings()
********************************************************************************************************/
	private void createAllExtractMethodRefactorings() {

		List<Refactoring> ExtractMethodCandidates = new ArrayList<>();
			
		for (CandidateExtractInlineMethod candidateMethod : candidateMethodRefactorings.candidatesForExtractMethodRef) {
			ExtractMethodCandidates.add(new ExtractMethod(candidateMethod.originalClassFullName, candidateMethod.original_or_target_MethodName, candidateMethod.targetClassFullName, candidateMethod.entityName, candidateMethod.entitySignature, candidateMethod.entityTypeFullName));
		}
			
		candidateRefactorings.put("ExtractMethod", ExtractMethodCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllDeleteClassRefactoring()
********************************************************************************************************/
	private void createAllDeleteClassRefactoring() {
					
		List<Refactoring> DeleteClassCandidates = new ArrayList<>();
		
		for (String caldidate : AdvancedProblem.getDeleteClassCandidates()) {
			DeleteClassCandidates.add(new DeleteClass(caldidate));
		}
		
		candidateRefactorings.put("DeleteClass", DeleteClassCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllChangeMethodParametersRefactoring()
********************************************************************************************************/
	private void createAllChangeMethodParametersRefactoring() {
						
		List<Refactoring> ChangeMethodParametersCandidates = new ArrayList<>();
				
		for (CandidateRenameEntity candidateMethod : candidateMethodRefactorings.candidatesForChangeMethodParametersRef) {
			ChangeMethodParametersCandidates.add(new ChangeMethodParameters(candidateMethod.originalClassFullName, candidateMethod.entityName, candidateMethod.entitySignature, candidateMethod.entityTypeFullName, candidateMethod.entityNewName, candidateMethod.targetClassFullName));
		}
				
		candidateRefactorings.put("ChangeMethodParameters", ChangeMethodParametersCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllRenameMethodRefactoring()
********************************************************************************************************/
	private void createAllRenameMethodRefactoring() {
					
		List<Refactoring> RenameMethodCandidates = new ArrayList<>();
			
		for (CandidateRenameEntity candidateMethod : candidateMethodRefactorings.candidatesForRenameMethodRef) {
			RenameMethodCandidates.add(new RenameMethod(candidateMethod.originalClassFullName, candidateMethod.entityName, candidateMethod.entitySignature, candidateMethod.entityTypeFullName, candidateMethod.entityNewName, candidateMethod.targetClassFullName));
		}
			
		candidateRefactorings.put("RenameMethod", RenameMethodCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllRenameFieldRefactoring()
********************************************************************************************************/
	private void createAllRenameFieldRefactoring() {
				
		List<Refactoring> RenameFieldCandidates = new ArrayList<>();
		
		for (CandidateRenameEntity candidateField : candidateFieldRefactorings.candidatesForRenameFieldRef) {
			RenameFieldCandidates.add(new RenameField(candidateField.originalClassFullName, candidateField.entityName, candidateField.entityNewName));
		}
		
		candidateRefactorings.put("RenameField", RenameFieldCandidates);
	}

/*******************************************************************************************************
 * Description: createAllDeleteMethodRefactoring()
********************************************************************************************************/
	private void createAllDeleteMethodRefactoring() {
				
		Map<String, List<Entity>> deleteMethodCandidates = AdvancedProblem.getInitialCandidateMethodRefactorings().deletedMethods;

		List<Refactoring> DeleteMethodCandidates = new ArrayList<>();
			
		for (String classFullName : deleteMethodCandidates.keySet()) {
				
			for (Entity method : deleteMethodCandidates.get(classFullName)) {
				DeleteMethodCandidates.add(new DeleteMethod(classFullName, method.getName(), method.getSignature(), method.getEntityTypeFullName()));
			}
		}
				
		candidateRefactorings.put("DeleteMethod", DeleteMethodCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllDeleteFieldRefactoring()
********************************************************************************************************/
	private void createAllDeleteFieldRefactoring() {
			
		Map<String, List<Entity>> deleteFieldCandidates = AdvancedProblem.getInitialCandidateFieldRefactorings().deletedFields;

		List<Refactoring> DeleteFieldCandidates = new ArrayList<>();
		
		for (String classFullName : deleteFieldCandidates.keySet()) {
			
			for (Entity field : deleteFieldCandidates.get(classFullName)) {
				DeleteFieldCandidates.add(new DeleteField(classFullName, field.getName()));
			}
		}
			
		candidateRefactorings.put("DeleteField", DeleteFieldCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllRenameClassRefactoring()
********************************************************************************************************/
	private void createAllRenameClassRefactoring() {
		
		Map<String, String> renameClassCandidates = AdvancedProblem.getRenameClassCandidates();
		
		List<Refactoring> RenameClassCandidates = new ArrayList<>();
		
		for (Entry<String, String> entry : renameClassCandidates.entrySet()) {
			RenameClassCandidates.add(new RenameClass(entry.getKey(), entry.getValue()));
		}
		
		candidateRefactorings.put("RenameClass", RenameClassCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllExtractInterfaceRefactorings()
********************************************************************************************************/
	private void createAllExtractInterfaceRefactorings() {
			
		List<Refactoring> ExtractInterfaceCandidates = new ArrayList<>();
		for (String candidateClass : AdvancedProblem.getNewClassCandidates()) {
				
			Set<String> cloneSubClasses = DesiredSourceInformation.cloneInterfaceClassMap(candidateClass);
				
			//If it is a class, or an interface which is not implements by any classes no need to process
			if (cloneSubClasses.isEmpty()) continue;
			
			ExtractInterfaceCandidates.add(new ExtractInterface(cloneSubClasses, candidateClass));
		}
			
		candidateRefactorings.put("ExtractInterface", ExtractInterfaceCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllExtractSuperClassRefactorings()
********************************************************************************************************/
	private void createAllExtractSuperClassRefactorings() {
		
		//If no class inherits from this class, then it is not a candidate for extract super class
		List<String> candidateClasses = new ArrayList<String>();
		for (String cls : AdvancedProblem.getNewClassCandidates()) {
			if (DesiredSourceInformation.get_Parent_Children_Map().get(cls) != null)
				candidateClasses.add(cls);
		}
		
		List<Refactoring> ExtractSuperClassCandidates = new ArrayList<>();
		for (String candidateClass : candidateClasses) {
			
			Set<String> cloneChildren = DesiredSourceInformation.clone_Parent_Children_Map(candidateClass);
			
			ExtractSuperClassCandidates.add(new ExtractSuperClass(cloneChildren, candidateClass));
		}
		
		candidateRefactorings.put("ExtractSuperClass", ExtractSuperClassCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllExtractSubClassRefactorings()
********************************************************************************************************/
	private void createAllExtractSubClassRefactorings() {
		
		List<Refactoring> ExtractSubClassCandidates = new ArrayList<>();
		
		for (String cls : AdvancedProblem.getNewClassCandidates()) {
			
			String parent = DesiredSourceInformation.classParentMap.get(cls);
			
			//To cover Interfaces
			if (parent == null) {
				for (Entry<String, Set<String>> map : DesiredSourceInformation.getInterfaceClassMap().entrySet()) {
					
					if (map.getValue().contains(cls)) {
						parent = map.getKey();
						break;
					}
				}
			}
			
			//If the class inherits from no class, then it is not a candidate for extract sub class
			if (parent != null && classOredr.contains(parent)) {
				
				//Also, at least one field or method should be pushdown from its super class to the newly created class.
				if (noEntityIsPushedDown(cls)) continue;
				
				ExtractSubClassCandidates.add(new ExtractSubClass(parent, cls));
			}
		}
		
		candidateRefactorings.put("ExtractSubClass", ExtractSubClassCandidates);
	}
	
/*******************************************************************************************************
 * Description: noEntityIsPushedDown()
********************************************************************************************************/
	private boolean noEntityIsPushedDown(String candidateClass) {
		
		for (CandidateEntity candidateMethod : candidateMethodRefactorings.candidatesForPushDownMethodRef) {
			if (candidateMethod.targetClassFullName.equals(candidateClass)) return false;
		}
		
		for (CandidateEntity candidateField : candidateFieldRefactorings.candidatesForPushDownFieldRef) {
			if (candidateField.targetClassFullName.equals(candidateClass)) return false;
		}
		
		return true;
	}
	
/*******************************************************************************************************
 * Description: createAllExtractClassRefactorings()
********************************************************************************************************/
	private void createAllExtractClassRefactorings() {

		List<Refactoring> ExtractClassCandidates = new ArrayList<>();
		for (String candidateClass : AdvancedProblem.getNewClassCandidates()) {
			
			//If it is an interface, no need to process
			if (DesiredSourceInformation.getInterfaceClassMap().get(candidateClass) != null) continue;
			
			String originalClass = findOriginalClass(candidateClass);
			
			if (originalClass == null) continue;
			
			ExtractClassCandidates.add(new ExtractClass(originalClass, candidateClass));
		}
		
		candidateRefactorings.put("ExtractClass", ExtractClassCandidates);
	}
	
/*******************************************************************************************************
 * Description: findOriginalClass()
 * 				We return the first class that a method or field of it is moved to the candidate class.
 * 				If it creates problem (more than a class is candidate), then the class with maximum method and field 
 * 				which are moved to the candidateClass should be return.
********************************************************************************************************/
	private String findOriginalClass(String candidateClass) {
		
		for (CandidateEntity candidateMethod : candidateMethodRefactorings.candidatesForMoveMethodRef) {
			if (candidateMethod.targetClassFullName.equals(candidateClass)) return candidateMethod.originalClassFullName;
		}
		
		for (CandidateEntity candidateField : candidateFieldRefactorings.candidatesForMoveFieldRef) {
			if (candidateField.targetClassFullName.equals(candidateClass)) return candidateField.originalClassFullName;
		}
		
		return null;
	}
	
/*******************************************************************************************************
 * Description: createAllPullUpFieldRefactorings()
********************************************************************************************************/
	private void createAllPullUpFieldRefactorings() {
		 
		List<Refactoring> PullUpFieldCandidates = new ArrayList<>();
		
		for (CandidateEntity candidateField : candidateFieldRefactorings.candidatesForPullUpFieldRef) {
			PullUpFieldCandidates.add(new PullUpField(candidateField.originalClassFullName, candidateField.entityName, candidateField.targetClassFullName));
		}
		
		candidateRefactorings.put("PullUpField", PullUpFieldCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllPullUpMethodRefactorings()
********************************************************************************************************/
	private void createAllPullUpMethodRefactorings() {

		List<Refactoring> PullUpMethodCandidates = new ArrayList<>();
		
		for (CandidateEntity candidateMethod : candidateMethodRefactorings.candidatesForPullUpMethodRef) {
			PullUpMethodCandidates.add(new PullUpMethod(candidateMethod.originalClassFullName, candidateMethod.entityName, candidateMethod.entitySignature, candidateMethod.entityTypeFullName, candidateMethod.targetClassFullName));
		}
		
		candidateRefactorings.put("PullUpMethod", PullUpMethodCandidates);
	}
			
/*******************************************************************************************************
 * Description: createAllPushDownFieldRefactorings()
********************************************************************************************************/
	private void createAllPushDownFieldRefactorings() {

		List<Refactoring> PushDownFieldCandidates = new ArrayList<>();
		
		for (CandidateEntity candidateField : candidateFieldRefactorings.candidatesForPushDownFieldRef) {
			
			String originalClassFullName = candidateField.originalClassFullName;
			String fieldName = candidateField.entityName;
			
			boolean considerBefore = false;
			for (Refactoring addedRefactoring : PushDownFieldCandidates) {
				
				PushDownField temp = (PushDownField)addedRefactoring;
				
				//If it is consider before no need to consider it again.
				if (temp.fieldName.equals(fieldName) && temp.originalClassFullName.equals(originalClassFullName)) {
					considerBefore = true;
					break;
				}
			}
			
			if (considerBefore) continue;
			
			List<String> targetChildren = new ArrayList<String>();
		
			for (CandidateEntity candid : candidateFieldRefactorings.candidatesForPushDownFieldRef) {
				if (candid.entityName.equals(fieldName) && candid.originalClassFullName.equals(originalClassFullName)) {
					targetChildren.add(candid.targetClassFullName);
				}
			}
			
			PushDownFieldCandidates.add(new PushDownField(originalClassFullName, fieldName, targetChildren));
		}
		
		candidateRefactorings.put("PushDownField", PushDownFieldCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllPushDownMethodRefactorings()
********************************************************************************************************/
	private void createAllPushDownMethodRefactorings() {
		
		List<Refactoring> PushDownMethodCandidates = new ArrayList<>();
		
		for (CandidateEntity candidateMethod : candidateMethodRefactorings.candidatesForPushDownMethodRef) {
		
			String originalClassFullName = candidateMethod.originalClassFullName;
			String methodName = candidateMethod.entityName;
			String methodSignature = candidateMethod.entitySignature;
			String methodReturnType = candidateMethod.entityTypeFullName;
			
			boolean considerBefore = false;
			for (Refactoring addedRefactoring : PushDownMethodCandidates) {
				
				PushDownMethod temp = (PushDownMethod)addedRefactoring;
				
				//If it is consider before no need to consider it again.
				if (temp.methodName.equals(methodName) && 
					temp.methodSignature.equals(methodSignature) && 
					temp.methodReturnType.equals(methodReturnType) && 
					temp.originalClassFullName.equals(originalClassFullName)) {
					
					considerBefore = true;
					break;
				}
			}
			
			if (considerBefore) continue;
			
			List<String> targetChildren = new ArrayList<String>();
		
			for (CandidateEntity candid : candidateMethodRefactorings.candidatesForPushDownMethodRef) {
			
				if (candid.entityName.equals(methodName) && candid.entitySignature.equals(methodSignature) && candid.originalClassFullName.equals(originalClassFullName))
					targetChildren.add(candid.targetClassFullName);
			}
		
			PushDownMethodCandidates.add(new PushDownMethod(originalClassFullName, methodName, methodSignature, methodReturnType ,targetChildren));
		}
		
		candidateRefactorings.put("PushDownMethod", PushDownMethodCandidates);
	}

/*******************************************************************************************************
 * Description: createAllMoveFieldRefactorings()
 *    			Note for simulate and non-simulate algorithm based on the differentiate algorithm, if 
 * 				the originalClass is renamed to the targetClass, then we do not check moving entity
 * 			    between two class. However, when a search-based algorithm is used as we are not sure 
 * 				about rename class candidate as we have a weak threshold, then we also allow move entities
 * 				between the original and the target class.Method isClassRenamed in override in class
 * 				SearchBasedCreateChromosome. 
********************************************************************************************************/
	private void createAllMoveFieldRefactorings() {
			
		List<Refactoring> MoveFieldCandidates = new ArrayList<>();
		
		for (CandidateEntity candidateField : candidateFieldRefactorings.candidatesForMoveFieldRef) {
			
			/** If original class is renamed to target class then no need to consider move field refactoring.*/ 
			if (isClassRenamed(candidateField.originalClassFullName, candidateField.targetClassFullName))
				continue;
			
			MoveFieldCandidates.add(new MoveField(candidateField.originalClassFullName, candidateField.entityName, candidateField.targetClassFullName));
		}
		
		candidateRefactorings.put("MoveField", MoveFieldCandidates);
	}
	
/*******************************************************************************************************
 * Description: createAllMoveMethodRefactorings()
 * 				Note for simulate and non-simulate algorithm based on the differentiate algorithm, if 
 * 				the originalClass is renamed to the targetClass, then we do not check moving entity
 * 			    between two class. However, when a search-based algorithm is used as we are not sure 
 * 				about rename class candidate as we have a weak threshold, then we also allow move entities
 * 				between the original and the target class.Method isClassRenamed in override in class
 * 				SearchBasedCreateChromosome. 
********************************************************************************************************/
	private void createAllMoveMethodRefactorings() {
			
		List<Refactoring> MoveMethodCandidates = new ArrayList<>();
		
		for (CandidateEntity candidateMethod : candidateMethodRefactorings.candidatesForMoveMethodRef) {
			
			/** If original class is renamed to target class then no need to consider move method refactoring.*/
			if (isClassRenamed(candidateMethod.originalClassFullName, candidateMethod.targetClassFullName))
				continue;
			
			MoveMethodCandidates.add(new MoveMethod(candidateMethod.originalClassFullName, candidateMethod.entityName, candidateMethod.entitySignature, candidateMethod.entityTypeFullName, candidateMethod.targetClassFullName));
		}
		
		candidateRefactorings.put("MoveMethod", MoveMethodCandidates);
	}
	
/*******************************************************************************************************
 * Description: isClassRenamed()
********************************************************************************************************/
	boolean isClassRenamed(String class1, String class2){
		
		for (Refactoring refInfo : candidateRefactorings.get("RenameClass")) {
			
			RenameClass renamedClass = (RenameClass)refInfo;
			
			if (renamedClass.getOriginalClassFullName().equals(class1) && 
				renamedClass.getNewClassFullName().equals(class2))
				return true;
		}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: setCandidateRefactoringsNull()
********************************************************************************************************/
	public static void setCandidateRefactoringsNull(){
		candidateRefactorings = null;
	}
	
/*******************************************************************************************************
 * Description: getCandidateRefactorings()
********************************************************************************************************/
	public static Map<String, List<Refactoring>> getCandidateRefactorings(){
		return candidateRefactorings;
	}
}