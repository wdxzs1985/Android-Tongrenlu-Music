package info.tongrenlu.component;

import android.content.Context;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import org.apache.commons.lang3.StringUtils;

/**
 * TODO: document your custom view class.
 */
public class AutoCompleteEmailView extends AppCompatAutoCompleteTextView implements TextWatcher {

    public static final String AT_MARK = "@";
    private static final String[] DOMAINS = new String[]{ "gmail.com",
                                                          "163.com",
                                                          "qq.com",
                                                          "msn.com" };

    public AutoCompleteEmailView(Context context) {
        super(context);
    }

    public AutoCompleteEmailView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompleteEmailView(final Context context,
                                 final AttributeSet attrs,
                                 final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        // nothing, block the default auto complete behavior
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
        final AutoCompleteEmailView autoComplete = this;
        autoComplete.post(new Runnable() {
            @Override
            public void run() {
                final String email = s.toString();
                final ArrayAdapter<String> adapter = (ArrayAdapter) autoComplete.getAdapter();

                adapter.setNotifyOnChange(false);
                adapter.clear();

                if (StringUtils.isNotBlank(email)) {
                    String[] tokens = StringUtils.split(email, AT_MARK, 2);
                    String username = tokens[0];
                    String domain = "";
                    if (tokens.length == 2) {
                        domain = tokens[1];
                    }

                    if (StringUtils.isNotBlank(username)) {
                        for (String fullDomain : DOMAINS) {
                            if (StringUtils.isBlank(domain) ||
                                StringUtils.startsWith(fullDomain, domain)) {
                                adapter.add(username + AT_MARK + fullDomain);
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                adapter.getFilter().filter(s, autoComplete);
            }
        });
    }


}
