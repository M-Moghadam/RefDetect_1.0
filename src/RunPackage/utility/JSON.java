package RunPackage.utility;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import moea.problem.AppliedRefactoringsInformation;

public class JSON {

/***********************************************************************************************************
 * Description : save_DetectedRefactorings_as_JSONFile
*************************************************************************************************************/	 
	public static String save_DetectedRefactorings_as_JSONFile(List<AppliedRefactoringsInformation> AppliedRefactorings,
															    String repository, String currentCommitID, String GITHUB_URL,
																double elapsedTimeInSeconds, double usedMemory, String toolName) {
			
		//Write results as JSON Files
		String JSONResult = JSON.commitJSON(repository, GITHUB_URL, currentCommitID, 
										    elapsedTimeInSeconds, (int) usedMemory, 
										    toolName, AppliedRefactorings);
		return JSONResult;
	}
	
/***********************************************************************************************************
 * Description : save_DetectedRefactorings_as_JSONFile
*************************************************************************************************************/	 
	public static String save_DetectedRefactorings_as_JSONFile(List<AppliedRefactoringsInformation> AppliedRefactorings,
															   String repository, String currentCommitID, String GITHUB_URL,
															   String toolName) {
				
			//Write results as JSON Files
			String JSONResult = JSON.commitJSON(repository, GITHUB_URL, currentCommitID, 
											    toolName, AppliedRefactorings);
			return JSONResult;
		}

/***********************************************************************************************************
 * Description : writeJSONInformationAsFile
*************************************************************************************************************/	 
	public static void writeJSONInformationASFile(String resultFileFullName, String JSONResult) {
			
		try {
	
			Path file = Paths.get(resultFileFullName);
			Files.writeString(file, JSONResult, StandardCharsets.UTF_8);
			
		} catch (IOException e) { e.printStackTrace(); }
	}	

/*******************************************************************************************************
 * Description: commitJSON()
********************************************************************************************************/	
	private static String commitJSON(String repository, String url, String currentCommitId, 
									 double executionTime, int usedMemory, String detectionTool, 
								     List<AppliedRefactoringsInformation> appliedRefactorings) {
		
		StringBuilder sb = new StringBuilder();
			
		sb.append("{").append("\n");
		sb.append("\t").append("\"").append("repository").append("\"").append(": ").append("\"").append(repository).append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("sha1").append("\"").append(": ").append("\"").append(currentCommitId).append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("url").append("\"").append(": ").append("\"").append(url).append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("refactorings").append("\"").append(": ");
		sb.append("[");
		int counter = 0;
		for(AppliedRefactoringsInformation appliedRefactoring : appliedRefactorings) {
			sb.append(toJSON(appliedRefactoring));
			if(counter < appliedRefactorings.size()-1) {
				sb.append(",");
			}
			counter++;
		}
		sb.append("],").append("\n");
		sb.append("\t").append("\"").append("time").append("\"").append(": ").append(executionTime).append(",").append("\n");
		sb.append("\t").append("\"").append("usedMemory").append("\"").append(": ").append(usedMemory).append(",").append("\n");
		sb.append("\t").append("\"").append("detectionTool").append("\"").append(": \"").append(detectionTool).append("\"").append("\n");
		sb.append("}");
		
		return sb.toString();
	}
	

/*******************************************************************************************************
 * Description: commitJSON()
********************************************************************************************************/	
	private static String commitJSON(String repository, String url, String currentCommitId, 
									 String detectionTool, List<AppliedRefactoringsInformation> appliedRefactorings) {
		
		StringBuilder sb = new StringBuilder();
			
		sb.append("{").append("\n");
		sb.append("\t").append("\"").append("repository").append("\"").append(": ").append("\"").append(repository).append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("sha1").append("\"").append(": ").append("\"").append(currentCommitId).append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("url").append("\"").append(": ").append("\"").append(url).append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("refactorings").append("\"").append(": ");
		sb.append("[");
		int counter = 0;
		for(AppliedRefactoringsInformation appliedRefactoring : appliedRefactorings) {
			sb.append(toJSON(appliedRefactoring));
			if(counter < appliedRefactorings.size()-1) {
				sb.append(",");
			}
			counter++;
		}
		sb.append("],").append("\n");
		sb.append("\t").append("\"").append("detectionTool").append("\"").append(": \"").append(detectionTool).append("\"").append("\n");
		sb.append("}");
		
		return sb.toString();
	}

/*******************************************************************************************************
 * Description: toJSON()
********************************************************************************************************/	
	private static String toJSON(AppliedRefactoringsInformation appliedRefactoring) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("{").append("\n");
		sb.append("\t\t").append("\"").append("type").append("\"").append(": ").append("\"").append(appliedRefactoring.refactoringType).append("\"").append(",").append("\n");
		sb.append("\t\t").append("\"").append("original class").append("\"").append(": ").append("\"").append(appliedRefactoring.originalClassFullName).append("\"").append(",").append("\n");
		sb.append("\t\t").append("\"").append("target class").append("\"").append(": ").append("\"").append(appliedRefactoring.targetClassFullName).append("\"").append(",").append("\n");
		sb.append("\t\t").append("\"").append("original entity").append("\"").append(": ").append("\"").append(appliedRefactoring.originalEntityName).append("\"").append(",").append("\n");
		sb.append("\t\t").append("\"").append("target entity").append("\"").append(": ").append("\"").append(appliedRefactoring.targetEntityName).append("\"").append(",").append("\n");
		sb.append("\t\t").append("\"").append("description").append("\"").append(": ").append("\"").append(appliedRefactoring.description).append("\"").append(",").append("\n");
		if(!appliedRefactoring.lang.isEmpty()){
			sb.append("\t\t").append("\"").append("validation").append("\"").append(": ").append("\"").append(appliedRefactoring.validation).append("\"").append(",").append("\n");
			sb.append("\t\t").append("\"").append("lang").append("\"").append(": ").append("\"").append(appliedRefactoring.lang).append("\"").append("\n");
		}else {
			sb.append("\t\t").append("\"").append("validation").append("\"").append(": ").append("\"").append(appliedRefactoring.validation).append("\"").append("\n");
		}

		sb.append("\t}");
		return sb.toString();
	}
}
