package simulation.candidateRefactorings.ExtractInlineMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import codeInformation.ElementInformation;
import codeInformation.Method;
import configuration.Thresholds;
import moea.problem.AdvancedProblem;
import simulation.candidateRefactorings.CandidateEntity;
import simulation.candidateRefactorings.CandidateRenameEntity;

public class Utility {
	
/*******************************************************************************************************
 * Description: getMethodInformation
 ********************************************************************************************************/
	static Set<CallingMethodInformation> getMethodInformation(String callingMethodName, String callingClassFullName, 
							                      	          CandidateForExtract_InlineMethodRefactoring callingObject) {
		
		Set<CallingMethodInformation> callingMethodInformation = new HashSet<CallingMethodInformation>();

		//In a case that the class is renamed, we extract its possible initial name.
		callingClassFullName = getClassName(callingClassFullName, callingObject);
		
		if (callingObject instanceof CandidateForExtractMethodRefactoring) {
			
			callingMethodInformation = getInitialMethodInformation(callingMethodName, callingClassFullName, callingObject.initialClassElementMap);
			
			if (callingMethodInformation.isEmpty()) callingMethodInformation.add(new CallingMethodInformation(callingClassFullName, callingMethodName));
		}
		
		else if (callingObject instanceof CandidateForInlineMethodRefactoring) {
			
			callingMethodInformation = getNewMethodInformation(callingMethodName, callingClassFullName, callingObject.desiredClassElementMap);
			
			if (callingMethodInformation.isEmpty()) callingMethodInformation.add(new CallingMethodInformation(callingClassFullName, callingMethodName));
		}
		
		return callingMethodInformation;
	}
	
/*******************************************************************************************************
 * Description: getClassName()
 ********************************************************************************************************/
	private static String getClassName(String callingClass, CandidateForExtract_InlineMethodRefactoring callingObject) {
		
		if (callingObject instanceof CandidateForExtractMethodRefactoring) {
			
			String originalClassName = getItsOriginalClass(callingClass);
			if (originalClassName == null) originalClassName = callingClass;
			return originalClassName;
		}
		
		if (callingObject instanceof CandidateForInlineMethodRefactoring) {
			
			String newClassName = getItsNewClass(callingClass);
			if (newClassName == null) newClassName = callingClass;
			return newClassName;
		}
		
		return null;
	}
	
/*******************************************************************************************************
 * Description: getItsOriginalClass()
********************************************************************************************************/
	static String getItsOriginalClass(String desiredClassFullName){
			
		for (Entry<String, String> entry : AdvancedProblem.getRenameClassCandidates().entrySet()) {
			
			if (entry.getValue().equals(desiredClassFullName)) return entry.getKey();
		}
			
		return null;
	}
	
/*******************************************************************************************************
 * Description: getItsNewClass()
********************************************************************************************************/
	private static String getItsNewClass(String initialClassFullName){
			
		return AdvancedProblem.getRenameClassCandidates().get(initialClassFullName);
	}
	
/*******************************************************************************************************
 * Description: isMethodInClass
 ********************************************************************************************************/
	private static boolean isMethodInClass(List<Method> methods, String methodName) {
		
		for (Method method : methods) {
			
			if (method.getName().equals(methodName)) return true;
		}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: getInitialMethodInformation()
 * 				If the method is in its class, then we return that. Otherwise, we need to check the suggested 
 * 				refactorings to find its possible original class in the original design.
 ********************************************************************************************************/
	private static Set<CallingMethodInformation> getInitialMethodInformation(String callingMethodName, String callingClassFullName, 
											                                 Map<String, ElementInformation> initialClassElementMap){
		
		Set<CallingMethodInformation> callingMethodInformation = new HashSet<CallingMethodInformation>();
		
		List<Method> methods = new ArrayList<Method>();
		try {methods = initialClassElementMap.get(callingClassFullName).methods;} catch (NullPointerException e) {}
		
		if (isMethodInClass(methods, callingMethodName)) {
			callingMethodInformation.add(new CallingMethodInformation(callingClassFullName, callingMethodName)); 
			return callingMethodInformation;
		}
		
		//Otherwise, we need to check all possible refactorings that might change the method.
		
		//If the method is renamed.
		for (CandidateRenameEntity renamedMethod : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForRenameMethodRef) {
			
			if (renamedMethod.entityNewName.equals(callingMethodName)) {
			
				if (renamedMethod.originalClassFullName.equals(callingClassFullName) || renamedMethod.targetClassFullName.equals(callingClassFullName)) 
					callingMethodInformation.add(new CallingMethodInformation(renamedMethod.originalClassFullName, renamedMethod.entityName));
			}
		}
		
		//or if the parameter of method is changed without renaming
		for (CandidateRenameEntity renamedMethod : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForChangeMethodParametersRef) {
			
			if (renamedMethod.entityNewName.equals(callingMethodName)) {
			
				if (renamedMethod.originalClassFullName.equals(callingClassFullName) || renamedMethod.targetClassFullName.equals(callingClassFullName)) 
					callingMethodInformation.add(new CallingMethodInformation(renamedMethod.originalClassFullName, renamedMethod.entityName));
			}
		}
		
		//If the method is moved without rename or changing its parameter.
		
		for (CandidateEntity MovedMethod : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForMoveMethodRef) {
			
			if (MovedMethod.targetClassFullName.equals(callingClassFullName) && MovedMethod.entityName.equals(callingMethodName))
				callingMethodInformation.add(new CallingMethodInformation(MovedMethod.originalClassFullName, MovedMethod.entityName));
		}
		
		for (CandidateEntity PushedDownMethod : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForPushDownMethodRef) {
			
			if (PushedDownMethod.targetClassFullName.equals(callingClassFullName) && PushedDownMethod.entityName.equals(callingMethodName))
				callingMethodInformation.add(new CallingMethodInformation(PushedDownMethod.originalClassFullName, PushedDownMethod.entityName));
		}
		
		for (CandidateEntity PulledUpMethod : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForPullUpMethodRef) {
			
			if (PulledUpMethod.targetClassFullName.equals(callingClassFullName) && PulledUpMethod.entityName.equals(callingMethodName))
				callingMethodInformation.add(new CallingMethodInformation(PulledUpMethod.originalClassFullName, PulledUpMethod.entityName));
		}
		
		return callingMethodInformation;
	}
	
/*******************************************************************************************************
 * Description: getNewMethodInformation()
 * 				If the method is in its class, then we return that. Otherwise, we need to check the suggested 
 * 				refactorings to find its possible original class in the target design.
 ********************************************************************************************************/
	private static Set<CallingMethodInformation> getNewMethodInformation(String callingMethodName, String callingClassFullName, 
										                                 Map<String, ElementInformation> desiredClassElementMap){
		
		Set<CallingMethodInformation> callingMethodInformation = new HashSet<CallingMethodInformation>();
		
		List<Method> methods = new ArrayList<Method>();
		try {methods = desiredClassElementMap.get(callingClassFullName).methods;} catch(NullPointerException e) {}
		
		if (isMethodInClass(methods, callingMethodName)) {
			callingMethodInformation.add(new CallingMethodInformation(callingClassFullName, callingMethodName)); 
			return callingMethodInformation;
		}
		
		//Otherwise, we need to check all possible refactorings that might change the method.
		
		//If the method is renamed.
		for (CandidateRenameEntity renameEntity : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForRenameMethodRef) {
			
			if (renameEntity.entityName.equals(callingMethodName)) {
			
				if (renameEntity.originalClassFullName.equals(callingClassFullName) || renameEntity.targetClassFullName.equals(callingClassFullName)) 
					callingMethodInformation.add(new CallingMethodInformation(renameEntity.targetClassFullName, renameEntity.entityNewName));
			}
		}
		
		//or if the parameter of method is changed without renaming
		for (CandidateRenameEntity renameEntity : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForChangeMethodParametersRef) {
			
			if (renameEntity.entityName.equals(callingMethodName)) {
					
				if (renameEntity.originalClassFullName.equals(callingClassFullName) || renameEntity.targetClassFullName.equals(callingClassFullName)) 
					callingMethodInformation.add(new CallingMethodInformation(renameEntity.targetClassFullName, renameEntity.entityNewName));
			}
		}
		
		//If the method is moved without rename.
		
		for (CandidateEntity MovedMethod : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForMoveMethodRef) {
			
			if (MovedMethod.originalClassFullName.equals(callingClassFullName) && MovedMethod.entityName.equals(callingMethodName))
				callingMethodInformation.add(new CallingMethodInformation(MovedMethod.targetClassFullName, MovedMethod.entityName));
		}
		
		for (CandidateEntity PushedDownMethod : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForPushDownMethodRef) {
			
			if (PushedDownMethod.originalClassFullName.equals(callingClassFullName) && PushedDownMethod.entityName.equals(callingMethodName))
				callingMethodInformation.add(new CallingMethodInformation(PushedDownMethod.targetClassFullName, PushedDownMethod.entityName));
		}
		
		for (CandidateEntity PulledUpMethod : AdvancedProblem.getInitialCandidateMethodRefactorings().candidatesForPullUpMethodRef) {
			
			if (PulledUpMethod.originalClassFullName.equals(callingClassFullName) && PulledUpMethod.entityName.equals(callingMethodName))
				callingMethodInformation.add(new CallingMethodInformation(PulledUpMethod.targetClassFullName, PulledUpMethod.entityName));
		}
		
		return callingMethodInformation;
	}
	
/*******************************************************************************************************
 * Description: decideAboutThreshold
********************************************************************************************************/
	static double decideAboutThreshold(Collection<Map<String, Double>> similarityValues){

		double similarityThreshold = Thresholds.extractInlineMethodThreshold;
		
		for (Map<String, Double> map : similarityValues) {
			
			for (Double value : map.values()) {
				if (value > similarityThreshold) {
					similarityThreshold = value;
				}
			}
		}
		
		
		if (similarityThreshold >= 0.7) similarityThreshold = similarityThreshold * 0.5;
		else if (similarityThreshold >= 0.5) similarityThreshold = similarityThreshold * 0.6;
		else if (similarityThreshold >= 0.3) similarityThreshold = similarityThreshold * 0.7;
		
		if (similarityThreshold < Thresholds.extractInlineMethodThreshold) 
			similarityThreshold = Thresholds.extractInlineMethodThreshold;
		
		return similarityThreshold;
	}
}

/*******************************************************************************************************
 * Description: CallingMethodInformation
********************************************************************************************************/
	class CallingMethodInformation {
		
		String classFullName;
		String methodName;
		
/*******************************************************************************************************
 * Description: CallingMethodInformation()
********************************************************************************************************/
		public CallingMethodInformation(String classFullName, String methodName) {
			
			this.classFullName = classFullName;
			this.methodName = methodName;
		}
	}