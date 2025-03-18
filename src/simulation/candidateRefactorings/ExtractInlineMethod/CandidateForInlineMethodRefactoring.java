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

public class CandidateForInlineMethodRefactoring extends CandidateForExtract_InlineMethodRefactoring {

	private Map<String, Map<String, Map<String, CallingMethods>>> initialCalledMap;
	
/*******************************************************************************************************
 * Description: CandidateForExtractMethodRefactoring()
********************************************************************************************************/
	public CandidateForInlineMethodRefactoring() {
		
		Call_Information initialCalledInformation = RefactoringProblem.getInitialSourceInformation().get_CallInformation();
		initialCalledMap = initialCalledInformation.getCalledInformationMap();
	}
	
/*******************************************************************************************************
 * Description: extractCandidaeForInlineMethod()
 * 				For each deleted method we only check other methods in that class, and do not check 
 * 				methods in other classes.
********************************************************************************************************/
	public List<CandidateExtractInlineMethod> extractCandidaeForInlineMethod(Map<String, List<Entity>> deletedMethodList){

		List<CandidateExtractInlineMethod> candidateMethodsForInlineMethod = new ArrayList<CandidateExtractInlineMethod>();
		
		for (Entry<String, List<Entity>> entrySet : deletedMethodList.entrySet()) {
			
			String deletedMethodClassFullName = entrySet.getKey();
			Map<String, Map<String, Set<VariablePair>>> desiredClassCalledVariableMap = desiredCalledVariableMap.get(deletedMethodClassFullName);
			
			//If class is renamed we need to work based on its first name
			if (desiredClassCalledVariableMap == null) {
				
				try {

					String newMethodClassFullName = AdvancedProblem.getRenameClassCandidates().get(deletedMethodClassFullName);
				
					desiredClassCalledVariableMap = desiredCalledVariableMap.get(newMethodClassFullName);
				
				} catch(NullPointerException e) {}
			}
			
			Map<String, Map<String, CallingMethods>> initialClassCalledMap = initialCalledMap.get(deletedMethodClassFullName);
			Map<String, CalledInformation> initialClassCallingMap = initialCallingMap.get(deletedMethodClassFullName);
			Map<String, Map<String, Set<VariablePair>>> initialClassCalledVariableMap = initialCalledVariableMap.get(deletedMethodClassFullName);
			
			if (initialClassCallingMap == null && initialClassCalledVariableMap == null) continue;
			
			for (Entity deletedMethod : entrySet.getValue()) {
				
				String deletedMethodName = deletedMethod.getName();
				
				/** We assume inlined method is called at least once in its original method.*/
				Map<String, Set<String>> MethodsWhichCalledDeletedMethod = new HashMap<String, Set<String>>();
				getMethodsWhichCalledInputMethod(deletedMethodName, deletedMethodClassFullName,
						    	                 initialClassCalledMap, deletedMethodList, MethodsWhichCalledDeletedMethod);
				
				//Test classes are compared with each other, and business classes are compared with each other.
				SourceInformation initialSourceInformation = RefactoringProblem.getInitialSourceInformation();
				removeFalseCallingClasses(deletedMethodClassFullName, MethodsWhichCalledDeletedMethod, initialSourceInformation);
				
				if (MethodsWhichCalledDeletedMethod.isEmpty()) continue;
				
				CalledInformation entitiesCalledByDeletedMethod = null;
				try {entitiesCalledByDeletedMethod = initialClassCallingMap.get(deletedMethodName);}catch(NullPointerException e) {}
				Map<String, Set<VariablePair>> variablesCalledByDeletedMethod = getAccessedVariables(deletedMethodName, initialClassCalledVariableMap);
				
				if (entitiesCalledByDeletedMethod == null && variablesCalledByDeletedMethod == null) continue;
				
				Map<String, Map<String, Double>> initialMethodCandidates = getInitialCandidates(MethodsWhichCalledDeletedMethod, deletedMethod, 
																								entitiesCalledByDeletedMethod, variablesCalledByDeletedMethod, 
																								initialClassCalledVariableMap, desiredClassCalledVariableMap);
				
				//Decide about final candidates.
				List<CandidateExtractInlineMethod> finalCandidates = getFinalCandidates(initialMethodCandidates, deletedMethod, deletedMethodClassFullName);
				candidateMethodsForInlineMethod.addAll(finalCandidates);
			}
		}
		
		return candidateMethodsForInlineMethod;
	}
	
/*******************************************************************************************************
 * Description: getCallingDifferences1()
********************************************************************************************************/
	@Override
	Map<String, Set<Pair>> getCallingDifferences1(String callingMethodName, String callingClassName,
												  String callingMethod_OtherName, String callingClass_OtherName){
		
		CalledInformation firstCalledEntities = null;
		
		try { firstCalledEntities = desiredCallingMap.get(callingClass_OtherName).get(callingMethod_OtherName); } catch(NullPointerException e) {}

		CalledInformation secondCalledEntities = initialCallingMap.get(callingClassName).get(callingMethodName);
		
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
		try {firstCalledVariablesMap = desiredClassCalledVariableMap.get(callingMethod_OtherName);} catch (NullPointerException e) {}
		
		Map<String, Set<VariablePair>> secondCalledVariablesMap = initialClassCalledVariableMap.get(callingMethodName);
		if (secondCalledVariablesMap == null) secondCalledVariablesMap = new HashMap<String, Set<VariablePair>>();
		
		return getCallingVariableDifferences(firstCalledVariablesMap, secondCalledVariablesMap);
	}
	
/*******************************************************************************************************
 * Description: createCandidate()
********************************************************************************************************/
	@Override
	CandidateExtractInlineMethod createCandidate(String deletedMethodClassFullName, Entity deletedMethod, 
												 String callingClassFullName, String targetMethodName){
		
		String deletedMethodName = deletedMethod.getName();
		String deletedMethodSignature = deletedMethod.getSignature();
		String deleteMethodReturnType = deletedMethod.getEntityTypeFullName();
		
		return new CandidateExtractInlineMethod(callingClassFullName, deletedMethodName, deletedMethodSignature, deleteMethodReturnType, 
												deletedMethodClassFullName, targetMethodName);
		
	}
	
/*******************************************************************************************************
 * Description: entitiesCalledByMethod()
 * 				If inlined method calls some deleted entities, they should be ignored.
********************************************************************************************************/
	int entitiesCalledByMethod(CalledInformation entitiesCalledByDeletedMethod){
		
		int result = entitiesCalledByDeletedMethod.calledMethods.size();
		
		Map<String, List<Entity>> deletedMethodList = AdvancedProblem.getInitialCandidateMethodRefactorings().deletedMethods;
		for (Pair calledMethod : entitiesCalledByDeletedMethod.calledMethods) {
			
			try {for (Entity deletedMethod : deletedMethodList.get(calledMethod.entityClassFullName)) {
				
				if (calledMethod.calledEntityName.equals(deletedMethod.getName())) {
					result--;
					break;
				}
			}}catch(NullPointerException e) {}
		}
		
		
		result += entitiesCalledByDeletedMethod.calledFields.size();
		
		Map<String, List<Entity>> deletedFieldList = AdvancedProblem.getInitialCandidateFieldRefactorings().deletedFields;
		for (Pair calledField : entitiesCalledByDeletedMethod.calledFields) {
		
			try {for (Entity deletedField : deletedFieldList.get(calledField.entityClassFullName)) {
				
				if (calledField.calledEntityName.equals(deletedField.getName())) {
					result--;
					break;
				}
			}}catch(NullPointerException e) {}
		}
		
		return result;
	}
}