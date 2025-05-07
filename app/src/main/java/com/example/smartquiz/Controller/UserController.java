package com.example.smartquiz.Controller;

import com.example.smartquiz.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

public class UserController extends BaseController {

    public void signUpAndCreateUserDocument(String email, String password, final User userProfileData,
            OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            userProfileData.setEmail(firebaseUser.getEmail());
                            userProfileData.setUid(firebaseUser.getUid());
                            db.collection("users").document(firebaseUser.getUid()).set(userProfileData)
                                    .addOnCompleteListener(firestoreTask -> {
                                        if (firestoreTask.isSuccessful()) {
                                            listener.onComplete(authTask); // Both auth and Firestore success
                                        } else {
                                            // Firestore write failed, attempt to delete the auth user
                                            firebaseUser.delete().addOnCompleteListener(deleteTask -> {
                                                // Regardless of delete success, report overall failure
                                                String errorMessage = "Failed to create user profile in Firestore.";
                                                if (deleteTask.isSuccessful()) {
                                                    errorMessage += " User auth record cleaned up.";
                                                } else {
                                                    errorMessage += " Failed to clean up user auth record. Manual intervention may be needed.";
                                                }
                                                // Create a failed AuthResult task to pass to the original listener
                                                Task<AuthResult> failedAuthTask = Tasks.forException(
                                                        new Exception(errorMessage, firestoreTask.getException()));
                                                listener.onComplete(failedAuthTask);
                                            });
                                        }
                                    });
                        } else {
                            // Should not happen if authTask was successful, but handle defensively
                            Task<AuthResult> failedAuthTask = Tasks.forException(
                                    new Exception("FirebaseUser was null after successful auth."));
                            listener.onComplete(failedAuthTask);
                        }
                    } else {
                        // Auth creation failed
                        listener.onComplete(authTask);
                    }
                });
    }

    public void signInWithEmailPassword(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void signOut() {
        auth.signOut();
    }

    public FirebaseUser getFirebaseUser() {
        return auth.getCurrentUser();
    }

    public void getCurrentUserDocument(EventListener<DocumentSnapshot> listener) {
        String userId = getCurrentUserId();
        if (userId != null) {
            db.collection("users").document(userId)
                    .addSnapshotListener(listener);
        } else {
            if (listener != null) {
                // Provide a proper FirebaseFirestoreException
                listener.onEvent(null, new FirebaseFirestoreException("User not logged in or UID not available.",
                        FirebaseFirestoreException.Code.UNAUTHENTICATED));
            }
        }
    }

    public void updateUserDocument(User userUpdateData, OnCompleteListener<Void> listener) {
        String userId = getCurrentUserId();
        if (userId != null) {
            db.collection("users").document(userId)
                    .set(userUpdateData, SetOptions.merge())
                    .addOnCompleteListener(listener);
        } else {
            if (listener != null) {
                Task<Void> failedTask = Tasks
                        .forException(new Exception("User not logged in, cannot update document."));
                listener.onComplete(failedTask);
            }
        }
    }

    public void deleteUserAccount(OnCompleteListener<Void> authListener, OnCompleteListener<Void> firestoreListener) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            // First delete Firestore document
            db.collection("users").document(userId).delete()
                    .addOnCompleteListener(firestoreTask -> {
                        if (firestoreTask.isSuccessful()) {
                            // Firestore document deleted, now delete auth user
                            firebaseUser.delete().addOnCompleteListener(authListener);
                        } else {
                            // Firestore deletion failed, call firestoreListener with failure
                            if (firestoreListener != null) {
                                firestoreListener.onComplete(firestoreTask);
                            }
                            // Optionally, do not proceed to delete auth user if Firestore delete failed
                            // Or call authListener with a failure too
                            if (authListener != null) {
                                Task<Void> failedAuthTask = Tasks.forException(new Exception(
                                        "Failed to delete user data from Firestore. Auth deletion not attempted.",
                                        firestoreTask.getException()));
                                authListener.onComplete(failedAuthTask);
                            }
                        }
                    });
        } else {
            if (authListener != null) {
                Task<Void> failedTask = Tasks.forException(new Exception("No user logged in to delete."));
                authListener.onComplete(failedTask);
            }
            if (firestoreListener != null) {
                Task<Void> failedTask = Tasks
                        .forException(new Exception("No user logged in to delete Firestore document."));
                firestoreListener.onComplete(failedTask);
            }
        }
    }
}