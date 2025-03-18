package gitHandler;

import RunPackage.utility.JSON;
import RunPackage.utility.TimeMemory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import moea.problem.AppliedRefactoringsInformation;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class RefDetectRunner {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please, specify the arguments\n");
            printUsage();
            return;
        }

        System.out.println("Welcome to RefDetect V1.01");
        System.out.println("Use '-h' to get help, if needed.");

        final String option = args[0];
        if (option.equalsIgnoreCase("-h") || option.equalsIgnoreCase("--h") ||
                option.equalsIgnoreCase("-help") || option.equalsIgnoreCase("--help")) {
            printUsage();
            return;
        }

        if (option.equalsIgnoreCase("-all")) {
            detectAll(args);
        } else if (option.equalsIgnoreCase("-c")) {
            detectAtCommit(args);
        } else if (option.equalsIgnoreCase("-bc")) {
            detectBetweenCommits(args);
        } else if (option.equalsIgnoreCase("-list")) {
            detectFromJsonList(args);
        } else if (option.equalsIgnoreCase("-allrepos")) {
            detectAllReposFromJsonList(args);
        } else {
            System.out.println("Incorrect command. Please, use '-h' option for help.\n");
        }
    }

    private static void detectAllReposFromJsonList(String[] args) throws Exception {
        String folderOfAllRepos = args[1];
        String jsonFilePath = args[2];
        String[] parameterToPass = new String[3];
        parameterToPass[0] = "-c";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
            Iterator<JsonNode> iterator = rootNode.iterator();
            while (iterator.hasNext()) {
                JsonNode jsonNode = iterator.next();

                String sha1 = jsonNode.get("sha1").asText();
                String repositoryUrl = jsonNode.get("repository").asText();
                String repositoryName = repositoryUrl.substring(repositoryUrl.lastIndexOf(File.separatorChar) + 1);
                parameterToPass[1] = folderOfAllRepos + File.separatorChar + repositoryName;
                parameterToPass[2] = sha1;
                detectAtCommit(parameterToPass);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void detectFromJsonList(String[] args) throws Exception {
        String folder = args[1];
        String jsonFilePath = args[2];
        String[] parameterToPass = new String[3];
        parameterToPass[0] = "-c";
        parameterToPass[1] = folder;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
            Iterator<JsonNode> iterator = rootNode.iterator();
            while (iterator.hasNext()) {
                JsonNode jsonNode = iterator.next();

                String sha1 = jsonNode.get("sha1").asText();
                parameterToPass[2] = sha1;
                detectAtCommit(parameterToPass);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Detects refactorings at the specific commit.
     */

    public static void makeFolder(String path){
        File folder = new File(path);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + folder);
            }
        }
    }
    private static void detectAtCommit(String[] args) throws Exception {
        String folder = args[1];
        String projectName = folder;
        String commitId = args[2];
        GitService gitService = new GitService();
        GitInfo retrievedInfo = null;
        TimeMemory.measure_Initial_Time_And_Memory();
        try (Repository repo = gitService.openRepository(folder)) {
            String gitURL = repo.getConfig().getString("remote", "origin", "url");
            GitHistoryRefDetect detector = new GitHistoryRefDetect();
            retrievedInfo = detector.detectAtCommit(repo, commitId, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<AppliedRefactoringsInformation> refactorings, boolean ktFilesChanged) {
//                    JsonUtil.commitJSON(sb, gitURL, commitId, refactorings, ktFilesChanged);
                }

                @Override
                public void handleException(String commit, Exception e) {
                    System.err.println("Error processing commit " + commit);
                    e.printStackTrace(System.err);
                }
            });
            TimeMemory.measure_Final_Time_And_Memory();

            if (retrievedInfo != null) {
                for (AppliedRefactoringsInformation refactoring : retrievedInfo.appliedRefactorings) {
                    System.out.println(refactoring.description);
                }

                String JSONResult = "[" + JSON.save_DetectedRefactorings_as_JSONFile(retrievedInfo.appliedRefactorings,
                        retrievedInfo.repositoryUrl, retrievedInfo.currentCommitID, retrievedInfo.commitUrl,
                        TimeMemory.elapsedTimeInSeconds, TimeMemory.usedMemory, "RefDetect 1.0") + ",";

                String repoName = projectName.substring(projectName.lastIndexOf(File.separatorChar) + 1);
                makeFolder(projectName +"_Json");
                String resultFileFullName = projectName +"_Json" + File.separatorChar + repoName + "-" + retrievedInfo.currentCommitID.substring(0, 8) + ".json";
                JSON.writeJSONInformationASFile(resultFileFullName, JSONResult.replaceAll(",$", "]"));
            }
        }
    }

    /**
     * Detects refactorings in all commits in the specified branch.
     */
    private static void detectAll(String[] args) throws Exception {
        if (args.length > 3) {
            System.out.println("Incorrect arguments. Please, use '-h' option for help.\n");
        }
        String folder = args[1];
        String projectName = folder;
        String branch = null;
        if (args.length == 3) {
            branch = args[2];
            List<AppliedRefactoringsInformation> RefDetect_AppliedRefactorings = new ArrayList<AppliedRefactoringsInformation>();
            GitService gitService = new GitService();
            List<GitInfo> allOfRetrievedInfo = new ArrayList<>();
            TimeMemory.measure_Initial_Time_And_Memory();
            try (Repository repo = gitService.openRepository(folder)) {
                String gitURL = repo.getConfig().getString("remote", "origin", "url");
                GitHistoryRefDetect detector = new GitHistoryRefDetect();
                allOfRetrievedInfo = detector.detectAll(repo, branch, new RefactoringHandler() {
                    private int commitCount = 0;

                    @Override
                    public void handle(String commitId, List<AppliedRefactoringsInformation> refactorings, boolean ktFilesChanged) {
//                    if (commitCount > 0) {
//                        sb.append(",").append("\n");
//                    }
//                    JsonUtil.commitJSON(sb, gitURL, commitId, refactorings, ktFilesChanged);
                        commitCount++;
                    }

                    @Override
                    public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
                        System.out.printf("Total count: [Commits: %d, Errors: %d, Refactorings: %d]%n",
                                commitsCount, errorCommitsCount, refactoringsCount);
                    }

                    @Override
                    public void handleException(String commit, Exception e) {
                        System.err.println("Error processing commit " + commit);
                        e.printStackTrace(System.err);
                    }
                });
                TimeMemory.measure_Final_Time_And_Memory();

                if (allOfRetrievedInfo != null) {

                    for (GitInfo infos : allOfRetrievedInfo) {
                        for (AppliedRefactoringsInformation refactoring : infos.appliedRefactorings) {
                            System.out.println(refactoring.description);
                        }
                    }

                    for (GitInfo retrievedInfo : allOfRetrievedInfo) {
                        String JSONResult = "[" + JSON.save_DetectedRefactorings_as_JSONFile(retrievedInfo.appliedRefactorings,
                                retrievedInfo.repositoryUrl, retrievedInfo.currentCommitID, retrievedInfo.commitUrl,
                                TimeMemory.elapsedTimeInSeconds, TimeMemory.usedMemory, "RefDetect 1.0") + ",";

                        String repoName = projectName.substring(projectName.lastIndexOf(File.separatorChar) + 1);
                        makeFolder(projectName +"_Json");
                        String resultFileFullName = projectName +"_Json" + File.separatorChar + repoName + "-" + retrievedInfo.currentCommitID.substring(0, 8) + ".json";
                        JSON.writeJSONInformationASFile(resultFileFullName, JSONResult.replaceAll(",$", "]"));
                    }
                }

            }
        }
    }

    /**
     * Detects refactorings in all commits in the range between two specified commits.
     */
    private static void detectBetweenCommits(String[] args) throws Exception {
        if (!(args.length == 3 || args.length == 4)) {
            System.out.println("Incorrect arguments. Please, use '-h' option for help.\n");
        }
        String folder = args[1];
        String projectName = folder;
        String startCommit = args[2];
        String endCommit = (args.length == 4) ? args[3] : null;
        GitService gitService = new GitService();
        List<GitInfo> allOfRetrievedInfo = new ArrayList<>();
        TimeMemory.measure_Initial_Time_And_Memory();
        try (Repository repo = gitService.openRepository(folder)) {
            String gitURL = repo.getConfig().getString("remote", "origin", "url");
            GitHistoryRefDetect detector = new GitHistoryRefDetect();
            allOfRetrievedInfo = detector.detectBetweenCommits(repo, startCommit, endCommit, new RefactoringHandler() {
                private int commitCount = 0;

                @Override
                public void handle(String commitId, List<AppliedRefactoringsInformation> refactorings, boolean ktFilesChanged) {
//                    if (commitCount > 0) {
//                        sb.append(",").append("\n");
//                    }
//                    JsonUtil.commitJSON(sb, gitURL, commitId, refactorings, ktFilesChanged);
                    commitCount++;
                }

                @Override
                public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
                    System.out.printf("Total count: [Commits: %d, Errors: %d, Refactorings: %d]%n",
                            commitsCount, errorCommitsCount, refactoringsCount);
                }

                @Override
                public void handleException(String commit, Exception e) {
                    System.err.println("Error processing commit " + commit);
                    e.printStackTrace(System.err);
                }
            });
            TimeMemory.measure_Final_Time_And_Memory();

            if (allOfRetrievedInfo != null) {
                for (GitInfo infos : allOfRetrievedInfo) {
                    for (AppliedRefactoringsInformation refactoring : infos.appliedRefactorings) {
                        System.out.println(refactoring.description);
                    }
                }

                for (GitInfo retrievedInfo : allOfRetrievedInfo) {
                    String JSONResult = "[" + JSON.save_DetectedRefactorings_as_JSONFile(retrievedInfo.appliedRefactorings,
                            retrievedInfo.repositoryUrl, retrievedInfo.currentCommitID, retrievedInfo.commitUrl,
                            TimeMemory.elapsedTimeInSeconds, TimeMemory.usedMemory, "RefDetect 1.0") + ",";

                    String repoName = projectName.substring(projectName.lastIndexOf(File.separatorChar) + 1);
                    makeFolder(projectName +"_Json");
                    String resultFileFullName = projectName +"_Json" + File.separatorChar + repoName + "-" + retrievedInfo.currentCommitID.substring(0, 8) + ".json";
                    JSON.writeJSONInformationASFile(resultFileFullName, JSONResult.replaceAll(",$", "]"));
                }
            }
        }
    }

    private static void printUsage() {
        System.out.println("-h Usage: refDetect_1.01.jar <args>");
        System.out.println(
                "-c <git-repo-folder> <commit-sha1> \t\t\t\tDetect refactorings at the specific commit <commit-sha1> for " +
                        "project <git-repo-folder>.");
        System.out.println(
                "-bc <git-repo-folder> <start-commit-sha1> <end-commit-sha1>\t\tDetect refactorings between " +
                        "<start-commit-sha1> and <end-commit-sha1> for a project <git-repo-folder>.");
        System.out.println(
                "-list <git-repo-folder> <json-file-path> \t\tDetect refactorings in a JSON file that Refdetect outputs, All refactoring JSON files should be combined in one JSON file.");
        System.out.println(
                "-allrepos <git-all-repos-folder> <all-repos-json-file-path> \t\tDetect refactorings in a JSON file that Refdetect outputs for multiple different repositories, All refactoring JSON files from each repo should be combined in one JSON file. eg: Final_Results.json");
        System.out.println(
                "-all <git-repo-folder> <branch>\t\t\t\t\tDetect all refactorings at the <branch> for <git-repo-folder>. " +
                        "If <branch> is not specified, commits from master branch are analyzed.");
    }
}