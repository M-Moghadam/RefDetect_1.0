package filter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import code2Text_Spoon.Code2Text;
import codeInformation.SourceInformation;
import moea.problem.AppliedRefactoringsInformation;

public class filterExistingResults {

	private static String refactoringType = "MoveAndRenameField";
	private static String sourceFileAddress = "./Test_Input_Repository/Java Projects/";
	
/*******************************************************************************************************
 * Description: main()
********************************************************************************************************/
	public static void main(String[] args) throws IOException {
		
		for (String applicationName : listFolders(sourceFileAddress)) {
			
			List<AppliedRefactoringsInformation> appliedRefactorings = new ArrayList<AppliedRefactoringsInformation>();
			
			String sha1 = readRefactoringInformation(applicationName, appliedRefactorings);
			
			boolean flag = false;
			for (AppliedRefactoringsInformation refactoring : appliedRefactorings) {
				
				if (refactoring.refactoringType.equals(refactoringType)) {
					flag = true;
					break;
				}
			}
			
			if (!flag) continue;	
			
			
			SourceInformation initialSourceInformation = Code2Text.get_Code_as_Text(sourceFileAddress + applicationName + "/prev");
			SourceInformation desiredSourceInformation = Code2Text.get_Code_as_Text(sourceFileAddress + applicationName + "/curr");
		
			List<AppliedRefactoringsInformation> notValidRefactoring = new ArrayList<AppliedRefactoringsInformation>();
			
			for (AppliedRefactoringsInformation refactoringInfo : appliedRefactorings) {
			
				if (!refactoringInfo.refactoringType.equals(refactoringType)) continue;
				
				if (!FilterMoveRenameEntities.isRefactoringValid(refactoringInfo, appliedRefactorings, initialSourceInformation, desiredSourceInformation)) {
				
					notValidRefactoring.add(refactoringInfo);
				}
			}
			
			if (notValidRefactoring.isEmpty()) continue;
			
			System.out.println(applicationName + " " + sha1);
			for (AppliedRefactoringsInformation ref : notValidRefactoring) {
				System.out.println(ref.description);
			}
		}
	}

/*******************************************************************************************************
 * Description: readRefactoringInformation()
********************************************************************************************************/
	private static String readRefactoringInformation(String applicationName, List<AppliedRefactoringsInformation> appliedRefactorings) throws IOException {
		
		String sha1 = "";
		
		String resultAddress = "./Final results_RefDetect_SB/RefDetect_SB/" + applicationName + "_RefDetectSBTool.json";
		
		//If file is not exist no need to continue
		if (!(new File(resultAddress).exists())) return sha1;
		
		 ObjectMapper objectMapper = new ObjectMapper();
		 
		 byte[] jsonData = Files.readAllBytes(Paths.get(resultAddress));
			
		 ArrayNode rootNode = (ArrayNode) objectMapper.readTree(jsonData);
		 Iterator<JsonNode> elements = rootNode.elements();
		 while (elements.hasNext()) {

			 JsonNode jsonNode = elements.next();
			 
			 sha1 = jsonNode.findValue("sha1").asText();
				
			 Iterator<JsonNode> refactoringSet = jsonNode.findValue("refactorings").elements();
			 while(refactoringSet.hasNext()){
				
				 JsonNode refactoring = refactoringSet.next();
				 
				 AppliedRefactoringsInformation appliedRef = new AppliedRefactoringsInformation();
				 appliedRef.refactoringType = refactoring.get("type").asText();
				 appliedRef.originalClassFullName = refactoring.get("original class").asText();
				 appliedRef.targetClassFullName = refactoring.get("target class").asText();
				 appliedRef.originalEntityName = refactoring.get("original entity").asText();
				 appliedRef.targetEntityName = refactoring.get("target entity").asText();
				 appliedRef.validation = refactoring.get("validation").asText();
				 appliedRef.description = refactoring.get("description").asText();
				 
				 appliedRefactorings.add(appliedRef);
			 }
		 }
		 
		 return sha1;
	}
	
/***********************************************************************************************************
 * Description : listFiles
*************************************************************************************************************/	 	
	private static List<String> listFolders(String path) {

		File file = new File(path);
		
		String[] folders = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});

		return Arrays.asList(folders);
	}
}
