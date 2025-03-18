package simulation.fogsaa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/***********************************************************************************************************
 * Description : GeneralStringInformation
**************************************************************************************************************/
public class GeneralStringInformation {

	//Contains information about classes in resulted design.
	String str1 = "";

	//Contains information about classes in desired design.
	String str2 = "";
	
	//"str1" and "str2" contains same classes and in the same order

	//Contains information about classes contains in resulted design, but not contain in desired design.
	String str3 = "";
	List<String> classOrderFullName3;

	//Contains information about classes contains in desired design, but not contain in resulted design.
	String str4 = "";
	List<String> classOrderFullName4;
	
/*******************************************************************************************************
 * Description: getStrings()
 *				Move classes from "str1" to "str3" if they are not in "str2". A similar technique 
 *				for "str2". It results the number of classes in "str1" and "str2" become the same. 			 
********************************************************************************************************/	
	void getStrings(List<String> resultedStr, List<String> resultedClassOrder, 
			        List<String> desiredStr, List<String> desiredClassOrder, 
			        Set<String> changedClasses){

		int resultedClassOrderSize = resultedClassOrder.size();
		int desiredClassOrderSize  = desiredClassOrder.size();

		int max = resultedClassOrderSize >= desiredClassOrderSize ? resultedClassOrderSize : desiredClassOrderSize;

		int i = 0;
		for (; i < resultedClassOrderSize; i++) {

			if (desiredClassOrderSize <= i) break;

			if (!resultedClassOrder.get(i).equals(desiredClassOrder.get(i))) break; 
		}

		for (int j = 0; j < i; j++) {  

			String clsUnderConsidresion = resultedClassOrder.get(j);
			
			/** Only classes which are changed needs to be compared.*/
			if (!isChanged(clsUnderConsidresion, changedClasses)) 
				continue;

			str1 += resultedStr.get(j);
			str2 += desiredStr.get(j);
		}

		classOrderFullName3 = new ArrayList<String>();
		classOrderFullName4 = new ArrayList<String>();
		for (int j = i; j < max; j++) { 

			if (resultedStr.size() > j && isChanged(resultedClassOrder.get(j), changedClasses)) {
				str3 += resultedStr.get(j);
				classOrderFullName3.add(resultedClassOrder.get(j));
			}

			if (desiredStr.size() > j  && isChanged(desiredClassOrder.get(j), changedClasses))  {
				str4 += desiredStr.get(j);
				classOrderFullName4.add(desiredClassOrder.get(j));
			}
		}
	}
		
/*******************************************************************************************************
 * Description: isChanged()
********************************************************************************************************/
	private boolean isChanged(String classFullName, Set<String> changedClasses){

		/** In a case that the FOGSSA is called two compare two designs.*/
		if (changedClasses == null) return true;

		if (changedClasses.contains(classFullName))	return true;

		return false;
	}
}