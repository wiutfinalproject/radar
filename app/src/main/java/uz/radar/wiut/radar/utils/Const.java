package uz.radar.wiut.radar.utils;

public interface Const {

    String DB_NAME = "radarDb";
    String ROW_ID = "_id";

//    Radar-Camera
    String TABLE_CAM = "camera";
    String CAM_ROAD = "name";
    String CAM_LATITUDE = "latitude";
    String CAM_LONGITUDE = "longitude";

    //table zapravka
    String TABLE_ZAPRAVKA = "zapravka";
    String ZAPRAVKA_NAME = "name";
    String ZAPRAVKA_LATITUDE = "latitude";
    String ZAPRAVKA_LONGITUDE = "longitude";
    String ZAPRAVKA_TYPE = "type";

    //table vulkanizaciya
    String TABLE_VULKANIZACIYA = "vulkanizaciya";
    String VULKANIZACIYA_NAME = "name";
    String VULKANIZACIYA_LATITUDE = "latitude";
    String VULKANIZACIYA_LONGITUDE = "longitude";

    String NOTIFICATION = "notification";
    String LANGUAGE = "language";
    String RUSSIAN = "ru";
    String UZBEK= "uz";
    String RESTARTING = "restart";
    Integer RADIUS = 200;
    Integer RADIUS_1KM = 1000;
    Integer VISIBLE_SPEED = 10;

    String HOST = "firebase";

    int LOCAION_UPDATING_TIME = 1000*2;
    String UTF_8 = "UTF-8";
}
