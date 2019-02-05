package uz.radar.wiut.radar.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import uz.radar.wiut.radar.utils.Const;

public class NewDb extends SQLiteOpenHelper implements Const {
    private static final int DB_VERSION = 1;

    public NewDb(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private static final String NEW_CAMERA_TABLE = "CREATE TABLE " + TABLE_CAM + "("
            + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CAM_ROAD + " TEXT NULL, "
            + CAM_LATITUDE + " REAL, "
            + CAM_LONGITUDE + " REAL" + ");";

    private static final String NEW_ZAPRAVKA_TABLE = "CREATE TABLE " + TABLE_ZAPRAVKA + "("
            + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ZAPRAVKA_NAME + " TEXT NULL, "
            + ZAPRAVKA_LATITUDE + " REAL, "
            + ZAPRAVKA_LONGITUDE + " REAL, "
            + ZAPRAVKA_TYPE + " TEXT NULL" + ");";

    private static final String NEW_VULKANIZACIYA_TABLE = "CREATE TABLE " + TABLE_VULKANIZACIYA + "("
            + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + VULKANIZACIYA_NAME + " TEXT NULL, "
            + VULKANIZACIYA_LATITUDE + " REAL, "
            + VULKANIZACIYA_LONGITUDE + " REAL" + ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAM);
        db.execSQL(NEW_CAMERA_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ZAPRAVKA);
        db.execSQL(NEW_ZAPRAVKA_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VULKANIZACIYA);
        db.execSQL(NEW_VULKANIZACIYA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CAM);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ZAPRAVKA);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_VULKANIZACIYA);
        onCreate(sqLiteDatabase);
    }
}
