package tien.nh.chatapp;

public class TempStorage {
    private static TempStorage instance;
    private int receiverId;

    private TempStorage() {
    }

    public static TempStorage getInstance() {
        if (instance == null) {
            instance = new TempStorage();
        }
        return instance;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }
}


