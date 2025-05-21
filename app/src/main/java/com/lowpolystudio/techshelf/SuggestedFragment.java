package com.lowpolystudio.techshelf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SuggestedFragment extends Fragment implements MainActivity.FragmentDataListener {

    private LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.suggested_fragment, container, false);
        linearLayout = view.findViewById(R.id.linearLayout);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onDataLoaded();  // Вызов после создания View
    }

    // Метод для добавления книг в LinearLayout
    @Override
    public void onDataLoaded() {
        if (getView() != null) {
            if (linearLayout != null) {
                linearLayout.removeAllViews();  // Очищаем перед добавлением новых книг
                for (int i = 0; i < ((MainActivity) requireActivity()).db_manager.bookNumber(); i++) {
                    View listItem = LayoutInflater.from(getContext())
                            .inflate(R.layout.list_item, linearLayout, false);

                    ImageView imageView = listItem.findViewById(R.id.item_image);
                    TextView textTitle = listItem.findViewById(R.id.item_title);
                    TextView textAuthor = listItem.findViewById(R.id.item_author);
                    TextView textTags = listItem.findViewById(R.id.item_tags);

                    textTitle.setText(((MainActivity) requireActivity()).db_manager.getBookField(i, "Title"));
                    textAuthor.setText("by: " + ((MainActivity) requireActivity()).db_manager.getBookField(i, "Author"));
                    ((MainActivity) requireActivity()).setBookCover(getContext(), imageView,
                            ((MainActivity) requireActivity()).db_manager.getBookCover(i));
                    textTags.setText(((MainActivity) requireActivity()).db_manager.getBookTags(i));
                    int number_book = i;
                    listItem.setOnClickListener(v -> {
                        ((MainActivity) requireActivity()).showBookInfoBottomSheet(number_book);
                    });

                    linearLayout.addView(listItem);
                }
            }
        }
    }
}
