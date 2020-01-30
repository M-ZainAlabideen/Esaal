package app.esaal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

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
import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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
        if (activity == null) {
            activity = getActivity();
        }
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
                            Snackbar.make(container, getString(R.string.noNotifications), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getResponse() != null && error.getResponse().getStatus() == 204) {
                            Snackbar.make(container, getString(R.string.noNotifications), Snackbar.LENGTH_SHORT).show();

                        } else {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(container,getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
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
                        loading.setVisibility(View.GONE);
                        Snackbar.make(container,getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
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
                        loading.setVisibility(View.GONE);
                        Snackbar.make(container,getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                    }
                }
        );
    }
}

