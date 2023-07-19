package tien.nh.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserManagementActivity extends AppCompatActivity implements AdminAdapter.OnUserDeleteListener{
    Button addUserButton;
    ImageButton btn_back;
    ChatDatabaseHelper dbHelper = new ChatDatabaseHelper(this);
    ListView listViewAdmin;
    AdminAdapter adminAdapter;
    private static final int UPDATE_USER_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        listViewAdmin = (ListView) findViewById(R.id.userListView) ;
        addUserButton = (Button) findViewById(R.id.addUserButton);
        btn_back = (ImageButton) findViewById(R.id.btn_back);

        dbHelper = new ChatDatabaseHelper(this); // Move the initialization here
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        ArrayList<User> usersList = getOtherUsers(currentUserId);

        adminAdapter = new AdminAdapter(this, usersList, UserManagementActivity.class);
        listViewAdmin.setAdapter(adminAdapter);

//        adminAdapter.setOnUserDeleteListener(this); // Set the listener
        adminAdapter.setOnUserDeleteListener(this::onDeleteUser);


        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), AddUserActivity.class);
                startActivity(intent);
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
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

            adminAdapter = new AdminAdapter(this, usersList, UserManagementActivity.class);
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
                        Intent intent = new Intent(getApplication(), UserManagementActivity.class);
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