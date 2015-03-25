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
package pw.bastiaan.github.ui.repo;

import android.app.Activity;

import pw.bastiaan.github.core.ResourcePager;
import pw.bastiaan.github.ui.NewsFragment;
import pw.bastiaan.github.ui.issue.IssuesViewActivity;
import pw.bastiaan.github.ui.user.EventPager;
import pw.bastiaan.github.ui.user.UserViewActivity;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;

import pw.bastiaan.github.Intents;
import pw.bastiaan.github.core.user.UserEventMatcher;

/**
 * Fragment to display a news feed for a specific repository
 */
public class RepositoryNewsFragment extends NewsFragment {

    private Repository repo;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        repo = getSerializableExtra(Intents.EXTRA_REPOSITORY);
    }

    @Override
    protected ResourcePager<Event> createPager() {
        return new EventPager() {

            @Override
            public PageIterator<Event> createIterator(int page, int size) {
                return service.pageEvents(repo, page, size);
            }
        };
    }

    /**
     * Start an activity to view the given repository
     *
     * @param repository
     */
    @Override
    protected void viewRepository(Repository repository) {
        if (!repo.generateId().equals(repository.generateId()))
            super.viewRepository(repository);
    }

    @Override
    protected void viewIssue(Issue issue, Repository repository) {
        startActivity(IssuesViewActivity.createIntent(issue, repo));
    }

    @Override
    protected boolean viewUser(User user) {
        if (repo.getOwner().getId() != user.getId()) {
            startActivity(UserViewActivity.createIntent(user));
            return true;
        }
        return false;
    }

    @Override
    protected void viewUser(UserEventMatcher.UserPair users) {
        if (!viewUser(users.from))
            viewUser(users.to);
    }
}
