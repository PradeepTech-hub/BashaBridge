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
import com.google.firebase.auth.FirebaseUser;
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
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Apply logo animation
        android.view.View logoContainer = findViewById(R.id.logoContainer);
        if (logoContainer != null) {
            android.view.animation.Animation fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.logo_fade_in);
            logoContainer.startAnimation(fadeIn);
        }

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
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            checkUserRoleAndNavigate(uid);
                        } else {
                            // User deleted from Firestore - force sign out
                            mAuth.signOut();
                            Toast.makeText(LoginActivity.this, "Account session expired.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure (e.g., allow offline if cached)
                        checkUserRoleAndNavigate(uid);
                    });
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

        // --- Offline Login Support ---
        if (!SyncManager.getInstance(this).isOnline()) {
            DBHelper dbHelper = new DBHelper(this);
            User localUser = dbHelper.getUserByEmail(email); 
            if (localUser != null) {
                if (selectedRole.equals(localUser.getRole())) {
                    Toast.makeText(this, "Offline Login Successful", Toast.LENGTH_SHORT).show();
                    proceedToDashboard(localUser);
                    return;
                } else {
                    Toast.makeText(this, "Role mismatch in offline mode", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Internet required for first login", Toast.LENGTH_SHORT).show();
            }
            btnLogin.setEnabled(true);
            return;
        }

        // Authenticate with Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // MANDATORY: Verify user exists in Firestore before allowing login
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        checkUserRoleAndNavigate(uid, selectedRole);
                                    } else {
                                        // User deleted from Firestore but exists in Auth
                                        mAuth.signOut();
                                        btnLogin.setEnabled(true);
                                        Toast.makeText(LoginActivity.this, "Account not found in database. Please register again.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    mAuth.signOut();
                                    btnLogin.setEnabled(true);
                                    Toast.makeText(LoginActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedToDashboard(User user) {
        if (user.getRole() != null && user.getRole().equalsIgnoreCase("teacher")) {
            startActivity(new Intent(LoginActivity.this, TeacherDashboardActivity.class));
        } else {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USER_STANDARD", user.getStandard());
            startActivity(intent);
        }
        finish();
    }

    private void signInWithGoogle() {
        int checkedId = toggleGroupRole.getCheckedButtonId();
        if (checkedId == View.NO_ID) {
            Toast.makeText(this, "Please select a role first", Toast.LENGTH_SHORT).show();
            return;
        }

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
                        // For Google Login, we still check if the user exists in Firestore
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        checkUserRoleAndNavigate(uid, selectedRole);
                                    } else {
                                        // If user is new or deleted from Firestore, redirect to Register or auto-create
                                        // But per requirement, we ensure they only log in if "valid existing"
                                        // Here we auto-create if it's the first time, but if you want strict existing only:
                                        // For now, I'll follow the existing logic of creating a profile if it's a new Google user,
                                        // but it will fail if they were deleted and you want to block them.
                                        checkIfUserExistsAndNavigate(uid, selectedRole);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    mAuth.signOut();
                                    btnGoogleLogin.setEnabled(true);
                                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
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
                        // User exists in Auth but not in Firestore - force sign out to enforce strict registration
                        mAuth.signOut();
                        btnGoogleLogin.setEnabled(true);
                        Toast.makeText(this, "Account not found. Please register first.", Toast.LENGTH_LONG).show();
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
                            user.setUid(uid);
                            new DBHelper(LoginActivity.this).saveUser(user); // Cache for offline

                            if (selectedRole != null && !selectedRole.equalsIgnoreCase(user.getRole())) {
                                mAuth.signOut();
                                btnLogin.setEnabled(true);
                                btnGoogleLogin.setEnabled(true);
                                String actualRole = (user.getRole() != null) ? user.getRole() : "none";
                                Toast.makeText(LoginActivity.this, "Access denied. You are registered as a " + actualRole + ", but selected " + selectedRole, Toast.LENGTH_LONG).show();
                                return;
                            }

                            proceedToDashboard(user);
                        }
                    } else {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Try offline even if navigation fails (e.g. network lost during fetch)
                    User localUser = new DBHelper(LoginActivity.this).getUser(uid);
                    if (localUser != null) {
                        proceedToDashboard(localUser);
                    } else {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRoleAndNavigate(String uid) {
        checkUserRoleAndNavigate(uid, null);
    }
}
