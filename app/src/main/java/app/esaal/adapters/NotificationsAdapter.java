package app.esaal.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.fragments.QuestionDetailsFragment;
import app.esaal.webservices.responses.notifications.Notification;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.viewHolder> {
    private Context context;
    private ArrayList<Notification> notificationsList;
    private SessionManager sessionManager;

    public NotificationsAdapter(Context context, ArrayList<Notification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_notification_tv_date)
        TextView date;
        @BindView(R.id.item_notification_iv_indicator)
        ImageView indicator;
        @BindView(R.id.item_notification_tv_subjectName)
        TextView subjectName;
        @BindView(R.id.item_notification_tv_title)
        TextView title;
        @BindView(R.id.item_notification_v_details)
        View details;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            sessionManager = new SessionManager(context);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public NotificationsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_notification, viewGroup, false);
        return new NotificationsAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.viewHolder viewHolder, final int position) {

        if(!notificationsList.get(position).isRead){
            viewHolder.indicator.setImageResource(R.mipmap.ic_notifi_circle_blue);
        }
        viewHolder.subjectName.setText(notificationsList.get(position).getSubjectName());
        if (notificationsList.get(position).getMessage().length() > 25)
            viewHolder.title.setText(notificationsList.get(position).getMessage().substring(0, 25) + "...");
        else
            viewHolder.title.setText(notificationsList.get(position).getMessage());

        viewHolder.date.setText(notificationsList.get(position).getNotificationDate());

        viewHolder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notificationsList.get(position).requestQuestionId == 0);
                else{
                    Navigator.loadFragment((FragmentActivity) context, QuestionDetailsFragment.newInstance((FragmentActivity) context,notificationsList.get(position).requestQuestionId), R.id.activity_main_fl_container, true);
                }
            }
        });

         if (MainActivity.isEnglish)  {
            Typeface cairo = Typeface.createFromAsset(context.getAssets(), "cairo_regular.ttf");
            viewHolder.title.setTypeface(cairo);
            viewHolder.date.setTypeface(cairo);
        }
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }
}




