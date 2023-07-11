package tien.nh.chatapp;

import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


public class UserAdapter extends GenericAdapter<User, UserAdapter.ViewHolder> {
    private Context context;
    private ArrayList<User> userList;
    private OnAddFriendClickListener onAddFriendClickListener;


    public UserAdapter(Context context, ArrayList<User> dataList) {
        super(context, new ArrayList<>(dataList));
        this.context = context;

    }


    @Override
    protected int getLayoutRes() {
        return R.layout.item_user;
    }

    @Override
    protected ViewHolder createViewHolder(View convertView) {
        ViewHolder viewHolder = new ViewHolder();
//        viewHolder.usernameTextView = convertView.findViewById(R.id.usernameTextView);
        viewHolder.emailTextView = convertView.findViewById(R.id.emailTextView);
        viewHolder.avatarTextView = convertView.findViewById(R.id.avatarTextView);
        viewHolder.btnAddFriend = convertView.findViewById(R.id.btn_addFriend);
        // Ánh xạ các view khác nếu cần

        return viewHolder;
    }

    @Override
    protected void bindData(ViewHolder viewHolder, User data) {
        viewHolder.emailTextView.setText(data.getEmail());
        ImageView avatarImageView = viewHolder.avatarTextView;
        String avatarPath = data.getAvatar();
        Glide.with(context)
                .load(avatarPath)
                .error(android.R.drawable.stat_notify_error) // Ảnh hiển thị khi xảy ra lỗi trong quá trình tải hình ảnh
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImageView);

        viewHolder.btnAddFriend.setTag(data.getId()); // Set the user ID as the tag

        viewHolder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int userId = (int) v.getId();
                int id = (int) data.getId();
                if (onAddFriendClickListener != null) {
                    onAddFriendClickListener.onAddFriendClick(id);
                }
            }

        });
        // Bind các dữ liệu khác nếu cần
    }

    protected static class ViewHolder extends GenericAdapter.ViewHolder {

        TextView emailTextView;
        ImageView avatarTextView;
        Button btnAddFriend;


        // Các view khác trong item layout
    }

    public interface OnAddFriendClickListener {
        void onAddFriendClick(int userId);

    }

    public void setOnAddFriendClickListener(OnAddFriendClickListener listener) {
        this.onAddFriendClickListener = listener;
    }

}