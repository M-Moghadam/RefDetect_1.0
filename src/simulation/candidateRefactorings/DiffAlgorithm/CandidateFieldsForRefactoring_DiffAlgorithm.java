package simulation.candidateRefactorings.DiffAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import simulation.calculateSimilarity.CalculateEntitySimilarity;
import simulation.calculateSimilarity.CalculateFieldSimilarity;
import simulation.candidateRefactorings.CandidateEntity;
import simulation.candidateRefactorings.CandidateRenameEntity;
import simulation.candidateRefactorings.Facility;
import simulation.candidateRefactorings.MatchEntities;
import codeInformation.Entity;
import simulation.candidateRefactorings.CandidateFieldRefactorings;

/*******************************************************************************************************
 * Description: CandidateFieldsForRefactoring_DiffAlgorithm
 * 				This class keeps information about candidate fields for PushDown, PullUp and Move.
********************************************************************************************************/
public class CandidateFieldsForRefactoring_DiffAlgorithm extends CandidateBasedonDiffAlgorithm implements Facility {

	private CandidateFieldRefactorings candidateFieldRefactorings = new CandidateFieldRefactorings();
	
/*******************************************************************************************************
 * Description: decideAboutEntities()
********************************************************************************************************/
	@Override
	public void decideAboutEntities(Map<String, String> renameClassCandidates,
								    float nameSimilarityImportance, float entitySimilarityThreshold){

		this.nameSimilarityImportance = nameSimilarityImportance;
		this.entitySimilarityThreshold = entitySimilarityThreshold;
		
		candidateFieldRefactorings.candidatesForPullUpFieldRef = new ArrayList<CandidateEntity>();
		candidateFieldRefactorings.candidatesForPushDownFieldRef = new ArrayList<CandidateEntity>();
		candidateFieldRefactorings.candidatesForMoveFieldRef = new ArrayList<CandidateEntity>();
		candidateFieldRefactorings.candidatesForRenameFieldRef = new ArrayList<CandidateRenameEntity>();
		
		//Now decide about which field should be Pulled Up or Pushed Down or Move or is Renamed.
		matchedFields = decideAboutEntities(renameClassCandidates, 
									        candidateFieldRefactorings.candidatesForPullUpFieldRef, 
									        candidateFieldRefactorings.candidatesForPushDownFieldRef, 
									        candidateFieldRefactorings.candidatesForMoveFieldRef, 
									        candidateFieldRefactorings.candidatesForRenameFieldRef, 
									        candidateFieldRefactorings.deletedFields, 
									        candidateFieldRefactorings.newFields);
	}
	
/*******************************************************************************************************
 * Description: entityIsValid()
********************************************************************************************************/
	@Override
	public boolean entityIsValid(Character entity) {
		return FieldIsValid(entity);
	}
	
/*******************************************************************************************************
 * Description: createEntity()
********************************************************************************************************/
	@Override
	public Entity createEntity(String entityName, Character entityAsCharacter, 
			 				   String classFullName, String designType) {

		return createField(entityName, entityAsCharacter, classFullName, designType);
	}
	
/*******************************************************************************************************
 * Description: calculateSimilarity()
********************************************************************************************************/
	@Override
	protected List<MatchEntities> calculateSimilarity(String moveFromClassFullName, String moveInClassFullName,
							 					      List<Entity> moveFromFields, List<Entity> moveInFields){
		
		CalculateEntitySimilarity calculateFieldSimilarity = new CalculateFieldSimilarity(moveFromClassFullName, moveInClassFullName);
		return calculateFieldSimilarity.calculateSimilarity(moveFromFields, moveInFields, 
														    moveFromClassFullName, moveInClassFullName,
															nameSimilarityImportance, entitySimilarityThreshold);
	}
	
/*******************************************************************************************************
 * Description: addToMoveInMap()
********************************************************************************************************/
	@Override
	public void addToMoveInMap(String classFullName, List<Entity> entityList,
							   Map<String, List<Entity>> entitiesMoveInMap){
		
		addFieldToMoveInMap(classFullName, entityList, entitiesMoveInMap);
	}
	
/*******************************************************************************************************
 * Description: addToMoveFromMap()
********************************************************************************************************/
	@Override
	public void addToMoveFromMap(String classFullName, List<Entity> entityList, 
						         Map<String, List<Entity>> entitiesMoveFromMap){
			
		addFieldToMoveFromMap(classFullName, entityList, entitiesMoveFromMap);
	}
	
/*******************************************************************************************************
 * Description: getcandidateFieldRefactorings()
********************************************************************************************************/
	@Override
	public CandidateFieldRefactorings getcandidateFieldRefactorings() {
		return candidateFieldRefactorings;
	}
}