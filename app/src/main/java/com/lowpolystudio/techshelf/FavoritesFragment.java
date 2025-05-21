package com.lowpolystudio.techshelf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FavoritesFragment extends Fragment implements MainActivity.FragmentDataListener{
    private LinearLayout favoritesContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.favorites_fragment, container, false);
        favoritesContainer = view.findViewById(R.id.favoritesContainer);

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
            if (favoritesContainer != null) {
                favoritesContainer.removeAllViews();  // Очищаем перед добавлением новых книг
                ((MainActivity) requireActivity()).getFavoriteBookIds(fav_ids -> {
                    for (String id : fav_ids) {
                        int i = Integer.parseInt(id);
                        View listItem = LayoutInflater.from(getContext())
                                .inflate(R.layout.list_item, favoritesContainer, false);

                        ImageView imageView = listItem.findViewById(R.id.item_image);
                        TextView textTitle = listItem.findViewById(R.id.item_title);
                        TextView textAuthor = listItem.findViewById(R.id.item_author);
                        TextView textTags = listItem.findViewById(R.id.item_tags);

                        textTitle.setText(((MainActivity) requireActivity()).db_manager.getBookField(i, "Title"));
                        textAuthor.setText("by: " + ((MainActivity) requireActivity()).db_manager.getBookField(i, "Author"));
                        ((MainActivity) requireActivity()).setBookCover(getContext(), imageView,
                                ((MainActivity) requireActivity()).db_manager.getBookCover(i));
                        textTags.setText(((MainActivity) requireActivity()).db_manager.getBookTags(i));

                        listItem.setOnClickListener(v -> {
                            ((MainActivity) requireActivity()).showBookInfoBottomSheet(i);
                        });

                        favoritesContainer.addView(listItem);
                    }
                    ProgressBar progressBar = getView().findViewById(R.id.favorites_loading);
                    progressBar.setVisibility(View.GONE);
                });
            }
        }
    }
}
