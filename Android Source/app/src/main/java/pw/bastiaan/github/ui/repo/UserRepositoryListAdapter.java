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

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import pw.bastiaan.github.ui.StyledText;
import pw.bastiaan.github.util.TypefaceUtils;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Adapter for a list of repositories
 */
public class UserRepositoryListAdapter extends
        RepositoryListAdapter<Repository> {

    private final String login;

    private int descriptionColor;

    /**
     * Create list adapter for repositories
     *
     * @param inflater
     * @param elements
     * @param user
     */
    public UserRepositoryListAdapter(LayoutInflater inflater,
            Repository[] elements, User user) {
        super(pw.bastiaan.github.R.layout.user_repo_item, inflater, elements);

        login = user.getLogin();
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getId();
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        TypefaceUtils.setOcticons(textView(view, 0),
                (TextView) view.findViewById(pw.bastiaan.github.R.id.tv_forks_icon),
                (TextView) view.findViewById(pw.bastiaan.github.R.id.tv_watchers_icon));
        descriptionColor = view.getResources().getColor(pw.bastiaan.github.R.color.text_description);
        return view;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { pw.bastiaan.github.R.id.tv_repo_icon, pw.bastiaan.github.R.id.tv_repo_description,
                pw.bastiaan.github.R.id.tv_language, pw.bastiaan.github.R.id.tv_watchers, pw.bastiaan.github.R.id.tv_forks,
                pw.bastiaan.github.R.id.tv_repo_name };
    }

    @Override
    protected void update(int position, Repository repository) {
        StyledText name = new StyledText();
        if (!login.equals(repository.getOwner().getLogin()))
            name.foreground(repository.getOwner().getLogin(), descriptionColor)
                    .foreground('/', descriptionColor);
        name.bold(repository.getName());
        setText(5, name);

        updateDetails(repository.getDescription(), repository.getLanguage(),
                repository.getWatchers(), repository.getForks(),
                repository.isPrivate(), repository.isFork(),
                repository.getMirrorUrl());
    }
}
