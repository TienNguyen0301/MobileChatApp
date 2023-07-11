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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

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