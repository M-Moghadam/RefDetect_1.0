package simulation.fogsaa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import codeInformation.ElementInformation;

public class Utility {
	
/*******************************************************************************************************
 * Description: orderStrings()
********************************************************************************************************/	
	static void orderStrings(List<String> resultedStr, List<String> resultedClassOrder, 
							 List<String> desiredStr, List<String> desiredClassOrder){

		ArrayList<String> resultedClassOrdertemp = new ArrayList<String>();

		for (int i = 0; i < resultedClassOrder.size(); i++) {

			String currentClass = resultedClassOrder.get(i);

			int matchindex = desiredClassOrder.indexOf(currentClass);

			/** It finds a similar one.*/
			if (matchindex != -1){

				resultedClassOrdertemp.add(currentClass);
				String temp1 = resultedStr.remove(i);
				resultedStr.add(resultedClassOrdertemp.size() - 1, temp1);

				desiredClassOrder.remove(matchindex);
				desiredClassOrder.add(resultedClassOrdertemp.size() - 1, currentClass);
				String temp2 = desiredStr.remove(matchindex);
				desiredStr.add(resultedClassOrdertemp.size() - 1, temp2);
			}
		}

		for (int i = 0; i < resultedClassOrder.size(); i++) { 

			String currentClass = resultedClassOrder.get(i);

			if (!resultedClassOrdertemp.contains(currentClass))
				resultedClassOrdertemp.add(currentClass);
		}
		
		/** To support pass by reference.*/  
		resultedClassOrder.clear();
		for (String c : resultedClassOrdertemp) {
			resultedClassOrder.add(c);
		}
	}
	
/*******************************************************************************************************
 * Description: convertMethods()		 
********************************************************************************************************/	
	static void convertMethods(GeneralStringInformation generalStringInformation) {
		
		generalStringInformation.str1 = convertMethod(generalStringInformation.str1);
		generalStringInformation.str2 = convertMethod(generalStringInformation.str2);
		generalStringInformation.str3 = convertMethod(generalStringInformation.str3);
		generalStringInformation.str4 = convertMethod(generalStringInformation.str4);
	}
	
/*******************************************************************************************************
 * Description: convertMethod()
 *				The method replace M with N, MP with O, MPP with P, and so on.
 *				(N, O, P, Q, S, T, U, V, W, X, Y, Z) 
********************************************************************************************************/	
	private static String convertMethod(String str) {
		
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP", "^");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP", "%");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP", "$");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP", "#");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP", "@");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPPPPPP", "!");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPPPPP", "9");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPPPP", "8");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPPP", "7");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPPP", "6");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPPP", "5");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPPP", "4");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPPP", "3");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPPP", "2");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPPP", "1");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPPP", "0");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPPP", "B");
		str = str.replaceAll("MPPPPPPPPPPPPPPPPP", "D");
		str = str.replaceAll("MPPPPPPPPPPPPPPPP", "E");
		str = str.replaceAll("MPPPPPPPPPPPPPPP", "F");
		str = str.replaceAll("MPPPPPPPPPPPPPP", "H");
		str = str.replaceAll("MPPPPPPPPPPPPP", "J");
		str = str.replaceAll("MPPPPPPPPPPPP", "K");
		str = str.replaceAll("MPPPPPPPPPPP", "L");
		str = str.replaceAll("MPPPPPPPPPP", "N");
		str = str.replaceAll("MPPPPPPPPP", "O");
		str = str.replaceAll("MPPPPPPPP", "Q");
		str = str.replaceAll("MPPPPPPP", "S");
		str = str.replaceAll("MPPPPPP", "T");
		str = str.replaceAll("MPPPPP", "U");
		str = str.replaceAll("MPPPP", "V");
		str = str.replaceAll("MPPP", "W");
		str = str.replaceAll("MPP", "X");
		str = str.replaceAll("MP", "Y");
		str = str.replaceAll("M", "Z");

		if (str.contains("P")) {
			System.out.println("There is a method in the program with more than 34 parameters!!");
			System.exit(0);
		}
		
		return str;
	}
	
/*******************************************************************************************************
 * Description: getSignature()
********************************************************************************************************/	
	public static String getSignature(Character str){

		if (str.equals('A')) return "A";
		
		switch (str) {
		
			case '^': return "MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP";
			case '%': return "MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP";
			case '$': return "MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP";
			case '#': return "MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP";
			case '@': return "MPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP";
			case '!': return "MPPPPPPPPPPPPPPPPPPPPPPPPPPPPP";
			case '9': return "MPPPPPPPPPPPPPPPPPPPPPPPPPPPP";	
			case '8': return "MPPPPPPPPPPPPPPPPPPPPPPPPPPP";	
			case '7': return "MPPPPPPPPPPPPPPPPPPPPPPPPPP";	
			case '6': return "MPPPPPPPPPPPPPPPPPPPPPPPPP";	
			case '5': return "MPPPPPPPPPPPPPPPPPPPPPPPP";	
			case '4': return "MPPPPPPPPPPPPPPPPPPPPPPP";	
			case '3': return "MPPPPPPPPPPPPPPPPPPPPPP";
			case '2': return "MPPPPPPPPPPPPPPPPPPPPP";
			case '1': return "MPPPPPPPPPPPPPPPPPPPP";
			case '0': return "MPPPPPPPPPPPPPPPPPPP";
			case 'B': return "MPPPPPPPPPPPPPPPPPP";
			case 'D': return "MPPPPPPPPPPPPPPPPP";
			case 'E': return "MPPPPPPPPPPPPPPPP";
			case 'F': return "MPPPPPPPPPPPPPPP";
			case 'H': return "MPPPPPPPPPPPPPP";
			case 'J': return "MPPPPPPPPPPPPP";
			case 'K': return "MPPPPPPPPPPPP";
			case 'L': return "MPPPPPPPPPPP";
			case 'N': return "MPPPPPPPPPP";
			case 'O': return "MPPPPPPPPP";
			case 'Q': return "MPPPPPPPP";
			case 'S': return "MPPPPPPP";
			case 'T': return "MPPPPPP";
			case 'U': return "MPPPPP";
			case 'V': return "MPPPP";
			case 'W': return "MPPP";
			case 'X': return "MPP";
			case 'Y': return "MP";
			case 'Z': return "M";
		}

		return null;
	}
	
/***********************************************************************************************************
 * Description : isMethodAbbrevation()
**************************************************************************************************************/
	public static boolean isMethodAbbrevation(char abbrevation) {
			
		switch (abbrevation) {

			case '^': 
			case '%': 
			case '$': 
			case '#': 
			case '@': 
			case '!': 	
			case '9':
			case '8':
			case '7':
			case '6':
			case '5':
			case '4': 
			case '3': 
			case '2': 
			case '1': 
			case '0':
			case 'B': 
			case 'D': 
			case 'E': 
			case 'F': 
			case 'H': 
			case 'J': 
			case 'K': 
			case 'L': 
			case 'N': 
			case 'O': 
			case 'Q': 
			case 'S': 
			case 'T': 
			case 'U': 
			case 'V': 
			case 'W': 
			case 'X': 
			case 'Y': 
			case 'Z': return true;
		}
			
		return false;
	}
	
/*******************************************************************************************************
 * Description: removeRs()
********************************************************************************************************/
	static void removeRs(GeneralStringInformation generalStringInformation){

		generalStringInformation.str1 = generalStringInformation.str1.replace("R", "");
		generalStringInformation.str2 = generalStringInformation.str2.replace("R", "");
		generalStringInformation.str3 = generalStringInformation.str3.replace("R", "");
		generalStringInformation.str4 = generalStringInformation.str4.replace("R", "");
	}
	
/***********************************************************************************************************
 * Description : getLocalStr()
 * 				 The method returns a substring from "generalStr" which starts at "generalPosition", 
 * 				 and finish before "C". It returns a class as string.
**************************************************************************************************************/
	static String getLocalStr(String generalStr, int generalPosition){
			
		int endIndex = generalStr.indexOf("C", generalPosition + 1);
			
		if (endIndex == -1) 
			endIndex = generalStr.length();
			
		return generalStr.substring(generalPosition, endIndex); 
	}
	
/*******************************************************************************************************
 * Description: cloneList()
********************************************************************************************************/
	public static <T> List<T> cloneList(List<T> original){
				
		List<T> clone = new ArrayList<T>();
				
		for (T element : original) clone.add(element);
				
		return clone;
	}
	
/***********************************************************************************************************
 * Description : getElementFullName()
**************************************************************************************************************/
	static String getElementFullName(char character, int position, String currentClassFullName,
									 Map<String, ElementInformation> classElementsMap,
									 Map<String, String> classParentMap,
									 Map<String, List<String>> R_Information) {
			
		String elementFullName = "";
			
		if (character == '-') return elementFullName;
			
		switch(character) {
		
			case 'C':
				elementFullName = currentClassFullName;
				break;
				
			case 'G':
				elementFullName = classParentMap.get(currentClassFullName);
				break;
				
			case 'A':
				elementFullName = classElementsMap.get(currentClassFullName).fields.get(position).getName();
				break;
				
			case 'R':{
				elementFullName = R_Information.get(currentClassFullName).get(position);
				break;
			}
				
			/** In this case it is a method.*/
			default:{
				elementFullName = classElementsMap.get(currentClassFullName).methods.get(position).getName();
			}
		}
			
		if (elementFullName.isEmpty()) {
			System.out.println("Bug: Element is not exist");
			System.exit(0);
		}
			
		return elementFullName;
	}
}