package moea.problem;

import moea.variable.RefactoringVariable;

public class BasicRefactoringProblem extends RefactoringProblem {

/*******************************************************************************************************
 * Description: createRefactoringVariable()
********************************************************************************************************/	
	@Override
	RefactoringVariable createRefactoringVariable() {
		return new RefactoringVariable(SIZE_OF_REFACTORING_LIST, 
				initialSourceInformation.clone(), null, this);
	}
}