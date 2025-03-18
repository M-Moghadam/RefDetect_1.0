package simulation.candidateRefactorings;

/*******************************************************************************************************
 * Description: CandidateExtractInlineMethod
********************************************************************************************************/
	public class CandidateExtractInlineMethod extends CandidateEntity {
				
		/** If Inline method is called, this field has the name of the target method.
		 *  If Extract method is called, this field has the name of the original method.*/
		public String original_or_target_MethodName;
				
		public CandidateExtractInlineMethod(String targetClassFullName, String entityName, String entitySignature, String entityType,
								     		String originalClassFullName, String original_or_target_MethodName) {

			super(originalClassFullName, targetClassFullName, entityName, entitySignature, entityType);

			this.original_or_target_MethodName = original_or_target_MethodName;
		}
	}
