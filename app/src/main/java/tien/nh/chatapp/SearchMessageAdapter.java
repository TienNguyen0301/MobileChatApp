package tien.nh.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.List;
public class SearchMessageAdapter extends ArrayAdapter<Message> {
    private Context context;
    private int resourceId;
    public SearchMessageAdapter(Context context, int resourceId, List<Message> messages) {
        super(context, resourceId, messages);
        this.context = context;
        this.resourceId = resourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Lấy thông tin tin nhắn hiện tại
        Message message = getItem(position);

        // Kiểm tra convertView để sử dụng lại hoặc inflate một layout mới
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message_search, parent, false);
        }

        TextView contentTextView = convertView.findViewById(R.id.searchText);
        ImageView avatar = convertView.findViewById(R.id.avatarTextView);

        contentTextView.setText(message.getMessage_text());

        // Lấy thông tin người gửi từ Firestore và hiển thị avatar
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference senderRef = db.collection(ChatDatabaseHelper.TABLE_USERS).document(String.valueOf(message.getSender_id()));
        senderRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String senderAvatarUrl = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_AVATAR);
                    Glide.with(context)
                            .load(senderAvatarUrl)
                            .error(android.R.drawable.stat_notify_error)
                            .apply(RequestOptions.circleCropTransform())
                            .into(avatar);
                }
            }
        });

        // Lấy thông tin người nhận từ Firestore và hiển thị avatar
        DocumentReference receiverRef = db.collection(ChatDatabaseHelper.TABLE_USERS).document(String.valueOf(message.getReceiver_id()));
        receiverRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String receiverAvatarUrl = documentSnapshot.getString(ChatDatabaseHelper.COLUMN_USER_AVATAR);
                    Glide.with(context)
                            .load(receiverAvatarUrl)
                            .error(android.R.drawable.stat_notify_error)
                            .apply(RequestOptions.circleCropTransform())
                            .into(avatar);
                }
            }
        });



        return convertView;
    }
}

