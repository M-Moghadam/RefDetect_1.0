package gitHandler;

import code2Text_Kotlin.Code2TextKotlin;
import codeInformation.SourceInformation;
import moea.problem.AppliedRefactoringsInformation;
import moea.problem.RunRefactoringProblem;
import moea.problem.Utility;
import moea.variable.RefactoringVariable;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class GitHistoryRefDetect {
    private static final String GITHUB_URL = "https://github.com/";
    private static final String BITBUCKET_URL = "https://bitbucket.org/";
//    private final RefactoringType[] refactoringTypesToConsider = RefactoringType.ALL;

    private void saveSourceInDisk(Map<String, String> fileContentsBefore, String commitNewFolder, String commitId) {

        if (commitId.endsWith("_Current")) commitId = "curr";
        else commitId = "prev";

        for (Map.Entry<String, String> entry : fileContentsBefore.entrySet()) {
            File targetFile = new File(commitNewFolder + "/"+ commitId + "/" + entry.getKey());
            File parent = targetFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }

            try {
                FileWriter myWriter = new FileWriter(targetFile);
                myWriter.write(entry.getValue().toCharArray());
                myWriter.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    protected GitInfo detectRefactorings(File projectFolder,
                                                                      GitService gitService,
                                                   Repository repository,
                                                   RevCommit currentCommit,
                                                   RefactoringHandler handler) throws Exception {
        List<AppliedRefactoringsInformation> refactoringsAtRevision = new ArrayList<>();
        String cloneURL = repository.getConfig().getString("remote", "origin", "url");
        String commitId = currentCommit.getId().getName();
        List<String> filePathsBefore = new ArrayList<>();
        List<String> filePathsCurrent = new ArrayList<>();
        Map<String, String> renamedFilesHint = new HashMap<>();
        gitService.fileTreeDiff(repository, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);

        Set<String> repositoryDirectoriesBefore = new LinkedHashSet<>();
        Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<>();
        Map<String, String> fileContentsBefore = new LinkedHashMap<>();
        Map<String, String> fileContentsCurrent = new LinkedHashMap<>();
        String commitFolder = projectFolder.toString();
        commitFolder = commitFolder.substring(0, commitFolder.toString().lastIndexOf(File.separatorChar));
        try (RevWalk walk = new RevWalk(repository)) {
            // If no kt files changed, there is no refactoring. Also, if there are
            // only ADD's or only REMOVE's there is no refactoring
            boolean ktFilesChanged = !filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty();
            if (ktFilesChanged && currentCommit.getParentCount() > 0) {
                RevCommit parentCommit = currentCommit.getParent(0);
                String parentCommitId = parentCommit.getId().getName();
                String newFolderName = projectFolder.getName() + "-" + commitId.substring(0, 8);
                String repoName = projectFolder.toString().substring(projectFolder.toString().lastIndexOf(File.separatorChar) + 1) + "_Diff";
                String newFolderPath = commitFolder + File.separatorChar + repoName + File.separatorChar + newFolderName;
                populateFileContents(repository, parentCommit, filePathsBefore, fileContentsBefore,
                    repositoryDirectoriesBefore);
                saveSourceInDisk(fileContentsBefore, newFolderPath, parentCommitId + "_Parent");
                String parentCommitFullName = parentCommitId + "_Parent";
                SourceInformation initialSourceInformation = new Code2TextKotlin().getStringRepresentationSourceCode(newFolderPath+File.separatorChar+"prev");
                populateFileContents(repository, currentCommit, filePathsCurrent, fileContentsCurrent,
                    repositoryDirectoriesCurrent);
                saveSourceInDisk(fileContentsCurrent, newFolderPath, commitId + "_Current");
                String currentCommitFullName = commitId + "_Current";
                SourceInformation desiredSourceInformation = new Code2TextKotlin().getStringRepresentationSourceCode(newFolderPath+File.separatorChar+"curr");
                RunRefactoringProblem NSGA_II = new RunRefactoringProblem();
                NondominatedPopulation result = NSGA_II.run(initialSourceInformation, desiredSourceInformation);
                Solution solution = result.get(0);
                RefactoringVariable variable = (RefactoringVariable) solution.getVariable(0);
                List<AppliedRefactoringsInformation> appliedRefs = Utility.getRefactringInformation(variable.getRefactorings());
                refactoringsAtRevision.addAll(appliedRefs);
                refactoringsAtRevision = filter(refactoringsAtRevision);
            } else {
                refactoringsAtRevision = Collections.emptyList();
            }
            handler.handle(commitId, refactoringsAtRevision, ktFilesChanged);

            walk.dispose();
        }
        return new GitInfo(refactoringsAtRevision, cloneURL, commitId, cloneURL.replace(".git", "") + "/commit/" + commitId);
    }

    private List<GitInfo> detect(GitService gitService,
                        Repository repository,
                        final RefactoringHandler handler,
                        Iterator<RevCommit> i) {
        int commitsCount = 0;
        int errorCommitsCount = 0;
        int refactoringsCount = 0;

        File metadataFolder = repository.getDirectory();
        File projectFolder = metadataFolder.getParentFile();
        String projectName = projectFolder.getName();
        List<AppliedRefactoringsInformation> refactoring = new ArrayList<>();
        GitInfo singleInfo = null;
        List<GitInfo> resultedData = new ArrayList<>();

        long time = System.currentTimeMillis();
        while (i.hasNext()) {
            RevCommit currentCommit = i.next();
            try {
                singleInfo = detectRefactorings(projectFolder, gitService, repository, currentCommit, handler);
                resultedData.add(singleInfo);
                refactoringsCount += singleInfo.appliedRefactorings.size();
//                refactoring.addAll(refactoringsAtRevision);

            } catch (Exception e) {
                handler.handleException(currentCommit.getId().getName(), e);
                errorCommitsCount++;
            }

            commitsCount++;
            long time2 = System.currentTimeMillis();
            if ((time2 - time) > 20000) {
                time = time2;
            }

        }

        handler.onFinish(refactoringsCount, commitsCount, errorCommitsCount);
        System.out.printf("Analyzed %s [Commits: %d, Errors: %d, Refactorings: %d]%n", projectName,
            commitsCount, errorCommitsCount, refactoringsCount);
        return resultedData;
    }

    public GitInfo detectAtCommit(Repository repository, String commitId, RefactoringHandler handler) {
        String cloneURL = repository.getConfig().getString("remote", "origin", "url");
        List<AppliedRefactoringsInformation> refactoring = null;
        GitInfo resultedData = null;
        File metadataFolder = repository.getDirectory();
        File projectFolder = metadataFolder.getParentFile();
        GitService gitService = new GitService();
        RevWalk walk = new RevWalk(repository);
        try {
            RevCommit commit = walk.parseCommit(repository.resolve(commitId));
            if (commit.getParentCount() > 0) {
                walk.parseCommit(commit.getParent(0));
                resultedData = this.detectRefactorings(projectFolder, gitService, repository, commit, handler);
            }
        } catch (Exception e) {
            handler.handleException(commitId, e);
        } finally {
            walk.close();
            walk.dispose();
        }
        return resultedData;
    }

//    public void detectAtCommit(Repository repository, String commitId, RefactoringHandler handler) {
//        detectAtCommit(repository, commitId, handler);
//    }

    public List<GitInfo> detectAll(Repository repository, String branch, final RefactoringHandler handler) throws Exception {
        GitService gitService = new GitService();
        List<GitInfo> allOfGitInfos = new ArrayList<>();
        RevWalk walk = gitService.createAllRevsWalk(repository, branch);
        try {
            allOfGitInfos = detect(gitService, repository, handler, walk.iterator());
        } finally {
            walk.dispose();
        }
        return allOfGitInfos;
    }

    public List<GitInfo> detectBetweenCommits(Repository repository, String startCommitId, String endCommitId,
                                     RefactoringHandler handler) throws Exception {
        List<GitInfo> allOfGitInfos = new ArrayList<>();
        GitService gitService = new GitService();
        Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repository, startCommitId, endCommitId);
        allOfGitInfos = detect(gitService, repository, handler, walk.iterator());
        return allOfGitInfos;
    }

    protected List<AppliedRefactoringsInformation> filter(List<AppliedRefactoringsInformation> refactoringsAtRevision) {
//        if (this.refactoringTypesToConsider == null) {
//            return refactoringsAtRevision;
//        }
        /*  TODO: perform filtration
            if (this.refactoringTypesToConsider.contains(ref.getRefactoringType())) {
            }*/
        return new ArrayList<>(refactoringsAtRevision);
    }

//    protected UMLModel createModelInKotlin(Map<String, String> fileContents, Set<String> repositoryDirectories) throws
//        Exception {
//        UMLModelPsiReaderCli psiReader = new UMLModelPsiReaderCli(repositoryDirectories);
//        psiReader.parseFiles(fileContents);
//        return psiReader.getUmlModel();
//    }

    private void populateFileContents(Repository repository,
                                      RevCommit commit,
                                      List<String> filePaths,
                                      Map<String, String> fileContents,
                                      Set<String> repositoryDirectories) throws Exception {
        RevTree parentTree = commit.getTree();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(parentTree);
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                String pathString = treeWalk.getPathString();
                if (filePaths.contains(pathString)) {
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(loader.openStream(), writer);
                    fileContents.put(pathString, writer.toString());
                }
                if (pathString.endsWith(".kt") && pathString.contains("/")) {
                    String directory = pathString.substring(0, pathString.lastIndexOf("/"));
                    repositoryDirectories.add(directory);
                    //include sub-directories
                    String subDirectory = directory;
                    while (subDirectory.contains("/")) {
                        subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf("/"));
                        repositoryDirectories.add(subDirectory);
                    }
                }
            }
        }
    }

    public static String extractCommitURL(String cloneURL, String commitId) {
        int indexOfDotGit = cloneURL.length();
        if (cloneURL.endsWith(".git")) {
            indexOfDotGit = cloneURL.indexOf(".git");
        } else if (cloneURL.endsWith("/")) {
            indexOfDotGit = cloneURL.length() - 1;
        }
        String commitResource = "/";
        if (cloneURL.startsWith(GITHUB_URL)) {
            commitResource = "/commit/";
        } else if (cloneURL.startsWith(BITBUCKET_URL)) {
            commitResource = "/commits/";
        }
        return cloneURL.substring(0, indexOfDotGit) + commitResource + commitId;
    }

}
