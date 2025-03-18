package moea.variable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.moeaframework.core.Variable;

import codeInformation.SourceInformation;
import moea.problem.AdvancedProblem;
import moea.problem.BasicRefactoringProblem;
import moea.problem.DiffRefactoringProblem_NoSimulation;
import moea.problem.RefactoringProblem;
import simulation.fogsaa.BestAlignment;
import simulation.simulateRefactoring.Refactoring;

public class RefactoringVariable implements Variable {

	private static final long serialVersionUID = 1L;

	private double fitnessValue;
	private boolean fitnessEvaluated;

	private List<Refactoring> refactoringList;
	private Set<String> changedClasses;

	private SourceInformation sourceInformation;
	
	private BestAlignment bestAlignment;
	
/*******************************************************************************************************
 * Description: RefactoringVariable()
********************************************************************************************************/
	public RefactoringVariable(int sizeofRefactoringList,
							   SourceInformation initialSourceInformation, 
							   BestAlignment cloneInitialAlignment, RefactoringProblem callingClass) {
		
		this.sourceInformation = initialSourceInformation;
		bestAlignment = cloneInitialAlignment;
		
		CreateChromosome createChromosome = null;
		
		if (callingClass instanceof BasicRefactoringProblem)
			createChromosome = new BasicCreateChromosome(this.sourceInformation);
		
		else if (callingClass instanceof DiffRefactoringProblem_NoSimulation) 
			createChromosome = new DiffCreateChromosome_NoSimulation(this.sourceInformation);
		
		changedClasses = new HashSet<String>();
		refactoringList = createChromosome.createRefactoringList(sizeofRefactoringList, changedClasses);
	}
	
/*******************************************************************************************************
 * Description: RefactoringVariable()
********************************************************************************************************/
	private RefactoringVariable() {
		
		//Note that we clone "refactoringList" after copy finished.
		
		//No need to copy previous changed class, they will be changed after crossover and mutation
		this.changedClasses = new HashSet<String>();
		
		this.sourceInformation = RefactoringProblem.getInitialSourceInformation().clone();
		
		// As it is clone before no need to clone it again.
		this.bestAlignment = AdvancedProblem.getCloneCopyofInitialAlignment();
	}

/*******************************************************************************************************
 * Description: copy()
********************************************************************************************************/
	@Override
	public Variable copy() {
		return new RefactoringVariable();
	}

/*******************************************************************************************************
 * Description: randomize()
********************************************************************************************************/
	@Override
	public void randomize() {
	}

/*******************************************************************************************************
 * Description: getRefactorings()
********************************************************************************************************/
	public List<Refactoring> getRefactorings() {
		return refactoringList;
	}
	
/*******************************************************************************************************
 * Description: getRefactorings()
********************************************************************************************************/
	public  void setRefactorings(List<Refactoring> refactoringList) {
		this.refactoringList = CloneRefactoring.cloneRefactoringList(refactoringList);  
	}

/*******************************************************************************************************
 * Description: getSourceInformation()
********************************************************************************************************/
	public SourceInformation getSourceInformation() {
		return sourceInformation;
	}
	
/*******************************************************************************************************
 * Description: getFitnessValue()
********************************************************************************************************/
	public double getFitnessValue() {
		return fitnessValue;
	}
	
/*******************************************************************************************************
 * Description: setFitnessValue()
********************************************************************************************************/
	public void setFitnessValue(double fitnessValue) {
		this.fitnessValue = fitnessValue;
	}
	
/*******************************************************************************************************
 * Description: isFitnessEvaluated()
********************************************************************************************************/
	public boolean isFitnessEvaluated() {
		return fitnessEvaluated;
	}
	
/*******************************************************************************************************
 * Description: setFitnessEvaluated()
********************************************************************************************************/
	public void setFitnessEvaluated(boolean fitnessEvaluated) {
		this.fitnessEvaluated = fitnessEvaluated;
	}
	
/*******************************************************************************************************
 * Description: getChangedClasses()
********************************************************************************************************/
	public Set<String> getChangedClasses(){
		return changedClasses;
	}
	
/*******************************************************************************************************
 * Description: setBestAlignment()
********************************************************************************************************/
	public void setBestAlignment(BestAlignment bestAlignment) {
		this.bestAlignment = bestAlignment;
	}
	
/*******************************************************************************************************
 * Description: getBestAlignment()
********************************************************************************************************/
	public BestAlignment getBestAlignment() {
		return bestAlignment;
	}

/*******************************************************************************************************
 * Description: encode()
 * 				The method is added in version 3.4 of MOEA, and not sure how to implement them now.
********************************************************************************************************/
	@Override
	public String encode() {
		return null;
	}
	
/*******************************************************************************************************
 * Description: decode()
 *              The method is added in version 3.4 of MOEA, and not sure how to implement them now
********************************************************************************************************/
	@Override
	public void decode(String value) {
	}
}