package pk.mohammadadnan.senahealth;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;
import org.acra.annotation.AcraToast;
import org.acra.data.StringFormat;

import pk.mohammadadnan.senahealth.BuildConfig;
import pk.mohammadadnan.senahealth.R;

@AcraCore(buildConfigClass = BuildConfig.class,
        reportFormat = StringFormat.JSON)
@AcraMailSender(mailTo = "adnanfreelancerr@gmail.com",
        resSubject = R.string.app_name,
        reportAsFile = true)
@AcraToast(resText = R.string.app_name)
public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
