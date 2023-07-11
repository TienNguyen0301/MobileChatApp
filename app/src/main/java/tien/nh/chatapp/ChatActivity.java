package tien.nh.chatapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.List;

import tien.nh.chatapp.databinding.ActivityChatBinding;


public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    TextView txtUsername, receiverText, senderText;
    ImageView imgUser, btn_back;

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


//        init();

        // Nhận thông tin user từ Intent
        Intent intent = getIntent();
        int id = intent.getIntExtra("_id", 0);
        String avatarPath = intent.getStringExtra("avatar");
        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");
        int role = intent.getIntExtra("role", 0);

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
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
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
            String image = ""; // Khởi tạo đường dẫn ảnh là rỗng

            if (message.length() > 0) {
                ContentValues values = new ContentValues();
                values.put("sender_id", currentUserId);
                values.put("receiver_id", receiveId);
                values.put("friendship_id", friendshipId);
                values.put("message_text", message);
                values.put("timestamp", formattedTime);
                values.put("status", "status");
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
                Uri imageUri = data.getData();

                // Lưu trữ đường dẫn hình ảnh mới vào biến newAvatarPath
                imagePath = imageUri.toString();
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                int currentUserId = sharedPreferences.getInt("currentUserId", 0);
                Intent intent = getIntent();
                int id = intent.getIntExtra("_id", 0);
                // Lưu ảnh vào cơ sở dữ liệu
                saveImageToDatabase(imagePath, currentUserId, id);

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

    private void saveImageToDatabase(String imagePath, int senderId, int receiverId) {
       if (!isImageSent) {
           isImageSent = true;
           ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(getApplicationContext());
           SQLiteDatabase db = databaseHelper.getWritableDatabase();

           String query = "SELECT _id FROM friendships " +
                   "WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
           String[] selectionArgs = {String.valueOf(receiverId), String.valueOf(senderId), String.valueOf(senderId), String.valueOf(receiverId)};

           // Lấy thời gian hiện tại
           Date currentTime = new Date();
           // Định dạng thời gian
           SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           String formattedTime = sdf.format(currentTime);

           Cursor cursor = db.rawQuery(query, selectionArgs);
           if (cursor.moveToFirst()) {
               int friendshipId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
               ContentValues values = new ContentValues();
               values.put("friendship_id", friendshipId);
               values.put("sender_id", senderId);
               values.put("receiver_id", receiverId);
//               values.put("message_text", "");
               values.put("timestamp", formattedTime);
               if (imagePath != null) {
                   // Chỉ lưu đường dẫn hình ảnh nếu imagePath khác null
                   values.put("image", imagePath);
               }
               values.put("status", "status");
               long insertedId = db.insert("messages", null, values);
               if (insertedId > -1) {
//                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                   Toast.makeText(this, "Image saved to database", Toast.LENGTH_SHORT).show();
                   if (!imagePath.isEmpty()) {
                       Message imageMessage = new Message( friendshipId, senderId, receiverId, imagePath, "status", formattedTime, true);
                       messageAdapter.addImageMessage(imageMessage);
                   }
                   updateUI(); // Cập nhật giao diện người dùng
               }else {
                   // Lưu thất bại
                   Toast.makeText(this, "Failed to save image to database", Toast.LENGTH_SHORT).show();
               }

           }
           cursor.close();
           db.close();
       }
    }
}

//    private void listenMessages() {
//        database.collection(Constants.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Constants.KEY_SENDER_ID, prferenceManager.getString(Constants.KEY_USER.ID))
//                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
//                .addSnapshowListener(eventListener);
//        database.collection(Constants.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
//                .whereEqualTo(Constants.KEY_RECEIVER_ID, prferenceManager.getString(Constants.KEY_USER.ID))
//                .addSnapshowListener(eventListener);
//    }
//
//    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
//        if (error != null){
//            return;
//        }
//        if (value != null) {
//            int count = messages.size();
//            for (DocumentChange documentChange : value.getDocumentChanges()){
//                if(documentChange.getType() == DocumentChange.Type.ADDED){
//                    Message message = new Message();
//                    message.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
//                    message.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
//                    message.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
//                    message.dateTime = documentChange.getDocument().getString(Constants.KEY_TIMESTAMP);
//                    message.add(message)
//                }
//            }
//            Collections.sort(messages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
//            if(count == 0){
//                messageAdapter.notifyDataSetChanged();
//            }else {
//                messageAdapter.notifyItemRangeInserted(messages.size(), messages.size());
//                binding.chatRecycleView.smoothScrollToPosition(messages.size() -1);
//            }
//            binding.chatRecycleView.setVisibility(View.VISIBLE);
//        }
//    }





//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
//
//        txtUsername = (TextView) findViewById(R.id.chat_name);
//        imgUser = (ImageView) findViewById(R.id.avatar_user);
//        textMessage = (EditText) findViewById(R.id.textMessage);
//        btnSend = (ImageButton) findViewById(R.id.imgSend);
//        receiverText =(TextView) findViewById(R.id.chat);
//        senderText =(TextView) findViewById(R.id.chat_text);
//        btnSendImage = (ImageButton) findViewById(R.id.send_img);
//        sender_img = (ImageView) findViewById(R.id.sender_img);
//
//
//        // Nhận thông tin user từ Intent
//        Intent intent = getIntent();
//        int id = intent.getIntExtra("_id", 0);
//        String name = intent.getStringExtra("name");
//        String email = intent.getStringExtra("email");
//        String avatarPath = intent.getStringExtra("avatar");
//        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//        int currentUserId = sharedPreferences.getInt("currentUserId", 0);
//
//        // ... và các thông tin khác
//        txtUsername.setText(name);
//        ArrayList<Message> messageList = getSendMessages(currentUserId, id);
//        ArrayList<Message> messageListReceiver = getReceiveMessages(currentUserId, id);
//
//        for (Message ms : messageList) {
//            // Lấy thông tin tin nhắn
//            int messageId = ms.getId();
//            String messagesText = ms.getMessage_text();
//            String imagePath = ms.getImage();
//            if (messagesText.equals("")){
//                senderText.setVisibility(View.GONE);
//                sender_img.setVisibility(View.VISIBLE);
////                Glide.with(this)
////                        .load(imagePath)
////                        .error(android.R.drawable.ic_lock_lock) // Ảnh hiển thị khi xảy ra lỗi trong quá trình tải hình ảnh
////                        .into(sender_img);
//
//            } else {
//                senderText.setText(messagesText);
//                senderText.setVisibility(View.VISIBLE);
//                sender_img.setVisibility(View.GONE);
//            }
//        }
//        for (Message ms : messageListReceiver) {
//
//            String messagesText = ms.getMessage_text();
//            receiverText.setText(messagesText);
//        }
//        // Hiển thị hình ảnh avatar
//        Glide.with(this)
//                .load(avatarPath)
//                .error(android.R.drawable.stat_notify_error) // Ảnh hiển thị khi xảy ra lỗi trong quá trình tải hình ảnh
//                .apply(RequestOptions.circleCropTransform())
//                .into(imgUser);
//
//
//
//        // Trong phương thức onCreate()
//        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//                new ActivityResultCallback<ActivityResult>() {
//                    @Override
//                    public void onActivityResult(ActivityResult result) {
//                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                            // Xử lý hình ảnh đã chọn
//                            Uri selectedImageUri = result.getData().getData();
//                            handleSelectedImage(selectedImageUri);
//                            String imagePath = selectedImageUri.toString();
//                            Intent intent = getIntent();
//                            int id = intent.getIntExtra("_id", 0);
//
//
//                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//                            int currentUserId = sharedPreferences.getInt("currentUserId", 0);
//
//                            int receiverId = id; // Lấy id người nhận
//                            int senderId = currentUserId; // Lấy id người gửi
//
//                            saveImageToDatabase(imagePath, senderId, receiverId);
//
//                        }
//                    }
//                });
//
//
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendMessage(id);
//                textMessage.setText("");
//            }
//        });
//
//        btnSendImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Open file chooser
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                imagePickerLauncher.launch(intent);
//
//            }
//        });
//    }
//
//    private void handleSelectedImage(Uri imageUri) {
//        selectedImageUri = imageUri;
//        // Load and display the image using Glide library
////        ImageView imageView = findViewById(R.id.imageView);
//        Glide.with(this).load(selectedImageUri).override(150, 150).into(sender_img);
//
//    }
//
//
//    private void saveImageToDatabase(String imagePath, int senderId, int receiverId) {
//        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(getApplicationContext());
//        SQLiteDatabase db = databaseHelper.getWritableDatabase();
//
//        String query = "SELECT _id FROM friendships " +
//                "WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
//        String[] selectionArgs = {String.valueOf(receiverId), String.valueOf(senderId), String.valueOf(senderId), String.valueOf(receiverId)};
//
//        // Lấy thời gian hiện tại
//        Date currentTime = new Date();
//        // Định dạng thời gian
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String formattedTime = sdf.format(currentTime);
//
//        Cursor cursor = db.rawQuery(query, selectionArgs);
//        if (cursor.moveToFirst()) {
//            int friendshipId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
//            ContentValues values = new ContentValues();
//            values.put("friendship_id", friendshipId);
//            values.put("sender_id", senderId);
//            values.put("receiver_id", receiverId);
//            values.put("message_text", "");
//            values.put("timestamp", formattedTime);
//            if (imagePath != null) {
//                // Chỉ lưu đường dẫn hình ảnh nếu imagePath khác null
//                values.put("image", imagePath);
//            }
//            values.put("status", "status");
//            long insertedId = db.insert("messages", null, values);
//            if (insertedId > -1) {
//                ArrayList<Message> messageList = getSendMessages(senderId, receiverId);
//                for (Message ms : messageList) {
//                    // Lấy thông tin tin nhắn
//                    int messageId = ms.getId();
//                    String image = ms.getImage();
//                    senderText.setVisibility(View.GONE);
//
//                    // Thực hiện các xử lý khác với thông tin tin nhắn
//                    // Ví dụ: hiển thị thông tin tin nhắn lên giao diện người dùng
//                }
//                // Lưu thành công
//                Toast.makeText(this, "Image saved to database", Toast.LENGTH_SHORT).show();
//            } else {
//                // Lưu thất bại
//                Toast.makeText(this, "Failed to save image to database", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        cursor.close();
//        db.close();
//    }
//
//
//
//    public void sendMessage(int userId) {
//        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(getApplicationContext());
//        SQLiteDatabase db = databaseHelper.getWritableDatabase();
//
//        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//        int currentUserId = sharedPreferences.getInt("currentUserId", 0);
//
//        String query = "SELECT _id FROM friendships " +
//                "WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
//        String[] selectionArgs = {String.valueOf(userId), String.valueOf(currentUserId), String.valueOf(currentUserId), String.valueOf(userId)};
//
//        Cursor cursor = db.rawQuery(query, selectionArgs);
//        // Lấy thời gian hiện tại
//        Date currentTime = new Date();
//
//        // Định dạng thời gian
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String formattedTime = sdf.format(currentTime);
//
//        if (cursor.moveToFirst()) {
//            int friendshipId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
//            // Sử dụng friendshipId cho các xử lý tiếp theo
//            String message = textMessage.getText().toString();
//            if (message.length()>0) {
//                ContentValues values = new ContentValues();
//                values.put("sender_id", currentUserId);
//                values.put("receiver_id", userId);
//                values.put("friendship_id", friendshipId);
//                values.put("message_text", message);
//                values.put("timestamp", formattedTime);
//                values.put("image", "");
//                values.put("status", "status");
//                long insertedId = db.insert("messages", null, values);
//                if (insertedId > -1){
//                    TextView newMessageTextView = new TextView(this);
//                    newMessageTextView.setText(message);
//
//                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.WRAP_CONTENT,
//                            LinearLayout.LayoutParams.WRAP_CONTENT
//                    );
//                    layoutParams.setMargins(0, 10, 0, 0); // Cài đặt khoảng cách giữa các tin nhắn
//
//                    layoutParams.gravity = Gravity.END;
//
//                    newMessageTextView.setLayoutParams(layoutParams);
//                    newMessageTextView.setBackgroundResource(R.drawable.bg_corner_16); // Tùy chỉnh background cho tin nhắn
//                    newMessageTextView.setTextSize(18);
//                    newMessageTextView.setPadding(10,10,10,10);
//
//                    LinearLayout chatContainer = findViewById(R.id.chatContainer);
//                    chatContainer.addView(newMessageTextView);
//
//                    ArrayList<Message> messageList = getSendMessages(currentUserId, userId);
//                    for (Message ms : messageList) {
//                        // Lấy thông tin tin nhắn
//                        int messageId = ms.getId();
//                        String messagesText = ms.getMessage_text();
//
//                        newMessageTextView.setText(messagesText);
//                        // Thực hiện các xử lý khác với thông tin tin nhắn
//                        // Ví dụ: hiển thị thông tin tin nhắn lên giao diện người dùng
//                    }
//                    Toast.makeText(this, "Send messages successed", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "Send messages failed", Toast.LENGTH_SHORT).show();
//                }
//            }
//        } else {
//            Toast.makeText(this, "Lỗi gửi tin nhắn", Toast.LENGTH_SHORT).show();
//        }
//        cursor.close();
//        db.close();
//
//        }
//
//    public ArrayList<Message> getSendMessages(int currentUserId, int userId) {
//        ArrayList<Message> messageList = new ArrayList<>();
//
//        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(getApplicationContext());
//        SQLiteDatabase db = databaseHelper.getReadableDatabase();
//
//        String query = "SELECT * FROM messages " +
//                "WHERE (sender_id = ? AND receiver_id = ?) " +
//                "ORDER BY timestamp ASC";
//
//        String[] selectionArgs = {String.valueOf(currentUserId), String.valueOf(userId)};
//
//        Cursor cursor = db.rawQuery(query, selectionArgs);
//
//        if (cursor.moveToFirst()) {
//            do {
//                int messageId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
//                int friendshipId = cursor.getInt(cursor.getColumnIndexOrThrow("friendship_id"));
//                int senderId = cursor.getInt(cursor.getColumnIndexOrThrow("sender_id"));
//                int receiverId = cursor.getInt(cursor.getColumnIndexOrThrow("receiver_id"));
//                String messageText = cursor.getString(cursor.getColumnIndexOrThrow("message_text"));
//                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
//                String image = cursor.getString(cursor.getColumnIndexOrThrow("image"));
//                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
//
//                // Tạo đối tượng Message và thêm vào danh sách
//                Message message = new Message(messageId, friendshipId, senderId, receiverId, messageText, image, status,timestamp);
//                messageList.add(message);
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        db.close();
//
//        return messageList;
//    }
//
//    public ArrayList<Message> getReceiveMessages(int currentUserId, int userId) {
//        ArrayList<Message> messageList = new ArrayList<>();
//
//        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(getApplicationContext());
//        SQLiteDatabase db = databaseHelper.getReadableDatabase();
//
//
//        String query = "SELECT * FROM messages " +
//                "WHERE (receiver_id = ? AND sender_id = ?)" +
//                "ORDER BY timestamp ASC";
//
//        String[] selectionArgs = {String.valueOf(currentUserId), String.valueOf(userId)};
//
//        Cursor cursor = db.rawQuery(query, selectionArgs);
//
//        if (cursor.moveToFirst()) {
//            do {
//                int messageId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
//                int friendshipId = cursor.getInt(cursor.getColumnIndexOrThrow("friendship_id"));
//                int senderId = cursor.getInt(cursor.getColumnIndexOrThrow("sender_id"));
//                int receiverId = cursor.getInt(cursor.getColumnIndexOrThrow("receiver_id"));
//                String messageText = cursor.getString(cursor.getColumnIndexOrThrow("message_text"));
//                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
//                String image = cursor.getString(cursor.getColumnIndexOrThrow("image"));
//                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
//
//                // Tạo đối tượng Message và thêm vào danh sách
//                Message message = new Message(messageId, friendshipId, senderId, receiverId, messageText, image, status,timestamp);
//                messageList.add(message);
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        db.close();
//
//        return messageList;
//    }
//
//
//}
//
//
//
