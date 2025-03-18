
# RefDetect: A Multi-Language Refactoring Detection Tool

RefDetect is a refactoring detection tool designed to identify and analyze refactoring operations across multiple programming languages. While the tool is capable of detecting refactorings in Java, C++, Kotlin, and Python, the current version only supports Java and Kotlin.

RefDetect is based on a similarity threshold to detect refactorings. It measures similarity between elements (e.g., methods, fields) using the **FOGSAA algorithm**.

RefDetect detects refactorings using evolutionary algorithms such as Genetic Algorithms (GA). However, the current version does not fully utilize the implemented evolutionary algorithm.

## Features
- Detects refactoring operations between two versions of a project.
- Supports Java and Kotlin.
- Utilizes MOEA Framework for multi-objective optimization (though this feature is not fully implemented in the current version).
- Extracts source information from Java project files using the Spoon framework.
- Extracts source information from Kotlin project files using PSI (Program Structure Interface).

## Prerequisites
Before running the tool, ensure you have the following installed:

- **Java 17** (or a compatible version)
- **Maven** (to manage dependencies)
- Required dependencies (**MOEA Framework**, **Spoon**, etc.). See the **pom.xml** file for details.


## Running the Example
The following Java class demonstrates how to use RefDetect to analyze refactorings between two (Java or Kotlin) project versions:

```java

import java.io.File;
import java.util.List;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import code2Text_Kotlin.Code2TextKotlin;
import moea.problem.AppliedRefactoringsInformation;
import moea.problem.RunRefactoringProblem;
import moea.problem.Utility;
import moea.variable.RefactoringVariable;
import code2Text_Spoon.Code2Text;
import codeInformation.SourceInformation;
import simulation.simulateRefactoring.Refactoring;

public class RefDetectRunner {

    public static void main(String[] args) {
        
        // Paths to the 'before' and 'after' versions of the project
        // These paths can point to directories containing either Java or Kotlin source code.
        // Currently, these paths are set to Java classes as default.
        String projectPathBefore = "./test/Java/before";  // Path to the "before" version of the code
        String projectPathAfter = "./test/Java/after";    // Path to the "after" version of the code
        
        // Default language is set to Java
        // This can be changed by passing a third argument ('Java' or 'Kotlin') when running the program.
        String language = "Java";  // Default language is Java

        // Check if arguments are provided
        if (args.length == 3) {
            projectPathBefore = args[0];  // Set the 'before' version path from the argument
            projectPathAfter = args[1];   // Set the 'after' version path from the argument
            language = args[2];           // Set the language ('Java' or 'Kotlin') from the argument
        } else if (args.length == 0) {
            // No arguments provided, use default values
            System.out.println("No arguments provided. Using default paths: 'before' = " + projectPathBefore + ", 'after' = " + projectPathAfter);
            System.out.println("language: " + language);
        } else {
            // Incorrect number of arguments, show usage and exit
            System.out.println("Usage: java -jar target/RefDetect.jar <path_to_before_version> <path_to_after_version> <language>");
            System.exit(1);
        }

        // Validate that both directories exist
        File beforeDir = new File(projectPathBefore);
        File afterDir = new File(projectPathAfter);

        if (!beforeDir.exists() || !afterDir.exists()) {
            // If any of the specified directories do not exist, print an error and exit
            System.out.println("Error: One or both of the specified directories do not exist.");
            System.exit(1);
        }

        // Extract source information based on the selected language (Java or Kotlin)
        SourceInformation sourceBeforeRefactoring = null;
        SourceInformation sourceAfterRefactoring = null;

        // Based on the 'language' variable, the appropriate method is called to extract source code information
        if (language.equalsIgnoreCase("Java")) {
            // For Java projects, use Code2Text for extracting source information
            System.out.println("Extracting source information for Java (before version)...");
            sourceBeforeRefactoring = Code2Text.get_Code_as_Text(projectPathBefore);
            System.out.println("Extracting source information for Java (after version)...");
            sourceAfterRefactoring = Code2Text.get_Code_as_Text(projectPathAfter);
        } else if (language.equalsIgnoreCase("Kotlin")) {
            // For Kotlin projects, use Code2TextKotlin for extracting source information
            System.out.println("Extracting source information for Kotlin (before version)...");
            sourceBeforeRefactoring = new Code2TextKotlin().getStringRepresentationSourceCode(projectPathBefore);
            System.out.println("Extracting source information for Kotlin (after version)...");
            sourceAfterRefactoring = new Code2TextKotlin().getStringRepresentationSourceCode(projectPathAfter);
        } else {
            // If an invalid language is provided, print an error and exit
            System.out.println("Invalid language specified. Please use 'Java' or 'Kotlin'.");
            System.exit(1);
        }

        // Initialize the refactoring detection problem runner
        RunRefactoringProblem refactoringRunner = new RunRefactoringProblem();

        // Run the refactoring detection
        System.out.println("Running the refactoring detection...");
        NondominatedPopulation result = refactoringRunner.run(sourceBeforeRefactoring, sourceAfterRefactoring);

        // Process and display detected refactorings
        System.out.println("Detected refactorings:");
        Solution solution = result.get(0);  // Get the first solution from the result
        RefactoringVariable variable = (RefactoringVariable) solution.getVariable(0);  // Get the refactorings variable from the solution
        List<Refactoring> ourRefactorings = variable.getRefactorings();  // Extract the list of refactorings

        // Print the descriptions of the detected refactorings
        for (AppliedRefactoringsInformation refactoring : Utility.getRefactringInformation(ourRefactorings)) {
            System.out.println(refactoring.description);
        }
    }
}
```

### Steps to Run
1. Prepare two versions of a project (before and after refactoring) and place them in directories such as `./test/before` and `./test/after`.
2. Compile and execute the `RefDetectRunner` class.
3. The detected refactorings will be displayed in the console output.

### Example Output:
```
Extracting source information for the before version...
Extracting source information for the after version...
Running the refactoring detection...
Detected refactorings:
PullUpMethod: Method setAsynchronous(boolean) in class Activity is pulled up to class FlowNode
InlineMethod: Method verifyReceiver() in class DriverConductor is inlined to method Timeout() in class DriverConductorTest
...
```
