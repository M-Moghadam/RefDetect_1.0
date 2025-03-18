package simulation.fogsaa;

public class FOGSAA_Parameters {

	/** The values for reward and penalty.*/
	static double match = 1;
	static double mismatch = -1;
	
	/** If we set different weights to a method and its parameters, it is necessary to set gap = 0
	if we want to have a almost close values for fMax and fMin in function "measureFitnessScore", 
	and also a close dissimilarity value when measuring dissimilarity in function "run". 
	More details in these functions.
	
	However, if we use different character such as X, Y, Z, .. for methods and their parameters, 
	then we can set any value to gap.*/
	static double gap = -0.5;
	
	/** To prevent extra or missing classes in resulted design, we can increase negative effect of 
	    dissimilarity. More weight to this field, increase negative effect of miss or extra classes.*/
	static double dissimilarityEffect = 2;
}