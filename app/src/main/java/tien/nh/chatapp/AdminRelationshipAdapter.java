package tien.nh.chatapp;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;




public class AdminRelationshipAdapter extends GenericAdapter<User, AdminRelationshipAdapter.ViewHolder>{
    private Context context;
    private ArrayList<User> friendList;




    public AdminRelationshipAdapter(Context context, ArrayList<User> dataList) {
        super(context, new ArrayList<>(dataList));
        this.context = context;
    }


    @Override
    protected int getLayoutRes() {
       return R.layout.friend;

    }
    @Override
    protected ViewHolder createViewHolder(View convertView) {

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.userFriend = convertView.findViewById(R.id.userFriend);
        viewHolder.avatarTextView = convertView.findViewById(R.id.avatarTextView);
        viewHolder.status_User = convertView.findViewById(R.id.statusUser);

        return viewHolder;
    }

    @Override
    protected void bindData(ViewHolder viewHolder, User data) {
        viewHolder.userFriend.setText(data.getEmail());
        viewHolder.status_User.setVisibility(View.GONE);
        String avatarPath = data.getAvatar();
        ImageView avatarImageView = viewHolder.avatarTextView;
        Glide.with(context)
                .load(avatarPath)
                .error(android.R.drawable.stat_notify_error) // Ảnh hiển thị khi xảy ra lỗi trong quá trình tải hình ảnh
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImageView);
    }


    protected static class ViewHolder extends GenericAdapter.ViewHolder {
        TextView userFriend;
        TextView status_User;
        ImageView avatarTextView;


    }

}
