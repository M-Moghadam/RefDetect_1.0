package configuration;

public class Thresholds {

	public float nameSimilarityValue;
	public float entitySimilarityThreshold;
	public float classSimilarityThreshod;
	
	public static float extractInlineMethodThreshold;
	
	public static float classConfidentThreshold;
	
	public Thresholds(float nameSimilarityValue, float entitySimilarityThreshold, 
					  float classSimilarityThreshod, float extractInlineMethodThreshold,
					  float classConfidentThreshold) {
	
		this.nameSimilarityValue = nameSimilarityValue; 
		this.entitySimilarityThreshold = entitySimilarityThreshold;
		this.classSimilarityThreshod = classSimilarityThreshod;
		
		Thresholds.extractInlineMethodThreshold = extractInlineMethodThreshold;
		
		Thresholds.classConfidentThreshold = classConfidentThreshold;
	}
}
