package com.lowpolystudio.techshelf;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        auth = FirebaseAuth.getInstance();

        TextView emailTextView = view.findViewById(R.id.emailTextView);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        Button prefChangeButton = view.findViewById(R.id.pref_change_button);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            emailTextView.setText("Logged in as:\n" + user.getEmail());
        }

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        prefChangeButton.setOnClickListener(v -> startActivity(new Intent(requireActivity(), PreferenceActivity.class)));

        return view;
    }
}
