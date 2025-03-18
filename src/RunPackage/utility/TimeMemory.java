package RunPackage.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

public class TimeMemory {
	
	private static Runtime runtime = Runtime.getRuntime();

	static long usedMemoryBefore = 0;
	static long time1 = 0;
			
	static long usedMemoryAfter = 0;
	static long time2 = 0;
			
	public static double elapsedTimeInSeconds;
	public static long usedMemory;
		
/***********************************************************************************************************
 * Description : bytesToMegabytes
*************************************************************************************************************/	 
	static long bytesToMegabytes(long bytes) {
		return (bytes / (1024L * 1024L));
	}
	
/***********************************************************************************************************
 * Description : measure_Initial_Time_And_Memory
*************************************************************************************************************/
	public static void measure_Initial_Time_And_Memory() {
		
		System.gc();
		TimeMemory.usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		TimeMemory.time1 = System.nanoTime();
	}
	
/***********************************************************************************************************
 * Description : measure_Final_Time_And_Memory
*************************************************************************************************************/
	public static void measure_Final_Time_And_Memory() {
		
		TimeMemory.time2 = System.nanoTime();
		TimeMemory.usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
		
		TimeMemory.usedMemory = bytesToMegabytes(TimeMemory.usedMemoryAfter - TimeMemory.usedMemoryBefore);
		
        TimeMemory.elapsedTimeInSeconds = TimeUnit.MILLISECONDS.convert(TimeMemory.time2 - TimeMemory.time1, TimeUnit.NANOSECONDS) / 1000.0;
        
        //We need its time in 3 decimal place
        BigDecimal bd = new BigDecimal(TimeMemory.elapsedTimeInSeconds).setScale(3, RoundingMode.HALF_DOWN);
        TimeMemory.elapsedTimeInSeconds = bd.doubleValue();
	}
}