package simulation.candidateRefactorings;

/*******************************************************************************************************
 * Description: CandidateRenameEntity
********************************************************************************************************/
	public class CandidateRenameEntity extends CandidateEntity {
			
		public String entityNewName;
				
		public CandidateRenameEntity(String originalClassFullName, String targetClassFullName, 
							  		 String entityName, String entitySignature, String entityType, String entityNewName) {
			
			super(originalClassFullName, targetClassFullName, entityName, entitySignature, entityType);
			this.entityNewName = entityNewName;
		}
	}