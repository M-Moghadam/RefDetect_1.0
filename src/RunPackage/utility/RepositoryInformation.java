package RunPackage.utility;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/***********************************************************************************************************
* Description : RepositoryInformation
*************************************************************************************************************/
	public class RepositoryInformation {
		
		public static String repository, currentCommitID, GITHUB_URL;
		
/***********************************************************************************************************
* Description : getCommitInformation
*************************************************************************************************************/
	public static void getCommitInformation(String projectName) {

		int index = projectName.lastIndexOf("-");
		currentCommitID = projectName.substring(index + 1);
		projectName = projectName.substring(0, index) + ".git";

		String JSONFileAddress = "./Data/RMinerdata.json";

		//JSON parser object to parse read file
		JSONParser jsonParser = new JSONParser();

		try {
			
			for (Object object : (JSONArray) jsonParser.parse(new FileReader(JSONFileAddress))) {

				JSONObject json = (JSONObject) object;
				String jsonRepository = (String) json.get("repository");

				if (jsonRepository.endsWith("/" + projectName)) {

					String jsonsha1 = (String) json.get("sha1");

					if (jsonsha1.startsWith(currentCommitID)) {
						repository = jsonRepository;
						currentCommitID = jsonsha1;
						GITHUB_URL = (String) json.get("url");
						break;
					}
				}
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}