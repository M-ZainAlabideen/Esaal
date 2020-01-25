package app.esaal.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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
import app.esaal.SplashActivity;
import app.esaal.adapters.NotificationsAdapter;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.notifications.Notification;
import app.esaal.webservices.responses.questionsAndReplies.Question;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.leolin.shortcutbadger.ShortcutBadger;
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

    private int pageIndex = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @BindView(R.id.fragment_notifications_cl_container)
    ConstraintLayout container;
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
        notifications.setLayoutManager(layoutManager);
        notificationApi();
    }

    private void notificationApi() {
        EsaalApiConfig.getCallingAPIInterface().notifications(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                pageIndex,
                new Callback<ArrayList<Notification>>() {
                    @Override
                    public void success(ArrayList<Notification> notifications, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (notifications != null && notifications.size() > 0) {
                                notificationsList.clear();
                                notificationsList.addAll(notifications);
                                setNotificationsAdapter();
                                fragment.notifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                    @Override
                                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                        super.onScrollStateChanged(recyclerView, newState);
                                    }

                                    @Override
                                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                        super.onScrolled(recyclerView, dx, dy);
                                        if (!isLastPage) {
                                            int visibleItemCount = layoutManager.getChildCount();

                                            int totalItemCount = layoutManager.getItemCount();

                                            int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                                /*isLoading variable used for check if the user send many requests
                                for pagination(make many scrolls in the same time)
                                1- if isLoading true >> there is request already sent so,
                                no more requests till the response of last request coming
                                2- else >> send new request for load more data (News)*/
                                            if (!isLoading) {

                                                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                                    isLoading = true;

                                                    pageIndex = pageIndex + 1;

                                                    getMoreNotifications();

                                                }
                                            }
                                        }
                                    }
                                });
                                updateNotificationsApi();
                            }
                        } else if (status == 204) {
                            isLastPage = true;
                            Snackbar.make(loading, getString(R.string.noNotifications), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getResponse() != null && error.getResponse().getStatus() == 204) {
                            Snackbar.make(loading, getString(R.string.noNotifications), Snackbar.LENGTH_SHORT).show();

                        } else {
                            GlobalFunctions.generalErrorMessage(loading, activity);
                        }
                    }
                }
        );
    }

    private void getMoreNotifications() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().notifications(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                pageIndex,
                new Callback<ArrayList<Notification>>() {
                    @Override
                    public void success(ArrayList<Notification> notifications, Response response) {
                        loading.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (notifications.size() > 0) {
                                notificationsList.addAll(notifications);
                                setNotificationsAdapter();

                            } else {
                                isLastPage = true;
                                pageIndex = pageIndex - 1;
                            }
                            isLoading = false;


                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.EnableLayout(container);
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                }
        );
    }

    private void setNotificationsAdapter() {
        notificationsAdapter = new NotificationsAdapter(activity, notificationsList);
        notifications.setAdapter(notificationsAdapter);
    }

    private void updateNotificationsApi() {
        EsaalApiConfig.getCallingAPIInterface().updateNotifications(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        if (response2.getStatus() == 200) {
                            MainActivity.hasNewNotifications = false;
                            ShortcutBadger.removeCount(activity);

                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                }
        );
    }
}

