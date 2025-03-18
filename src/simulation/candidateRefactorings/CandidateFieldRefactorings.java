package simulation.candidateRefactorings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codeInformation.Entity;

public class CandidateFieldRefactorings {

	//This field contains fields that should be pulled up.
	public List<CandidateEntity> candidatesForPullUpFieldRef;
		
	//This field contains fields that should be pushed down.
	public List<CandidateEntity> candidatesForPushDownFieldRef;
			
	//This field contains fields that should be moved.
	public List<CandidateEntity> candidatesForMoveFieldRef;
		
	//This field contains fields that are renamed.
	public List<CandidateRenameEntity> candidatesForRenameFieldRef;
	
	//This field contains fields in MoveFrom that we could not find any similar one for them. 
	public Map<String, List<Entity>> deletedFields = new HashMap<String, List<Entity>>();
		
	//This field contains fields in MoveIn that we could not find any similar one for them. 
	public Map<String, List<Entity>> newFields =new HashMap<String, List<Entity>>();
	
/*******************************************************************************************************
 * Description: removeDuplicate()
 * 				For these three types of refactoring we can have duplicate refactorings in search based algorithm.
 * 				So, we remove them by this method.
********************************************************************************************************/
	public void removeDuplicate(){
		RemoveDuplicateRefactorings.removeDuplicate(candidatesForPullUpFieldRef);
		RemoveDuplicateRefactorings.removeDuplicate(candidatesForPushDownFieldRef);
		RemoveDuplicateRefactorings.removeDuplicate(candidatesForMoveFieldRef);
	}
}
