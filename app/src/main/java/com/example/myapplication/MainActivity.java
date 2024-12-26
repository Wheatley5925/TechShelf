package com.example.myapplication;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;



public class MainActivity extends AppCompatActivity {
    public dbManager db_manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db_manager = new dbManager(this);
        db_manager.open();

        setContentView(R.layout.activity_main);  // <- Сначала загружаем макет

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

        // Загружаем фрагмент по умолчанию
        if (savedInstanceState == null) {
            SuggestedFragment fragment = new SuggestedFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }


    }

    public void showBookInfoBottomSheet(int i) {
        // Создаем BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.book_info);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        // Настраиваем элементы внутри диалога
        ImageView bookCover = bottomSheetDialog.findViewById(R.id.dialog_book_image);
        TextView bookTitle = bottomSheetDialog.findViewById(R.id.dialog_book_title);
        TextView bookAuthor = bottomSheetDialog.findViewById(R.id.dialog_book_author);
        TextView bookDescription = bottomSheetDialog.findViewById(R.id.dialog_book_desc);

        // Устанавливаем данные
        setBookCover(this, bookCover, db_manager.getBookCover(i));
        bookTitle.setText(db_manager.getBookField(i, "Title"));
        bookAuthor.setText("by: " + db_manager.getBookField(i, "Author"));
        bookDescription.setText(db_manager.getBookField(i, "Description"));

        // Установка обработчика нажатия
        bookCover.setOnClickListener(v -> {
            FullScreenImageDialog dialog =
                    FullScreenImageDialog.newInstance(db_manager.getBookCover(i));
            dialog.show(getSupportFragmentManager(), "full_screen_dialog");
        });

        bottomSheetDialog.show();
    }

    public void setBookCover(Context context, ImageView imageView, String imageName) {
        try {
            // Путь к файлу в папке assets
            InputStream inputStream = context.getAssets().open("bookImgs/" + imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);
            inputStream.close();
        } catch (IOException e) {
            if (e.getCause() != null)
                // Установим стандартную обложку, если изображение не найдено
                imageView.setImageResource(R.drawable.book);
        }
    }

    public interface FragmentDataListener {
        void onDataLoaded();
    }
}

