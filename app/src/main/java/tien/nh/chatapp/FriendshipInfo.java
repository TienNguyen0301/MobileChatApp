package tien.nh.chatapp;

public class FriendshipInfo {
    private int user1Id;
    private int user2Id;

    public FriendshipInfo(int user1Id, int user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }
    public int getUser1Id() {
        return user1Id;
    }

    public int getUser2Id() {
        return user2Id;
    }
}
