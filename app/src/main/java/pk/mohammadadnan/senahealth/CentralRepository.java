package pk.mohammadadnan.senahealth;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import pk.mohammadadnan.senahealth.database.CentralDatabase;
import pk.mohammadadnan.senahealth.database.dao.VitalsDao;
import pk.mohammadadnan.senahealth.database.entity.VitalsEntity;

public class CentralRepository {

    private VitalsDao vitalsDao;

    public CentralRepository(Application application) {
        CentralDatabase db = CentralDatabase.getInstance(application);
        vitalsDao = db.vitalsDao();
    }

    public void insertVital(final VitalsEntity vitalsEntity){
        new Thread(() -> vitalsDao.insert(vitalsEntity)).start();
    }

    public void updateVital(final VitalsEntity vitalsEntity){
        new Thread(() -> vitalsDao.update(vitalsEntity)).start();
    }

    public void deleteVital(final VitalsEntity vitalsEntity){
        new Thread(() -> vitalsDao.delete(vitalsEntity)).start();
    }

    public LiveData<List<VitalsEntity>> getAllVitals(){
        return vitalsDao.getVitals();
    }

    public LiveData<List<VitalsEntity>> getAllVitals(int measurementType){
        return vitalsDao.getVitals(measurementType);
    }


    public List<VitalsEntity> getAllVitalsInit(){
        return vitalsDao.getAllVitalssInit();
    }

    public List<VitalsEntity> getAllVitalsInit(int measurementType){
        return vitalsDao.getAllVitalsInit(measurementType);
    }

}
