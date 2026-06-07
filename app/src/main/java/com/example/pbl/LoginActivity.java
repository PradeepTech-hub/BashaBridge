package com.example.pbl;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvRegister;
    private com.google.android.material.button.MaterialButtonToggleGroup toggleGroupRole;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvRegister = findViewById(R.id.tvRegister);
        toggleGroupRole = findViewById(R.id.toggleGroupRole);

        btnLogin.setOnClickListener(v -> loginUser());
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            checkUserRoleAndNavigate(mAuth.getCurrentUser().getUid());
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        int checkedId = toggleGroupRole.getCheckedButtonId();
        final String selectedRole = (checkedId == R.id.btnTeacher) ? "teacher" : "student";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRoleAndNavigate(mAuth.getCurrentUser().getUid(), selectedRole);
                    } else {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        int checkedId = toggleGroupRole.getCheckedButtonId();
        final String selectedRole = (checkedId == R.id.btnTeacher) ? "teacher" : "student";
        
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        int checkedId = toggleGroupRole.getCheckedButtonId();
        final String selectedRole = (checkedId == R.id.btnTeacher) ? "teacher" : "student";
        
        btnGoogleLogin.setEnabled(false);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        checkIfUserExistsAndNavigate(uid, selectedRole);
                    } else {
                        btnGoogleLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Firebase auth failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfUserExistsAndNavigate(String uid, String selectedRole) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        checkUserRoleAndNavigate(uid, selectedRole);
                    } else {
                        // User exists in Auth but not in Firestore - redirected to complete registration
                        Toast.makeText(this, "Please register to complete your profile", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        btnGoogleLogin.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    btnGoogleLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserRoleAndNavigate(String uid, String selectedRole) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (selectedRole != null && !selectedRole.equals(user.getRole())) {
                                mAuth.signOut();
                                btnLogin.setEnabled(true);
                                Toast.makeText(LoginActivity.this, "Access denied. You are not registered as a " + selectedRole, Toast.LENGTH_LONG).show();
                                return;
                            }

                            if ("teacher".equals(user.getRole())) {
                                startActivity(new Intent(LoginActivity.this, TeacherDashboardActivity.class));
                            } else {
                                // For students, we might need to ensure they have a standard selected
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("USER_STANDARD", user.getStandard());
                                startActivity(intent);
                            }
                            finish();
                        }
                    } else {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserRoleAndNavigate(String uid) {
        checkUserRoleAndNavigate(uid, null);
    }
}
