package tien.nh.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.regex.Pattern;





import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import android.net.Uri;




public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnRegister, btnUpAvatar;
    EditText userName, userEmail, userPassword, userPhone;
    ChatDatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //register firebase
        userName = (EditText) findViewById(R.id.editTextName);
        userEmail = (EditText) findViewById(R.id.editTextEmail);
        userPhone = (EditText) findViewById(R.id.editTextPhone);
        userPassword = (EditText) findViewById(R.id.editTextPassword);

        // Initialize database helper
        dbHelper = new ChatDatabaseHelper(this);

        // Initialize buttons and set click listeners
        btnRegister = findViewById(R.id.btn_register);
        btnUpAvatar = findViewById(R.id.btnUploadAvatar);
        btnRegister.setOnClickListener(this);
        btnUpAvatar.setOnClickListener(this);

        // Initialize EditText fields
        userName = findViewById(R.id.editTextName);
        userEmail = findViewById(R.id.editTextEmail);
        userPhone = findViewById(R.id.editTextPhone);
        userPassword = findViewById(R.id.editTextPassword);

        // Trong phương thức onCreate()
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            // Xử lý hình ảnh đã chọn
                            Uri selectedImageUri = result.getData().getData();
                            handleSelectedImage(selectedImageUri);
                        }
                    }
                });


    }

    private void handleSelectedImage(Uri imageUri) {
        selectedImageUri = imageUri;
        // Load and display the image using Glide library
        ImageView imageView = findViewById(R.id.imageView);
        Glide.with(this).load(selectedImageUri).override(150, 150).into(imageView);
    }

    public boolean isValidEmail(String email) {
        // Biểu thức chính quy để kiểm tra email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);

        // Kiểm tra tính hợp lệ của email
        return pattern.matcher(email).matches();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_register) {

            // Retrieve user input values
            String name = userName.getText().toString();
            String email = userEmail.getText().toString();
            String phone = userPhone.getText().toString();
            String password = userPassword.getText().toString();

            boolean isValid = isValidEmail(email);
            if(isValid) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Insert user data into the "users" table
                ContentValues values = new ContentValues();
                String imagePath = selectedImageUri.toString();
                values.put("name", name);
                values.put("email", email);
                values.put("phone", phone);
                values.put("password", password);
                values.put("avatar", imagePath);

//                values.put("role", 2);

                long insertedId = db.insert("users", null, values);
                // Reset the selected image URI for the next registration
                selectedImageUri = null;

                // Start the main activity
                startActivity(new Intent(this, MainActivity.class));
            } else {
                userEmail.requestFocus();
                Toast.makeText(getApplicationContext(), "Email không hợp lệ", Toast.LENGTH_SHORT).show();

            }


        }

        if (v.getId() == R.id.btnUploadAvatar) {
            // Open file chooser
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);

        }

    }
}



