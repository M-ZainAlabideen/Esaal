package app.esaal.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.NotificationsAdapter;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.notifications.Notification;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class NotificationsFragment extends Fragment {
    public static FragmentActivity activity;
    public static NotificationsFragment fragment;
    private SessionManager sessionManager;
    private ArrayList<Notification> notificationsList = new ArrayList<>();
    private NotificationsAdapter notificationsAdapter;
    private LinearLayoutManager layoutManager;

    @BindView(R.id.fragment_notifications_rv_notifications)
    RecyclerView notifications;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static NotificationsFragment newInstance(FragmentActivity activity) {
        fragment = new NotificationsFragment();
        NotificationsFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, true, false, false, "notifications", getString(R.string.notifications));

        sessionManager = new SessionManager(activity);
        layoutManager = new LinearLayoutManager(activity);
        notificationsAdapter = new NotificationsAdapter(activity, notificationsList);
        notifications.setLayoutManager(layoutManager);
        notifications.setAdapter(notificationsAdapter);

        if (notificationsList.size() > 0) {
            loading.setVisibility(View.GONE);
        } else {
            notificationApi();
        }
    }

    private void notificationApi() {
        EsaalApiConfig.getCallingAPIInterface().notifications(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<ArrayList<Notification>>() {
                    @Override
                    public void success(ArrayList<Notification> notifications, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (notifications != null && notifications.size() > 0) {
                                notificationsList.addAll(notifications);
                                notificationsAdapter.notifyDataSetChanged();
                                updateNotificationsApi();
                            }
                        } else if (status == 204) {
                            Snackbar.make(loading, getString(R.string.noNotifications), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                }
        );
    }

    private void updateNotificationsApi() {
        EsaalApiConfig.getCallingAPIInterface().updateNotifications(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                    }

                    @Override
                    public void failure(RetrofitError error) {
                    }
                }
        );
    }
}

