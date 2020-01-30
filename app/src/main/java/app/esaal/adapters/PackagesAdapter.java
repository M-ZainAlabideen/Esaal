package app.esaal.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.webservices.responses.packages.Package;
import app.esaal.webservices.responses.packages.PackageSubject;
import app.esaal.webservices.responses.subjects.Subject;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PackagesAdapter extends RecyclerView.Adapter<PackagesAdapter.viewHolder> {
    Context context;
    List<Package> packagesList;
    OnItemClickListener listener;

    public PackagesAdapter(Context context, List<Package> packagesList, OnItemClickListener listener) {
        this.context = context;
        this.packagesList = packagesList;
        this.listener = listener;
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_package_v_selectPackage)
        View selectPackage;
        @BindView(R.id.item_package_cv_container)
        CardView container;
        @BindView(R.id.item_package_tv_cost)
        TextView cost;
        @BindView(R.id.item_package_tv_name)
        TextView name;
        @BindView(R.id.item_package_tv_questionsNum)
        TextView questionsNum;
        @BindView(R.id.item_package_tv_packSubjects)
        TextView packSubjects;
        @BindView(R.id.item_package_tv_duration)
        TextView duration;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public PackagesAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_package, viewGroup, false);
        return new PackagesAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull PackagesAdapter.viewHolder viewHolder, final int position) {
        viewHolder.container.setCardBackgroundColor(Color.parseColor(packagesList.get(position).colorCode));
        viewHolder.cost.setText(packagesList.get(position).price + " " + context.getString(R.string.currency));
        viewHolder.name.setText(packagesList.get(position).getName());
        viewHolder.questionsNum.setText(packagesList.get(position).numberOfQuestion + " " + context.getString(R.string.questionWord));

        viewHolder.packSubjects.setText("");
        if (packagesList.get(position).packageSubjects != null && packagesList.get(position).packageSubjects.size() > 0) {
            for (PackageSubject value : packagesList.get(position).packageSubjects) {
                if (viewHolder.packSubjects.getText().toString().isEmpty()) {
                    viewHolder.packSubjects.append(value.subject.getName());
                } else {
                    viewHolder.packSubjects.append(" - " + value.subject.getName());
                }
            }
        }

        viewHolder.selectPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.packageClick(packagesList.get(position).id);
            }
        });

        if (MainActivity.isEnglish) {
            Typeface enBold = Typeface.createFromAsset(context.getAssets(), "montserrat_medium.ttf");
            viewHolder.name.setTypeface(enBold);
            viewHolder.cost.setTypeface(enBold);
        } else {
            Typeface arBold = Typeface.createFromAsset(context.getAssets(), "cairo_bold.ttf");
            Typeface cairo = Typeface.createFromAsset(context.getAssets(), "cairo_regular.ttf");

            viewHolder.name.setTypeface(arBold);
            viewHolder.cost.setTypeface(arBold);
            viewHolder.questionsNum.setTypeface(cairo);
            viewHolder.duration.setTypeface(cairo);
            viewHolder.packSubjects.setTypeface(cairo);
    }
}

    @Override
    public int getItemCount() {
        return packagesList.size();
    }

public interface OnItemClickListener {
    void packageClick(int subscriptionId);
}
}





