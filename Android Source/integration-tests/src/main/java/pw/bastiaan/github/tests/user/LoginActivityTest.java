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
package pw.bastiaan.github.tests.user;

import android.accounts.AccountManager;
import android.view.View;
import android.widget.EditText;

import pw.bastiaan.github.accounts.AccountUtils;
import pw.bastiaan.github.accounts.LoginActivity;
import pw.bastiaan.github.tests.ActivityTest;

/**
 * Tests of {@link pw.bastiaan.github.accounts.LoginActivity}
 */
public class LoginActivityTest extends ActivityTest<LoginActivity> {

    /**
     * Create navigation_drawer_header_background for {@link LoginActivity}
     */
    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    /**
     * Verify authenticator is registered
     */
    public void testHasAuthenticator() {
        assertTrue(AccountUtils.hasAuthenticator(AccountManager
            .get(getActivity())));
    }

    /**
     * Verify activity was created successfully
     *
     * @throws Throwable
     */
    public void testSignInIsDisabled() throws Throwable {
        View loginMenu = view(pw.bastiaan.github.R.id.m_login);
        assertFalse(loginMenu.isEnabled());
        final EditText login = editText(pw.bastiaan.github.R.id.et_login);
        final EditText password = editText(pw.bastiaan.github.R.id.et_password);
        focus(login);
        send("loginname");
        assertEquals("loginname", login.getText().toString());
        assertFalse(loginMenu.isEnabled());
        focus(password);
        send("password");
        assertEquals("password", password.getText().toString());
        assertTrue(loginMenu.isEnabled());
    }
}
