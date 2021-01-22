package pk.mohammadadnan.senahealth.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.taidoc.pclinklibrary.android.bluetooth.util.BluetoothUtil;
import com.taidoc.pclinklibrary.connection.AndroidBluetoothConnection;
import com.taidoc.pclinklibrary.connection.util.ConnectionManager;
import com.taidoc.pclinklibrary.constant.PCLinkLibraryConstant;
import com.taidoc.pclinklibrary.constant.PCLinkLibraryEnum;
import com.taidoc.pclinklibrary.exceptions.CommunicationTimeoutException;
import com.taidoc.pclinklibrary.exceptions.ExceedRetryTimesException;
import com.taidoc.pclinklibrary.exceptions.NotConnectSerialPortException;
import com.taidoc.pclinklibrary.exceptions.NotSupportMeterException;
import com.taidoc.pclinklibrary.interfaces.BleUtilsListener;
import com.taidoc.pclinklibrary.meter.AbstractMeter;
import com.taidoc.pclinklibrary.meter.record.AbstractRecord;
import com.taidoc.pclinklibrary.meter.record.BloodGlucoseRecord;
import com.taidoc.pclinklibrary.meter.record.BloodPressureRecord;
import com.taidoc.pclinklibrary.meter.record.SpO2Record;
import com.taidoc.pclinklibrary.meter.record.TemperatureRecord;
import com.taidoc.pclinklibrary.meter.record.WeightScaleRecord;
import com.taidoc.pclinklibrary.meter.util.MeterManager;
import com.taidoc.pclinklibrary.util.BleUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pk.mohammadadnan.senahealth.R;
import pk.mohammadadnan.senahealth.database.entity.VitalsEntity;
import pk.mohammadadnan.senahealth.ui.viewmodels.VitalsViewModel;

public class AddFragment extends Fragment {

    private VitalsViewModel vitalsViewModel;

    /*
     **Manual Fields
     */
    private ImageView glucoseImage;
    private ImageView pressureImage;
    private ImageView tempImage;
    private ImageView weightImage;
    private ImageView oxygenImage;
    private TextView glucoseText;
    private TextView pressureText;
    private TextView tempText;
    private TextView weightText;
    private TextView oxygenText;

    private EditText measOne;
    private EditText measTwo;
    private EditText measThree;
    private TextView titleOne;
    private TextView titleTwo;
    private TextView titleThree;
    private TextView unitOne;
    private TextView unitTwo;
    private TextView unitThree;
    private Button save;

    private int selectedMeasurementType = 1;
    private boolean isSaved = false;

    /*
    **Bluetooth Fields
     */
    private TextView btText;
    private Spinner btSpinner;
    private Button btScan;
    private Button btRead;
    
    private long time;
    private int measurementType;
    private float measureOne,measureTwo,measureThree;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final String TAG = AddFragment.class.getSimpleName();

    public boolean isSearching = false;

    public int needRequestPermission;

    private List<String> remoteDeviceNameList;
    private ArrayAdapter<String> selectedMeterSpinnerAdapter;

    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter mAdapter;
    private BleUtils mBleUtils;

    private BleUtilsListener mBleUtilsListener = new BleUtilsListener() {
        @Override
        public void onScanned(BluetoothDevice device, int rssi) {
            if (device != null && !TextUtils.isEmpty(device.getName())) {
                for(String nameAndAddress:remoteDeviceNameList){
                    if(nameAndAddress.split("/")[1].equals(device.getAddress())){
                        return;
                    }
                }
                remoteDeviceNameList.add(device.getName()+"/"+device.getAddress());
                selectedMeterSpinnerAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onLost(BluetoothDevice device) {
            if (device != null && !TextUtils.isEmpty(device.getName())) {
                remoteDeviceNameList.remove(device.getName()+"/"+device.getAddress());
                selectedMeterSpinnerAdapter.notifyDataSetChanged();
            }
        }
    };

    // Listeners
    private AdapterView.OnItemSelectedListener mSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
        }
    };

    private Button.OnClickListener mReadOnClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (btSpinner.getSelectedItem() != null) {
                mMacAddress = btSpinner.getSelectedItem().toString().split("/")[1];
                if ("".equals(mMacAddress)) {
                    if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                        setupAndroidBluetoothConnection();
                        connectMeter();
                    } else {
                        showAlertDialog(R.string.pair_meter_first);
                    }
                } else if (mTaiDocMeter == null) {
                    setupAndroidBluetoothConnection();
                    connectMeter();
                } else{
                    mTaiDocMeter = null;
                    setupAndroidBluetoothConnection();
                    connectMeter();
                }
            } else {
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.bluetooth_need_to_pair)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {}).show();
            }
        }
    };

    private Button.OnClickListener mScanOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!isSearching){
                btText.setText("Click on the box below and select your device. Rescan if device is not shown. Once selected, click read.");
                btSpinner.setVisibility(View.VISIBLE);
                btRead.setVisibility(View.VISIBLE);
                mProcessDialog = ProgressDialog.show(getContext(), null,
                        getString(R.string.search_button), true);
                mProcessDialog.setCancelable(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        processScanDeviceTimeout();
                        dimissProcessDialog();
                        isSearching = false;
                        if(btSpinner.getSelectedItem() == null){
                            new AlertDialog.Builder(getContext())
                                    .setMessage(R.string.bluetooth_need_to_pair)
                                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {}).show();
                        }
                    }
                }, SCAN_PERIOD);

                if (mBleUtils != null) {
                    mBleUtils.scanLeDevice(true);
                }
                isSearching = true;
            }else {
                processScanDeviceTimeout();
                isSearching = false;
            }

        }
    };


    // Message types sent from the meterCommuHandler Handler
    public static final int MESSAGE_STATE_CONNECTING = 1;
    public static final int MESSAGE_STATE_CONNECT_FAIL = 2;
    public static final int MESSAGE_STATE_CONNECT_DONE = 3;
    public static final int MESSAGE_STATE_CONNECT_NONE = 4;
    public static final int MESSAGE_STATE_CONNECT_METER_SUCCESS = 5;
    public static final int MESSAGE_STATE_CHECK_METER_BT_DISTENCE = 7;
    public static final int MESSAGE_STATE_CHECK_METER_BT_DISTENCE_FAIL = 8;
    public static final int MESSAGE_STATE_NOT_SUPPORT_METER = 9;
    public static final int MESSAGE_STATE_NOT_CONNECT_SERIAL_PORT = 10;
    public static final int MESSAGE_STATE_SCANED_DEVICE = 11;

    // Tag and Debug flag
    private static final boolean DEBUG = true;

    // Views
    private ProgressDialog mProcessDialog = null;

    private AndroidBluetoothConnection.LeConnectedListener mLeConnectedListener = new AndroidBluetoothConnection.LeConnectedListener() {

        @Override
        public void onConnectionTimeout() {
            disconnectMeter();
            dimissProcessDialog();
            showAlertDialog(R.string.timeout);
        }

        @Override
        public void onConnectionStateChange_Disconnect(BluetoothGatt gatt,
                                                       int status, int newState) {
            disconnectMeter();
            dimissProcessDialog();
            showAlertDialog(R.string.not_support_meter);
        }

        @SuppressLint("NewApi")
        @Override
        public void onDescriptorWrite_Complete(BluetoothGatt gatt,
                                               BluetoothGattDescriptor descriptor, int status) {
            mConnection.LeConnected(gatt.getDevice());
        }

        @Override
        public void onCharacteristicChanged_Notify(BluetoothGatt gatt,
                                                   BluetoothGattCharacteristic characteristic) {
            new Thread(() -> {
                Looper.prepare();

                try {
                    mTaiDocMeter = MeterManager.detectConnectedMeter(mConnection);
                } catch (Exception e) {
                    meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_NOT_SUPPORT_METER);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dimissProcessDialog();
                        if (mTaiDocMeter == null) {
                            meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_NOT_SUPPORT_METER);
                        }
                        getMeasurement();
                    }
                });

                Looper.loop();
            }).start();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // TODO Auto-generated method stub

        }
    };

    // Handlers
    // The Handler that gets information back from the android bluetooth connection
    private final Handler mBTConnectionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case PCLinkLibraryConstant.MESSAGE_STATE_CHANGE:
                        if (DEBUG) {
                            Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                        } /* end of if */
                        switch (msg.arg1) {
                            case AndroidBluetoothConnection.STATE_CONNECTED_BY_LISTEN_MODE:
                                try {
                                    mTaiDocMeter = MeterManager.detectConnectedMeter(mConnection);
                                } catch (Exception e) {
                                    throw new NotSupportMeterException();
                                }
                                dimissProcessDialog();
                                if (mTaiDocMeter == null) {
                                    throw new NotSupportMeterException();
                                }/* end of if */
                                break;
                            case AndroidBluetoothConnection.STATE_CONNECTING:
                                // 暫無需特別處理的事項
                                break;
                            case AndroidBluetoothConnection.STATE_SCANED_DEVICE:
                                meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_SCANED_DEVICE);
                                break;
                            case AndroidBluetoothConnection.STATE_LISTEN:
                                // 暫無需特別處理的事項
                                break;
                            case AndroidBluetoothConnection.STATE_NONE:
                        } /* end of switch */
                        break;
                    case PCLinkLibraryConstant.MESSAGE_TOAST:
                        // 暫無需特別處理的事項
                        break;
                    default:
                        break;
                } /* end of switch */
            } catch (NotSupportMeterException e) {
                Log.e(TAG, "not support meter", e);
                showAlertDialog(R.string.not_support_meter);
            } /* end of try-catch */
        }
    };

    private AndroidBluetoothConnection mConnection;

    private final Handler meterCommuHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CONNECTING:
                    mProcessDialog = ProgressDialog.show(getContext(), null,
                            getString(R.string.connection_meter_and_get_result), true);
                    mProcessDialog.setCancelable(false);
                    break;
                case MESSAGE_STATE_SCANED_DEVICE:
                    // 取得Bluetooth Device資訊
                    final BluetoothDevice device = BluetoothUtil.getPairedDevice(mConnection.getConnectedDeviceAddress());
                    // Attempt to connect to the device
                    mConnection.LeConnect(getContext(), device);
                    // 在mLeConnectedListener會收
                    break;
                case MESSAGE_STATE_CONNECT_DONE:
                    dimissProcessDialog();
                    break;
                case MESSAGE_STATE_CONNECT_FAIL:
                    dimissProcessDialog();
                    showAlertDialog(R.string.connect_meter_fail);
                    break;
                case MESSAGE_STATE_CONNECT_NONE:
                    dimissProcessDialog();
                    break;
                case MESSAGE_STATE_CONNECT_METER_SUCCESS:
                    showAlertDialog(R.string.connect_meter_success);
                    break;
                case MESSAGE_STATE_CHECK_METER_BT_DISTENCE:
                    ProgressDialog baCmdDialog = new ProgressDialog(
                            getContext());
                    baCmdDialog.setCancelable(false);
                    baCmdDialog.setMessage("send ba command");
                    baCmdDialog.setButton(DialogInterface.BUTTON_POSITIVE, "cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Use either finish() or return() to either close the activity
                                    // or just
                                    // the dialog
                                    dialog.dismiss();
                                    return;
                                }
                            });
                    baCmdDialog.show();
                    break;
                case MESSAGE_STATE_CHECK_METER_BT_DISTENCE_FAIL:
                    showAlertDialog(R.string.check_bt_fail);
                    break;
                case MESSAGE_STATE_NOT_SUPPORT_METER:
                    dimissProcessDialog();
                    showAlertDialog(R.string.not_support_meter);
                    break;
                case MESSAGE_STATE_NOT_CONNECT_SERIAL_PORT:
                    showAlertDialog(R.string.not_connect_serial_port);
                    break;
            } /* end of switch */
        }
    };

    private String mMacAddress;
    private AbstractMeter mTaiDocMeter = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        vitalsViewModel = new ViewModelProvider(getActivity()).get(VitalsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_add, container, false);

        findViews(root);
        setListeners();

        if (needRequestPermission == 0) {
            needRequestPermission = !checkPermissions() ? 1 : 2;
            if (needRequestPermission == 1) {
                requestPermissions();
            }
        }

        mAdapter = BluetoothUtil.getBluetoothAdapter();
        mBleUtils = new BleUtils(mAdapter, mBleUtilsListener);
        if (mBleUtils != null) {
            mBleUtils.initScanner();
        }

        remoteDeviceNameList = new ArrayList<String>();
        selectedMeterSpinnerAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item,
                remoteDeviceNameList);
        selectedMeterSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        btSpinner.setAdapter(selectedMeterSpinnerAdapter);

        return root;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermissions() {
        // Android M Permission check 
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_COARSE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length == 0) {
            return;
        }
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                for(int i=0; i<permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            if (needRequestPermission == 1) {
                                needRequestPermission = 2;
                            }
                        }
                    }
                }
                return;
        }
    }

    private void processScanDeviceTimeout() {
        if (mBleUtils != null) {
            mBleUtils.scanLeDevice(false);
        }
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

        clearTextManual();
        selectManual(1);

        resetBluetooth();
    }

    @Override
    public void onStop() {
        super.onStop();

        disconnectMeter();
        dimissProcessDialog();
        processScanDeviceTimeout();
    }

    private void findViews(View root){
        glucoseImage = root.findViewById(R.id.glucose_ic_manual);
        pressureImage = root.findViewById(R.id.pressure_ic_manual);
        tempImage = root.findViewById(R.id.temp_ic_manual);
        weightImage = root.findViewById(R.id.weight_ic_manual);
        oxygenImage = root.findViewById(R.id.oxygen_ic_manual);
        glucoseText = root.findViewById(R.id.glucose_tx_manual);
        pressureText = root.findViewById(R.id.pressure_tx_manual);
        tempText = root.findViewById(R.id.temp_tx_manual);
        weightText = root.findViewById(R.id.weight_tx_manual);
        oxygenText = root.findViewById(R.id.oxygen_tx_manual);

        measOne = root.findViewById(R.id.edit_one_manual);
        measTwo = root.findViewById(R.id.edit_two_manual);
        measThree = root.findViewById(R.id.edit_three_manual);
        titleOne = root.findViewById(R.id.title_one_manual);
        titleTwo = root.findViewById(R.id.title_two_manual);
        titleThree = root.findViewById(R.id.title_three_manual);
        unitOne = root.findViewById(R.id.unit_one_manual);
        unitTwo = root.findViewById(R.id.unit_two_manual);
        unitThree = root.findViewById(R.id.unit_three_manual);
        save = root.findViewById(R.id.save_manual);

        btText = root.findViewById(R.id.text_bt);
        btSpinner = root.findViewById(R.id.spinner_bt);
        btScan = root.findViewById(R.id.scan_bt);
        btRead = root.findViewById(R.id.read_bt);
    }
    
    private void setListeners(){
        glucoseImage.setOnClickListener(view -> {
            if(selectedMeasurementType!=1 && !isSaved){
                selectedMeasurementType = 1;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });
        glucoseText.setOnClickListener(view -> {
            if(selectedMeasurementType!=1){
                selectedMeasurementType = 1;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });

        pressureImage.setOnClickListener(view -> {
            if(selectedMeasurementType!=2){
                selectedMeasurementType = 2;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });
        pressureText.setOnClickListener(view -> {
            if(selectedMeasurementType!=2){
                selectedMeasurementType = 2;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });

        tempImage.setOnClickListener(view -> {
            if(selectedMeasurementType!=3){
                selectedMeasurementType = 3;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });
        tempText.setOnClickListener(view -> {
            if(selectedMeasurementType!=3){
                selectedMeasurementType = 3;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });

        weightImage.setOnClickListener(view -> {
            if(selectedMeasurementType!=4){
                selectedMeasurementType = 4;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });
        weightText.setOnClickListener(view -> {
            if(selectedMeasurementType!=4){
                selectedMeasurementType = 4;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });

        oxygenImage.setOnClickListener(view -> {
            if(selectedMeasurementType!=5){
                selectedMeasurementType = 5;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });
        oxygenText.setOnClickListener(view -> {
            if(selectedMeasurementType!=5){
                selectedMeasurementType = 5;
                clearTextManual();
                selectManual(selectedMeasurementType);
            }
        });

        save.setOnClickListener(view -> {
            if(!isSaved){
                if(measOne.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(),"Enter all the values!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(selectedMeasurementType == 2 && ( measThree.getText().toString().isEmpty() || measTwo.getText().toString().isEmpty())){
                    Toast.makeText(getActivity(),"Enter all the values!",Toast.LENGTH_SHORT).show();
                    return;
                }
                String measureTwo = measTwo.getText().toString();
                String measureThree = measThree.getText().toString();
                if(measTwo.getText().toString().isEmpty()){
                    measureTwo = "0";
                }
                if(measThree.getText().toString().isEmpty()){
                    measureThree = "0";
                }
                vitalsViewModel.insert(new VitalsEntity(System.currentTimeMillis(), selectedMeasurementType,Float.parseFloat(measOne.getText().toString()), Float.parseFloat(measureTwo), Float.parseFloat(measureThree), false));
                Toast.makeText(getActivity(),"Value saved!",Toast.LENGTH_SHORT).show();
                savedManual(false);
                isSaved = true;
            }else {
                savedManual(true);
                clearTextManual();
                selectManual(1);
                isSaved = false;
            }
        });

        btRead.setOnClickListener(mReadOnClickListener);
        btSpinner.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);
        btScan.setOnClickListener(mScanOnClickListener);
    }

    private void selectManual(int i){
        resetManual();
        inputStatesManual(i);
        switch (i){
            case 1:
                glucoseImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                glucoseText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                break;
            case 2:
                pressureImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                pressureText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                break;
            case 3:
                tempImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                tempText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                break;
            case 4:
                weightImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                weightText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                break;
            case 5:
                oxygenImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                oxygenText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                break;
        }
    }

    private void clearTextManual(){
        measOne.getText().clear();
        measTwo.getText().clear();
        measThree.getText().clear();
    }

    private void resetManual(){
        glucoseImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.grey_light));
        glucoseText.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_light));
        pressureImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.grey_light));
        pressureText.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_light));
        tempImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.grey_light));
        tempText.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_light));
        weightImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.grey_light));
        weightText.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_light));
        oxygenImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.grey_light));
        oxygenText.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_light));
    }

    private void inputStatesManual(int i){

        measTwo.setVisibility(View.GONE);
        titleTwo.setVisibility(View.GONE);
        unitTwo.setVisibility(View.GONE);

        measThree.setVisibility(View.GONE);
        titleThree.setVisibility(View.GONE);
        unitThree.setVisibility(View.GONE);

        switch (i){
            case 1:
                titleOne.setText("Blood Glucose");
                unitOne.setText("mg/dL");
                measOne.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                break;
            case 2:
                titleOne.setText("Systolic");
                unitOne.setText("mmHg");
                measOne.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                measTwo.setVisibility(View.VISIBLE);
                titleTwo.setVisibility(View.VISIBLE);
                titleTwo.setText("Diastolic");
                unitTwo.setVisibility(View.VISIBLE);
                unitTwo.setText("mmHg");
                measThree.setVisibility(View.VISIBLE);
                titleThree.setVisibility(View.VISIBLE);
                titleThree.setText("Heatbeats");
                unitThree.setVisibility(View.VISIBLE);
                unitThree.setText("bpm");
                break;
            case 3:
                titleOne.setText("Temperature");
                unitOne.setText("°F");
                measOne.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case 4:
                titleOne.setText("Weight");
                unitOne.setText("lb");
                measOne.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case 5:
                titleOne.setText("Oxygen Saturation");
                unitOne.setText("%");
                measOne.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                break;
        }
    }

    private void savedManual(boolean reset){
        if(!reset){

            glucoseText.setVisibility(View.GONE);
            pressureImage.setVisibility(View.GONE);
            pressureText.setVisibility(View.GONE);
            tempImage.setVisibility(View.GONE);
            tempText.setVisibility(View.GONE);
            weightImage.setVisibility(View.GONE);
            weightText.setVisibility(View.GONE);
            oxygenImage.setVisibility(View.GONE);
            oxygenText.setVisibility(View.GONE);
            measOne.setVisibility(View.GONE);
            titleOne.setVisibility(View.GONE);
            unitOne.setVisibility(View.GONE);
            measTwo.setVisibility(View.GONE);
            titleTwo.setVisibility(View.GONE);
            unitTwo.setVisibility(View.GONE);
            measThree.setVisibility(View.GONE);
            titleThree.setVisibility(View.GONE);
            unitThree.setVisibility(View.GONE);

            glucoseImage.setImageResource(R.drawable.checked);
            glucoseImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.green));
            save.setText("Add another log");
        }else{

            glucoseText.setVisibility(View.VISIBLE);
            pressureImage.setVisibility(View.VISIBLE);
            pressureText.setVisibility(View.VISIBLE);
            tempImage.setVisibility(View.VISIBLE);
            tempText.setVisibility(View.VISIBLE);
            weightImage.setVisibility(View.VISIBLE);
            weightText.setVisibility(View.VISIBLE);
            oxygenImage.setVisibility(View.VISIBLE);
            oxygenText.setVisibility(View.VISIBLE);
            measOne.setVisibility(View.VISIBLE);
            titleOne.setVisibility(View.VISIBLE);
            unitOne.setVisibility(View.VISIBLE);

            glucoseImage.setImageResource(R.drawable.meas_oxygen_circle);
            save.setText("Save");
        }

    }

    private void resetBluetooth(){
        btText.setText("Click on scan to look for your bluetooth device");
        btSpinner.setVisibility(View.GONE);
        btRead.setVisibility(View.GONE);
    }

    private void updatePairedList() {
        Map<String, String> addrs = new HashMap<String, String>();
        String addrKey = "BLE_PAIRED_METER_ADDR_" + String.valueOf(0);
        addrs.put(addrKey, mMacAddress);
        mConnection.updatePairedList(addrs, 1);
    }

    private void connectMeter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_CONNECTING);
                    updatePairedList();
                    mConnection.setLeConnectedListener(mLeConnectedListener);

                    if (mConnection.getState() == AndroidBluetoothConnection.STATE_NONE) {
                        // Start the Android Bluetooth connection services to listen mode
                        mConnection.LeListen();

                        if (DEBUG) {
                            Log.i(TAG, "into listen mode");
                        }
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mConnection.getState() == AndroidBluetoothConnection.STATE_LISTEN) {
                                if (mLeConnectedListener != null) {
                                    mLeConnectedListener.onConnectionTimeout();
                                }
                            }
                        }
                    }, 10000);
                } catch (CommunicationTimeoutException e) {
                    meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_CONNECT_FAIL);
                } catch (NotSupportMeterException | ExceedRetryTimesException e) {
                    meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_NOT_SUPPORT_METER);
                } catch (NotConnectSerialPortException e) {
                    meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_NOT_CONNECT_SERIAL_PORT);
                }
                Looper.loop();
            }
        }).start();
    }

    private void dimissProcessDialog() {
        if (mProcessDialog != null) {
            mProcessDialog.dismiss();
            mProcessDialog = null;
        } /* end of if */
    }

    private void disconnectMeter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    if(mConnection != null){
                        mConnection.setLeConnectedListener(null);
                        mConnection.LeDisconnect();
                    }
                    if (mTaiDocMeter != null) {
                        mTaiDocMeter.turnOffMeterOrBluetooth(0);
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                Looper.loop();
            }
        }).start();
    }

    private void setupAndroidBluetoothConnection() {
        if (mConnection == null) {
            try {
                mConnection = ConnectionManager.createAndroidBluetoothConnection(mBTConnectionHandler);
                mConnection.canScanV3KNV(false);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void showAlertDialog(int messageConntentRStringId) {

        new AlertDialog.Builder(getContext())
                .setMessage(messageConntentRStringId)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
    }

    private void getMeasurement(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_CONNECTING);

                    // Get Latest Measurement Record
                    AbstractRecord record = mTaiDocMeter.getStorageDataRecord(0,
                            PCLinkLibraryEnum.User.CurrentUser);
                    getLatestRecord(record);

                    if(time == 0){
                        Toast.makeText(getContext(),"No Reading Found!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    vitalsViewModel.insert(new VitalsEntity(time,measurementType,measureOne,measureTwo,measureThree,false));
                    Toast.makeText(getContext(),"Reading Saved!",Toast.LENGTH_SHORT).show();
                    getActivity().runOnUiThread(() -> {
                        resetBluetooth();
                    });
                    disconnectMeter();
                } catch (Exception e) {
                    disconnectMeter();
                    meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_CONNECT_FAIL);
                } finally {
                    meterCommuHandler.sendEmptyMessage(MESSAGE_STATE_CONNECT_DONE);
                } /* end of try-catch-finally */
                Looper.loop();
            }
        }).start();
    }

    private void getLatestRecord(AbstractRecord record) {
        time = 0;
        measurementType = 0;
        measureOne = 0;
        measureTwo = 0;
        measureThree = 0;
        // Convert value and set views
        if (record == null) {
            return;
        }
        // Convert value and set views
        if (record instanceof BloodPressureRecord) {
            float sysValue = ((BloodPressureRecord) record)
                    .getSystolicValue();
            float diaValue = ((BloodPressureRecord) record)
                    .getDiastolicValue();
            float pulseValue = ((BloodPressureRecord) record).getPulseValue();
            Date date = ((BloodPressureRecord) record).getMeasureTime();

            time = date.getTime();
            measurementType = 2;
            measureOne = sysValue;
            measureTwo = diaValue;
            measureThree = pulseValue;

        } else if (record instanceof BloodGlucoseRecord) {
            // convert value
            float bgValue = ((BloodGlucoseRecord) record).getGlucoseValue();
            Date date = ((BloodGlucoseRecord) record).getMeasureTime();

            time = date.getTime();
            measurementType = 1;
            measureOne = bgValue;
            measureTwo = 0;
            measureThree = 0;
        } else if (record instanceof TemperatureRecord) {
            // Convert value
            float thermometerValue = (float)((((TemperatureRecord) record).getObjectTemperatureValue() * 1.8) + 32);
            Date date = ((TemperatureRecord) record).getMeasureTime();

            time = date.getTime();
            measurementType = 3;
            measureOne = thermometerValue;
            measureTwo = 0;
            measureThree = 0;
        } else if (record instanceof SpO2Record) {
            int spO2Value = ((SpO2Record) record).getSpO2();
            Date date = ((SpO2Record) record).getMeasureTime();

            time = date.getTime();
            measurementType = 5;
            measureOne = (float) spO2Value;
            measureTwo = 0;
            measureThree = 0;
        } else if (record instanceof WeightScaleRecord) {
            // Convert value
            float weight = (float) (((WeightScaleRecord) record).getWeight() * 2.20462F);
            Date date = ((WeightScaleRecord) record).getMeasureTime();

            time = date.getTime();
            measurementType = 4;
            measureOne = weight;
            measureTwo = 0;
            measureThree = 0;
        }
    }
}