package simulation.CandidateRenameClassRefactoring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import codeInformation.ElementInformation;
import codeInformation.Entity;
import codeInformation.Field;
import codeInformation.Method;
import codeInformation.SourceInformation;
import configuration.Thresholds;
import moea.problem.AdvancedProblem;
import moea.problem.RefactoringProblem;
import simulation.calculateSimilarity.CalculateEntitySimilarity;
import simulation.calculateSimilarity.CalculateFieldSimilarity;
import simulation.calculateSimilarity.CalculateMethodSimilarity;
import simulation.candidateRefactorings.MatchEntities;
import simulation.candidateRefactorings.DiffAlgorithm.Service;
import simulation.fogsaa.DisSimilarity;

public class CandidateClassforRenameRefactoring {
	
	/** This variable is true if we are comparing classes for Rename Class refactoring.*/
	public static boolean renameClassRefactoringIsChecking = false;
	
/*******************************************************************************************************
 * Description: extractRenameClassRefactoring()
********************************************************************************************************/
	public Map<String, String> extractRenameClassRefactoring(DisSimilarity disSimilarity, 
															 float nameSimilarityImportance,
															 float entitySimilarityThreshold,
															 float classSimilarityThreshod) {

		RenameClassCandidates detailedRenameClassCandidates = new RenameClassCandidates();
		
		renameClassRefactoringIsChecking = true;
		
		SourceInformation initialSourceInformation = RefactoringProblem.getInitialSourceInformation();
		SourceInformation desiredSourceInformation = RefactoringProblem.getDesiredSourceInformation();
		
		Map<String, ElementInformation> initialClassElementMap = initialSourceInformation.getClassElementsMap();
		Map<String, ElementInformation> desiredClassElementMap = desiredSourceInformation.getClassElementsMap();
		
		//Key is the original class name and its value is the new class name in the target class - A Rename refactoring candidate
		Map<String, String> renameClassCandidates = new HashMap<String, String>();
		
		//This contain list of classes that we are pretty sure we found their original classes, and no need to compare them with the other classes.
		Set<String> matchedBefore = new HashSet<String>();
		for (String originalClassFullName : disSimilarity.resultedClassOrder) {
			
			ElementInformation originalClassElements = initialClassElementMap.get(originalClassFullName);
			
			float maxSimilarity = 0;
			String similarestClass = null;
			
			boolean isTestClass1 = initialSourceInformation.isTestClass(originalClassFullName);
			
			for (String desiredClassFullName : DisSimilarity.desiredClassOrder) {
				
				if (matchedBefore.contains(desiredClassFullName)) continue;
				
				boolean isTestClass2 = desiredSourceInformation.isTestClass(desiredClassFullName);
				
				//Test classes are compared with each other, and business classes are compared with each other.
				if (Boolean.compare(isTestClass1, isTestClass2) != 0) continue;
				
				ElementInformation desiredClassElement = desiredClassElementMap.get(desiredClassFullName);
				
				float currentSimilarity = 0;
				boolean areSmilarClasses = false;
				if (are_classes_similar(originalClassFullName, originalClassElements, desiredClassFullName, desiredClassElement)) {
					currentSimilarity = Thresholds.classConfidentThreshold + 0.1f;
					areSmilarClasses = true;
				}
				else {
				currentSimilarity = measureSimilarity(originalClassElements, originalClassFullName,
												      desiredClassElement, desiredClassFullName,
													  nameSimilarityImportance, entitySimilarityThreshold);
				}
				
				currentSimilarity = (float) round(currentSimilarity, 2);
				int result = Float.compare(currentSimilarity, maxSimilarity);
				
				if (result >= 0 && currentSimilarity != 0) {
					
					if (result == 0) {
						
						if (returnClassWithStrongerPath(originalClassFullName, desiredClassFullName, similarestClass).equals(similarestClass)) continue;
					}
					
					maxSimilarity = currentSimilarity;
					similarestClass = desiredClassFullName;
					
					if (maxSimilarity > Thresholds.classConfidentThreshold) {
						if (areSmilarClasses) break;
					}
				}
			}

			if (maxSimilarity >= classSimilarityThreshod && similarestClass != null) {
				
				renameClassCandidates.put(originalClassFullName, similarestClass);
				
				//We save the similarity between classes using this.
				detailedRenameClassCandidates.put(originalClassFullName, similarestClass, maxSimilarity);
				
				if (maxSimilarity > Thresholds.classConfidentThreshold) matchedBefore.add(similarestClass);
			}
		}

		renameClassRefactoringIsChecking = false;

		return renameClassCandidates;
	}
	
/*******************************************************************************************************
 * Description: are_classes_similar()
 * 				The similarity is based on two criteria: the class name and the number of methods and fields defined in each class.
********************************************************************************************************/
	private boolean are_classes_similar(String originalClassFullName, ElementInformation originalClassElements, 
										String desiredClassFullName, ElementInformation desiredClassElement) {
	
		int index = originalClassFullName.lastIndexOf(".");
		if (index != -1) originalClassFullName = originalClassFullName.substring(index);
		
		if (!desiredClassFullName.endsWith(originalClassFullName)) return false;
		
		if (originalClassElements.fields.size() != desiredClassElement.fields.size()) return false;
		
		if (originalClassElements.methods.size() != desiredClassElement.methods.size()) return false;
		
		return true;
	}
	
/*******************************************************************************************************
 * Description: round()
********************************************************************************************************/
	public double round(double value, int places) {
	    
		if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
/*******************************************************************************************************
 * Description: returnClassWithStrongerPath()
********************************************************************************************************/
	private String returnClassWithStrongerPath(String originalClassFullName, 
								               String classFullName1, 
								               String classFullName2) {
		
		String[] split_originalClass = originalClassFullName.split("[.]");
		String[] split_Class1 = classFullName1.split("[.]");
		String[] split_Class2 = classFullName2.split("[.]");
		
		int i = split_originalClass.length - 1;
		int j = split_Class1.length - 1;
		int k = split_Class2.length - 1;
		
		for (; i > -1 && j > -1 && k > -1; i--, j--, k--) {
			
			String originalClass = split_originalClass[i];
			String class1 = split_Class1[j];
			String class2 = split_Class2[k];
			
			if (originalClass.equals(class1) && originalClass.equals(class2)) continue;
			
			if (originalClass.equals(class1)) return classFullName1;
			
			if (originalClass.equals(class2)) return classFullName2;
		}
		
		return classFullName2;
	}
	
/*******************************************************************************************************
 * Description: measureSimilarity()
********************************************************************************************************/
	private float measureSimilarity(ElementInformation originalClassElement, String originalClassFullName,
								    ElementInformation desiredClassElement, String desiredClassFullName,
									float nameSimilarityImportance, float entitySimilarityThreshold) {
		
		List<Entity> originalFields = new ArrayList<Entity>();
		for (Field field : originalClassElement.fields) {
			originalFields.add(field);
		}
		
		List<Entity> desiredFields = new ArrayList<Entity>();
		for (Field field : desiredClassElement.fields) {
			desiredFields.add(field);
		}
		
		int allFields = originalFields.size() + desiredFields.size();
		
		//Now measure fields similarity.
		CalculateEntitySimilarity calculateFieldSimilarity = new CalculateFieldSimilarity(originalClassFullName, desiredClassFullName);
		List<MatchEntities> matchedFields = calculateFieldSimilarity.calculateSimilarity(originalFields, desiredFields,
																						 originalClassFullName, desiredClassFullName,																		 
																						 nameSimilarityImportance, entitySimilarityThreshold);
		
		//An entity can not be matched to more than one entity except they have similar normalized and relationship similarity values.
		removeDuplicateMatches(matchedFields); 
		
		//Now we need to reduce similarity value based on fields which are not match
		int differentFields = getMisMatchEntities(originalFields, originalClassFullName, 
												  desiredFields, desiredClassFullName, 
												  matchedFields);

		
		
		List<Entity> originalMethods = new ArrayList<Entity>();
		for (Method method : originalClassElement.methods) {
			originalMethods.add(method);
		}
		
		List<Entity> desiredMethods = new ArrayList<Entity>();
		for (Method method : desiredClassElement.methods) {
			desiredMethods.add(method);
		}
		
		int allMethods = originalMethods.size() + desiredMethods.size();
		
		//Now measure methods similarity.
		CalculateEntitySimilarity calculateMethodSimilarity = new CalculateMethodSimilarity(originalClassFullName, desiredClassFullName);
		List<MatchEntities> matchedMethods = calculateMethodSimilarity.calculateSimilarity(originalMethods, desiredMethods,
																						   originalClassFullName, desiredClassFullName,																			   
																						   nameSimilarityImportance, entitySimilarityThreshold);
		
		//An entity can not be matched to more than one entity except they have similar normalized and relationship similarity values.
		removeDuplicateMatches(matchedMethods);
		
		//Now we need to reduce similarity value based on methods which are not match
		int diffrentMethods = getMisMatchEntities(originalMethods, originalClassFullName, 
												  desiredMethods, desiredClassFullName,
												  matchedMethods);
 
		int matchedConstructors = getNumberofMatchedConstructors(matchedMethods);
		
		String originalClassName = originalClassFullName.substring(originalClassFullName.lastIndexOf(".") + 1);
		String desiredClassName = desiredClassFullName.substring(desiredClassFullName.lastIndexOf(".") + 1);
		
		
		//Now check inner classes if there is any.
		Set<String> n1 = RefactoringProblem.getInitialSourceInformation().getNestedClassMap().get(originalClassFullName);
		Set<String> n2 = RefactoringProblem.getDesiredSourceInformation().getNestedClassMap().get(desiredClassFullName);

		int matchedNestedClass = 0, differentNestedClasses = 0, allNestedClasses = 0;
		if (n1 != null && n2 != null) {
			
			Map<String, String> renameClassCandidates = AdvancedProblem.getRenameClassCandidates();
			
			for (String nestedClassName1 : n1) {
				
				String nestedClassFullname1 = originalClassFullName + "." + nestedClassName1;
				
				for (String nestedClassName2 : n2) {
					
					String nestedClassFullname2 = desiredClassFullName + "." + nestedClassName2;
					
					if (Service.classeIsRenamed(nestedClassFullname1, nestedClassFullname2, renameClassCandidates)) {
						matchedNestedClass++;
						break;
					}
				}
			}
		}
		
		try {allNestedClasses += n1.size();} catch(NullPointerException e) {}
		try {allNestedClasses += n2.size();} catch(NullPointerException e) {}
		differentNestedClasses = allNestedClasses - (matchedNestedClass * 2);
		
		//Now check their hierarchy structure.
		int hierarchySimilarity = getHirechySimilarity(originalClassFullName, desiredClassFullName);
		
		
		return getClassSimilarity(matchedFields.size(), differentFields, 
								  matchedMethods.size(), diffrentMethods,
								  matchedNestedClass, differentNestedClasses,
								  allFields, allMethods, allNestedClasses, matchedConstructors,
								  hierarchySimilarity, originalClassName, desiredClassName);
	}
	
/*******************************************************************************************************
 * Description: getHirechySimilarity()
********************************************************************************************************/
	private int getHirechySimilarity(String initialClassFullName, String desiredClassFullName) {
		
		int similarityValue = 0;
		
		Map<String, String> renameClassCandidates = AdvancedProblem.getRenameClassCandidates();
		
		SourceInformation initialSourceInformation = RefactoringProblem.getInitialSourceInformation();
		SourceInformation desiredSourceInformation = RefactoringProblem.getDesiredSourceInformation();
		
		
		try { 

			//First parent classes
			String intialParent = initialSourceInformation.getClassParentMap().get(initialClassFullName);
			String desiredParent = desiredSourceInformation.getClassParentMap().get(desiredClassFullName);
				
			if (intialParent.equals(desiredParent) || desiredParent.equals(renameClassCandidates.get(intialParent)))
				similarityValue++;
		} catch(NullPointerException e) {}
		
		try {
			//Then parent interfaces
			Set<String> intialParentSet = getParentInterface(initialSourceInformation.getInterfaceClassMap(), initialClassFullName);
			Set<String> desiredParentSet = getParentInterface(desiredSourceInformation.getInterfaceClassMap(), desiredClassFullName);
			
			for (String p1 : intialParentSet) {
				
				if (desiredParentSet.contains(p1) || desiredParentSet.contains(renameClassCandidates.get(p1))) 
					similarityValue++; 
			}
			
		} catch(NullPointerException e) {}
		
		
		//Second children classes

		Set<String> desiredChildren = null;
		Set<String> initialChildren = null;
		
		try {
			
			Map<String, Set<String>> desiredParentChildrenMap = desiredSourceInformation.getParent_Children_Map();
			
			if (desiredParentChildrenMap != null) {
				desiredChildren = desiredParentChildrenMap.get(desiredClassFullName);
				if (desiredChildren != null) {
					initialChildren = initialSourceInformation.getParent_Children_Map().get(initialClassFullName);
				}
			}
			
			if (desiredChildren == null) {
				desiredChildren = desiredSourceInformation.getInterfaceClassMap().get(desiredClassFullName);
				initialChildren = initialSourceInformation.getInterfaceClassMap().get(initialClassFullName);
			}
		
		} catch (NullPointerException e) { }
		

		if (initialChildren == null || desiredChildren == null) return similarityValue;
		
		
		for (String child : initialChildren) {
				
			if (desiredChildren.contains(child)) { 
				similarityValue++; 
			}
				
			else {
				if (renameClassCandidates != null && desiredChildren.contains(renameClassCandidates.get(child))) {
					similarityValue++; 
				}
			}
		}
			
		return similarityValue;
	}
	
/*******************************************************************************************************
 * Description: getParentInterface()
********************************************************************************************************/
	private Set<String> getParentInterface(Map<String, Set<String>> interfaceClassMap, String ClassFullName){
		
		Set<String> parentInterface = new HashSet<String>();
		
		for (Entry<String, Set<String>> map : interfaceClassMap.entrySet()) {
			
			String parent = map.getKey();
			
			if (map.getValue().contains(ClassFullName)) parentInterface.add(parent); 
		}
		
		return parentInterface;
	}
	
/*******************************************************************************************************
 * Description: getNumberofMatchedConstructors()
********************************************************************************************************/
	private int getNumberofMatchedConstructors(List<MatchEntities> matchedMethods) {
		
		int matchedConstructor = 0;
		
		for (MatchEntities matchEntity : matchedMethods) {
			
			if (Service.isConstructor(matchEntity.originalClassFullName, matchEntity.originalEntity.getName()) &&
				Service.isConstructor(matchEntity.desiredClassFullName, matchEntity.desiredEntity.getName())) {
				matchedConstructor++;
			}
		}
		
		return matchedConstructor;
	}
	
/*******************************************************************************************************
 * Description: removeDuplicateMatches()
 * 				The matchedEntities are sort based on their normalized and then relationship similarity values.
********************************************************************************************************/
	private void removeDuplicateMatches(List<MatchEntities> matchedEntities) {
		
		Service.sort(matchedEntities);
		
		Service.keepMatchestEntities(matchedEntities);
	}
	
/*******************************************************************************************************
 * Description: getClassSimilarity()
********************************************************************************************************/
	private float getClassSimilarity(int matchedFields, int differentFields, 
									 int matchedMethods, int diffrentMethods,
									 int matchedNestedClasses, int differentNestedClasses,
									 int allFields, int allMethods, int allNestedClasses, int matchedConstructor,
									 int hierarchySimilarity, String originalClassName, String desiredClassName) {
		
		int matchedEntities = matchedFields +  matchedMethods + (matchedNestedClasses * 2) + hierarchySimilarity;
		int differentEntities = differentFields + diffrentMethods + (differentNestedClasses * 2);
		
		//If two class are empty then assume they are the same, if they have similar name.
		if (matchedEntities == 0 && differentEntities == 0 && originalClassName.equals(desiredClassName)) return 1;
		
		else if (matchedEntities == 0) return 0;
		
		float similarityValue = matchedEntities * 2;
		
		//To detect Move Class refactorings if class names are equals then 10% similarity is increased.
		if (originalClassName.equals(desiredClassName)) {
			similarityValue += similarityValue * 0.1;
		}
		
		//For each pair of constructors which are matched, 10% similarity value is increased.
		if (matchedConstructor != 0)  
			similarityValue += similarityValue * (0.1 * matchedConstructor);
		
		return similarityValue / ((matchedEntities * 2) + differentEntities);
	}
	
/*******************************************************************************************************
 * Description: getEntitySimilarity()
********************************************************************************************************/
	private int getMisMatchEntities(List<Entity> originalEntities, String originalClassFullName, 
									List<Entity> desiredEntities, String desiredClassFullName, 
									List<MatchEntities> matchedEntities) {

		//First find different entities in the original class.
		int differentEntities = 0;
		label:for (Entity originalEntity : originalEntities) {
			
			String originalEntityName = originalEntity.getName();
			String originalEntitySignature = originalEntity.getSignature();
			
			for (MatchEntities matchEntity : matchedEntities) {
				
				if (originalEntityName.equals(matchEntity.originalEntity.getName()) && 
					originalEntitySignature.equals(matchEntity.originalEntity.getSignature()))
						continue label;
			}
			
			//At this line we know entity is different.
			differentEntities++;
		}
		
		
		//Second find different entities in the desired class.
		label:for (Entity desiredEntity : desiredEntities) {
			
			String desiredEntityName = desiredEntity.getName();
			String desiredEntitySignature = desiredEntity.getSignature();
			
			for (MatchEntities matchEntity : matchedEntities) {
				
				if (desiredEntityName.equals(matchEntity.desiredEntity.getName()) && 
					desiredEntitySignature.equals(matchEntity.desiredEntity.getSignature()))
						continue label;
			}
			
			//At this line we know entity is different.
			differentEntities++;
		}
		
		return differentEntities;
	}
}