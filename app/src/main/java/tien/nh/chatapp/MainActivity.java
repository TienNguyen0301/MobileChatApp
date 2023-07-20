package tien.nh.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;


import java.util.HashMap;
import java.util.List;


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

        fetchDataFromFirestore();

        // Đăng ký lắng nghe sự thay đổi dữ liệu trên Firebase Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference usersRef = firestore.collection(ChatDatabaseHelper.TABLE_USERS);
        CollectionReference friendshipsRef = firestore.collection(ChatDatabaseHelper.TABLE_FRIENDSHIPS);
        CollectionReference messagesRef = firestore.collection(ChatDatabaseHelper.TABLE_MESSAGES);

        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Xử lý lỗi khi nhận sự kiện thay đổi dữ liệu
                    return;
                }

                // Kiểm tra nếu có sự thay đổi dữ liệu
                if (value != null && !value.isEmpty()) {
                    // Gọi lại hàm fetchDataFromFirestore để nạp lại dữ liệu
                    fetchDataFromFirestore();
                }
            }
        });
        friendshipsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Xử lý lỗi khi nhận sự kiện thay đổi dữ liệu
                    return;
                }

                // Kiểm tra nếu có sự thay đổi dữ liệu
                if (value != null && !value.isEmpty()) {
                    // Gọi lại hàm fetchDataFromFirestore để nạp lại dữ liệu
                    fetchDataFromFirestore();
                }
            }
        });
        messagesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Xử lý lỗi khi nhận sự kiện thay đổi dữ liệu
                    return;
                }

                // Kiểm tra nếu có sự thay đổi dữ liệu
                if (value != null && !value.isEmpty()) {
                    // Gọi lại hàm fetchDataFromFirestore để nạp lại dữ liệu
                    fetchDataFromFirestore();
                }
            }
        });

//        resetDatabase(this);

    }





    // Nạp dữ liệu từ firebase xuoogns sqlite
    private void fetchDataFromFirestore() {
        FirebaseFirestore firestoreUsers = FirebaseFirestore.getInstance();
        firestoreUsers.collection(ChatDatabaseHelper.TABLE_USERS).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Xóa dữ liệu cũ trong SQLite để cập nhật lại từ dữ liệu Firebase
                        dbHelper.deleteAllUsers();

                        // Duyệt qua danh sách các DocumentSnapshot
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Lấy dữ liệu từ DocumentSnapshot
                            int documentId = Integer.parseInt(documentSnapshot.getId());
                            String nameFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_NAME);
                            String emailFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_EMAIL);
                            String phoneFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_PHONE);
                            String passwordFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_PASSWORD);
                            String avatarFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_AVATAR);
                            Long role = documentSnapshot.getLong(ChatDatabaseHelper.COLUMN_USER_ROLE);
                            int roleFB = role != null ? role.intValue() : 0;

                            // Thực hiện các thao tác cập nhật cơ sở dữ liệu SQLite tại đây
                            // Ví dụ: thêm mới hoặc cập nhật bản ghi trong SQLite

                            SQLiteDatabase db = dbHelper.getWritableDatabase();

                            // Insert user data into the "users" table
                            ContentValues values = new ContentValues();
                            values.put("_id", documentId);
                            values.put("name", nameFb);
                            values.put("email", emailFb);
                            values.put("phone", phoneFb);
                            values.put("password", passwordFb);
                            values.put("avatar", avatarFb);
                            values.put("role", roleFB);

                            // Thêm mới người dùng vào cơ sở dữ liệu SQLite
                            dbHelper.insertUser(documentId, nameFb, emailFb, phoneFb, passwordFb, avatarFb, roleFB);

                        }

                        // Sau khi hoàn thành việc cập nhật cơ sở dữ liệu SQLite
                        // có thể cập nhật giao diện hoặc thực hiện các thao tác khác
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý lỗi khi lấy dữ liệu từ Firebase
                        Log.d("ERROR", "Lỗi khi lấy dữ liệu từ Firebase");
                    }
                });

        FirebaseFirestore firestoreFriendShips = FirebaseFirestore.getInstance();
        firestoreFriendShips.collection(ChatDatabaseHelper.TABLE_FRIENDSHIPS).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Xóa dữ liệu cũ trong SQLite để cập nhật lại từ dữ liệu Firebase
                        dbHelper.deleteAllFriendShips();

                        // Duyệt qua danh sách các DocumentSnapshot
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Lấy dữ liệu từ DocumentSnapshot
                            int documentId = Integer.parseInt(documentSnapshot.getId());
                            Long user1 = documentSnapshot.getLong(ChatDatabaseHelper.COLUMN_FRIENDSHIP_USER1);
                            int user1Fb = user1 != null ? user1.intValue() : 0;
                            Long user2 = documentSnapshot.getLong(ChatDatabaseHelper.COLUMN_FRIENDSHIP_USER2);
                            int user2Fb = user2 != null ? user2.intValue() : 0;
                            String friendship_statusFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_FRIENDSHIP_STATUS);
                            String created_dateFb = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_FRIENDSHIP_CREATED_DATE);

                            // Thực hiện các thao tác cập nhật cơ sở dữ liệu SQLite tại đây
                            // Ví dụ: thêm mới hoặc cập nhật bản ghi trong SQLite

                            SQLiteDatabase db = dbHelper.getWritableDatabase();

                            // Insert user data into the "users" table
                            ContentValues values = new ContentValues();
                            values.put("_id", documentId);
                            values.put("user1", user1Fb);
                            values.put("user2", user2Fb);
                            values.put("friendship_status", friendship_statusFb);
                            values.put("friendship_created_date", created_dateFb);


                            // Thêm mới người dùng vào cơ sở dữ liệu SQLite
                            dbHelper.insertFriendship(documentId, user1Fb, user2Fb, friendship_statusFb, created_dateFb);

                        }

                        // Sau khi hoàn thành việc cập nhật cơ sở dữ liệu SQLite
                        // có thể cập nhật giao diện hoặc thực hiện các thao tác khác
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý lỗi khi lấy dữ liệu từ Firebase
                        Log.d("ERROR", "Lỗi khi lấy dữ liệu từ Firebase");
                    }
                });

        FirebaseFirestore firestoreMessage = FirebaseFirestore.getInstance();
        firestoreMessage.collection(ChatDatabaseHelper.TABLE_MESSAGES).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Xóa dữ liệu cũ trong SQLite để cập nhật lại từ dữ liệu Firebase
                        dbHelper.deleteAllMessages();

                        // Duyệt qua danh sách các DocumentSnapshot
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Lấy dữ liệu từ DocumentSnapshot
                            int documentId = Integer.parseInt(documentSnapshot.getId());
                            Long frienshipid = documentSnapshot.getLong(ChatDatabaseHelper.COLUMN_FRIENDSHIP_ID);
                            int friendShip_idFb = frienshipid != null ? frienshipid.intValue() : 0;
                            Long senderid = documentSnapshot.getLong(ChatDatabaseHelper.COLUMN_SENDER_ID);
                            int senderId_Fb = senderid != null ? senderid.intValue() : 0;
                            Long receiverid = documentSnapshot.getLong(ChatDatabaseHelper.COLUMN_RECEIVER_ID);
                            int receiverId_Fb = receiverid != null ? receiverid.intValue() : 0;
                            String messageText = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_MESSAGE_TEXT);
                            String status = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_MESSAGE_STATUS);
                            String timestamp = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_TIMESTAMP);


                            // Thực hiện các thao tác cập nhật cơ sở dữ liệu SQLite tại đây
                            // Ví dụ: thêm mới hoặc cập nhật bản ghi trong SQLite

                            SQLiteDatabase db = dbHelper.getWritableDatabase();

                            // Insert user data into the "users" table
                            ContentValues values = new ContentValues();
                            values.put("_id", documentId);
                            values.put("friendship_id", friendShip_idFb);
                            values.put("sender_id", senderId_Fb);
                            values.put("receiver_id", receiverId_Fb);
                            values.put("message_text", messageText);
                            values.put("status", status);
                            values.put("timestamp", timestamp);

                            // Thêm mới người dùng vào cơ sở dữ liệu SQLite
                            dbHelper.insertMessage(documentId, friendShip_idFb, senderId_Fb,receiverId_Fb, messageText,status,timestamp);

                        }

                        // Sau khi hoàn thành việc cập nhật cơ sở dữ liệu SQLite
                        // có thể cập nhật giao diện hoặc thực hiện các thao tác khác
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý lỗi khi lấy dữ liệu từ Firebase
                        Log.d("ERROR", "Lỗi khi lấy dữ liệu từ Firebase");
                    }
                });


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

                        // get currentUserId
                        int id = loginCursor.getColumnIndexOrThrow("_id");
                        int currentUserId = loginCursor.getInt(id);

                        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putInt("currentUserId", currentUserId);
                        editor.apply();

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

                        // get currentUserId
                        int id = loginCursor.getColumnIndexOrThrow("_id");
                        int currentUserId = loginCursor.getInt(id);

                        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putInt("currentUserId", currentUserId);
                        editor.apply();

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

}