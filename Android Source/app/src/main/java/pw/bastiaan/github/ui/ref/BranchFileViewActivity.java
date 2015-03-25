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
package pw.bastiaan.github.ui.ref;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.github.kevinsawicki.wishlist.ViewUtils;

import pw.bastiaan.github.core.code.RefreshBlobTask;
import pw.bastiaan.github.core.commit.CommitUtils;
import pw.bastiaan.github.ui.BaseActivity;
import pw.bastiaan.github.ui.MarkdownLoader;
import pw.bastiaan.github.util.AvatarLoader;
import pw.bastiaan.github.util.HttpImageGetter;
import pw.bastiaan.github.util.MarkdownUtils;
import pw.bastiaan.github.util.PreferenceUtils;
import pw.bastiaan.github.util.ShareUtils;
import pw.bastiaan.github.util.SourceEditor;
import pw.bastiaan.github.util.ToastUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.util.EncodingUtils;

import pw.bastiaan.github.Intents;

/**
 * Activity to view a file on a branch
 */
public class BranchFileViewActivity extends BaseActivity implements
    LoaderCallbacks<CharSequence> {

    private static final String TAG = "BranchFileViewActivity";

    private static final String ARG_TEXT = "text";

    private static final String ARG_REPO = "repo";

    /**
     * Create intent to show file in commit
     *
     * @param repository
     * @param branch
     * @param file
     * @param blobSha
     * @return intent
     */
    public static Intent createIntent(Repository repository, String branch,
        String file, String blobSha) {
        Intents.Builder builder = new Intents.Builder("branch.file.VIEW");
        builder.repo(repository);
        builder.add(Intents.EXTRA_BASE, blobSha);
        builder.add(Intents.EXTRA_PATH, file);
        builder.add(Intents.EXTRA_HEAD, branch);
        return builder.toIntent();
    }

    private Repository repo;

    private String sha;

    private String path;

    private String file;

    private String branch;

    private boolean isMarkdownFile;

    private String renderedMarkdown;

    private Blob blob;

    private ProgressBar loadingBar;

    private WebView codeView;

    private SourceEditor editor;

    private MenuItem markdownItem;

    @Inject
    private AvatarLoader avatars;

    @Inject
    private HttpImageGetter imageGetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(pw.bastiaan.github.R.layout.commit_file_view);

        repo = getSerializableExtra(Intents.EXTRA_REPOSITORY);
        sha = getStringExtra(Intents.EXTRA_BASE);
        path = getStringExtra(Intents.EXTRA_PATH);
        branch = getStringExtra(Intents.EXTRA_HEAD);

        loadingBar = finder.find(pw.bastiaan.github.R.id.pb_loading);
        codeView = finder.find(pw.bastiaan.github.R.id.wv_code);

        codeView.getSettings().setBuiltInZoomControls(true);

        file = CommitUtils.getName(path);
        isMarkdownFile = MarkdownUtils.isMarkdown(file);
        editor = new SourceEditor(codeView);
        editor.setWrap(PreferenceUtils.getCodePreferences(this).getBoolean(
            PreferenceUtils.WRAP, false));

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(pw.bastiaan.github.R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(file);
        actionBar.setSubtitle(branch);
        avatars.bind(actionBar, repo.getOwner());

        loadContent();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu optionsMenu) {
        getMenuInflater().inflate(pw.bastiaan.github.R.menu.file_view, optionsMenu);

        MenuItem wrapItem = optionsMenu.findItem(pw.bastiaan.github.R.id.m_wrap);
        if (PreferenceUtils.getCodePreferences(this).getBoolean(PreferenceUtils.WRAP, false))
            wrapItem.setTitle(pw.bastiaan.github.R.string.disable_wrapping);
        else
            wrapItem.setTitle(pw.bastiaan.github.R.string.enable_wrapping);

        markdownItem = optionsMenu.findItem(pw.bastiaan.github.R.id.m_render_markdown);
        if (isMarkdownFile) {
            markdownItem.setEnabled(blob != null);
            markdownItem.setVisible(true);
            if (PreferenceUtils.getCodePreferences(this).getBoolean(
                PreferenceUtils.RENDER_MARKDOWN, true))
                markdownItem.setTitle(pw.bastiaan.github.R.string.show_raw_markdown);
            else
                markdownItem.setTitle(pw.bastiaan.github.R.string.render_markdown);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case pw.bastiaan.github.R.id.m_wrap:
                if (editor.getWrap())
                    item.setTitle(pw.bastiaan.github.R.string.enable_wrapping);
                else
                    item.setTitle(pw.bastiaan.github.R.string.disable_wrapping);
                editor.toggleWrap();
                PreferenceUtils.save(PreferenceUtils.getCodePreferences(this)
                    .edit().putBoolean(PreferenceUtils.WRAP, editor.getWrap()));
                return true;

            case pw.bastiaan.github.R.id.m_share:
                shareFile();
                return true;

            case pw.bastiaan.github.R.id.m_render_markdown:
                if (editor.isMarkdown()) {
                    item.setTitle(pw.bastiaan.github.R.string.render_markdown);
                    editor.toggleMarkdown();
                    editor.setSource(file, blob);
                } else {
                    item.setTitle(pw.bastiaan.github.R.string.show_raw_markdown);
                    editor.toggleMarkdown();
                    if (renderedMarkdown != null)
                        editor.setSource(file, renderedMarkdown, false);
                    else
                        loadMarkdown();
                }
                PreferenceUtils.save(PreferenceUtils.getCodePreferences(this)
                    .edit().putBoolean(PreferenceUtils.RENDER_MARKDOWN, editor.isMarkdown()));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<CharSequence> onCreateLoader(int loader, Bundle args) {
        final String raw = args.getString(ARG_TEXT);
        final IRepositoryIdProvider repo = (IRepositoryIdProvider) args
            .getSerializable(ARG_REPO);
        return new MarkdownLoader(this, repo, raw, imageGetter, false);
    }

    @Override
    public void onLoadFinished(Loader<CharSequence> loader,
        CharSequence rendered) {
        if (rendered == null)
            ToastUtils.show(this, pw.bastiaan.github.R.string.error_rendering_markdown);

        ViewUtils.setGone(loadingBar, true);
        ViewUtils.setGone(codeView, false);

        if (!TextUtils.isEmpty(rendered)) {
            renderedMarkdown = rendered.toString();
            if (markdownItem != null)
                markdownItem.setEnabled(true);
            editor.setMarkdown(true).setSource(file, renderedMarkdown, false);
        }
    }

    @Override
    public void onLoaderReset(Loader<CharSequence> loader) {
    }

    private void shareFile() {
        String id = repo.generateId();
        startActivity(ShareUtils.create(path + " at " + branch + " on " + id,
                "https://github.com/" + id + "/blob/" + branch + '/' + path));
    }

    private void loadMarkdown() {
        ViewUtils.setGone(loadingBar, false);
        ViewUtils.setGone(codeView, true);

        String markdown = new String(
            EncodingUtils.fromBase64(blob.getContent()));
        Bundle args = new Bundle();
        args.putCharSequence(ARG_TEXT, markdown);
        args.putSerializable(ARG_REPO, repo);
        getSupportLoaderManager().restartLoader(0, args, this);
    }

    private void loadContent() {
        ViewUtils.setGone(loadingBar, false);
        ViewUtils.setGone(codeView, true);

        new RefreshBlobTask(repo, sha, this) {

            @Override
            protected void onSuccess(Blob blob) throws Exception {
                super.onSuccess(blob);

                BranchFileViewActivity.this.blob = blob;

                if (markdownItem != null)
                    markdownItem.setEnabled(true);

                if (isMarkdownFile
                    && PreferenceUtils.getCodePreferences(
                    BranchFileViewActivity.this).getBoolean(
                    PreferenceUtils.RENDER_MARKDOWN, true))
                    loadMarkdown();
                else {
                    ViewUtils.setGone(loadingBar, true);
                    ViewUtils.setGone(codeView, false);

                    editor.setMarkdown(false).setSource(file, blob);
                }
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                Log.d(TAG, "Loading file contents failed", e);

                ViewUtils.setGone(loadingBar, true);
                ViewUtils.setGone(codeView, false);
                ToastUtils.show(BranchFileViewActivity.this, e,
                    pw.bastiaan.github.R.string.error_file_load);
            }
        }.execute();
    }

}
