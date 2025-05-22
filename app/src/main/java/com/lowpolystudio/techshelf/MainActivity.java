package com.lowpolystudio.techshelf;

import android.Manifest;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseApp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class MainActivity extends AppCompatActivity {

    public interface Callback<T> {
        void onResult(T result);
    }

    public dbManager db_manager;
    public FirebaseFirestore db;
    public FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db_manager = new dbManager(this);
        db_manager.open();

        FirebaseApp.initializeApp(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101); // any request code
            }
        }

        db.collection("users").document(user.getUid())
                .collection("preferences").document("tags")
                .get().addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        startActivity(new Intent(this, PreferenceActivity.class));
                    }
                });

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_suggested) {
                selectedFragment = new SuggestedFragment();
            } else if (item.getItemId() == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (item.getItemId() == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (item.getItemId() == R.id.nav_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            SuggestedFragment fragment = new SuggestedFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }


        setDailyInactivityAlarm();
    }

    @SuppressLint("SetTextI18n")
    public void showBookInfoBottomSheet(int i) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.book_info);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        ImageView bookCover = bottomSheetDialog.findViewById(R.id.dialog_book_image);
        TextView bookTitle = bottomSheetDialog.findViewById(R.id.dialog_book_title);
        TextView bookAuthor = bottomSheetDialog.findViewById(R.id.dialog_book_author);
        TextView bookTags = bottomSheetDialog.findViewById(R.id.dialog_book_tags);
        TextView bookDescription = bottomSheetDialog.findViewById(R.id.dialog_book_desc);
        Button start_reading_btn = bottomSheetDialog.findViewById(R.id.start_reading_btn);
        ToggleButton favorite_btn = bottomSheetDialog.findViewById(R.id.favorite_button);

        assert bookCover != null;
        setBookCover(this, bookCover, db_manager.getBookCover(i));
        assert bookTitle != null;
        bookTitle.setText(db_manager.getBookField(i, "Title"));
        assert bookAuthor != null;
        bookAuthor.setText("by: " + db_manager.getBookField(i, "Author"));
        assert bookTags != null;
        bookTags.setText("tags: " + db_manager.getBookTags(i));
        assert bookDescription != null;
        bookDescription.setText(db_manager.getBookField(i, "Description"));

        bookCover.setOnClickListener(v -> {
            FullScreenImageDialog dialog =
                    FullScreenImageDialog.newInstance(db_manager.getBookCover(i));
            dialog.show(getSupportFragmentManager(), "full_screen_dialog");
        });

        DocumentReference favRef = db.collection("users")
                .document(user.getUid())
                .collection("favorites")
                .document(""+i);


        assert favorite_btn != null;
        favorite_btn.setOnCheckedChangeListener(null);

        favRef.get().addOnSuccessListener(doc -> favorite_btn.setChecked(doc.exists()));

        favorite_btn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", FieldValue.serverTimestamp());

                favRef.set(data)
                        .addOnSuccessListener(aVoid ->
                                Log.d("Favorites", "Added to favorites"))
                        .addOnFailureListener(e ->
                                Log.e("Favorites", "Failed to add", e));

            } else {
                favRef.delete()
                        .addOnSuccessListener(aVoid ->
                                Log.d("Favorites", "Removed from favorites"))
                        .addOnFailureListener(e ->
                                Log.e("Favorites", "Failed to remove", e));
            }
        });

        assert start_reading_btn != null;
        start_reading_btn.setOnClickListener(v -> {
            var file_name = db_manager.getBookField(i, "FileName");
                PdfViewerActivity.start(this, file_name, i);
        });

        bottomSheetDialog.show();
    }

    public void setBookCover(Context context, ImageView imageView, String imageName) {
        try {
            InputStream inputStream = context.getAssets().open("bookImgs/" + imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);
            inputStream.close();
        } catch (IOException e) {
            if (e.getCause() != null)
                imageView.setImageResource(R.drawable.book);
        }
    }

    public void getFavoriteBookIds(Callback<List<String>> callback) {
        if (user == null) {
            callback.onResult(Collections.emptyList());
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("favorites")
                .orderBy("timestamp", Query.Direction.DESCENDING)  // <-- sort by timestamp
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> favoriteBookIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        favoriteBookIds.add(doc.getId());
                    }
                    callback.onResult(favoriteBookIds);
                })
                .addOnFailureListener(e -> {
                    Log.e("Favorites", "Failed to fetch sorted favorites", e);
                    callback.onResult(Collections.emptyList());
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        long now = System.currentTimeMillis();
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putLong("last_open_time", now).apply();
    }

    private void setDailyInactivityAlarm() {
        Log.d("AlarmTest", "setDailyInactivityAlarm() called");

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, InactivityReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        long triggerAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000L;

        Log.d("AlarmTest", "Setting alarm for " + triggerAt);

        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Notifications", "Permission granted");
            } else {
                Log.d("Notifications", "Permission denied");
            }
        }
    }

    public void loadBooksByUserPreferences(Callback<List<Integer>> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        assert user != null;
        firestore.collection("users")
                .document(user.getUid())
                .collection("preferences")
                .document("tags")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> languages = (List<String>) doc.get("languages");
                        List<String> purposes = (List<String>) doc.get("purposes");

                        if (languages != null && purposes != null &&
                                !languages.isEmpty() && !purposes.isEmpty()) {

                            List<Integer> bookIds = db_manager.getBookIdsForPreferences(languages, purposes);
                            callback.onResult(bookIds);

                        } else {
                            Log.w("Preferences", "Empty preferences found");
                            callback.onResult(Collections.emptyList());
                        }
                    } else {
                        Log.w("Preferences", "No preferences found in Firestore");
                        callback.onResult(Collections.emptyList());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Preferences", "Failed to load preferences", e);
                    callback.onResult(Collections.emptyList());
                });
    }


    public interface FragmentDataListener {
        void onDataLoaded();
    }
}

