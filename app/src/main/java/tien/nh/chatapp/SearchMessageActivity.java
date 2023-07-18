package tien.nh.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchMessageActivity extends AppCompatActivity {

    EditText searchText;

    SearchMessageAdapter searchMessageAdapter;
    List<Message> messagesList;

    Button buttonSearch;

    TextView textNoResults;

    private ListView chatListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_message);

        searchText = (EditText) findViewById(R.id.editMessageSearch);
        chatListView = (ListView) findViewById(R.id.listMessages);
        buttonSearch = (Button) findViewById(R.id.btnSearchMessage);
        textNoResults = (TextView) findViewById(R.id.textNoResults);


        searchMessageAdapter = new SearchMessageAdapter(SearchMessageActivity.this, R.layout.item_message_search, new ArrayList<>());
        chatListView.setAdapter(searchMessageAdapter);
        messagesList = new ArrayList<>();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        int receiverId = TempStorage.getInstance().getReceiverId();


        retrieveMessagesAndSetAdapter(currentUserId, receiverId);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = searchText.getText().toString();
                searchMessages(search);
            }
        });
    }

    private void searchMessages(String keyword) {
        ArrayList<Message> searchResults = new ArrayList<>();
        for (Message message : messagesList) {
            if (message.getMessage_text().toLowerCase().contains(keyword.toLowerCase())) {
                searchResults.add(message);
            }
        }
        // Tạo adapter mới với danh sách tin nhắn tìm kiếm được
        SearchMessageAdapter adapter = new SearchMessageAdapter(SearchMessageActivity.this, R.layout.item_message_search, searchResults);
        chatListView.setAdapter(adapter);

        // Kiểm tra danh sách tin nhắn tìm thấy
        if (searchResults.isEmpty()) {
            // Không tìm thấy tin nhắn, hiển thị thông báo
            chatListView.setVisibility(View.GONE);
            textNoResults.setVisibility(View.VISIBLE);
        } else {
            // Hiển thị tin nhắn tìm thấy, ẩn thông báo
            chatListView.setVisibility(View.VISIBLE);
            textNoResults.setVisibility(View.GONE);
        }
    }


    private void retrieveMessagesAndSetAdapter(int currentId, int receiverId) {
        // Retrieve messages from Firestore and set adapter for chatListView
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference messagesRef = db.collection(ChatDatabaseHelper.TABLE_MESSAGES);
        messagesRef.whereEqualTo(ChatDatabaseHelper.COLUMN_SENDER_ID, currentId)
                .whereEqualTo(ChatDatabaseHelper.COLUMN_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Message message = document.toObject(Message.class);
                            messagesList.add(message);
                        }
                        // Lấy tin nhắn ngược lại (người gửi là receiverId và người nhận là currentId)
                        messagesRef.whereEqualTo(ChatDatabaseHelper.COLUMN_SENDER_ID, receiverId)
                                .whereEqualTo(ChatDatabaseHelper.COLUMN_RECEIVER_ID, currentId)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task1.getResult()) {
                                            Message message = document.toObject(Message.class);
                                            messagesList.add(message);
                                        }
                                        // Sắp xếp danh sách tin nhắn theo thời gian
                                        Collections.sort(messagesList, new Comparator<Message>() {
                                            @Override
                                            public int compare(Message m1, Message m2) {
                                                return m1.getTimestamp().compareTo(m2.getTimestamp());
                                            }
                                        });
//                                        ArrayList<Message> arrayListMessageList = new ArrayList<>(messagesList);
//                                        SearchMessageAdapter adapter = new SearchMessageAdapter(SearchMessageActivity.this, R.layout.item_message_search, arrayListMessageList);
//                                        chatListView.setAdapter(adapter);
                                    } else {

                                    }
                                });
                    } else {

                    }
                });
    }



}