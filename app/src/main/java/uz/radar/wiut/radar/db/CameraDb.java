package uz.radar.wiut.radar.db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import uz.radar.wiut.radar.models.LocationObject;

public class CameraDb extends IDbCRUD<LocationObject> {

    public CameraDb(Context context) {
        super(context);
    }

    @Override
    public void delete(String tableName) {
        super.delete(tableName);
    }

    @Override
    public void insert(LocationObject objectToInsert) {

    }

    @Override
    public LocationObject getById(long id) {
        return null;
    }

    @Override
    public ArrayList<LocationObject> getAll() {
        return null;
    }


    @Override
    public void close() {
        super.close();
    }
}
