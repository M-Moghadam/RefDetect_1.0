package moea.operators.crossover;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;

import moea.operators.Operators;
import moea.variable.RefactoringVariable;
import simulation.simulateRefactoring.Refactoring;
import simulation.simulateRefactoring.SimulateRefactorings;

public class OnePointCrossoverKessentani extends Operators {

/*******************************************************************************************************
 * Description: OnePointCrossoverKessentani()
 * 				This cross implement the crossover proposed by the paper "Search-based detection of model level changes"
********************************************************************************************************/
	public OnePointCrossoverKessentani(double probability) {
		super(probability);
	}

/*******************************************************************************************************
 * Description: evolve() 
********************************************************************************************************/
	@Override
	public Solution[] evolve(Solution[] parents) {

		List<Refactoring> refactoringList1 = ((RefactoringVariable) parents[0].getVariable(0)).getRefactorings();
		List<Refactoring> refactoringList2 = ((RefactoringVariable) parents[1].getVariable(0)).getRefactorings();
		
		if ((PRNG.nextDouble() <= probability) && (refactoringList1.size() > 1) && (refactoringList2.size() > 1)) {
		
			Solution result1 = copy(parents[0]);
			Solution result2 = copy(parents[1]);

			RefactoringVariable variable1 = (RefactoringVariable) result1.getVariable(0);
			RefactoringVariable variable2 = (RefactoringVariable) result2.getVariable(0);

			refactoringList1 = variable1.getRefactorings();
			refactoringList2 = variable2.getRefactorings();
			
			int size1 = refactoringList1.size();
			int size2 = refactoringList2.size();
			
			//As two chromosome might have different size, we need to determine crossover point based on the small one.
			int minSize = size1 <= size2 ?  size1 : size2;
			
			int crossoverPoint = PRNG.nextInt(minSize - 1);
			
			swap(refactoringList1, refactoringList2, crossoverPoint);
			
			// Simulate refactorings in first child
			SimulateRefactorings simulateRefactorings = new SimulateRefactorings(variable1.getSourceInformation());
			Set<String> changedClasses1 = variable1.getChangedClasses();
						
			List<Refactoring> removeList = new ArrayList<Refactoring>();
			for (Refactoring ref : refactoringList1) {
				Set<String> changedCls = simulateRefactorings.simulateRefactoring(ref);
				if (changedCls != null) changedClasses1.addAll(changedCls);
				else removeList.add(ref);
			}
			
			// Refactorings which are not applicable are deleted from list.
			for (Refactoring ref : removeList) {
				refactoringList1.remove(ref);
			}
			
			// Simulate refactorings in second child
			removeList.clear();
			simulateRefactorings = new SimulateRefactorings(variable2.getSourceInformation());
			Set<String> changedClasses2 = variable2.getChangedClasses();

			for (Refactoring ref : refactoringList2) {
				Set<String> changedCls = simulateRefactorings.simulateRefactoring(ref);
				if (changedCls != null) changedClasses2.addAll(changedCls);
				else removeList.add(ref);
			}
			
			// Refactorings which are not applicable are deleted from list.
			for (Refactoring ref : removeList) {
				refactoringList2.remove(ref);
			}
			
			if (refactoringList1.isEmpty() || refactoringList2.isEmpty()) 
				return parents;

			return new Solution[] { result1, result2 };
		}

		return parents;
	}

/*******************************************************************************************************
 * Description: swap() 
********************************************************************************************************/
	private void swap(List<Refactoring> refactoringList1, List<Refactoring> refactoringList2, int crossoverPoint) {
		
        ArrayList<Refactoring> tempList1 = new ArrayList<>();
        ArrayList<Refactoring> tempList2 = new ArrayList<>();

        for (int i = crossoverPoint; i < refactoringList1.size(); i++) {
            tempList2.add(refactoringList1.get(i));
        }

        for (int i = refactoringList1.size() - 1; i >= crossoverPoint; i--) {
        	refactoringList1.remove(i);
        }

        for (int i = crossoverPoint; i < refactoringList2.size(); i++) {
            tempList1.add(refactoringList2.get(i));
        }

        for (int i = refactoringList2.size() - 1; i >= crossoverPoint; i--) {
        	refactoringList2.remove(i);
        }

        refactoringList2.addAll(tempList2);

        refactoringList1.addAll(tempList1);
        
		//Note in their paper Kessentani et al. mentioned that the resulting child should have at maximum a length limit with is 600 (page 25).
        while (refactoringList1.size() > 600) {
        	refactoringList1.remove(600);
        }

        // Remove elements starting from index 600 if list2 has more than 600 elements
        while (refactoringList2.size() > 600) {
        	refactoringList2.remove(600);
        }
	}

/*******************************************************************************************************
 * Description: getArity() 
********************************************************************************************************/
	@Override
	public int getArity() {
		return 2;
	}
}