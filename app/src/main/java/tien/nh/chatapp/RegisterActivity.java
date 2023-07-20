package tien.nh.chatapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.widget.Toast;
import android.content.ContentValues;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;





public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnRegister, btnUpAvatar;
    EditText userName, userEmail, userPassword, userPhone;
    ChatDatabaseHelper dbHelper;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    private User user;

    String imagePath;
    // Khởi tạo CallbackManager trong phương thức onCreate


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
                            imagePath = getImagePathFromUri(selectedImageUri);

                        }
                    }
                });



    }





    // Phương thức để lấy đường dẫn của ảnh từ Uri
    private String getImagePathFromUri(Uri uri) {
        String imagePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return imagePath;
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

    public boolean isValidPhoneNumber(String phoneNumber) {
        // Remove non-numeric characters from the phone number
        String numericPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

        // Check if the resulting string contains only digits
        if (!numericPhoneNumber.matches("[0-9]+")) {
            return false;
        }

        // Optionally, you can set rules for the minimum and maximum length of the phone number
        int minPhoneNumberLength = 10; // For example, a valid phone number must have at least 10 digits
        int maxPhoneNumberLength = 15; // For example, a valid phone number must not exceed 15 digits

        int phoneNumberLength = numericPhoneNumber.length();
        return phoneNumberLength >= minPhoneNumberLength && phoneNumberLength <= maxPhoneNumberLength;
    }


    private void registerUser(String name, String phone, String password, String email, String avatar, String status, int role) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersRef = database.collection(ChatDatabaseHelper.TABLE_USERS);

        // Kiểm tra xem bộ sưu tập "users" có bất kỳ tài liệu nào hay không
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot.isEmpty()) {
                    // Bộ sưu tập "users" chưa có tài liệu nào, tạo một tài liệu mới với ID là 1
                    createNewUser(usersRef.document("1"), name, phone, password, email, avatar, status, role);
                } else {
                    // Bộ sưu tập "users" đã có tài liệu, sử dụng transaction để tăng giá trị ID lên 1 và tạo tài liệu mới
                    createUserWithIncrementedId(usersRef, name, phone, password, email, avatar, status, role);
                }
            } else {
                // Lỗi khi truy vấn bộ sưu tập "users"
                Toast.makeText(getApplicationContext(), "Failed to query users collection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewUser(DocumentReference userRef, String name, String phone, String password, String email, String avatar, String status, int role) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);
        userData.put("password", password);
        userData.put("email", email);
        userData.put("avatar", avatar);
        userData.put("status", status);
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

    private void createUserWithIncrementedId(CollectionReference usersRef, String name, String phone, String password, String email, String avatar, String status, int role) {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                if (documents.isEmpty()) {
                    // Bộ sưu tập "users" chưa có tài liệu nào, tạo tài liệu mới với ID là 1
                    createNewUser(usersRef.document("1"), name, phone, password, email, avatar, status, role);
                } else {
                    // Lấy tài liệu cuối cùng trong danh sách và tăng giá trị ID lên 1
                    DocumentSnapshot lastDocument = documents.get(documents.size() - 1);
                    String lastUserId = lastDocument.getId();
                    long newId = Long.parseLong(lastUserId) + 1;

                    // Tạo tài liệu mới với ID mới
                    createNewUser(usersRef.document(String.valueOf(newId)), name, phone, password, email, avatar, status, role);
                }
            } else {
                // Lỗi khi truy vấn bộ sưu tập "users"
                Toast.makeText(getApplicationContext(), "Failed to query users collection", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_register) {

            // Retrieve user input values
            String name = userName.getText().toString();
            String email = userEmail.getText().toString();
            String phone = userPhone.getText().toString();
            String password = userPassword.getText().toString();

            // Check if all required fields are filled
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() ) {
                Toast.makeText(getApplicationContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate the phone number format
            boolean isValidPhoneNumber = isValidPhoneNumber(phone);
            if (!isValidPhoneNumber) {
                Toast.makeText(getApplicationContext(), "Invalid phone number format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate the email format
            boolean isValidEmail = isValidEmail(email);
            if (!isValidEmail) {
                Toast.makeText(getApplicationContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }else  {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Insert user data into the "users" table
                ContentValues values = new ContentValues();
//                String imagePath = selectedImageUri.toString();
                values.put("name", name);
                values.put("email", email);
                values.put("phone", phone);
                values.put("password", password);
                values.put("avatar", imagePath);

//                registerUser(name,phone,password,email,imagePath,0);

                // Create a Firebase Storage reference
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                // Generate a unique file name for the image
                String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";

                // Create a reference to the image file in Firebase Storage
                StorageReference imageRef = storageRef.child("avatars/" + fileName);

                // Upload the image to Firebase Storage
                UploadTask uploadTask = imageRef.putFile(selectedImageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Get the public download URL of the uploaded image
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save the download URL to Firestore or perform any other desired operations
                        String imageUrl = uri.toString();
                        registerUser(name, phone, password, email, imageUrl, "offline", 0);
                    }).addOnFailureListener(e -> {
                        // Failed to get the download URL
                        Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    // Failed to upload the image
                    Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });





//                values.put("role", 2);



                // Start the main activity
                startActivity(new Intent(this, MainActivity.class));
            }
//            else {
//                userEmail.requestFocus();
//                Toast.makeText(getApplicationContext(), "Email không hợp lệ", Toast.LENGTH_SHORT).show();
//
//            }

        }

        if (v.getId() == R.id.btnUploadAvatar) {
            // Open file chooser
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);

        }


    }
}



