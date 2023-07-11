package tien.nh.chatapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import tien.nh.chatapp.databinding.ItemMessageReceivedBinding;
import tien.nh.chatapp.databinding.ItemMessageReceivedImageBinding;
import tien.nh.chatapp.databinding.ItemMessageSentBinding;
import tien.nh.chatapp.databinding.ItemMessageSentImageBinding;

public  class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messageList;
    private int senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_SENT_IMAGE = 3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    private User user;

    public MessageAdapter(List<Message> messageList, int senderId, User user) {
        this.messageList = messageList;
        this.senderId = senderId;
        this.user = user;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if(viewType == VIEW_TYPE_SENT) {
//            return new SendMessageViewHolder(ItemMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false));
//        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
//            return new SendImageMessageViewHolder(ItemMessageSentImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
//        }else {
//            return new ReceivedMessageViewHolder(ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false));
//        }


        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SENT) {
            ItemMessageSentBinding sentBinding = ItemMessageSentBinding.inflate(inflater, parent, false);
            return new SendMessageViewHolder(sentBinding);
        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            ItemMessageSentImageBinding sentImageBinding = ItemMessageSentImageBinding.inflate(inflater, parent, false);
            return new SendImageMessageViewHolder(sentImageBinding);
        }
//        else if (viewType == VIEW_TYPE_RECEIVED_IMAGE){
//            ItemMessageReceivedImageBinding receivedImageBinding = ItemMessageReceivedImageBinding.inflate(inflater, parent, false);
//            return new ReceivedImageMessageViewHolder(receivedImageBinding);
//        }
        else {
            ItemMessageReceivedBinding receivedBinding = ItemMessageReceivedBinding.inflate(inflater, parent, false);
            return new ReceivedMessageViewHolder(receivedBinding, user);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SendMessageViewHolder) holder).setData(messageList.get(position));
        }else if (getItemViewType(position) == VIEW_TYPE_SENT_IMAGE) {
            ((SendImageMessageViewHolder) holder).setData(messageList.get(position));
        }
//        else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_IMAGE){
//            ((ReceivedImageMessageViewHolder) holder).setData(messageList.get(position));
//        }
        else {
            ((ReceivedMessageViewHolder) holder).setData(messageList.get(position));

        }


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSender_id() == senderId) {
            if (messageList.get(position).getMessage_text() == null) {
                return VIEW_TYPE_SENT_IMAGE;
            } else {
                return VIEW_TYPE_SENT;
            }
        } else {
//            if (messageList.get(position).getImage() == null) {
                return VIEW_TYPE_RECEIVED;
//            } else {
//                return VIEW_TYPE_RECEIVED_IMAGE;
//            }
        }
    }


    static class SendMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemMessageSentBinding binding;
        SendMessageViewHolder(ItemMessageSentBinding itemMessageSentBinding){
            super(itemMessageSentBinding.getRoot());
            binding = itemMessageSentBinding;
        }

        void setData(Message message) {
            binding.textMessage.setText(message.getMessage_text());
//            binding.textDateTime.setText(message.getTimestamp());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemMessageReceivedBinding binding;
        private User user;
        ReceivedMessageViewHolder(ItemMessageReceivedBinding itemMessageReceivedBinding, User user){
            super(itemMessageReceivedBinding.getRoot());
            binding = itemMessageReceivedBinding;
            this.user = user;
        }

        void setData(Message message) {
            binding.textMessage.setText(message.getMessage_text());
//            binding.textDateTime.setText(message.getTimestamp());
//            binding.textDateTime.setText(user.getUsername());
            Glide.with(binding.imgProfile.getContext())
                    .load(user.getAvatar())
                    .error(android.R.drawable.stat_notify_error)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imgProfile);

        }
    }

    static class SendImageMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemMessageSentImageBinding binding;

        SendImageMessageViewHolder(ItemMessageSentImageBinding itemMessageSentImageBinding) {
            super(itemMessageSentImageBinding.getRoot());
            binding = itemMessageSentImageBinding;
        }

        void setData(Message message) {
            // Áp dụng dữ liệu cho ViewHolder của tin nhắn gửi ảnh
            // Ví dụ: hiển thị ảnh trong ImageView
            Glide.with(binding.imageMessage.getContext())
                    .load(message.getImage())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_facebook)) // Placeholder image while loading
                    .into(binding.imageMessage);
//            binding.textDateTime.setText(message.getImage().toString());
        }
    }

    static class ReceivedImageMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemMessageReceivedImageBinding binding;

        ReceivedImageMessageViewHolder(ItemMessageReceivedImageBinding itemMessageReceivedImageBinding) {
            super(itemMessageReceivedImageBinding.getRoot());
            binding = itemMessageReceivedImageBinding;
        }

        void setData(Message message) {
            // Áp dụng dữ liệu cho ViewHolder của tin nhắn nhận ảnh
            // Ví dụ: hiển thị ảnh trong ImageView
            Glide.with(binding.imageMessage.getContext())
                    .load(message.getImage())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_facebook)) // Placeholder image while loading
                    .into(binding.imageMessage);
            binding.textDateTime.setText(message.getImage().toString());
        }
    }

    public void addImageMessage(Message message) {
        if (message.getMessage_text() == null) {
            messageList.add(message);
            notifyItemInserted(messageList.size() - 1);
        }
    }

//    public void addReceivedImageMessage(Message message) {
//        if (message.getImage() != null) {
//            messageList.add(message);
//            notifyItemInserted(messageList.size() - 1);
//        }
//    }

}

//public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{
//    private ArrayList<Message> messageList;
//
//    public void setData(ArrayList<Message> list){
//        this.messageList = list;
//        notifyDataSetChanged();
//    }
//
//    public void addMessage(Message message) {
//        messageList.add(message);
//        notifyItemInserted(messageList.size() - 1);
//    }
//
//    public MessageAdapter(ArrayList<Message> messageList) {
//        this.messageList = messageList;
//    }
//    @NonNull
//    @Override
//    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view;
//
//        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
//
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
//        Message message = messageList.get(position);
//        if (message == null){
//            return;
//        }
//        holder.textMessageContent.setText(message.getMessage_text());
//    }
//
//    @Override
//    public int getItemCount() {
//        if (messageList != null){
//            return messageList.size();
//        }
//        return 0;
//    }
//
//
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        TextView textMessageContent;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            textMessageContent = itemView.findViewById(R.id.textMessageContent);
//        }
//    }
//}
