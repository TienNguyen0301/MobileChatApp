package tien.nh.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class AdminListFriendUserActivity extends AppCompatActivity {
    private User selectedUser;
    private ListView friendListView;
    private AdminRelationshipAdapter adminRelationshipAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list_friend_user);

        friendListView = (ListView) findViewById(R.id.listFriend_Admin);

        // Nhận dữ liệu người dùng được chọn từ Intent
        selectedUser = (User) getIntent().getSerializableExtra("selectedUser");

        ArrayList<User> getFriendList = getFriendList(selectedUser.getId());

        AdminRelationshipAdapter friendList = new AdminRelationshipAdapter(this, getFriendList);
        friendListView.setAdapter(friendList);
    }

    public ArrayList<User> getFriendList(int currentUserId) {
        ArrayList<User> getFriendList = new ArrayList<>();

        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String query = "SELECT users.* FROM users " +
                "INNER JOIN friendships ON (users._id = friendships.user1 OR users._id = friendships.user2) " +
                "WHERE (friendships.user1 = ? OR friendships.user2 = ?) AND friendships.friendship_status = 'accepted' " +
                "AND users._id != ?";
        String[] selectionArgs = {String.valueOf(currentUserId), String.valueOf(currentUserId), String.valueOf(currentUserId)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        while (cursor.moveToNext()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            int role = cursor.getInt(cursor.getColumnIndexOrThrow("role"));

            User user = new User(userId, username, phone, email, avatar, role);
            getFriendList.add(user);
        }

        cursor.close();
        db.close();

        return getFriendList;
    }
}