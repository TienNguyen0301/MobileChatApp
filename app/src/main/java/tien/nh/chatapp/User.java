package tien.nh.chatapp;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String idUser;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private int role;
    private String password;


//    public User(String username, String phone, String password, String email, String avatar, int role) {
//        this.username = username;
//        this.phone = phone;
//        this.password = password;
//        this.email = email;
//        this.avatar = avatar;
//        this.role = role;
//    }

//    public User( String username,String phone, String email, String avatar, int role) {
//        this.username = username;
//        this.phone = phone;
//        this.email = email;
//        this.avatar = avatar;
//        this.role = role;
//    }

    public User(int id, String username, String phone, String email, String avatar, int role) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
        this.role = role;
    }

    public String getPassword() {
        return password;
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

}
