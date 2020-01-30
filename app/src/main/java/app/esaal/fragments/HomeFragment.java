package app.esaal.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.FilterAdapter;
import app.esaal.adapters.QuestionsAdapter;
import app.esaal.adapters.SliderAdapter;
import app.esaal.classes.FixControl;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.RecyclerItemClickListener;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.questionsAndReplies.Question;
import app.esaal.webservices.responses.slider.Slider;
import app.esaal.webservices.responses.subjects.Subject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeFragment extends Fragment {
    public static FragmentActivity activity;
    public static HomeFragment fragment;
    private SessionManager sessionManager;
    private ArrayList<Slider> sliderList = new ArrayList<>();
    private SliderAdapter sliderAdapter;
    private int currentPage = 0;
    private int NUM_PAGES = 0;
    private ArrayList<Subject> subjectList = new ArrayList<>();
    private ArrayList<Question> questionsList = new ArrayList<>();
    private QuestionsAdapter questionsAdapter;
    LinearLayoutManager layoutManager;

    private int questionPageIndex = 1;
    private int filterPageIndex = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @BindView(R.id.fragment_home_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_home_iv_sliderPlaceholder)
    ImageView sliderPlaceholder;
    @BindView(R.id.fragment_home_vp_slider)
    RtlViewPager slider;
    @BindView(R.id.home_circleIndicator_sliderCircle)
    CircleIndicator sliderCircles;
    @BindView(R.id.fragment_home_tv_latestQuestionsWord)
    TextView latestQuestionsWord;
    @BindView(R.id.fragment_home_rv_questions)
    RecyclerView questions;
    @BindView(R.id.fragment_home_iv_questionMark)
    ImageView questionMark;
    @BindView(R.id.fragment_home_tv_introText)
    TextView introText;
    @BindView(R.id.fragment_home_tv_addQuestion)
    TextView addQuestion;

    @BindView(R.id.loading)
    ProgressBar loading;

    public static HomeFragment newInstance(FragmentActivity activity) {
        fragment = new HomeFragment();
        HomeFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        sessionManager = new SessionManager(activity);
        boolean isQuestion = false;
        if (sessionManager.isGuest()) {
            isQuestion = false;
            sessionManager.setTeacher(false);
        } else {
            isQuestion = true;
        }
        MainActivity.setupAppbar(true, true, true, isQuestion, "home", getString(R.string.ask));
        loading.setVisibility(View.GONE);
        GlobalFunctions.hasNewNotificationsApi(activity);
        FilterAdapter.subjectsSelectedIds.clear();
        if (sessionManager.isTeacher()) {
            MainActivity.addQuestion.setVisibility(View.GONE);
        } else {
            MainActivity.addQuestion.setVisibility(View.VISIBLE);
        }
        int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_slider);
        slider.getLayoutParams().height = Height;

        if (sliderList.size() <= 0) {
            sliderApi();
        } else {
            setupSlider();
        }

        slider.setCurrentItem(0, true);
        final Handler handler = new Handler();
        final Runnable update = new Runnable() {
            @Override
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                slider.setCurrentItem(currentPage++, true);
            }
        };


        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 5000, 5000);


        layoutManager = new LinearLayoutManager(activity);
        questionsAdapter = new QuestionsAdapter(activity, questionsList);
        questions.setLayoutManager(layoutManager);
        questions.setAdapter(questionsAdapter);


        if (sessionManager.isLoggedIn()) {
            questionPageIndex = 1;
            isLastPage = false;
            isLoading = false;
            questionsApi();
        } else {
            setupViews(false);
        }

        if (MainActivity.isEnglish) {
            Typeface enBold = Typeface.createFromAsset(activity.getAssets(), "montserrat_medium.ttf");
            latestQuestionsWord.setTypeface(enBold);
            latestQuestionsWord.setTypeface(enBold);
        } else {
            Typeface arBold = Typeface.createFromAsset(activity.getAssets(), "cairo_bold.ttf");
            introText.setTypeface(arBold);
            introText.setTypeface(arBold);
        }

        //change the color of editText in searchView
        EditText searchEditText = (EditText) MainActivity.search.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        searchEditText.setHintTextColor(getResources().getColor(R.color.colorPrimaryDark));

        MainActivity.search.setMaxWidth(Integer.MAX_VALUE);
        MainActivity.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MainActivity.search.onActionViewCollapsed();
                SearchFragment fragment = SearchFragment.newInstance(activity);
                Bundle b = new Bundle();
                b.putString("query", query);
                fragment.setArguments(b);
                Navigator.loadFragment(activity, fragment, R.id.activity_main_fl_container, true);
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
                isLastPage = false;
                isLoading = false;
                subjectsApi();
            }
        });
    }

    public void filterPopUp(final ArrayList<Subject> subjectsList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View filterView = ((Activity) activity).getLayoutInflater().inflate(R.layout.custom_dialog_filter, null);
        RecyclerView filterRecycler = (RecyclerView) filterView.findViewById(R.id.custom_dialog_filter_rv_filterBy);
        TextView done = (TextView) filterView.findViewById(R.id.custom_dialog_filter_tv_done);
        filterRecycler.setLayoutManager(new GridLayoutManager(activity, 3));
        final FilterAdapter filterAdapter = new FilterAdapter(activity, subjectsList, null);
        filterRecycler.setAdapter(filterAdapter);
        builder.setCancelable(true);

        builder.setView(filterView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setGravity(Gravity.CENTER);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionsList.clear();
                String totalSelectedSubjects = "";
                for (Integer item : FilterAdapter.subjectsSelectedIds) {
                    if (FilterAdapter.subjectsSelectedIds.get(0) == item) {
                        totalSelectedSubjects = item + "";
                    } else {
                        totalSelectedSubjects = totalSelectedSubjects + "," + item;
                    }
                }
                filterPageIndex = 1;
                dialog.cancel();
                filterResultsApi(totalSelectedSubjects);
            }
        });
        filterRecycler.addOnItemTouchListener(new RecyclerItemClickListener(activity, filterRecycler, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (subjectsList.get(position).id == 0) {
                    questionPageIndex = 1;
                    questionsList.clear();
                    FilterAdapter.subjectsSelectedIds.clear();
                    questionsApi();
                    dialog.cancel();
                } else {
                    for (int i = 0; i < FilterAdapter.subjectsSelectedIds.size(); i++) {
                        if (FilterAdapter.subjectsSelectedIds.get(i) == subjectsList.get(position).id) {
                            FilterAdapter.subjectsSelectedIds.remove(i);
                            filterAdapter.notifyDataSetChanged();
                            return;
                        }
                    }
                    FilterAdapter.subjectsSelectedIds.add(subjectsList.get(position).id);
                    filterAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));


    }

    private void subjectsApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().userSubjects(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<ArrayList<Subject>>() {
                    @Override
                    public void success(ArrayList<Subject> subjects, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            Subject all = new Subject();
                            all.setName(getString(R.string.all));
                            all.id = 0;
                            subjectList.clear();
                            subjectList.add(all);
                            subjectList.addAll(subjects);
                            filterPopUp(subjectList);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.EnableLayout(container);
                        if (error.getResponse() != null && error.getResponse().getStatus() == 202) {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(activity.findViewById(R.id.fragment_home_cl_outerContainer), getString(R.string.noSubjects), Snackbar.LENGTH_SHORT).show();
                        } else {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(activity.findViewById(R.id.fragment_home_cl_outerContainer), getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void filterResultsApi(final String subjectIds) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().filterResult(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                subjectIds,
                filterPageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
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

                                                    getMoreFilterResults(subjectIds);

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
                        GlobalFunctions.EnableLayout(container);

                        if (error.getResponse() != null && error.getResponse().getStatus() == 201) {
                            Snackbar.make(activity.findViewById(R.id.fragment_home_cl_outerContainer), getString(R.string.noQuestions), Snackbar.LENGTH_SHORT).show();
                        } else {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(activity.findViewById(R.id.fragment_home_cl_outerContainer), getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void getMoreFilterResults(String subjectIds) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);

        EsaalApiConfig.getCallingAPIInterface().filterResult(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                subjectIds,
                filterPageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);

                        int status = response.getStatus();
                        if (status == 200) {
                            if (questions.size() > 0) {
                                questionsList.addAll(questions);
                                questionsAdapter.notifyDataSetChanged();

                            } else {
                                isLastPage = true;
                                filterPageIndex = filterPageIndex - 1;
                            }
                            isLoading = false;


                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.EnableLayout(container);
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_home_cl_outerContainer), getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @OnClick(R.id.fragment_home_tv_addQuestion)
    public void addQuestionClick() {
        if (sessionManager.getUserId() == 0) {
            Navigator.loadFragment(activity, LoginFragment.newInstance(activity), R.id.activity_main_fl_container, false);
        } else {
            if (sessionManager.hasPackage()) {
                Navigator.loadFragment(activity, AddQuestionFragment.newInstance(activity, "add", null), R.id.activity_main_fl_container, true);
            } else {
                Snackbar.make(activity.findViewById(R.id.fragment_home_cl_outerContainer), getString(R.string.packageFirst), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void setupViews(boolean hasQuestions) {
        if (hasQuestions) {
            latestQuestionsWord.setVisibility(View.VISIBLE);
            questions.setVisibility(View.VISIBLE);

            questionMark.setVisibility(View.GONE);
            introText.setVisibility(View.GONE);
            addQuestion.setVisibility(View.GONE);
        } else {
            latestQuestionsWord.setVisibility(View.GONE);
            questions.setVisibility(View.GONE);

            questionMark.setVisibility(View.VISIBLE);
            if (!sessionManager.isTeacher()) {
                introText.setVisibility(View.VISIBLE);
                addQuestion.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupSlider() {
        NUM_PAGES = sliderList.size();
        sliderAdapter = new SliderAdapter(activity, sliderList);
        slider.setAdapter(sliderAdapter);
        sliderCircles.setViewPager(slider);
        sliderCircles.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
                currentPage = position;
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private void sliderApi() {
        EsaalApiConfig.getCallingAPIInterface().slider(
                new Callback<ArrayList<Slider>>() {
                    @Override
                    public void success(ArrayList<Slider> sliders, Response response) {
                        int status = response.getStatus();
                        if (status == 200) {
                            sliderList.addAll(sliders);
                            setupSlider();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_home_cl_outerContainer), activity.getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void questionsApi() {
        GlobalFunctions.DisableLayout(container);

        EsaalApiConfig.getCallingAPIInterface().questions(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                questionPageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);

                        int status = response.getStatus();
                        if (status == 200) {
                            latestQuestionsWord.setVisibility(View.VISIBLE);
                            if (questions != null && questions.size() > 0) {
                                questionsList.clear();
                                for (Question value : questions) {
                                    if (value.isPending && (sessionManager.getUserId() == value.pendingUserId)) {
                                        questionsList.add(value);
                                    }
                                }
                                for (Question value : questions) {
                                    if (!value.isPending) {
                                        questionsList.add(value);
                                    }
                                }
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
                            } else {
                                isLastPage = true;
                            }
                            setupViews(true);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);

                        if (error.getResponse() != null && error.getResponse().getStatus() == 201) {
                            setupViews(false);
                        } else {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(activity.findViewById(R.id.fragment_home_cl_outerContainer), getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void getMoreQuestions() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().questions(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                questionPageIndex,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (questions.size() > 0) {
                                questionsList.addAll(questions);
                                questionsAdapter.notifyDataSetChanged();

                            } else {
                                isLastPage = true;
                                questionPageIndex = questionPageIndex - 1;
                            }
                            isLoading = false;


                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.EnableLayout(container);
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_home_cl_outerContainer), getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }
}

