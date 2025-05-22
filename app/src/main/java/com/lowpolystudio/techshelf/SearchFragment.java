package com.lowpolystudio.techshelf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class SearchFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.search_fragment, container, false);
        LinearLayout layout = view.findViewById(R.id.search_results_container);

        Spinner languageSpinner = view.findViewById(R.id.language_spinner);
        Spinner purposeSpinner = view.findViewById(R.id.purpose_spinner);
        Button searchButton = view.findViewById(R.id.search_button);

        List<String> languages = ((MainActivity) requireActivity()).db_manager.getTagsByType("language");
        List<String> purposes = ((MainActivity) requireActivity()).db_manager.getTagsByType("purpose");

        ArrayAdapter<String> lang_adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, languages);
        ArrayAdapter<String> purpose_adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, purposes);

        languageSpinner.setAdapter(lang_adapter);
        purposeSpinner.setAdapter(purpose_adapter);

        searchButton.setOnClickListener(
                l -> {
                    layout.removeAllViews();
                    List<Integer> results = ((MainActivity) requireActivity()).db_manager.getBookIdsByTags(languageSpinner.getSelectedItem().toString(), purposeSpinner.getSelectedItem().toString());

                    if (results.isEmpty()) {
                        TextView emptiness = new TextView(getActivity());
                        emptiness.setText("Nothing found 😢");
                        emptiness.setGravity(17);
                        layout.addView(emptiness);
                    }
                    else {
                        for (int bookId : results) {
                            View bookItem = LayoutInflater.from(getContext()).inflate(R.layout.list_item, container, false);
                            TextView title = bookItem.findViewById(R.id.item_title);
                            ImageView cover = bookItem.findViewById(R.id.item_image);
                            title.setText(((MainActivity) requireActivity()).db_manager.getBookField(bookId, "Title"));
                            ((MainActivity) requireActivity()).setBookCover(getContext(), cover, ((MainActivity) requireActivity()).db_manager.getBookCover(bookId));
                            bookItem.setOnClickListener(v -> ((MainActivity) requireActivity()).showBookInfoBottomSheet(bookId));
                            layout.addView(bookItem);
                        }
                    }
                }
        );

        return view;
    }
}
