package com.looseboxes.idisc.googleservicesextras;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.looseboxes.idisc.common.util.Logx;

public class DeepLinkActivity extends AppCompatActivity implements OnClickListener {
    public static final String EXTRA_STRING_DEEPLINK;
    public static final String EXTRA_STRING_INVITATIONID;

    static {
        EXTRA_STRING_INVITATIONID = DeepLinkActivity.class.getName() + ".invitaionID";
        EXTRA_STRING_DEEPLINK = DeepLinkActivity.class.getName() + ".deepLink";
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deep_link);
        findViewById(R.id.deep_link_ok_button).setOnClickListener(this);
    }

    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        processReferralIntent(intent.getStringExtra(EXTRA_STRING_INVITATIONID), intent.getStringExtra(EXTRA_STRING_DEEPLINK));
    }

    private void processReferralIntent(String invitationId, String deepLink) {
        try {
            Logx.debug(getClass(), "Found: Referral: InvitationID: {0}, DeepLink: {1}", invitationId, deepLink);
            if (deepLink != null) {
                if (invitationId != null) {
                    ((TextView) findViewById(R.id.invitation_id_text)).setText(getString(R.string.fmt_invitation_id, new Object[]{invitationId}));
                }
            } else if (invitationId != null) {
                ((TextView) findViewById(R.id.invitation_id_text)).setText(getString(R.string.fmt_invitation_id, new Object[]{invitationId}));
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.deep_link_ok_button) {
            finish();
        }
    }
}
