package pk.mohammadadnan.senahealth.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pk.mohammadadnan.senahealth.MainActivity;
import pk.mohammadadnan.senahealth.R;
import pk.mohammadadnan.senahealth.models.UserIdObjects;
import pk.mohammadadnan.senahealth.models.UserObjects;
import pk.mohammadadnan.senahealth.requests.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xdroid.toaster.Toaster;

import static xdroid.core.Global.getContext;


public class LoginFragment extends FragmentActivity {

    private EditText email;
    private EditText password;
    private Button loginButton;

    private UserObjects UserResponse;
    private String UserId;

    private SharedPreferences prefs;

    final String PREFS_FILE = "MyPrefsFile";

    final String PREF_LOGIN = "is_logged_in";
    final boolean DOESNT_EXIST_LOGIN = false;

    final String PREF_EMAIL = "my_email";
    final String PREF_SHA256_PASSWORD = "my_password";
    final String PREF_ID = "my_id";
    final String PREF_NAME = "my_name";
    final String PREF_AVATAR = "my_avatar";
    final String DOESNT_EXIST_STRING = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        email = findViewById(R.id.email_login);
        password = findViewById(R.id.password_login);
        loginButton = findViewById(R.id.button_login);

        prefs = this.getApplicationContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        if(prefs.getBoolean(PREF_LOGIN,DOESNT_EXIST_LOGIN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

            finish();
            return;
        }

        getUsers();
        getUserId("adnan490480@yahoo.com");

        loginButton.setOnClickListener(view -> {
            if(UserResponse == null){
                getUsers();
                Toast.makeText(getApplicationContext(),"Check your connectivity and try again",Toast.LENGTH_SHORT).show();
                return;
            }

            String emailEntered = email.getText().toString().trim();
            String sha256PasswordEntered = org.apache.commons.codec.digest.DigestUtils.sha256Hex(password.getText().toString());

            for(UserObjects.Response.Results results:UserResponse.getResponse().getResults()){
                String emailAddress = (results.getAuthentication().getEmailObject().getEmailAddress() == null)?"":results.getAuthentication().getEmailObject().getEmailAddress();
                String sha256 = (results.getSha256() == null)?"":results.getSha256();

                String firstName = (results.getFirstName() == null)?"":results.getFirstName()+" ";
                String lastName = (results.getLastName() == null)?"":results.getLastName();
                String name = firstName + lastName;

                if(emailAddress.equals(emailEntered) && sha256.equals(sha256PasswordEntered)){
                    prefs.edit().putBoolean(PREF_LOGIN,true).apply();
                    prefs.edit().putString(PREF_EMAIL,emailEntered).apply();
                    prefs.edit().putString(PREF_SHA256_PASSWORD,sha256PasswordEntered).apply();
                    prefs.edit().putString(PREF_ID,results.get_id()).apply();
                    prefs.edit().putString(PREF_NAME,name).apply();
                    prefs.edit().putString(PREF_AVATAR,results.getAvatar()).apply();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                    finish();
                    return;
                }

            }
            Toast.makeText(getApplicationContext(),"Incorrect Credentials!",Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void getUsers(){
        Call<UserObjects> users = ServiceGenerator.getSenaHealthApi().getUsers();
        users.enqueue(new Callback<UserObjects>() {
            @Override
            public void onResponse(Call<UserObjects> call, Response<UserObjects> response) {
                if (!response.isSuccessful()) {
//                    Toast.makeText(getApplicationContext(),"Unsuccessful Response code: "+response.code(),Toast.LENGTH_SHORT).show();
                    UserResponse = null;
                    return;
                }
                UserResponse = response.body();

            }
            @Override
            public void onFailure(Call<UserObjects> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                UserResponse = null;
            }
        });
    }

    private void getUserId(String email){
        List<List<String>> query = new ArrayList<>();
        List<String> queryItem = new ArrayList<>();
        queryItem.add("email");
        queryItem.add("==");
        queryItem.add(email);
        query.add(queryItem);
        UserIdObjects userIdObject = new UserIdObjects("User", query);
        Call<String> userId = ServiceGenerator.getSenaHealthApi().postUserId(userIdObject);
        userId.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) {
                    UserId = null;
                    Toaster.toast("Message:"+response.message());
                    return;
                }
                UserId = response.body();
                Toaster.toast("ID:"+UserId);
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
                UserId = null;
            }
        });
    }
}