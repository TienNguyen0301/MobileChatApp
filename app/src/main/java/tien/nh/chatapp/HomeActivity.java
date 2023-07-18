package tien.nh.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import android.widget.FrameLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.widget.PopupMenu;



public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    ImageView m_avatar_user;

    TextView status_user;
    ImageButton overflowButton;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        m_avatar_user = (ImageView) findViewById(R.id.avatar_user);
        status_user = (TextView) findViewById(R.id.status_user);

        // Nhận thông tin user từ Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String avatarPath = intent.getStringExtra("avatar");
        int role = intent.getIntExtra("role", 0);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);



        // Check status user
        // Cập nhật thông tin người dùng trên Firebase Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userRef = firestore.collection(ChatDatabaseHelper.TABLE_USERS).document(String.valueOf(currentUserId));
        userRef.update("status", "online");

        // Đăng ký lắng nghe sự thay đổi dữ liệu trên Firebase Firestore
        CollectionReference usersRef = firestore.collection(ChatDatabaseHelper.TABLE_USERS);
        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Xử lý lỗi khi nhận sự kiện thay đổi dữ liệu
                    return;
                }

                // Kiểm tra nếu có sự thay đổi dữ liệu
                if (value != null && !value.isEmpty()) {
                    status_user.setText("Online");

                }
            }
        });

        // Hiển thị hình ảnh avatar
        Glide.with(this)
                .load(avatarPath)
                .error(android.R.drawable.stat_notify_error) // Ảnh hiển thị khi xảy ra lỗi trong quá trình tải hình ảnh
                .apply(RequestOptions.circleCropTransform())
                .into(m_avatar_user);

        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        
        //show menu option
        overflowButton = findViewById(R.id.overflow_button);
        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), overflowButton);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.setting_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = R.id.menu_item1;
                        // Toast message on menu item clicked
                        if(menuItem.getItemId() == id){
                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear(); // Xóa tất cả dữ liệu trong SharedPreferences
                            editor.apply(); // Áp dụng thay đổi

                            // Check status user
                            // Cập nhật thông tin người dùng trên Firebase Firestore
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            DocumentReference userRef = firestore.collection(ChatDatabaseHelper.TABLE_USERS).document(String.valueOf(currentUserId));
                            userRef.update("status", "offline");

                            //chuyển sang màn hình đăng nhập
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish(); // Đóng màn hình hiện tại (nếu cần thiết)
                        }
                        Toast.makeText(getApplicationContext(), "Bạn đã đăng xuất ", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });
        
        // Tạo instance của HomeFragment
        HomeFragment homeFragment = new HomeFragment();
        showFragment(homeFragment);

        Bundle bundle = new Bundle();
        bundle.putInt("currentUserId", currentUserId);

        Fragment fragment = new HomeFragment();
        fragment.setArguments(bundle);
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);
        // Cập nhật thông tin người dùng trên Firebase Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userRef = firestore.collection(ChatDatabaseHelper.TABLE_USERS).document(String.valueOf(currentUserId));
        userRef.update("status", "offline");
    }

    @Override
    public void onDestroy() {
        // Cập nhật thông tin người dùng trên Firebase Firestore
        super.onDestroy();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("currentUserId", 0);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userRef = firestore.collection(ChatDatabaseHelper.TABLE_USERS).document(String.valueOf(currentUserId));
        userRef.update("status", "offline");
    }

    // Hiển thị fragment trong container
    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Ẩn fragment hiện tại nếu có
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        // Hiển thị fragment mới
        transaction.add(R.id.fragment_container, fragment);
        transaction.show(fragment);
        transaction.commit();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_home){
            showFragment(new HomeFragment());
            return true;
        }
//        if (item.getItemId() == R.id.action_search){
//            showFragment(new ListFriendFragment());
//            return true;
//        }
        if (item.getItemId() == R.id.action_profile){
            showFragment(new Fragment1());
            return true;
        }
        if (item.getItemId() == R.id.action_us){
            showFragment(new DefaultFragment());
            return true;
        }

        return false;

    }
}