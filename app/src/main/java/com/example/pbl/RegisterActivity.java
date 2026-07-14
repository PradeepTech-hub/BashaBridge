package com.example.pbl;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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

public class RegisterActivity extends AppCompatActivity {

    private static final String TEACHER_ACCESS_CODE = "2026";
    private EditText etName, etEmail, etPassword, etStandard, etAccessCode;
    private com.google.android.material.textfield.TextInputLayout tilStandard, tilAccessCode;
    private RadioGroup rgRole;
    private Button btnRegister, btnGoogleRegister;
    private TextView tvLogin;
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
        setContentView(R.layout.activity_register);

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

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etStandard = findViewById(R.id.etStandard);
        etAccessCode = findViewById(R.id.etAccessCode);
        tilStandard = findViewById(R.id.tilStandard);
        tilAccessCode = findViewById(R.id.tilAccessCode);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        tvLogin = findViewById(R.id.tvLogin);

        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTeacher) {
                tilStandard.setVisibility(View.GONE);
                tilAccessCode.setVisibility(View.VISIBLE);
            } else {
                tilStandard.setVisibility(View.VISIBLE);
                tilAccessCode.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(v -> registerUser());
        btnGoogleRegister.setOnClickListener(v -> signInWithGoogle());

        tvLogin.setOnClickListener(v -> finish());
    }

    private void signInWithGoogle() {
        // For registration with Google, we first validate if they selected a role/standard
        String role = rgRole.getCheckedRadioButtonId() == R.id.rbTeacher ? "teacher" : "student";
        String standard = etStandard.getText().toString().trim();
        String accessCode = etAccessCode.getText().toString().trim();

        // Standard is required for students
        if (role.equals("student") && TextUtils.isEmpty(standard)) {
            etStandard.setError("Please enter your standard before using Google Register");
            return;
        }

        // --- SECURITY CHECK: Teacher Access Code for Google Sign-in ---
        if (role.equals("teacher")) {
            if (TextUtils.isEmpty(accessCode)) {
                tilAccessCode.setError("Teacher Access Code is required");
                return;
            } else {
                tilAccessCode.setError(null);
            }
            
            if (!accessCode.equals(TEACHER_ACCESS_CODE)) {
                tilAccessCode.setError("Invalid Teacher Access Code");
                return;
            } else {
                tilAccessCode.setError(null);
                Toast.makeText(this, "Teacher Access Code Verified", Toast.LENGTH_SHORT).show();
            }
        }

        // Force account picker by signing out first
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        btnGoogleRegister.setEnabled(false);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        String name = mAuth.getCurrentUser().getDisplayName();
                        String email = mAuth.getCurrentUser().getEmail();
                        String role = rgRole.getCheckedRadioButtonId() == R.id.rbTeacher ? "teacher" : "student";
                        String standardInput = etStandard.getText().toString().trim();
                        // For teachers, standard is not applicable, so we set it to empty string
                        String finalStandard = role.equals("teacher") ? "" : standardInput;

                        User user = new User(uid, name, email, role, finalStandard, System.currentTimeMillis());
                        new DBHelper(RegisterActivity.this).saveUser(user); // Cache for offline
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    if (role.equals("teacher")) {
                                        startActivity(new Intent(RegisterActivity.this, TeacherDashboardActivity.class));
                                    } else {
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    }
                                    finishAffinity();
                                })
                                .addOnFailureListener(e -> {
                                    btnGoogleRegister.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        btnGoogleRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Firebase auth failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String standardInput = etStandard.getText().toString().trim();
        String accessCode = etAccessCode.getText().toString().trim();
        String role = rgRole.getCheckedRadioButtonId() == R.id.rbTeacher ? "teacher" : "student";

        // Basic input validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (role.equals("student") && TextUtils.isEmpty(standardInput)) {
            etStandard.setError("Standard is required for students");
            return;
        }

        // --- SECURITY CHECK: Teacher registration access code verification ---
        if (role.equals("teacher")) {
            if (TextUtils.isEmpty(accessCode)) {
                tilAccessCode.setError("Teacher Access Code is required");
                return;
            } else {
                tilAccessCode.setError(null);
            }
            
            if (!accessCode.equals(TEACHER_ACCESS_CODE)) {
                tilAccessCode.setError("Invalid Teacher Access Code");
                return;
            } else {
                tilAccessCode.setError(null);
                Toast.makeText(this, "Teacher Access Code Verified", Toast.LENGTH_SHORT).show();
            }
        }

        btnRegister.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // For teachers, standard is empty as per requirements
                        String finalStandard = role.equals("teacher") ? "" : standardInput;
                        User user = new User(uid, name, email, role, finalStandard, System.currentTimeMillis());
                        new DBHelper(RegisterActivity.this).saveUser(user); // Cache for offline
                        
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    if (role.equals("teacher")) {
                                        startActivity(new Intent(RegisterActivity.this, TeacherDashboardActivity.class));
                                    } else {
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    }
                                    finishAffinity();
                                })
                                .addOnFailureListener(e -> {
                                    btnRegister.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
