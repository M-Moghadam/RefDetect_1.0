package simulation.candidateRefactorings.DiffAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import codeInformation.Entity;
import simulation.candidateRefactorings.CandidateMethodRefactorings;
import simulation.candidateRefactorings.CandidateRenameEntity;
import simulation.candidateRefactorings.Facility;
import simulation.candidateRefactorings.MatchEntities;
import simulation.calculateSimilarity.CalculateEntitySimilarity;
import simulation.calculateSimilarity.CalculateMethodSimilarity;
import simulation.candidateRefactorings.CandidateEntity;
import simulation.candidateRefactorings.CandidateExtractInlineMethod;

/*******************************************************************************************************
 * Description: CandidateMethodsForRefactoring_DiffAlgorithm
 * 				This class keeps information about candidate methods for PushDown, PullUp and Move.
********************************************************************************************************/
public class CandidateMethodsForRefactoring_DiffAlgorithm extends CandidateBasedonDiffAlgorithm implements Facility {

	private CandidateMethodRefactorings candidateMethodRefactorings = new CandidateMethodRefactorings();
	
/*******************************************************************************************************
 * Description: decideAboutEntities()
********************************************************************************************************/
	@Override
	public void decideAboutEntities(Map<String, String> renameClassCandidates,
		    						float nameSimilarityImportance, float entitySimilarityThreshold){
		
		this.nameSimilarityImportance = nameSimilarityImportance;
		this.entitySimilarityThreshold = entitySimilarityThreshold;
		
		candidateMethodRefactorings.candidatesForPullUpMethodRef = new ArrayList<CandidateEntity>();
		candidateMethodRefactorings.candidatesForPushDownMethodRef = new ArrayList<CandidateEntity>();
		candidateMethodRefactorings.candidatesForMoveMethodRef = new ArrayList<CandidateEntity>();
		candidateMethodRefactorings.candidatesForExtractMethodRef = new ArrayList<CandidateExtractInlineMethod>();
		candidateMethodRefactorings.candidatesForInlineMethodRef = new ArrayList<CandidateExtractInlineMethod>();
		candidateMethodRefactorings.candidatesForRenameMethodRef = new ArrayList<CandidateRenameEntity>();
		candidateMethodRefactorings.candidatesForChangeMethodParametersRef = new ArrayList<CandidateRenameEntity>();
		
		//Now decide about which methods should be Pulled Up or Pushed Down or Move.
		matchedMethods = decideAboutEntities(renameClassCandidates,  
											 candidateMethodRefactorings.candidatesForPullUpMethodRef, 
											 candidateMethodRefactorings.candidatesForPushDownMethodRef, 
											 candidateMethodRefactorings.candidatesForMoveMethodRef, 
											 candidateMethodRefactorings.candidatesForRenameMethodRef,
											 candidateMethodRefactorings.deletedMethods, 
											 candidateMethodRefactorings.newMethods);
		
		
		/** At the end of second round we try to find Extract, Inline Method candidates and also Change Method Signature candidates.*/
		if (secondRound) {
		
			List<CandidateRenameEntity> ChangeMethodParameter_FalsePositiveList = decideAboutChangeMethodSignature(candidateMethodRefactorings, renameClassCandidates);
			
			updateRefactorings(ChangeMethodParameter_FalsePositiveList, candidateMethodRefactorings);
			
			decideAboutExtractInlineMethodRefactorings(candidateMethodRefactorings);
		}
	}
	
/*******************************************************************************************************
 * Description: calculateSimilarity()
 ********************************************************************************************************/
	@Override
	protected List<MatchEntities> calculateSimilarity(String moveFromClassFullName, String moveInClassFullName,
							 					      List<Entity> moveFromMethods, List<Entity> moveInMethods){
		
		CalculateEntitySimilarity calculateMethodSimilarity = new CalculateMethodSimilarity(moveFromClassFullName, moveInClassFullName);
		return calculateMethodSimilarity.calculateSimilarity(moveFromMethods, moveInMethods,
														     moveFromClassFullName, moveInClassFullName, 
														     nameSimilarityImportance, entitySimilarityThreshold);
	}
	
/*******************************************************************************************************
 * Description: entityIsValid()
********************************************************************************************************/
	@Override
	public boolean entityIsValid(Character entity) {
		return methodIsValid(entity);
	}
	
/*******************************************************************************************************
 * Description: createEntity()
********************************************************************************************************/
	@Override
	public Entity createEntity(String entityName, Character entityAsCharacter, 
 							   String classFullName, String designType) {

		return createMethod(entityName, entityAsCharacter, classFullName, designType);
	}

/*******************************************************************************************************
 * Description: addToMoveInMap()
********************************************************************************************************/
	@Override
	public void addToMoveInMap(String classFullName, List<Entity> entityList,
							   Map<String, List<Entity>> entitiesMoveInMap){
		
		addMethodToMoveInMap(classFullName, entityList, entitiesMoveInMap);
	}
	
/*******************************************************************************************************
 * Description: addToMoveFromMap()
********************************************************************************************************/
	@Override
	public void addToMoveFromMap(String classFullName, List<Entity> entityList, 
								 Map<String, List<Entity>> entitiesMoveFromMap){
			
		addMethodToMoveFromMap(classFullName, entityList, entitiesMoveFromMap);
	}
	
/*******************************************************************************************************
 * Description: getcandidateMthodRefactorings()
********************************************************************************************************/
	@Override
	public CandidateMethodRefactorings getcandidateMthodRefactorings() {
		return candidateMethodRefactorings;
	}
}