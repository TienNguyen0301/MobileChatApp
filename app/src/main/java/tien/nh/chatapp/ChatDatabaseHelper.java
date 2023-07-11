package tien.nh.chatapp;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 3;


    // Table names
    private static final String TABLE_USERS = "users";
    public static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_FRIENDSHIPS = "friendships";




    // Users table column
    public static final String COLUMN_ID = "_id";
    private static final String COLUMN_USER_NAME = "name";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String COLUMN_USER_PHONE = "phone";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_AVATAR = "avatar";
    private static final String COLUMN_USER_ROLE = "role";

    // Friendships table column names
    private static final String COLUMN_FRIENDSHIP_USER1 = "user1";
    private static final String COLUMN_FRIENDSHIP_USER2 = "user2";
    private static final String COLUMN_FRIENDSHIP_STATUS = "friendship_status";
    private static final String COLUMN_FRIENDSHIP_ID = "friendship_id";

    private static final String COLUMN_FRIENDSHIP_CREATED_DATE = "friendship_created_date";


    // Messages table column names
    private static final String COLUMN_SENDER_ID = "sender_id";
    private static final String COLUMN_RECEIVER_ID = "receiver_id";
    private static final String COLUMN_MESSAGE_TEXT = "message_text";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_MESSAGE_IMAGE = "image";
    private static final String COLUMN_MESSAGE_STATUS = "status";

    // Create users table query
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
            + COLUMN_USER_PHONE + " TEXT,"
            + COLUMN_USER_PASSWORD + " TEXT,"
            + COLUMN_USER_AVATAR + " TEXT,"
            + COLUMN_USER_ROLE + " TEXT DEFAULT '0'"
            + ")";

    // Create conversations table query
    private static final String CREATE_TABLE_FRIENDSHIPS = "CREATE TABLE " + TABLE_FRIENDSHIPS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FRIENDSHIP_USER1 + " INTEGER,"
            + COLUMN_FRIENDSHIP_USER2 + " INTEGER,"
            + COLUMN_FRIENDSHIP_STATUS + " TEXT,"
            + COLUMN_FRIENDSHIP_CREATED_DATE + " DATETIME,"
            + "FOREIGN KEY(" + COLUMN_FRIENDSHIP_USER1 + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_FRIENDSHIP_USER2 + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ")";


    // Create messages table query
    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FRIENDSHIP_ID + " INTEGER,"
            + COLUMN_SENDER_ID + " INTEGER,"
            + COLUMN_RECEIVER_ID + " INTEGER,"
            + COLUMN_MESSAGE_TEXT + " TEXT,"
            + COLUMN_MESSAGE_IMAGE + " TEXT,"
            + COLUMN_MESSAGE_STATUS + " TEXT,"
            + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COLUMN_FRIENDSHIP_ID + ") REFERENCES " + TABLE_FRIENDSHIPS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_SENDER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_RECEIVER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ")";


    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all the tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_FRIENDSHIPS);
        db.execSQL(CREATE_TABLE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDSHIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);

        if (oldVersion < 2) {
            // Thêm cột "avatar" vào bảng "users"
//            db.execSQL("ALTER TABLE users ADD COLUMN avatar TEXT");
            onCreate(db);
        }

        // Create fresh tables
        onCreate(db);
    }

    public FriendshipInfo getFriendshipInfo(int userId1, int userId2) {
        FriendshipInfo friendshipInfo = null;

        SQLiteDatabase db = getReadableDatabase();

        // Thực hiện truy vấn để kiểm tra mối quan hệ bạn bè giữa hai người dùng
        String query = "SELECT * FROM friendships " +
                "WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?) AND friendship_status = 'accept'";

        String[] selectionArgs = {String.valueOf(userId1), String.valueOf(userId2), String.valueOf(userId2), String.valueOf(userId1)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            int user1Id = cursor.getInt(cursor.getColumnIndexOrThrow("user1"));
            int user2Id = cursor.getInt(cursor.getColumnIndexOrThrow("user2"));
            friendshipInfo = new FriendshipInfo(user1Id, user2Id);
        }

        cursor.close();
        db.close();

        return friendshipInfo;
    }
}
