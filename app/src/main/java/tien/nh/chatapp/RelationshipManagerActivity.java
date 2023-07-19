package tien.nh.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

public class RelationshipManagerActivity extends AppCompatActivity {

    ImageButton btn_back, btnLogout;
    ListView userListView;
    AdminAdapter adminAdapter;
    ChatDatabaseHelper dbHelper = new ChatDatabaseHelper(this);
    private static final int UPDATE_USER_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relationship_manager);

        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btnLogout = (ImageButton) findViewById(R.id.btnLogout);
        userListView = (ListView) findViewById(R.id.userListView);


        dbHelper = new ChatDatabaseHelper(this); // Move the initialization here
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        ArrayList<User> usersList = getOtherUsers(currentUserId);

        adminAdapter = new AdminAdapter(this, usersList, RelationshipManagerActivity.class);
        userListView.setAdapter(adminAdapter);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = adminAdapter.getItem(position);

                // Chuyển sang màn hình mới và truyền dữ liệu người dùng được chọn
                Intent intent = new Intent(RelationshipManagerActivity.this, AdminListFriendUserActivity.class);
                intent.putExtra("selectedUser", selectedUser);
                startActivity(intent);
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), AdminActivity.class);
                startActivity(intent);
            }
        });

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

            adminAdapter = new AdminAdapter(this, usersList, RelationshipManagerActivity.class);
            userListView.setAdapter(adminAdapter);
        }
    }

    private ArrayList<User> getOtherUsers(int currentUserId) {
        ArrayList<User> userOtherList = new ArrayList<>();

        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Truy vấn cơ sở dữ liệu để lấy danh sách người dùng khác
//        String query = "SELECT * FROM users WHERE _id <> ?";
        String query = "SELECT * FROM users WHERE _id <> ? AND role = 0 ";

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


}