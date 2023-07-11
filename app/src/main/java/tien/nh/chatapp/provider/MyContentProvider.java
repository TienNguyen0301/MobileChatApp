package tien.nh.chatapp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import tien.nh.chatapp.ChatDatabaseHelper;

public class MyContentProvider extends ContentProvider {
    // Các hằng số cho Content Provider
    private static final String AUTHORITY = "tien.nh.chatapp.provider";
    private static final String BASE_PATH = "messages";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    // Các mã truy vấn
    private static final int MESSAGES = 1;
    private static final int MESSAGE_ID = 2;

    // Khai báo các URI Matcher
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, MESSAGES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", MESSAGE_ID);
    }

    private ChatDatabaseHelper database;
    @Override
    public boolean onCreate() {
        database = new ChatDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);
        switch (match) {
            case MESSAGES:
                cursor = db.query(ChatDatabaseHelper.TABLE_MESSAGES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MESSAGE_ID:
                selection = ChatDatabaseHelper.COLUMN_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(ChatDatabaseHelper.TABLE_MESSAGES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
