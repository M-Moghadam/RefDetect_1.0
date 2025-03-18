package configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetPropertyThresholds {
	
	public Thresholds thresholds = null;
	
/*******************************************************************************************************
 * Description: GetPropertyThresholds()
********************************************************************************************************/
    public GetPropertyThresholds(String algorithm) {
        
        InputStream inputStream = null;
    	
    	try {
            Properties prop = new Properties();
            String propFileName = "config.thresholds";
            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
           
            String[] splits = prop.getProperty(algorithm).split(",");
            
            String nameSimilarityValue = splits[0].split("=")[1].trim();
            String entitySimilarityThreshold = splits[1].split("=")[1].trim();
            String classSimilarityThreshod = splits[2].split("=")[1].trim();
            
            String extractInlineMethodThreshold = "0";
            try {extractInlineMethodThreshold = splits[3].split("=")[1].trim();} catch (ArrayIndexOutOfBoundsException e) {}
            
            String classConfidentThreshold = classSimilarityThreshod;
            try {classConfidentThreshold = splits[4].split("=")[1].trim();} catch (ArrayIndexOutOfBoundsException e) {}
            
            thresholds = new Thresholds(Float.valueOf(nameSimilarityValue), 
            						    Float.valueOf(entitySimilarityThreshold), 
            						    Float.valueOf(classSimilarityThreshod),
            						    Float.valueOf(extractInlineMethodThreshold),
            						    Float.valueOf(classConfidentThreshold));
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
        	try {
        		inputStream.close();
        	} catch (IOException e) {
        		System.out.println("Exception: " + e);
			}
        }
    }
}