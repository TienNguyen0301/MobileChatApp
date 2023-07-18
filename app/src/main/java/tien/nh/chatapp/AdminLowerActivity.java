package tien.nh.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminLowerActivity extends AppCompatActivity implements AdminAdapter.OnUserDeleteListener{
    ChatDatabaseHelper dbHelper = new ChatDatabaseHelper(this);
    ListView listViewAdmin;
    AdminAdapter adminAdapter; // Declare the adminAdapter as a class-level field
    ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lower);

        listViewAdmin = (ListView) findViewById(R.id.userListView) ;
        btnLogout = (ImageButton) findViewById(R.id.btnLogout);

        dbHelper = new ChatDatabaseHelper(this); // Move the initialization here
        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
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
    }

    private ArrayList<User> getOtherUsers(int currentUserId) {
        ArrayList<User> userOtherList = new ArrayList<>();

        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Truy vấn cơ sở dữ liệu để lấy danh sách người dùng khác
        String query = "SELECT * FROM users WHERE _id <> ? AND role = 0";
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
                userOtherList.add(user);
            }
        }
        // Đóng Cursor sau khi sử dụng
        cursor.close();
        return userOtherList;
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
}