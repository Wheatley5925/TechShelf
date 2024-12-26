package com.example.myapplication;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BookReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reader);

        WebView webView = findViewById(R.id.webView);

        String bookPath = getIntent().getStringExtra("BOOK_PATH");
        if (bookPath != null) {
            displayBook(webView, bookPath);
        }
    }

    private void displayBook(WebView webView, String assetPath) {
        try {
            // Копируем файл из assets во временную директорию
            File file = copyAssetToTemp(assetPath);

            // Загружаем документ Aspose
            Document doc = new Document(file.getAbsolutePath());

            // Конвертируем документ в HTML
            File htmlFile = new File(getCacheDir(), "output.html");
            doc.save(new FileOutputStream(htmlFile), SaveFormat.HTML);

            // Загружаем HTML в WebView
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl("file:///" + htmlFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File copyAssetToTemp(String assetPath) throws Exception {
        InputStream in = getAssets().open(assetPath);
        File outFile = new File(getCacheDir(), new File(assetPath).getName());
        FileOutputStream out = new FileOutputStream(outFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.close();
        return outFile;
    }
}
