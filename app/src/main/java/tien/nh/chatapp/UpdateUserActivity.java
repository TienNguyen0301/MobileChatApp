package tien.nh.chatapp;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateUserActivity extends AppCompatActivity {

    EditText username, phoneUser, roleUser, emailUser;
    ImageView avatarUser;
    Button btnUpdateUser, btnHuy;
    private boolean isTextChanged = false;
    private static final int UPDATE_USER_REQUEST_CODE = 1;
    private static final int PICK_IMAGE_REQUEST_CODE = 2;
    private String newAvatarPath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        username = (EditText) findViewById(R.id.username);
        emailUser = (EditText) findViewById(R.id.email);
        phoneUser = (EditText) findViewById(R.id.phone);
        roleUser = (EditText) findViewById(R.id.role);
        avatarUser = (ImageView) findViewById(R.id.avatar);
        btnUpdateUser = (Button) findViewById(R.id.btnUpdateUser);
        btnHuy = (Button) findViewById(R.id.btnHuy);


        // Nhận thông tin user từ Intent
        Intent intent = getIntent();
        int id = intent.getIntExtra("userId", 0);
        String name = intent.getStringExtra("userName");
        String email = intent.getStringExtra("userEmail");
        String avatarPath = intent.getStringExtra("avatar");
        String phone = intent.getStringExtra("phone");
        int role = intent.getIntExtra("role", 0);

        username.setText(name);
        emailUser.setText(email);
        phoneUser.setText(phone);
        roleUser.setText(String.valueOf(role));

        //set giá trị mặc định của avatar khi người kh thay đổi avatar mới
        newAvatarPath = avatarPath;

        // Hiển thị hình ảnh avatar
        Glide.with(this)
                .load(avatarPath)
                .error(android.R.drawable.stat_notify_error) // Ảnh hiển thị khi xảy ra lỗi trong quá trình tải hình ảnh
                .apply(RequestOptions.circleCropTransform())
                .into(avatarUser);

        avatarUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open file chooser
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
            }
        });


        btnUpdateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // At least one text field has been modified
                // Get the updated values from the EditText fields
                String updatedUserName = username.getText().toString();
                String updatedUserEmail = emailUser.getText().toString();
                String updatedPhone = phoneUser.getText().toString();
                int roleU = Integer.parseInt(roleUser.getText().toString());
                if (isTextChanged || !updatedUserName.equals(name) || !updatedUserEmail.equals(email) || !updatedPhone.equals(phone) || roleU != role || !newAvatarPath.isEmpty()) {

                    // Perform the necessary database update operations with the updated values
                    ChatDatabaseHelper dbHelper = new ChatDatabaseHelper(UpdateUserActivity.this);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    // Prepare the update query
                    ContentValues values = new ContentValues();
                    values.put("name", updatedUserName);
                    values.put("email", updatedUserEmail);
                    values.put("phone", updatedPhone);
                    values.put("role", roleU);
                    if (newAvatarPath != null) {
                        // Avatar has been changed
                        values.put("avatar", newAvatarPath);
                    }
                    // Update the user record in the database
//                    db.update("users", values, "_id = ?", new String[]{String.valueOf(id)});
//                    db.close();

                    // Cập nhật thông tin người dùng trên Firebase Firestore
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    DocumentReference userRef = firestore.collection(ChatDatabaseHelper.TABLE_USERS).document(String.valueOf(id));
                    userRef.update("name", updatedUserName,
                                    "email", updatedUserEmail,
                                    "phone", updatedPhone,
                                    "avatar", newAvatarPath,
                                    "role", roleU)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Cập nhật thành công trên Firebase Firestore
                                    // Tiếp tục xử lý hoặc hiển thị thông báo thành công
                                    // Create an intent to pass back the updated user information
                                    Intent intent = new Intent(getApplication(), UserManagementActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý lỗi khi cập nhật trên Firebase Firestore
                                    // Hiển thị thông báo lỗi hoặc thực hiện các thao tác khác
                                }
                            });



                    // Reset the flag after processing the changes
                    isTextChanged = false;
                } else {
                    // No text fields have been modified
                    // You can show an appropriate message to the user or perform any other action
                }

            }
        });

        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to pass back the updated user information
                Intent intent = new Intent(getApplication(), UserManagementActivity.class);
                startActivity(intent);
            }
        });


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
            newAvatarPath = imageUri.toString();
        }
    }


}