package com.looseboxes.idisc.googleservicesextras;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.bc.android.core.util.Logx;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitation.IntentBuilder;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.looseboxes.idisc.common.App;
import com.bc.android.core.notice.Popup;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.io.IOException;

public class AppInviteActivity extends AppCompatActivity implements OnConnectionFailedListener, OnClickListener {
    private static final int REQUEST_INVITE = 0;
    private GoogleApiClient mGoogleApiClient;

    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.appinvite);
            findViewById(R.id.invite_button).setOnClickListener(this);
            this.mGoogleApiClient = new Builder(this).addApi(AppInvite.API).enableAutoManage(this, this).build();
            AppInvite.AppInviteApi.getInvitation(this.mGoogleApiClient, this, true).setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
                public void onResult(AppInviteInvitationResult result) {
                    Logx.getInstance().debug(getClass(), "getInvitation:onResult: {0}", result.getStatus());
                }
            });
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logx.getInstance().debug(getClass(), "onConnectionFailed: {0}", connectionResult);
        Popup.getInstance().show((Activity) this, getString(R.string.err_google_play_services), 0);
    }

    private void onInviteClicked() {
        try {
            Uri uri;
            PropertiesManager pm = App.getPropertiesManager(this);
            try {
                uri = Uri.parse(pm.getString(PropertyName.appInvitesUrl) + "?installationkey=" + App.getId(this));
            } catch (IOException e) {
                uri = Uri.parse(pm.getString(PropertyName.appServiceUrl));
            }
            startActivityForResult(new IntentBuilder(getString(R.string.msg_invite)).setMessage(getString(R.string.invitation_message)).setDeepLink(uri).setCustomImage(Uri.parse(pm.getString(PropertyName.appInvitesCustomImageUrl))).setCallToActionText(getString(R.string.invitation_cta)).build(), 0);
        } catch (Exception e2) {
            Logx.getInstance().log(getClass(), e2);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            Logx.getInstance().debug(getClass(), "onActivityResult requestCode: {0}, resultCode: {1}", Integer.valueOf(requestCode), Integer.valueOf(resultCode));
            if (requestCode != 0) {
                return;
            }
            if (resultCode == -1) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Object str = getString(R.string.fmt_sent_invitations, new Object[]{Integer.valueOf(ids.length)});
                Logx.getInstance().debug(getClass(), str);
                Popup.getInstance().show((Activity) this, str, 0);
                return;
            }
            Popup.getInstance().show((Activity) this, getString(R.string.err_sending_invitation), 0);
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.invite_button) {
            onInviteClicked();
        }
    }
}
