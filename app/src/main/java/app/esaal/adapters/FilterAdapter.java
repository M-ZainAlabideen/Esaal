package app.esaal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

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
    public static ArrayList<Integer> subjectsSelectedIds = new ArrayList<>();

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
            viewHolder.subjectBg.setBackground(context.getResources().getDrawable(R.mipmap.box_filter_subject_unsel));
            String name = subjectsList.get(position).getName();
            if (name.length() > 8)
                viewHolder.subjectName.setText(name.substring(0, 7));
            else
                viewHolder.subjectName.setText(name);
            if (subjectsSelectedIds.size() != 0) {
                for (Integer item : subjectsSelectedIds) {
                    if (subjectsList.get(position).id == item)
                        viewHolder.subjectBg.setBackground(context.getResources().getDrawable(R.mipmap.box_filter_subject_sel));
                }
            }
        } else if (countriesList != null) {
            String name = countriesList.get(position).getName();
            if (name.length() > 8)
                viewHolder.subjectName.setText(name.substring(0, 7));
            else
                viewHolder.subjectName.setText(name);
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
