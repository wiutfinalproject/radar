package uz.radar.wiut.radar.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import uz.radar.wiut.radar.models.LocationObject;
import uz.radar.wiut.radar.utils.Const;

public class AZSDb extends IDbCRUD<LocationObject> implements Const {
    public AZSDb(Context context) {
        super(context);
    }

    @Override
    public void delete(String tableName) {
        super.delete(tableName);
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public long insert(LocationObject objectToInsert) {
        ContentValues values = new ContentValues();
        values.put(ZAPRAVKA_NAME, objectToInsert.getName());
        values.put(ZAPRAVKA_LATITUDE, objectToInsert.getLattitude());
        values.put(ZAPRAVKA_LONGITUDE, objectToInsert.getLongitude());
        return db.insert(TABLE_ZAPRAVKA, null, values);
    }

    @Override
    public LocationObject getById(long id) {
        String quary = "SELECT * FROM "+ TABLE_ZAPRAVKA + " WHERE id == " + id;
        Cursor cursor = db.rawQuery(quary, null);
        LocationObject azsObj = new LocationObject();
        azsObj.setName(cursor.getString(cursor.getColumnIndex(CAM_ROAD)));
        azsObj.setLattitude(cursor.getDouble(cursor.getColumnIndex(CAM_LATITUDE)));
        azsObj.setLongitude(cursor.getDouble(cursor.getColumnIndex(CAM_LONGITUDE)));
        return azsObj;
    }

    @Override
    public ArrayList<LocationObject> getAll() {
        ArrayList<LocationObject> azsList = new ArrayList<>();
        String quary = "SELECT * FROM "+ TABLE_CAM;
        Cursor cursor = db.rawQuery(quary, null);
        if ( cursor.moveToFirst()) {
            do {
                LocationObject azsObj = new LocationObject();
                azsObj.setName(cursor.getString(cursor.getColumnIndex(CAM_ROAD)));
                azsObj.setLattitude(cursor.getDouble(cursor.getColumnIndex(CAM_LATITUDE)));
                azsObj.setLongitude(cursor.getDouble(cursor.getColumnIndex(CAM_LONGITUDE)));
                azsList.add(azsObj);
            } while (cursor.moveToNext());
        }
        return azsList;
    }
}
