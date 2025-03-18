package moea.problem;

import java.util.HashSet;
import java.util.Set;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import codeInformation.SourceInformation;
import moea.variable.RefactoringVariable;
import simulation.fogsaa.FOGSAA_Algorithm;
import simulation.simulateRefactoring.SimulateRefactorings;

public abstract class RefactoringProblem extends AbstractProblem {

	private static final int NUMBER_OF_VARIABLE = 1;
	private static final int NUMBER_OF_OBJECTIVE = 1;
	static int SIZE_OF_REFACTORING_LIST;

	static SourceInformation initialSourceInformation;
	private static SourceInformation desiredSourceInformation;
	
/*******************************************************************************************************
 * Description: RefactoringProblem()
********************************************************************************************************/
	public RefactoringProblem() {
		
		super(NUMBER_OF_VARIABLE, NUMBER_OF_OBJECTIVE);
		
		//We set these values as null for each test case
		SimulateRefactorings.setglobalChangedClassesNull(); 
		SimulateRefactorings.originalClasses = null;
	}

/*******************************************************************************************************
 * Description: evaluate()
********************************************************************************************************/
	@Override
	public void evaluate(Solution solution) {
		
		//No need to run any evaluation if "DiffRefactoringProblem_NoSimulation" is running.
		if (RunRefactoringProblem.refactoringClassName.endsWith("DiffRefactoringProblem_NoSimulation")) 
			return;
		
		// Variable solution
		RefactoringVariable refactoringVariable = (RefactoringVariable) solution.getVariable(0);

		//If no refactoring is detected no need to run evaluation.
		if (refactoringVariable.getRefactorings().isEmpty()) return;
		
		//If the chromosome is evaluated before no need to evaluated again.		
		if (refactoringVariable.isFitnessEvaluated()) return;
		
		SourceInformation resultedSourceInformation = refactoringVariable.getSourceInformation();

		/** To have a precise value in FOGSAA algorithm, we run the algorithm on classes that are 
		changed in this chromosome + all classes that are changed in other chromosome and are included 
		in the original design.*/
		Set<String> changedClasses = new HashSet<String>(); 
		changedClasses.addAll(SimulateRefactorings.getglobalChangedClasses());
		changedClasses.addAll(refactoringVariable.getChangedClasses());
		
		FOGSAA_Algorithm fogsaa = new FOGSAA_Algorithm(resultedSourceInformation, 
													   desiredSourceInformation,
													   changedClasses); 
			
		refactoringVariable.setFitnessValue(-fogsaa.run());
		refactoringVariable.setBestAlignment(fogsaa.getBestAlignment());
		refactoringVariable.setFitnessEvaluated(true);

		// set object solution
		solution.setObjective(0, refactoringVariable.getFitnessValue());
	}
	
/*******************************************************************************************************
 * Description: newSolution()
********************************************************************************************************/
	@Override
	public Solution newSolution() {

		Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives());
		solution.setVariable(0, createRefactoringVariable());

		return solution;
	}
	
/*******************************************************************************************************
 * Description: createRefactoringVariable()
********************************************************************************************************/	
	abstract RefactoringVariable createRefactoringVariable();
	
/*******************************************************************************************************
 * Description: getInitialSourceInformation()
********************************************************************************************************/
	public static SourceInformation getInitialSourceInformation() {
		return initialSourceInformation;
	}
	
/*******************************************************************************************************
 * Description: getDesiredSourceInformation()
********************************************************************************************************/
	public static SourceInformation getDesiredSourceInformation() {
		return desiredSourceInformation;
	}

/*******************************************************************************************************
 * Description: Set_Initial_AND_Desired_SourceInformation()
********************************************************************************************************/
	public static void Set_Initial_AND_Desired_SourceInformation(SourceInformation initialSourceInformation,
																 SourceInformation desiredSourceInformation) {
		
		RefactoringProblem.initialSourceInformation = initialSourceInformation;
		RefactoringProblem.desiredSourceInformation = desiredSourceInformation;
	}

/*******************************************************************************************************
 * Description: set_SIZE_OF_REFACTORING_LIST()
********************************************************************************************************/
	public static void set_SIZE_OF_REFACTORING_LIST(int number) {
		SIZE_OF_REFACTORING_LIST = number;
	}
}