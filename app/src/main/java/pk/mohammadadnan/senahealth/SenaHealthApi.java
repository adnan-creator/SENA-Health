package pk.mohammadadnan.senahealth;

import pk.mohammadadnan.senahealth.models.MeasurementObjects;
import pk.mohammadadnan.senahealth.models.MoodObjects;
import pk.mohammadadnan.senahealth.models.UserIdObjects;
import pk.mohammadadnan.senahealth.models.UserObjects;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SenaHealthApi {

    @POST("wf/userfeels")
    Call<MoodObjects> postMood(@Body MoodObjects moodObjects);

    @POST("wf/appreadings")
    Call<MeasurementObjects> postMeasurement(@Body MeasurementObjects measurementObjects);

    @POST("https://en19mwr8c5zvgx9.m.pipedream.net?")
    Call<String> postUserId(@Body UserIdObjects userIdObjects);

    @GET("obj/user")
    Call<UserObjects> getUsers();
}
