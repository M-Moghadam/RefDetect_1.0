package simulation.candidateRefactorings.DiffAlgorithm;

import java.util.List;
import java.util.Map;

import codeInformation.Entity;
import simulation.candidateRefactorings.CandidateRefactorings;
import simulation.candidateRefactorings.CandidateRenameEntity;
import simulation.candidateRefactorings.MatchEntities;

public abstract class CandidateBasedonDiffAlgorithm extends CandidateRefactorings {

/*******************************************************************************************************
 * Description: keepMatchestEntities()
********************************************************************************************************/
	protected void keepMatchestEntities(List<MatchEntities> matchEntityList) {
		
		Service.keepMatchestEntities(matchEntityList);
	}
	
/*******************************************************************************************************
 * Description: didOnlyRenameRefactoringHappen()
********************************************************************************************************/
	@Override
	protected boolean didOnlyRenameRefactoringHappen(Entity originalEntity, Entity desiredEntity, 
								         		     String moveInClassFullName, String moveFromClassFullName,
								         		     Map<String, String> renameClassCandidates,
								         		     List<CandidateRenameEntity> candidateEntitiesForRename){
		
		/** If "moveFromClassFullName" is renamed to "moveInClassFullName", but entities have similar name,
		    then it is a rename class refactoring, and no need to check entities for refactoring.*/
		if (Service.classeIsRenamed(moveFromClassFullName, moveInClassFullName, renameClassCandidates)) 	
			if (getName(originalEntity.getName()).equals(getName(desiredEntity.getName()))) 
				return true;
			
		
		if (createRenameRefactoring(originalEntity, desiredEntity, 
		        			        moveInClassFullName, moveFromClassFullName,
		                            renameClassCandidates, candidateEntitiesForRename)) {
		
			//No need to check the rest of refactorings, if it is a simple Rename refactoring.
			if (Service.classeIsRenamed(moveFromClassFullName, moveInClassFullName, renameClassCandidates)) 
				return true;
		}
		
		return false;
	}
}