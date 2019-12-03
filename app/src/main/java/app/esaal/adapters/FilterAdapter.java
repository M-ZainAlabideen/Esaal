package app.esaal.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import app.esaal.R;
import app.esaal.webservices.responses.countries.Country;
import app.esaal.webservices.responses.subjects.Subject;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.viewHolder> {

    Context context;
    ArrayList<Subject> subjectsList;
    ArrayList<Country> countriesList;
    public static int selectedPosition;

    public FilterAdapter(Context context, ArrayList<Subject> subjectsList, ArrayList<Country> countriesList) {
        this.context = context;
        this.subjectsList = subjectsList;
        this.countriesList = countriesList;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_filter_tv_subjectName)
        TextView subjectName;
        @BindView(R.id.item_filter_cl_subjectBg)
        ConstraintLayout subjectBg;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public FilterAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_filter, viewGroup, false);
        return new FilterAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilterAdapter.viewHolder viewHolder, int position) {
        if (subjectsList != null) {
            String name = subjectsList.get(position).getName();
            if (name.length() > 8)
                viewHolder.subjectName.setText(name.substring(0, 7));
            else
                viewHolder.subjectName.setText(name);
            if (selectedPosition != -1) {
                if (position == selectedPosition)
                    viewHolder.subjectBg.setBackground(context.getResources().getDrawable(R.mipmap.box_filter_subject_sel));
            }
        } else if (countriesList != null) {
            viewHolder.subjectName.setText(countriesList.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        if (subjectsList != null)
            return subjectsList.size();
        else
            return countriesList.size();
    }

}
