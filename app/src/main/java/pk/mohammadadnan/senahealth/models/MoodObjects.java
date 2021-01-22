package pk.mohammadadnan.senahealth.models;

public class MoodObjects {

    private long timestamp;
    private int feelingToday;
    private int normallyFeel;
    private String userId;

    public MoodObjects(long timestamp, int feelingToday, int normallyFeel, String userId) {
        this.timestamp = timestamp;
        this.feelingToday = feelingToday;
        this.normallyFeel = normallyFeel;
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getFeelingToday() {
        return feelingToday;
    }

    public int getNormallyFeel() {
        return normallyFeel;
    }

    public String getUserId() {
        return userId;
    }
}
