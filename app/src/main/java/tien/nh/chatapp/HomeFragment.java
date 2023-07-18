package tien.nh.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;
import android.widget.Toast;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements UserAdapter.OnAddFriendClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_home2, container, false);

        // Ánh xạ ListView từ layout
        ListView listViewUsers = rootView.findViewById(R.id.listViewUsers);

        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        ArrayList<User> userOtherList = getOtherUsers(currentUserId);

        // Khởi tạo adapter và thiết lập danh sách người dùng
        UserAdapter userAdapter = new UserAdapter(requireContext(), userOtherList);
        listViewUsers.setAdapter(userAdapter);
        userAdapter.setOnAddFriendClickListener(this::onAddFriendClick);

        return rootView;
    }


    private ArrayList<User> getOtherUsers(int currentUserId) {
        ArrayList<User> userOtherList = new ArrayList<>();

        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Truy vấn cơ sở dữ liệu để lấy danh sách người dùng khác
        String query = "SELECT * FROM users WHERE _id <> ? AND role = 0";
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

            // Kiểm tra nếu người dùng không phải là người đang đăng nhập
            if (currentUserId != userId) {
                User user = new User(userId, username, phone, email, avatar, role);
                userOtherList.add(user);
            }
        }
        // Đóng Cursor sau khi sử dụng
        cursor.close();
        return userOtherList;
    }

    private void addFriend(int friendUserId) {
        // Thực hiện các bước để thêm mối quan hệ kết bạn vào cơ sở dữ liệu hoặc bộ sưu tập
        // Ví dụ: sử dụng cơ sở dữ liệu SQLite
        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Lấy thông tin người dùng đang đăng nhập
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);

        // Lấy thời gian hiện tại
        Date currentTime = new Date();

        // Định dạng thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(currentTime);

        // Thực hiện truy vấn INSERT để thêm mối quan hệ kết bạn
        ContentValues values = new ContentValues();
        values.put("user1", currentUserId); // ID của người dùng hiện tại
        values.put("user2", friendUserId); // ID của người dùng muốn kết bạn
        values.put("friendship_status", "waiting"); // ID của người dùng muốn kết bạn

        values.put("friendship_created_date", formattedTime);

        addDataToFireBase(currentUserId, friendUserId, "waiting", formattedTime);
        long insertedId = db.insert("friendships", null, values);

        // Trước khi thêm lời mời kết bạn, kiểm tra sự tồn tại của lời mời đã gửi từ user1 đến user2
//        if (isFriendshipRequestSent(currentUserId, friendUserId)) {
//            // Lời mời kết bạn đã tồn tại, không thêm vào cơ sở dữ liệu
//            Toast.makeText(getContext(), "Friendship request already sent", Toast.LENGTH_SHORT).show();
//        } else {
//            // Thêm lời mời kết bạn vào cơ sở dữ liệu
//            //
//            // Thực hiện truy vấn INSERT để thêm mối quan hệ kết bạn
//            ContentValues values = new ContentValues();
//            values.put("user1", currentUserId); // ID của người dùng hiện tại
//            values.put("user2", friendUserId); // ID của người dùng muốn kết bạn
//            values.put("friendship_status", "waiting"); // ID của người dùng muốn kết bạn
//
//            values.put("friendship_created_date", formattedTime);
//
//            long insertedId = db.insert("friendships", null, values);
//            if (insertedId > -1) {
//                // Insertion successful
//                Toast.makeText(requireContext(), "ADD FRIEND SUCCESS", Toast.LENGTH_SHORT).show();
//            } else {
//                // Insertion failed
//                Toast.makeText(requireContext(), "ADD FRIEND FAILED", Toast.LENGTH_SHORT).show();
//            }
//
//        }
        // Đóng kết nối cơ sở dữ liệu sau khi sử dụng
        db.close();

    }

    private void addDataToFireBase( int user1, int user2, String friendship_status, String created_date){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersRef = database.collection(ChatDatabaseHelper.TABLE_FRIENDSHIPS);

        // Kiểm tra xem bộ sưu tập "friendships" có bất kỳ tài liệu nào hay không
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot.isEmpty()) {
                    // Bộ sưu tập "users" chưa có tài liệu nào, tạo một tài liệu mới với ID là 1
                    createNewFriendShip(usersRef.document("1"),user1,user2, friendship_status,created_date);
                } else {
                    // Bộ sưu tập "users" đã có tài liệu, sử dụng transaction để tăng giá trị ID lên 1 và tạo tài liệu mới
                    createFriendShipWithIncrementedId(usersRef, user1,user2, friendship_status,created_date);
                }
            } else {
                // Lỗi khi truy vấn bộ sưu tập "users"
                Toast.makeText(getContext(), "Failed to query users collection", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void createNewFriendShip(DocumentReference userRef, int user1, int user2, String friendship_status, String created_date) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("user1", user1);
        userData.put("user2", user2);
        userData.put("friendship_status", friendship_status);
        userData.put("created_date", created_date);

        // Tạo tài liệu mới với ID là 1
        userRef.set(userData).addOnSuccessListener(aVoid -> {
            // Đăng ký người dùng thành công
            Toast.makeText(getContext(), "Successed", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            // Lỗi khi đăng ký người dùng
            Toast.makeText(getContext() , "Failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void createFriendShipWithIncrementedId(CollectionReference usersRef, int user1, int user2, String friendship_status, String created_date) {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                if (documents.isEmpty()) {
                    // Bộ sưu tập "users" chưa có tài liệu nào, tạo tài liệu mới với ID là 1
                    createNewFriendShip(usersRef.document("1"), user1,user2, friendship_status,created_date);
                } else {
                    // Lấy tài liệu cuối cùng trong danh sách và tăng giá trị ID lên 1
                    DocumentSnapshot lastDocument = documents.get(documents.size() - 1);
                    String lastUserId = lastDocument.getId();
                    long newId = Long.parseLong(lastUserId) + 1;

                    // Tạo tài liệu mới với ID mới
                    createNewFriendShip(usersRef.document(String.valueOf(newId)),  user1,user2, friendship_status,created_date);
                }
            } else {
                // Lỗi khi truy vấn bộ sưu tập "users"
                Toast.makeText(getContext(), "Failed to query users collection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isFriendshipRequestSent(int user1Id, int user2Id) {
        ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String query = "SELECT * FROM friendships WHERE user1 = ? AND user2 = ?";
        String[] selectionArgs = {String.valueOf(user1Id), String.valueOf(user2Id)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        boolean requestSent = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return requestSent;
    }
    @Override
    public void onAddFriendClick(int friendUserId) {
        addFriend(friendUserId);
    }
}

