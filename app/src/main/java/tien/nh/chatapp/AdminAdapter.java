package tien.nh.chatapp;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class AdminAdapter extends GenericAdapter<User, AdminAdapter.ViewHolder>{
    private Context context;
    private ArrayList<User> userList;
    private OnUserDeleteListener deleteListener;



    public AdminAdapter(Context context, ArrayList<User> dataList) {
        super(context, dataList);
        this.context = context;
        this.userList = new ArrayList<>();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.item_user_adminpage;
    }

    @Override
    protected ViewHolder createViewHolder(View convertView) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.avatarUserAdminPage = convertView.findViewById(R.id.avatarTextView);
        viewHolder.emailUserAdminPage = convertView.findViewById(R.id.emailTextView);
        viewHolder.btnDelete = convertView.findViewById(R.id.btnDeleteUser);
        viewHolder.btnUpdate = convertView.findViewById(R.id.btnUpdateUser);

        return viewHolder;
    }

    @Override
    protected void bindData(ViewHolder viewHolder, User data) {
        viewHolder.emailUserAdminPage.setText(data.getEmail());
        ImageView avatarUserAdminPage = viewHolder.avatarUserAdminPage;
        String avatarPath = data.getAvatar();
        Glide.with(context)
                .load(avatarPath)
                .error(android.R.drawable.stat_notify_error) // Ảnh hiển thị khi xảy ra lỗi trong quá trình tải hình ảnh
                .apply(RequestOptions.circleCropTransform())
                .into(avatarUserAdminPage);
        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteListener != null) {
                    deleteListener.onDeleteUser(data); // Triggers the delete operation
                }
            }
        });

        viewHolder.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển sang Activity khác
                Intent intent = new Intent(context, UpdateUserActivity.class);

                // Truyền dữ liệu cần thiết từ User vào Intent (nếu có)
                intent.putExtra("userId", data.getId());
                intent.putExtra("userName", data.getUsername());
                intent.putExtra("userEmail", data.getEmail());
                intent.putExtra("avatar", data.getAvatar());
                intent.putExtra("phone", data.getPhone());
                intent.putExtra("role", data.getRole());

                // Khởi chạy Activity mới
                context.startActivity(intent);
            }
        });
    }


    protected static class ViewHolder extends GenericAdapter.ViewHolder {
        TextView emailUserAdminPage;
        ImageView avatarUserAdminPage;
        ImageButton btnDelete, btnUpdate;
    }

    public interface OnUserDeleteListener {
        void onDeleteUser(User user);
    }

    public void setOnUserDeleteListener(OnUserDeleteListener listener) {
        deleteListener = listener;
    }

    public void setData(ArrayList<User> data) {
        userList.clear();
        userList.addAll(data);
        notifyDataSetChanged();
    }


}
