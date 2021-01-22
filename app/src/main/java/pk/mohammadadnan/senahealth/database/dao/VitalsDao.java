package pk.mohammadadnan.senahealth.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pk.mohammadadnan.senahealth.database.entity.VitalsEntity;

@Dao
public interface VitalsDao {
    // The conflict strategy defines what happens,
    // if there is an existing entry.
    // The default action is ABORT.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VitalsEntity vitalsEntity);

    @Update
    void update(VitalsEntity vitalsEntity);

    @Delete
    void delete(VitalsEntity vitalsEntity);

    @Query("SELECT * from vitals_table WHERE measurementType LIKE :measurementType ORDER BY time DESC")
    LiveData<List<VitalsEntity>> getVitals(int measurementType);

    @Query("SELECT * from vitals_table WHERE measurementType LIKE :measurementType ORDER BY time DESC")
    List<VitalsEntity> getAllVitalsInit(int measurementType);

    @Query("SELECT * from vitals_table ORDER BY time DESC")
    LiveData<List<VitalsEntity>> getVitals();

    @Query("SELECT * from vitals_table ORDER BY time DESC")
    List<VitalsEntity> getAllVitalssInit();
}
