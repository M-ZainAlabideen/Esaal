package app.esaal.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.QuestionsAdapter;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.questionsAndReplies.Question;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchFragment extends Fragment {
    public static FragmentActivity activity;
    public static SearchFragment fragment;
    private SessionManager sessionManager;
    private int pageIndex = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private ArrayList<Question> searchResultList = new ArrayList<>();
    private QuestionsAdapter questionsAdapter;
    private LinearLayoutManager layoutManager;

    @BindView(R.id.fragment_questions_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_questions_rv_questions)
    RecyclerView searchResult;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static SearchFragment newInstance(FragmentActivity activity) {
        fragment = new SearchFragment();
        SearchFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_questions, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.searchResult));
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);

        layoutManager = new LinearLayoutManager(activity);
        questionsAdapter = new QuestionsAdapter(activity, searchResultList);
        searchResult.setLayoutManager(layoutManager);
        searchResult.setAdapter(questionsAdapter);
        if (searchResultList.size() > 0) {
            loading.setVisibility(View.GONE);
        } else {
            searchResultsApi(getArguments().getString("query"));
        }
    }


    private void searchResultsApi(final String query) {
        EsaalApiConfig.getCallingAPIInterface().searchResults(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                query,
                pageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (questions.size() > 0) {
                                searchResultList.clear();
                                searchResultList.addAll(questions);
                                questionsAdapter.notifyDataSetChanged();
                                //add Scroll listener to the recycler , for pagination
                                searchResult.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                                                    getMoreResults(query);

                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            isLastPage = true;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        if (error.getResponse() != null && error.getResponse().getStatus() == 201) {
                            Snackbar.make(container, getString(R.string.noQuestions), Snackbar.LENGTH_SHORT).show();
                        } else {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(container,getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();

                        }
                    }
                }
        );
    }

    private void getMoreResults(String query) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().searchResults(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                query,
                pageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);

                        int status = response.getStatus();
                        if (status == 200) {
                            if (questions.size() > 0) {
                                searchResultList.addAll(questions);
                                questionsAdapter.notifyDataSetChanged();

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
                });
    }
}

