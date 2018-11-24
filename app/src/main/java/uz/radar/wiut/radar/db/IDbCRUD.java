package uz.radar.wiut.radar.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public abstract class IDbCRUD<T> {
    protected SQLiteDatabase db;

    public IDbCRUD(Context context) {
        NewDb newDb = new NewDb(context);
        db = newDb.getWritableDatabase();
    }

    public void delete(String tableName) {
        String quary = "SELECT * FROM " + tableName;
        Cursor cursor = db.rawQuery(quary, null);
        if (cursor.moveToFirst()) {
            db.delete(tableName, null, null);
        }
    }


    public void close(){
        db.close();
    }

    public abstract void insert(T objectToInsert);

    public abstract T getById(long id);

    public abstract ArrayList<T> getAll();



}
