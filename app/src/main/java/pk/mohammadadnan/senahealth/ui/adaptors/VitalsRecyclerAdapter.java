package pk.mohammadadnan.senahealth.ui.adaptors;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import pk.mohammadadnan.senahealth.R;
import pk.mohammadadnan.senahealth.database.entity.VitalsEntity;
import pk.mohammadadnan.senahealth.ui.viewmodels.VitalsViewModel;
import pk.mohammadadnan.senahealth.utilities.DateUtils;

public class VitalsRecyclerAdapter extends RecyclerView.Adapter<VitalsRecyclerAdapter.VitalsViewHolder> {

    private VitalsViewModel vitalsViewModel;

    private Context context;
    private List<VitalsEntity> vitalsEntityList;
    private boolean isList;

    public VitalsRecyclerAdapter(Context context, List<VitalsEntity> vitalsEntityList, VitalsViewModel vitalsViewModel, boolean isList) {
        this.context = context;
        this.vitalsEntityList = new ArrayList<>(vitalsEntityList);
        this.vitalsViewModel = vitalsViewModel;
        this.isList = isList;
    }


    @Override
    public VitalsRecyclerAdapter.VitalsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VitalsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false));
    }

    @Override
    public void onBindViewHolder(VitalsViewHolder holder, int position) {
        VitalsEntity vitalsEntity = vitalsEntityList.get(position);

        if(!isList){
            holder.itemMore.setVisibility(View.GONE);
            holder.parentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(context,"Item Deleted",Toast.LENGTH_SHORT).show();
                    vitalsViewModel.delete(vitalsEntity);
                    Snackbar.make(view,"Log Deleted",Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> vitalsViewModel.insert(vitalsEntity))
                            .setActionTextColor(ContextCompat.getColor(context, R.color.peach))
                            .show();
                    return false;
                }
            });
        }else{
            holder.itemMore.setVisibility(View.VISIBLE);
            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("measurement_type",vitalsEntity.getMeasurementType());
                    Navigation.findNavController(view).navigate(R.id.home_to_vitals,bundle);
                }
            });
        }
        holder.measTwo.setVisibility(View.INVISIBLE);
        holder.unitTwo.setVisibility(View.INVISIBLE);
        holder.time.setText(DateUtils.getString(vitalsEntity.getTime()));
        switch (vitalsEntity.getMeasurementType()){
            case 1:
                holder.itemIcon.setImageResource(R.drawable.meas_glucose);
                holder.itemTitle.setText("Blood Glucose");
                holder.measOne.setText((String.format("%.0f",vitalsEntity.getMeasurementOne())));
                holder.unitOne.setText("mg/dL");
                break;
            case 2:
                holder.itemIcon.setImageResource(R.drawable.meas_pressure);
                holder.measTwo.setVisibility(View.VISIBLE);
                holder.unitTwo.setVisibility(View.VISIBLE);
                holder.itemTitle.setText("Blood Pressure");
                holder.measOne.setText(String.format("%.0f",vitalsEntity.getMeasurementOne())+"/"+String.format("%.0f",vitalsEntity.getMeasurementTwo()));
                holder.unitOne.setText("mmHg");
                holder.measTwo.setText((String.format("%.0f",vitalsEntity.getMeasurementThree())));
                holder.unitTwo.setText("bpm");
                break;
            case 3:
                holder.itemIcon.setImageResource(R.drawable.meas_temp);
                holder.itemTitle.setText("Temperature");
                holder.measOne.setText((String.format("%.1f",vitalsEntity.getMeasurementOne())));
                holder.unitOne.setText("°F");
                break;
            case 4:
                holder.itemIcon.setImageResource(R.drawable.meas_weight);
                holder.itemTitle.setText("Weight");
                holder.measOne.setText((String.format("%.1f",vitalsEntity.getMeasurementOne())));
                holder.unitOne.setText("lb");
                break;
            case 5:
                holder.itemIcon.setImageResource(R.drawable.meas_oxygen);
                holder.itemTitle.setText("Oxygen Saturation");
                holder.measOne.setText((String.format("%.0f",vitalsEntity.getMeasurementOne())));
                holder.unitOne.setText("%");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return vitalsEntityList.size();
    }

    class VitalsViewHolder extends RecyclerView.ViewHolder {
        private View parentView;
        private ImageView itemMore;
        private ImageView itemIcon;
        private  TextView itemTitle;
        private  TextView measOne;
        private  TextView measTwo;
        private  TextView unitOne;
        private  TextView unitTwo;
        private  TextView time;


        public VitalsViewHolder(View itemView) {
            super(itemView);
            parentView = itemView;
            itemMore = (ImageView) itemView.findViewById(R.id.item_more);
            itemIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            itemTitle = (TextView) itemView.findViewById(R.id.item_title);
            measOne = (TextView) itemView.findViewById(R.id.item_measOne);
            measTwo = (TextView) itemView.findViewById(R.id.item_measTwo);
            unitOne = (TextView) itemView.findViewById(R.id.item_measOne_unit);
            unitTwo = (TextView) itemView.findViewById(R.id.item_measTwo_unit);
            time = (TextView) itemView.findViewById(R.id.item_time);
        }
    }
}