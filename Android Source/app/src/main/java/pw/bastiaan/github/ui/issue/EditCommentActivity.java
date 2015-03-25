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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import pw.bastiaan.github.ui.comment.CommentPreviewPagerAdapter;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import pw.bastiaan.github.Intents;

/**
 * Activity to edit a comment on an {@link Issue}
 */
public class EditCommentActivity extends
        pw.bastiaan.github.ui.comment.CreateCommentActivity {

    /**
     * Create intent to edit a comment
     *
     * @param repoId
     * @param issueNumber
     * @param user
     * @return intent
     */
    public static Intent createIntent(RepositoryId repoId, int issueNumber, Comment comment,
            User user) {
        Intents.Builder builder = new Intents.Builder("issue.comment.edit.VIEW");
        builder.repo(repoId);
        builder.add(Intents.EXTRA_COMMENT, comment);
        builder.add(Intents.EXTRA_ISSUE_NUMBER, issueNumber);
        builder.add(Intents.EXTRA_USER, user);
        return builder.toIntent();
    }

    private RepositoryId repositoryId;

    /**
     * Comment to edit.
     */
    private Comment comment;

    private int issueNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        comment = getSerializableExtra(Intents.EXTRA_COMMENT);
        issueNumber = getIntExtra(Intents.EXTRA_ISSUE_NUMBER);
        repositoryId = new RepositoryId(getStringExtra(Intents.EXTRA_REPOSITORY_OWNER),
                getStringExtra(Intents.EXTRA_REPOSITORY_NAME));

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(pw.bastiaan.github.R.string.issue_title) + issueNumber);
        actionBar.setSubtitle(repositoryId.generateId());
        avatars.bind(actionBar, (User) getSerializableExtra(Intents.EXTRA_USER));
    }

    @Override
    protected void createComment(String comment) {
        editComment(comment);
    }

    /**
     * Edit comment.
     *
     * @param commentText
     */
    protected void editComment(String commentText) {
        comment.setBody(commentText);

        new EditCommentTask(this, repositoryId, comment) {
            @Override
            protected void onSuccess(Comment comment) throws Exception {
                super.onSuccess(comment);

                finish(comment);
            }
        }.start();
    }

    @Override
    protected CommentPreviewPagerAdapter createAdapter() {
        CommentPreviewPagerAdapter commentPreviewPagerAdapter = new CommentPreviewPagerAdapter(this, repositoryId);
        commentPreviewPagerAdapter.setCommentText(comment != null ? comment.getBody() : null);
        return commentPreviewPagerAdapter;
    }
}
