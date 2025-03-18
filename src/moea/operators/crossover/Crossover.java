package moea.operators.crossover;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import moea.operators.Operators;
import simulation.simulateRefactoring.Refactoring;

public abstract class Crossover extends Operators {

/*******************************************************************************************************
 * Description: Crossover()
********************************************************************************************************/
	public Crossover(double probability) {
		super(probability);
	}
	
/*******************************************************************************************************
 * Description: createTwoPoints() 
********************************************************************************************************/
	int[] createTwoPoints(int size1, int size2) {
		
		int minSize = size1 <= size2 ?  size1 : size2;
		
		int[] points = createTwoPoints(minSize);
		
		return points;
	}
	
/*******************************************************************************************************
 * Description: getSimilarRefactorings() 
********************************************************************************************************/
	Set<Refactoring> getSimilarRefactorings(int crossoverPoint1, int crossoverPoint2, 
											List<Refactoring> refactoringList1, List<Refactoring> refactoringList2){
		
		Set<Refactoring> SimilarRefactorings = new HashSet<Refactoring>();
		
		for (int i = crossoverPoint1; i < crossoverPoint2; i++) {
			
			Refactoring ref1 = refactoringList1.get(i);
			
			String ref1Description = ref1.refactoringToString();
			
			for (int j = crossoverPoint1; j < crossoverPoint2; j++) {
				
				Refactoring ref2 = refactoringList2.get(j);
				
				if (ref1Description.equals(ref2.refactoringToString())) {
					
					SimilarRefactorings.add(ref1);
					
					break;
				}
			}
		}
		
		return SimilarRefactorings;
	}
	
/*******************************************************************************************************
 * Description: getArity() 
********************************************************************************************************/
	@Override
	public int getArity() {
		return 2;
	}
}
