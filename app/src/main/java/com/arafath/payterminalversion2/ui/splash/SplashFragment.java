package com.arafath.payterminalversion2.ui.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arafath.payterminalversion2.R;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Branded splash. The session gate in MainActivity owns navigation, so this
 * screen simply renders the PayTerminal identity while the gate resolves
 * LOADING into LOGGED_OUT / NEEDS_TERMINAL / READY.
 */
@AndroidEntryPoint
public class SplashFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }
}