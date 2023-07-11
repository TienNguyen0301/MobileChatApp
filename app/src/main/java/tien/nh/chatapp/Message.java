package tien.nh.chatapp;

public class Message {
    private int id;

    private int friendship_id;
    private int sender_id;
    private int receiver_id;
    private String message_text;
    private String timestamp;
    private String image;
    private String status;

    private boolean isImageMessage;


    public Message(int friendship_id, int sender_id, int receiver_id, String image, String status, String timestamp, boolean isImageMessage){
        this.friendship_id = friendship_id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.image = image;
        this.status = status;
        this.timestamp = timestamp;
        this.isImageMessage = isImageMessage;
    }
    public Message(int id,int friendship_id, int sender_id, int receiver_id, String message_text, String image, String status, String timestamp) {
        this.id = id;
        this.friendship_id = friendship_id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.message_text = message_text;
        this.timestamp = timestamp;
        this.image = image;
        this.status = status;
    }
    public boolean isImageMessage() {
        return isImageMessage;
    }

    public int getId() {
        return id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public String getMessage_text() {
        return message_text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getImage() {
        return image;
    }

    public String getStatus() {
        return status;
    }

}
