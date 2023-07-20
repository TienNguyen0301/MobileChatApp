package tien.nh.chatapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tien.nh.chatapp.databinding.ActivityChatBinding;


public class ChatActivity extends AppCompatActivity{

    TextView txtUsername, receiverText, senderText, statusUser;
    ImageView imgUser, btn_back, setting_user;

    ImageButton btnSend, btnSendImage;
    EditText textMessage;
    RecyclerView chatRecycleView;


    private  MessageAdapter messageAdapter;
    private List<Message> messages;
    private ActivityChatBinding binding;
    private static final int PICK_IMAGE_REQUEST_CODE = 2;

    private boolean isImageSent = false;
    String imagePath;

    private int documentIdCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatRecycleView = (RecyclerView) findViewById(R.id.chatRecycleView);
        txtUsername = (TextView) findViewById(R.id.chat_name);
        imgUser = (ImageView) findViewById(R.id.avatar_user);
        btnSend = (ImageButton) findViewById(R.id.imgSend) ;
        textMessage = (EditText) findViewById(R.id.textMessage) ;
        btnSendImage = (ImageButton) findViewById(R.id.send_img) ;
        btn_back = (ImageView) findViewById(R.id.btn_back);
        statusUser = (TextView) findViewById(R.id.status_User);
        setting_user = (ImageView) findViewById(R.id.setting_user);


        // Nhận thông tin user từ Intent
        Intent intent = getIntent();
        int id = intent.getIntExtra("_id", 0);
        String avatarPath = intent.getStringExtra("avatar");
        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");
        int role = intent.getIntExtra("role", 0);

        TempStorage.getInstance().setReceiverId(id);

        User user = new User(id, name, phone, email,avatarPath, role);

        messages = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        messageAdapter = new MessageAdapter(messages, currentUserId, user);
        chatRecycleView.setAdapter(messageAdapter);

        txtUsername.setText(name);
        Glide.with(this)
                .load(avatarPath)
                .error(android.R.drawable.stat_notify_error) // Ảnh hiển thị khi xảy ra lỗi trong quá trình tải hình ảnh
                .apply(RequestOptions.circleCropTransform())
                .into(imgUser);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(id);
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });
        btnSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open file chooser
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
            }
        });
        setting_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), setting_user);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.menu_user_setting, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id1 = R.id.menu_item1;
                        int id2 = R.id.menu_item2;
                        // Toast message on menu item clicked
                        if(menuItem.getItemId() == id1){
                            //chuyển sang màn hình đăng nhập
                            Intent intent = new Intent(getApplicationContext(), SearchMessageActivity.class);
                            startActivity(intent);
                            finish(); // Đóng màn hình hiện tại (nếu cần thiết)
                        } else if(menuItem.getItemId() == id2){
                            //chuyển sang màn hình đăng nhập
                            Intent intent = new Intent(getApplicationContext(), MessageHistoryActivity.class);
                            startActivity(intent);
                            finish(); // Đóng màn hình hiện tại (nếu cần thiết)
                        }
                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference userRef = database.collection(ChatDatabaseHelper.TABLE_USERS).document(String.valueOf(id));
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Xử lý lỗi khi nhận sự kiện thay đổi dữ liệu
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // Lấy giá trị của trường "status"
                    String status = snapshot.getString("status");
                   statusUser.setText(status);
                }
            }
        });


        CollectionReference messagesRef = database.collection(ChatDatabaseHelper.TABLE_MESSAGES);
        // Query for messages where sender_id is either currentUserId or receiveId, and receiver_id is either currentUserId or receiveId
        Query query = messagesRef.whereIn(ChatDatabaseHelper.COLUMN_SENDER_ID, Arrays.asList(currentUserId, id))
                .whereIn(ChatDatabaseHelper.COLUMN_RECEIVER_ID, Arrays.asList(currentUserId, id));

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Handle the error if there's any
                    Log.e("ChatActivity", "Error getting messages: " + error.getMessage());
                    return;
                }

                if (querySnapshot != null) {
                    // Lấy danh sách tin nhắn từ querySnapshot
                    messages.clear(); // Clear the existing list
                    List<Message> newMessages = querySnapshot.toObjects(Message.class);
                    messages.addAll(newMessages);

                    // Cập nhật danh sách tin nhắn trong adapter
                    messageAdapter.notifyDataSetChanged();
                    chatRecycleView.scrollToPosition(messages.size() - 1);
                }
            }
        });

    }

    private void updateUI() {
        messageAdapter.notifyDataSetChanged();
        Log.d("ChatActivity", "Scrolling to position: " + (messages.size() - 1)); // Kiểm tra log
        binding.chatRecycleView.scrollToPosition(messages.size() - 1);
    }

    @Override
    public void onBackPressed() {
        // Kiểm tra lớp hiện tại của Activity
        if (getClass() == ChatActivity.class) {
            // Xử lý tại đây khi người dùng nhấn nút Back trong ChatActivity
            // Ví dụ: Đóng ChatActivity và quay lại trang trước đó
            finish(); // Đóng ChatActivity
        } else {
            super.onBackPressed(); // Thực hiện hành động mặc định của nút Back
        }
    }

//    // Hàm lọc các tin nhắn có senderId hoặc receiverId tương ứng với thông tin người gửi và người nhận
//    private List<Message> filterMessages(List<Message> allMessages, int senderId, int receiverId) {
//        List<Message> filteredMessages = new ArrayList<>();
//        for (Message message : allMessages) {
//            if (message.getSender_id() == senderId || message.getSender_id() == receiverId) {
//                filteredMessages.add(message);
//            }
//        }
//        return filteredMessages;
//    }

    private void createNewMessage(CollectionReference  userRef, String documentId, int friendship_id, int senderId, int receiverId, String messageText, String status, String timestamp) {
        // Tăng giá trị documentIdCounter lên 1
        documentIdCounter++;

        // Tạo chuỗi documentId dựa trên giá trị documentIdCounter
        int Id = Integer.valueOf(documentIdCounter);

        // Tạo tài liệu mới với documentId và các trường dữ liệu khác
        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("documentId", Id);
        messageData.put("friendship_id", friendship_id);
        messageData.put("sender_id", senderId);
        messageData.put("receiver_id", receiverId);
        messageData.put("message_text", messageText);
        messageData.put("status", status);
        messageData.put("timestamp", timestamp);

        userRef.document(documentId).set(messageData)
                .addOnSuccessListener(aVoid -> {
                    // Tài liệu đã được tạo thành công
                    Toast.makeText(getApplicationContext(), "Send message successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi ghi dữ liệu
                    Toast.makeText(getApplicationContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendMessageFireBase(int friendship_id, int senderId, int receiverId, String messageText, String status, String timestamp) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersRef = database.collection(ChatDatabaseHelper.TABLE_MESSAGES);


        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                long newId = documents.size() + 1; // Tạo custom ID tăng dần
                String documentId = String.format("%03d", newId);

                // Tạo tài liệu mới với custom ID
                createNewMessage(usersRef, String.valueOf(documentId), friendship_id, senderId, receiverId, messageText, status, timestamp);
            } else {
                // Lỗi khi truy vấn bộ sưu tập "messages"
                Toast.makeText(getApplicationContext(), "Failed to query messages collection", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendMessage(int receiveId) {
        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(getApplicationContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);
        String query = "SELECT _id FROM friendships " +
                "WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
        String[] selectionArgs = {String.valueOf(receiveId), String.valueOf(currentUserId), String.valueOf(currentUserId), String.valueOf(receiveId)};

        Cursor cursor = db.rawQuery(query, selectionArgs);
        // Lấy thời gian hiện tại
        Date currentTime = new Date();

        // Định dạng thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(currentTime);

        if (cursor.moveToFirst()) {
            int friendshipId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            // Sử dụng friendshipId cho các xử lý tiếp theo
            String message = textMessage.getText().toString();

            if (message.length() > 0) {
                ContentValues values = new ContentValues();
                values.put("sender_id", currentUserId);
                values.put("receiver_id", receiveId);
                values.put("friendship_id", friendshipId);
                values.put("message_text", message);
                values.put("timestamp", formattedTime);
                values.put("status", "status");
                sendMessageFireBase(friendshipId,currentUserId,receiveId, message,"status",formattedTime);
                long insertedId = db.insert("messages", null, values);
                if (insertedId > -1) {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    // Add the message to the local list
                    messages.add(new Message(friendshipId, currentUserId, receiveId, message, "status", formattedTime));

                    // Notify the adapter that the data has changed
                    messageAdapter.notifyDataSetChanged();
                    chatRecycleView.scrollToPosition(messages.size() - 1);
                    textMessage.setText("");
                    updateUI();
                } else {
                    Toast.makeText(this,"Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (!isImageSent) { // Kiểm tra biến cờ trước khi gửi ảnh
                // Lấy đường dẫn hình ảnh từ kết quả
                Uri selectedImageUri = data.getData();

                // Lưu trữ đường dẫn hình ảnh mới vào biến newAvatarPath
                imagePath = getImagePathFromUri(selectedImageUri);

                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                int currentUserId = sharedPreferences.getInt("currentUserId", 0);
                Intent intent = getIntent();

                int id = intent.getIntExtra("_id", 0);


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
                        sendImageFireBase(imageUrl);
//                        saveImageToDatabase(imageUrl,currentUserId,id);
                    }).addOnFailureListener(e -> {
                        // Failed to get the download URL
                        Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    // Failed to upload the image
                    Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });


            }
        }
    }

    private void sendImageFireBase(String image) {
        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(getApplicationContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Intent intent = getIntent();
        int receiveId = intent.getIntExtra("_id", 0);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);
        String query = "SELECT _id FROM friendships " +
                "WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";

        String[] selectionArgs = {String.valueOf(receiveId), String.valueOf(currentUserId), String.valueOf(currentUserId), String.valueOf(receiveId)};

        Cursor cursor = db.rawQuery(query, selectionArgs);
        // Lấy thời gian hiện tại
        Date currentTime = new Date();

        // Định dạng thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(currentTime);

        // Chuyển đổi Uri thành String
        String imagePathString = image.toString();

        if (cursor.moveToFirst()) {
            int friendshipId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            // Sử dụng friendshipId cho các xử lý tiếp theo
            sendMessageImageFireBase(friendshipId, currentUserId, receiveId,imagePathString, "status", formattedTime);
            updateUI();
        }
    }

    private void sendMessageImageFireBase(int friendship_id, int senderId, int receiverId, String image, String status, String timestamp) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersRef = database.collection(ChatDatabaseHelper.TABLE_MESSAGES);


        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                long newId = documents.size() + 1; // Tạo custom ID tăng dần
                String documentId = String.format("%03d", newId);

                // Tạo tài liệu mới với custom ID
                createNewMessageImage(usersRef, String.valueOf(documentId), friendship_id, senderId, receiverId, image, status, timestamp);
            } else {
                // Lỗi khi truy vấn bộ sưu tập "messages"
                Toast.makeText(getApplicationContext(), "Failed to query messages collection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewMessageImage(CollectionReference  userRef, String documentId, int friendship_id, int senderId, int receiverId, String image, String status, String timestamp) {
        // Tăng giá trị documentIdCounter lên 1
        documentIdCounter++;

        // Tạo chuỗi documentId dựa trên giá trị documentIdCounter
        int Id = Integer.valueOf(documentIdCounter);

        // Tạo tài liệu mới với documentId và các trường dữ liệu khác
        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("documentId", Id);
        messageData.put("friendship_id", friendship_id);
        messageData.put("sender_id", senderId);
        messageData.put("receiver_id", receiverId);
        messageData.put("image", image);
        messageData.put("status", status);
        messageData.put("timestamp", timestamp);

        userRef.document(documentId).set(messageData)
                .addOnSuccessListener(aVoid -> {
                    // Tài liệu đã được tạo thành công
                    Toast.makeText(getApplicationContext(), "Send message successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi ghi dữ liệu
                    Toast.makeText(getApplicationContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

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

}

