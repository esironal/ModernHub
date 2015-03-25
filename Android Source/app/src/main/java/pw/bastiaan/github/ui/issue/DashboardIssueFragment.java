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
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import pw.bastiaan.github.core.ResourcePager;
import pw.bastiaan.github.core.issue.IssueStore;
import pw.bastiaan.github.ui.PagedItemFragment;
import pw.bastiaan.github.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;

import pw.bastiaan.github.RequestCodes;

/**
 * Fragment to display a pageable list of dashboard issues
 */
public class DashboardIssueFragment extends PagedItemFragment<RepositoryIssue> {

    /**
     * Filter data argument
     */
    public static final String ARG_FILTER = "filter";

    @Inject
    private IssueService service;

    @Inject
    private IssueStore store;

    @Inject
    private AvatarLoader avatars;

    private Map<String, String> filterData;

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        filterData = (Map<String, String>) getArguments().getSerializable(
                ARG_FILTER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.ISSUE_VIEW) {
            notifyDataSetChanged();
            forceRefresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivityForResult(
                IssuesViewActivity.createIntent(items, position
                        - getListAdapter().getHeadersCount()), RequestCodes.ISSUE_VIEW);
    }

    @Override
    protected ResourcePager<RepositoryIssue> createPager() {
        return new ResourcePager<RepositoryIssue>() {

            @Override
            protected RepositoryIssue register(RepositoryIssue resource) {
                return store.addIssue(resource);
            }

            @Override
            protected Object getId(RepositoryIssue resource) {
                return resource.getId();
            }

            @Override
            public PageIterator<RepositoryIssue> createIterator(int page,
                    int size) {
                return service.pageIssues(filterData, page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return pw.bastiaan.github.R.string.loading_issues;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return pw.bastiaan.github.R.string.error_issues_load;
    }

    @Override
    protected SingleTypeAdapter<RepositoryIssue> createAdapter(
            List<RepositoryIssue> items) {
        return new DashboardIssueListAdapter(avatars, getActivity()
                .getLayoutInflater(), items.toArray(new RepositoryIssue[items
                .size()]));
    }
}
