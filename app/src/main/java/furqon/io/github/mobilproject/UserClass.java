package furqon.io.github.mobilproject;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserClass {
    public static class User{
        private static final String TAG = "USER-CLASS";
        private static FirebaseAuth mAuth;
        public static FirebaseUser currentUser;

        public static void init(){
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
        }

    }
}
