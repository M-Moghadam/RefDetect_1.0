package moea.variable;

import java.util.ArrayList;
import java.util.List;

import org.moeaframework.core.PRNG;

import simulation.simulateRefactoring.Refactoring;

public interface CreateRefactorings {
	
/*******************************************************************************************************
 * Description: createRefactoring()
********************************************************************************************************/	
	default Refactoring createRefactoring(List<Refactoring> appliedRefactorings, List<String> candidateRefactoringTypes) {
			
		Refactoring newRefactoringInformation = null;
			
		while (!candidateRefactoringTypes.isEmpty()) {
				
			String refactoringType = PRNG.nextItem(candidateRefactoringTypes);
				
			newRefactoringInformation = createRefactoring(refactoringType);
			
			if (isValidRefactoring(refactoringType, candidateRefactoringTypes, 
								   newRefactoringInformation, appliedRefactorings)) 
				break;
		}
				
		return newRefactoringInformation;
	}
	
/*******************************************************************************************************
 * Description: isValidRefactoring()
********************************************************************************************************/
	abstract boolean isValidRefactoring(String refactoringType, List<String> candidateRefactoringTypes, 
									    Refactoring newRefactoringInformation, 
									    List<Refactoring> refactoringList);
	
/*******************************************************************************************************
 * Description: initializeList()
********************************************************************************************************/
	default List<String> initializeList(){
				
		List<String> list = new ArrayList<String>();
				
		list.add("ExtractSuperClass"); list.add("ExtractSubClass"); list.add("ExtractClass"); list.add("PullUpField"); 
		list.add("PullUpMethod"); list.add("PushDownField"); list.add("PushDownMethod"); list.add("MoveField"); 
		list.add("MoveMethod"); list.add("RenameClass"); list.add("DeleteField"); list.add("DeleteMethod"); 
		list.add("RenameField"); list.add("RenameMethod"); list.add("DeleteClass"); list.add("ExtractMethod");
		list.add("InlineMethod"); list.add("ChangeMethodParameters"); list.add("ExtractInterface");
				
		return list;
	}
		
/*******************************************************************************************************
 * Description: createRefactoring()
********************************************************************************************************/
	default Refactoring createRefactoring(String refactoringType) {
		
		switch (refactoringType) {
			case "ExtractSuperClass":
				return createExtractSuperClassRefactoring();
			case "ExtractSubClass":
				return createExtractSubClassRefactoring();
			case "ExtractClass":
				return createExtractClassRefactoring();
			case "PullUpField":
				return createPullUpFieldRefactoring();
			case "PullUpMethod":
				return createPullUpMethodRefactoring();
			case "PushDownField":
				return createPushDownFieldRefactoring();
			case "PushDownMethod":
				return createPushDownMethodRefactoring();
			case "MoveField":
				return createMoveFieldRefactoring();
			case "MoveMethod":
				return createMoveMethodRefactoring();
			case "RenameClass":
				return createRenameClassRefactoring();
			case "DeleteField":
				return createDeleteFieldRefactoring();
			case "DeleteMethod":
				return createDeleteMethodRefactoring();
			case "RenameField":
				return createRenameFieldRefactoring();
			case "RenameMethod":
				return createRenameMethodRefactoring();
			case "DeleteClass": 
				return createDeleteClassRefactoring();
			case "ExtractMethod": 
				return createExtractMethodRefactoring();
			case "InlineMethod": 
				return createInlineMethodRefactoring();
			case "ChangeMethodParameters": 
				return createChangeMethodParameterRefactoring();
			case "ExtractInterface":
				return createExtractInterfaceRefactoring();
		}
		
		return null;
	}
	
/*******************************************************************************************************
 * Description: all abstract methods which are implemented in subclasses
********************************************************************************************************/
	abstract Refactoring createPullUpFieldRefactoring();
	abstract Refactoring createPullUpMethodRefactoring();
	abstract Refactoring createPushDownFieldRefactoring();
	abstract Refactoring createPushDownMethodRefactoring();
	abstract Refactoring createMoveFieldRefactoring();
	abstract Refactoring createMoveMethodRefactoring();
	abstract Refactoring createExtractSuperClassRefactoring();
	abstract Refactoring createExtractSubClassRefactoring();
	abstract Refactoring createExtractClassRefactoring();
	abstract Refactoring createExtractInterfaceRefactoring();
	abstract Refactoring createRenameClassRefactoring();
	abstract Refactoring createDeleteFieldRefactoring();
	abstract Refactoring createDeleteMethodRefactoring();
	abstract Refactoring createRenameFieldRefactoring();
	abstract Refactoring createRenameMethodRefactoring();
	abstract Refactoring createDeleteClassRefactoring();
	abstract Refactoring createExtractMethodRefactoring();
	abstract Refactoring createInlineMethodRefactoring();
	abstract Refactoring createChangeMethodParameterRefactoring();
}