package pk.mohammadadnan.senahealth.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import pk.mohammadadnan.senahealth.CentralRepository;
import pk.mohammadadnan.senahealth.database.entity.VitalsEntity;

public class VitalsViewModel extends AndroidViewModel {

    private CentralRepository mRepo;
    private LiveData<List<VitalsEntity>> allVitals;

    public VitalsViewModel(Application application) {
        super(application);
        mRepo = new CentralRepository(application);
        allVitals = mRepo.getAllVitals();
    }

    public LiveData<List<VitalsEntity>> getAllVitals() {
        return allVitals;
    }
    public LiveData<List<VitalsEntity>> getAllVitals(int measurementType) {
        return mRepo.getAllVitals(measurementType);
    }
    public void insert(VitalsEntity vitalsEntity){
        mRepo.insertVital(vitalsEntity);
    }
    public void update(VitalsEntity vitalsEntity){
        mRepo.updateVital(vitalsEntity);
    }
    public void delete(VitalsEntity vitalsEntity){
        mRepo.deleteVital(vitalsEntity);
    }
}