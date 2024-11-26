package com.example.myapplication;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;


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
                showBookInfoBottomSheet(number_book + 1); // Pass the book number
            });

            // Добавляем элемент в LinearLayout
            linearLayout.addView(listItem);
        }
    }

    private void showBookInfoBottomSheet(int i) {
        // Создаем BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.book_info);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        bottomSheetDialog.show();
    }
}

