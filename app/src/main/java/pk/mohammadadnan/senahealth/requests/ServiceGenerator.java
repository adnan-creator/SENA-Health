package pk.mohammadadnan.senahealth.requests;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pk.mohammadadnan.senahealth.SenaHealthApi;
import pk.mohammadadnan.senahealth.utilities.Constants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Modeled after @brittbarak's example on Github
 * https://github.com/brittBarak/NetworkingDemo
 * https://twitter.com/brittbarak
 */
public class ServiceGenerator {

//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
//            @Override
//            public okhttp3.Response intercept(Chain chain) throws IOException {
//                Request newRequest  = chain.request().newBuilder()
//                        .addHeader("Authorization", "Bearer " + "cb814402cd2fab41c78effd159ea4b74")
//                        .build();
//                return chain.proceed(newRequest);
//            }
//        }).build();

    public static SenaHealthApi getSenaHealthApi(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(SenaHealthApi.class);
    }
}
