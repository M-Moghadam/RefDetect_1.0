package moea.variable;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import codeInformation.SourceInformation;
import simulation.simulateRefactoring.Refactoring;

public class DiffCreateChromosome_NoSimulation extends AdvancedCreateChromosome {

/*******************************************************************************************************
 * Description: DiffCreateChromosome_NoSimulation()
********************************************************************************************************/
	public DiffCreateChromosome_NoSimulation(SourceInformation sourceInformation) {
		super(sourceInformation);
	}
	
/*******************************************************************************************************
 * Description: createRefactoringList()
********************************************************************************************************/
	@Override
	public List<Refactoring> createRefactoringList(int sizeofRefactoringList, Set<String> changedClasses) {
		
		List<Refactoring> refactoringList = new ArrayList<Refactoring>();

		for (List<Refactoring> candidateRefactoringList : candidateRefactorings.values()) {
			
			for (Refactoring candidateRefactoring : candidateRefactoringList) {
				
				refactoringList.add(candidateRefactoring);
			}
		}
		
		return refactoringList;
	}
}