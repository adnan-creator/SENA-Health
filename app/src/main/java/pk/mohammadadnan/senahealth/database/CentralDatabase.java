package pk.mohammadadnan.senahealth.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pk.mohammadadnan.senahealth.database.dao.VitalsDao;
import pk.mohammadadnan.senahealth.database.entity.VitalsEntity;

@Database(entities = VitalsEntity.class, version = 2)
public abstract class CentralDatabase extends RoomDatabase {

    public abstract VitalsDao vitalsDao();

    private static CentralDatabase INSTANCE;

    public static synchronized CentralDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    CentralDatabase.class, "central_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        Log.e("MY LOG", "CENTRAL DB CALLED");

        return INSTANCE;
    }
}
