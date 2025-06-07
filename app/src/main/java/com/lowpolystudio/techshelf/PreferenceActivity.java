package com.lowpolystudio.techshelf;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PreferenceActivity extends AppCompatActivity {
    LinearLayout languageContainer, purposeContainer;
    List<CheckBox> languageChecks = new ArrayList<>();
    List<CheckBox> purposeChecks = new ArrayList<>();

    dbManager db_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        db_manager = new dbManager(this);
        db_manager.open();

        languageContainer = findViewById(R.id.language_container);
        purposeContainer = findViewById(R.id.purpose_container);
        Button btnSave = findViewById(R.id.btn_save_preferences);

        db_manager.loadTagsByType("language", languageContainer, languageChecks);
        db_manager.loadTagsByType("purpose", purposeContainer, purposeChecks);

        btnSave.setOnClickListener(v -> savePreferences());
    }



    private void savePreferences() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> languages = languageChecks.stream()
                .filter(CheckBox::isChecked)
                .map(cb -> cb.getText().toString())
                .collect(Collectors.toList());

        List<String> purposes = purposeChecks.stream()
                .filter(CheckBox::isChecked)
                .map(cb -> cb.getText().toString())
                .collect(Collectors.toList());

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("languages", languages);
        prefs.put("purposes", purposes);

        assert user != null;
        db.collection("users")
                .document(user.getUid())
                .collection("preferences")
                .document("tags")
                .set(prefs)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Preferences saved", Toast.LENGTH_SHORT).show();
                    finish(); // or start MainActivity
                })
                .addOnFailureListener(e ->
                        Log.e("Preferences", "Failed to save", e));
    }
}

