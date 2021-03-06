package com.microsoft.onedrive.apiexplorer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.microsoft.authenticate.AuthException;
import com.microsoft.authenticate.AuthListener;
import com.microsoft.authenticate.AuthSession;
import com.microsoft.authenticate.AuthStatus;

import java.util.Arrays;
import java.util.List;

/**
 * The sign in activity
 */
public class SignIn extends Activity {

    /**
     * The scopes used for this app
     */
    private static final List<String> SCOPES = Arrays.asList("wl.signin", "onedrive.readwrite");

    /**
     * The default auth listener for this class
     */
    private final AuthListener mAuthListener = new AuthListener() {
        @Override
        public void onAuthComplete(final AuthStatus status, final AuthSession session, final Object userState) {
            if (status == AuthStatus.CONNECTED) {
                afterSuccessfulSignIn();
            } else {
                findViewById(android.R.id.text1).setVisibility(View.INVISIBLE);
                findViewById(android.R.id.progress).setVisibility(View.INVISIBLE);
                Toast.makeText(SignIn.this,
                        getString(R.string.sign_in_failed, status.toString()),
                        Toast.LENGTH_LONG)
                        .show();
            }
        }

        @Override
        public void onAuthError(final AuthException exception, final Object userState) {
            findViewById(android.R.id.text1).setVisibility(View.INVISIBLE);
            findViewById(android.R.id.progress).setVisibility(View.INVISIBLE);
            Toast.makeText(SignIn.this,
                getString(R.string.sign_in_failed, exception.getMessage()),
                Toast.LENGTH_LONG)
            .show();
        }
    };

    /**
     * The actions that should be taken after a successful sign in
     */
    private void afterSuccessfulSignIn() {
        Toast.makeText(this, getString(R.string.signed_in), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        if (savedInstanceState == null) {
            final NotSignedInFragment notSignedInFragment = new NotSignedInFragment();
            notSignedInFragment.init(mAuthListener);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, notSignedInFragment)
                    .commit();
        }

        final BaseApplication baseApplication = (BaseApplication) getApplication();
        baseApplication.getAuthClient().initialize(new AuthListener() {
            @Override
            public void onAuthComplete(final AuthStatus status, final AuthSession session, final Object userState) {
                if (status == AuthStatus.CONNECTED) {
                    afterSuccessfulSignIn();
                } else {
                    baseApplication.getAuthClient().login(SignIn.this, SCOPES, mAuthListener);
                }
            }

            @Override
            public void onAuthError(final AuthException exception, final Object userState) {
                baseApplication.getAuthClient().login(SignIn.this, SCOPES, mAuthListener);
            }
        });
    }

    /**
     * The fragment to display when the user is not signed in
     */
    public static class NotSignedInFragment extends Fragment {

        /**
         * The auth listener for the inner fragment
         */
        private AuthListener mAuthListener;

        /**
         * Initializes this fragment
         * @param authListener The auth listener to use if the user presses sign in
         */
        public void init(final AuthListener authListener) {
            mAuthListener = authListener;
        }

        @Override
        public View onCreateView(final LayoutInflater inflater,
                                 final ViewGroup container,
                                 final Bundle savedInstanceState) {
            final View signInFragment = inflater.inflate(R.layout.fragment_sign_in, container, false);

            final Button signIn = (Button) signInFragment.findViewById(R.id.sign_in);
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final Activity activity = getActivity();
                    final BaseApplication baseApplication = (BaseApplication) activity.getApplication();
                    baseApplication.getAuthClient().login(getActivity(), SCOPES, mAuthListener);
                }
            });
            return signInFragment;
        }
    }
}
