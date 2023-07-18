package tien.nh.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageHistoryActivity extends AppCompatActivity {
    List<Message> messagesList;
    ListView messageHistory;

    SearchMessageAdapter searchMessageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_history);

        messageHistory = (ListView) findViewById(R.id.messageHistory);

        searchMessageAdapter = new SearchMessageAdapter(MessageHistoryActivity.this, R.layout.item_message_search, new ArrayList<>());
        messageHistory.setAdapter(searchMessageAdapter);
        messagesList = new ArrayList<>();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        int receiverId = TempStorage.getInstance().getReceiverId();


        retrieveMessagesAndSetAdapter(currentUserId, receiverId);


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
                                        ArrayList<Message> arrayListMessageList = new ArrayList<>(messagesList);
                                        SearchMessageAdapter adapter = new SearchMessageAdapter(MessageHistoryActivity.this, R.layout.item_message_search, arrayListMessageList);
                                        messageHistory.setAdapter(adapter);
                                    } else {

                                    }
                                });
                    } else {

                    }
                });
    }
}