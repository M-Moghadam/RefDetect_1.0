package moea.operators.mutation;

import java.util.List;
import java.util.Map;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;

import moea.operators.Operators;
import moea.variable.AdvancedCreateChromosome;
import moea.variable.RefactoringVariable;
import simulation.simulateRefactoring.Refactoring;

public class MutationKessentani extends Operators {

/*******************************************************************************************************
 * Description: MutationKessentani()
 *  * 			This cross implement the mutation proposed by the paper "Search-based detection of model level changes"
********************************************************************************************************/
	public MutationKessentani(double probability) {
		super(probability);
	}

/*******************************************************************************************************
 * Description: evolve() 
********************************************************************************************************/
	@Override
	public Solution[] evolve(Solution[] parents) {

		List<Refactoring> refactoringList = ((RefactoringVariable) parents[0].getVariable(0)).getRefactorings();
		
		 Map<String, List<Refactoring>> allCandidateRefactorings = AdvancedCreateChromosome.getCandidateRefactorings();

		if (PRNG.nextDouble() <= probability && refactoringList.size() > 1) {

			Solution result = copy(parents[0]);

			RefactoringVariable variable = (RefactoringVariable) result.getVariable(0);
			refactoringList = variable.getRefactorings();
			
			for (int i = 0; i < refactoringList.size(); i++) {
				
				//For each gene if the random value is less than 0.2, the selected gene is mutated.
				if (PRNG.nextDouble() > 0.2) continue;
				
				String refactoringType = refactoringList.get(i).getRefactoringType();
				
				//A refactoring can be replaced with another refactoring with similar type.
				List<Refactoring> newCandidateRefactorings = allCandidateRefactorings.get(refactoringType);
			
				Refactoring newRefactoring = PRNG.nextItem(newCandidateRefactorings);
				
				refactoringList.set(i, newRefactoring);
			}
			
			// Simulate refactorings in the first child
			simulateRefactorings(variable, refactoringList);
						
			if (refactoringList.isEmpty()) return parents;
						
			return new Solution[] { result };
		}

		return parents;
	}

/*******************************************************************************************************
 * Description: getArity()
********************************************************************************************************/
	@Override
	public int getArity() {
		return 1;
	}
}