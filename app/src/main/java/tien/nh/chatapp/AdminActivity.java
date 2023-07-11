package tien.nh.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

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