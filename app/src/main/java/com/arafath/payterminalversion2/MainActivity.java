package com.arafath.payterminalversion2;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.arafath.payterminalversion2.ui.session.SessionViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Single-Activity host. Observes the session gate (logged out / needs terminal /
 * ready) and navigates to the correct destination, so login, pairing and logout
 * all converge here instead of living in each screen.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        if (navHost == null) {
            return;
        }
        navController = navHost.getNavController();

        SessionViewModel sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        sessionViewModel.getState().observe(this, this::onSessionState);
    }

    private void onSessionState(SessionViewModel.State state) {
        switch (state) {
            case NEEDS_TERMINAL:
                navigateTo(R.id.pairTerminalFragment);
                break;
            case READY:
                navigateTo(R.id.homeFragment);
                break;
            default:
                // LOADING and LOGGED_OUT both resolve to the login screen.
                navigateTo(R.id.loginFragment);
                break;
        }
    }

    private void navigateTo(int destination) {
        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() == destination) {
            return;
        }
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(navController.getGraph().getStartDestinationId(), true)
                .setLaunchSingleTop(true)
                .build();
        navController.navigate(destination, null, options);
    }
}