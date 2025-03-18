package simulation.simulateRefactoring_RefDetect;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import codeInformation.Call_Information.CallingMethods;
import codeInformation.Call_Information;
import codeInformation.SourceInformation;
import codeInformation.SuperSourceInformation;
import simulation.simulateRefactoring.Utility;

public class ExtractMethod extends simulation.simulateRefactoring.ExtractMethod {

/*******************************************************************************************************
 * Description: ExtractMethod()
********************************************************************************************************/
	public ExtractMethod(String originalClassFullName, String originalMethodName, 
						 String targetClassFullName, String extractedMethodName, 
						 String extractedMethodSignature, String extractedMethodReturnType) {
			
		super(originalClassFullName, originalMethodName, targetClassFullName, 
			  extractedMethodName, extractedMethodSignature, extractedMethodReturnType);
	}
	
/*******************************************************************************************************
 * Description: updateRefernces()
 * ********************************************************************************************************/
	@Override
	protected void updateRefernces(SourceInformation sourceInformation, 
							       String targetClassFullName, String extractedMethodName, 
							       Set<String> changedClasses, SuperSourceInformation desiredInformation) {
		
		Map<String, Map<String, Map<String, CallingMethods>>> desiredcalledInformationMap = desiredInformation.get_CallInformation().getCalledInformationMap();		
		Map<String, CallingMethods> map = desiredcalledInformationMap.get(targetClassFullName).get(extractedMethodName);
		
		Call_Information calledInformation = sourceInformation.get_CallInformation();
		List<String> classOrder = sourceInformation.getClassOrder();
		List<String> codeAsString = sourceInformation.getCodeAsString();
		List<Set<String>> R_Information = sourceInformation.get_R_Information();
		
		for (Entry<String, CallingMethods> entry : map.entrySet()) {
			
			String callingClass = entry.getKey();
			
			//We only consider classes which are currently in the program.
			if (!classOrder.contains(callingClass)) continue;
			
			CallingMethods values = entry.getValue();
			
			for (int i = 0; i < values.nameList.size(); i++) {
				
				if (!values.nameList.get(i).equals(getOriginalMethodName())) continue;
				
				calledInformation.setCalledInformationMap(targetClassFullName, extractedMethodName, callingClass, 
														  values.nameList.get(i), values.calledByObjectList.get(i));
			}
			
			//Also add one R in the calling class if the calling and target classes are different.
			if (!targetClassFullName.equals(callingClass)) {
				Utility.add_R_Element(codeAsString, classOrder, callingClass, targetClassFullName, R_Information);
				changedClasses.add(callingClass);
			}
		}
	}
}