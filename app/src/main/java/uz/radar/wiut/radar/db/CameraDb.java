package uz.radar.wiut.radar.db;

import android.content.Context;

import java.util.List;

public class CameraDb extends IDbCRUD<Object> {

    public CameraDb(Context context) {
        super(context);
    }

    @Override
    public void delete(String tableName) {
        super.delete(tableName);
    }

    @Override
    public void insert(Object objectToInsert) {

    }

    @Override
    public Object getById(long id) {
        return null;
    }

    @Override
    public List<Object> getAll() {
        return null;
    }


    @Override
    public void close() {
        super.close();
    }
}
