package simulation.fogsaa;

/***********************************************************************************************************
 * Description : NodeInformation
 * 				 A class responsible for creating a node.
**************************************************************************************************************/
public class NodeInformation {

	/** As some Example: "C,C" or "A,-" or "G,A"*/
	String nodeName;

	double presentScore;

	/** [Tmax and Tmin]*/
	FutureScore fitnessScore;

	/**[p1 and p2]*/
	Position position;

	/** The type of alignment: 
		Type = 1: indicates a match. Type = 2: indicates a mismatch.
		Type = 3: indicates a gap in first element. Type = 4: indicates a gap in second element.*/
	int type;
	
	/** Part1Name is related to part1 and Part2Name is related to part2 in nodeName.
	    If part1 is a class (C) => Part1Name contains full name of the class.
	    If part1 is a generalization relation (G) => Part1Name contains full name of parent class.
	    If part1 is an attribute => Part1Name is name of the attribute.
	    If part1 is a method => Part1Name contains name of the method.
	    If part1 is a relation => Part1Name contains full name of called class.*/
	String Part1Name;
	String Part2Name;

	NodeInformation(String nodeName, double presentScore, FutureScore fitnessScore, 
					Position position, int type, 
					String Part1Name, String Part2Name){
		
		this.nodeName = nodeName;
		this.presentScore = presentScore;
		this.fitnessScore = fitnessScore;
		this.position = position;
		this.type = type;
		
		this.Part1Name = Part1Name;
		this.Part2Name = Part2Name;
	}
}

/***********************************************************************************************************
 * Description : Position
 * 				 A class which keeps two values (position1 and position2).
**************************************************************************************************************/
	class Position {
	
		final int position1;
		final int position2;

		Position(final int first, final int second) {
			this.position1 = first;
			this.position2 = second;
		}
	}
	
/***********************************************************************************************************
 * Description : FutureScore
 * 				 A class which keeps two values (fMax and Fmin).
**************************************************************************************************************/
	class FutureScore {
			
		final double fMax;
		final double fMin;

		FutureScore(final double fMax, final double fMin) {
			this.fMax = fMax;
			this.fMin = fMin;
		}
	}
