package app.esaal.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.FilterAdapter;
import app.esaal.adapters.QuestionsAdapter;
import app.esaal.classes.Navigator;
import app.esaal.classes.RecyclerItemClickListener;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.questionsAndReplies.Question;
import app.esaal.webservices.responses.subjects.Subject;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class QuestionsFragment extends Fragment {
    public static FragmentActivity activity;
    public static QuestionsFragment fragment;
    private SessionManager sessionManager;
    private int questionPageIndex = 1;
    private int filterPageIndex = 1;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private ArrayList<Question> questionsList = new ArrayList<>();
    private QuestionsAdapter questionsAdapter;
    private LinearLayoutManager layoutManager;

    @BindView(R.id.fragment_questions_rv_questions)
    RecyclerView questions;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static QuestionsFragment newInstance(FragmentActivity activity) {
        fragment = new QuestionsFragment();
        QuestionsFragment.activity = activity;
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
        MainActivity.setupAppbar(true, true, false, true, "account", getString(R.string.questionsAndReplies));
        sessionManager = new SessionManager(activity);
        FilterAdapter.selectedPosition = -1;
        layoutManager = new LinearLayoutManager(activity);
        questionsAdapter = new QuestionsAdapter(activity, questionsList);
        questions.setLayoutManager(layoutManager);
        questions.setAdapter(questionsAdapter);
        if (questionsList.size() > 0) {
            loading.setVisibility(View.GONE);
        } else {
            questionsApi();
        }

        //change the color of editText in searchView
        EditText searchEditText = (EditText) MainActivity.search.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        searchEditText.setHintTextColor(getResources().getColor(R.color.colorPrimaryDark));

        MainActivity.search.setMaxWidth(Integer.MAX_VALUE);
        MainActivity.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MainActivity.search.onActionViewCollapsed();
                SearchFragment fragment = SearchFragment.newInstance(activity);
                Bundle b = new Bundle();
                b.putString("query",query);
                fragment.setArguments(b);
                Navigator.loadFragment(activity,fragment, R.id.activity_main_fl_container, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        MainActivity.filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subjectsApi();
            }
        });
    }

    public void filterPopUp(final ArrayList<Subject> subjectsList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View profileDialogView = ((Activity) activity).getLayoutInflater().inflate(R.layout.custom_dialog_filter, null);
        RecyclerView filterRecycler = (RecyclerView) profileDialogView.findViewById(R.id.custom_dialog_filter_rv_filterBy);

        filterRecycler.setLayoutManager(new GridLayoutManager(activity, 3));
        final FilterAdapter filterAdapter = new FilterAdapter(activity, subjectsList,null);
        filterRecycler.setAdapter(filterAdapter);
        builder.setCancelable(true);

        builder.setView(profileDialogView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setGravity(Gravity.CENTER);

        filterRecycler.addOnItemTouchListener(new RecyclerItemClickListener(activity, filterRecycler, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FilterAdapter.selectedPosition = position;
                filterAdapter.notifyDataSetChanged();
                dialog.cancel();
                loading.setVisibility(View.VISIBLE);
                questionsList.clear();
                filterResultsApi(subjectsList.get(FilterAdapter.selectedPosition).id);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));


    }
    private void subjectsApi(){
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().userSubjects(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<ArrayList<Subject>>() {
                    @Override
                    public void success(ArrayList<Subject> subjects, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            filterPopUp(subjects);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        int failureStatus = error.getResponse().getStatus();
                        if(failureStatus == 202){
                            Snackbar.make(loading,getString(R.string.noSubjects),Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void questionsApi() {
        EsaalApiConfig.getCallingAPIInterface().questions(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                questionPageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (questions.size() > 0) {
                                questionsList.clear();
                                questionsList.addAll(questions);
                                questionsAdapter.notifyDataSetChanged();
                                //add Scroll listener to the recycler , for pagination
                                fragment.questions.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                                                    questionPageIndex = questionPageIndex + 1;

                                                    getMoreQuestions();

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
                        int failureStatus = error.getResponse().getStatus();
                        if (failureStatus == 201) {
                            Snackbar.make(loading, getString(R.string.noQuestions), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
    private void getMoreQuestions(){
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().questions(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                questionPageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (questions.size() > 0) {
                                questionsList.addAll(questions);
                                questionsAdapter.notifyDataSetChanged();

                            }
                            else{
                                isLastPage = true;
                                questionPageIndex = questionPageIndex - 1;
                            }
                            isLoading = false;


                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
    }
    private void filterResultsApi(final int subjectId){
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().filterResult(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                subjectId,
                filterPageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (questions.size() > 0) {
                                questionsList.addAll(questions);
                                questionsAdapter.notifyDataSetChanged();
                                //add Scroll listener to the recycler , for pagination
                                fragment.questions.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                                                    filterPageIndex = filterPageIndex + 1;

                                                    getMoreFilterResults(subjectId);

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
                        int failureStatus = error.getResponse().getStatus();
                        if (failureStatus == 201) {
                            Snackbar.make(loading, getString(R.string.noQuestions), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
    private void getMoreFilterResults(int subjectId){
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().filterResult(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                subjectId,
                filterPageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (questions.size() > 0) {
                                questionsList.addAll(questions);
                                questionsAdapter.notifyDataSetChanged();

                            }
                            else{
                                isLastPage = true;
                                filterPageIndex = filterPageIndex - 1;
                            }
                            isLoading = false;


                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }
}

