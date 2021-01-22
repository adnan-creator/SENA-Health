package pk.mohammadadnan.senahealth.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.koushikdutta.ion.Ion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pk.mohammadadnan.senahealth.SenaHealthApi;
import pk.mohammadadnan.senahealth.database.entity.VitalsEntity;
import pk.mohammadadnan.senahealth.models.MoodObjects;
import pk.mohammadadnan.senahealth.ui.adaptors.VitalsRecyclerAdapter;
import pk.mohammadadnan.senahealth.ui.viewmodels.VitalsViewModel;
import pk.mohammadadnan.senahealth.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xdroid.toaster.Toaster;

public class HomeFragment extends Fragment {

    private VitalsViewModel vitalsViewModel;

    private TextView name;
    private ImageView avatar;

    private CardView happinessCard;
    private TextView question;
    private TextView score;
    private SeekBar seekBar;
    private Button button;
    private ImageView sad;
    private ImageView happy;

    private RecyclerView recyclerView;
    private VitalsRecyclerAdapter adapter;

    private ConstraintLayout emptyLayout;

    private SenaHealthApi senaHealthApi;

    private String formattedDate;
    private boolean isSecondPage = false;
    private int moodToday,moodNormally;

    private SharedPreferences prefs;

    final String PREFS_FILE = "MyPrefsFile";

    final String PREF_NAME = "my_name";
    final String PREF_AVATAR = "my_avatar";
    final String PREF_ID = "my_id";
    final String PREF_DATE_SAVED = "my_date";

    final String DOESNT_EXIST_NAME = "There";
    final String DOESNT_EXIST_STRING = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        vitalsViewModel = new ViewModelProvider(getActivity()).get(VitalsViewModel.class);
        prefs = getActivity().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        name = root.findViewById(R.id.name_home);
        avatar = root.findViewById(R.id.avatar_home);

        happinessCard = root.findViewById(R.id.card_happiness);
        question = root.findViewById(R.id.question_happiness);
        score = root.findViewById(R.id.score_happiness);
        seekBar = root.findViewById(R.id.seekBar_happiness);
        button = root.findViewById(R.id.button_happiness);
        sad = root.findViewById(R.id.sad_happiness);
        happy = root.findViewById(R.id.happy_happiness);

        recyclerView = root.findViewById(R.id.list_home);

        emptyLayout = root.findViewById(R.id.empty_home);

        String nameString = prefs.getString(PREF_NAME, DOESNT_EXIST_NAME);
        if(nameString.contains(" ")) {
            String[] parts= nameString.split(" ");
            nameString = parts[0];
        }
        name.setText("Hi "+nameString+"!");

        String url = prefs.getString(PREF_AVATAR, DOESNT_EXIST_STRING);
        if(!url.equals("")){
            Ion.with(avatar)
                    // use a placeholder google_image if it needs to load from the network
                    .placeholder(R.drawable.loading)
                    // load the url
                    .load("http:"+url);
        }

        formattedDate = getStringDate();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sena.health/api/1.1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        senaHealthApi = retrofit.create(SenaHealthApi.class);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                score.setText(String.valueOf(seekBar.getProgress()+1));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        button.setOnClickListener(view -> {
            if(!isSecondPage){
                moodToday = seekBar.getProgress()+1;
                question.setText("How do you normally feel?");
                seekBar.setProgress(9);
                button.setText("SAVE");
                isSecondPage = true;
            }else{
                moodNormally = seekBar.getProgress()+1;
                postMood(moodToday,moodNormally);
            }
        });

        vitalsViewModel.getAllVitals().observe(getViewLifecycleOwner(), allvitals -> {
            List<VitalsEntity> recentVitals = new ArrayList<>();
            if(allvitals.size() == 0){emptyLayout.setVisibility(View.VISIBLE);return;}
            recentVitals.add(allvitals.get(0));
            List<Integer> existingValueTypes = new ArrayList<>();
            existingValueTypes.add(allvitals.get(0).getMeasurementType());
            for(VitalsEntity vitalsEntity: allvitals){
                boolean skip = false;
                for(Integer integer:existingValueTypes){
                    if (vitalsEntity.getMeasurementType() == integer) {skip = true;break;}
                }
                if(!skip){
                    recentVitals.add(vitalsEntity);
                    existingValueTypes.add(vitalsEntity.getMeasurementType());
                }
                if(existingValueTypes.size() == 5)break;
            }
            if(recentVitals.size() != 0)emptyLayout.setVisibility(View.GONE);
            adapter = new VitalsRecyclerAdapter(getActivity(), recentVitals, vitalsViewModel,true);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setNestedScrollingEnabled(false);
        });

        return root;
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
        if(
                !prefs.getString(PREF_DATE_SAVED,DOESNT_EXIST_STRING).equals("") &&
                        prefs.getString(PREF_DATE_SAVED,DOESNT_EXIST_STRING).equals(formattedDate))
        {
            happinessCard.setVisibility(View.GONE);
        }else{
            resetHappiness();
            happinessCard.setVisibility(View.VISIBLE);
        }
    }

    private void postMood(int moodToday, int moodNormally) {

        MoodObjects mood = new MoodObjects(
                System.currentTimeMillis(),
                moodToday,
                moodNormally,
                prefs.getString(PREF_ID,DOESNT_EXIST_STRING)
        );

        Call<MoodObjects> call = senaHealthApi.postMood(mood);
        call.enqueue(new Callback<MoodObjects>() {
            @Override
            public void onResponse(Call<MoodObjects> call, Response<MoodObjects> response) {
                if (!response.isSuccessful()) {
                    Log.e("UNSUCCESSFUL CODE: --------> ",response.code()+"");
                    Toaster.toast("Check connection save again!");
                }else {
                    Log.e("SUCCESSFUL CODE: --------> ",response.code()+"");
                    happinessCard.setVisibility(View.VISIBLE);
                    savedHappiness();
                    prefs.edit().putString(PREF_DATE_SAVED,formattedDate).apply();
                }
            }
            @Override
            public void onFailure(Call<MoodObjects> call, Throwable t) {
                Log.e("FAILURE CODE: --------> ",t.getMessage());
                Toaster.toast("Check connection save again!");
            }
        });
    }

    private String getStringDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        return df.format(c);
    }

    private void resetHappiness(){
        question.setText("How are you feeling today?");
        sad.setVisibility(View.VISIBLE);
        happy.setVisibility(View.VISIBLE);
        score.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        seekBar.setProgress(9);
        button.setText("NEXT");
        isSecondPage = false;
    }

    private void savedHappiness(){
        question.setText("Thanks for your feedback!");
        sad.setVisibility(View.GONE);
        happy.setVisibility(View.GONE);
        score.setVisibility(View.GONE);
        seekBar.setVisibility(View.GONE);
        button.setVisibility(View.GONE);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                happinessCard.setVisibility(View.GONE);
                resetHappiness();
            }
        },5000);
    }
}