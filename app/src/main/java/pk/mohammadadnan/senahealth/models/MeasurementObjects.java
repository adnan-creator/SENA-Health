package pk.mohammadadnan.senahealth.models;

public class MeasurementObjects {
    
    private long Timestamp;
    private int MeasurementType;
    private float MeasurementOne;
    private float MeasurementTwo;
    private float MeasurementThree;
    private String id;

    public MeasurementObjects(long timestamp, int measurementType, float measurementOne, float measurementTwo, float measurementThree, String id) {
        Timestamp = timestamp;
        MeasurementType = measurementType;
        MeasurementOne = measurementOne;
        MeasurementTwo = measurementTwo;
        MeasurementThree = measurementThree;
        this.id = id;
    }

    public long getTimestamp() {
        return Timestamp;
    }

    public int getMeasurementType() {
        return MeasurementType;
    }

    public float getMeasurementOne() {
        return MeasurementOne;
    }

    public float getMeasurementTwo() {
        return MeasurementTwo;
    }

    public float getMeasurementThree() {
        return MeasurementThree;
    }

    public String getId() {
        return id;
    }
}
