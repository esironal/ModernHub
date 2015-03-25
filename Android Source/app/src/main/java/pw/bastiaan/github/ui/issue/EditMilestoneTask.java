/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pw.bastiaan.github.ui.issue;

import android.accounts.Account;

import pw.bastiaan.github.core.issue.IssueStore;
import pw.bastiaan.github.ui.DialogFragmentActivity;
import pw.bastiaan.github.ui.ProgressDialogTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.MilestoneService;

import pw.bastiaan.github.RequestCodes;

/**
 * Task to edit a milestone
 */
public class EditMilestoneTask extends ProgressDialogTask<Issue> {

    @Inject
    private MilestoneService service;

    @Inject
    private IssueStore store;

    private final MilestoneDialog milestoneDialog;

    private final IRepositoryIdProvider repositoryId;

    private final int issueNumber;

    private int milestoneNumber;

    /**
     * Create task to edit a milestone
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditMilestoneTask(final DialogFragmentActivity activity,
            final IRepositoryIdProvider repositoryId, final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
        milestoneDialog = new MilestoneDialog(activity, RequestCodes.ISSUE_MILESTONE_UPDATE,
                repositoryId, service);
    }

    @Override
    protected Issue run(Account account) throws Exception {
        Issue editedIssue = new Issue();
        editedIssue.setNumber(issueNumber);
        editedIssue.setMilestone(new Milestone().setNumber(milestoneNumber));
        return store.editIssue(repositoryId, editedIssue);
    }

    /**
     * Prompt for milestone selection
     *
     * @param milestone
     *            current milestone
     * @return this task
     */
    public EditMilestoneTask prompt(Milestone milestone) {
        milestoneDialog.show(milestone);
        return this;
    }

    /**
     * Edit issue to have given milestone
     *
     * @param milestone
     * @return this task
     */
    public EditMilestoneTask edit(Milestone milestone) {
        if (milestone != null)
            milestoneNumber = milestone.getNumber();
        else
            milestoneNumber = -1;

        showIndeterminate(pw.bastiaan.github.R.string.updating_milestone);

        super.execute();

        return this;
    }
}
