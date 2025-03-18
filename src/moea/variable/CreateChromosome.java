package moea.variable;

import java.util.List;
import java.util.Map;
import java.util.Set;


import codeInformation.ElementInformation;
import codeInformation.SourceInformation;
import simulation.simulateRefactoring.Refactoring;

public abstract class CreateChromosome {

	SourceInformation sourceInformation;
	
	List<String> classOredr;
	Map<String, String> classParentMap;
	List<String> childrenClasses;
	Map<String, ElementInformation> classElementMap;
	
/*******************************************************************************************************
 * Description: CreateChromosome()
********************************************************************************************************/
	public CreateChromosome(SourceInformation sourceInformation) {
		
		this.sourceInformation = sourceInformation;
		
		classOredr = sourceInformation.getClassOrder();
		classParentMap = sourceInformation.getClassParentMap();
		childrenClasses = sourceInformation.getChildrenClasses();
		classElementMap = sourceInformation.getClassElementsMap();
	}
	
	public abstract List<Refactoring> createRefactoringList(int sizeofRefactoringList, Set<String> changedClasses);
}