package com.lowpolystudio.techshelf;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.io.IOException;
import java.io.InputStream;

public class FullScreenImageDialog extends DialogFragment {
    private static final String ARG_IMAGE_NAME = "image_name";
    private String imageName;

    public static FullScreenImageDialog newInstance(String imageName) {
        FullScreenImageDialog fragment = new FullScreenImageDialog();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_NAME, imageName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageName = getArguments().getString(ARG_IMAGE_NAME);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fullscreen_image);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        ImageView fullScreenImageView = dialog.findViewById(R.id.fullScreenImageView);

        // Устанавливаем изображение
        loadImageFromAssets(requireContext(), fullScreenImageView, imageName);

        // Закрыть диалог по нажатию на изображение
        fullScreenImageView.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private void loadImageFromAssets(Context context, ImageView imageView, String imageName) {
        try {
            InputStream inputStream = context.getAssets().open("bookImgs/" + imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.book);
        }
    }


}
