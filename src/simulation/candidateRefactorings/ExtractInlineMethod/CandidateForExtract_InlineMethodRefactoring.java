package simulation.candidateRefactorings.ExtractInlineMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import codeInformation.Call_Information;
import codeInformation.Call_Information.CalledInformation;
import codeInformation.Call_Information.CallingMethods;
import codeInformation.Call_Information.Pair;
import codeInformation.Call_Information.VariablePair;
import codeInformation.ElementInformation;
import codeInformation.Entity;
import codeInformation.SourceInformation;
import moea.problem.RefactoringProblem;
import simulation.candidateRefactorings.CandidateExtractInlineMethod;
import simulation.candidateRefactorings.DiffAlgorithm.Service;
//import translation.validation.java.NeedOnlyForTranslationJavaToKotlin;

public abstract class CandidateForExtract_InlineMethodRefactoring {
	
	Map<String, Map<String, CalledInformation>> initialCallingMap;
	Map<String, Map<String, Map<String, Set<VariablePair>>>> initialCalledVariableMap;
	Map<String, ElementInformation> initialClassElementMap;
	
	Map<String, Map<String, CalledInformation>> desiredCallingMap;
	Map<String, Map<String, Map<String, Set<VariablePair>>>> desiredCalledVariableMap;
	Map<String, ElementInformation> desiredClassElementMap;
	
/*******************************************************************************************************
 * Description: CandidateForExtract_InlineMethodRefactoring
********************************************************************************************************/
	public CandidateForExtract_InlineMethodRefactoring() {
		
		Call_Information initialCalledInformation = RefactoringProblem.getInitialSourceInformation().get_CallInformation();
		initialCallingMap = initialCalledInformation.getCallingInformationMap();
		initialCalledVariableMap = initialCalledInformation.getCalledVariableInformationMap();
		initialClassElementMap = RefactoringProblem.getInitialSourceInformation().getClassElementsMap();
		
		Call_Information desiredCalledInformation = RefactoringProblem.getDesiredSourceInformation().get_CallInformation();
		desiredCallingMap = desiredCalledInformation.getCallingInformationMap();
		desiredCalledVariableMap = desiredCalledInformation.getCalledVariableInformationMap();
		desiredClassElementMap = RefactoringProblem.getDesiredSourceInformation().getClassElementsMap();
	}
	
/*******************************************************************************************************
 * Description: getInitialCandidates()
 * 				If the function is called by Extract Method, then the method is the newly extracted method, 
 * 				and if the function is called by Inline Method, it is the method which is inlined. 
********************************************************************************************************/
	Map<String, Map<String, Double>> getInitialCandidates(Map<String, Set<String>> MethodsWhichCalledMethod, Entity method,
														  CalledInformation entitiesCalledByMethod, Map<String, Set<VariablePair>> variablesCalledByMethod, 
														  Map<String, Map<String, Set<VariablePair>>> initialClassCalledVariableMap,
														  Map<String, Map<String, Set<VariablePair>>> desiredClassCalledVariableMap){
		
		String methodSignature = method.getSignature();
		
		Map<String, Map<String, Double>> initialMethodCandidates = new HashMap<String, Map<String, Double>>();
		
		for (Entry<String, Set<String>> entry : MethodsWhichCalledMethod.entrySet()) {
			
			String callingClassName = entry.getKey();
			
			Map<String, Double> temp = new HashMap<String, Double>();
			
			for (String callingMethodName : entry.getValue()) {
				
				//To cover a case that calling class or calling method are renamed. 
				Set<CallingMethodInformation> callingMethodInformation = Utility.getMethodInformation(callingMethodName, callingClassName, this);
				
				for (CallingMethodInformation callingMethodInfo : callingMethodInformation) {
					
					String callingClass_OtherName = callingMethodInfo.classFullName;
					String callingMethod_OtherName = callingMethodInfo.methodName;
				
					double similarEntityCalls = 0;
					int differencesSize = 0;
				
					if (entitiesCalledByMethod != null) {
					
						Map<String, Set<Pair>> entityDifferences = getCallingDifferences1(callingMethodName, callingClassName, 
																					  	  callingMethod_OtherName, callingClass_OtherName);
				
						//If there is not any difference between methods, no need to check similarity
						if (!entityDifferences.isEmpty()) {
							similarEntityCalls = findSimilarEntityCalls(entitiesCalledByMethod, entityDifferences);
							differencesSize = entityDifferences.size();
						}
					}
				
					double similarVariableCalls = 0;
					if (variablesCalledByMethod != null) {
					
						Map<String, Set<VariablePair>> variableDifferences = getCallingVariableDifferences1(callingMethodName, callingClassName, callingMethod_OtherName, 
																									    	initialClassCalledVariableMap, desiredClassCalledVariableMap);
						
						if (!variableDifferences.isEmpty())  {
							similarVariableCalls += findSimilarVariableCalls(variablesCalledByMethod, variableDifferences);
							differencesSize += variableDifferences.size();
						}
					}
				
					//Now measure similarity
					double similarity = similarEntityCalls + similarVariableCalls;
				
					if (similarity != 0  || differencesSize != 0) {
				
						//Parameters of the new method should be counted as similarity between new method and the refactored original method.
						int numberofParameters = methodSignature.length() - methodSignature.replace("P", "").length();
						similarity += numberofParameters / 2.0;
						
						similarity /= getAllCalls(entitiesCalledByMethod, variablesCalledByMethod);
						
						temp.put(callingMethodName, similarity);
					}
				}
			}
			
			if (!temp.isEmpty()) initialMethodCandidates.put(callingClassName, temp);
		}
		
		return initialMethodCandidates;
	}
	
/*******************************************************************************************************
 * Description: getFinalCandidates()
 * 				If the function is called by Extract Method then the method is newly created method (extracted one),
 * 				if the function is called by Inline Method then the method is deleted method (inlined one).
 * 				classFullName is the class that the method will be added (in extracted case), or the class that the method was there (in inlined case).
********************************************************************************************************/	
	List<CandidateExtractInlineMethod> getFinalCandidates(Map<String, Map<String, Double>> initialMethodCandidates, Entity method, String classFullName) {
		
		List<CandidateExtractInlineMethod> candidateMethodsFor_Extract_Or_Inline_Method = new ArrayList<CandidateExtractInlineMethod>();
		
		/** Decide about Extract Method candidate.*/
		double similarityThreshold = Utility.decideAboutThreshold(initialMethodCandidates.values());
		
		for (Entry<String, Map<String, Double>> entry1 : initialMethodCandidates.entrySet()) {
			
			String callingClass = entry1.getKey();
			Map<String, Double> values = entry1.getValue();
		
			for (Entry<String, Double> entry2 : values.entrySet()) {
				
				Double similarity = entry2.getValue();
				
				if (similarity >= similarityThreshold) {
			
					CandidateExtractInlineMethod candidate = createCandidate(classFullName, method, callingClass, entry2.getKey());
					
					candidateMethodsFor_Extract_Or_Inline_Method.add(candidate);
				}
			}
		}
		
		return candidateMethodsFor_Extract_Or_Inline_Method;
	}
	
/*******************************************************************************************************
 * Description: createCandidate
********************************************************************************************************/
	abstract CandidateExtractInlineMethod createCandidate(String classFullName, Entity method, 
														  String callingClass, String methodName);
	
/*******************************************************************************************************
 * Description: getMethodsWhichCalledInputMethod
 * 				This method returns methods that called the input method (Extracted or inline method).
********************************************************************************************************/
	void getMethodsWhichCalledInputMethod(String inputMethodName, String inputMethodClassFullName,
									      Map<String, Map<String, CallingMethods>> classCalledInformationMap,
									      Map<String, List<Entity>> new_or_delete_MethodList, 
									      Map<String, Set<String>> callingMethodsMap) {
		
		getMethodsWhichCalledInputMethod(inputMethodName, inputMethodClassFullName, 
									     classCalledInformationMap, new_or_delete_MethodList, 
									     callingMethodsMap, 0);
		
	}
	
/*******************************************************************************************************
 * Description: getMethodsWhichCalledInputMethod
 * 				This method returns methods that called the input method (Extracted or inline method).
********************************************************************************************************/
	private void getMethodsWhichCalledInputMethod(String inputMethodName, String inputMethodClassFullName,
										  		  Map<String, Map<String, CallingMethods>> classCalledInformationMap,
										          Map<String, List<Entity>> new_or_delete_MethodList, 
										          Map<String, Set<String>> callingMethodsMap, int counter) {
				
		if (classCalledInformationMap == null) return;
		
		Map<String, CallingMethods> callingClassInformation = classCalledInformationMap.get(inputMethodName);
		
		//Map<String, CallingMethods> callingClassInformation = NeedOnlyForTranslationJavaToKotlin.get(classCalledInformationMap, inputMethodName);
		
		if (callingClassInformation == null) return;
		
		
		for (Entry<String, CallingMethods> entry : callingClassInformation.entrySet()) {
			
			String callingClass = entry.getKey();
			
			Set<String> callingMethods = new HashSet<String>();
			
			for (String callingMethod : entry.getValue().nameList) {
				
				//Prevent recursive calls
				if (callingMethod.equals(inputMethodName) && callingClass.equals(inputMethodClassFullName)) continue;
				
				callingMethods.add(callingMethod);
				
				//If calling method is itself deleted or new, then consider methods which call the calling method
				//isSecondLevel(callingMethod, callingClass, classCalledInformationMap, new_or_delete_MethodList, callingMethodsMap, counter); 
			}
			
			if (!callingMethods.isEmpty()) {
				
				Set<String> values = callingMethodsMap.get(callingClass);
				
				if (values == null) callingMethodsMap.put(callingClass, callingMethods);
				else values.addAll(callingMethods);
			}
		}
	}

///*******************************************************************************************************
// * Description: isSecondLevel
// * 				If the calling method is itself deleted or new, then consider methods which call the calling method.
//********************************************************************************************************/
//	private boolean isSecondLevel(String callingMethod, String callingClass, 
//								  Map<String, Map<String, CallingMethods>> classCalledInformationMap,
//  			  		  			  Map<String, List<Entity>> new_or_delete_MethodList, 
//  			  		  			  Map<String, Set<String>> callingMethodsMap, int counter) {
//		
//		//To prevent an infinite loop.
//		if (++counter > 2) return false;
//		
//		try {
//
//			for (Entity entity : new_or_delete_MethodList.get(callingClass)) {
//
//				if (entity.getName().equals(callingMethod)) {
//					
//					getMethodsWhichCalledInputMethod(callingMethod, callingClass, 
//													 classCalledInformationMap, new_or_delete_MethodList, callingMethodsMap, counter);
//					
//					return true;
//				}
//			}
//
//		} catch (NullPointerException e) {}
//			
//		return false;
//	}

/*******************************************************************************************************
 * Description: findSimilarVariableCalls
********************************************************************************************************/
	private double findSimilarVariableCalls(Map<String, Set<VariablePair>> calledVariables, 
											Map<String, Set<VariablePair>> variableDifferences) {

		double similarityValue = 0;

		Set<VariablePair> diffReadVariables = variableDifferences.get("Read");

		if (diffReadVariables != null && calledVariables.get("Read") != null) {

			for (VariablePair readVariable : calledVariables.get("Read")) {

				for (VariablePair diffVariable : diffReadVariables) {

					if (readVariable.variableName.equals(diffVariable.variableName))
						if (readVariable.variableType == diffVariable.variableType || (readVariable.variableType != null && readVariable.variableType.equals(diffVariable.variableType))) { 
							similarityValue++;
							diffReadVariables.remove(diffVariable);
							break;
						}
				}
			}
		}

		Set<VariablePair> diffWriteVariables = variableDifferences.get("Write");

		if (diffWriteVariables != null && calledVariables.get("Write") != null) {

			for (VariablePair writeVariable : calledVariables.get("Write")) {

				for (VariablePair diffVariable : diffWriteVariables) {

					if (writeVariable.variableName.equals(diffVariable.variableName))
						if (writeVariable.variableType.equals(diffVariable.variableType)) {
							similarityValue++;
							diffWriteVariables.remove(diffVariable);
							break;
						}
				}
			}
		}

		return similarityValue;
	}
	
/*******************************************************************************************************
 * Description: getCallingDifferences
 * 				This method returns different methods and fields that are called by the input method in 
 * 				the	initial and desired designs.
 ********************************************************************************************************/
	Map<String, Set<Pair>> getCallingDifferences(CalledInformation firstCalledEntities, CalledInformation secondCalledEntities) {
				
		Map<String, Set<Pair>> differences = new HashMap<String, Set<Pair>>();

		if (firstCalledEntities == null) return differences;

		/** Now compute their differences in terms of calling entities.*/

		//First different fields

		Set<Pair> diffFields = new HashSet<Pair>();
		List<ClonePair> secondCalledFields = clone(secondCalledEntities.calledFields);

		for (Pair calledField1 : firstCalledEntities.calledFields) {

			boolean found = false;

			for (ClonePair calledField2 : secondCalledFields) {

				if (similarEntities(calledField1.calledEntityName, calledField1.entityClassFullName, 
								  	calledField2.calledEntityName, calledField2.entityClassFullName)) {
						
					found = true;
					secondCalledFields.remove(calledField2);
					break;
				}
			}

			if (!found) diffFields.add(calledField1);
		}

		if (!diffFields.isEmpty())
			differences.put("Field", diffFields);

		//Second different methods

		Set<Pair> diffMethods = new HashSet<Pair>();
		List<ClonePair> secondCalledMethods = clone(secondCalledEntities.calledMethods);

		for (Pair calledMethod1 : firstCalledEntities.calledMethods) {

			boolean found = false;

			for (ClonePair calledMethdod2 : secondCalledMethods) {

				if (similarEntities(calledMethod1.calledEntityName, calledMethod1.entityClassFullName,
									calledMethdod2.calledEntityName, calledMethdod2.entityClassFullName)) {
					
					found = true;
					secondCalledMethods.remove(calledMethdod2);
					break;
				}
			}

			if (!found) diffMethods.add(calledMethod1);
		}

		if (!diffMethods.isEmpty())
			differences.put("Method", diffMethods);

		return differences;
	}
	
/*******************************************************************************************************
 * Description: similarEntities()
********************************************************************************************************/
	private boolean similarEntities(String entityName1, String entityClassFullName1, 
								    String entityName2, String entityClassFullName2) {
		
		String entityClassName1 = entityClassFullName1.substring(entityClassFullName1.lastIndexOf(".") + 1);
		String entityClassName2 = entityClassFullName2.substring(entityClassFullName2.lastIndexOf(".") + 1);
		
		if (this instanceof CandidateForExtractMethodRefactoring) {
			
			if (Service.similarCalled_or_CallingEntities(entityName1, entityClassFullName1, entityName2, entityClassFullName2)) return true;
			
			return Service.similarCalled_or_CallingEntities(entityName1, entityClassName1, entityName2, entityClassName2);
		}
		
		else if (this instanceof CandidateForInlineMethodRefactoring) {
			
			if (Service.similarCalled_or_CallingEntities(entityName2, entityClassFullName2, entityName1, entityClassFullName1)) return true;
			
			return Service.similarCalled_or_CallingEntities(entityName2, entityClassName2, entityName1, entityClassName1);
		}
		
		else {
			System.out.println("Bug: This method is called on class" + this.getClass());
			System.exit(0);
		}
		
		return false;
	}

/*******************************************************************************************************
 * Description: findSimilarEntityCalls
********************************************************************************************************/
	private double findSimilarEntityCalls(CalledInformation calledEntities, 
								  		  Map<String, Set<Pair>> differences) {

		double similarityValue = 0;
				
		Set<Pair> diffFields = differences.get("Field");
		if (diffFields != null) {
			for (Pair calledField : calledEntities.calledFields) {

				for (Pair diffField : diffFields) {

					if (similarEntity(calledField, diffField)) {

						similarityValue++;
						diffFields.remove(diffField);
						break;
					}
				}
			}
		}



		Set<Pair> diffMethods = differences.get("Method");
		if (diffMethods != null) {
			for (Pair calledMethod : calledEntities.calledMethods) {

				for (Pair diffMethod : diffMethods) {

					if (similarEntity(calledMethod, diffMethod)) {

						similarityValue++;
						diffMethods.remove(diffMethod);
						break;
					}
				}
			}
		}

		return similarityValue;
	}
	
/*******************************************************************************************************
 * Description: similarEntity
********************************************************************************************************/
	private boolean similarEntity(Pair calledEntity, Pair differentEntity) {
		
		if (this instanceof CandidateForExtractMethodRefactoring) {
			
			return Service.similarCalled_or_CallingEntities(differentEntity.calledEntityName, differentEntity.entityClassFullName, 
								 		   					calledEntity.calledEntityName, calledEntity.entityClassFullName);
		}
		
		else if (this instanceof CandidateForInlineMethodRefactoring) {
			
			return Service.similarCalled_or_CallingEntities(calledEntity.calledEntityName, calledEntity.entityClassFullName, 
															differentEntity.calledEntityName, differentEntity.entityClassFullName);
		}
		
		else {
			System.out.println("Bug: This method is called on class" + this.getClass());
			System.exit(0);
		}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: getAccessedVariables
********************************************************************************************************/
	Map<String, Set<VariablePair>> getAccessedVariables(String CallingMethodName, 
														Map<String, Map<String, Set<VariablePair>>> classCalledVariableMap) {
			
		HashSet<VariablePair> variablesRead = new HashSet<VariablePair>();
		HashSet<VariablePair> variablesWrite = new HashSet<VariablePair>();

		if (classCalledVariableMap == null) return null;

		Map<String, Set<VariablePair>> accessedVariables = classCalledVariableMap.get(CallingMethodName);

		try {
			Set<VariablePair> readSet = accessedVariables.get("Read");
			if (readSet != null) variablesRead.addAll(readSet);
		}catch(NullPointerException e){ /** Cover when accessedVariables is null.*/ }	

		try {
			Set<VariablePair> writeSet = accessedVariables.get("Write");
			if (writeSet != null) variablesWrite.addAll(writeSet);
		}catch(NullPointerException e){ /** Cover when accessedVariables is null.*/ }

		if (variablesRead.isEmpty() && variablesWrite.isEmpty()) return null;

		Map<String, Set<VariablePair>> calledVariblesInformation = new HashMap<String, Set<VariablePair>>();
		calledVariblesInformation.put("Read", variablesRead);
		calledVariblesInformation.put("Write", variablesWrite);

		return calledVariblesInformation;
	}
	
/*******************************************************************************************************
 * Description: getCallingDifferences1()
********************************************************************************************************/
	abstract Map<String, Set<Pair>> getCallingDifferences1(String callingMethodName, String callingClassName, 
														   String callingMethod_OtherName, String callingClass_OtherName);
	
/*******************************************************************************************************
 * Description: getCallingVariableDifferences1()
********************************************************************************************************/
	abstract Map<String, Set<VariablePair>> getCallingVariableDifferences1(String callingMethodName, String callingClassName, String callingMethod_OtherName, 
																		   Map<String, Map<String, Set<VariablePair>>> initialClassCalledVariableMap,
																		   Map<String, Map<String, Set<VariablePair>>> desiredClassCalledVariableMap);
	
/*******************************************************************************************************
 * Description: getCallingVariableDifferences
********************************************************************************************************/
	Map<String, Set<VariablePair>> getCallingVariableDifferences(Map<String, Set<VariablePair>> firstCalledVariablesMap, 
																 Map<String, Set<VariablePair>> secondCalledVariablesMap) {
				
		Map<String, Set<VariablePair>> differences = new HashMap<String, Set<VariablePair>>();

		if (firstCalledVariablesMap == null) return differences;
		
		//First variables which are read
		
		Set<VariablePair> diffVariablesRead = new HashSet<VariablePair>();
		Set<CloneVariablePair> secondVariablesRead = clone(secondCalledVariablesMap.get("Read"));
		
		Set<VariablePair> firstCalledVariablesSet = firstCalledVariablesMap.get("Read");
		if (firstCalledVariablesSet != null) {
			
			for (VariablePair variablesRead1 : firstCalledVariablesSet) {

				boolean found = false;

				for (CloneVariablePair variableRead2 : secondVariablesRead) {

					if (variablesRead1.variableName.equals(variableRead2.variableName)) {
						
						String variable1Type = variablesRead1.variableType;
						String variable2Type = variableRead2.variableType;
						
						if (variable1Type == null && variable2Type != null) continue;
						
						if (variable1Type == variable2Type || variable1Type.equals(variable2Type)) {
							found = true;
							secondVariablesRead.remove(variableRead2);
							break;
						}
					}
				}

				if (!found) diffVariablesRead.add(variablesRead1);
			}
		}

		if (!diffVariablesRead.isEmpty())
			differences.put("Read", diffVariablesRead);

		
		//Second variables which are write
		Set<VariablePair> diffVariablesWrite= new HashSet<VariablePair>();
		Set<CloneVariablePair> secondVariablesWrite = clone(secondCalledVariablesMap.get("Write"));
		
		firstCalledVariablesSet = firstCalledVariablesMap.get("Write");
		if (firstCalledVariablesSet != null) {
			
			for (VariablePair variablesWrite1 : firstCalledVariablesSet) {
	
				boolean found = false;
	
				for (CloneVariablePair variableWrite2 : secondVariablesWrite) {
	
					if (variablesWrite1.variableName.equals(variableWrite2.variableName)) {
						
						String variable1Type = variablesWrite1.variableType;
						String variable2Type = variableWrite2.variableType;
						
						if (variable1Type == null && variable2Type != null) continue;
						
						if (variable1Type == variable2Type || variable1Type.equals(variable2Type)) {
							found = true;
							secondVariablesWrite.remove(variableWrite2);
							break;
						}
					}
				}
	
				if (!found) diffVariablesWrite.add(variablesWrite1);
			}
		}

		if (!diffVariablesWrite.isEmpty())
			differences.put("Write", diffVariablesWrite);
		
		return differences;
	}
	
/*******************************************************************************************************
 * Description: getAllCalls
********************************************************************************************************/
	private double getAllCalls(CalledInformation entitiesCalledByInputMethod, 
					   		   Map<String, Set<VariablePair>> variablesCalledByInputMethod) {
			
		double size = 0;
			
		try {
			size = entitiesCalledByMethod(entitiesCalledByInputMethod);
		}catch(NullPointerException e){ /** Cover when accessedVariables is null.*/ }
			
		try {
			size +=  variablesCalledByInputMethod.get("Read").size();
		}catch(NullPointerException e){ /** Cover when accessedVariables is null.*/ }
			
		try {
			size += variablesCalledByInputMethod.get("Write").size();
		}catch(NullPointerException e){ /** Cover when accessedVariables is null.*/ }
			
		return size;
	}	
	
	abstract int entitiesCalledByMethod(CalledInformation entitiesCalledByDeletedMethod);
	
/*******************************************************************************************************
 * Description: ClonePair
********************************************************************************************************/
	private class ClonePair {
							
		String calledEntityName;
		String entityClassFullName;
							
		ClonePair(String calledEntityName, String entityClassFullName){ 
			this.calledEntityName = calledEntityName;
			this.entityClassFullName = entityClassFullName;
		}
	}
		
/*******************************************************************************************************
 * Description: CloneVariablePair
********************************************************************************************************/
	private class CloneVariablePair {
								
		String variableName;
		String variableType;
								
		CloneVariablePair(String variableName, String variableType){ 
			this.variableName = variableName;
			this.variableType = variableType;
		}
	}
		
/*******************************************************************************************************
 * Description: clone()
********************************************************************************************************/
	private List<ClonePair> clone(List<Pair> calledEntities){
				
		List<ClonePair> cloneList =  new ArrayList<ClonePair>();
				
		for (Pair calledEntity : calledEntities) {
			cloneList.add(new ClonePair(calledEntity.calledEntityName, calledEntity.entityClassFullName));
		}
				
		return cloneList;
	}
		
/*******************************************************************************************************
 * Description: clone()
********************************************************************************************************/
	private Set<CloneVariablePair> clone(Set<VariablePair> accessedVariables){
					
		Set<CloneVariablePair> cloneList =  new HashSet<CloneVariablePair>();
			
		try {
			for (VariablePair accessedVariable : accessedVariables) {
				cloneList.add(new CloneVariablePair(accessedVariable.variableName, accessedVariable.variableType));
			}
		}catch(NullPointerException e){ /** Cover when accessedVariables is null.*/ }
					
		return cloneList;
	}
	
/*******************************************************************************************************
* Description: removeFalseCallingClasses()
* 			   Methods in test classes can only call method in test class, and the same for business class.
********************************************************************************************************/
	void removeFalseCallingClasses(String methodClassFullName, 
								   Map<String, Set<String>> MethodsWhichCalledMethod,
								   SourceInformation sourceInformation){
		
		boolean isTestClass1 = sourceInformation.isTestClass(methodClassFullName);
		
		Set<String> removeList = new HashSet<String>();
		
		for (String callingClass : MethodsWhichCalledMethod.keySet()) {
			
			boolean isTestClass2 = sourceInformation.isTestClass(callingClass);
			
			//Test classes are compared with each other, and business classes are compared with each other.
			if (Boolean.compare(isTestClass1, isTestClass2) != 0) 
				removeList.add(callingClass);
			
		}
		
		for (String removeClass : removeList) {
			MethodsWhichCalledMethod.remove(removeClass);
		}
	}
}
