package uz.radar.wiut.radar.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import uz.radar.wiut.radar.models.LocationObject;
import uz.radar.wiut.radar.utils.Const;

public class VulkanizatsiyaDb extends IDbCRUD<LocationObject> implements Const {
    public VulkanizatsiyaDb(Context context) {
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
        values.put(VULKANIZACIYA_NAME, objectToInsert.getName());
        values.put(VULKANIZACIYA_LATITUDE, objectToInsert.getLattitude());
        values.put(VULKANIZACIYA_LONGITUDE, objectToInsert.getLongitude());
        return db.insert(TABLE_VULKANIZACIYA, null, values);
    }

    @Override
    public LocationObject getById(long id) {
        String quary = "SELECT * FROM "+ TABLE_VULKANIZACIYA + " WHERE id == " + id;
        Cursor cursor = db.rawQuery(quary, null);
        LocationObject vulkObj = new LocationObject();
        vulkObj.setName(cursor.getString(cursor.getColumnIndex(CAM_ROAD)));
        vulkObj.setLattitude(cursor.getDouble(cursor.getColumnIndex(CAM_LATITUDE)));
        vulkObj.setLongitude(cursor.getDouble(cursor.getColumnIndex(CAM_LONGITUDE)));
        return vulkObj;
    }

    @Override
    public ArrayList<LocationObject> getAll() {
        ArrayList<LocationObject> vulkCamera = new ArrayList<>();
        String quary = "SELECT * FROM "+ TABLE_VULKANIZACIYA;
        Cursor cursor = db.rawQuery(quary, null);
        if ( cursor.moveToFirst()) {
            do {
                LocationObject vulkObj = new LocationObject();
                vulkObj.setName(cursor.getString(cursor.getColumnIndex(CAM_ROAD)));
                vulkObj.setLattitude(cursor.getDouble(cursor.getColumnIndex(CAM_LATITUDE)));
                vulkObj.setLongitude(cursor.getDouble(cursor.getColumnIndex(CAM_LONGITUDE)));
                vulkCamera.add(vulkObj);
            } while (cursor.moveToNext());
        }
        return vulkCamera;
    }
}
