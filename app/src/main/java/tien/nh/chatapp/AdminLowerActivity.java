package tien.nh.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

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


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("users", "_id = ?", new String[]{String.valueOf(userId)});
        db.close();


        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        // Refresh the user list in the adapter
        ArrayList<User> usersList = getOtherUsers(currentUserId);
        adminAdapter.setData(usersList);
        adminAdapter.notifyDataSetChanged();

        // Handle the navigation or show a toast message here
        Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
    }
}