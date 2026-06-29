package com.example.pbl;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.checkbox.MaterialCheckBox;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.UUID;

public class ManageContentActivity extends AppCompatActivity {

    private RecyclerView rvContent;
    private FloatingActionButton fabAdd;
    private ContentAdapter adapter;
    private List<Word> contentList;
    private List<Word> filteredList;
    private FirebaseFirestore db;
    private String contentType; // "Words" or "Sentences"

    private Uri selectedImageUri;
    private ImageView ivPreview;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Log.d("ManageContent", "Local image selected: " + uri.toString());
                    if (ivPreview != null) {
                        Glide.with(this)
                                .load(uri)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .into(ivPreview);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // --- Role-based Access Control ---
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish();
            return;
        }

        User user = new DBHelper(this).getUser(uid);
        if (user != null && "teacher".equalsIgnoreCase(user.getRole())) {
            setupActivity();
        } else {
            // Fallback to Firestore
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User remoteUser = documentSnapshot.toObject(User.class);
                        if (remoteUser != null && "teacher".equalsIgnoreCase(remoteUser.getRole())) {
                            remoteUser.setUid(uid);
                            new DBHelper(this).saveUser(remoteUser);
                            setupActivity();
                        } else {
                            Toast.makeText(this, "Access Denied: Teacher role required", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Access Denied: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void setupActivity() {
        setContentView(R.layout.activity_manage_content);

        db = FirebaseFirestore.getInstance();
        contentType = getIntent().getStringExtra("TYPE");
        if (contentType == null) contentType = "Words";

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Manage " + contentType);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvContent = findViewById(R.id.rvContent);
        fabAdd = findViewById(R.id.fabAdd);

        contentList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ContentAdapter(filteredList, this::showEditDialog, this::deleteContent);
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setAdapter(adapter);

        setupFilters();

        fabAdd.setOnClickListener(v -> showEditDialog(null));

        loadContent();
    }

    private void loadContent() {
        String collection = contentType.equals("Words") ? "words" : "sentences";
        db.collection(collection).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("ManageContent", "Firestore load failed", task.getException());
                Toast.makeText(this, "Error loading Firestore content", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use LinkedHashMap to preserve order and merge by key
            java.util.Map<String, Word> mergedMap = new java.util.LinkedHashMap<>();
            
            // 1. Add default data from DataManager
            List<Word> defaults = DataManager.getAllDefaultContent(contentType);
            for (Word w : defaults) {
                // Key format: "englishText_standard"
                String key = (w.getEnglish().trim() + "_" + w.getStandard()).toLowerCase();
                mergedMap.put(key, w);
            }
            
            // 2. Add/Overwrite with Firestore data
            if (task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Word word = document.toObject(Word.class);
                        word.setId(document.getId());
                        
                        Log.d("ManageContent", "Firestore Item: " + word.getEnglish() + " | URL: " + word.getImageUrl());
                        
                        String key = (word.getEnglish().trim() + "_" + word.getStandard()).toLowerCase();
                        mergedMap.put(key, word);
                    } catch (Exception e) {
                        Log.e("ManageContent", "Error parsing Firestore document", e);
                    }
                }
            }
            
            // Update the UI on the main thread
            runOnUiThread(() -> {
                contentList.clear();
                contentList.addAll(mergedMap.values());
                applyFilters();
                Log.d("ManageContent", "Displaying " + contentList.size() + " items");
            });
        });
    }

    private void setupFilters() {
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        com.google.android.material.chip.ChipGroup chipGroup = findViewById(R.id.chipGroupFilters);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
    }

    private void applyFilters() {
        String query = ((EditText) findViewById(R.id.etSearch)).getText().toString().toLowerCase().trim();
        int checkedChipId = ((com.google.android.material.chip.ChipGroup) findViewById(R.id.chipGroupFilters)).getCheckedChipId();

        filteredList.clear();
        for (Word word : contentList) {
            boolean matchesSearch = word.getEnglish().toLowerCase().contains(query) ||
                    word.getKannada().toLowerCase().contains(query) ||
                    (word.getHindi() != null && word.getHindi().toLowerCase().contains(query));

            boolean matchesStd = true;
            if (checkedChipId == R.id.chipStd1) matchesStd = (word.getStandard() == 1);
            else if (checkedChipId == R.id.chipStd2) matchesStd = (word.getStandard() == 2);

            if (matchesSearch && matchesStd) {
                filteredList.add(word);
            }
        }

        if (checkedChipId == R.id.chipSortAZ) {
            filteredList.sort((w1, w2) -> w1.getEnglish().compareToIgnoreCase(w2.getEnglish()));
        }

        adapter.notifyDataSetChanged();
    }

    private void showEditDialog(Word word) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_content, null);
        builder.setView(view);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etEnglish = view.findViewById(R.id.etEnglish);
        EditText etKannada = view.findViewById(R.id.etKannada);
        EditText etHindi = view.findViewById(R.id.etHindi);
        MaterialCheckBox cbStd1 = view.findViewById(R.id.cbStd1);
        MaterialCheckBox cbStd2 = view.findViewById(R.id.cbStd2);
        AutoCompleteTextView spinnerCategory = view.findViewById(R.id.spinnerCategory);
        ivPreview = view.findViewById(R.id.ivDialogImage);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        selectedImageUri = null;

        String[] categories = {"Animals", "Fruits", "Daily Use", "School Objects", "Numbers", "Sentences"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(catAdapter);

        if (word != null) {
            tvTitle.setText("Edit " + (contentType.equals("Words") ? "Word" : "Sentence"));
            etEnglish.setText(word.getEnglish());
            etKannada.setText(word.getKannada());
            etHindi.setText(word.getHindi());
            
            if (word.getStandard() == 1) cbStd1.setChecked(true);
            else if (word.getStandard() == 2) cbStd2.setChecked(true);
            
            spinnerCategory.setText(word.getCategory(), false);
            
            // Enhanced Preview
            if (word.getImageUrl() != null && !word.getImageUrl().isEmpty()) {
                Glide.with(this).load(word.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivPreview);
            } else if (word.getImageResId() != 0) {
                ivPreview.setImageResource(word.getImageResId());
            } else {
                ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            tvTitle.setText("Add New " + (contentType.equals("Words") ? "Word" : "Sentence"));
            ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        AlertDialog dialog = builder.create();

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            String english = etEnglish.getText().toString().trim();
            String kannada = etKannada.getText().toString().trim();
            String hindi = etHindi.getText().toString().trim();
            String category = spinnerCategory.getText().toString().trim();
            
            boolean std1 = cbStd1.isChecked();
            boolean std2 = cbStd2.isChecked();

            if (TextUtils.isEmpty(english) || TextUtils.isEmpty(kannada) || TextUtils.isEmpty(category)) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!std1 && !std2) {
                Toast.makeText(this, "Please select at least one standard", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Integer> selectedStandards = new ArrayList<>();
            if (std1) selectedStandards.add(1);
            if (std2) selectedStandards.add(2);

            btnSave.setEnabled(false);
            if (selectedImageUri != null) {
                uploadImageAndSave(english, kannada, hindi, selectedStandards, category, word, dialog);
            } else {
                saveToFirestore(english, kannada, hindi, selectedStandards, category, word != null ? word.getImageUrl() : null, word, dialog);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void uploadImageAndSave(String english, String kannada, String hindi, List<Integer> standards, String category, Word oldWord, AlertDialog dialog) {
        if (selectedImageUri == null) {
            saveToFirestore(english, kannada, hindi, standards, category, oldWord != null ? oldWord.getImageUrl() : null, oldWord, dialog);
            return;
        }

        Toast.makeText(this, "Uploading image to Cloudinary...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(selectedImageUri)
                .unsigned("basha_bridge_uploads")
                .option("folder", "content_images")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("ManageContent", "Cloudinary upload started: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Log.d("ManageContent", "Cloudinary upload success: " + imageUrl);
                        runOnUiThread(() -> saveToFirestore(english, kannada, hindi, standards, category, imageUrl, oldWord, dialog));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("ManageContent", "Cloudinary upload failed: " + error.getDescription());
                        runOnUiThread(() -> {
                            Toast.makeText(ManageContentActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                            enableSaveButton(dialog);
                        });
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                }).dispatch();
    }

    private void enableSaveButton(AlertDialog dialog) {
        View btnSave = dialog.findViewById(R.id.btnSave);
        if (btnSave != null) btnSave.setEnabled(true);
    }

    private void saveToFirestore(String english, String kannada, String hindi, List<Integer> standards, String category, String imageUrl, Word oldWord, AlertDialog dialog) {
        String collection = contentType.equals("Words") ? "words" : "sentences";
        int resId = oldWord != null ? oldWord.getImageResId() : android.R.drawable.ic_menu_gallery;

        if (oldWord != null && oldWord.getId() != null) {
            // Update existing document
            Word newWord = new Word(english, kannada, hindi, resId);
            newWord.setStandard(standards.get(0)); // Keep the first selected standard
            newWord.setCategory(category.trim());
            newWord.setImageUrl(imageUrl);

            db.collection(collection).document(oldWord.getId()).set(newWord)
                    .addOnSuccessListener(aVoid -> {
                        dialog.dismiss();
                        loadContent();
                        Toast.makeText(this, "Content updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        View btnSave = dialog.findViewById(R.id.btnSave);
                        if (btnSave != null) btnSave.setEnabled(true);
                    });
        } else {
            // Add new document(s) - one for each selected standard
            for (int i = 0; i < standards.size(); i++) {
                int std = standards.get(i);
                Word newWord = new Word(english, kannada, hindi, resId);
                newWord.setStandard(std);
                newWord.setCategory(category.trim());
                newWord.setImageUrl(imageUrl);

                boolean isLast = (i == standards.size() - 1);
                db.collection(collection).add(newWord)
                        .addOnSuccessListener(documentReference -> {
                            if (isLast) {
                                dialog.dismiss();
                                loadContent();
                                Toast.makeText(this, "Successfully added to " + standards.size() + " standard(s)", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save for Standard " + std, Toast.LENGTH_SHORT).show();
                            View btnSave = dialog.findViewById(R.id.btnSave);
                            if (btnSave != null) btnSave.setEnabled(true);
                        });
            }
        }
    }

    private void deleteContent(Word word) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Content")
                .setMessage("Are you sure you want to delete this?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String collection = contentType.equals("Words") ? "words" : "sentences";
                    db.collection(collection).document(word.getId()).delete()
                            .addOnSuccessListener(aVoid -> loadContent());
                })
                .setNegativeButton("No", null)
                .show();
    }
}
