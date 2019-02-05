package uz.radar.wiut.radar.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import uz.radar.wiut.radar.models.LocationObject;
import uz.radar.wiut.radar.utils.Const;

public class CameraDb extends IDbCRUD<LocationObject> implements Const {

    public CameraDb(Context context) {
        super(context);
    }

    @Override
    public void delete(String tableName) {
        super.delete(tableName);
    }

    @Override
    public long insert(LocationObject objectToInsert) {
        ContentValues values = new ContentValues();
        values.put(CAM_ROAD, objectToInsert.getName());
        values.put(CAM_LATITUDE, objectToInsert.getLattitude());
        values.put(CAM_LONGITUDE, objectToInsert.getLongitude());
        return db.insert(TABLE_CAM, null, values);
    }

    @Override
    public LocationObject getById(long id) {
        String quary = "SELECT * FROM "+ TABLE_CAM + " WHERE id == " + id;
        Cursor cursor = db.rawQuery(quary, null);
        LocationObject roadCamera = new LocationObject();
        roadCamera.setName(cursor.getString(cursor.getColumnIndex(CAM_ROAD)));
        roadCamera.setLattitude(cursor.getDouble(cursor.getColumnIndex(CAM_LATITUDE)));
        roadCamera.setLongitude(cursor.getDouble(cursor.getColumnIndex(CAM_LONGITUDE)));
        return roadCamera;
    }

    @Override
    public ArrayList<LocationObject> getAll() {
        ArrayList<LocationObject> cameraList = new ArrayList<>();
        String quary = "SELECT * FROM "+ TABLE_CAM;
        Cursor cursor = db.rawQuery(quary, null);
        if ( cursor.moveToFirst()) {
            do {
                LocationObject roadCamera = new LocationObject();
                roadCamera.setName(cursor.getString(cursor.getColumnIndex(CAM_ROAD)));
                roadCamera.setLattitude(cursor.getDouble(cursor.getColumnIndex(CAM_LATITUDE)));
                roadCamera.setLongitude(cursor.getDouble(cursor.getColumnIndex(CAM_LONGITUDE)));
                cameraList.add(roadCamera);
            } while (cursor.moveToNext());
        }
        return cameraList;
    }


    @Override
    public void close() {
        super.close();
    }
}
