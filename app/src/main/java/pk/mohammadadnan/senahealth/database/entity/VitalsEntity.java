package pk.mohammadadnan.senahealth.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vitals_table")
public class VitalsEntity {
    @PrimaryKey
    private long time;
    private int measurementType;
    private float measurementOne;
    private float measurementTwo;
    private float measurementThree;
    private boolean isSynced = false;

    public VitalsEntity(long time, int measurementType, float measurementOne, float measurementTwo, float measurementThree, boolean isSynced) {
        this.time = time;
        this.measurementType = measurementType;
        this.measurementOne = measurementOne;
        this.measurementTwo = measurementTwo;
        this.measurementThree = measurementThree;
        this.isSynced = isSynced;
    }

    public long getTime() {
        return time;
    }

    public int getMeasurementType() {
        return measurementType;
    }

    public float getMeasurementOne() {
        return measurementOne;
    }

    public float getMeasurementTwo() {
        return measurementTwo;
    }

    public float getMeasurementThree() {
        return measurementThree;
    }

    public boolean isSynced() {
        return isSynced;
    }
}
