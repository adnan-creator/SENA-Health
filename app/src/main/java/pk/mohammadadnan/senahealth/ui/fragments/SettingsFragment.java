package pk.mohammadadnan.senahealth.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pk.mohammadadnan.senahealth.MainActivity;
import pk.mohammadadnan.senahealth.R;
import pk.mohammadadnan.senahealth.SenaHealthApi;
import pk.mohammadadnan.senahealth.database.entity.VitalsEntity;
import pk.mohammadadnan.senahealth.models.MoodObjects;
import pk.mohammadadnan.senahealth.ui.viewmodels.VitalsViewModel;
import pk.mohammadadnan.senahealth.utilities.CSVWriter;
import pk.mohammadadnan.senahealth.utilities.DateUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xdroid.toaster.Toaster;

public class SettingsFragment extends Fragment {

    private VitalsViewModel vitalsViewModel;

    private ImageView avatar;
    private TextView name;
    private TextView email;
    private Button export;
    private Button logout;

    private ArrayList<VitalsEntity> vitalsEntityArrayList;

    private SharedPreferences prefs;

    final String PREFS_FILE = "MyPrefsFile";

    final String PREF_LOGIN = "is_logged_in";

    final String PREF_EMAIL = "my_email";
    final String PREF_SHA256_PASSWORD = "my_password";
    final String PREF_ID = "my_id";
    final String PREF_NAME = "my_name";
    final String PREF_AVATAR = "my_avatar";
    final String DOESNT_EXIST_STRING = "";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        vitalsViewModel = new ViewModelProvider(SettingsFragment.this).get(VitalsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = this.getActivity().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        vitalsEntityArrayList = new ArrayList<>();

        avatar = root.findViewById(R.id.avatar_settings);
        name = root.findViewById(R.id.name_settings);
        email = root.findViewById(R.id.email_settings);
        export = root.findViewById(R.id.export_settings);
        logout = root.findViewById(R.id.logout_settings);

        name.setText(prefs.getString(PREF_NAME,DOESNT_EXIST_STRING));
        email.setText(prefs.getString(PREF_EMAIL,DOESNT_EXIST_STRING));

        String url = prefs.getString(PREF_AVATAR,DOESNT_EXIST_STRING);

        if(!url.equals("")){
            Ion.with(avatar)
                    // use a placeholder google_image if it needs to load from the network
                    .placeholder(R.drawable.loading)
                    // load the url
                    .load("http:"+url);
        }

        vitalsViewModel.getAllVitals().observe(getViewLifecycleOwner(), allvitals -> {
            vitalsEntityArrayList = new ArrayList<>(allvitals);
        });

        export.setOnClickListener(view -> {
            export(vitalsEntityArrayList);
        });

        logout.setOnClickListener(view -> {
            prefs.edit().putString(PREF_EMAIL,DOESNT_EXIST_STRING).apply();
            prefs.edit().putString(PREF_SHA256_PASSWORD,DOESNT_EXIST_STRING).apply();
            prefs.edit().putString(PREF_ID,DOESNT_EXIST_STRING).apply();
            prefs.edit().putString(PREF_NAME,DOESNT_EXIST_STRING).apply();
            prefs.edit().putString(PREF_AVATAR,DOESNT_EXIST_STRING).apply();

            prefs.edit().putBoolean(PREF_LOGIN,false).apply();

            Intent intent = new Intent(getActivity(), LoginFragment.class);
            startActivity(intent);

            getActivity().finish();
        });

        return root;
    }

    private void export(List<VitalsEntity> vitalsEntities){
        if( !pk.mohammadadnan.senahealth.utilities.FileUtils.isExternalStorageWritable() ){
            Toast.makeText(getActivity(),"Cannot write to external storage!",Toast.LENGTH_SHORT).show();
            return;
        }
        File backupDir = pk.mohammadadnan.senahealth.utilities.FileUtils.createDirIfNotExist(pk.mohammadadnan.senahealth.utilities.FileUtils.getAppDir(getActivity()) + "/backup");
        String fileName = createBackupFileName();
        File backupFile = new File(backupDir, fileName);
        boolean success = false;
        if(!backupFile.exists()){
            try {
                success = backupFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!success){
                Toast.makeText(getActivity(),"Failed to create the backup file",Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            success = backupFile.delete();
            if(!success){
                Toast.makeText(getActivity(),"Failed to delete old file",Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                success = backupFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!success){
                Toast.makeText(getActivity(),"Failed to create the backup file",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        long starTime = System.currentTimeMillis();
        if(!writeCSV(backupFile,vitalsEntities)){
            Toast.makeText(getActivity(),"No Entries found!",Toast.LENGTH_SHORT).show();
            return;
        }
        long endTime = System.currentTimeMillis();
        Log.e("Campaign Fragment: -------->", "Creating backup took " + (endTime - starTime) + "ms.");
        Toast.makeText(getActivity(),"Export file generated successfully!",Toast.LENGTH_SHORT).show();

        Uri path = FileProvider.getUriForFile(
                getActivity(),
                "pk.mohammadadnan.senahealth.provider", //(use your app signature + ".provider" )
                backupFile);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent .setType("vnd.android.cursor.dir/email");
        emailIntent .putExtra(Intent.EXTRA_STREAM, path);
        startActivity(Intent.createChooser(emailIntent , "Export to..."));
    }

    private boolean writeCSV(File file,List<VitalsEntity> vitalsEntities){
        boolean attendeesFound = false;
        ArrayList<VitalsEntity> MyVitals = new ArrayList<>(vitalsEntities);
        try {
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            csvWrite.writeNext(new String[]{
                    "Date & Time",
                    "Measurement Type",
                    "Measurement 1",
                    "Measurement 2",
                    "Measurement 3"
            });

            for(VitalsEntity vitalsEntity:MyVitals){
                attendeesFound = true;

                String measurementType = "";
                String measurementOne = "";
                String measurementTwo = "";
                String measurementThree = "";
                switch (vitalsEntity.getMeasurementType()){
                    case 1:
                        measurementType = "Blood Glucose";
                        measurementOne = String.format("%.0f",vitalsEntity.getMeasurementOne()) + "mg/dL";
                        measurementTwo = "";
                        measurementThree = "";
                        break;
                    case 2:
                        measurementType = "Blood Pressure";
                        measurementOne = String.format("%.0f",vitalsEntity.getMeasurementOne()) + "mmHg";
                        measurementTwo = String.format("%.0f",vitalsEntity.getMeasurementTwo()) + "mmHg";
                        measurementThree = String.format("%.0f",vitalsEntity.getMeasurementThree()) + "bpm";
                        break;
                    case 3:
                        measurementType = "Temperature";
                        measurementOne = String.format("%.1f",vitalsEntity.getMeasurementOne()) + "F";
                        measurementTwo = "";
                        measurementThree = "";
                        break;
                    case 4:
                        measurementType = "Weight";
                        measurementOne = String.format("%.1f",vitalsEntity.getMeasurementOne()) + "lb";
                        measurementTwo = "";
                        measurementThree = "";
                        break;
                    case 5:
                        measurementType = "Oxygen Saturation";
                        measurementOne = String.format("%.0f",vitalsEntity.getMeasurementOne()) + "%";
                        measurementTwo = "";
                        measurementThree = "";
                        break;
                }
                String[] arrStr = {
                        DateUtils.getString(vitalsEntity.getTime()),
                        measurementType,
                        measurementOne,
                        measurementTwo,
                        measurementThree
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
        } catch (IOException sqlEx) {
            sqlEx.printStackTrace();
            Log.e("Export Data", " Export not working ");
        }
        return attendeesFound;
    }

    private static String createBackupFileName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        return "db_backup_" + sdf.format(new Date()) + ".csv";
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.senadark));
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(0);
    }

}