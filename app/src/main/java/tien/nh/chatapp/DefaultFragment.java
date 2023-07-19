package tien.nh.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import android.widget.AdapterView;
import android.content.Intent;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DefaultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DefaultFragment extends Fragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;




    public DefaultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DefaultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DefaultFragment newInstance(String param1, String param2) {
        DefaultFragment fragment = new DefaultFragment();
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
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_default, container, false);
        View rootView = inflater.inflate(R.layout.fragment_default, container, false);

        // Ánh xạ ListView từ layout
        ListView listFriends = rootView.findViewById(R.id.listFriend);
        Button buttonSearch = rootView.findViewById(R.id.buttonSearch);
        EditText editTextSearch = rootView.findViewById(R.id.editTextSearch);

        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        ArrayList<User> getFriendList = getFriendList(currentUserId);

//        UserAdapter userFriendAdapter = new UserAdapter(getActivity(), usersByFriendship);
        FriendAdapter friendList = new FriendAdapter(getActivity(), getFriendList);
        listFriends.setAdapter(friendList);

        //Chuyển sang activity chat với user mà người dùng chọn
        listFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Xử lý sự kiện khi người dùng chạm vào item

                // Lấy thông tin về user được chọn từ danh sách bạn bè
                User selectedUser = (User) parent.getItemAtPosition(position);

                // Chuyển sang Activity mới và truyền dữ liệu của item
                Intent intent = new Intent(getActivity(), ChatActivity.class);

                intent.putExtra("_id", selectedUser.getId()); // Truyền ID của user được chọn
                intent.putExtra("name", selectedUser.getUsername()); // Truyền tên của user được chọn
                intent.putExtra("email", selectedUser.getEmail());
                intent.putExtra("avatar", selectedUser.getAvatar());
                startActivity(intent);
            }
        });

        // xử lí khi người dùng nhấn nút search
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = editTextSearch.getText().toString();
                performSearch(searchQuery);
                ArrayList<User> userListSearch =  performSearch(searchQuery);
                FriendAdapter friendList = new FriendAdapter(getActivity(), userListSearch);
                listFriends.setAdapter(friendList);
            }

        });


        return rootView;
    }

    private ArrayList<User> performSearch(String searchQuery) {
        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        ArrayList<User> userListSearch = new ArrayList<>();

        // Thực hiện truy vấn tìm kiếm
        if (TextUtils.isEmpty(searchQuery)) {
            userListSearch = getFriendList(currentUserId);
        } else {
            String query = "SELECT * FROM users WHERE email LIKE '%" + searchQuery + "%'";
            Cursor cursor = db.rawQuery(query, null);


            // Trích xuất dữ liệu từ Cursor và tạo danh sách người dùng
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Lấy thông tin từ Cursor
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                    String username = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                    int role = cursor.getInt(cursor.getColumnIndexOrThrow("role"));


                    // Tạo đối tượng User từ thông tin lấy được
                    User user = new User(userId, username, phone, email, avatar, role);
                    // Thêm user vào danh sách
                    userListSearch.add(user);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }
        db.close();

        return userListSearch;
    }

    public ArrayList<User> getFriendList(int currentUserId) {
        ArrayList<User> getFriendList = new ArrayList<>();

        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(requireContext());
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