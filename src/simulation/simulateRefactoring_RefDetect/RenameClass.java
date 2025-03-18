package simulation.simulateRefactoring_RefDetect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import codeInformation.ElementInformation;
import codeInformation.Field;
import codeInformation.Method;
import codeInformation.SourceInformation;
import codeInformation.Call_Information;
import codeInformation.Call_Information.CallingMethods;
import simulation.simulateRefactoring.precondition.ClassRefactoringPreconditions;

/*******************************************************************************************************
 * Description: RenameClass
********************************************************************************************************/	
public class RenameClass extends simulation.simulateRefactoring.RenameClass {

/*******************************************************************************************************
 * Description: RenameClass()
********************************************************************************************************/	
	public RenameClass(String originalClassFullName, String newClassFullName) {
		super(originalClassFullName, newClassFullName);
	}

/*******************************************************************************************************
 * Description: simulate()
********************************************************************************************************/
	public Set<String> simulate(SourceInformation sourceInformation){
		
		Set<String> changedClasses = new HashSet<String>();
		
		List<String> classOrder = sourceInformation.getClassOrder();
				
		//If the new class is not created before, then call the original Rename Class simulation.
		if(!ClassRefactoringPreconditions.isClassInProgram(newClassFullName, classOrder)) return super.simulate(sourceInformation);
		
		//The original class should be in the program.
		if(!ClassRefactoringPreconditions.isClassInProgram(originalClassFullName, classOrder)) return null;
		
		/** In this line we know a class with a similar name exist in the target program. We continue if the existing class have all fields and 
		 *  methods in the new class. Note the opposite is not checked. In fact, it is not necessary both class have similar fields and methods.*/
		
		if (!existingClassHaveEntitiesOfNewClass(sourceInformation)) return null;
		
		//If it is an interface. We assume that an interface is renamed to an interface
		Map<String, Set<String>> interfaceClassMap = sourceInformation.getInterfaceClassMap();
		try {
			
			sourceInformation.getInterfaces().remove(originalClassFullName);
		
			Set<String> childeren = interfaceClassMap.remove(originalClassFullName);
		
			if (childeren != null) {
			
				Set<String> existingChildren = interfaceClassMap.get(newClassFullName);
				if (existingChildren == null) existingChildren = new HashSet<String>();
			
				existingChildren.addAll(childeren);
				interfaceClassMap.put(newClassFullName, existingChildren);
			}
		}catch(NullPointerException e) {}
			
		//If it inherits from any interface.
		for (Entry<String, Set<String>> entry : interfaceClassMap.entrySet()) {
			
			Set<String> childrenClasses = entry.getValue();
			
			if (childrenClasses.remove(originalClassFullName)) childrenClasses.add(newClassFullName);
		}
		
		
		Map<String, ElementInformation> classElementsMap = sourceInformation.getClassElementsMap();
		ElementInformation elementInformation = classElementsMap.remove(originalClassFullName);
		
		if (elementInformation != null) { 
			
			for (ElementInformation entity : classElementsMap.values()) {

				for (Field field : entity.fields) {

					if (field.getFieldTypeFullName().equals(originalClassFullName)) 
						field.setFieldTypeFullName(newClassFullName);
				}
				
				for (Method method : entity.methods) {

					if (method.getMethodReturnTypeFullName().equals(originalClassFullName)) 
						method.setMethodReturnTypeFullName(newClassFullName);
				}
			}
		}
		else {
			elementInformation = new ElementInformation(new ArrayList<Method>(), new ArrayList<Field>());
		}
		
		//ChildrenClasses
		List<String> childrenClasses = sourceInformation.getChildrenClasses();
		int index = childrenClasses.indexOf(originalClassFullName);
		if (index != -1) childrenClasses.set(index, newClassFullName);
		
		
		//ClassParentMap: It is changed if the previous created class does not inheritance from any class.
		
		boolean isParentChanged = false;
		Map<String, String> classParentMap = sourceInformation.getClassParentMap();
		String parent = classParentMap.remove(originalClassFullName);
		if (parent != null && classParentMap.get(newClassFullName) == null) { classParentMap.put(newClassFullName, parent); isParentChanged = true;}
		
		for (String key : classParentMap.keySet()) {
			if (classParentMap.get(key).equals(originalClassFullName))
				classParentMap.put(key, newClassFullName);
		}
		
		
		//Parent_Children_Map
		Map<String, Set<String>> parentChildrenMap = sourceInformation.getParent_Children_Map();
		Set<String> children = parentChildrenMap.remove(originalClassFullName);
		if (children != null) {
			
			Set<String> previousChildren = parentChildrenMap.get(newClassFullName);
			if (previousChildren == null) previousChildren = new HashSet<String>();
			
			previousChildren.addAll(children);
			
			parentChildrenMap.put(newClassFullName, previousChildren);
		}
		
		
		for (Set<String> childSet : parentChildrenMap.values()) {
			
			if (childSet.remove(originalClassFullName)) {
				
				if (isParentChanged) childSet.add(newClassFullName);
			}
		}
		
		//NestedClassMap -  Note: we do not change inner class on first inner class and so on
		Map<String, Set<String>> nestedClassMap = sourceInformation.getNestedClassMap();
		Set<String> innerClasses = nestedClassMap.remove(originalClassFullName);
		
		if (innerClasses != null) {
			
			Set<String> existingInnerClasses = nestedClassMap.get(newClassFullName);
			if (existingInnerClasses == null) existingInnerClasses = new HashSet<String>();
			
			existingInnerClasses.addAll(innerClasses);
			
			nestedClassMap.put(newClassFullName, existingInnerClasses);
		}

		
		//class order: we do not need to add new class, it is there actually, but we need to delete the original one
		
		int newClassIndex = classOrder.indexOf(newClassFullName);
		int originalClassIndex = classOrder.indexOf(originalClassFullName);
		
		List<String> codeAsString = sourceInformation.getCodeAsString();
		
		List<Set<String>> R_Information = sourceInformation.get_R_Information();
		
		for (String connectedClass : R_Information.get(originalClassIndex)) {
			
			if (R_Information.get(newClassIndex).add(connectedClass)) {
				
				//This means a new connection is added, so we need to add one R in codeAsString for this class as well.
				codeAsString.set(newClassIndex, codeAsString.get(newClassIndex) + "R");
			}
		}
		
		for (int i = 0; i < R_Information.size(); i++) {
			
			Set<String> classSet = R_Information.get(i);
			
			if (classSet.remove(originalClassFullName)) {
				
				if (!classSet.add(newClassFullName)) {
					
					String str = codeAsString.get(i);
					
					//We should delete one R as for newClassFullName is added before, and R for originalClassFullName should be deleted.
					codeAsString.set(i, str.substring(0, str.length() - 1));
				}
			}
		}
		
		//Now remove the original class from the codeAsString and classorder.
		codeAsString.remove(originalClassIndex);
		classOrder.remove(originalClassIndex);
		R_Information.remove(originalClassIndex);
		
		
		//CalledInformation: we expected that classes are the same, so we do not change the call information and keep the previous ones.
		Call_Information calledInformation = sourceInformation.get_CallInformation();
		Map<String, Map<String, Map<String, CallingMethods>>> calledInformationMap = calledInformation.getCalledInformationMap();
		
		Map<String, Map<String, CallingMethods>> classInformationMap = calledInformationMap.remove(originalClassFullName);
		
		try {for (Entry<String, Map<String, CallingMethods>> entry1 : classInformationMap.entrySet()) {
			
			String calledMethod = entry1.getKey();
			
			for (Entry<String, CallingMethods> entry2 : entry1.getValue().entrySet()) {
				
				String callingClass = entry2.getKey();
				
				CallingMethods callingMethods = entry2.getValue();
				
				for (int i = 0; i < callingMethods.nameList.size(); i++) {
					
					calledInformation.setCalledInformationMap(newClassFullName, calledMethod, callingClass, 
															  callingMethods.nameList.get(i), callingMethods.calledByObjectList.get(i));
				}
			}
		}}catch(NullPointerException e) {}
		

		for (Map<String, Map<String, CallingMethods>> map1 : calledInformationMap.values()) {
			
			for (Map<String, CallingMethods> map2 : map1.values()) {
				
				CallingMethods callingClass = map2.remove(originalClassFullName);
				if (callingClass != null) map2.put(newClassFullName, callingClass);
				
				for (CallingMethods callingMethod : map2.values()) {

					for (int i = 0; i < callingMethod.calledByObjectList.size(); i++) {
						
						String type = callingMethod.calledByObjectList.get(i);
						if (type.equals(originalClassFullName))
							callingMethod.calledByObjectList.set(i, newClassFullName);
					}
				}
			}
		}
		
		changedClasses.add(newClassFullName);
		changedClasses.add(originalClassFullName);
	
		return changedClasses;
	}
	
/*******************************************************************************************************
 * Description: existingClassHaveEntitiesOfNewClass()
********************************************************************************************************/
	private boolean existingClassHaveEntitiesOfNewClass(SourceInformation sourceInformation){
		
		Map<String, ElementInformation> classElementsMap = sourceInformation.getClassElementsMap();
		
		ElementInformation originalClassElementsInformation = classElementsMap.get(originalClassFullName);
		
		ElementInformation existingClassElementsInformation = classElementsMap.get(newClassFullName);
		
		if (originalClassElementsInformation == null) return true;
		
		if (existingClassElementsInformation == null) return false;
		
		Set<String> names = new HashSet<String>();
		
		try {for (Field existingClassField : existingClassElementsInformation.fields) names.add(existingClassField.getName());}catch(NullPointerException e) {}
			
		try {for (Field originalClassField : originalClassElementsInformation.fields) {if(!names.contains(originalClassField.getName())) return false;}}catch (NullPointerException e) {}
		
		names.clear();
		
		try {for (Method existingClassMethod : existingClassElementsInformation.methods) names.add(existingClassMethod.getName());}catch(NullPointerException e) {}
		
		try {for (Method originalClassMethod : originalClassElementsInformation.methods) {if(!names.contains(originalClassMethod.getName())) return false;}}catch(NullPointerException e) {}
		
		return true;
	}
	
/*******************************************************************************************************
 * Description: getOriginalClassFullName()
********************************************************************************************************/
	public String getOriginalClassFullName(){
		return originalClassFullName;
	}
	
/*******************************************************************************************************
 * Description: getRefactoringType()
********************************************************************************************************/
	@Override
	public String getRefactoringType() {
		return "RenameClass";
	}
}