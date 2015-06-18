package info.tongrenlu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

import info.tongrenlu.component.AutoCompleteTextView;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements TextWatcher{

    public static final String TAG = LoginActivity.class.getName();

    public static final String AT_MARK = "@";
    private static final String[] DOMAINS = new String[] {
            "gmail.com", "163.com", "qq.com", "msn.com"
    };

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private TextInputLayout mEmailInputLayout;
    private TextInputLayout mPasswordInputLayout;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailInputLayout = (TextInputLayout) findViewById(R.id.email_layout);
        mEmailInputLayout.setErrorEnabled(true);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.password_layout);
        mPasswordInputLayout.setErrorEnabled(true);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    private void populateAutoComplete() {
        final ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        mEmailView.setAdapter(adapter);
        mEmailView.addTextChangedListener(this);
    }


        @Override
        public void beforeTextChanged(final CharSequence s,
        final int start,
        final int count,
        final int after) {

        }

        @Override
        public void onTextChanged(final CharSequence s,
        final int start,
        final int before,
        final int count) {

        }

        @Override
        public void afterTextChanged(final Editable s) {
            mEmailView.post(new Runnable() {
                @Override
                public void run() {
                    final String email = s.toString();
                    final ArrayAdapter<String> adapter = (ArrayAdapter) mEmailView.getAdapter();

                    adapter.setNotifyOnChange(false);
                    adapter.clear();

                    if (StringUtils.isNotBlank(email)) {
                        String[] tokens = StringUtils.split(email,AT_MARK,2);
                        String username = tokens[0];
                        String domain = "";
                        if (tokens.length == 2) {
                            domain = tokens[1];
                        }

                        if(StringUtils.isNotBlank(username)) {
                            for (String fullDomain : DOMAINS) {
                                if (StringUtils.isBlank(domain) || StringUtils.startsWith(fullDomain,domain)){
                                    adapter.add(username + AT_MARK + fullDomain);
                                }
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    adapter.getFilter().filter(s, mEmailView);
                }
            });
        }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
//        mEmailView.setError(null);
        mEmailInputLayout.setError(null);
//        mPasswordView.setError(null);
        mPasswordInputLayout.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordInputLayout.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailInputLayout.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailInputLayout.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate()
                          .setDuration(shortAnimTime)
                          .alpha(show ? 0 : 1)
                          .setListener(new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                  mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                              }
                          });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate()
                         .setDuration(shortAnimTime)
                         .alpha(show ? 1 : 0)
                         .setListener(new AnimatorListenerAdapter() {
                             @Override
                             public void onAnimationEnd(Animator animation) {
                                 mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                             }
                         });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }





    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final OkHttpClient mClient;
        private String mEmailError;
        private String mPasswordError;
        private String mError;
        public final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            mClient= new OkHttpClient();
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            mClient.setCookieHandler(cookieManager);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String salt = this.getSalt();

            if(StringUtils.isNotBlank(salt)) {
                String encPassword = md5Hex(md5Hex(mPassword) + salt);

                Map signInResult = doLogin(mEmail, encPassword);
                if(signInResult!= null) {
                    Boolean result = (Boolean) signInResult.get("result");
                    if (result) {



                        return true;
                    } else {
                        mEmailError = (String) signInResult.get("emailError");
                        mPasswordError = (String) signInResult.get("passwordError");
                        mError = (String) signInResult.get("error");
                    }
                }
            }
            return false;
        }

        private String md5Hex(String text) {
            return new String(Hex.encodeHex(DigestUtils.md5(text)));
        }

        private String getSalt() {
            // リクエストオブジェクトを作って
            Request request = new Request.Builder()
                    .url("http://www.tongrenlu.info/signin/salt")
                    .get()
                    .build();

            // リクエストして結果を受け取って
            try {
                Response response = mClient.newCall(request).execute();

                if(response.isSuccessful()) {
                    Map map = new Gson().fromJson(response.body().string(),HashMap.class);
                    String salt = (String) map.get("salt");
                    return salt;
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
        }

        private Map doLogin(String email, String password) {
            RequestBody body = new FormEncodingBuilder()
                    .add("email", email)
                    .add("password", password)
                    .add("autoLogin", "1")
                    .build();


            Request request = new Request.Builder()
                    .url("http://www.tongrenlu.info/signin")
                    .post(body)
                    .build();
            // リクエストして結果を受け取って
            try {
                Response response = mClient.newCall(request).execute();

                if(response.isSuccessful()) {
                    String responseString = response.body().string();
                    Log.d(TAG, responseString);

                    Map map = new Gson().fromJson(responseString,HashMap.class);
                    return map;
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {


                if(StringUtils.isNotBlank(mEmailError)) {
                    mEmailInputLayout.setError(mEmailError);
                }
                if(StringUtils.isNotBlank(mPasswordError)) {
                    mPasswordInputLayout.setError(mPasswordError);
                }


                if(StringUtils.isNotBlank(mError)) {
                    Snackbar.make(findViewById(R.id.login_form), mError, Snackbar.LENGTH_SHORT);
                }

                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

