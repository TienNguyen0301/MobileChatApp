package tien.nh.chatapp;

public class User {
    private int id;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private int role;

    // Các thuộc tính khác của người dùng

    public User(int id, String username, String phone, String email, String avatar, int role) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
        this.role = role;

        // Khởi tạo các thuộc tính khác của người dùng
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getId() {
        return id;
    }
    public int getRole() {
        return role;
    }

// Các phương thức getter và setter cho các thuộc tính khác của người dùng
}
