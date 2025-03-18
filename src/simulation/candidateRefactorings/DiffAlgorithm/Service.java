package simulation.candidateRefactorings.DiffAlgorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import moea.problem.AdvancedProblem;
import moea.problem.RefactoringProblem;
import codeInformation.DesiredSourceInformation;
import codeInformation.ElementInformation;
import codeInformation.Entity;
import codeInformation.Field;
import codeInformation.Method;
import simulation.candidateRefactorings.CandidateRefactorings;
import simulation.candidateRefactorings.MatchEntities;
import simulation.simulateRefactoring.Utility;

public class Service {
	
/*******************************************************************************************************
 * Description: keepMatchestEntities()
********************************************************************************************************/
	public static void keepMatchestEntities(List<MatchEntities> matchEntityList) {

		Set<MatchEntities> removeList = new HashSet<MatchEntities>();

		for (int i = 0; i < matchEntityList.size(); i++) {

			MatchEntities matchEntities1 = matchEntityList.get(i);

			//If it is marked as deleted before, no need to check more.
			if (removeList.contains(matchEntities1)) continue;

			float normalizedSimilarityValue1 = matchEntities1.normalizedSimilarityValue;
			float relationshipSimilarityValue1 = matchEntities1.realRelationshipSimilarityValue;

			String originalClassFullname = matchEntities1.originalClassFullName;
			String originalEntityName = matchEntities1.originalEntity.getName();
			String originalEntitySignature = matchEntities1.originalEntity.getSignature();

			String desiredClassFullname = matchEntities1.desiredClassFullName;
			String desiredEntityName = matchEntities1.desiredEntity.getName();
			String desiredEntitySignature = matchEntities1.desiredEntity.getSignature();

			boolean flag = false;
			for (int j = matchEntityList.size() - 1; j > i; j--) {

				MatchEntities matchEntities2 = matchEntityList.get(j);

				float normalizedSimilarityValue2 = matchEntities2.normalizedSimilarityValue;
				float relationshipSimilarityValue2 = matchEntities2.realRelationshipSimilarityValue;

				//As the list is sorted we can break and no continue the second loop
				if (normalizedSimilarityValue1 == normalizedSimilarityValue2 && relationshipSimilarityValue1 == relationshipSimilarityValue2) {
					flag = true;
					
					//Later I should include the below instruction in Similarity formula.
					if (normalizedSimilarityValue1 >= 0.6 && relationshipSimilarityValue1 >= 9 && 
						(!matchEntities1.originalEntity.getName().equals(matchEntities1.desiredEntity.getName()))) {
						
						break;
					}
				} 

				//An original entity can not be match with two desired entities
				if (originalClassFullname.equals(matchEntities2.originalClassFullName)) {

					if (originalEntityName.equals(matchEntities2.originalEntity.getName()))

						if (originalEntitySignature.equals(matchEntities2.originalEntity.getSignature())) {

							//An original entity can not be matched to a class twice.
							if (desiredClassFullname.equals(matchEntities2.desiredClassFullName)) {
								remove(matchEntities1, matchEntities2, removeList, flag);
								continue;
							}
							
							//An original entity can be matched with more than one entities if they are in its inner classes
							if (hasSimilarOutterClass(desiredClassFullname, matchEntities2.desiredClassFullName, originalClassFullname))
								continue;

							//An original entity can be pushed down to more than one subclass.
							if (!isPartOf_A_HierarchyStructure(originalClassFullname, desiredClassFullname, matchEntities2.desiredClassFullName)) {
								remove(matchEntities1, matchEntities2, removeList, flag);
								continue;
							}
						}
				}

				//An desired entity can not be match with two original entities
				if (desiredClassFullname.equals(matchEntities2.desiredClassFullName)) {
					if (desiredEntityName.equals(matchEntities2.desiredEntity.getName()))
						if (desiredEntitySignature.equals(matchEntities2.desiredEntity.getSignature())) {

							//An desired entity can not be matched to a class twice.
							if (originalClassFullname.equals(matchEntities2.originalClassFullName)) {
								remove(matchEntities1, matchEntities2, removeList, flag);
								continue;
							}

							//An desired entity can be resulted because of more than one entities of its inner classes
							if (hasSimilarOutterClass(originalClassFullname, matchEntities2.originalClassFullName, desiredClassFullname))
								continue;
							
							//An desired entity can be resulted because of more than one pull up.
							if (!isPartOf_A_HierarchyStructure(desiredClassFullname, originalClassFullname, matchEntities2.originalClassFullName)) {
								remove(matchEntities1, matchEntities2, removeList, flag);
							}
						}
				}
			}
		}

		for (MatchEntities weakMatch : removeList) {
			matchEntityList.remove(weakMatch);
		}
	}
	
/*******************************************************************************************************
 * Description: hasSimilarOutterClass()
*********************************************************************************************************/
	private static boolean hasSimilarOutterClass(String classFullName1, String classFullName2, String outterClassFullName) {
		
		int index1 = classFullName1.lastIndexOf(".");
		
		if (outterClassFullName.length() != index1) return false;
		
		Map<String, Set<String>> nestedClassMap = RefactoringProblem.getInitialSourceInformation().getNestedClassMap();
		Set<String> nestedClassses = nestedClassMap.get(outterClassFullName);
		
		try {
			
			if (nestedClassses.contains(classFullName1.substring(index1 + 1)) && nestedClassses.contains(classFullName2.substring(index1 + 1)))
				return true;
		
		}catch (IndexOutOfBoundsException | NullPointerException e) {}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: remove()
*********************************************************************************************************/
	private static void remove(MatchEntities matchEntities1, MatchEntities matchEntities2, 
							   Set<MatchEntities> removeList, boolean flag) {
		
		/** If similar entity has less similarity value, it will be deleted.*/
		if (!flag) { removeList.add(matchEntities2); return; }
		
		/** If they have different type, then remove them.*/
		boolean flag1 = false;
		String originalEntityType = matchEntities1.originalEntity.getEntityTypeFullName();
		String desiredEntityType = matchEntities1.desiredEntity.getEntityTypeFullName();
		if (Service.hasSimilarType(originalEntityType, desiredEntityType) == 0) {
			removeList.add(matchEntities1);
			flag1 = true;
		}

		boolean flag2 = false;
		originalEntityType = matchEntities2.originalEntity.getEntityTypeFullName();
		desiredEntityType = matchEntities2.desiredEntity.getEntityTypeFullName();
		if (Service.hasSimilarType(originalEntityType, desiredEntityType) == 0) {
			removeList.add(matchEntities2);
			flag2 = true;
		}

		if (flag1 || flag2) return;
		
		
		/** If both entities have similar similarity values, then one which have similar original and desired classes has priority. Otherwise, both will be deleted.*/
		boolean hasSimilarClassName = classeIsRenamed(matchEntities1.originalClassFullName, 
												      matchEntities1.desiredClassFullName, 
												      AdvancedProblem.getRenameClassCandidates());
		
		if (!hasSimilarClassName) {
			removeList.add(matchEntities1);
			flag1 = true;
		}
		
		hasSimilarClassName = classeIsRenamed(matchEntities2.originalClassFullName, 
				   							  matchEntities2.desiredClassFullName, 
				   							  AdvancedProblem.getRenameClassCandidates());
		
		if (!hasSimilarClassName) {
			removeList.add(matchEntities2);
			flag2 = true;
		}
		
		if (flag1 || flag2) return;
		

		/** If they have different name then remove them.*/
		String originalEntitySimpleName = matchEntities1.originalEntity.getName();
		int index1 = originalEntitySimpleName.indexOf("(");
		String desiredEntitySimpleName = matchEntities1.desiredEntity.getName();
		if (index1 != -1) {
			originalEntitySimpleName = originalEntitySimpleName.substring(0, index1);
			desiredEntitySimpleName = desiredEntitySimpleName.substring(0, desiredEntitySimpleName.indexOf("("));
		}
		if (!originalEntitySimpleName.equals(desiredEntitySimpleName))	removeList.add(matchEntities1);
		
		
		originalEntitySimpleName = matchEntities2.originalEntity.getName();
		index1 = originalEntitySimpleName.indexOf("(");
		desiredEntitySimpleName = matchEntities2.desiredEntity.getName();
		if (index1 != -1) {
			originalEntitySimpleName = originalEntitySimpleName.substring(0, index1);
			desiredEntitySimpleName = desiredEntitySimpleName.substring(0, desiredEntitySimpleName.indexOf("("));
		}
		if (!originalEntitySimpleName.equals(desiredEntitySimpleName))	removeList.add(matchEntities2);
	}
	
/*******************************************************************************************************
 * Description: classeIsRenamed()
 * 				If the first class is renamed to the second one, return true.
********************************************************************************************************/
	public static boolean classeIsRenamed(String classFullName1, String classFullName2, 
								   		  Map<String, String> renameClassCandidates) {
			
		if (classFullName1.equals(classFullName2)) return true;
			
		String renameClass = null;
		try{ renameClass = renameClassCandidates.get(classFullName1);} catch (NullPointerException e) {}
			
		if (renameClass != null && renameClass.equals(classFullName2)) return true;
			
		return false;
	}
	
/*******************************************************************************************************
 * Description: isPartOf_A_HierarchyStructure()
 * 				If child1 and child2 are children of superClass, return true.
*********************************************************************************************************/
	private static boolean isPartOf_A_HierarchyStructure(String superClass, String child1, String child2) {

		Map<String, String> initialClassParentMap = RefactoringProblem.getInitialSourceInformation().getClassParentMap();
		if (Utility.getParent(child1, superClass, initialClassParentMap))
			if (Utility.getParent(child2, superClass, initialClassParentMap))
				return true;


		if (Utility.getParent(child1, superClass, DesiredSourceInformation.classParentMap))
			if (Utility.getParent(child2, superClass, DesiredSourceInformation.classParentMap))
				return true;

		return false;
	}
		
/*******************************************************************************************************
 * Description: sort()
 * 				This function is implemented based on insertion sort, and works based on normalized and 
 * 				then relationship similarity values.
********************************************************************************************************/	
	public static void sort(List<MatchEntities> matchEntityList){

		for (int i = 1; i < matchEntityList.size(); i++) {
			
			MatchEntities current = cloneEntity(matchEntityList.get(i));
			float normalizedValue1 = current.normalizedSimilarityValue;

			int j = i - 1;
			
			//First sort based on normalized similarity value.

			while(j >= 0) {
				
				MatchEntities entity2 = matchEntityList.get(j);

				if(normalizedValue1 <= entity2.normalizedSimilarityValue) break; 
				
				matchEntityList.set(j + 1, entity2);
				j--;
			}

			matchEntityList.set(j + 1, current);

			if (j == -1) continue;

			//Then sort based on relationship similarity value.
			
			current = cloneEntity(matchEntityList.get(j + 1));
			normalizedValue1 = current.normalizedSimilarityValue;
			float relationshipValue1 = current.realRelationshipSimilarityValue;
			
			while(j >= 0) {
			
				MatchEntities entity2 = matchEntityList.get(j);
				float normalizedValue2 = entity2.normalizedSimilarityValue;
				float relationshipValue2 = entity2.realRelationshipSimilarityValue;
			
				if (!(relationshipValue1 > relationshipValue2 && normalizedValue1 == normalizedValue2)) break;

				matchEntityList.set(j + 1, entity2);
				j--;
			}
			
			matchEntityList.set(j + 1, current);
		}
	}
	
/*******************************************************************************************************
 * Description: cloneEntity()
********************************************************************************************************/	
	private static MatchEntities cloneEntity(MatchEntities entity) {
		
		Entity originalEntity = new Entity(entity.originalEntity.getName(), entity.originalEntity.getSignature(), entity.originalEntity.getEntityTypeFullName(), entity.originalEntity.Clone_Modifier());
		Entity desiredEntity = new Entity(entity.desiredEntity.getName(), entity.desiredEntity.getSignature(), entity.desiredEntity.getEntityTypeFullName(), entity.desiredEntity.Clone_Modifier());
		
		return new MatchEntities(entity.originalClassFullName, entity.desiredClassFullName, originalEntity, desiredEntity, 
						         entity.normalizedSimilarityValue, entity.realRelationshipSimilarityValue);
		
	}
	
/*******************************************************************************************************
 * Description: similarCalled_or_CallingEntities()
 * 				In this method we check both entities' names and their class names.
********************************************************************************************************/
	public static boolean similarCalled_or_CallingEntities(String originalEntityName, String originalClassFullName, 
								   						   String desiredEntityName, String desiredClassFullName) {

		char type = 'F';
		int hasSimilarType = -1;
		
		boolean bothEntitiesAreConstructor = false;
		if (originalEntityName.indexOf(")") != -1) { 
			type = 'M';
			bothEntitiesAreConstructor = bothEntitiesAreConstructor(originalEntityName, desiredEntityName, 
																	originalClassFullName, desiredClassFullName); 
		}

		//Rename class should be supported.
		boolean hasSimilarClassName = classeIsRenamed(originalClassFullName, desiredClassFullName, AdvancedProblem.getRenameClassCandidates());
		if (similarNames(originalEntityName, desiredEntityName) || bothEntitiesAreConstructor) {  
			if (hasSimilarClassName) {
				
				hasSimilarType = hasSimilarType(originalEntityName, originalClassFullName, 
						       					desiredEntityName, desiredClassFullName, type);
				
				if (hasSimilarType == 1) return true;
			}
		}

		if (matchedBefore(originalEntityName, originalClassFullName, desiredEntityName, desiredClassFullName, type))
			return true;

		if (CandidateRefactorings.secondRound && DesiredSourceInformation.classOrder.contains(desiredClassFullName)) return false;

		/** If it is first run we allow they have different class names if they are in MoveIn and MoveFrom lists.*/

		if (!(hasSimilarSimpleName(originalEntityName, desiredEntityName) || bothEntitiesAreConstructor) || hasSimilarType == 0) return false;
		
		if (hasSimilarType == -1 && hasSimilarType(originalEntityName, originalClassFullName, desiredEntityName, desiredClassFullName, type) == 0)
			return false;
			
		/** At this stage, we know entities' names are similar and only their classes are different. 
		 *  We check if the entity is moved from the the original and moved in the desired classes, 
		 *  we find entities as similar.*/
		boolean part1 = false, part2 = false;

		try {
			if (type == 'F') {
				for (Entity field : AdvancedProblem.fieldsMoveFromMap.get(originalClassFullName)) {
					if (field.getName().equals(originalEntityName)) {part1 = true; break;}
				}
				for (Entity field : AdvancedProblem.fieldsMoveInMap.get(desiredClassFullName)) {
					if (field.getName().equals(desiredEntityName)) {part2 = true; break;}
				} 
			}

			else if(type == 'M') {  
				
				for (Entity method : AdvancedProblem.methodsMoveFromMap.get(originalClassFullName)) {
					if (method.getName().equals(originalEntityName)) {part1 = true; break;}
				}
				
				for (Entity method : AdvancedProblem.methodsMoveInMap.get(desiredClassFullName)) {
					if (method.getName().equals(desiredEntityName)) {part2 = true; break;}
				}
			}
		}catch (NullPointerException e) {
			//To support null situations.
		}

		if (part1 && part2) return true;
		
		
		if (!(part1 || part2)) {
		
			/** When all classes are not included in the project, Spoon cannot detect the right class for
		    	method and field references. In this case we use simple name instead of class full name.*/
			try{originalClassFullName = originalClassFullName.substring(originalClassFullName.lastIndexOf(".") + 1);} catch (StringIndexOutOfBoundsException e) {}
			try{desiredClassFullName = desiredClassFullName.substring(desiredClassFullName.lastIndexOf(".") + 1);} catch (StringIndexOutOfBoundsException e) {}
		
			if (originalClassFullName.endsWith(desiredClassFullName)) return true;
		}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: hasSimilarSimpleName()
 * 				For methods we ignore their parameters, if they are different.
********************************************************************************************************/
	private static boolean hasSimilarSimpleName(String name1, String name2) {
		
		if (name1.equals(name2)) return true;
		
		int index = name1.indexOf("(");
		try{if (name1.substring(0, index).equals(name2.substring(0, index))) return true;
		} catch(StringIndexOutOfBoundsException e) {}
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: similarNames()
********************************************************************************************************/
	public static boolean similarNames(String MethodName1, String MethodName2){
		
		if (MethodName1.equals(MethodName2)) return true;
		
		int index1 = MethodName1.indexOf("(") + 1;
		int index2 = MethodName2.indexOf("(") + 1;
		
		if (index1 == 0 || index2 == 0) return false;
		
		if (!MethodName1.substring(0, index1).equals(MethodName2.substring(0, index2))) return false;
		
		String[] parameters1 = removeSpecialCharachters(MethodName1.substring(index1, MethodName1.length() - 1)).split(",");
		String[] parameters2 = removeSpecialCharachters(MethodName2.substring(index1, MethodName2.length() - 1)).split(",");

		if (parameters1.length != parameters2.length) return false;
		
		for (int i = 0; i < parameters1.length; i++) {
			
			index1 = parameters1[i].lastIndexOf(".");
			String p1 = parameters1[i];
			if (index1 != -1) p1 = parameters1[i].substring(index1 + 1);
			
			String p2 = parameters2[i];
			index2 = parameters2[i].lastIndexOf(".");
			if (index2 != -1) p2 = parameters2[i].substring(index2 + 1);
			
			//Because of a bug in spoon we need to only check class name.
			if (!p1.equals(p2)) return false;
			
			if (Service.hasSimilarType(parameters1[i], parameters2[i]) == 0) return false;
		}
		
		return true;
	}
	
/*******************************************************************************************************
 * Description: removeSpecialCharachters
 * 				The method removes < and > character and the string between them. 
********************************************************************************************************/
	public static String removeSpecialCharachters(String methodName){
			
		int index1 = methodName.indexOf("<");
		while (index1 != -1) {
			
			int index2 = getProperIndex(methodName, index1);
			methodName = methodName.substring(0, index1) + methodName.substring(index2);
				
			index1 = methodName.indexOf("<");
		}
		
		return methodName;
	}
	
/*******************************************************************************************************
 * Description: getProperIndex()
 * 			    This method returns the index of the ">" character related to "<" in position index.
********************************************************************************************************/
	private static int getProperIndex(String methodName, int index) {
		
		int count = 0; 
		
		int i = index;
		for (; i < methodName.length(); i++) {
		
			char Char = methodName.charAt(i);
			if (Char == '<') count++;
			
			if (Char == '>') {
				if (--count == 0) break;
			}
		}

		return i + 1;
	}
	
/*******************************************************************************************************
 * Description: bothEntitiesAreConstructor()
********************************************************************************************************/
	public static boolean bothEntitiesAreConstructor(String originalEntityName, String desiredEntityName, 
			   								   		 String moveFromClassFullName, String moveInClassFullName) {
		
		if (isConstructor(moveFromClassFullName, originalEntityName) &&
			isConstructor(moveInClassFullName, desiredEntityName))
			
			return true;
		
		return false;
	}

/*******************************************************************************************************
 * Description: hasSimilarType()
 * 				For fields they should have similar types and for method they should have similar return type.
 * 				
 * 				return 1 if they have similar type, and 0 otherwise.
********************************************************************************************************/
	private static int hasSimilarType(String originalEntityName, String originalClassFullName, 
								   	  String desiredEntityName, String desiredClassFullName, char type) {
		
		Map<String, ElementInformation> initialClassElementMap = RefactoringProblem.getInitialSourceInformation().getClassElementsMap();
		Map<String, ElementInformation> desiredClassElementMap = RefactoringProblem.getDesiredSourceInformation().getClassElementsMap();

		ElementInformation initialElements = initialClassElementMap.get(originalClassFullName);
		ElementInformation desiredElements = desiredClassElementMap.get(desiredClassFullName);

		if (initialElements == null && desiredElements == null) return 1;
		if (initialElements == null || desiredElements == null) return 0;
		
		String type1 = null;
		String type2 = null;
		
		//If the input parameters are fields, they must have similar type.
		if (type == 'F') {
		
			//For a case that .class is called
			if (originalEntityName.equals("class") && desiredEntityName.equals("class")) {
				type1 = originalClassFullName;
				type2 = desiredClassFullName;
			}
			
			else {
			
				for (Field field : initialElements.fields) {
			
					if (field.getName().equals(originalEntityName)) {
						type1 = field.getFieldTypeFullName();
						break;
					}
				}

				for (Field field : desiredElements.fields) {
					if (field.getName().equals(desiredEntityName)) {
						type2 = field.getFieldTypeFullName();
						break;
					}
				}
			}
		}
		
		//If the input parameters are methods, they must have similar return type.
		if (type == 'M') {
				
			for (Method method : initialElements.methods) {
				if (method.getName().equals(originalEntityName)) {
					type1 = method.getMethodReturnTypeFullName();
					break;
				}
			}

			for (Method method : desiredElements.methods) {
				if (method.getName().equals(desiredEntityName)) {
					type2 = method.getMethodReturnTypeFullName();
					break;
				}
			}
			
			//For a default constructor which is not defined in classElementMap.
			if (type1 == null && isConstructor(originalClassFullName, originalEntityName)) type1 = originalClassFullName;
			if (type2 == null && isConstructor(desiredClassFullName, desiredEntityName)) type2 = desiredClassFullName;
		}
		
		return hasSimilarType(type1, type2);
	}
	
/*******************************************************************************************************
 * Description: hasSimilarType()
 * 				Note that to cover a case that a simple type is replaced with a collection of that type,			
 * 				I defined the below function. As an example, when (String) is replaced with List<String>
********************************************************************************************************/
	public static int hasSimilarType(String type1, String type2){
		
		if (type1 == null || type2 == null) return 0;
		
		Types extractedTypes = extractTypes(type1, type2);
		
		for (int i = 0; i < 4; i++) {
			
			if (i == 0) {
				type1 = extractedTypes.type1;
				type2 = extractedTypes.type2;
			}
			else if (i == 1){
				type1 = extractedTypes.type3;
				type2 = extractedTypes.type4;
			}
			
			else if (i == 2){
				type1 = extractedTypes.type1;
				type2 = extractedTypes.type4;
			}
			
			else {
				type1 = extractedTypes.type3;
				type2 = extractedTypes.type2;
			}
			
			if (type1 == null && type2 == null) return 0;
			
			if (type1 == null || type2 == null) continue;
			
			if (type1.equals(type2)) return 1;
					
			if (PrimativeWrapper.similar(type1, type2)) return 1;
			
			try{if (AdvancedProblem.getRenameClassCandidates().get(type1).equals(type2)) return 1;}
			catch(NullPointerException e) { }
				
			//If type is changed to one of its superclass or its super interfaces, we assume they are the same.
			try{if (DesiredSourceInformation.classParentMap.get(type1).equals(type2))
				return 1;
			}catch(NullPointerException e) {}
			
			try{
				for (String child : DesiredSourceInformation.getInterfaceClassMap().get(type2)) {
					if (child.equals(type1)) return 1;
			}}catch(NullPointerException e) {}
	
			//Because of a bug in Spoon where it does not return types correctly (When class is not defined in the selected classes).
			
			type1 = getLastParts(type1);
				
			type2 = getLastParts(type2);
			
			if (type1.equals(type2)) return 1;
		}

		return 0;
	}
	
/*******************************************************************************************************
 * Description: getLastParts
 * 				For a case like java.lang.Map<java.lang.Map<Java.lang.String, Java.lang.String>>, the method returns 
 * 				Map<Map<String,String>>
*********************************************************************************************************/
	private static String getLastParts(String type) {
		
		type = type.trim();
		
		String temp = "", result = "";
		
		for (int i = 0; i < type.length(); i++) {
			
			char Char = type.charAt(i);
			temp += Char;
			
			if (Char == '<' ||  Char == '>' || Char == ',') {
				
				result += getLastParts(temp.substring(0, temp.length() - 1)) + Char;
				temp = "";
			}
		}
		
		result += temp.substring(temp.lastIndexOf(".") + 1);
		
		return result;
	}
	
/*******************************************************************************************************
 * Description: extractTypes
*********************************************************************************************************/
	private static Types extractTypes(String type1, String type2) {
		
		Types result = new Types();
		
		int index1 = type1.indexOf("<");
		if (index1 != -1) {
			result.type3 = type1.substring(index1 + 1, type1.length() - 1);
			type1 = type1.substring(0, index1);
		}
		
		int index2 = type2.indexOf("<");
		if (index2 != -1) {
			result.type4 = type2.substring(index2 + 1, type2.length() - 1);
			type2 = type2.substring(0, index2);
		}
		
		result.type1 = type1;
		result.type2 = type2;
		
		return result;
	}
	
/*******************************************************************************************************
 * Description: Types
*********************************************************************************************************/
	private static class Types {
		
		String type1, type2, type3, type4;
		
		Types(){
			type3 = null;
			type4 = null;
		}
	}
	
/*******************************************************************************************************
 * Description: isConstructor()
*********************************************************************************************************/
	public static boolean isConstructor(String classFullName, String entityName) {
		
		String className = classFullName.substring(classFullName.lastIndexOf(".") + 1);
		
		if (entityName.startsWith(className + "(")) 
			return true;
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: matchedBefore()
********************************************************************************************************/
	public static boolean matchedBefore(String originalEntityName, String originalClassFullName, 
								 		String desiredEntityName, String desiredClassFullName, 
								 		char type){
		
		List<MatchEntities> matchedEntities;
		if (type == 'F')
			matchedEntities  = CandidateRefactorings.matchedFields;
		else matchedEntities = CandidateRefactorings.matchedMethods;
		
		if (matchedEntities == null) return false;
		
		for (MatchEntities entity : matchedEntities) {
				
			if (entity.originalEntity.getName().equals(originalEntityName) && 
				entity.originalClassFullName.equals(originalClassFullName) &&
				entity.desiredEntity.getName().equals(desiredEntityName) &&
				entity.desiredClassFullName.equals(desiredClassFullName)) {
					
				return true;
			}
		}
			
		return false;
	}
	
/*******************************************************************************************************
 * Description: fieldIsValid()
********************************************************************************************************/
	public static boolean fieldIsValid(Character entity) {
				
		if (entity.equals('A')) return true;
				
		return false;
	}
	
/*******************************************************************************************************
 * Description: methodIsValid()
********************************************************************************************************/
	public static boolean methodIsValid(Character entity) {
		
		return simulation.fogsaa.Utility.isMethodAbbrevation(entity);
	}
}

/*******************************************************************************************************
* Description: PrimativeWrapper
********************************************************************************************************/
class PrimativeWrapper {
	
	private final static Map<String, String> Primitive2Wrapper = fillMap1();
	private final static Map<String, String> Wrapper2Primitive = fillMap2();
	
	private static Map<String, String> fillMap1() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("char", "java.lang.Character");
		map.put("byte", "java.lang.Byte");
		map.put("short", "java.lang.Short");
		map.put("int", "java.lang.Integer");
		map.put("long", "java.lang.Long");
		map.put("float", "java.lang.Float");
		map.put("double", "java.lang.Double");
		map.put("boolean", "java.lang.Boolean");
		
		map.put("char", "Character");
		map.put("byte", "Byte");
		map.put("short", "Short");
		map.put("int", "Integer");
		map.put("long", "Long");
		map.put("float", "Float");
		map.put("double", "Double");
		map.put("boolean", "Boolean");
		
		return map;
	}
	
	private static Map<String, String> fillMap2() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("java.lang.Character", "char");
		map.put("java.lang.Byte", "byte");
		map.put("java.lang.Short", "short");
		map.put("java.lang.Integer", "int");
		map.put("java.lang.Long", "long");
		map.put("java.lang.Float", "float");
		map.put("java.lang.Double", "double");
		map.put("java.lang.Boolean", "boolean");
		
		map.put("Character", "char");
		map.put("Byte", "byte");
		map.put("Short", "short");
		map.put("Integer", "int");
		map.put("Long", "long");
		map.put("Float", "float");
		map.put("Double", "double");
		map.put("Boolean", "boolean");
		
		return map;
	}
	
	static boolean similar(String type1, String type2) {
		
		try {if (Primitive2Wrapper.get(type1).equals(type2)) return true; } catch(NullPointerException e) {} 
		try {if (Wrapper2Primitive.get(type1).equals(type2)) return true; } catch(NullPointerException e) {} 	     
		
		return false;
	}
}
