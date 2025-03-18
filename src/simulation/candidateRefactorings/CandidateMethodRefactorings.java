package simulation.candidateRefactorings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codeInformation.Entity;

public class CandidateMethodRefactorings {

	//This field contains methods that should be pulled up.
	public List<CandidateEntity> candidatesForPullUpMethodRef;
		
	//This field contains methods that should be pushed down.
	public List<CandidateEntity> candidatesForPushDownMethodRef;
			
	//This field contains methods that should be moved.
	public List<CandidateEntity> candidatesForMoveMethodRef;
	
	//This field contains methods that are extracted using Extract Method refactoring.
	public List<CandidateExtractInlineMethod> candidatesForExtractMethodRef;
	
	//This field contains methods that are inlined using Inline Method refactoring.
	public List<CandidateExtractInlineMethod> candidatesForInlineMethodRef;
	
	//This field contains methods that are renamed.
	public List<CandidateRenameEntity> candidatesForRenameMethodRef;
	
	//This field contains methods that their signature is changed.
	public List<CandidateRenameEntity> candidatesForChangeMethodParametersRef;
	
	//This field contains methods in MoveFrom that we could not find any similar one for them. 
	public Map<String, List<Entity>> deletedMethods = new HashMap<String, List<Entity>>();
	
	//This field contains methods in MoveIn that we could not find any similar one for them. 
	public Map<String, List<Entity>> newMethods = new HashMap<String, List<Entity>>();
	
	
/*******************************************************************************************************
 * Description: removeDuplicate()
 * 				For these three types of refactoring we can have duplicate refactorings in search based algorithm.
 * 				So, we remove them by this method.
********************************************************************************************************/
	public void removeDuplicate(){
		
		RemoveDuplicateRefactorings.removeDuplicate(candidatesForPullUpMethodRef);
		RemoveDuplicateRefactorings.removeDuplicate(candidatesForPushDownMethodRef);
		RemoveDuplicateRefactorings.removeDuplicate(candidatesForMoveMethodRef);
	}
}
