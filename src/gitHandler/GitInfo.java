package gitHandler;

import moea.problem.AppliedRefactoringsInformation;

import java.util.ArrayList;
import java.util.List;

public class GitInfo {
    public GitInfo(List<AppliedRefactoringsInformation> appliedRefactorings, String repositoryUrl, String currentCommitID, String commitUrl) {
        this.appliedRefactorings = appliedRefactorings;
        this.repositoryUrl = repositoryUrl;
        this.currentCommitID = currentCommitID;
        this.commitUrl = commitUrl;
    }

    List<AppliedRefactoringsInformation> appliedRefactorings;
    String repositoryUrl;
    String currentCommitID;
    String commitUrl;

}
