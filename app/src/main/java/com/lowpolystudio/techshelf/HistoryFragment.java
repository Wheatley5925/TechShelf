package com.lowpolystudio.techshelf;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

public class HistoryFragment extends Fragment {

    private LinearLayout historyContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.history_fragment, container, false);
        historyContainer = view.findViewById(R.id.historyContainer);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onDataLoaded();  // Вызов после создания View
    }

    // Метод для добавления книг в LinearLayout
    public void onDataLoaded() {
        if (getView() == null || historyContainer == null) return;

        ((MainActivity) requireActivity()).db.collection("users")
                .document(((MainActivity) requireActivity()).user.getUid())
                .collection("history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyContainer.removeAllViews();
                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView emptiness = new TextView(getActivity());
                        emptiness.setText("Start reading to have reading history 😉");
                        emptiness.setGravity(17);

                        historyContainer.addView(emptiness);
                    }
                    else {

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Long bookId = doc.getLong("bookId");
                            Long lastPage = doc.getLong("lastPage");
                            Long pageCount = doc.getLong("pageCount");
                            Timestamp timestamp = doc.getTimestamp("timestamp");

                            // Skip if null
                            if (lastPage == null || timestamp == null) continue;

                            int bookIndex = bookId.intValue();

                            View listItem = LayoutInflater.from(getContext())
                                    .inflate(R.layout.history_item, historyContainer, false);

                            ImageView imageView = listItem.findViewById(R.id.item_image);
                            TextView textTitle = listItem.findViewById(R.id.item_title);
                            TextView textLastOpened = listItem.findViewById(R.id.item_date);
                            ProgressBar progressBar = listItem.findViewById(R.id.progressMeter);
                            TextView textProgress = listItem.findViewById(R.id.progressText);

                            String title = ((MainActivity) requireActivity()).db_manager.getBookField(bookIndex, "Title");

                            textTitle.setText(title);
                            textLastOpened.setText("Last opened: " +
                                    android.text.format.DateFormat.format("dd MMM yyyy HH:mm", timestamp.toDate()));

                            int lastPageInt = lastPage.intValue();
                            int progress = (int) ((lastPageInt / (float) pageCount) * 100);

                            progressBar.setProgress(progress, true);
                            textProgress.setText(lastPageInt + 1 + " / " + pageCount + "  ");

                            ((MainActivity) requireActivity()).setBookCover(getContext(), imageView,
                                    ((MainActivity) requireActivity()).db_manager.getBookCover(bookIndex));

                            listItem.setOnClickListener(v -> {
                                var file_name = ((MainActivity) requireActivity()).db_manager.getBookField(bookIndex, "FileName");
                                PdfViewerActivity.start(requireActivity(), file_name, bookIndex);
                            });

                            historyContainer.addView(listItem);
                        }
                    }
                    ProgressBar progressBar = getView().findViewById(R.id.history_loading);
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e ->
                        Log.e("FirestoreHistory", "Failed to load history", e));
    }

}
