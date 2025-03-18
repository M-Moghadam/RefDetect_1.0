package simulation.candidateRefactorings.ExtractInlineMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import moea.problem.AdvancedProblem;
import moea.problem.RefactoringProblem;
import simulation.candidateRefactorings.CandidateExtractInlineMethod;
import codeInformation.Call_Information;
import codeInformation.Call_Information.CalledInformation;
import codeInformation.Call_Information.CallingMethods;
import codeInformation.Call_Information.Pair;
import codeInformation.Call_Information.VariablePair;
import codeInformation.Entity;
import codeInformation.SourceInformation;

public class CandidateForExtractMethodRefactoring extends CandidateForExtract_InlineMethodRefactoring {

	private Map<String, Map<String, Map<String, CallingMethods>>> desiredCalledMap;
	
/*******************************************************************************************************
 * Description: CandidateForExtractMethodRefactoring()
********************************************************************************************************/
	public CandidateForExtractMethodRefactoring() {
		
		Call_Information desiredCalledInformation = RefactoringProblem.getDesiredSourceInformation().get_CallInformation();
		desiredCalledMap = desiredCalledInformation.getCalledInformationMap();
	}
	
/*******************************************************************************************************
 * Description: extractCandidaeForExtractMethod()
********************************************************************************************************/
	public List<CandidateExtractInlineMethod> extractCandidaeForExtractMethod(Map<String, List<Entity>> newMethodList){
		
		List<CandidateExtractInlineMethod> candidateMethodsForExtractMethod = new ArrayList<CandidateExtractInlineMethod>();
		
		for (Entry<String, List<Entity>> entrySet : newMethodList.entrySet()) {
			
			String newMethodClassFullName = entrySet.getKey();
			Map<String, Map<String, CallingMethods>> desiredClassCalledMap = desiredCalledMap.get(newMethodClassFullName);
			Map<String, CalledInformation> desiredClassCallingMap = desiredCallingMap.get(newMethodClassFullName);
			Map<String, Map<String, Set<VariablePair>>> desiredClassCalledVariableMap = desiredCalledVariableMap.get(newMethodClassFullName);
			
			Map<String, Map<String, Set<VariablePair>>> initialClassCalledVariableMap = initialCalledVariableMap.get(newMethodClassFullName);
			
			if (initialClassCalledVariableMap == null) {
				
				//If class is renamed we need to work based on its first name
				String initialClassFullName = Utility.getItsOriginalClass(newMethodClassFullName);
				
				initialClassCalledVariableMap = initialCalledVariableMap.get(initialClassFullName);
			}
			
			for (Entity newMethod : entrySet.getValue()) {
				
				String newMethodName = newMethod.getName();
				
				/** We assume extracted method is called at least once in its original method.*/
				Map<String, Set<String>> MethodsWhichCalledNewMethod = new HashMap<String, Set<String>>();
				getMethodsWhichCalledInputMethod(newMethodName, newMethodClassFullName,
												 desiredClassCalledMap, newMethodList, MethodsWhichCalledNewMethod);
				
				//Test classes are compared with each other, and business classes are compared with each other.
				SourceInformation desiredSourceInformation = RefactoringProblem.getDesiredSourceInformation();
				removeFalseCallingClasses(newMethodClassFullName, MethodsWhichCalledNewMethod, desiredSourceInformation);
				
				if (MethodsWhichCalledNewMethod.isEmpty()) continue;
				
				CalledInformation entitiesCalledByNewMethod = null;
				try { entitiesCalledByNewMethod = desiredClassCallingMap.get(newMethodName); } catch(NullPointerException e){}
				
				Map<String, Set<VariablePair>> variablesCalledByNewMethod = getAccessedVariables(newMethodName, desiredClassCalledVariableMap);
				
				if (entitiesCalledByNewMethod == null && variablesCalledByNewMethod == null) continue;
				
				Map<String, Map<String, Double>> initialMethodCandidates = getInitialCandidates(MethodsWhichCalledNewMethod, newMethod,
																								entitiesCalledByNewMethod, variablesCalledByNewMethod, 
																								initialClassCalledVariableMap, desiredClassCalledVariableMap);

				//Decide about final candidates.
				List<CandidateExtractInlineMethod> finalCandidates = getFinalCandidates(initialMethodCandidates, newMethod, newMethodClassFullName);
				candidateMethodsForExtractMethod.addAll(finalCandidates);
			}
		}
		
		return candidateMethodsForExtractMethod;
	}
	
/*******************************************************************************************************
 * Description: getCallingDifferences1()
********************************************************************************************************/
	@Override
	Map<String, Set<Pair>> getCallingDifferences1(String callingMethodName, String callingClassName, 
												  String callingMethod_OtherName, String callingClass_OtherName){
		
		CalledInformation firstCalledEntities = null;
		
		try { firstCalledEntities = initialCallingMap.get(callingClass_OtherName).get(callingMethod_OtherName); } catch(NullPointerException e) {}
		
		CalledInformation secondCalledEntities = desiredCallingMap.get(callingClassName).get(callingMethodName);
		
		return getCallingDifferences(firstCalledEntities, secondCalledEntities);
	}
	
/*******************************************************************************************************
 * Description: getCallingVariableDifferences1()
********************************************************************************************************/
	@Override
	Map<String, Set<VariablePair>> getCallingVariableDifferences1(String callingMethodName, String callingClassName, String callingMethod_OtherName, 
											                      Map<String, Map<String, Set<VariablePair>>> initialClassCalledVariableMap, 
											                      Map<String, Map<String, Set<VariablePair>>> desiredClassCalledVariableMap){
		
		Map<String, Set<VariablePair>> firstCalledVariablesMap = null;
		try {firstCalledVariablesMap = initialClassCalledVariableMap.get(callingMethod_OtherName);} catch (NullPointerException e) {}
		
		Map<String, Set<VariablePair>> secondCalledVariablesMap = desiredClassCalledVariableMap.get(callingMethodName);
		if (secondCalledVariablesMap == null) secondCalledVariablesMap = new HashMap<String, Set<VariablePair>>();
		
		return getCallingVariableDifferences(firstCalledVariablesMap, secondCalledVariablesMap);
	}
	
/*******************************************************************************************************
 * Description: createCandidate()
********************************************************************************************************/
	@Override
	CandidateExtractInlineMethod createCandidate(String newMethodClassFullName, Entity newMethod, 
												 String callingClassFullName, String originalMethodName){
		
		String newMethodName = newMethod.getName();
		String newMethodSignature = newMethod.getSignature();
		String newMethodReturnType = newMethod.getEntityTypeFullName();
		
		return new CandidateExtractInlineMethod(newMethodClassFullName, newMethodName, newMethodSignature, newMethodReturnType, 
												callingClassFullName, originalMethodName);
	}
	
/*******************************************************************************************************
 * Description: entitiesCalledByMethod()
 * 				If extracted method calls some new entities, they should be ignored.
********************************************************************************************************/
	int entitiesCalledByMethod(CalledInformation entitiesCalledByNewMethod){
			
		int result = entitiesCalledByNewMethod.calledMethods.size();
			
		Map<String, List<Entity>> newMethodList = AdvancedProblem.getInitialCandidateMethodRefactorings().newMethods;
		for (Pair calledMethod : entitiesCalledByNewMethod.calledMethods) {
				
			try {for (Entity newMethod : newMethodList.get(calledMethod.entityClassFullName)) {
					
				if (calledMethod.calledEntityName.equals(newMethod.getName())) {
					result--;
					break;
				}
			}}catch(NullPointerException e) {}
		}
			
			
		result += entitiesCalledByNewMethod.calledFields.size();
			
		Map<String, List<Entity>> newFieldList = AdvancedProblem.getInitialCandidateFieldRefactorings().newFields;
		for (Pair calledField : entitiesCalledByNewMethod.calledFields) {
			
			try {for (Entity newField : newFieldList.get(calledField.entityClassFullName)) {
					
				if (calledField.calledEntityName.equals(newField.getName())) {
					result--;
					break;
				}
			}}catch(NullPointerException e) {}
		}
			
		return result;
	}
}