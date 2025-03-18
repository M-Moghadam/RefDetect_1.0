package RMiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.refactoringminer.api.Refactoring;

import gr.uom.java.xmi.UMLAttribute;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.AddParameterRefactoring;
import gr.uom.java.xmi.diff.ChangeVariableTypeRefactoring;
import gr.uom.java.xmi.diff.EncapsulateAttributeRefactoring;
import gr.uom.java.xmi.diff.ExtractClassRefactoring;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.ExtractSuperclassRefactoring;
import gr.uom.java.xmi.diff.InlineOperationRefactoring;
import gr.uom.java.xmi.diff.MoveAndRenameAttributeRefactoring;
import gr.uom.java.xmi.diff.MoveAndRenameClassRefactoring;
import gr.uom.java.xmi.diff.MoveAttributeRefactoring;
import gr.uom.java.xmi.diff.MoveClassRefactoring;
import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import gr.uom.java.xmi.diff.PullUpAttributeRefactoring;
import gr.uom.java.xmi.diff.PullUpOperationRefactoring;
import gr.uom.java.xmi.diff.PushDownAttributeRefactoring;
import gr.uom.java.xmi.diff.PushDownOperationRefactoring;
import gr.uom.java.xmi.diff.RemoveParameterRefactoring;
import gr.uom.java.xmi.diff.RenameAttributeRefactoring;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import gr.uom.java.xmi.diff.ReorderParameterRefactoring;
import moea.problem.AppliedRefactoringsInformation;

public class Utility {

/*******************************************************************************************************
 * Description: getRefactringInformation()
********************************************************************************************************/
	public static List<AppliedRefactoringsInformation> getRefactringInformation(List<Refactoring> refactorings) {
		
		List<AppliedRefactoringsInformation> returnList = new ArrayList<AppliedRefactoringsInformation>();
		
		for (Refactoring refactoring : refactorings) {
			
			AppliedRefactoringsInformation appliedRef = new AppliedRefactoringsInformation();
			
			switch (refactoring.getName()) {

			case "Encapsulate Attribute":
				appliedRef.refactoringType = "EncapsulateField";
				EncapsulateAttributeRefactoring EncapsulateField = (EncapsulateAttributeRefactoring)refactoring;

				UMLAttribute OriginalUMLAttributeEncapsulate = EncapsulateField.getAttributeBefore();
				appliedRef.originalClassFullName = OriginalUMLAttributeEncapsulate.getClassName();
				appliedRef.originalEntityName = OriginalUMLAttributeEncapsulate.getName();
				
				UMLAttribute TargetUMLOperationAttributeEncapsulate = EncapsulateField.getAttributeAfter();
				appliedRef.targetClassFullName = TargetUMLOperationAttributeEncapsulate.getClassName();
				appliedRef.targetEntityName = TargetUMLOperationAttributeEncapsulate.getName();
				
				UMLOperation getterMethod = EncapsulateField.getAddedGetter();
				String GetterMethodName = (getterMethod == null) ? "null" : getMethodName(getterMethod.getKey());
				
				UMLOperation setterMethod = EncapsulateField.getAddedSetter();
				String SetterMethodName = (setterMethod == null) ? "null" : getMethodName(setterMethod.getKey());
				
				appliedRef.description = "EncapsulateField: The Encapsulate Field refactoring has been applied to the Field " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + ", getterMethodName: " + GetterMethodName + ", setterMethodName: " + SetterMethodName + "";
				break;
			
			case "Reorder Parameter":
			case "Change Parameter Type":
			case "Add Parameter":
			case "Remove Parameter":
				changeMethodParameters(appliedRef, refactoring);
				appliedRef.refactoringType = "ChangeMethodParameters";
				appliedRef.description = "ChangeMethodParameters: Parameters of method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is changed to " + appliedRef.targetEntityName + "";
				break;
			
			case "Inline Method": 
				appliedRef.refactoringType = "InlineMethod";
				InlineOperationRefactoring inlineMethod = (InlineOperationRefactoring)refactoring;

				UMLOperation OriginalUMLOperationInline = inlineMethod.getInlinedOperation();
				appliedRef.originalClassFullName = OriginalUMLOperationInline.getClassName();
				appliedRef.originalEntityName = getMethodName(OriginalUMLOperationInline.getKey());
				
				UMLOperation TargetUMLOperationInline = (UMLOperation) inlineMethod.getTargetOperationAfterInline();
				appliedRef.targetClassFullName = TargetUMLOperationInline.getClassName();
				appliedRef.targetEntityName = getMethodName(TargetUMLOperationInline.getKey());
				appliedRef.description = "InlineMethod: Method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is inlined to method " + appliedRef.targetEntityName + " in class " + getClassName(appliedRef.targetClassFullName) + "";
				break;
				
			case "Move And Inline Method": 
				appliedRef.refactoringType = "MoveAndInlineMethod";
				InlineOperationRefactoring inlineMoveMethod = (InlineOperationRefactoring)refactoring;

				UMLOperation OriginalUMLOperationInlineMove = inlineMoveMethod.getInlinedOperation();
				appliedRef.originalClassFullName = OriginalUMLOperationInlineMove.getClassName();
				appliedRef.originalEntityName = getMethodName(OriginalUMLOperationInlineMove.getKey());
				
				UMLOperation TargetUMLOperationInlineMove = (UMLOperation) inlineMoveMethod.getTargetOperationAfterInline();
				appliedRef.targetClassFullName = TargetUMLOperationInlineMove.getClassName();
				appliedRef.targetEntityName = getMethodName(TargetUMLOperationInlineMove.getKey());
				appliedRef.description = "MoveAndInlineMethod: Method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is inlined to method " + appliedRef.targetEntityName + " in class " + getClassName(appliedRef.targetClassFullName) + "";
				break;
				

			case "Extract And Move Method": 
				appliedRef.refactoringType = "ExtractAndMoveMethod";
				ExtractOperationRefactoring extractMoveMethod = (ExtractOperationRefactoring)refactoring;
				
				UMLOperation OriginalUMLOperationExtractMove = extractMoveMethod.getExtractedOperation();
				appliedRef.targetClassFullName = OriginalUMLOperationExtractMove.getClassName();
				appliedRef.targetEntityName = getMethodName(OriginalUMLOperationExtractMove.getKey());
				
				UMLOperation TargetUMLOperationExtractMove = (UMLOperation) extractMoveMethod.getSourceOperationBeforeExtraction();
				appliedRef.originalClassFullName = TargetUMLOperationExtractMove.getClassName();
				appliedRef.originalEntityName = getMethodName(TargetUMLOperationExtractMove.getKey());
				appliedRef.description = "ExtractAndMoveMethod: Method " + appliedRef.targetEntityName + " in class " + getClassName(appliedRef.targetClassFullName) + " is extracted from method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + "";
				break;
				
			case "Extract Method": 
				appliedRef.refactoringType = "ExtractMethod";
				ExtractOperationRefactoring extractMethod = (ExtractOperationRefactoring)refactoring;
				
				UMLOperation OriginalUMLOperationExtract = extractMethod.getExtractedOperation();
				appliedRef.targetClassFullName = OriginalUMLOperationExtract.getClassName();
				appliedRef.targetEntityName = getMethodName(OriginalUMLOperationExtract.getKey());
				
				UMLOperation TargetUMLOperationExtract = (UMLOperation) extractMethod.getSourceOperationBeforeExtraction();
				appliedRef.originalClassFullName = TargetUMLOperationExtract.getClassName();
				appliedRef.originalEntityName = getMethodName(TargetUMLOperationExtract.getKey());
				appliedRef.description = "ExtractMethod: Method " + appliedRef.targetEntityName + " in class " + getClassName(appliedRef.targetClassFullName) + " is extracted from method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + "";
				break;
				

			case "Rename Method":
				appliedRef.refactoringType = "RenameMethod";
				RenameOperationRefactoring renameMethod = (RenameOperationRefactoring)refactoring;
				
				UMLOperation OriginalUMLOperationRename = renameMethod.getOriginalOperation();
				appliedRef.originalClassFullName = OriginalUMLOperationRename.getClassName();
				appliedRef.originalEntityName = getMethodName(OriginalUMLOperationRename.getKey());
				
				UMLOperation TargetUMLOperationRename = renameMethod.getRenamedOperation();
				appliedRef.targetEntityName = getMethodName(TargetUMLOperationRename.getKey());
				appliedRef.targetClassFullName = TargetUMLOperationRename.getClassName();
				
				appliedRef.description = "RenameMethod: Method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is renamed to " + appliedRef.targetEntityName + "";
				break;
			
				
			case "Move And Rename Method":
				appliedRef.refactoringType = "MoveAndRenameMethod";
				MoveOperationRefactoring MoveRenameMethod = (MoveOperationRefactoring)refactoring;
				
				UMLOperation OriginalUMLOperationRenameMove = MoveRenameMethod.getOriginalOperation();
				appliedRef.originalClassFullName = OriginalUMLOperationRenameMove.getClassName();
				appliedRef.originalEntityName = getMethodName(OriginalUMLOperationRenameMove.getKey());
				
				UMLOperation TargetUMLOperationRenameMove = MoveRenameMethod.getMovedOperation();
				appliedRef.targetEntityName = getMethodName(TargetUMLOperationRenameMove.getKey());
				appliedRef.targetClassFullName = TargetUMLOperationRenameMove.getClassName();
				appliedRef.description = "MoveAndRenameMethod: Method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is renamed to " + appliedRef.targetEntityName + " and moved to class " + getClassName(appliedRef.targetClassFullName) +"";
				break;
				

			case "Rename Attribute":
				appliedRef.refactoringType = "RenameField";
				RenameAttributeRefactoring renameField = (RenameAttributeRefactoring)refactoring;
				
				UMLAttribute OriginalUMLAttributeRename = renameField.getOriginalAttribute();
				appliedRef.originalClassFullName = OriginalUMLAttributeRename.getClassName();
				appliedRef.originalEntityName = OriginalUMLAttributeRename.getName();
				
				UMLAttribute TargetUMLAttributeRename = renameField.getRenamedAttribute();
				appliedRef.targetEntityName = TargetUMLAttributeRename.getName();
				appliedRef.description = "RenameField: Field " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is rename to " + appliedRef.targetEntityName + "";
				break;
			
			case "Move And Rename Attribute":
				appliedRef.refactoringType = "MoveAndRenameField";
				MoveAndRenameAttributeRefactoring MoveRenameField = (MoveAndRenameAttributeRefactoring)refactoring;
				
				UMLAttribute OriginalUMLAttributeRenameMove = MoveRenameField.getOriginalAttribute();
				appliedRef.originalClassFullName = OriginalUMLAttributeRenameMove.getClassName();
				appliedRef.originalEntityName = OriginalUMLAttributeRenameMove.getName();
				
				UMLAttribute TargetUMLAttributeRenameMove = MoveRenameField.getMovedAttribute();
				appliedRef.targetEntityName = TargetUMLAttributeRenameMove.getName();
				appliedRef.targetClassFullName = TargetUMLAttributeRenameMove.getClassName();
				appliedRef.description = "MoveAndRenameField: Field " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is renamed to " + appliedRef.targetEntityName + " and moved to class " + getClassName(appliedRef.targetClassFullName) +"";
				break;

			case "Rename Class":
				appliedRef.refactoringType = "RenameClass";
				RenameClassRefactoring renameClass = (RenameClassRefactoring)refactoring;
				appliedRef.originalClassFullName = renameClass.getOriginalClassName();
				appliedRef.targetClassFullName = renameClass.getRenamedClassName();
				appliedRef.description = "RenameClass: Class " + appliedRef.originalClassFullName + " is Rename to " + appliedRef.targetClassFullName + "";
				break;
				
			case "Move Class":
				appliedRef.refactoringType = "MoveClass";
				MoveClassRefactoring moveClass = (MoveClassRefactoring)refactoring;
				appliedRef.originalClassFullName = moveClass.getOriginalClassName();
				appliedRef.targetClassFullName = moveClass.getMovedClassName();
				appliedRef.description = "MoveClass: Class " + appliedRef.originalClassFullName + " is Move to " + appliedRef.targetClassFullName + "";
				break;
				
				
			case "Move And Rename Class":
				appliedRef.refactoringType = "MoveAndRenameClass";
				MoveAndRenameClassRefactoring moveRenameClass = (MoveAndRenameClassRefactoring)refactoring;
				appliedRef.originalClassFullName = moveRenameClass.getOriginalClassName();
				appliedRef.targetClassFullName = moveRenameClass.getRenamedClassName();
				appliedRef.description = "MoveAndRenameClass: Class " + appliedRef.originalClassFullName + " is MoveAndRename to " + appliedRef.targetClassFullName + "";
				break;	
	
			
			case "Extract Subclass":
				appliedRef.refactoringType = "ExtractSubClass";
				ExtractClassRefactoring extractSubClass = (ExtractClassRefactoring)refactoring;
				appliedRef.originalClassFullName = extractSubClass.getExtractedClass().getName();
				appliedRef.targetClassFullName = extractSubClass.getOriginalClass().getName();
				appliedRef.description = "Extract SubClass: Subclass " + getClassName(appliedRef.originalClassFullName) + " is extracted from parent class " + getClassName(appliedRef.targetClassFullName) + "";
				break;
				
			
			case "Extract Class": 
				appliedRef.refactoringType = "ExtractClass";
				ExtractClassRefactoring extractClass = (ExtractClassRefactoring)refactoring;
				appliedRef.originalClassFullName = extractClass.getExtractedClass().getName();
				appliedRef.targetClassFullName = extractClass.getOriginalClass().getName();
				appliedRef.description = "ExtractClass: Class " + getClassName(appliedRef.originalClassFullName) + " is extracted from class " + getClassName(appliedRef.targetClassFullName) + "";
				break;
				
				
			case "Extract Superclass": 
				appliedRef.refactoringType = "ExtractSuperClass";
				ExtractSuperclassRefactoring extractsuperClass = (ExtractSuperclassRefactoring)refactoring;
				UMLClass UMLSuperClass = extractsuperClass.getExtractedClass();
				appliedRef.originalClassFullName = UMLSuperClass.getName();

				String subclasses1 = "";
				for (String subClassName : getChildrenClassesAsSort(extractsuperClass.getSubclassSetBefore())) {
					subclasses1 += getClassName(subClassName) + ",";
					appliedRef.targetClassFullName +=  subClassName + ",";
				}
				subclasses1 = subclasses1.substring(0, subclasses1.length() - 1);;
				appliedRef.targetClassFullName = appliedRef.targetClassFullName.substring(0, appliedRef.targetClassFullName.length() - 1);;
				appliedRef.description = "ExtractSuperClass: Superclass " + getClassName(appliedRef.originalClassFullName) + " is extracted from children classes " + subclasses1 + "";
				break;
			
					
			case "Extract Interface": 
				appliedRef.refactoringType = "ExtractInterface";
				ExtractSuperclassRefactoring extractInterface = (ExtractSuperclassRefactoring)refactoring;
				UMLClass UMLInterface = extractInterface.getExtractedClass();
				appliedRef.originalClassFullName = UMLInterface.getName();
				
				String subclasses = "";
				for (String subClassName : getChildrenClassesAsSort(extractInterface.getSubclassSetBefore())) {
					subclasses += getClassName(subClassName) + ",";
					appliedRef.targetClassFullName +=  subClassName + ",";
				}
				subclasses = subclasses.substring(0, subclasses.length() - 1);;
				appliedRef.targetClassFullName = appliedRef.targetClassFullName.substring(0, appliedRef.targetClassFullName.length() - 1);;
				appliedRef.description = "ExtractInterface: Interface " + getClassName(appliedRef.originalClassFullName) + " is extracted from classes " + subclasses + "";
				break;


			case "Pull Up Attribute": 
				appliedRef.refactoringType = "PullUpField";
				PullUpAttributeRefactoring pullUpField = (PullUpAttributeRefactoring)refactoring;
				
				UMLAttribute OriginalUMLAttributePullUp = pullUpField.getOriginalAttribute();
				appliedRef.originalClassFullName = OriginalUMLAttributePullUp.getClassName();
				appliedRef.originalEntityName = OriginalUMLAttributePullUp.getName();
				
				UMLAttribute TargetUMLAttributePullUp = pullUpField.getMovedAttribute();
				appliedRef.targetClassFullName = TargetUMLAttributePullUp.getClassName();
				appliedRef.description = "PullUpField: Field " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is pulled up to class " + getClassName(appliedRef.targetClassFullName) + "";
				break;
				

			case "Pull Up Method": 
				appliedRef.refactoringType = "PullUpMethod";
				PullUpOperationRefactoring pullUpMethod = (PullUpOperationRefactoring)refactoring;

				UMLOperation OriginalUMLOperationPullUp = pullUpMethod.getOriginalOperation();
				appliedRef.originalClassFullName = OriginalUMLOperationPullUp.getClassName();
				appliedRef.originalEntityName = getMethodName(OriginalUMLOperationPullUp.getKey());
				
				UMLOperation TargetUMLOperationPullUp = pullUpMethod.getMovedOperation();
				appliedRef.targetClassFullName = TargetUMLOperationPullUp.getClassName();
				appliedRef.description = "PullUpMethod: Method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is pulled up to class " + getClassName(appliedRef.targetClassFullName) + "";
				break;
				

			case "Push Down Attribute": 
				appliedRef.refactoringType = "PushDownField";
				PushDownAttributeRefactoring pushDownField = (PushDownAttributeRefactoring)refactoring;
				
				UMLAttribute OriginalUMLAttributePushDown = pushDownField.getOriginalAttribute();
				appliedRef.originalClassFullName = OriginalUMLAttributePushDown.getClassName();
				appliedRef.originalEntityName = OriginalUMLAttributePushDown.getName();
				
				UMLAttribute TargetUMLAttributePushDown = pushDownField.getMovedAttribute();
				appliedRef.targetClassFullName = TargetUMLAttributePushDown.getClassName();
				appliedRef.description = "PushDownField: Field " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is pushed down to class " + getClassName(appliedRef.targetClassFullName) + "";
				break;
				

			case "Push Down Method": 
				appliedRef.refactoringType = "PushDownMethod";
				PushDownOperationRefactoring pushDownMethod = (PushDownOperationRefactoring)refactoring;
				
				UMLOperation OriginalUMLOperationPushDown = pushDownMethod.getOriginalOperation();
				appliedRef.originalClassFullName = OriginalUMLOperationPushDown.getClassName();
				appliedRef.originalEntityName = getMethodName(OriginalUMLOperationPushDown.getKey());
				
				UMLOperation TargetUMLOperationPushDown = pushDownMethod.getMovedOperation();
				appliedRef.targetClassFullName = TargetUMLOperationPushDown.getClassName();
				appliedRef.description = "PushDownMethod: Method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is pushed down to class " + getClassName(appliedRef.targetClassFullName) + "";
				break;


			case "Move Attribute": 
				appliedRef.refactoringType = "MoveField";
				MoveAttributeRefactoring moveField = (MoveAttributeRefactoring)refactoring;
				
				UMLAttribute OriginalUMLAttributeMove = moveField.getOriginalAttribute();
				appliedRef.originalClassFullName = OriginalUMLAttributeMove.getClassName();
				appliedRef.originalEntityName = OriginalUMLAttributeMove.getName();
				
				UMLAttribute TargetUMLAttributeMove = moveField.getMovedAttribute();
				appliedRef.targetClassFullName = TargetUMLAttributeMove.getClassName();
				appliedRef.description = "MoveField: Field " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is moved to class " + getClassName(appliedRef.targetClassFullName) + "";
				break;
							

			case "Move Method":
				appliedRef.refactoringType = "MoveMethod";
				MoveOperationRefactoring moveMethod = (MoveOperationRefactoring)refactoring;
				
				UMLOperation OriginalUMLOperationMove = moveMethod.getOriginalOperation();
				appliedRef.originalClassFullName = OriginalUMLOperationMove.getClassName();
				appliedRef.originalEntityName = getMethodName(OriginalUMLOperationMove.getKey());
				
				UMLOperation TargetUMLOperationMove = moveMethod.getMovedOperation();
				appliedRef.targetClassFullName = TargetUMLOperationMove.getClassName();
				
				appliedRef.description = "MoveMethod: Method " + appliedRef.originalEntityName + " in class " + getClassName(appliedRef.originalClassFullName) + " is moved to class " + getClassName(appliedRef.targetClassFullName) + "";
				break;
			}
			
			if (!appliedRef.refactoringType.isEmpty())
				returnList.add(appliedRef);
		}
		
		reviseList(returnList);
		
		return returnList;
	}

/*******************************************************************************************************
 * Description: reviseList
********************************************************************************************************/
	private static void reviseList(List<AppliedRefactoringsInformation> appliedRefactorings) {
		
		deleteDuplicateRefactorings(appliedRefactorings);
		
		CreateMoveAndChangeMethodParametersRefactoring(appliedRefactorings);
		
		updateChangeMethodParameters_RenameMethodRefactorings(appliedRefactorings);
		
		Mix_Rename_Move_ChangeMethodParameters(appliedRefactorings);
	}
	
/*******************************************************************************************************
 * Description: deleteDuplicateRefactorings
 * 				RMiner returns duplicate refactorings. This function removes duplicate ones.
********************************************************************************************************/
	private static void deleteDuplicateRefactorings(List<AppliedRefactoringsInformation> appliedRefactorings) {
		
		for (int i = 0; i < appliedRefactorings.size(); i++) {
			
			AppliedRefactoringsInformation appliedRef1 = appliedRefactorings.get(i);
			
			for (int j = i + 1; j < appliedRefactorings.size(); j++) {
				
				if (appliedRefactorings.get(j).description.equals(appliedRef1.description)) {
					appliedRefactorings.remove(j);
					j--;
				}
			}
		}
	}
	
/*******************************************************************************************************
 * Description: Mix_Rename_Move_ChangeMethodParameters
 * 				When we have Rename Method, Move Method & Change Method Parameters, we create a single
 * 				refactorings. 
********************************************************************************************************/
	private static void Mix_Rename_Move_ChangeMethodParameters(List<AppliedRefactoringsInformation> appliedRefactorings){
		
		for (int i = 0; i < appliedRefactorings.size(); i++) {
			
			AppliedRefactoringsInformation ref1 = appliedRefactorings.get(i);
			
			if (!ref1.refactoringType.equals("ChangeMethodParameters")) continue;

			for (AppliedRefactoringsInformation ref2 : appliedRefactorings) {

				if (!ref2.refactoringType.equals("MoveAndRenameMethod")) continue;

				if (ref1.originalClassFullName.equals(ref2.originalClassFullName) && 
					ref1.originalEntityName.equals(ref2.originalEntityName) &&
					ref1.targetEntityName.equals(ref2.targetEntityName)) {

					
					//We change ref2 and remove ref1
					ref2.refactoringType = "MoveAndRenameAndChangeMethodParameters";
					ref2.description = "MoveAndRenameAndChangeMethodParameters: Method " + ref2.originalEntityName + " in class " + getClassName(ref2.originalClassFullName) + " is renamed and its parameters changed to " + ref2.targetEntityName + " and moved to class " + getClassName(ref2.targetClassFullName) +"";

					
					appliedRefactorings.remove(ref1);
					i--;
					break;
				}
			}
		}
	}	
	
/*******************************************************************************************************
 * Description: updateChangeMethodParameters_RenameMethodRefactorings
 * 				When we have both Rename Method & Change Method Parameters, the description for RMiner and 
 * 				our tool are different. Therefore, using this function we change description of RMiner to be 
 * 				matched with description of our tool.
********************************************************************************************************/
	private static void updateChangeMethodParameters_RenameMethodRefactorings(List<AppliedRefactoringsInformation> appliedRefactorings){
	
		for (AppliedRefactoringsInformation ref1 : appliedRefactorings) {
			
			if (!ref1.refactoringType.equals("ChangeMethodParameters")) continue;
			
			for (AppliedRefactoringsInformation ref2 : appliedRefactorings) {
			
				if (!ref2.refactoringType.equals("RenameMethod")) continue;
				
				if (ref1.originalClassFullName.equals(ref2.originalClassFullName) && 
					ref1.originalEntityName.equals(ref2.originalEntityName) &&
					ref1.targetEntityName.equals(ref2.targetEntityName)) {
					
					ref1.originalEntityName = getTargetMethodName_OriginalMethodParameters(ref1.originalEntityName, ref1.targetEntityName);
					ref1.description = "ChangeMethodParameters: Parameters of method " + ref1.originalEntityName + " in class " + getClassName(ref1.originalClassFullName) + " is changed to " + ref1.targetEntityName + "";

					ref2.targetEntityName = getTargetMethodName_OriginalMethodParameters(ref2.originalEntityName, ref2.targetEntityName);
					ref2.description = "RenameMethod: Method " + ref2.originalEntityName + " in class " + getClassName(ref2.originalClassFullName) + " is renamed to " + ref2.targetEntityName + "";
				}
			}
			
			/** RMiner in some cases detects only change Method Parameters instead of Change Method Parameters and Rename Method.
			 *  I update Change Method Parameters to be matched with our algorithm results.*/
			String methodName1 = ref1.originalEntityName;
			String methodName2 = ref1.targetEntityName;
			int index1 = methodName1.lastIndexOf("(");
			int index2 = methodName2.lastIndexOf("(");
			
			if (methodName1.substring(0, index1).equals(methodName2.substring(0, index2))) continue;
			
			String newName = methodName2.substring(0, index2) + methodName1.substring(index1);
			
			ref1.description = "ChangeMethodParameters: Parameters of method " + newName + " in class " + getClassName(ref1.originalClassFullName) + " is changed to " + ref1.targetEntityName + "";
		}
	}
	
/*******************************************************************************************************
 * Description: getTargetMethodName_OriginalMethodParameters
********************************************************************************************************/
	private static String getTargetMethodName_OriginalMethodParameters(String originalMethodName, String targetMethodName) {
		
		String originalMethodParameters = originalMethodName.substring(originalMethodName.indexOf("("));
		
		String targetName = targetMethodName.substring(0, targetMethodName.indexOf("("));
		
		return targetName + originalMethodParameters;
	}
	
/*******************************************************************************************************
 * Description: CreateMoveAndChangeMethodParametersRefactoring
********************************************************************************************************/
	private static void CreateMoveAndChangeMethodParametersRefactoring(List<AppliedRefactoringsInformation> appliedRefactorings){
			
		//Create Move and Change Parameters.
		for (int i = 0; i < appliedRefactorings.size(); i++) {

			if (appliedRefactorings.get(i).refactoringType.equals("MoveMethod")) {

				AppliedRefactoringsInformation moveRefactoring = appliedRefactorings.get(i);

				for (int j = 0; j < appliedRefactorings.size(); j++) {

					AppliedRefactoringsInformation appliedRef2 = appliedRefactorings.get(j);

					if (appliedRef2.refactoringType.equals("ChangeMethodParameters") && 
						moveRefactoring.originalClassFullName.equals(appliedRef2.originalClassFullName) &&
						moveRefactoring.originalEntityName.equals(appliedRef2.originalEntityName)) {

						moveRefactoring.refactoringType = "MoveAndChangeMethodParameters";
						moveRefactoring.targetEntityName = appliedRef2.targetEntityName;
						moveRefactoring.description = "MoveAndChangeMethodParameters: Method " + appliedRef2.originalEntityName + " in class " + getClassName(appliedRef2.originalClassFullName) + " is changed to " + appliedRef2.targetEntityName + " and moved to class " + getClassName(moveRefactoring.targetClassFullName) +"";
						appliedRefactorings.remove(j);
						if (i > j) i--;
						break;
					}
				}
			}	
		}
	}

/*******************************************************************************************************
 * Description: getChildrenClassesAsSort
********************************************************************************************************/
	private static List<String> getChildrenClassesAsSort(Set<String> subclasses){
		
		List<String> subClassList = new ArrayList<String>();
		
		for (String subClass : subclasses) {
			subClassList.add(subClass);
		}
		Collections.sort(subClassList);
		
		return subClassList;
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
	private static String getMethodName(String MethodFullName) {
		
		String methodName = MethodFullName.substring(MethodFullName.lastIndexOf("#") + 1);
		methodName = methodName.replace(" ", "");
		
		String[] parts1 = methodName.split("\\(");
		String result = getSimpleName(parts1[0]) + "(";
		
		String[] parts2 = parts1[1].split(",");
		
		for (String part : parts2) {
			
			int index = part.lastIndexOf(".");
			if (index != -1) {
				part = part.substring(index + 1);
			}
			result += part + ",";
		}
		
		return result.substring(0, result.length() - 1);
	}
	
/*******************************************************************************************************
 * Description: getClassName
********************************************************************************************************/
	private static String getClassName(String classFullName) {
		return classFullName.substring(classFullName.lastIndexOf(".") + 1);
	}
	
/*******************************************************************************************************
 * Description: changeMethodParameters()
********************************************************************************************************/
	private static void changeMethodParameters(AppliedRefactoringsInformation appliedRef, Refactoring refactoring){
			
		switch(refactoring.getName()) {
				
		case "Reorder Parameter": 
			ReorderParameterRefactoring changeReorderMethodParameters = (ReorderParameterRefactoring)refactoring;
			UMLOperation UMLOperationBeforeReorder = changeReorderMethodParameters.getOperationBefore();
			appliedRef.originalClassFullName = UMLOperationBeforeReorder.getClassName();
			appliedRef.originalEntityName = getMethodName(UMLOperationBeforeReorder.getKey());
			UMLOperation UMLOperationAfterReorder = changeReorderMethodParameters.getOperationAfter();
			appliedRef.targetEntityName = getMethodName(UMLOperationAfterReorder.getKey());
			appliedRef.targetClassFullName = UMLOperationAfterReorder.getClassName();
			break;

		case "Change Parameter Type":
			ChangeVariableTypeRefactoring changeMethodParametersType = (ChangeVariableTypeRefactoring)refactoring;
			UMLOperation UMLOperationBeforeChangeType = (UMLOperation) changeMethodParametersType.getOperationBefore();
			appliedRef.originalClassFullName = UMLOperationBeforeChangeType.getClassName();
			appliedRef.originalEntityName = getMethodName(UMLOperationBeforeChangeType.getKey());
			UMLOperation UMLOperationAfterChangeType = (UMLOperation) changeMethodParametersType.getOperationAfter();
			appliedRef.targetEntityName = getMethodName(UMLOperationAfterChangeType.getKey());
			appliedRef.targetClassFullName = UMLOperationAfterChangeType.getClassName();
			break;

		case "Remove Parameter":
			RemoveParameterRefactoring removeMethodParameters = (RemoveParameterRefactoring)refactoring;
			UMLOperation UMLOperationBeforeRemovePar = removeMethodParameters.getOperationBefore();
			appliedRef.originalClassFullName = UMLOperationBeforeRemovePar.getClassName();
			appliedRef.originalEntityName = getMethodName(UMLOperationBeforeRemovePar.getKey());
			UMLOperation UMLOperationAfterRemovePar = removeMethodParameters.getOperationAfter();
			appliedRef.targetEntityName = getMethodName(UMLOperationAfterRemovePar.getKey());
			appliedRef.targetClassFullName = UMLOperationAfterRemovePar.getClassName();
			break;

		case "Add Parameter":
			AddParameterRefactoring addMethodParameters = (AddParameterRefactoring)refactoring;
			UMLOperation UMLOperationBeforeAddPar = addMethodParameters.getOperationBefore();
			appliedRef.originalClassFullName = UMLOperationBeforeAddPar.getClassName();
			appliedRef.originalEntityName = getMethodName(UMLOperationBeforeAddPar.getKey());
			UMLOperation UMLOperationAfterAddPar = addMethodParameters.getOperationAfter();
			appliedRef.targetEntityName = getMethodName(UMLOperationAfterAddPar.getKey());
			appliedRef.targetClassFullName = UMLOperationAfterAddPar.getClassName();
			break;
		}
	}
}