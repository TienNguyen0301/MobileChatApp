package tien.nh.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.database.sqlite.SQLiteDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity implements AdminAdapter.OnUserDeleteListener{

    ChatDatabaseHelper dbHelper = new ChatDatabaseHelper(this);
    ListView listViewAdmin;
    AdminAdapter adminAdapter; // Declare the adminAdapter as a class-level field
    ImageButton btnLogout;
    Button addUserButton;

    private static final int UPDATE_USER_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        listViewAdmin = (ListView) findViewById(R.id.userListView) ;
        btnLogout = (ImageButton) findViewById(R.id.btnLogout);
        addUserButton = (Button) findViewById(R.id.addUserButton);


        dbHelper = new ChatDatabaseHelper(this); // Move the initialization here
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);


        ArrayList<User> usersList = getOtherUsers(currentUserId);

        adminAdapter = new AdminAdapter(this, usersList);
        listViewAdmin.setAdapter(adminAdapter);

        adminAdapter.setOnUserDeleteListener(this); // Set the listener

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear(); // Xóa tất cả dữ liệu trong SharedPreferences
                editor.apply(); // Áp dụng thay đổi

                //chuyển sang màn hình đăng nhập
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish(); // Đóng màn hình hiện tại (nếu cần thiết)
            }
        });

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), AddUserActivity.class);
                startActivity(intent);
            }
        });


    }

    // Trong AdminActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            // Xử lý khi UpdateUserActivity đã hoàn thành cập nhật thành công
            // Ví dụ: làm mới danh sách người dùng
            SharedPreferences sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            int currentUserId = sharedPreferences.getInt("currentUserId", 0);
            ArrayList<User> usersList = getOtherUsers(currentUserId);

            adminAdapter = new AdminAdapter(this, usersList);
            listViewAdmin.setAdapter(adminAdapter);
        }

    }


    private ArrayList<User> getOtherUsers(int currentUserId) {
        ArrayList<User> userOtherList = new ArrayList<>();

        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Truy vấn cơ sở dữ liệu để lấy danh sách người dùng khác
//        String query = "SELECT * FROM users WHERE _id <> ?";
        String query = "SELECT * FROM users WHERE _id <> ? AND role <> ? ";

        String[] selectionArgs = {String.valueOf(currentUserId), "2"};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        // Duyệt qua từng hàng trong Cursor
        while (cursor.moveToNext()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            int role = cursor.getInt(cursor.getColumnIndexOrThrow("role"));
            // Kiểm tra nếu người dùng không phải là người đang đăng nhập
            if (currentUserId != userId) {
                User user = new User(userId, username, phone, email, avatar,role);
                userOtherList.add(user);
            }
        }
        // Đóng Cursor sau khi sử dụng
        cursor.close();
        return userOtherList;
    }

    private ArrayList<User> getUser(int currentUserId) {
        ArrayList<User> getUser = new ArrayList<>();

        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Truy vấn cơ sở dữ liệu để lấy danh sách người dùng khác
//        String query = "SELECT * FROM users WHERE _id <> ?";
        String query = "SELECT * FROM users WHERE _id <> ?";

        String[] selectionArgs = {String.valueOf(currentUserId)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        // Duyệt qua từng hàng trong Cursor
        while (cursor.moveToNext()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            int role = cursor.getInt(cursor.getColumnIndexOrThrow("role"));
            // Kiểm tra nếu người dùng không phải là người đang đăng nhập
            if (currentUserId != userId) {
                User user = new User(userId, username, phone, email, avatar,role);
                getUser.add(user);
            }
        }
        // Đóng Cursor sau khi sử dụng
        cursor.close();
        return getUser;
    }

    @Override
    public void onDeleteUser(User user) {
        // Handle the delete operation here
        int userId = user.getId();


        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference usersRef = firestore.collection(ChatDatabaseHelper.TABLE_USERS);

        // Xác định document reference cho người dùng cần xóa
        DocumentReference userDocRef = usersRef.document(String.valueOf(userId));

        // Xóa người dùng từ Firestore
        userDocRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Xóa thành công, thực hiện các thao tác khác
                        // Ví dụ: cập nhật lại giao diện, thông báo thành công, vv.
                        Intent intent = new Intent(getApplication(), AdminActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý lỗi khi xóa người dùng
                        Log.d("ERROR", "Lỗi khi xóa người dùng từ Firestore");
                    }
                });
    }

//    private void fetchDataFromFirestore() {
//        FirebaseFirestore firestoreUsers = FirebaseFirestore.getInstance();
//        firestoreUsers.collection(ChatDatabaseHelper.TABLE_USERS).get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        // Xóa dữ liệu cũ trong SQLite để cập nhật lại từ dữ liệu Firebase
//                        dbHelper.deleteAllUsers();
//
//                        // Duyệt qua danh sách các DocumentSnapshot
//                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                            // Lấy dữ liệu từ DocumentSnapshot
//                            int documentId = Integer.parseInt(documentSnapshot.getId());
//                            String nameFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_NAME);
//                            String emailFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_EMAIL);
//                            String phoneFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_PHONE);
//                            String passwordFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_PASSWORD);
//                            String avatarFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_AVATAR);
//                            Long role = documentSnapshot.getLong(ChatDatabaseHelper.COLUMN_USER_ROLE);
//                            int roleFB = role != null ? role.intValue() : 0;
//
//                            // Thực hiện các thao tác cập nhật cơ sở dữ liệu SQLite tại đây
//                            // Ví dụ: thêm mới hoặc cập nhật bản ghi trong SQLite
//
//                            SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//                            // Insert user data into the "users" table
//                            ContentValues values = new ContentValues();
//                            values.put("_id", documentId);
//                            values.put("name", nameFb);
//                            values.put("email", emailFb);
//                            values.put("phone", phoneFb);
//                            values.put("password", passwordFb);
//                            values.put("avatar", avatarFb);
//                            values.put("role", roleFB);
//
//                            // Thêm mới người dùng vào cơ sở dữ liệu SQLite
//                            dbHelper.insertUser(documentId, nameFb, emailFb, phoneFb, passwordFb, avatarFb, roleFB);
//
//                        }
//
//                        // Sau khi hoàn thành việc cập nhật cơ sở dữ liệu SQLite
//                        // có thể cập nhật giao diện hoặc thực hiện các thao tác khác
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Xử lý lỗi khi lấy dữ liệu từ Firebase
//                        Log.d("ERROR", "Lỗi khi lấy dữ liệu từ Firebase");
//                    }
//                });
//
//
//    }
}