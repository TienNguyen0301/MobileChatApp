package tien.nh.chatapp;

public class Friend {
    private int id;
    private int user1;
    private int user2;
    private String friendship_status;

    private String friendship_created_date;

    public Friend(int id, int user1, int user2, String friendship_status,String friendship_created_date) {
        this.id = id;
        this.user1 = user1;
        this.user2 = user2;
        this.friendship_status = friendship_status;
        this.friendship_created_date = friendship_created_date;
    }

    public int getId() {
        return id;
    }

    public int getUser1() {
        return user1;
    }

    public int getUser2() {
        return user2;
    }
    public String getFriendship_status() {
        return friendship_status;
    }


    public String getFriendship_created_date() {
        return friendship_created_date;
    }
}
