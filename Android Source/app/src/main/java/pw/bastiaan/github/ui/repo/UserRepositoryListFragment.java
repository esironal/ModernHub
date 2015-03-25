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

import static pw.bastiaan.github.ResultCodes.RESOURCE_CHANGED;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import pw.bastiaan.github.core.ResourcePager;
import pw.bastiaan.github.ui.PagedItemFragment;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;

import pw.bastiaan.github.Intents;
import pw.bastiaan.github.RequestCodes;

/**
 * Fragment to display a list of repositories for a {@link User}
 */
public class UserRepositoryListFragment extends PagedItemFragment<Repository> {

    @Inject
    private RepositoryService service;

    private User user;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        user = getSerializableExtra(Intents.EXTRA_USER);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(pw.bastiaan.github.R.string.no_repositories);
    }

    @Override
    protected ResourcePager<Repository> createPager() {
        return new ResourcePager<Repository>() {

            @Override
            protected Object getId(Repository resource) {
                return resource.getId();
            }

            @Override
            public PageIterator<Repository> createIterator(int page, int size) {
                return service.pageRepositories(user.getLogin(), page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return pw.bastiaan.github.R.string.loading_repositories;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return pw.bastiaan.github.R.string.error_repos_load;
    }

    @Override
    protected SingleTypeAdapter<Repository> createAdapter(List<Repository> items) {
        return new UserRepositoryListAdapter(getActivity().getLayoutInflater(),
                items.toArray(new Repository[items.size()]), user);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.REPOSITORY_VIEW && resultCode == RESOURCE_CHANGED) {
            forceRefresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Repository repo = (Repository) list.getItemAtPosition(position);
        startActivityForResult(RepositoryViewActivity.createIntent(repo),
                RequestCodes.REPOSITORY_VIEW);
    }
}
