package simulation.candidateRefactorings;

/*******************************************************************************************************
 * Description: CandidateEntity
********************************************************************************************************/
	public class CandidateEntity {
			
		public String originalClassFullName;
		public String targetClassFullName;
		public String entityName;
		public String entitySignature;
		
		//For fields: contains its type, and for methods: contains its return type.
		public String entityTypeFullName;
			
		public CandidateEntity(String originalClassFullName, String targetClassFullName, 
							   String entityName, String entitySignature, String entityType) {
			
			this.originalClassFullName = originalClassFullName;
			this.targetClassFullName = targetClassFullName;
			this.entityName = entityName;
			this.entitySignature = entitySignature;
			this.entityTypeFullName = entityType;
		}
		
		@Override
		public String toString(){
			return entityName + entitySignature + entityTypeFullName + originalClassFullName + targetClassFullName;
		}
	}