package moea.variable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moeaframework.core.PRNG;

import codeInformation.DesiredSourceInformation;
import codeInformation.ElementInformation;
import codeInformation.Field;
import codeInformation.Method;
import codeInformation.SourceInformation;
import simulation.simulateRefactoring.ClassRefactoring;
import simulation.simulateRefactoring.DeleteClass;
import simulation.simulateRefactoring.DeleteField;
import simulation.simulateRefactoring.DeleteMethod;
import simulation.simulateRefactoring.ExtractClass;
import simulation.simulateRefactoring.ExtractInterface;
import simulation.simulateRefactoring.ExtractSubClass;
import simulation.simulateRefactoring_RefDetect.ExtractSuperClass;
import simulation.simulateRefactoring_RefDetect.RenameClass;
import simulation.simulateRefactoring.MoveField;
import simulation.simulateRefactoring.MoveMethod;
import simulation.simulateRefactoring.PullUpField;
import simulation.simulateRefactoring.PullUpMethod;
import simulation.simulateRefactoring.PushDownField;
import simulation.simulateRefactoring.PushDownMethod;
import simulation.simulateRefactoring.Refactoring;
import simulation.simulateRefactoring.RenameField;
import simulation.simulateRefactoring.RenameMethod;
import simulation.simulateRefactoring.SimulateRefactorings;
import simulation.simulateRefactoring.Utility;
import simulation.simulateRefactoring.precondition.FieldRefactoringPreconditions;
import simulation.simulateRefactoring.precondition.MethodRefactoringPreconditions;
import simulation.simulateRefactoring.precondition.Precondition;

public class BasicCreateChromosome extends CreateChromosome implements CreateRefactorings {

/*******************************************************************************************************
 * Description: BasicCreateChromosome()
********************************************************************************************************/
	public BasicCreateChromosome(SourceInformation sourceInformation) {
		super(sourceInformation);
	}
	
/*******************************************************************************************************
 * Description: createRefactoringList()
********************************************************************************************************/
	public List<Refactoring> createRefactoringList(int sizeofRefactoringList, Set<String> changedClasses) {

		List<Refactoring> refactoringList = new ArrayList<Refactoring>();

		List<String> candidateRefactoringTypes = initializeList();
		
		for (int i = 0; i < sizeofRefactoringList && (!candidateRefactoringTypes.isEmpty()); i++) {

			Refactoring candidateRefactoring = createRefactoring(refactoringList, candidateRefactoringTypes);

			if (candidateRefactoring == null) continue;

			// Simulate refactoring
			SimulateRefactorings simulateRefactorings = new SimulateRefactorings(sourceInformation);
			Set<String> changedCls = simulateRefactorings.simulateRefactoring(candidateRefactoring);
			if (changedCls != null) {
				refactoringList.add(candidateRefactoring);
				changedClasses.addAll(changedCls);
			}
		}

		return refactoringList;
	}
	
/*******************************************************************************************************
 * Description: isValidRefactoring()
********************************************************************************************************/
	@Override
	public boolean isValidRefactoring(String refactoringType, List<String> candidateRefactoringTypes, 
									   Refactoring newRefactoringInformation,
									   List<Refactoring> appliedRefactorings) {
		
		//This function is not implemented completely. Need to be compare with the similar method in SearchBasedCreateChromosome.
		
		//The algorithm cannot create any refactorings of this type, so we can remove that.
		if (newRefactoringInformation == null) {
			candidateRefactoringTypes.remove(refactoringType);
			return false;
		}
		
		//If it is created before, add it to the duplicate list
		if (isDuplicate(newRefactoringInformation, appliedRefactorings)) return false;
		
		return true;
	}
	
/*******************************************************************************************************
 * Description: isDuplicate
 * 				If a similar refactoring is created before no need to create a duplicate one.
 * 
 * 				Maybe this decrease the speed of the program, in this case we need to remove or improve this function.
 * 
 * 			    Note that when a field for example "foo" and a method "foo()" want to be moved from class "A" 
 * 			    to class "B", this method detects the second one as duplicate. However, a case like this where 
 * 			    a field has a similar name with a method with no parameter is less, so I can ignore that.
********************************************************************************************************/
	private boolean isDuplicate(Refactoring newrefactoring, List<Refactoring> refactoringList){

		//This function is not responsible for null, so return false;
		if (newrefactoring == null) return false;
		
		String newRefactoringInformation = newrefactoring.refactoringToString();

		if (newrefactoring instanceof ClassRefactoring) 
			newRefactoringInformation = ((ClassRefactoring)newrefactoring).getNewClassFullName();
		
		for (Refactoring existRefactoring : refactoringList) {

			String existRefactoringInformation = existRefactoring.refactoringToString();
			
			if (existRefactoring instanceof ClassRefactoring) 
				existRefactoringInformation = ((ClassRefactoring)existRefactoring).getNewClassFullName();
			
			if (newRefactoringInformation.equals(existRefactoringInformation)) 
				return true;
		}

		return false;
	}
	
/*******************************************************************************************************
 * Description: createChangeMethodParameterRefactoring()
********************************************************************************************************/
	public Refactoring createChangeMethodParameterRefactoring() {

		//This function is not implement, and should be written later.
				
		return null;
	}
	
/*******************************************************************************************************
 * Description: createInlineMethodRefactoring()
********************************************************************************************************/
	public Refactoring createInlineMethodRefactoring() {

		//This function is not implement, and should be written later.
			
		return null;
	}
	
/*******************************************************************************************************
 * Description: createExtractMethodRefactoring()
********************************************************************************************************/
	public Refactoring createExtractMethodRefactoring() {

		//This function is not implement, and should be written later.
		
		return null;
	}
	
/*******************************************************************************************************
 * Description: createRenameClassRefactoring()
********************************************************************************************************/
	public Refactoring createRenameClassRefactoring() {
		
		//This function is not implement completely, and should be written later.
		
		String originalClassFullName = PRNG.nextItem(classOredr);
		
		String newClassFullName = PRNG.nextItem(DesiredSourceInformation.classOrder);
		
		return new RenameClass(originalClassFullName, newClassFullName);
	}

/*******************************************************************************************************
 * Description: createRenameFieldRefactoring()
********************************************************************************************************/
	public Refactoring createRenameFieldRefactoring() {
		
		//This function is not implement completely and should be written later.

		//Note that this function should cover a case that a field is renamed and moved.
		
		String originalClassFullName = PRNG.nextItem(classOredr);

		List<Field> fields1 = getFields(originalClassFullName, classElementMap);
		if(fields1 == null) return null;
		
		String desiredClassFullName = PRNG.nextItem(DesiredSourceInformation.classOrder);
		List<Field> fields2 = DesiredSourceInformation.classElementsMap.get(desiredClassFullName).fields;
		if(fields2.isEmpty()) return null;
				
		Field field1 = PRNG.nextItem(fields1);
		Field field2 = PRNG.nextItem(fields2);
		
		return new RenameField(originalClassFullName, field1.getName(), field2.getName());
	}

/*******************************************************************************************************
 * Description: createRenameMethodRefactoring()
********************************************************************************************************/
	public Refactoring createRenameMethodRefactoring() {

		//This function is not implement completely and should be written later.

		//Note that this function should cover a case that a method is renamed and moved.
		
		String originalClassFullName = PRNG.nextItem(classOredr);

		List<Method> methods1 = getMethods(originalClassFullName, classElementMap);
		if (methods1 == null) return null;
		
		String desiredClassFullName = PRNG.nextItem(DesiredSourceInformation.classOrder);
		List<Method> methods2 = DesiredSourceInformation.classElementsMap.get(desiredClassFullName).methods;
		if(methods2.isEmpty()) return null;
				
		Method method1 = PRNG.nextItem(methods1);
		Method method2 = PRNG.nextItem(methods2);
		
		return new RenameMethod(originalClassFullName, method1.getName(), method1.getSignature(), method1.getEntityTypeFullName(), method2.getName(), desiredClassFullName);
	}
	
/*******************************************************************************************************
* Description: createDeleteClassRefactoring()
********************************************************************************************************/
	public Refactoring createDeleteClassRefactoring() {

		String originalClassFullName = PRNG.nextItem(classOredr);

		return new DeleteClass(originalClassFullName);
	}
	
/*******************************************************************************************************
 * Description: createDeleteMethodRefactoring()
********************************************************************************************************/
	public Refactoring createDeleteMethodRefactoring() {

		String originalClassFullName = PRNG.nextItem(classOredr);

		List<Method> methods = getMethods(originalClassFullName, classElementMap);
		if (methods == null) return null;

		Method method = PRNG.nextItem(methods);
		
		return new DeleteMethod(originalClassFullName, method.getName(), method.getSignature(), method.getMethodReturnTypeFullName());
	}
	
/*******************************************************************************************************
 * Description: createDeleteFieldRefactoring()
********************************************************************************************************/
	public Refactoring createDeleteFieldRefactoring() {

		String originalClassFullName = PRNG.nextItem(classOredr);

		List<Field> fields = getFields(originalClassFullName, classElementMap);
		if(fields == null) return null;

		Field field = PRNG.nextItem(fields);
		
		return new DeleteField(originalClassFullName, field.getName());
	}
	
/*******************************************************************************************************
 * Description: createExtractSuperClassRefactoring()
********************************************************************************************************/
	public Refactoring createExtractSuperClassRefactoring() {

		String originalClassFullName = PRNG.nextItem(classOredr);

		//We allow Extract SuperClass decides about name of new class.
		return new ExtractSuperClass(originalClassFullName, sourceInformation);
	}
	
/*******************************************************************************************************
 * Description: createExtractSubClassRefactoring()
********************************************************************************************************/
	public Refactoring createExtractSubClassRefactoring() {

		String originalClassFullName = PRNG.nextItem(classOredr);

		//We allow Extract SubClass decides about name of new class.
		return new ExtractSubClass(originalClassFullName, sourceInformation);
	}
		
/*******************************************************************************************************
 * Description: createExtractClassRefactoring()
********************************************************************************************************/
	public Refactoring createExtractClassRefactoring() {

		String originalClassFullName = PRNG.nextItem(classOredr);

		//We allow Extract Class decides about name of new class.
		return new ExtractClass(originalClassFullName, sourceInformation);
	}
	
/*******************************************************************************************************
 * Description: createExtractInterfaceRefactoring()
********************************************************************************************************/
	public Refactoring createExtractInterfaceRefactoring() {
			
		String originalClassFullName = PRNG.nextItem(classOredr);

		//We allow Extract Interface decides about name of new class.
		return new ExtractInterface(originalClassFullName, sourceInformation);
	}
	
/*******************************************************************************************************
 * Description: createPullUpFieldRefactoring()
********************************************************************************************************/
	public Refactoring createPullUpFieldRefactoring() {

		if (childrenClasses.isEmpty()) return null;

		String originalClassFullName = PRNG.nextItem(childrenClasses);

		List<Field> fields = getFields(originalClassFullName, classElementMap);
		if(fields == null) return null;

		Field field = PRNG.nextItem(fields);
		
		Map<String, String> classParentMap = sourceInformation.getClassParentMap();
		String parentClassFullName = classParentMap.get(originalClassFullName);
		
		if (parentClassFullName == null) return null;

		return new PullUpField(originalClassFullName, field.getName(), parentClassFullName);
	}

/*******************************************************************************************************
 * Description: createPullUpMethodRefactoring()
********************************************************************************************************/
	public Refactoring createPullUpMethodRefactoring() {

		if (childrenClasses.isEmpty()) return null;

		String originalClassFullName = PRNG.nextItem(childrenClasses);

		List<Method> methods = getMethods(originalClassFullName, classElementMap);
		if (methods == null) return null;

		Method method = PRNG.nextItem(methods);
		
		Map<String, String> classParentMap = sourceInformation.getClassParentMap();
		String parentClassFullName = classParentMap.get(originalClassFullName);
		
		if (parentClassFullName == null) return null;

		return new PullUpMethod(originalClassFullName, method.getName(), method.getSignature(), method.getMethodReturnTypeFullName(), parentClassFullName);
	}

/*******************************************************************************************************
 * Description: createPushDownFieldRefactoring()
********************************************************************************************************/
	public Refactoring createPushDownFieldRefactoring() {

		if (childrenClasses.isEmpty()) return null;
		
		String childClassFullName = PRNG.nextItem(childrenClasses);

		String parentClassFullName = classParentMap.get(childClassFullName);
		
		List<Field> fields = getFields(parentClassFullName, classElementMap);
		if(fields == null) return null;
		
		Field field = PRNG.nextItem(fields);

		//We only move field to classes which are used the field.
		Set<String> targetDirectChildren = getDirectChildrenWhereEntityISUsed(parentClassFullName, field.getName());
		
		if (targetDirectChildren.isEmpty()) return null;
		
		PushDownField pushDownField = new PushDownField(parentClassFullName, field.getName(), new ArrayList<String>(targetDirectChildren));

		return pushDownField;
	}
	
/*******************************************************************************************************
 * Description: getDirectChildrenWhereEntityISUsed()
 * 				This method return children classes where entity (field or method) is used through them.
 * 				Note that if the entity is used by the original class, then this method return an empty set.
********************************************************************************************************/
	private Set<String> getDirectChildrenWhereEntityISUsed(String originalClassFullName, String entityName){

		Set<String> targetDirectChildren = new HashSet<String>();

		Set<String> directChildren = Utility.getChildrenByParent(sourceInformation, originalClassFullName);

		Set<String> targetCandidateClasses = Precondition.classesThatEntityCalledThroughThem(sourceInformation, originalClassFullName, entityName);

		//If original class is used where the entity is used through it, the the refactoring will be rejected.
		if (targetCandidateClasses.contains(originalClassFullName)) return targetDirectChildren;

		for (String cls : targetCandidateClasses) {
			targetDirectChildren.add(Utility.getParent(cls, new ArrayList<String>(directChildren), classParentMap));
		}

		return targetDirectChildren;
	}
			
/*******************************************************************************************************
 * Description: createPushDownMethodRefactoring()
********************************************************************************************************/
	public Refactoring createPushDownMethodRefactoring() {

		if (childrenClasses.isEmpty()) return null;
		
		String childClassFullName = PRNG.nextItem(childrenClasses);

		String parentClassFullName = classParentMap.get(childClassFullName);
		
		List<Method> methods = getMethods(parentClassFullName, classElementMap);
		if (methods == null) return null;
		
		Method method = PRNG.nextItem(methods);

		//We only move method to classes which are used the field.
		Set<String> targetDirectChildren = getDirectChildrenWhereEntityISUsed(parentClassFullName, method.getName());

		if (targetDirectChildren.isEmpty()) return null;
		
		PushDownMethod pushDownMethod = new PushDownMethod(parentClassFullName, method.getName(), method.getSignature(), method.getMethodReturnTypeFullName(), new ArrayList<String>(targetDirectChildren));

		return pushDownMethod;
	}

/*******************************************************************************************************
 * Description: createMoveFieldRefactoring()
 * 				If classes are related through inheritance, then we create Move Field and during 
 * 				Move Field simulation, Pull Up or Push Down field refactoring is called.
 * 				If classes are not related through inheritance, then classes should be related through 
 * 				a field. Otherwise,	the refactoring is rejected. 
********************************************************************************************************/
	public Refactoring createMoveFieldRefactoring() {
		
		String originalClassFullName = PRNG.nextItem(classOredr);
		
		String targetClassFullName = PRNG.nextItem(classOredr);
		
		if (originalClassFullName.equals(targetClassFullName)) return null;
		
		List<Field> fields = getFields(originalClassFullName, classElementMap);
		if(fields == null) return null;

		Field field = PRNG.nextItem(fields);
		
		if (!relatedThroughInheritance(originalClassFullName, targetClassFullName)) {
		
			/** We only move fields to a related class. We only consider related through FieldDeclaration.*/
			
			FieldRefactoringPreconditions precondition = new FieldRefactoringPreconditions(sourceInformation);
			if (precondition.isRelatedThroughFieldDeclaration(sourceInformation, originalClassFullName, targetClassFullName, field.getName()) == null)
				return null;
		}

		return new MoveField(originalClassFullName, field.getName(), targetClassFullName);
	}

/*******************************************************************************************************
 * Description: createMoveMethodRefactoring()
 * 				If classes are related through inheritance, then we create Move Method and during 
 * 				Move Method simulation, Pull Up or Push Down Method refactoring is called.
 * 				If classes are not related through inheritance, then classes should be related through 
 * 				a field or method parameter. Otherwise,	the refactoring is rejected. 
********************************************************************************************************/
	public Refactoring createMoveMethodRefactoring() {
		
		String originalClassFullName = PRNG.nextItem(classOredr);
		
		String targetClassFullName = PRNG.nextItem(classOredr);
		
		if (originalClassFullName.equals(targetClassFullName)) return null;
		
		List<Method> methods = getMethods(originalClassFullName, classElementMap);
		if (methods == null) return null;

		Method method = PRNG.nextItem(methods);
		
		if (!relatedThroughInheritance(originalClassFullName, targetClassFullName)) {
		
			/** We only move methods to a related class. In this version we only consider related through FieldDeclaration. 
			Later we need to include Method parameter as well. See class "RelatedClasses" and "MoveMethod" for more details.*/
			
			MethodRefactoringPreconditions precondition = new MethodRefactoringPreconditions(sourceInformation);
			if (precondition.isRelatedThroughFieldDeclaration(sourceInformation, originalClassFullName, targetClassFullName, null) == null)
				return null;
		}

		return new MoveMethod(originalClassFullName, method.getName(), method.getSignature(), method.getMethodReturnTypeFullName(), targetClassFullName);
	}
	
/*******************************************************************************************************
 * Description: relatedThroughInheritance()
********************************************************************************************************/
	private boolean relatedThroughInheritance(String originalClassFullName, String targetClassFullName) {
		
		if (Utility.getParent(originalClassFullName, targetClassFullName, classParentMap)) return true;
		
		if (Utility.getParent(targetClassFullName, originalClassFullName, classParentMap)) return true;
		
		return false;
	}
	
/*******************************************************************************************************
 * Description: getMethods()
********************************************************************************************************/
	private List<Method> getMethods(String classFullName, Map<String, ElementInformation> classElementMap){

		List<Method> methods = null;
		try{
			methods = classElementMap.get(classFullName).methods;
		}catch(NullPointerException e){ return null; }

		if(methods.isEmpty()) return null;

		return methods;
	}

/*******************************************************************************************************
 * Description: getFields()
********************************************************************************************************/
	private List<Field> getFields(String classFullName, Map<String, ElementInformation> classElementMap){

		List<Field> fields = null;
		try{
			fields = classElementMap.get(classFullName).fields;
		}catch(NullPointerException e){ return null; }

		if(fields.isEmpty()) return null;

		return fields;
	}
}