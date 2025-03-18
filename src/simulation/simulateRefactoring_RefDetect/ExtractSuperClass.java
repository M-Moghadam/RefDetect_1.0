package simulation.simulateRefactoring_RefDetect;

import java.util.List;
import java.util.Set;

import codeInformation.SourceInformation;
import codeInformation.SuperSourceInformation;

public class ExtractSuperClass extends simulation.simulateRefactoring.ExtractSuperClass {
	
/*******************************************************************************************************
 * Description: ExtractSuperClass()
********************************************************************************************************/	
	public ExtractSuperClass(String childClassFullName, SourceInformation sourceInformation) {
		super(childClassFullName, sourceInformation);
	}
		
/*******************************************************************************************************
 * Description: ExtractSuperClass()
********************************************************************************************************/	
	public ExtractSuperClass(Set<String> childrenClassesFullName, String newClassFullName) {
		super(childrenClassesFullName, newClassFullName);
	}

/*******************************************************************************************************
 * Description: getParentofNewClass()
********************************************************************************************************/
	@Override
	protected String getParentofNewClass(SuperSourceInformation superSourceInformation, 
									  Set<String> childrenClassesFullName,
									  String newClassFullName){
		
		List<String> classOrder = superSourceInformation.getClassOrder();
		
		String ParentofNewClass = superSourceInformation.getClassParentMap().get(newClassFullName);
		while (!(ParentofNewClass == null || classOrder.contains(ParentofNewClass))) {
			ParentofNewClass = superSourceInformation.getClassParentMap().get(ParentofNewClass);
		}
			
		return ParentofNewClass;
	}
}