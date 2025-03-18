package simulation.fogsaa;

import java.util.List;

/***********************************************************************************************************
 * Description : Similarity()
**************************************************************************************************************/
public class Similarity {
		
	public String classFullName;
	
	public List<Character> resultedAlignment;
	public List<String> resultedAlignmentFullName;
	
	public List<Character> desiredAlignment;
	public List<String> desiredAlignmentFullName;
	
	public List<Integer> types;
	
	double currentScore;
	
	//The maximum possible value that we can gain.
	double possibleMaximumScore;
	
	Similarity(String classFullName,
			   List<Character> resultedAlignment,
			   List<String> resultedAlignmentFullName,
			   List<Character> desiredAlignment, 
			   List<String> desiredAlignmentFullName,
			   List<Integer> types, double currentScore, double possibleMaximumScore){
		
		this.classFullName = classFullName;
		
		this.resultedAlignment = resultedAlignment;
		this.resultedAlignmentFullName = resultedAlignmentFullName;
		
		this.desiredAlignment = desiredAlignment;
		this.desiredAlignmentFullName = desiredAlignmentFullName;
		
		this.types = types;
		
		this.currentScore = currentScore;
		
		this.possibleMaximumScore = possibleMaximumScore;
	}
}	