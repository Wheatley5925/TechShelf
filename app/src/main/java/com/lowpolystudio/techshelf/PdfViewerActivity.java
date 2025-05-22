package com.lowpolystudio.techshelf;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PdfViewerActivity extends AppCompatActivity {

    public FirebaseFirestore db;
    public FirebaseUser user;
    private static String PDF_FILE_NAME = "C_Sharp_Programming.pdf";
    private static PDFView pdfView;
    private static int bookId = 0;
    private static int currentPage = 0;
    private int totalPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        setContentView(R.layout.activity_pdf_renderer);

        pdfView = findViewById(R.id.pdfView);
        Button btnExit = findViewById(R.id.btnExit);
        Button btnBookmark = findViewById(R.id.btnBookmark);
        Button btnPage = findViewById(R.id.btnPage);

        try {
            File file = getFileFromAssets(this, "books/" + PDF_FILE_NAME);
            openPdf(file);
        } catch (IOException e) {
            Log.e("FILE_ERROR", PDF_FILE_NAME + " could not be opened");
        }

        btnExit.setOnClickListener(v -> exitBookView());
        btnBookmark.setOnClickListener(v -> showBookmarkPopup());
        btnPage.setOnClickListener(v -> showPageInputDialog());

    }

    private void openPdf(File file) {
        pdfView.fromFile(file)
                .defaultPage(currentPage)
                .enableSwipe(true) // Disable scrolling
                .swipeHorizontal(false) // Horizontal navigation
                .enableDoubletap(true)
                .enableAntialiasing(true)
                .enableAnnotationRendering(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .onLoad(nbPages -> totalPages = nbPages)
                .load();
    }

    public void saveBookProgress(int bookId, int pageNumber, int pagesTotal) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("FirestoreTest", "No user logged in.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("bookId", bookId);
        data.put("lastPage", pageNumber);
        data.put("pageCount", pagesTotal);
        data.put("timestamp", new Timestamp(new Date()));

        db.collection("users")
                .document(user.getUid())
                .collection("history")
                .document(""+bookId)
                .set(data)
                .addOnSuccessListener(aVoid ->
                        Log.d("FirestoreTest", "Book progress saved successfully."))
                .addOnFailureListener(e ->
                        Log.e("FirestoreTest", "Failed to save book progress.", e));
    }


    private void exitBookView() {
        saveBookProgress(bookId, pdfView.getCurrentPage(), totalPages);
        finish();
    }

    private void showPageInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String a = "Go to page (" + totalPages + " max)";
        builder.setTitle(a);

        // Input field (Only numbers)
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        // "Go" Button
        builder.setPositiveButton("Go", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                int selectedPage = Integer.parseInt(inputText) - 1; // Convert to 0-based index
                if (selectedPage >= 0 && selectedPage < totalPages) {
                    currentPage = selectedPage;
                    pdfView.jumpTo(currentPage, true);
                } else {
                    Toast.makeText(this, "Invalid page number!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private File getFileFromAssets(Context context, String assetFileName) throws IOException {
        File cacheDir = context.getCacheDir();
        File outFile = new File(cacheDir, assetFileName.substring(assetFileName.lastIndexOf("/") + 1));

        if (outFile.exists()) {
            return outFile;
        }

        InputStream inputStream = context.getAssets().open(assetFileName);
        FileOutputStream outputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return outFile;
    }

    public static void start(Context context, String file_name, int id) {
        Intent intent = new Intent(context, PdfViewerActivity.class);
        context.startActivity(intent);
        PDF_FILE_NAME = file_name;
        bookId = id;

        getLastPageIfInHistory(""+id, result -> {
            if (result != null)
                pdfView.jumpTo(result, true);
        });
    }

    public static void getLastPageIfInHistory(String bookId, MainActivity.Callback<Integer> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onResult(null);
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("history")
                .document(bookId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long lastPage = documentSnapshot.getLong("lastPage");
                        callback.onResult(lastPage != null ? lastPage.intValue() : null);
                    } else {
                        callback.onResult(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreCheck", "Failed to check history", e);
                    callback.onResult(null);
                });
    }

    public void showBookmarkPopup() {
        @SuppressLint("InflateParams") View popupView = LayoutInflater.from(this).inflate(R.layout.dialog_bookmarks, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setElevation(10);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        FloatingActionButton bookmarkAdd = popupView.findViewById(R.id.bookmark_add_button);

        bookmarkAdd.setOnClickListener(v -> addBookmark(pdfView.getCurrentPage(), popupView, popupWindow));

        loadBookmarksInto(popupView, popupWindow);
    }

    private void loadBookmarksInto(View popupView, PopupWindow popupWindow) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String bookId = "" + PdfViewerActivity.bookId; // get this from your context

        LinearLayout container = popupView.findViewById(R.id.bookmark_list_container);

        container.removeAllViews();
        assert user != null;
        db.collection("users")
                .document(user.getUid())
                .collection("bookmarks")
                .document(bookId)
                .collection("pages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        Long page = doc.getLong("page");
                        Timestamp timestamp = doc.getTimestamp("timestamp");

                        if (page == null || timestamp == null) continue;

                        @SuppressLint("InflateParams") View bookmarkView = LayoutInflater.from(this).inflate(R.layout.bookmark_item, null);
                        TextView bookmarkText = bookmarkView.findViewById(R.id.bookmark_text);
                        ImageButton bookmarkDelete = bookmarkView.findViewById(R.id.delete_button);

                        bookmarkText.setText("Page " + (page + 1) + " — " +
                                android.text.format.DateFormat.format("dd MMM yyyy HH:mm",
                                        timestamp.toDate()));
                        bookmarkText.setPadding(8, 16, 8, 16);
                        bookmarkView.setClickable(true);
                        bookmarkView.setBackgroundResource(android.R.drawable.list_selector_background);

                        int goToPage = page.intValue();
                        bookmarkView.setOnClickListener(v -> {
                            pdfView.jumpTo(goToPage, true); // Your existing method
                            popupWindow.dismiss();
                        });

                        bookmarkDelete.setOnClickListener(v -> {
                            db.collection("users")
                                    .document(user.getUid())
                                    .collection("bookmarks")
                                    .document(bookId)
                                    .collection("pages")
                                    .document(doc.getId())
                                    .delete();
                            container.removeView(bookmarkView);
                        });

                        container.addView(bookmarkView);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Bookmarks", "Failed to load bookmarks", e));
    }

    private void addBookmark(int page, View popupView, PopupWindow popupWindow) {
        LinearLayout container = popupView.findViewById(R.id.bookmark_list_container);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FieldValue time = FieldValue.serverTimestamp();

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("timestamp", time);

        @SuppressLint("InflateParams") View bookmarkView = LayoutInflater.from(this).inflate(R.layout.bookmark_item, null);

        assert user != null;
        db.collection("users")
                .document(user.getUid())
                .collection("bookmarks")
                .document("" + bookId)
                .collection("pages")
                .add(data)
                .addOnSuccessListener(r -> {
                    r.get().addOnSuccessListener(s -> {
                        Timestamp timestamp = s.getTimestamp("timestamp");

                        TextView bookmarkText = bookmarkView.findViewById(R.id.bookmark_text);
                        ImageButton bookmarkDelete = bookmarkView.findViewById(R.id.delete_button);

                        assert timestamp != null;
                        bookmarkText.setText("Page " + (page + 1) + " — " +
                                android.text.format.DateFormat.format("dd MMM yyyy HH:mm",
                                        timestamp.toDate()));
                        bookmarkText.setPadding(8, 16, 8, 16);
                        bookmarkView.setClickable(true);
                        bookmarkView.setBackgroundResource(android.R.drawable.list_selector_background);

                        bookmarkView.setOnClickListener(v -> {
                            pdfView.jumpTo(page, true); // Your existing method
                            popupWindow.dismiss();
                        });

                        bookmarkDelete.setOnClickListener(v -> {
                            db.collection("users")
                                    .document(user.getUid())
                                    .collection("bookmarks")
                                    .document(""+bookId)
                                    .collection("pages")
                                    .document(r.getId())
                                    .delete();
                            container.removeView(bookmarkView);
                        });
                    });

                    container.addView(bookmarkView);
                })
                .addOnFailureListener(e -> Log.e("Bookmark", "Failed", e));



    }

}
