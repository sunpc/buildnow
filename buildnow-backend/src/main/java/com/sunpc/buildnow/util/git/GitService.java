/**
 *  Class: GitService
 *  Description: GIT Service
 *  Created for zDevOps v1.0
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 12/08/2018: Initial version. (V1.0)
 *  2. 12/19/2018: Added WindowCacheConfig for performance tuning. (V1.0)
 *  3. 01/03/2019: Add create(). (V1.1)
 */
package com.sunpc.buildnow.util.git;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author PengChengSun
 *
 */
@Service
public class GitService {

	private Git git = null;

	public void open(String localRepository) throws IOException {
		// configure WindowCacheConfig
		// https://stackoverflow.com/questions/18221987/how-to-tune-egit-for-large-repositories
		WindowCacheConfig cfg = new WindowCacheConfig();
		cfg.setPackedGitLimit(500 * WindowCacheConfig.MB);
		cfg.install();

		// Git.open
		git = Git.open(new File(localRepository + "/.git"));
	}

	public void close() {
		git.close();
	}

	public void create(String remoteUrl, String localRepository)
			throws InvalidRemoteException, TransportException, GitAPIException {
		File repoDir = new File(localRepository);
		Git.cloneRepository().setURI(remoteUrl).setDirectory(repoDir)
				.setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out))).call();
	}

	public void checkout(String branchName) throws RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException, GitAPIException {
		// check if local branch exists
		boolean branchExists = branchExist(branchName,false);
//		List<Ref> branchList = git.branchList().call();
//		for (Ref ref : branchList) {
//			String[] refNameArray = ref.getName().split("/");
//			if (refNameArray[refNameArray.length - 1].toUpperCase().trim().equals(branchName.toUpperCase().trim())) {
//				branchExists = true;
//				break;
//			}
//		}

		// check if remote branch exists
		boolean remoteExists = branchExist(branchName, true);
//		if (!branchExists) {
//			List<Ref> remoteList = git.branchList().setListMode(ListMode.REMOTE).call();
//			for (Ref ref : remoteList) {
//				String[] refNameArray = ref.getName().split("/");
//				if (refNameArray[refNameArray.length - 1].toUpperCase().trim()
//						.equals(branchName.toUpperCase().trim())) {
//					remoteExists = true;
//					break;
//				}
//			}
//		}

		// check out
		if (branchExists) {
			git.checkout().setName(branchName).call();
		} else if (remoteExists) {
			git.checkout().setCreateBranch(true).setName(branchName).setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
					.setStartPoint("origin/" + branchName).call();
		} else {
			git.checkout().setCreateBranch(true).setName(branchName).call();
		}
	}

	public void pull() throws WrongRepositoryStateException, InvalidConfigurationException, InvalidRemoteException,
			CanceledException, RefNotFoundException, RefNotAdvertisedException, NoHeadException, TransportException,
			GitAPIException {
		git.pull().setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out))).call();
	}

	public void push() throws InvalidRemoteException, TransportException, GitAPIException {
		git.push().setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out))).call();
	}

	public String commit(String message) throws NoHeadException, NoMessageException, UnmergedPathsException,
			ConcurrentRefUpdateException, WrongRepositoryStateException, AbortedByHookException, GitAPIException {
		// get different entries
		List<DiffEntry> diffEntries = git.diff().setShowNameAndStatusOnly(true).call();
		if (diffEntries == null || diffEntries.size() == 0) {
			return "Nothing to commit";
		}

		// check all updates
		List<String> updateFiles = new ArrayList<String>();
		for (DiffEntry entry : diffEntries) {
			switch (entry.getChangeType()) {
			case ADD:
			case COPY:
			case RENAME:
			case MODIFY:
				updateFiles.add(entry.getNewPath());
				break;
			case DELETE:
				updateFiles.add(entry.getOldPath());
				break;
			}
		}

		// add
		AddCommand addCmd = git.add();
		for (String file : updateFiles) {
			addCmd.addFilepattern(file);
		}
		addCmd.call();

		// commit
		CommitCommand commitCmd = git.commit();
		for (String file : updateFiles) {
			commitCmd.setOnly(file);
		}
		RevCommit revCommit = commitCmd.setMessage(message).call();

		// return the revision name
		return "Committed revision " + revCommit.getName();
	}

	public boolean branchExist(String branchName, boolean isRemote) throws RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException, GitAPIException{
		List<Ref> branchList;
		if(isRemote){
			branchList = git.branchList().setListMode(ListMode.REMOTE).call();
		}else {
			branchList = git.branchList().call();
		}
		for (Ref ref : branchList) {
			String[] refNameArray = ref.getName().split("/");
			if (refNameArray[refNameArray.length - 1].toUpperCase().trim().equals(branchName.toUpperCase().trim())) {
				return true;
			}
		}
		return false;
	}

	public void removeLocalBranch(String branchName)throws RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException, GitAPIException{
			git.branchDelete().setBranchNames(branchName).call();

	}

	public void reset() throws RefAlreadyExistsException, RefNotFoundException,
            InvalidRefNameException, CheckoutConflictException, GitAPIException{
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
    }

}
