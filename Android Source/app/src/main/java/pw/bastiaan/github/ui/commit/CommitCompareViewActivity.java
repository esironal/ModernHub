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
package pw.bastiaan.github.ui.commit;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import pw.bastiaan.github.ui.DialogFragmentActivity;
import pw.bastiaan.github.ui.repo.RepositoryViewActivity;
import pw.bastiaan.github.util.AvatarLoader;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Repository;

import pw.bastiaan.github.Intents;

/**
 * Activity to display a comparison between two commits
 */
public class CommitCompareViewActivity extends DialogFragmentActivity {

    /**
     * Create intent for this activity
     *
     * @param repository
     * @param base
     * @param head
     * @return intent
     */
    public static Intent createIntent(final Repository repository,
        final String base, final String head) {
        Intents.Builder builder = new Intents.Builder("commits.compare.VIEW");
        builder.add(Intents.EXTRA_BASE, base);
        builder.add(Intents.EXTRA_HEAD, head);
        builder.repo(repository);
        return builder.toIntent();
    }

    private Repository repository;

    @Inject
    private AvatarLoader avatars;

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = getSerializableExtra(Intents.EXTRA_REPOSITORY);

        setContentView(pw.bastiaan.github.R.layout.commit_compare);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(pw.bastiaan.github.R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setSubtitle(repository.generateId());
        avatars.bind(actionBar, repository.getOwner());

        fragment = getSupportFragmentManager()
            .findFragmentById(android.R.id.list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionsMenu) {
        if (fragment != null)
            fragment.onCreateOptionsMenu(optionsMenu, getMenuInflater());

        return super.onCreateOptionsMenu(optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = RepositoryViewActivity.createIntent(repository);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                if (fragment != null)
                    return fragment.onOptionsItemSelected(item);
                else
                    return super.onOptionsItemSelected(item);
        }
    }
}
