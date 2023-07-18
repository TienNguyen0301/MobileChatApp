package tien.nh.chatapp;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 3;


    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_MESSAGES = "messages";
    public static final String TABLE_FRIENDSHIPS = "friendships";




    // Users table column
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_AVATAR = "avatar";
    public static final String COLUMN_USER_ROLE = "role";

    // Friendships table column names
    public static final String COLUMN_FRIENDSHIP_USER1 = "user1";
    public static final String COLUMN_FRIENDSHIP_USER2 = "user2";
    public static final String COLUMN_FRIENDSHIP_STATUS = "friendship_status";
    public static final String COLUMN_FRIENDSHIP_ID = "friendship_id";

    public static final String COLUMN_FRIENDSHIP_CREATED_DATE = "friendship_created_date";


    // Messages table column names
    public static final String COLUMN_SENDER_ID = "sender_id";
    public static final String COLUMN_RECEIVER_ID = "receiver_id";
    public static final String COLUMN_MESSAGE_TEXT = "message_text";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_MESSAGE_IMAGE = "image";
    public static final String COLUMN_MESSAGE_STATUS = "status";

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

    // Create friendships table query
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

    public void deleteAllUsers() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_USERS, null, null);
        db.close();
    }

    public void deleteAllFriendShips() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FRIENDSHIPS, null, null);
        db.close();
    }

    public void deleteAllMessages() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MESSAGES, null, null);
        db.close();
    }

    public void insertUser(int id, String name, String email, String phone, String password, String avatar, int role) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PHONE, phone);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_AVATAR, avatar);
        values.put(COLUMN_USER_ROLE, role);

        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public void insertFriendship(int id, int user1, int user2, String friendship_status, String friendship_created_date) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_FRIENDSHIP_USER1, user1);
        values.put(COLUMN_FRIENDSHIP_USER2, user2);
        values.put(COLUMN_FRIENDSHIP_STATUS, friendship_status);
        values.put(COLUMN_FRIENDSHIP_CREATED_DATE, friendship_created_date);

        db.insert(TABLE_FRIENDSHIPS, null, values);
        db.close();
    }

    public void insertMessage(int id, int friendship_id, int sender_id, int receiver_id, String message_text, String status, String timestamp) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_FRIENDSHIP_ID, friendship_id);
        values.put(COLUMN_SENDER_ID, sender_id);
        values.put(COLUMN_RECEIVER_ID, receiver_id);
        values.put(COLUMN_MESSAGE_TEXT, message_text);
        values.put(COLUMN_MESSAGE_STATUS, status);
        values.put(COLUMN_TIMESTAMP, timestamp);

        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }




}
