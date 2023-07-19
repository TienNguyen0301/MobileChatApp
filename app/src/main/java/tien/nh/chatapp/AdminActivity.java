package tien.nh.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity{
    ImageButton btnLogout;


    TextView quanly_user, quanly_relationship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnLogout = (ImageButton) findViewById(R.id.btnLogout);
        quanly_user = (TextView) findViewById(R.id.quanly_user);
        quanly_relationship = (TextView) findViewById(R.id.quanly_relationship) ;

        quanly_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), UserManagementActivity.class);
                startActivity(intent);
            }
        });
        quanly_relationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), RelationshipManagerActivity.class);
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




}