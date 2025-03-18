package simulation.candidateRefactorings;

import codeInformation.Entity;

/*******************************************************************************************************
 * Description: MatchEntities
********************************************************************************************************/
public class MatchEntities {
	
	public String originalClassFullName;
	public String desiredClassFullName;
	public Entity originalEntity;
	public Entity desiredEntity;
	public float normalizedSimilarityValue;
	public float realRelationshipSimilarityValue;

/*******************************************************************************************************
 * Description: MatchEntities()
********************************************************************************************************/	
	public MatchEntities(String originalClassFullName, String desiredClassFullName,
						 Entity originalEntity, Entity desiredEntity, 
						 float normalizedSimilarityValue, float realRelationshipSimilarityValue) {
			
		this.originalClassFullName = originalClassFullName;
		this.desiredClassFullName = desiredClassFullName;
		this.originalEntity = originalEntity;
		this.desiredEntity = desiredEntity;
		this.normalizedSimilarityValue = normalizedSimilarityValue;
		this.realRelationshipSimilarityValue = realRelationshipSimilarityValue;
	}

/*******************************************************************************************************
 * Description: MatchEntities()
********************************************************************************************************/
	public MatchEntities(String originalClassFullName, String desiredClassFullName,
			 			 Entity originalEntity, Entity desiredEntity) { 

		this.originalClassFullName = originalClassFullName;
		this.desiredClassFullName = desiredClassFullName;
		this.originalEntity = originalEntity;
		this.desiredEntity = desiredEntity;
	}
}