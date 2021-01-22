package pk.mohammadadnan.senahealth.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import pk.mohammadadnan.senahealth.MainActivity;
import pk.mohammadadnan.senahealth.R;
import pk.mohammadadnan.senahealth.ui.adaptors.VitalsRecyclerAdapter;
import pk.mohammadadnan.senahealth.ui.viewmodels.VitalsViewModel;

public class VitalsFragment extends Fragment {

    private VitalsViewModel vitalsViewModel;

    private RecyclerView recyclerView;
    private VitalsRecyclerAdapter adapter;
    private TextView title;
    private ImageView close;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vitalsViewModel = new ViewModelProvider(getActivity()).get(VitalsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_vitals, container, false);

        recyclerView = root.findViewById(R.id.recycler_vitals);
        title = root.findViewById(R.id.title_vitals);
        close = root.findViewById(R.id.close_vitals);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).popBackStack();
            }
        });

        if (getArguments() != null) {
            switch (getArguments().getInt("measurement_type")){
                case 1:title.setText("Blood Glucose logs");break;
                case 2:title.setText("Blood Pressure logs");break;
                case 3:title.setText("Temperature logs");break;
                case 4:title.setText("Weight logs");break;
                case 5:title.setText("Oxygen Saturation logs");break;
                default:title.setText("All logs");break;
            }
            vitalsViewModel.getAllVitals(getArguments().getInt("measurement_type")).observe(getViewLifecycleOwner(), allvitals -> {
                if(allvitals.size() == 0){Navigation.findNavController(root).popBackStack();}
                adapter = new VitalsRecyclerAdapter(getActivity(), allvitals, vitalsViewModel,false);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            });
        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.senalighter));
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(0);
        MainActivity.hideBottomNav();
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.showBottomNav();
    }
}