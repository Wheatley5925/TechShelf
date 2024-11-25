package com.example.myapplication;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Найдем LinearLayout внутри HorizontalScrollView
        LinearLayout linearLayout = findViewById(R.id.linearLayout);

        // Добавим несколько элементов
        for (int i = 0; i < 5; i++) {
            // Создаем новый элемент списка
            View listItem = LayoutInflater.from(this).inflate(R.layout.list_item, linearLayout, false);

            // Найдем ImageView и TextView в элементе
            ImageView imageView = listItem.findViewById(R.id.item_image);
            TextView textView = listItem.findViewById(R.id.item_text);

            // Устанавливаем изображение и текст
            imageView.setImageResource(R.drawable.book); // Замените на ваши изображения
            textView.setText("Book " + (i + 1));

            int number_book = i;

            // Добавьте обработчик нажатия
            listItem.setOnClickListener(v -> {
                // Вызовите метод для отображения информации
                showBookInfo("Книга " + (number_book + 1), "Описание для книги " + (number_book + 1), R.drawable.book);
            });

            // Добавляем элемент в LinearLayout
            linearLayout.addView(listItem);
        }
    }

    private void showBookInfo(String title, String description, int imageResId) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.book_info, null);
        dialog.setContentView(dialogView);

        // Настраиваем элементы внутри диалога
        ImageView bookImage = dialogView.findViewById(R.id.dialog_book_image);
        TextView bookTitle = dialogView.findViewById(R.id.dialog_book_title);
        TextView bookDescription = dialogView.findViewById(R.id.dialog_book_description);

        // Устанавливаем данные
        bookImage.setImageResource(imageResId);
        bookTitle.setText(title);
        bookDescription.setText(description);

        Log.d("showBookInfo", "Title: " + title);
        Log.d("showBookInfo", "Description: " + description);
        Log.d("showBookInfo", "ImageResId: " + imageResId);

        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) dialogView.getParent());

        // Показываем BottomSheetDialog
        dialog.show();
    }
}

