package moea.variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

public class CloneRefactoring {

/*******************************************************************************************************
 * Description: cloneRefactoringList()
 * 				Note we only clone refactoring types which are not empty.
********************************************************************************************************/
	public static Map<String, List<Refactoring>> cloneRefactoringList(Map<String, List<Refactoring>> refactoringList) {
		
		Map<String, List<Refactoring>> cloneRefactoringList = new HashMap<String, List<Refactoring>>();
		
		for (Entry<String, List<Refactoring>> entry : refactoringList.entrySet()) {
			
			List<Refactoring> refactorings = entry.getValue();
			if (refactorings.isEmpty()) continue;
			
			cloneRefactoringList.put(entry.getKey(), cloneRefactoringList(refactorings));
		}
			
		return cloneRefactoringList;
	}
	
/*******************************************************************************************************
 * Description: cloneRefactoringList()
********************************************************************************************************/
	public static List<Refactoring> cloneRefactoringList(List<Refactoring> refactoringList) {
		
		ArrayList<Refactoring> cloneRefactoringList = new ArrayList<Refactoring>();
		
		for (Refactoring candidateRefactoring : refactoringList) 
			cloneRefactoringList.add(cloneRefactoring(candidateRefactoring));
		
		return cloneRefactoringList;
	}
	
/*******************************************************************************************************
 * Description: cloneRefactoring()
********************************************************************************************************/
	public static Refactoring cloneRefactoring(Refactoring candidateRefactoring) {

		switch (candidateRefactoring.getClass().getSimpleName()) {

		case "InlineMethod":
			InlineMethod inlineMethodCandidate = (InlineMethod) candidateRefactoring;
			return new InlineMethod(inlineMethodCandidate.originalClassFullName, inlineMethodCandidate.methodName, inlineMethodCandidate.methodSignature, inlineMethodCandidate.methodReturnType, inlineMethodCandidate.getTargetClassFullName(), inlineMethodCandidate.getTargetMethodName());
		
		case "ExtractMethod":
			ExtractMethod extractMethodCandidate = (ExtractMethod) candidateRefactoring;
			return new ExtractMethod(extractMethodCandidate.originalClassFullName, extractMethodCandidate.getOriginalMethodName(), extractMethodCandidate.getTargetClassFullName(), extractMethodCandidate.methodName, extractMethodCandidate.methodSignature, extractMethodCandidate.methodReturnType);

		case "DeleteClass":
			DeleteClass deleteClassRefactoring = (DeleteClass) candidateRefactoring;
			return new DeleteClass(deleteClassRefactoring.getOriginalClassFullName());
		
		case "ChangeMethodParameters":
			ChangeMethodParameters changeMethodParameterRefactoring = (ChangeMethodParameters) candidateRefactoring;
			return new ChangeMethodParameters(changeMethodParameterRefactoring.originalClassFullName, changeMethodParameterRefactoring.methodName, changeMethodParameterRefactoring.methodSignature, changeMethodParameterRefactoring.methodReturnType, changeMethodParameterRefactoring.getNewMethodName(), changeMethodParameterRefactoring.targetClassFullName);
			
		case "RenameMethod":
			RenameMethod renameMethodRefactoring = (RenameMethod) candidateRefactoring;
			return new RenameMethod(renameMethodRefactoring.originalClassFullName, renameMethodRefactoring.methodName, renameMethodRefactoring.methodSignature, renameMethodRefactoring.methodReturnType, renameMethodRefactoring.getNewMethodName(), renameMethodRefactoring.targetClassFullName);
		
		case "RenameField":
			RenameField renameFieldRefactoring = (RenameField) candidateRefactoring;
			return new RenameField(renameFieldRefactoring.originalClassFullName, renameFieldRefactoring.fieldName, renameFieldRefactoring.getNewFieldName());
		
		case "DeleteMethod":
			DeleteMethod deleteMethodRefactoring = (DeleteMethod) candidateRefactoring;
			return new DeleteMethod(deleteMethodRefactoring.originalClassFullName, deleteMethodRefactoring.methodName, deleteMethodRefactoring.methodSignature, deleteMethodRefactoring.methodReturnType);
		
		case "DeleteField":
			DeleteField deleteFieldRefactoring = (DeleteField) candidateRefactoring;
			return new DeleteField(deleteFieldRefactoring.originalClassFullName, deleteFieldRefactoring.fieldName);
		
		case "RenameClass":
			RenameClass renameClassRefactoring = (RenameClass) candidateRefactoring;
			return new RenameClass(renameClassRefactoring.getOriginalClassFullName(), renameClassRefactoring.getNewClassFullName());
			
		case "ExtractSuperClass": 
			ExtractSuperClass extractSuperClassCandidate = (ExtractSuperClass) candidateRefactoring;
			return new ExtractSuperClass(extractSuperClassCandidate.getChildrenClassesFullName(), extractSuperClassCandidate.getNewClassFullName());

		case "ExtractSubClass":
			ExtractSubClass extractSubClassCandidate = (ExtractSubClass) candidateRefactoring;
			return new ExtractSubClass(extractSubClassCandidate.getParentClassFullName(), extractSubClassCandidate.getNewClassFullName());

		case "ExtractClass": 
			ExtractClass extractClassCandidate = (ExtractClass) candidateRefactoring;
			return new ExtractClass(extractClassCandidate.getOriginalClassFullName(), extractClassCandidate.getNewClassFullName());

		case "ExtractInterface": 
			ExtractInterface extractInterfaceCandidate = (ExtractInterface) candidateRefactoring;
			return new ExtractInterface(extractInterfaceCandidate.getChildrenClassesFullName(), extractInterfaceCandidate.getNewClassFullName());
			
		case "PullUpField": 
			PullUpField pullUpFieldCandidate = (PullUpField) candidateRefactoring;
			return new PullUpField(pullUpFieldCandidate.originalClassFullName, pullUpFieldCandidate.fieldName, pullUpFieldCandidate.getParentClassFullName());

		case "PullUpMethod": 
			PullUpMethod pullUpMethodCandidate = (PullUpMethod) candidateRefactoring;
			return new PullUpMethod(pullUpMethodCandidate.originalClassFullName, pullUpMethodCandidate.methodName, pullUpMethodCandidate.methodSignature, pullUpMethodCandidate.methodReturnType, pullUpMethodCandidate.getParentClassFullName());

		case "PushDownField": 
			PushDownField pushDownFieldCandidate = (PushDownField) candidateRefactoring;
			return new PushDownField(pushDownFieldCandidate.originalClassFullName, pushDownFieldCandidate.fieldName, pushDownFieldCandidate.getChildClassesFullName());

		case "PushDownMethod": 
			PushDownMethod pushDownMethodCandidate = (PushDownMethod) candidateRefactoring;
			return new PushDownMethod(pushDownMethodCandidate.originalClassFullName, pushDownMethodCandidate.methodName, pushDownMethodCandidate.methodSignature, pushDownMethodCandidate.methodReturnType, pushDownMethodCandidate.getChildClassesFullName());

		case "MoveField": 
			MoveField moveFieldCandidate = (MoveField) candidateRefactoring;
			return new MoveField(moveFieldCandidate.originalClassFullName, moveFieldCandidate.fieldName, moveFieldCandidate.getTargetClassFullName());

		case "MoveMethod":
			MoveMethod moveMethodCandidate = (MoveMethod) candidateRefactoring;
			return new MoveMethod(moveMethodCandidate.originalClassFullName, moveMethodCandidate.methodName, moveMethodCandidate.methodSignature, moveMethodCandidate.methodReturnType, moveMethodCandidate.getTargetClassFullName());
		}

		return null;
	}
}
