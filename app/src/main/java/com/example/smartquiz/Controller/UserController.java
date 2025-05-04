package com.example.smartquiz.Controller;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;

public class UserController extends BaseController{
    public void createUser(UserController userController, OnCompleteListener<Void> listener) {
        db.collection("users").document(getCurrentUserId()).set(userController)
                .addOnCompleteListener(listener);
    }

    public void getCurrentUser(EventListener<DocumentSnapshot> listener) {
        db.collection("users").document(getCurrentUserId())
                .addSnapshotListener(listener);
    }
}

