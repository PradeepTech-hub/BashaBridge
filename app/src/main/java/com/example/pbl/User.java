package com.example.pbl;

public class User {
    private String uid;
    private String name;
    private String email;
    private String role; // "student" or "teacher"
    private String standard;
    private long createdAt;

    public User() {
        // Required for Firestore
    }

    public User(String uid, String name, String email, String role, String standard, long createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.standard = standard;
        this.createdAt = createdAt;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
