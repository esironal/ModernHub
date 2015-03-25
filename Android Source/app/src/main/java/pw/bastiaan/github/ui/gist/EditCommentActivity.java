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
package pw.bastiaan.github.ui.gist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import pw.bastiaan.github.ui.comment.CommentPreviewPagerAdapter;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;

import pw.bastiaan.github.Intents;

/**
 * Activity to edit a comment on a {@link Gist}
 */
public class EditCommentActivity extends
        pw.bastiaan.github.ui.comment.CreateCommentActivity {

    /**
     * Create intent to edit a comment
     *
     * @param gist
     * @return intent
     */
    public static Intent createIntent(Gist gist, Comment comment) {
        Intents.Builder builder = new Intents.Builder("gist.comment.edit.VIEW");
        builder.gist(gist);
        builder.add(Intents.EXTRA_COMMENT, comment);
        return builder.toIntent();
    }

    private Gist gist;

    /**
     * Comment to edit.
     */
    private Comment comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        gist = getSerializableExtra(Intents.EXTRA_GIST);
        comment = getSerializableExtra(Intents.EXTRA_COMMENT);
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(pw.bastiaan.github.R.string.gist_title) + gist.getId());
        User user = gist.getUser();
        if (user != null)
            actionBar.setSubtitle(user.getLogin());
        avatars.bind(actionBar, user);
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

        new EditCommentTask(this, gist.getId(), comment) {
            @Override
            protected void onSuccess(Comment comment) throws Exception {
                super.onSuccess(comment);

                finish(comment);
            }
        }.start();
    }

    @Override
    protected CommentPreviewPagerAdapter createAdapter() {
        CommentPreviewPagerAdapter commentPreviewPagerAdapter = new CommentPreviewPagerAdapter(this, null);
        commentPreviewPagerAdapter.setCommentText(comment != null ? comment.getBody() : null);
        return commentPreviewPagerAdapter;
    }
}
