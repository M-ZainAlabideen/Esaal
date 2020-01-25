package app.esaal.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.SessionManager;
import app.esaal.fragments.AddQuestionFragment;
import app.esaal.fragments.RegistrationFragment;
import app.esaal.webservices.responses.subjects.Subject;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.viewHolder> {
    private Context context;
    private ArrayList<Subject> subjectsList;
    public SubjectsAdapter(Context context, ArrayList<Subject> subjectsList) {
        this.context = context;
        this.subjectsList = subjectsList;
    }

    public class viewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.item_subject_cl_subjectBg)
        public ConstraintLayout subjectBg;
        @BindView(R.id.item_subject_tv_subjectName)
        public TextView subjectName;
        @BindView(R.id.item_subject_v_select)
        View select;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public SubjectsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_subject, viewGroup, false);
        return new SubjectsAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SubjectsAdapter.viewHolder viewHolder, final int position) {
        viewHolder.subjectName.setText(subjectsList.get(position).getName());
        viewHolder.select.setVisibility(View.INVISIBLE);
        viewHolder.subjectName.setTextColor(Color.parseColor("#9E9D9D"));
        viewHolder.subjectBg.setBackground(null);
        if(!MainActivity.isEnglish) {
            Typeface cairo = Typeface.createFromAsset(context.getAssets(), "cairo_regular.ttf");
            viewHolder.subjectName.setTypeface(cairo);
        }

        if (subjectsList.get(position).id == AddQuestionFragment.selectedSubjectId) {
            viewHolder.select.setVisibility(View.VISIBLE);
            viewHolder.subjectName.setTextColor(Color.parseColor("#000000"));
            if (MainActivity.isEnglish) {
                Typeface enBold = Typeface.createFromAsset(context.getAssets(), "montserrat_medium.ttf");
                viewHolder.subjectName.setTypeface(enBold);
            }
            else{
                Typeface arBold = Typeface.createFromAsset(context.getAssets(), "cairo_bold.ttf");
                viewHolder.subjectName.setTypeface(arBold);
            }
        }
        viewHolder.subjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddQuestionFragment.selectedSubjectId = subjectsList.get(position).id;
                notifyDataSetChanged();
            }
        });

    }


    @Override
    public int getItemCount() {
        return subjectsList.size();
    }

}



