package simulation.simulateRefactoring_RefDetect;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import simulation.simulateRefactoring.Utility;
import simulation.simulateRefactoring.precondition.MethodRefactoringPreconditions;
import codeInformation.Call_Information.CallingMethods;
import codeInformation.ElementInformation;
import codeInformation.SourceInformation;

public class InlineMethod extends simulation.simulateRefactoring.InlineMethod {

/*******************************************************************************************************
 * Description: InlineMethod()
********************************************************************************************************/
	public InlineMethod(String originalClassFullName, String inlinedMethodName, 
						String inlinedMethodSignature, String inlinedMethodReturnType, 
						String targetClassFullName, String targetMethodName) {
			
		super(originalClassFullName, inlinedMethodName, 
			  inlinedMethodSignature, inlinedMethodReturnType, 
			  targetClassFullName, targetMethodName);
	}
	
/*******************************************************************************************************
 * Description: simulate()
 * 				Note that this version supports a case that a method is inlined in more than one method.
********************************************************************************************************/
		@Override
		public Set<String> simulate(SourceInformation sourceInformation) {

			//As first step checks preconditions.
			MethodRefactoringPreconditions precondition = new MethodRefactoringPreconditions(sourceInformation);
			
			/** The target class and target method should be in the program.*/
			if (!precondition.isPreconditionsValid(targetClassFullName, targetMethodName)) return null;
			
			/** The original class should be in the program.*/
			if (!precondition.isClassInProgram(originalClassFullName)) return null;
			
			/** The method that should be inlined must be in the program.*/
			boolean isMethodInProgram = true;
			if (!precondition.isMethodinClass(originalClassFullName, methodName)) {
				
				isMethodInProgram = false;
				
				if (!isMethodProbablyDeletedByPreviousInline(sourceInformation, 
															 originalClassFullName, methodName, 
															 targetClassFullName, targetMethodName))
					return null;			
			}
			
			List<String> codeAsString = sourceInformation.getCodeAsString();
			List<String> classOrder = sourceInformation.getClassOrder();
			Map<String, ElementInformation> classElementsMap = sourceInformation.getClassElementsMap();

			/** If the method is in the program then remove that. Otherwise, if it is deleted 
			 *  due to a previous inline method refactoring and ignore the next instructions.*/
			if (isMethodInProgram) {
			
				/** Class where the method is defined.*/
				int originalClassIndex = classOrder.indexOf(originalClassFullName);
				Utility.removeElement(codeAsString, originalClassIndex, methodSignature);
				removeMethodfromclassElementsMap(classElementsMap, originalClassFullName);
			}
			
			Set<String> changedClasses = new HashSet<String>();
			updateRefernces(sourceInformation, methodName, changedClasses);
				
			changedClasses.add(originalClassFullName);
			changedClasses.add(targetClassFullName);
			
			return changedClasses;
		}

/*******************************************************************************************************
 * Description: isMethodProbablyDeletedByPreviousInline()
********************************************************************************************************/
	protected boolean isMethodProbablyDeletedByPreviousInline(SourceInformation sourceInformation, 
															  String originalClassFullName, String inlineMethodName, 
															  String targetClassFullName, String targetMethodName) {

		Map<String, Map<String, Map<String, CallingMethods>>> calledInformationMap = sourceInformation.get_CallInformation().getCalledInformationMap();
		
		try {
			
			 Map<String, CallingMethods> callingInformation = calledInformationMap.get(originalClassFullName).get(inlineMethodName);
			
			 for (String callingMethod : callingInformation.get(targetClassFullName).nameList) {
				
				 if (callingMethod.equals(targetMethodName)) return true;
			}
		} catch(NullPointerException e) {}
		
		return false;
	}
}