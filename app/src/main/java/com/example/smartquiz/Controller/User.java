package com.example.smartquiz.Controller;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;

public class User extends BaseController{
    public void createUser(User user, OnCompleteListener<Void> listener) {
        db.collection("users").document(getCurrentUserId()).set(user)
                .addOnCompleteListener(listener);
    }

    public void getCurrentUser(EventListener<DocumentSnapshot> listener) {
        db.collection("users").document(getCurrentUserId())
                .addSnapshotListener(listener);
    }
}

