package app.esaal.adapters;

import android.content.Context;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.fragments.QuestionDetailsFragment;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.notifications.Notification;
import app.esaal.webservices.responses.questionsAndReplies.Question;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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
        @BindView(R.id.item_notification_tv_description)
        TextView description;
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

        if (!notificationsList.get(position).isRead) {
            viewHolder.indicator.setImageResource(R.mipmap.ic_notifi_circle_blue);
        }

        viewHolder.title.setText(notificationsList.get(position).getMessage());
        viewHolder.date.setText(notificationsList.get(position).getNotificationDate());


        if (notificationsList.get(position).getSubjectName() != null && !notificationsList.get(position).getSubjectName().isEmpty()) {
            viewHolder.subjectName.setVisibility(View.VISIBLE);
            viewHolder.subjectName.setText(notificationsList.get(position).getSubjectName());
        } else {
            viewHolder.subjectName.setVisibility(View.GONE);
        }
        if (notificationsList.get(position).questionDescription != null && !notificationsList.get(position).questionDescription.isEmpty()) {
            viewHolder.description.setVisibility(View.VISIBLE);
            String description = notificationsList.get(position).questionDescription;
            viewHolder.description.setText(description);
        } else {
            viewHolder.description.setVisibility(View.GONE);
        }

        viewHolder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notificationsList.get(position).requestQuestionId == 0) ;
                else {
                    questionByIdApi(notificationsList.get(position).requestQuestionId,viewHolder);
                }
            }
        });

        if (MainActivity.isEnglish) {
            Typeface enBold = Typeface.createFromAsset(context.getAssets(), "montserrat_medium.ttf");
            viewHolder.subjectName.setTypeface(enBold);
            Typeface cairo = Typeface.createFromAsset(context.getAssets(), "cairo_regular.ttf");
            viewHolder.title.setTypeface(cairo);
            viewHolder.date.setTypeface(cairo);
        } else {
            Typeface arBold = Typeface.createFromAsset(context.getAssets(), "cairo_bold.ttf");
            viewHolder.subjectName.setTypeface(arBold);
        }
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    //call this api when click on notification to make sure if this question still available or not
    private void questionByIdApi(final int questionId,NotificationsAdapter.viewHolder viewHolder) {
        EsaalApiConfig.getCallingAPIInterface().questionById(
                sessionManager.getUserToken(),
                questionId,
                sessionManager.getUserId(),
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        int status = response.getStatus();
                        if (status == 200) {
                            if (questions != null && questions.size() != 0) {
                                Navigator.loadFragment((FragmentActivity) context, QuestionDetailsFragment.newInstance((FragmentActivity) context, questionId), R.id.activity_main_fl_container, true);
                            } else {
                                Snackbar.make(viewHolder.details, context.getString(R.string.questionAnswered), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                    }
                }
        );
    }


}