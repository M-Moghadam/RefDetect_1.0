package moea.operators;

import java.util.List;
import java.util.Set;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import moea.variable.RefactoringVariable;
import simulation.simulateRefactoring.Refactoring;
import simulation.simulateRefactoring.SimulateRefactorings;

public abstract class Operators implements Variation {

	//The probability of applying the operator.
	protected final double probability;
	
/*******************************************************************************************************
 * Description: OrderCrossover()
********************************************************************************************************/
	public Operators(double probability) {
		this.probability = probability;
	}
		
/*******************************************************************************************************
 * Description: copy() 
 * 				This method supports a deep copy of chromosome
********************************************************************************************************/
	protected Solution copy(Solution parent) {
		
		Solution result = parent.copy();
		
		RefactoringVariable variable = (RefactoringVariable) result.getVariable(0);
		
		//It supports clone copy
		variable.setRefactorings(((RefactoringVariable)parent.getVariable(0)).getRefactorings());
		
		return result;
	}
	
/*******************************************************************************************************
 * Description: createTwoPoints()
********************************************************************************************************/	
	protected int[] createTwoPoints(int minSize) {
			
		int[] points = new int[2];
			
		int point1 = PRNG.nextInt(minSize);
			
		int point2;
		while (true) {
			point2 = PRNG.nextInt(minSize);
			if (point1 != point2) break;
		}
			
		if (point1 > point2) {
			int swap = point1;
			point1 = point2;
			point2 = swap;
		}
		
		points[0] = point1;
		points[1] = point2;
		
		return points;
	}
	
/*******************************************************************************************************
 * Description: simulateRefactorings()
********************************************************************************************************/
	protected void simulateRefactorings(RefactoringVariable variable, List<Refactoring> refactoringList) {
		
		SimulateRefactorings simulateRefactorings = new SimulateRefactorings(variable.getSourceInformation());
		Set<String> changedClasses = variable.getChangedClasses();
		
		//Apply the refactorings.
		for (int i = 0; i < refactoringList.size(); i++) {
			
			Refactoring applicableRefactoring = refactoringList.get(i);
				
			//Fist simulate the refactoring.
			Set<String> changedCls = simulateRefactorings.simulateRefactoring(applicableRefactoring);
			
			if (changedCls != null)	changedClasses.addAll(changedCls);
			else { refactoringList.remove(i); i--; continue; }
		}
		
		//We update refactoringList in chromosome with changes in refactoringList
		variable.setRefactorings(refactoringList);
	}
	
/*******************************************************************************************************
 * Description: getName()
********************************************************************************************************/
	@Override
	public String getName() {
		return this.getClass().getName();
	}
}