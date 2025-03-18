package RunPackage.utility;

import java.util.List;

import codeInformation.SourceInformation;
import simulation.simulateRefactoring.Refactoring;

/***********************************************************************************************************
 * Description : RefDetectResults
*************************************************************************************************************/	 
	public class RefDetectResults {
		
		public SourceInformation initialSourceInformation = null;
		public SourceInformation resultedSourceInformation = null;
		public List<Refactoring> detectedRefactorings = null;
		
		public RefDetectResults(SourceInformation initialSourceInformation, 
				                SourceInformation resultedSourceInformation, 
				                List<Refactoring> detectedRefactorings){
			
			this.initialSourceInformation = initialSourceInformation;
			this.resultedSourceInformation = resultedSourceInformation;
			this.detectedRefactorings = detectedRefactorings;
		}
	}
