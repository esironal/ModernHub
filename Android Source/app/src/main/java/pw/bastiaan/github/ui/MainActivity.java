package pw.bastiaan.github.ui;

import static pw.bastiaan.github.ui.NavigationDrawerObject.TYPE_SEPERATOR;
import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import pw.bastiaan.github.R;
import pw.bastiaan.github.accounts.AccountUtils;
import pw.bastiaan.github.core.user.UserComparator;
import pw.bastiaan.github.persistence.AccountDataManager;
import pw.bastiaan.github.ui.gist.GistsPagerFragment;
import pw.bastiaan.github.ui.issue.FilterListFragment;
import pw.bastiaan.github.ui.issue.IssueDashboardPagerFragment;
import pw.bastiaan.github.ui.repo.OrganizationLoader;
import pw.bastiaan.github.ui.user.HomePagerFragment;
import pw.bastiaan.github.util.AvatarLoader;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.User;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
    LoaderManager.LoaderCallbacks<List<User>> {

    private static final String TAG = "MainActivity";

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Inject
    private AccountDataManager accountDataManager;

    @Inject
    private Provider<UserComparator> userComparatorProvider;

    private List<User> orgs = Collections.emptyList();

    private NavigationDrawerAdapter navigationAdapter;

    private User org;

    @Inject
    private AvatarLoader avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        getSupportLoaderManager().initLoader(0, null, this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
            getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    }

    private void reloadOrgs() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getMenuInflater().inflate(R.menu.home, optionMenu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuItem searchItem = optionMenu.findItem(R.id.m_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(optionMenu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Restart loader if default account doesn't match currently loaded
        // account
        List<User> currentOrgs = orgs;
        if (currentOrgs != null && !currentOrgs.isEmpty()
            && !AccountUtils.isUser(this, currentOrgs.get(0)))
            reloadOrgs();
    }

    @Override
    public Loader<List<User>> onCreateLoader(int i, Bundle bundle) {
        return new OrganizationLoader(this, accountDataManager,
            userComparatorProvider);
    }

    @Override
    public void onLoadFinished(Loader<List<User>> listLoader, final List<User> orgs) {
        if (orgs.isEmpty())
            return;

        org = orgs.get(0);
        this.orgs = orgs;

        if (navigationAdapter != null)
            navigationAdapter.setOrgs(orgs);
        else {
            navigationAdapter = new NavigationDrawerAdapter(MainActivity.this, orgs, avatars);
            mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), navigationAdapter, avatars, org);

            Window window = getWindow();
            if (window == null)
                return;
            View view = window.getDecorView();
            if (view == null)
                return;

            view.post(new Runnable() {

                @Override
                public void run() {
                    MainActivity.this.onNavigationDrawerItemSelected(0);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<List<User>> listLoader) {

    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (navigationAdapter.getItem(position).getType() == TYPE_SEPERATOR)
            return;
        Fragment fragmet;
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                fragmet = new HomePagerFragment();
                args.putSerializable("org", org);
                break;
            case 1:
                fragmet = new GistsPagerFragment();
                break;
            case 2:
                fragmet = new IssueDashboardPagerFragment();
                break;
            case 3:
                fragmet = new FilterListFragment();
                break;
            default:
                fragmet = new HomePagerFragment();
                args.putSerializable("org", orgs.get(position - 5));
                break;
        }
        fragmet.setArguments(args);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, fragmet).commit();
    }

}
