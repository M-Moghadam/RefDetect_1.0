package moea.operators;

import org.moeaframework.core.operator.DefaultOperators;

import moea.operators.crossover.OnePointCrossoverKessentani;
import moea.operators.mutation.MutationKessentani;
import moea.variable.RefactoringVariable;

public class MyStandardOperators extends DefaultOperators  {
	
	public MyStandardOperators() {
		
		setMutationHint(RefactoringVariable.class, "MutationKessentani");
		
		setCrossoverHint(RefactoringVariable.class, "OnePointCrossoverKessentani");
		
		
		register("OnePointCrossoverKessentani", (properties, problem) -> new OnePointCrossoverKessentani(properties.getDouble("OnePointCrossoverKessentani.rate", 0.6)));
		
		register("MutationKessentani", (properties, problem) -> new MutationKessentani(properties.getDouble("MutationKessentani.rate", 0.4)));
		
	}
}		
