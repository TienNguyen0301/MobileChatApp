package tien.nh.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

public class AddUserActivity extends AppCompatActivity {
    EditText username, phoneUser, roleUser, emailUser;
    ImageView avatarUser;
    Button btnThem, btnHuy;
    private String avatarPath;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        username = (EditText) findViewById(R.id.username);
        emailUser = (EditText) findViewById(R.id.email);
        phoneUser = (EditText) findViewById(R.id.phone);
        roleUser = (EditText) findViewById(R.id.role);
        avatarUser = (ImageView) findViewById(R.id.avatar);
        btnThem = (Button) findViewById(R.id.btnThem);
        btnHuy = (Button) findViewById(R.id.btnHuy);

        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy thông tin người dùng từ các EditText
                String name = username.getText().toString();
                String email = emailUser.getText().toString();
                String phone = phoneUser.getText().toString();
                String password = phoneUser.getText().toString();
                int roleU = Integer.parseInt(roleUser.getText().toString());

                registerUser(name,phone,password,email,avatarPath,roleU);

                // Thực hiện việc thêm người dùng vào cơ sở dữ liệu
                addUserToDatabase(name, phone, email, password, avatarPath, roleU);
                Intent intent = new Intent(getApplication(), AdminActivity.class);
                startActivity(intent);
            }
        });

        avatarUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open file chooser
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Kiểm tra lớp hiện tại của Activity
        if (getClass() == AddUserActivity.class) {
            // Xử lý tại đây khi người dùng nhấn nút Back trong ChatActivity
            // Ví dụ: Đóng ChatActivity và quay lại trang trước đó
            finish(); // Đóng ChatActivity
        } else {
            super.onBackPressed(); // Thực hiện hành động mặc định của nút Back
        }
    }

    private void registerUser(String name, String phone, String password, String email, String avatar, int role) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersRef = database.collection(ChatDatabaseHelper.TABLE_USERS);

        // Kiểm tra xem bộ sưu tập "users" có bất kỳ tài liệu nào hay không
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot.isEmpty()) {
                    // Bộ sưu tập "users" chưa có tài liệu nào, tạo một tài liệu mới với ID là 1
                    createNewUser(usersRef.document("1"), name, phone, password, email, avatar, role);
                } else {
                    // Bộ sưu tập "users" đã có tài liệu, sử dụng transaction để tăng giá trị ID lên 1 và tạo tài liệu mới
                    createUserWithIncrementedId(usersRef, name, phone, password, email, avatar, role);
                }
            } else {
                // Lỗi khi truy vấn bộ sưu tập "users"
                Toast.makeText(getApplicationContext(), "Failed to query users collection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewUser(DocumentReference userRef, String name, String phone, String password, String email, String avatar, int role) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);
        userData.put("password", password);
        userData.put("email", email);
        userData.put("avatar", avatar);
        userData.put("role", role);

        // Tạo tài liệu mới với ID là 1
        userRef.set(userData).addOnSuccessListener(aVoid -> {
            // Đăng ký người dùng thành công
            Toast.makeText(getApplicationContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            // Lỗi khi đăng ký người dùng
            Toast.makeText(getApplicationContext(), "Failed to register user", Toast.LENGTH_SHORT).show();
        });
    }

    private void createUserWithIncrementedId(CollectionReference usersRef, String name, String phone, String password, String email, String avatar, int role) {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                if (documents.isEmpty()) {
                    // Bộ sưu tập "users" chưa có tài liệu nào, tạo tài liệu mới với ID là 1
                    createNewUser(usersRef.document("1"), name, phone, password, email, avatar, role);
                } else {
                    // Lấy tài liệu cuối cùng trong danh sách và tăng giá trị ID lên 1
                    DocumentSnapshot lastDocument = documents.get(documents.size() - 1);
                    String lastUserId = lastDocument.getId();
                    long newId = Long.parseLong(lastUserId) + 1;

                    // Tạo tài liệu mới với ID mới
                    createNewUser(usersRef.document(String.valueOf(newId)), name, phone, password, email, avatar, role);
                }
            } else {
                // Lỗi khi truy vấn bộ sưu tập "users"
                Toast.makeText(getApplicationContext(), "Failed to query users collection", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addUserToDatabase(String username, String phone, String email, String password, String avatar, int role) {
        // Thực hiện việc thêm người dùng vào cơ sở dữ liệu ở đây
        // Sử dụng các thông tin như tên, số điện thoại, email để thêm người dùng mới
        // Có thể sử dụng phương thức addUser() đã được đề cập trong câu trả lời trước
        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", username);
        values.put("phone", phone);
        values.put("email", email);
        values.put("password", password);
        values.put("avatar", avatar);
        values.put("role", role);

        long newRowId = db.insert("users", null, values);

        db.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Lấy đường dẫn hình ảnh từ kết quả
            Uri imageUri = data.getData();

            // Hiển thị hình ảnh mới trong ImageView bằng Glide hoặc các phương pháp khác
            Glide.with(this)
                    .load(imageUri)
                    .override(150, 150)
                    .apply(RequestOptions.circleCropTransform())
                    .into(avatarUser);

            // Lưu trữ đường dẫn hình ảnh mới vào biến newAvatarPath
            avatarPath = imageUri.toString();
        }
    }

}