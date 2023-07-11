package tien.nh.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.widget.Toast;
import android.database.Cursor;

import android.content.SharedPreferences;

import java.io.File;

import android.util.Log;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView registerId;
    Button btnLogin;

    EditText editEmail, editPassword;

    ChatDatabaseHelper dbHelper = new ChatDatabaseHelper(this); // Replace 'this' with your activity or fragment context

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerId = (TextView) findViewById(R.id.register_id);
        btnLogin = (Button) findViewById(R.id.login_btn);

        editEmail = (EditText) findViewById(R.id.editTextEmail);
        editPassword = (EditText) findViewById(R.id.editTextPassword);

        registerId.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

//        resetDatabase(this);
    }
    // Inside a separate class or method
    public void resetDatabase(Context context) {
        // Use the context parameter here
        context.deleteDatabase("chat.db");
    }

    private String extractFieldValue(String output, String fieldName) {
        String fieldValue = "";

        // Tìm vị trí của trường trong chuỗi output
        int fieldStartIndex = output.indexOf(fieldName);
        if (fieldStartIndex != -1) {
            // Tìm vị trí của dấu hai chấm (:) sau trường
            int colonIndex = output.indexOf(":", fieldStartIndex);
            if (colonIndex != -1) {
                // Tìm vị trí của dấu phẩy (,) sau trường
                int commaIndex = output.indexOf(",", colonIndex);
                if (commaIndex != -1) {
                    // Lấy giá trị trường từ chuỗi output
                    fieldValue = output.substring(colonIndex + 2, commaIndex);
                }
            }
        }

        return fieldValue;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.register_id) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
        if (v.getId() == R.id.login_btn) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Truy vấn SQL để kiểm tra đăng nhập
            String inputEmail = editEmail.getText().toString();
            String inputPassword = editPassword.getText().toString();

            String query = "SELECT * FROM users WHERE email = ? AND password = ?";

            String[] selectionArgs = {inputEmail, inputPassword};
            Cursor loginCursor = db.rawQuery(query, selectionArgs);

                if (loginCursor.moveToFirst()) {

                    // Đăng nhập thành công
                    // Thực hiện các tác vụ sau khi đăng nhập thành công
                    // Lấy giá trị role từ cột tương ứng trong Cursor
                    int roleColumnIndex = loginCursor.getColumnIndexOrThrow("role");
                    int roleUser = loginCursor.getInt(roleColumnIndex);
                    // Kiểm tra role và chuyển đổi hoạt động tương ứng

                    if (roleUser == 0) {
                        // Role là 0, chuyển đến hoạt động ActivityA
                        // Lấy thông tin user từ cursor
                        int nameColumnIndex = loginCursor.getColumnIndexOrThrow("name");
                        String name = loginCursor.getString(nameColumnIndex);

                        int emailColumnIndex = loginCursor.getColumnIndexOrThrow("email");
                        String email = loginCursor.getString(emailColumnIndex);

                        int avatarColumnIndex = loginCursor.getColumnIndexOrThrow("avatar");
                        String avatarPath = loginCursor.getString(avatarColumnIndex);


                        // get currentUserId
                        int id = loginCursor.getColumnIndexOrThrow("_id");
                        int currentUserId = loginCursor.getInt(id);

                        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putInt("currentUserId", currentUserId);
                        editor.apply();

                        // ... và các thông tin khác
                        // Thực hiện các tác vụ sau khi đăng nhập thành công và lấy thông tin user
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("avatar", avatarPath);
                        intent.putExtra("role", roleUser);

                        startActivity(intent);

                    } else if(roleUser == 2){

                        // Lấy thông tin user từ cursor
                        int nameColumnIndex = loginCursor.getColumnIndexOrThrow("name");
                        String name = loginCursor.getString(nameColumnIndex);

                        int emailColumnIndex = loginCursor.getColumnIndexOrThrow("email");
                        String email = loginCursor.getString(emailColumnIndex);

                        int avatarColumnIndex = loginCursor.getColumnIndexOrThrow("avatar");
                        String avatarPath = loginCursor.getString(avatarColumnIndex);

                        // ... và các thông tin khác
                        // Thực hiện các tác vụ sau khi đăng nhập thành công và lấy thông tin user
                        Intent intent = new Intent(this, AdminActivity.class);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("avatar", avatarPath);
                        intent.putExtra("role", roleUser);


                        startActivity(intent);

                    }else if(roleUser == 1) {

                        int nameColumnIndex = loginCursor.getColumnIndexOrThrow("name");
                        String name = loginCursor.getString(nameColumnIndex);

                        int emailColumnIndex = loginCursor.getColumnIndexOrThrow("email");
                        String email = loginCursor.getString(emailColumnIndex);

                        int avatarColumnIndex = loginCursor.getColumnIndexOrThrow("avatar");
                        String avatarPath = loginCursor.getString(avatarColumnIndex);

                        // ... và các thông tin khác
                        // Thực hiện các tác vụ sau khi đăng nhập thành công và lấy thông tin user
                        Intent intent = new Intent(this, AdminLowerActivity.class);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("avatar", avatarPath);
                        intent.putExtra("role", roleUser);


                        startActivity(intent);
                    }
            } else {
                    Toast.makeText(getApplicationContext(), "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                }
            loginCursor.close();
        }
    }
}