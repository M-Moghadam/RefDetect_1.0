package moea.problem;

import java.util.HashSet;
import java.util.Set;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.spi.OperatorFactory;

import codeInformation.DesiredSourceInformation;
import codeInformation.SourceInformation;
import moea.operators.MyStandardOperators;
import moea.variable.AdvancedCreateChromosome;
import simulation.fogsaa.BestAlignment;
import simulation.fogsaa.DisSimilarity;
import simulation.fogsaa.FOGSAA_Algorithm;
import simulation.simulateRefactoring.Refactoring;

public class RunRefactoringProblem {
	
	//This field has the address of config file
	private static String configFileAddress;
	
	public static String refactoringClassName;
	
/*******************************************************************************************************
 * Description: run()
********************************************************************************************************/
	public NondominatedPopulation run(SourceInformation initialSourceInformation, SourceInformation desiredSourceInformation) {
	    
		RefactoringProblem.set_SIZE_OF_REFACTORING_LIST(1);
		
		//As first step we set desired and initial design in simulation package.
		Refactoring.setDesiredSourceInformation(new DesiredSourceInformation());
		Refactoring.setInitialSourceInformation(initialSourceInformation);
		
		//This line should be moved to configure file.
		Class<DiffRefactoringProblem_NoSimulation> refactoringClass = DiffRefactoringProblem_NoSimulation.class;
		refactoringClassName = refactoringClass.getName();
		
		extractCandidateRefactorings(initialSourceInformation, desiredSourceInformation);
			
		OperatorFactory.getInstance().addProvider(new MyStandardOperators());
		
		NondominatedPopulation result = new Executor()
											    .withAlgorithm("GA")
											    .withProblemClass(refactoringClass)
											    .withProperty("populationSize", 1)
											    .withMaxEvaluations(1)
												.run();
		
		
		//We need to set manually added refactorings to null if there is any.
		configFileAddress = null;
		
		return result;
	}
	
/*******************************************************************************************************
 * Description: extractCandidateRefactorings()
********************************************************************************************************/
	void extractCandidateRefactorings(SourceInformation initialSourceInformation, 
								      SourceInformation desiredSourceInformation) {
		
		setStaticFieldsNull();
		
		RefactoringProblem.Set_Initial_AND_Desired_SourceInformation(initialSourceInformation, desiredSourceInformation);
		
		if (!refactoringClassName.endsWith("BasicRefactoringProblem")) {
			
			// Here, we consider all classes. 
			Set<String> changedClasses = new HashSet<String>();
			changedClasses.addAll(initialSourceInformation.getClassOrder());
			changedClasses.addAll(desiredSourceInformation.getClassOrder());
			
			FOGSAA_Algorithm fogsaa = new FOGSAA_Algorithm(initialSourceInformation, desiredSourceInformation, changedClasses);
			fogsaa.run();
			BestAlignment initialAlignment = fogsaa.getBestAlignment();
			AdvancedProblem.setInitialAlignment(initialAlignment);
			
			//Extract desired source information - Later we can use this information directly
			DesiredSourceInformation.extractDesiredSourceInformation(desiredSourceInformation);
			
			//Extract initial candidates for refactoring
			AdvancedProblem ob;
			if (refactoringClassName.contains("DiffRefactoringProblem")) 
				ob = new DiffRefactoringProblem(0);
			
			else ob = null;
			
			ob.extractInitialCandidateFieldsAndMethods(initialAlignment);
		}
	}
	
/*******************************************************************************************************
 * Description: setStaticFieldsNull()
********************************************************************************************************/
	private void setStaticFieldsNull() {
		
		DisSimilarity.setDesiredInformationNull();
		
		AdvancedCreateChromosome.setCandidateRefactoringsNull();
		
		AdvancedProblem.setStaticFieldsNull();
	}

/*******************************************************************************************************
 * Description: setConfigFileAddress()
********************************************************************************************************/
	public static void setConfigFileAddress(String address) {
		configFileAddress = address;
	}
	
/*******************************************************************************************************
 * Description: getConfigFileAddress()
********************************************************************************************************/
	public static String getConfigFileAddress() {
		return configFileAddress;
	}
}