package com.looseboxes.idisc.common.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public interface FragmentHandler {
    Context getContext();

    Fragment getFragment();

    void update(Intent intent, boolean z);

    void update(Bundle bundle, boolean z);
}
