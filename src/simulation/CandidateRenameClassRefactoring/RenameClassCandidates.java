package simulation.CandidateRenameClassRefactoring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import configuration.Thresholds;

public class RenameClassCandidates {

	//Key is the name of the original class, value is a map that its key is the new name of class, and value is their similarity.
	private static Map<String, Map<String, Float>> candidates;
	
/*******************************************************************************************************
 * Description: RenameClassCandidates()
********************************************************************************************************/
	public RenameClassCandidates() {
		candidates = new HashMap<String, Map<String, Float>>();
	}
	
/*******************************************************************************************************
 * Description: put()
********************************************************************************************************/
	public void put(String originalClassFullName, String renamedClassFullName, Float similarityValue) {
		
		if (candidates.get(originalClassFullName) != null) {
			System.out.println("Bug: A class is renamed to two classes.");
			System.exit(0);
		}
		
		Map<String, Float> map = new HashMap<String, Float>();
		map.put(renamedClassFullName, similarityValue);
		
		candidates.put(originalClassFullName, map);
	}
		
/*******************************************************************************************************
 * Description: getSimilarityValueUsingOriginalClass()
********************************************************************************************************/
	public static float getSimilarityValueUsingOriginalClass(String originalClassFullName) {
		
		Map<String, Float> renameClassCandidate = candidates.get(originalClassFullName);
		
		if (renameClassCandidate == null) return -1;
		
		return renameClassCandidate.values().iterator().next();
	}
	
/*******************************************************************************************************
* Description: getSimilarityValueUsingTargetClass()
********************************************************************************************************/
	public static float getSimilarityValueUsingTargetClass(String targetClassFullName) {
	
		float finalSimilarity = -1;
		
		for (Entry<String, Map<String, Float>> entry : candidates.entrySet()) {
		
			Float similarity = entry.getValue().get(targetClassFullName);
			
			if (similarity == null) continue;
			
			if (similarity >= finalSimilarity) finalSimilarity = similarity;
		}
			
		return finalSimilarity;
	}
	
/*******************************************************************************************************
* Description: isItStrongRenameClassCandidate()
********************************************************************************************************/
	public static boolean isItStrongRenameClassCandidate(String originalClassFullName, 
													     String targetClassFullName) {
		
		Map<String, Float> result = candidates.get(originalClassFullName);
		
		if (result != null) {
		
			String renameClassCandidate = result.keySet().iterator().next();
			float similarityValue = result.values().iterator().next();
		
			if (renameClassCandidate.equals(targetClassFullName) && similarityValue >= Thresholds.classConfidentThreshold) return true;
		}
		
		return false;
	}	
}
