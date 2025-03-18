package simulation.candidateRefactorings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import codeInformation.DesiredSourceInformation;
import codeInformation.ElementInformation;
import codeInformation.Entity;
import codeInformation.SourceInformation;
import moea.problem.AdvancedProblem;
import moea.problem.RefactoringProblem;
import simulation.CandidateRenameClassRefactoring.CandidateClassforRenameRefactoring;
import simulation.candidateRefactorings.DiffAlgorithm.Service;
import simulation.fogsaa.BestAlignment;
import simulation.fogsaa.DisSimilarity;
import simulation.fogsaa.Similarity;
import simulation.simulateRefactoring.Refactoring;
import simulation.simulateRefactoring.Utility;

/*******************************************************************************************************
 * Description: CandidateRefactorings
********************************************************************************************************/
public abstract class CandidateRefactorings {
	
	protected Map<String, List<Entity>> entitiesMoveFromMap;
	protected Map<String, List<Entity>> entitiesMoveInMap;
	
	//This field keeps matched fields.
	public static List<MatchEntities> matchedFields;
	
	//This field keeps matched methods.
	public static List<MatchEntities> matchedMethods;
	
	protected float nameSimilarityImportance, entitySimilarityThreshold;
	
	/** This variable shows we are in the second or first round of the algorithm.*/
	public static boolean secondRound = false;

/*******************************************************************************************************
 * Description: entityIsValid()
********************************************************************************************************/
	public abstract boolean entityIsValid(Character entity);
	
/*******************************************************************************************************
 * Description: createEntity()
 * 				"designType" determines we are consider the original or desired design.
********************************************************************************************************/
	public abstract Entity createEntity(String entityName, Character entityAsCharacter, 
										String classFullName, String designType);
	
/*******************************************************************************************************
 * Description: addToMoveInMap()
********************************************************************************************************/
	public abstract void addToMoveInMap(String classFullName, List<Entity> entityList,
								 		Map<String, List<Entity>> entitiesMoveInMap);
	
/*******************************************************************************************************
 * Description: addToMoveFromMap()
********************************************************************************************************/
	public abstract void addToMoveFromMap(String classFullName, List<Entity> entityList, 
			  			  		   	      Map<String, List<Entity>> entitiesMoveFromMap);
	
/*******************************************************************************************************
 * Description: decideAboutEntities()
********************************************************************************************************/
	public abstract void decideAboutEntities(Map<String, String> renameClassCandidates, 
									  		 float nameSimilarityImportance, float entitySimilarityThreshold);
	
/*******************************************************************************************************
 * Description: extractMoveInMoveFormEntities()
********************************************************************************************************/
	public void extractMoveInMoveFormEntities(BestAlignment initialAlignment) {
		
		entitiesMoveFromMap = new HashMap<String, List<Entity>>();
		entitiesMoveInMap = new HashMap<String, List<Entity>>();
		
		//First extract candidate entities based on similarity list (str1 & str2)
		getCandidateEntitiesBasedOnSimilarityList(initialAlignment);
		
		//Second extract candidate entities based on dissimilarity list (str3 & str4)
		getCandidateEntitiesBasedOnDissimilarityList(initialAlignment.disSimilarity);
	}
	
/*******************************************************************************************************
 * Description: getCandidateEntitiesBasedOnDissimilarityList()
********************************************************************************************************/
	private void getCandidateEntitiesBasedOnDissimilarityList(DisSimilarity disSimilarity){
	
		int size = disSimilarity.resultedClassOrder.size();
		
		for (int i = 0; i < size; i++) {
			
			List<Entity> temp = new ArrayList<Entity>();
			
			String classInformation = disSimilarity.resultedDissimilarList.get(i);
			ArrayList<String> entityNames = disSimilarity.resultedListOfFullNames.get(i);
			String classFullName = disSimilarity.resultedClassOrder.get(i);
			
			for (int j = 0; j < classInformation.length(); j++) {
				
				Character entityAsCharacter = classInformation.charAt(j);
				
				if (entityIsValid(entityAsCharacter)) {
					
					Entity entity = createEntity(entityNames.get(j), entityAsCharacter, 
												 classFullName, "originalDesign");
					temp.add(entity);
				}
			}
		
			if (!temp.isEmpty()) {
				addToMoveFromMap(classFullName, temp, entitiesMoveFromMap);
			}
		}
		
		
		size = DisSimilarity.desiredClassOrder.size();
		
		for (int i = 0; i < size; i++) {
			
			List<Entity> temp = new ArrayList<Entity>();
			
			String classInformation = DisSimilarity.desiredDissimilarList.get(i);
			ArrayList<String> entityNames = DisSimilarity.desiredListOfFullNames.get(i);
			String classFullName = DisSimilarity.desiredClassOrder.get(i);

			
			for (int j = 0; j < classInformation.length(); j++) {
				
				Character entityAsCharacter = classInformation.charAt(j);
				
				if (entityIsValid(entityAsCharacter)) {
					
					Entity entity = createEntity(entityNames.get(j), entityAsCharacter, 
												 classFullName, "desiredDesign");
					temp.add(entity);
				}
			}
		
			if (!temp.isEmpty()) {
				addToMoveInMap(classFullName, temp, entitiesMoveInMap);
			}
		}
	}
	
/*******************************************************************************************************
 * Description: getCandidateEntitiesBasedOnSimilarityList()
********************************************************************************************************/
	private void getCandidateEntitiesBasedOnSimilarityList(BestAlignment initialAlignment){
		
		//First extract candidate classes contain entities that should be moved.
		for (Similarity sim : initialAlignment.similarityList) {
					
			List<Character> resultedAlignment = sim.resultedAlignment;
			List<Integer> types = sim.types;
					
			List<Entity> temp = new ArrayList<Entity>();
					
			for (int i = 0; i < resultedAlignment.size(); i++) {
						
				Character entityAsCharacter = resultedAlignment.get(i);

				if (!entityAsCharacter.equals('-')) {
					if (entityIsValid(entityAsCharacter)) {
						if (types.get(i) == 4 || types.get(i) == 2) {

							Entity entity = createEntity(sim.resultedAlignmentFullName.get(i), entityAsCharacter, 
														 sim.classFullName, "originalDesign");
							temp.add(entity);
						}
					}
				}
			}

			if (!temp.isEmpty()) {
				addToMoveFromMap(sim.classFullName, temp, entitiesMoveFromMap);
			}
		}

		//Next extract classes that some entities from other classes should be moved to them.
		for (Similarity sim : initialAlignment.similarityList) {

			List<Character> desiredAlignment = sim.desiredAlignment;
			List<Integer> types = sim.types;

			List<Entity> temp = new ArrayList<Entity>();

			for (int i = 0; i < desiredAlignment.size(); i++) {

				Character entityAsCharacter = desiredAlignment.get(i);

				if (!entityAsCharacter.equals('-')) {
					if (entityIsValid(entityAsCharacter)) { 
						if(types.get(i) == 3 || types.get(i) == 2) {

							Entity entity = createEntity(sim.desiredAlignmentFullName.get(i), entityAsCharacter, 
														 sim.classFullName, "desiredDesign");
							temp.add(entity);
						}
					}
				}
			}

			if (!temp.isEmpty()) {
				addToMoveInMap(sim.classFullName, temp, entitiesMoveInMap);
			}
		}
	}
	
/*******************************************************************************************************
 * Description: isParent()
********************************************************************************************************/
	protected boolean isParent(String child, String parent, 
							   Map<String, String> parentsMap, Map<String, Set<String>> interfaceClassMap) {
		
		if (Utility.getParent(child, parent, parentsMap)) return true;
			
		try {
			
			for (String classChild : interfaceClassMap.get(parent)) 
				if (classChild.equals(child)) 
					return true;
		
		}catch (NullPointerException e) {} 
			
		
		return false;
	}

/*******************************************************************************************************
 * Description: calculateSimilarity()
********************************************************************************************************/
	protected abstract List<MatchEntities> calculateSimilarity(String moveFromClassFullName, String moveInClassFullName,
									  				 		   List<Entity> moveFromEntities, List<Entity> moveInEntities);
	
/*******************************************************************************************************
 * Description: findMatchEntities()
********************************************************************************************************/
	protected List<MatchEntities> findMatchEntities(){
			
		SourceInformation initialSourceInformation = RefactoringProblem.getInitialSourceInformation();
		SourceInformation desiredSourceInformation = RefactoringProblem.getDesiredSourceInformation();
		
		List<MatchEntities> matchEntityList = new ArrayList<MatchEntities>();
			
		for (String moveFromClassFullName : entitiesMoveFromMap.keySet()) {
			
			boolean isTestClass1 = initialSourceInformation.isTestClass(moveFromClassFullName);
			
			List<Entity> moveFromEntities = entitiesMoveFromMap.get(moveFromClassFullName);
				
			for (String moveInClassFullName : entitiesMoveInMap.keySet()) {
				
				String similarClass = getSimilarClass(moveFromClassFullName);
				
				//If we find the similar class, then we continue with that and ignore the rest. It increase the speed of the algorithm
				if (!similarClass.isBlank()) moveInClassFullName = similarClass;
								
				boolean isTestClass2 = desiredSourceInformation.isTestClass(moveInClassFullName);
				
				//Test classes are compared with each other, and business classes are compared with each other.
				if (Boolean.compare(isTestClass1, isTestClass2) != 0) continue;
				
				List<Entity> moveInEntities = entitiesMoveInMap.get(moveInClassFullName);

				matchEntityList.addAll(calculateSimilarity(moveFromClassFullName, moveInClassFullName,
														   moveFromEntities, moveInEntities));
				
				//If we find the similar class, then no need to check the rest classes.
				if (!similarClass.isBlank()) break;
			}
		}
			
		return matchEntityList;
	}
	
/*******************************************************************************************************
 * Description: getSimilarClass()
 * 				If we find the similar class before, then we return that.
********************************************************************************************************/
	private String getSimilarClass(String originalClassFullName) {
		
		//The method does not try to find similar one if we are calling this method during finding rename class candidates 
		if (CandidateClassforRenameRefactoring.renameClassRefactoringIsChecking == true) return "";
		
		String desiredClassFullName = AdvancedProblem.getRenameClassCandidates().get(originalClassFullName);
		
		if (desiredClassFullName == null) return "";
		
		int index = originalClassFullName.lastIndexOf(".");
		String originalClassName = originalClassFullName;
		if (index != -1) originalClassName = originalClassFullName.substring(index);

		if (!desiredClassFullName.endsWith(originalClassName)) return "";

		SourceInformation initialSourceInformation = RefactoringProblem.getInitialSourceInformation();
		SourceInformation desiredSourceInformation = RefactoringProblem.getDesiredSourceInformation();
		
		ElementInformation originalClassElements = initialSourceInformation.getClassElementsMap().get(originalClassFullName);
		ElementInformation desiredClassElement = desiredSourceInformation.getClassElementsMap().get(desiredClassFullName);
		
		if (originalClassElements.fields.size() != desiredClassElement.fields.size()) return "";

		if (originalClassElements.methods.size() != desiredClassElement.methods.size()) return "";

		return desiredClassFullName;
	}
	
/*******************************************************************************************************
 * Description: extract_Deleted_And_New_Entities()
********************************************************************************************************/
	protected void extract_Deleted_And_New_Entities(List<MatchEntities> matchEntityList, 
													Map<String, List<Entity>> deletedEntities,							  			
													Map<String, List<Entity>> newEntities){
		
		extractDeletedEntities(matchEntityList, deletedEntities);
		extractNewEntities(matchEntityList, newEntities);
	}
	
/*******************************************************************************************************
 * Description: extractDeletedEntities()
********************************************************************************************************/
	private void extractDeletedEntities(List<MatchEntities> matchEntityList, 
										Map<String, List<Entity>> deletedEntities){
		
		deletedEntities.clear();

		for (MatchEntities matchEntities : matchEntityList) {
		
			String originalClassFullName = matchEntities.originalClassFullName;
			String originalEntityName = matchEntities.originalEntity.getName();
			String originalEntitySignature = matchEntities.originalEntity.getSignature();
			
			List<Entity> candidateEntities = entitiesMoveFromMap.get(originalClassFullName);
			
			for (Entity entity : candidateEntities) {
						
				if (originalEntityName.equals(entity.getName()) && originalEntitySignature.equals(entity.getSignature())) {
					
					candidateEntities.remove(entity);
					break;
				}
			}
		}	
		
		for (String classFullName : entitiesMoveFromMap.keySet()) {
			
			List<Entity> candidateEntities = entitiesMoveFromMap.get(classFullName);
			
			if (!candidateEntities.isEmpty())
				deletedEntities.put(classFullName, candidateEntities);
		}
	}
	
/*******************************************************************************************************
 * Description: extractNewEntities()
********************************************************************************************************/
	 private void extractNewEntities(List<MatchEntities> matchEntityList, 
			 						 Map<String, List<Entity>> newEntities){
		
		 newEntities.clear();

		 for (MatchEntities matchEntities : matchEntityList) {

			 String desiredClassFullName = matchEntities.desiredClassFullName;
			 String desiredEntityName = matchEntities.desiredEntity.getName();
			 String desiredEntitySignature = matchEntities.desiredEntity.getSignature();

			 List<Entity> candidateEntities = entitiesMoveInMap.get(desiredClassFullName);

			 for (Entity entity : candidateEntities) {

				 if (desiredEntityName.equals(entity.getName()) && 
					 desiredEntitySignature.equals(entity.getSignature())) {

					 	candidateEntities.remove(entity);
					 	break;
				 }
			 }
		 }	

		 for (String classFullName : entitiesMoveInMap.keySet()) {

			 List<Entity> candidateEntities = entitiesMoveInMap.get(classFullName);

			 if (!candidateEntities.isEmpty())
				 newEntities.put(classFullName, candidateEntities);
		 }
	 }
	 
/*******************************************************************************************************
 * Description: decideAboutEntities()
 * 				This method determines which entities should be Renamed, Pulled Up or Pushed Down or Move.
********************************************************************************************************/
	 protected List<MatchEntities> decideAboutEntities(Map<String, String> renameClassCandidates, 
			 								 		   List<CandidateEntity> candidateEntitiesForPullUp,
			 								 		   List<CandidateEntity> candidateEntitiesForPushDown,
			 								 		   List<CandidateEntity> candidateEntitiesForMove,
			 								 		   List<CandidateRenameEntity> candidateEntitiesForRename,
			 								 		   Map<String, List<Entity>> deletedEntities, 
			 								 		   Map<String, List<Entity>> newEntities){
	 		
		 /** As first step we find entities which are match (based on their names and relationships)*/
		 List<MatchEntities> matchEntityList = findMatchestEntities();

		 //We need to determine entities which are probably deleted from the original design or are new in the desired design.
		 extract_Deleted_And_New_Entities(matchEntityList, deletedEntities, newEntities);
		 
		 Map<String, String> classParentMapDesired = DesiredSourceInformation.classParentMap;
		 Map<String, Set<String>> interfaceClassMapDesired = DesiredSourceInformation.getInterfaceClassMap();
		 
		 SourceInformation initialSOurceInformation = Refactoring.getInitialSourceInformation();
		 Map<String, String> classParentMapInitial = initialSOurceInformation.getClassParentMap();
		 Map<String, Set<String>> interfaceClassMapInitial = initialSOurceInformation.getInterfaceClassMap();
		 
		 /** Now decide about refactoring types.*/
		 for (MatchEntities matchEntities : matchEntityList) {

			 Entity originalEntity = matchEntities.originalEntity;
			 String moveFromClassFullName = matchEntities.originalClassFullName;

			 Entity desiredEntity = matchEntities.desiredEntity;
			 String moveInClassFullName = matchEntities.desiredClassFullName;


			 //If only rename refactoring (class, method or field) happened, then no need to check the other refactorings for these two entities.
			 if (didOnlyRenameRefactoringHappen(originalEntity, desiredEntity, 
					 						  	moveInClassFullName, moveFromClassFullName,
					 						  	renameClassCandidates, candidateEntitiesForRename))
				 continue;


			 decideAboutMovePullUpPushDown(desiredEntity, moveInClassFullName, moveFromClassFullName, 
					 					   candidateEntitiesForPullUp, candidateEntitiesForPushDown, candidateEntitiesForMove,
					 					   classParentMapInitial, interfaceClassMapInitial,
					 					   classParentMapDesired, interfaceClassMapDesired);
		 }

		 return matchEntityList;
	 }
	 
/*******************************************************************************************************
 * Description: findMatchestEntities()
********************************************************************************************************/
	 protected List<MatchEntities> findMatchestEntities() {
	 		
		 /** As first step we find entities which are match (based on their names and relationships)*/
		 List<MatchEntities> matchEntityList = findMatchEntities();
	 		
		 /** As second step we sort entities pair based on their similarity value.*/
		 Service.sort(matchEntityList);
	 		
		 keepMatchestEntities(matchEntityList);
	 		
		 return matchEntityList;
	 }
	 
	 protected abstract void keepMatchestEntities(List<MatchEntities> matchEntityList);
	 
	 protected abstract boolean didOnlyRenameRefactoringHappen(Entity originalEntity, Entity desiredEntity, 
			 												   String moveInClassFullName, String moveFromClassFullName,
			 												   Map<String, String> renameClassCandidates, 
			 												   List<CandidateRenameEntity> candidateEntitiesForRename);
	 
		
/*******************************************************************************************************
 * Description: decideAboutMovePullUpPushDown()
********************************************************************************************************/
	 void decideAboutMovePullUpPushDown(Entity desiredEntity, 
			 							String moveInClassFullName, String moveFromClassFullName,
			 							List<CandidateEntity> candidateEntitiesForPullUp,
			 							List<CandidateEntity> candidateEntitiesForPushDown,
			 							List<CandidateEntity> candidateEntitiesForMove,
			 							Map<String, String> classParentMapOriginal,
			 							Map<String, Set<String>> interfaceClassMapOriginal,
			 							Map<String, String> classParentMapDesired,
			 							Map<String, Set<String>> interfaceClassMapDesired){
	 		
		 if (moveInClassFullName.equals(moveFromClassFullName)) return;
		 
		 /** Note that we pass "desiredEntity". Therefore, if the entity is renamed, 
	 		 first it is renamed	and then the other refactoring will be applied.*/
		 CandidateEntity candidateEntity = new CandidateEntity(moveFromClassFullName, 
				 											   moveInClassFullName, 
				 											   desiredEntity.getName(), 
				 											   desiredEntity.getSignature(),
				 											   desiredEntity.getEntityTypeFullName());

		 //Candidate for PullUp
		 if (isParent(moveFromClassFullName, moveInClassFullName, classParentMapDesired, interfaceClassMapDesired)) {
			 addToList(candidateEntitiesForPullUp, candidateEntity);
		 }
		 else if (isParent(moveFromClassFullName, moveInClassFullName, classParentMapOriginal, interfaceClassMapOriginal)) {
			 addToList(candidateEntitiesForPullUp, candidateEntity);
		 } 

		 //Candidate for PushDown
		 else if(isParent(moveInClassFullName, moveFromClassFullName, classParentMapDesired, interfaceClassMapDesired)) {
			 addToList(candidateEntitiesForPushDown, candidateEntity);
		 }
		 
		 else if(isParent(moveInClassFullName, moveFromClassFullName, classParentMapOriginal, interfaceClassMapOriginal)) {
			 addToList(candidateEntitiesForPushDown, candidateEntity);
		 }

		 //Candidate for Move
		 else {
			 addToList(candidateEntitiesForMove, candidateEntity);
		 }
	 }
	 
/*******************************************************************************************************
* Description: addToList
* 				If the refactoring is not in the list, add it to the list.
********************************************************************************************************/
	 private void addToList(List<CandidateEntity> candidatesForRefactoring, CandidateEntity candidateEntity) {
	 		
		 for (CandidateEntity can : candidatesForRefactoring) {
	 			
			 if (can.entityName.equals(candidateEntity.entityName) &&
				 can.entityTypeFullName.equals(candidateEntity.entityTypeFullName) &&
				 can.originalClassFullName.equals(candidateEntity.originalClassFullName) &&
				 can.targetClassFullName.equals(candidateEntity.targetClassFullName)) {
				
				 return;
			 }
		 }
	 		
		 candidatesForRefactoring.add(candidateEntity);
	 }	
	 
/*******************************************************************************************************
 * Description: createRenameRefactoring()
********************************************************************************************************/
	 protected boolean createRenameRefactoring(Entity originalEntity, Entity desiredEntity, 
			 								   String moveInClassFullName, String moveFromClassFullName,
			 								   Map<String, String> renameClassCandidates,
			 								   List<CandidateRenameEntity> candidateEntitiesForRename){

		 /** First check Rename Entity refactoring.*/
		 if (!originalEntity.getName().equals(desiredEntity.getName())) {

			 /** It is a Rename Entity refactoring.*/
			 CandidateRenameEntity candidateRenameEntity = new CandidateRenameEntity(moveFromClassFullName, 
					 																 moveInClassFullName,											  		
					 																 originalEntity.getName(), 
					 																 originalEntity.getSignature(),
					 																 originalEntity.getEntityTypeFullName(),
					 																 desiredEntity.getName());

			 //If two constructors are renamed to each other, we assume a Rename Class refactoring might also happened.
			 if (Service.bothEntitiesAreConstructor(originalEntity.getName(), desiredEntity.getName(), moveFromClassFullName, moveInClassFullName)) {

				 try{if (AdvancedProblem.getDeleteClassCandidates().contains(moveFromClassFullName) && AdvancedProblem.getNewClassCandidates().contains(moveInClassFullName)) {

					 /** Note if a rename class is detected for "moveFromClassFullName" using the class "CandidateClassforRenameRefactoring", then
					  *  we should trust that, and do not replace the previous value. Otherwise, a new rename class refactoring is added as a candidate.*/  
					 String newClassName = renameClassCandidates.get(moveFromClassFullName);
					 if (newClassName == null || newClassName.isEmpty()) {
					 
						 renameClassCandidates.put(moveFromClassFullName, moveInClassFullName);
					 }
	
				 }} catch(NullPointerException e) {}
			 }
			 
			 candidateEntitiesForRename.add(candidateRenameEntity);
			 
			 return true;
		 }

		 return false;
	 }
	 
/*******************************************************************************************************
 * Description: getName()
********************************************************************************************************/
	 protected String getName(String name) {
	 		
		 int index = name.lastIndexOf("(");
	 		
		 if (index == -1)
			 index = name.lastIndexOf(".");
		 else index = name.substring(0, index).lastIndexOf(".");
	 		
		 if (index == -1) return name;
	 		
		 //If it is a full name, return only its name
		 return name.substring(index + 1);
	 }

/*******************************************************************************************************
 * Description: getcandidateFieldRefactorings()
********************************************************************************************************/
	public CandidateFieldRefactorings getcandidateFieldRefactorings() {
			return null;
	}
	
/*******************************************************************************************************
 * Description: getcandidateMthodRefactorings()
********************************************************************************************************/
	public CandidateMethodRefactorings getcandidateMthodRefactorings() {
		return null;
	}
}