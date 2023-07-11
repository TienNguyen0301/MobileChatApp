package tien.nh.chatapp;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;




public class FriendAdapter extends GenericAdapter<User, FriendAdapter.ViewHolder>{
    private Context context;
    private ArrayList<User> friendList;

    private OnAcceptFriendClickListener onAcceptFriendClickListener;


    public FriendAdapter(Context context, ArrayList<User> dataList) {
        super(context, new ArrayList<>(dataList));
        this.context = context;
    }

    private Fragment getActiveFragment() {
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null && fragments.size() > 0) {
            return fragments.get(fragments.size() - 1);
        }
        return null;
    }


    @Override
    protected int getLayoutRes() {
        Fragment currentFragment = getActiveFragment();
        if (currentFragment instanceof DefaultFragment) {
            return R.layout.friend;
        } else {
            return R.layout.item_friend;
        }
    }

    @Override
    protected ViewHolder createViewHolder(View convertView) {

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.userFriend = convertView.findViewById(R.id.userFriend);
        viewHolder.dateAt = convertView.findViewById(R.id.dateAt);
        viewHolder.avatarTextView = convertView.findViewById(R.id.avatarTextView);
        viewHolder.btnAccept = convertView.findViewById(R.id.btn_acceptFriend);

        return viewHolder;
    }

    @Override
    protected void bindData(ViewHolder viewHolder, User data) {
        viewHolder.userFriend.setText(data.getEmail());
        viewHolder.dateAt.setText(data.getPhone());

        String avatarPath = data.getAvatar();
        ImageView avatarImageView = viewHolder.avatarTextView;
        Glide.with(context)
                .load(avatarPath)
                .error(android.R.drawable.stat_notify_error) // Ảnh hiển thị khi xảy ra lỗi trong quá trình tải hình ảnh
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImageView);

        if(getLayoutRes() == R.layout.item_friend){
            viewHolder.btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) data.getId();
                    if (onAcceptFriendClickListener != null) {
                        onAcceptFriendClickListener.onAcceptFriendClick(id);
                    }
                }
            });
        }
    }


    protected static class ViewHolder extends GenericAdapter.ViewHolder {
        TextView userFriend;
        TextView dateAt;

        ImageView avatarTextView;

        Button btnAccept;



        // Các view khác trong item layout
    }

    public interface OnAcceptFriendClickListener {
        void onAcceptFriendClick(int userId);
    }

    public void setOnAcceptFriendClickListener(OnAcceptFriendClickListener listener) {
        this.onAcceptFriendClickListener = listener;
    }


}
