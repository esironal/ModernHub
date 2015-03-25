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

import static android.view.View.GONE;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import pw.bastiaan.github.core.issue.IssueFilter;
import pw.bastiaan.github.ui.DialogFragmentActivity;
import pw.bastiaan.github.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.Set;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;

import pw.bastiaan.github.Intents;

/**
 * Activity to create or edit an issues filter for a repository
 */
public class EditIssuesFilterActivity extends DialogFragmentActivity {

    /**
     * Create intent for creating an issue filter for the given repository
     *
     * @param filter
     * @return intent
     */
    public static Intent createIntent(IssueFilter filter) {
        return new Intents.Builder("repo.issues.filter.VIEW").add(Intents.EXTRA_ISSUE_FILTER,
            filter).toIntent();
    }

    private static final int REQUEST_LABELS = 1;

    private static final int REQUEST_MILESTONE = 2;

    private static final int REQUEST_ASSIGNEE = 3;

    @Inject
    private CollaboratorService collaborators;

    @Inject
    private MilestoneService milestones;

    @Inject
    private LabelService labels;

    @Inject
    private AvatarLoader avatars;

    private LabelsDialog labelsDialog;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    private IssueFilter filter;

    private TextView labelsText;

    private TextView milestoneText;

    private TextView assigneeText;

    private ImageView avatarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(pw.bastiaan.github.R.layout.issues_filter_edit);

        labelsText = finder.find(pw.bastiaan.github.R.id.tv_labels);
        milestoneText = finder.find(pw.bastiaan.github.R.id.tv_milestone);
        assigneeText = finder.find(pw.bastiaan.github.R.id.tv_assignee);
        avatarView = finder.find(pw.bastiaan.github.R.id.iv_avatar);

        if (savedInstanceState != null)
            filter = (IssueFilter) savedInstanceState
                .getSerializable(Intents.EXTRA_ISSUE_FILTER);

        if (filter == null)
            filter = (IssueFilter) getIntent().getSerializableExtra(
                Intents.EXTRA_ISSUE_FILTER);

        final Repository repository = filter.getRepository();

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(pw.bastiaan.github.R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(pw.bastiaan.github.R.string.filter_issues_title);
        actionBar.setSubtitle(repository.generateId());
        avatars.bind(actionBar, repository.getOwner());

        OnClickListener assigneeListener = new OnClickListener() {

            public void onClick(View v) {
                if (assigneeDialog == null)
                    assigneeDialog = new AssigneeDialog(
                        EditIssuesFilterActivity.this, REQUEST_ASSIGNEE,
                        repository, collaborators);
                assigneeDialog.show(filter.getAssignee());
            }
        };

        findViewById(pw.bastiaan.github.R.id.tv_assignee_label)
            .setOnClickListener(assigneeListener);
        assigneeText.setOnClickListener(assigneeListener);

        OnClickListener milestoneListener = new OnClickListener() {

            public void onClick(View v) {
                if (milestoneDialog == null)
                    milestoneDialog = new MilestoneDialog(
                        EditIssuesFilterActivity.this, REQUEST_MILESTONE,
                        repository, milestones);
                milestoneDialog.show(filter.getMilestone());
            }
        };

        findViewById(pw.bastiaan.github.R.id.tv_milestone_label)
            .setOnClickListener(milestoneListener);
        milestoneText.setOnClickListener(milestoneListener);

        OnClickListener labelsListener = new OnClickListener() {

            public void onClick(View v) {
                if (labelsDialog == null)
                    labelsDialog = new LabelsDialog(
                        EditIssuesFilterActivity.this, REQUEST_LABELS,
                        repository, labels);
                labelsDialog.show(filter.getLabels());
            }
        };

        findViewById(pw.bastiaan.github.R.id.tv_labels_label)
            .setOnClickListener(labelsListener);
        labelsText.setOnClickListener(labelsListener);

        updateAssignee();
        updateMilestone();
        updateLabels();

        RadioButton openButton = (RadioButton) findViewById(pw.bastiaan.github.R.id.rb_open);

        openButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
                if (isChecked)
                    filter.setOpen(true);
            }
        });

        RadioButton closedButton = (RadioButton) findViewById(pw.bastiaan.github.R.id.rb_closed);

        closedButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
                if (isChecked)
                    filter.setOpen(false);
            }
        });

        if (filter.isOpen())
            openButton.setChecked(true);
        else
            closedButton.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(pw.bastiaan.github.R.menu.issue_filter, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case pw.bastiaan.github.R.id.m_apply:
                Intent intent = new Intent();
                intent.putExtra(Intents.EXTRA_ISSUE_FILTER, filter);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Intents.EXTRA_ISSUE_FILTER, filter);
    }

    private void updateLabels() {
        Set<Label> selected = filter.getLabels();
        if (selected != null)
            LabelDrawableSpan.setText(labelsText, selected);
        else
            labelsText.setText(pw.bastiaan.github.R.string.none);
    }

    private void updateMilestone() {
        Milestone selected = filter.getMilestone();
        if (selected != null)
            milestoneText.setText(selected.getTitle());
        else
            milestoneText.setText(pw.bastiaan.github.R.string.none);
    }

    private void updateAssignee() {
        User selected = filter.getAssignee();
        if (selected != null) {
            avatars.bind(avatarView, selected);
            assigneeText.setText(selected.getLogin());
        } else {
            avatarView.setVisibility(GONE);
            assigneeText.setText(pw.bastiaan.github.R.string.assignee_anyone);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
            case REQUEST_LABELS:
                filter.setLabels(LabelsDialogFragment.getSelected(arguments));
                updateLabels();
                break;
            case REQUEST_MILESTONE:
                filter.setMilestone(MilestoneDialogFragment.getSelected(arguments));
                updateMilestone();
                break;
            case REQUEST_ASSIGNEE:
                filter.setAssignee(AssigneeDialogFragment.getSelected(arguments));
                updateAssignee();
                break;
        }
    }
}
