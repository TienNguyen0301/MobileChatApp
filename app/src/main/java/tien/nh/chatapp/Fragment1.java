package tien.nh.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import java.util.ArrayList;
import android.content.ContentValues;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment1 extends Fragment implements FriendAdapter.OnAcceptFriendClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Fragment1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment1.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment1 newInstance(String param1, String param2) {
        Fragment1 fragment = new Fragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_1, container, false);

        // Ánh xạ ListView từ layout
        ListView listViewUsers = rootView.findViewById(R.id.listFriendShip);

        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        ArrayList<User> usersByFriendship = getFriendshipRequests(currentUserId);

        FriendAdapter fr = new FriendAdapter(getActivity(), usersByFriendship);
        listViewUsers.setAdapter(fr);
        fr.setOnAcceptFriendClickListener(this::onAcceptFriendClick);


        return rootView;
    }

    public void acceptFriendshipRequest(int userId) {
        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        String query = "SELECT * FROM friendships WHERE (user1 = '" + currentUserId + "' AND user2 = '" + userId + "') OR (user1 = '" + userId + "' AND user2 = '" + currentUserId + "')";
        Cursor cursor = db.rawQuery(query, null);

        // Kiểm tra và gộp dữ liệu nếu cần thiết
        if (cursor.moveToFirst()) {
            do {
                // Lấy thông tin từ dòng dữ liệu hiện tại
                int rowId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("friendship_status"));

                // Kiểm tra trạng thái lời mời kết bạn
                if (status.equals("waiting")) {
                    // Gộp dòng dữ liệu vào mối quan hệ bạn bè
                    // Thực hiện các thao tác cần thiết, ví dụ:
                    // - Cập nhật trạng thái thành mối quan hệ bạn bè
                    // - Xóa dòng dữ liệu thứ hai nếu cần thiết

                    // Cập nhật trạng thái thành mối quan hệ bạn bè
                    ContentValues values = new ContentValues();
                    values.put("friendship_status", "accept");
                    int updatedRows = db.update("friendships", values, "_id = ?", new String[]{String.valueOf(rowId)});

                    if (updatedRows > 0) {
                        // Cập nhật thành công, gọi phương thức loadFriendList() để cập nhật danh sách bạn bè trên giao diện
                        loadFriendList();
                        // Cập nhật thành công
                        Toast.makeText(requireContext(), "Friendship request accepted", Toast.LENGTH_SHORT).show();
                    } else {
                        // Cập nhật thất bại
                        Toast.makeText(requireContext(), "Failed to accept friendship request", Toast.LENGTH_SHORT).show();
                    }

                    // Hiển thị thông báo hoặc thông tin thành công
                    Toast.makeText(getContext(), "Đã trở thành bạn bè", Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
//         Xóa dòng dữ liệu thứ hai (nếu có)
        // Cần chỉnh sửa query và thực hiện truy vấn DELETE phù hợp
        String queryDelete = "SELECT * FROM friendships WHERE (user1 = '" + currentUserId + "' AND user2 = '" + userId + "') OR (user1 = '" + userId + "' AND user2 = '" + currentUserId + "') AND (friendship_status = 'accept')";
        Cursor cursorDelete = db.rawQuery(queryDelete, null);
        if (cursorDelete != null && cursorDelete.getCount() == 2) {
            // Tiến hành xóa dòng dữ liệu thứ hai
            cursorDelete.moveToNext(); // Di chuyển con trỏ đến dòng dữ liệu thứ hai
            int rowFriendId = cursorDelete.getInt(cursor.getColumnIndexOrThrow("_id")); // Lấy giá trị của cột "id"

            // Thực hiện truy vấn DELETE để xóa dòng dữ liệu thứ hai
            String deleteQuery = "DELETE FROM friendships WHERE _id = " + rowFriendId;
            db.execSQL(deleteQuery);
            cursorDelete.close(); // Đóng con trỏ sau khi đã hoàn thành việc sử dụng nó
        }

        db.close();
    }

    private void loadFriendList() {
        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);
        // Thực hiện việc load danh sách bạn bè từ cơ sở dữ liệu hoặc bộ sưu tập
        ArrayList<User> friendList = getFriendshipRequests(currentUserId); // Thay thế bằng phương thức lấy danh sách bạn bè từ cơ sở dữ liệu

        ListView listView = getView().findViewById(R.id.listFriendShip); // Thay thế bằng ID của RecyclerView trong giao diện của bạn
        FriendAdapter adapter = new FriendAdapter(getActivity(),friendList); // Tạo adapter với danh sách bạn bè
        listView.setAdapter(adapter);

    }

    private void acceptFriendshipRequestFireBase(int userId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference friendshipsRef = database.collection("friendships");

        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        Query query = friendshipsRef.whereEqualTo(ChatDatabaseHelper.COLUMN_FRIENDSHIP_USER1, userId)
                .whereEqualTo(ChatDatabaseHelper.COLUMN_FRIENDSHIP_USER2, currentUserId);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        DocumentReference documentRef = documentSnapshot.getReference();
                        documentRef.update(ChatDatabaseHelper.COLUMN_FRIENDSHIP_STATUS, "accepted")
                                .addOnSuccessListener(aVoid -> {
                                    // Cập nhật thành công
                                })
                                .addOnFailureListener(e -> {
                                    // Xảy ra lỗi
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Xảy ra lỗi khi truy vấn dữ liệu
                });

    }
    public ArrayList<User> getFriendshipRequests(int currentUserId) {
        ArrayList<User> friendshipRequestList = new ArrayList<>();

        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Truy vấn cơ sở dữ liệu để lấy danh sách lời mời kết bạn
        String query = "SELECT users.* FROM users " +
                "INNER JOIN friendships ON users._id = friendships.user1 " +
                "WHERE friendships.user2 = ? AND friendships.friendship_status = 'waiting'";

        String[] selectionArgs = {String.valueOf(currentUserId)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        // Duyệt qua từng hàng trong Cursor
        while (cursor.moveToNext()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            int role = cursor.getInt(cursor.getColumnIndexOrThrow("role"));
            User user = new User(userId, username, phone, email, avatar, role);
            friendshipRequestList.add(user);
        }

        // Đóng Cursor sau khi sử dụng
        cursor.close();
        db.close();

        return friendshipRequestList;
    }


    private ArrayList<User> getUsersByFriendship(int userId) {
        ArrayList<User> userList = new ArrayList<>();

        // Fetch data from the database
        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Query to fetch the list of users based on friendship
        String query = "SELECT users.* FROM users " +
                "INNER JOIN friendships ON users._id = friendships._id " +
                "WHERE friendships.user1 = ?";

        String[] selectionArgs = {String.valueOf(userId)};
        Cursor cursor = db.rawQuery(query, selectionArgs);
//        Cursor cursor = db.rawQuery(query, new String[]{userId});

        // Loop through the cursor and create User objects
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            // Retrieve other user information
            int role = cursor.getInt(cursor.getColumnIndexOrThrow("role"));
            User user = new User(id, username, phone, email, avatar, role);
            userList.add(user);
        }

        // Close the cursor and database connection
        cursor.close();
        db.close();

        return userList;
    }

    @Override
    public void onAcceptFriendClick(int friendUserId) {
        acceptFriendshipRequest(friendUserId);
        acceptFriendshipRequestFireBase(friendUserId);
    }

//    private ArrayList<Friend> getListFriend() {
//
//        ArrayList<Friend> friendList = new ArrayList<>();
//
//        // Fetch data from the database
//        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(requireContext());
//        SQLiteDatabase db = databaseHelper.getReadableDatabase();
//
//        // Get the ID of the currently logged-in user
//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//        String UserId = sharedPreferences.getString("currentUserId", "");
//
////        // Query to fetch the list of friends for the current user
////        String query = "SELECT users.* FROM users " +
////                "INNER JOIN friendships ON users._id = friendships.friendship_id " +
////                    "WHERE friendships.user1 = ?";
//        String query = "SELECT * FROM friendships";
//        Cursor cursor = db.rawQuery(query, null);
//
//        // Loop through the cursor and create Friend objects
//        while (cursor.moveToNext()) {
//            int friendId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
//            String user1 = cursor.getString(cursor.getColumnIndexOrThrow("user1"));
//            String user2 = cursor.getString(cursor.getColumnIndexOrThrow("user2"));
//            String friendship_created_date = cursor.getString(cursor.getColumnIndexOrThrow("friendship_created_date"));
//
//            // Retrieve other friend information
//
//            Friend friend = new Friend(friendId, user1, user2, friendship_created_date);
//            friendList.add(friend);
//        }
//        // Close the cursor and database connection
//        cursor.close();
//        db.close();
//
//        return friendList;
////        String[] selectionArgs = {UserId};
////        Cursor cursor = db.rawQuery(query, selectionArgs);
//
//        // Loop through the cursor and create User objects
////        while (cursor.moveToNext()) {
////            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
////            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
////            String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
////            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
////            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
////            // Retrieve other user information
////
////            User user = new User(userId, username, phone,email, avatar);
////            userListFriendShip.add(user);
////        }
////        // Close the cursor and database connection
////        cursor.close();
////        db.close();
//
////        return userListFriendShip;
//
//    }


}
