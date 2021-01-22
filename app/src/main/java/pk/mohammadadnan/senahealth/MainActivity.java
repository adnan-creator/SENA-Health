package pk.mohammadadnan.senahealth;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pk.mohammadadnan.senahealth.models.MeasurementObjects;
import pk.mohammadadnan.senahealth.database.entity.VitalsEntity;
import pk.mohammadadnan.senahealth.ui.viewmodels.VitalsViewModel;
import pk.mohammadadnan.senahealth.utilities.CSVWriter;
import pk.mohammadadnan.senahealth.utilities.DateUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public static BottomNavigationView navView;
    private VitalsViewModel vitalsViewModel;

    private SenaHealthApi senaHealthApi;

    private boolean isSyncing = false;

    private ArrayList<VitalsEntity> vitalsEntityArrayList;

    public int needRequestPermission;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private SharedPreferences prefs;

    final String PREFS_FILE = "MyPrefsFile";

    final String PREF_ID = "my_id";
    final String DOESNT_EXIST_STRING = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vitalsViewModel = new ViewModelProvider(MainActivity.this).get(VitalsViewModel.class);

        prefs = this.getApplicationContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        vitalsEntityArrayList = new ArrayList<>();

        navView = findViewById(R.id.nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sena.health/api/1.1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        senaHealthApi = retrofit.create(SenaHealthApi.class);
        //getPosts();
        //getComments();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (needRequestPermission == 0) {
                needRequestPermission = !checkPermissions() ? 1 : 2;
                if (needRequestPermission == 1) {
                    requestPermissions();
                }
            }
        }

        vitalsViewModel.getAllVitals().observe(MainActivity.this, allvitals -> {
            vitalsEntityArrayList = new ArrayList<>(allvitals);
            syncData();
        });

//        fixBlinking();

    }

    @Override
    protected void onResume() {
        super.onResume();
        syncData();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermissions() {
        // Android M Permission check 
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions() {
        // Android M Permission check 

        requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_COARSE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length == 0) {
            return;
        }

        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                for(int i=0; i<permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            if (needRequestPermission == 1) {
                                needRequestPermission = 2;
                            }
                        }
                    }
                }
                return;
        }
    }

    public static void hideBottomNav(){
        navView.setVisibility(View.GONE);
    }

    public static void showBottomNav(){
        navView.setVisibility(View.VISIBLE);
    }

    private void postMeasurement(VitalsEntity vitalsEntity) {

        MeasurementObjects measurement = new MeasurementObjects(
                vitalsEntity.getTime(),
                vitalsEntity.getMeasurementType(),
                vitalsEntity.getMeasurementOne(),
                vitalsEntity.getMeasurementTwo(),
                vitalsEntity.getMeasurementThree(),
                prefs.getString(PREF_ID,DOESNT_EXIST_STRING)
        );

        Call<MeasurementObjects> call = senaHealthApi.postMeasurement(measurement);
        call.enqueue(new Callback<MeasurementObjects>() {
            @Override
            public void onResponse(Call<MeasurementObjects> call, Response<MeasurementObjects> response) {
                if (!response.isSuccessful()) {
                    Log.e("UNSUCCESSFUL CODE: --------> ",response.code()+"");
                }else {
                    Log.e("SUCCESSFUL CODE: --------> ",response.code()+"");
                    vitalsViewModel.insert(new VitalsEntity(
                            vitalsEntity.getTime(),
                            vitalsEntity.getMeasurementType(),
                            vitalsEntity.getMeasurementOne(),
                            vitalsEntity.getMeasurementTwo(),
                            vitalsEntity.getMeasurementThree(),
                            true
                    ));
                }
            }
            @Override
            public void onFailure(Call<MeasurementObjects> call, Throwable t) {
                Log.e("FAILURE CODE: --------> ",t.getMessage());
            }
        });
    }

    private void syncData(){
        if(!isSyncing){
            isSyncing = true;
            for(VitalsEntity vitalsEntity:vitalsEntityArrayList){
                if(!vitalsEntity.isSynced()){
                    postMeasurement(vitalsEntity);
                }
            }
            isSyncing = false;
            boolean isSynced = true;
            for(VitalsEntity vitalsEntity:vitalsEntityArrayList){
                if (!vitalsEntity.isSynced()) {
                    isSynced = false;
                    break;
                }
            }
//            if(isSynced){
//                Toast.makeText(getApplicationContext(),"All values are synced!",Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(getApplicationContext(),"Check connection and sync again!",Toast.LENGTH_SHORT).show();
//            }
        }
    }

    private void fixBlinking(){

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navView.getChildAt(0);
        try {
            Field field = menuView.getClass().getDeclaredField("set");
            field.setAccessible(true);
            AutoTransition transitionSet = (AutoTransition) field.get(menuView);
            for(int i = transitionSet.getTransitionCount()-1;i>=0;i--){
                Transition transition = transitionSet.getTransitionAt(i);
                if (transition != null) {
                    transitionSet.removeTransition(transition);
                }
            }
            field.set(menuView,transitionSet);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e("Main Activity fix blinking--->", e.getMessage(), e);
        }
    }
}