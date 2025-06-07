package com.lowpolystudio.techshelf;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import com.google.firebase.firestore.Query;

public class FirestoreUtils {

    public interface FirestoreCallback {
        void onSuccess(DocumentSnapshot snapshot);
        void onFailure(Exception e);
    }

    public static void getWithTimeoutAndFallback(
            DocumentReference docRef,
            long timeoutMillis,
            FirestoreCallback callback
    ) {
        final boolean[] responded = {false};

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable fallback = () -> {
            if (!responded[0]) {
                Log.w("Firestore", "Timeout reached, trying cache");
                docRef.get(Source.CACHE)
                        .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot))
                        .addOnFailureListener(callback::onFailure);
            }
        };
        handler.postDelayed(fallback, timeoutMillis);

        docRef.get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    responded[0] = true;
                    handler.removeCallbacks(fallback);
                    callback.onSuccess(snapshot);
                })
                .addOnFailureListener(e -> {
                    responded[0] = true;
                    handler.removeCallbacks(fallback);
                    Log.w("Firestore", "Server access failed, trying cache", e);
                    docRef.get(Source.CACHE)
                            .addOnSuccessListener(callback::onSuccess)
                            .addOnFailureListener(callback::onFailure);
                });
    }

    public static void queryWithTimeoutAndFallback(
            Query query,
            long timeoutMillis,
            QueryCallback callback
    ) {
        final boolean[] responded = {false};

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable fallback = () -> {
            if (!responded[0]) {
                Log.w("Firestore", "Query timeout, trying cache...");
                query.get(Source.CACHE)
                        .addOnSuccessListener(callback::onSuccess)
                        .addOnFailureListener(callback::onFailure);
            }
        };
        handler.postDelayed(fallback, timeoutMillis);

        query.get(Source.SERVER)
                .addOnSuccessListener(result -> {
                    responded[0] = true;
                    handler.removeCallbacks(fallback);
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    responded[0] = true;
                    handler.removeCallbacks(fallback);
                    Log.e("Firestore", "Query server failed, trying cache...", e);
                    query.get(Source.CACHE)
                            .addOnSuccessListener(callback::onSuccess)
                            .addOnFailureListener(callback::onFailure);
                });
    }
    public static void documentWithTimeoutAndFallback(
            DocumentReference docRef,
            long timeoutMillis,
            DocumentCallback callback
    ) {
        final boolean[] responded = {false};

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable fallback = () -> {
            if (!responded[0]) {
                Log.w("Firestore", "Document timeout, trying cache...");
                docRef.get(Source.CACHE)
                        .addOnSuccessListener(callback::onSuccess)
                        .addOnFailureListener(callback::onFailure);
            }
        };
        handler.postDelayed(fallback, timeoutMillis);

        docRef.get(Source.SERVER)
                .addOnSuccessListener(result -> {
                    responded[0] = true;
                    handler.removeCallbacks(fallback);
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    responded[0] = true;
                    handler.removeCallbacks(fallback);
                    Log.e("Firestore", "Server document fetch failed, trying cache...", e);
                    docRef.get(Source.CACHE)
                            .addOnSuccessListener(callback::onSuccess)
                            .addOnFailureListener(callback::onFailure);
                });
    }

    public interface QueryCallback {
        void onSuccess(QuerySnapshot querySnapshot);
        void onFailure(Exception e);
    }

    public interface DocumentCallback {
        void onSuccess(DocumentSnapshot documentSnapshot);
        void onFailure(Exception e);
    }

}
