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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tien.nh.chatapp.databinding.ActivityChatBinding;


public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    TextView txtUsername, receiverText, senderText, statusUser;
    ImageView imgUser, btn_back, setting_user;

    ImageButton btnSend, btnSendImage;
    EditText textMessage;
    RecyclerView chatRecycleView;


    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private static final int REQUEST_IMAGE_PICK = 1;

    private  MessageAdapter messageAdapter;
    private List<Message> messages;
    private ActivityChatBinding binding;
    private static final int PICK_IMAGE_REQUEST_CODE = 2;

    private static final int LOADER_ID = 1; // ID của CursorLoader
    private boolean isImageSent = false;
    String imagePath;

    private int documentIdCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        setContentView(R.layout.activity_chat);

        chatRecycleView = (RecyclerView) findViewById(R.id.chatRecycleView);
        txtUsername = (TextView) findViewById(R.id.chat_name);
        imgUser = (ImageView) findViewById(R.id.avatar_user);
        btnSend = (ImageButton) findViewById(R.id.imgSend) ;
        textMessage = (EditText) findViewById(R.id.textMessage) ;
        btnSendImage = (ImageButton) findViewById(R.id.send_img) ;
        btn_back = (ImageView) findViewById(R.id.btn_back);
        statusUser = (TextView) findViewById(R.id.status_User);
        setting_user = (ImageView) findViewById(R.id.setting_user);


//        init();

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
//        messageAdapter = new MessageAdapter(user);

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


//        DocumentReference userRef = database.collection(ChatDatabaseHelper.TABLE_USERS).document(String.valueOf(id));
//        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    // Xử lý lỗi khi nhận sự kiện thay đổi dữ liệu
//                    return;
//                }
//
//                if (snapshot != null && snapshot.exists()) {
//                    // Lấy giá trị của trường "status"
//                    String status = snapshot.getString("status");
//                   statusUser.setText(status);
//                }
//            }
//        });


//        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);


        CollectionReference messagesRef = database.collection(ChatDatabaseHelper.TABLE_MESSAGES);
        messagesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Xử lý lỗi nếu có
                    Log.e("ChatActivity", "Error getting messages: " + error.getMessage());
                    return;
                }

                if (querySnapshot != null) {
                    // Lấy danh sách tin nhắn từ querySnapshot
                    List<Message> messages = querySnapshot.toObjects(Message.class);
                    // Cập nhật danh sách tin nhắn trong adapter
                    messageAdapter.updateData(messages);
                    chatRecycleView.scrollToPosition(messages.size() - 1);
                }
            }
        });



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


    private void sendMessage(int receiveId) {
        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(getApplicationContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);
        String query = "SELECT _id FROM friendships " +
                "WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
//        String query = "SELECT users.* FROM users " +
//                "INNER JOIN friendships ON (users._id = friendships.user1 OR users._id = friendships.user2) " +
//                "WHERE (friendships.user1 = ? OR friendships.user2 = ?) AND friendships.friendship_status = 'accepted' " +
//                "AND users._id != ?";
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
                    getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    textMessage.setText("");
                    updateUI(); // Thêm dòng này để cập nhật giao diện người dùng
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri baseUri = Uri.parse("content://tien.nh.chatapp.provider"); // Địa chỉ URI cơ sở
        Uri uriMessage = Uri.withAppendedPath(baseUri, ChatDatabaseHelper.TABLE_MESSAGES); // URI cho bảng tin nhắn
        // Tạo và trả về CursorLoader cho bảng tin nhắn
        Uri uri = uriMessage; // Thay thế CONTENT_URI bằng URI tương ứng của bảng tin nhắn trong cơ sở dữ liệu SQLite của bạn
        String[] projection = null; // Các cột bạn muốn lấy từ bảng (null sẽ lấy tất cả các cột)
        String selection = null; // Điều kiện lựa chọn (null nếu không có)
        String[] selectionArgs = null; // Đối số cho điều kiện lựa chọn (null nếu không có)
        String sortOrder = null; // Thứ tự sắp xếp (null nếu không cần)
        return new CursorLoader(this, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Xử lý khi dữ liệu đã tải xong vào Cursor
        if (data != null) {
            // Đọc dữ liệu từ Cursor và cập nhật danh sách tin nhắn
            List<Message> newMessages = readMessagesFromCursor(data);
            messages.clear();
            messages.addAll(newMessages);
            messageAdapter.notifyDataSetChanged();
            updateUI();
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Reset dữ liệu khi CursorLoader được thiết lập lại
        messages.clear();
        messageAdapter.notifyDataSetChanged();
    }

    private List<Message> readMessagesFromCursor(Cursor cursor) {
        // Xử lý cursor và trả về danh sách tin nhắn
        List<Message> messages = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        Intent intent = getIntent();
        int selectedUserId = intent.getIntExtra("_id", 0);
        // Đọc dữ liệu từ cursor và thêm vào danh sách tin nhắn
        if (cursor.moveToFirst()) {
            do {
                int messageId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                int friendshipId = cursor.getInt(cursor.getColumnIndexOrThrow("friendship_id"));
                int senderId = cursor.getInt(cursor.getColumnIndexOrThrow("sender_id"));
                int receiverId = cursor.getInt(cursor.getColumnIndexOrThrow("receiver_id"));
                String messageText = cursor.getString(cursor.getColumnIndexOrThrow("message_text"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                String image = cursor.getString(cursor.getColumnIndexOrThrow("image"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                // Kiểm tra nếu tin nhắn là của người dùng hiện tại
                if ((senderId == currentUserId && receiverId == selectedUserId) || (senderId == selectedUserId && receiverId == currentUserId)) {
                    // Tạo đối tượng Message và thêm vào danh sách
                    Message message = new Message( messageId,friendshipId, senderId, receiverId, messageText, image, status,timestamp);
                    messages.add(message);
//                    messageAdapter.addReceivedImageMessage(message);
                }
//
            } while (cursor.moveToNext());
        }

        // Đóng cursor sau khi hoàn thành
        cursor.close();

        return messages;
    }

    private void updateUI() {
        messageAdapter.notifyDataSetChanged();
        Log.d("ChatActivity", "Scrolling to position: " + (messages.size() - 1)); // Kiểm tra log
        binding.chatRecycleView.scrollToPosition(messages.size() - 1);
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

//            if (message.length() > 0) {
//                ContentValues values = new ContentValues();
//                values.put("sender_id", currentUserId);
//                values.put("receiver_id", receiveId);
//                values.put("friendship_id", friendshipId);
//                values.put("timestamp", formattedTime);
//                values.put("image", image);
//                values.put("status", "status");

//                long insertedId = db.insert("messages", null, values);
//                if (insertedId > -1) {
//                    getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
//                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
//                    textMessage.setText("");
//                    updateUI(); // Thêm dòng này để cập nhật giao diện người dùng
//                } else {
//                    Toast.makeText(this,"Failed", Toast.LENGTH_SHORT).show();
//                }
//            }
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

//    private void saveImageToDatabase(String image, int senderId, int receiverId) {
//       if (!isImageSent  && image != null && !image.isEmpty()) {
//           isImageSent = true;
//           ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(getApplicationContext());
//           SQLiteDatabase db = databaseHelper.getWritableDatabase();
//
//           String query = "SELECT _id FROM friendships " +
//                   "WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
//           String[] selectionArgs = {String.valueOf(receiverId), String.valueOf(senderId), String.valueOf(senderId), String.valueOf(receiverId)};
//
//           // Lấy thời gian hiện tại
//           Date currentTime = new Date();
//           // Định dạng thời gian
//           SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//           String formattedTime = sdf.format(currentTime);
//
//           Cursor cursor = db.rawQuery(query, selectionArgs);
//           if (cursor.moveToFirst()) {
//               int friendshipId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
//               ContentValues values = new ContentValues();
//               values.put("friendship_id", friendshipId);
//               values.put("sender_id", senderId);
//               values.put("receiver_id", receiverId);
////               values.put("message_text", "");
//               values.put("timestamp", formattedTime);
//
//                   values.put("image", image);
//
//               values.put("status", "status");
//
//
//               long insertedId = db.insert("messages", null, values);
//               if (insertedId > -1) {
////                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
//                   Toast.makeText(this, "Image saved to database", Toast.LENGTH_SHORT).show();
//                   if (!imagePath.isEmpty()) {
//                       Message imageMessage = new Message( friendshipId, senderId, receiverId, imagePath, "status", formattedTime, true);
//                       messageAdapter.addImageMessage(imageMessage);
//                   }
//                   updateUI(); // Cập nhật giao diện người dùng
//               }else {
//                   // Lưu thất bại
//                   Toast.makeText(this, "Failed to save image to database", Toast.LENGTH_SHORT).show();
//               }
//
//           }
//           cursor.close();
//           db.close();
//       }
//    }
}

