package simulation.candidateRefactorings;

import java.util.List;

public class RemoveDuplicateRefactorings {

/*******************************************************************************************************
 * Description: removeDuplicate()
********************************************************************************************************/
	static void removeDuplicate(List<CandidateEntity> candidatesRef) {
		
		for (int i = candidatesRef.size() - 1; i > -1 ; i--) {
			
			CandidateEntity ref1 = candidatesRef.get(i);
			
			for (int j = 0; j < i ; j++) {
				
				CandidateEntity ref2 = candidatesRef.get(j);
				
				if (equalRefactorings(ref1, ref2)) {
					candidatesRef.remove(i);
					break;
				}
			}
		}
	}
	
/*******************************************************************************************************
 * Description: equalRefactorings()
********************************************************************************************************/
	private static boolean equalRefactorings(CandidateEntity ref1, CandidateEntity ref2) {
		
		if (ref1.toString().equals(ref2.toString())) return true;
		
		return false;
	}
}