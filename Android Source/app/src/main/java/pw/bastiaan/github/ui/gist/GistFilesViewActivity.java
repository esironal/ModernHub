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

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static pw.bastiaan.github.Intents.EXTRA_GIST_ID;
import static pw.bastiaan.github.Intents.EXTRA_POSITION;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.github.kevinsawicki.wishlist.ViewUtils;
import pw.bastiaan.github.Intents.Builder;
import pw.bastiaan.github.core.gist.FullGist;
import pw.bastiaan.github.core.gist.GistStore;
import pw.bastiaan.github.core.gist.RefreshGistTask;
import pw.bastiaan.github.ui.FragmentProvider;
import pw.bastiaan.github.ui.PagerActivity;
import pw.bastiaan.github.ui.ViewPager;
import pw.bastiaan.github.util.AvatarLoader;
import pw.bastiaan.github.util.HttpImageGetter;
import com.google.inject.Inject;
import com.viewpagerindicator.TitlePageIndicator;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;

/**
 * Activity to page through the content of all the files in a Gist
 */
public class GistFilesViewActivity extends PagerActivity {

    /**
     * Create intent to show files with an initial selected file
     *
     * @param gist
     * @param position
     * @return intent
     */
    public static Intent createIntent(Gist gist, int position) {
        return new Builder("gist.files.VIEW").gist(gist.getId())
            .add(EXTRA_POSITION, position).toIntent();
    }

    private String gistId;

    private int initialPosition;

    private ViewPager pager;

    private ProgressBar loadingBar;

    private TitlePageIndicator indicator;

    private Gist gist;

    @Inject
    private GistStore store;

    @Inject
    private AvatarLoader avatars;

    @Inject
    private HttpImageGetter imageGetter;

    private GistFilesPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gistId = getStringExtra(EXTRA_GIST_ID);
        initialPosition = getIntExtra(EXTRA_POSITION);

        setContentView(pw.bastiaan.github.R.layout.pager_with_title);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(pw.bastiaan.github.R.id.toolbar));

        pager = finder.find(pw.bastiaan.github.R.id.vp_pages);
        loadingBar = finder.find(pw.bastiaan.github.R.id.pb_loading);
        indicator = finder.find(pw.bastiaan.github.R.id.tpi_header);

        if (initialPosition < 0)
            initialPosition = 0;

        getSupportActionBar().setTitle(getString(pw.bastiaan.github.R.string.gist_title) + gistId);

        gist = store.getGist(gistId);
        if (gist != null)
            configurePager();
        else {
            ViewUtils.setGone(loadingBar, false);
            ViewUtils.setGone(pager, true);
            ViewUtils.setGone(indicator, true);
            new RefreshGistTask(this, gistId, imageGetter) {

                @Override
                protected void onSuccess(FullGist gist) throws Exception {
                    super.onSuccess(gist);

                    GistFilesViewActivity.this.gist = gist.getGist();
                    configurePager();
                }

            }.execute();
        }
    }

    private void configurePager() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        User author = gist.getUser();
        if (author != null) {
            actionBar.setSubtitle(author.getLogin());
            avatars.bind(actionBar, author);
        } else
            actionBar.setSubtitle(pw.bastiaan.github.R.string.anonymous);

        ViewUtils.setGone(loadingBar, true);
        ViewUtils.setGone(pager, false);
        ViewUtils.setGone(indicator, false);

        adapter = new GistFilesPagerAdapter(this, gist);
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);

        if (initialPosition < adapter.getCount()) {
            pager.scheduleSetItem(initialPosition);
            onPageSelected(initialPosition);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (gist != null) {
                    Intent intent = GistsViewActivity.createIntent(gist);
                    intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP
                        | FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected FragmentProvider getProvider() {
        return adapter;
    }
}
