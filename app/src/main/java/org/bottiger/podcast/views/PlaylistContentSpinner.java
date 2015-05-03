package org.bottiger.podcast.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.bottiger.podcast.R;
import org.bottiger.podcast.playlist.Playlist;
import org.bottiger.podcast.playlist.filters.SubscriptionFilter;
import org.bottiger.podcast.provider.Subscription;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by apl on 19-02-2015.
 */
public class PlaylistContentSpinner  implements DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnCancelListener {

    private Context mContext;
    private AlertDialog mAlertDialog;

    private Playlist mPlaylist;
    private SubscriptionFilter mSubscriptionFilter;

    private ArrayAdapter<String> mAdapter;
    private List<Subscription> mSubscriptions = new LinkedList<>();
    private List<CheckBox> mCheckboxes = new LinkedList<>();

    private RadioGroup mRadioGroup;

    private String mSpinnerPrefix;
    private String mShownAll;
    private String mShownSome;

    private static boolean modifyingState = false;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (modifyingState)
                return;

            modifyingState = true;
            setRadioButtonState();
            modifyingState = false;
        }
    };
    private RadioGroup.OnCheckedChangeListener RadioOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (modifyingState)
                return;

            modifyingState = true;
            switch (checkedId) {
                case R.id.radioNone:
                    checkNone();
                    break;
                case R.id.radioAll:
                    checkAll();
                    break;
                case R.id.radioCustom:
                    checkCustom();
                    break;
            }

            modifyingState = false;
        }
    };

    public PlaylistContentSpinner(Context context) {
        init(context);
    }

    private void init(Context argContext) {
        mContext = argContext;

        mSpinnerPrefix = mContext.getResources().getString(R.string.playlist_filter_prefix);
        mShownAll = mContext.getResources().getString(R.string.playlist_filter_all);
        mShownSome = mContext.getResources().getString(R.string.playlist_filter_some);
    }

    /**
     * The Opening the Dialog
     * @return
     */
    public boolean performClick() {
        mSubscriptions.clear();
        List<Subscription> list = Subscription.allAsList(mContext.getContentResolver());
        for (Subscription s : list) {
            if (s.getStatus() == Subscription.STATUS_SUBSCRIBED)
                mSubscriptions.add(s);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View view = inflater.inflate(R.layout.filter_subscriptions, null);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.filter_subscription_checkboxes);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.subscription_selection_type);
        mRadioGroup.setOnCheckedChangeListener(RadioOnCheckedChangeListener);

        mCheckboxes.clear();
        for (Subscription subscription : mSubscriptions) {
            View itemView = inflater.inflate(R.layout.filter_subscriptions_item, null);
            TextView tv = (TextView) itemView.findViewById(R.id.item_text);
            final CheckBox checkBox = (CheckBox) itemView.findViewById(R.id.item_checkbox);
            tv.setText(subscription.getTitle());

            if (mSubscriptionFilter.isShown(subscription.getId()))
                checkBox.setChecked(true);

            checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });

            linearLayout.addView(itemView);

            mCheckboxes.add(checkBox);
        }

        setRadioButtonState();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        // Add action buttons
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (mRadioGroup.getCheckedRadioButtonId()) {
                            case R.id.radioNone:
                                mSubscriptionFilter.setMode(SubscriptionFilter.MODE.SHOW_NONE, getContext());
                                break;
                            case R.id.radioAll:
                                mSubscriptionFilter.setMode(SubscriptionFilter.MODE.SHOW_ALL, getContext());
                                break;
                            case R.id.radioCustom: {
                                mSubscriptionFilter.clear();
                                for (int i = 0; i < mCheckboxes.size(); i++) {
                                    CheckBox checkbox = mCheckboxes.get(i);
                                    if (checkbox.isChecked()) {
                                        Subscription subscription = mSubscriptions.get(i);
                                        mSubscriptionFilter.add(subscription.getId());
                                    }
                                }
                                mSubscriptionFilter.setMode(SubscriptionFilter.MODE.SHOW_SELECTED, getContext());
                                break;
                            }
                        }

                        mPlaylist.notifyPlaylistChanged();
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.setOnCancelListener(this);

        mAlertDialog = builder.show();
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
    }

    @Override
    public void onCancel(DialogInterface dialog) {
    }

    public void setSubscriptions(Playlist argPlaylist) {
        mPlaylist = argPlaylist;
        mSubscriptionFilter = argPlaylist.getSubscriptionFilter();
    }

    private void checkAll() {
        for (CheckBox checkBox : mCheckboxes) {
            checkBox.setChecked(true);
        }
    }

    private void checkNone() {
        for (CheckBox checkBox : mCheckboxes) {
            checkBox.setChecked(false);
        }
    }

    private void checkCustom() {

    }

    private void setRadioButtonState() {
        boolean anyChecked = false;
        boolean anyUnchecked = false;
        for (CheckBox checkBox : mCheckboxes) {
            if (checkBox.isChecked()) {
                anyChecked = true;
            } else {
                anyUnchecked = true;
            }
        }

        if (anyChecked && anyUnchecked) {
            mRadioGroup.check(R.id.radioCustom);
            return;
        }

        if (anyChecked) {
            mRadioGroup.check(R.id.radioAll);
            return;
        }

        if (anyUnchecked) {
            mRadioGroup.check(R.id.radioNone);
            return;
        }
    }

    private Context getContext() {
        return mContext;
    }
}
