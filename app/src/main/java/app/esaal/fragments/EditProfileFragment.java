package app.esaal.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.riontech.staggeredtextgridview.StaggeredTextGridView;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.FilterAdapter;
import app.esaal.adapters.StaggeredSubjectsAdapter;
import app.esaal.classes.FixControl;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.RecyclerItemClickListener;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.requests.StudentRequest;
import app.esaal.webservices.requests.TeacherRequest;
import app.esaal.webservices.responses.authorization.User;
import app.esaal.webservices.responses.countries.Country;
import app.esaal.webservices.responses.subjects.Subject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EditProfileFragment extends Fragment {
    public static FragmentActivity activity;
    public static EditProfileFragment fragment;
    private SessionManager sessionManager;
    private User userData;
    private ArrayList<Subject> subjectsList = new ArrayList<>();
    StaggeredSubjectsAdapter subjectsAdapter;

    private ArrayList<Country> countriesList = new ArrayList<>();
    private AlertDialog dialog;
    private boolean isGulfCountry;
    private int countryId;
    public static ArrayList<Integer> subjectsIds = new ArrayList<>();

    @BindView(R.id.fragment_edit_profile_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_edit_profile_et_firstName)
    EditText firstName;
    @BindView(R.id.fragment_edit_profile_et_middleName)
    EditText middleName;
    @BindView(R.id.fragment_edit_profile_et_lastName)
    EditText lastName;
    @BindView(R.id.fragment_edit_profile_et_email)
    EditText email;
    @BindView(R.id.fragment_edit_profile_et_mobile)
    EditText mobile;
    @BindView(R.id.fragment_edit_profile_et_userName)
    EditText userName;
    @BindView(R.id.fragment_edit_profile_tv_countryWord)
    TextView countryWord;
    @BindView(R.id.fragment_edit_profile_et_country)
    EditText country;
    @BindView(R.id.fragment_edit_profile_tv_accountNumWord)
    TextView accountNumWord;
    @BindView(R.id.fragment_edit_profile_et_accountNum)
    EditText accountNum;
    @BindView(R.id.fragment_edit_profile_tv_swiftCodeWord)
    TextView swiftCodeWord;
    @BindView(R.id.fragment_edit_profile_et_swiftCode)
    EditText swiftCode;
    @BindView(R.id.fragment_edit_profile_tv_bankNameWord)
    TextView bankNameWord;
    @BindView(R.id.fragment_edit_profile_et_bankName)
    EditText bankName;
    @BindView(R.id.fragment_edit_profile_tv_bankAddressWord)
    TextView bankAddressWord;
    @BindView(R.id.fragment_edit_profile_et_bankAddress)
    EditText bankAddress;
    @BindView(R.id.fragment_edit_profile_tv_personalAddressWord)
    TextView personalAddressWord;
    @BindView(R.id.fragment_edit_profile_et_personalAddress)
    EditText personalAddress;
    @BindView(R.id.fragment_edit_profile_tv_IBANWord)
    TextView IBANWord;
    @BindView(R.id.fragment_edit_profile_et_IBAN)
    EditText IBAN;
    @BindView(R.id.fragment_edit_profile_tv_descriptionWord)
    TextView descriptionWord;
    @BindView(R.id.fragment_edit_profile_et_description)
    EditText description;
    @BindView(R.id.fragment_edit_profile_tv_subjectsWord)
    TextView subjectWord;
    @BindView(R.id.fragment_edit_profile_stv_subjects)
    StaggeredTextGridView subjects;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.loading2)
    ProgressBar loading2;

    public static EditProfileFragment newInstance(FragmentActivity activity) {
        fragment = new EditProfileFragment();
        EditProfileFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.editProfile));
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);

        container.setVisibility(View.GONE);

        countryWord.setVisibility(View.GONE);
        country.setVisibility(View.GONE);
        IBAN.setVisibility(View.GONE);
        IBANWord.setVisibility(View.GONE);
        swiftCode.setVisibility(View.GONE);
        swiftCodeWord.setVisibility(View.GONE);
        accountNum.setVisibility(View.GONE);
        accountNumWord.setVisibility(View.GONE);
        bankName.setVisibility(View.GONE);
        bankNameWord.setVisibility(View.GONE);
        bankAddress.setVisibility(View.GONE);
        bankAddressWord.setVisibility(View.GONE);
        personalAddress.setVisibility(View.GONE);
        personalAddressWord.setVisibility(View.GONE);
        subjectWord.setVisibility(View.GONE);
        subjects.setVisibility(View.GONE);
        descriptionWord.setVisibility(View.GONE);
        description.setVisibility(View.GONE);
        loading2.setVisibility(View.GONE);

        if (sessionManager.isTeacher()) {
            if (countriesList.size() > 0) {
                loading.setVisibility(View.GONE);
            } else {
                countriesApi();
            }

            subjectsAdapter = new StaggeredSubjectsAdapter(activity, subjectsList, subjectsIds, true);
            subjects.setAdapter(subjectsAdapter);
        }
        if (userData == null) {
            userByIdApi();
        } else {
            setData();
        }
    }

    @OnClick(R.id.fragment_edit_profile_et_country)
    public void countryClick() {
        SelectCountryPopUp(activity, countriesList);
    }

    @OnClick(R.id.fragment_edit_profile_tv_save)
    public void saveClick() {

        String firstNameStr = firstName.getText().toString();
        String middleNameStr = middleName.getText().toString();
        String lastNameStr = lastName.getText().toString();
        String userNameStr = userName.getText().toString();
        String emailStr = email.getText().toString();
        String mobileStr = mobile.getText().toString();
        String countryStr = country.getText().toString();
        String IBANStr = IBAN.getText().toString();
        String accountNumStr = accountNum.getText().toString();
        String bankAddressStr = bankAddress.getText().toString();
        String personalAddressStr = personalAddress.getText().toString();
        String descriptionStr = description.getText().toString();
        String swiftCodeStr = swiftCode.getText().toString();
        String bankNameStr = bankName.getText().toString();

        String subjectsIdsStr = null;
        for (Integer value : subjectsIds) {
            if (subjectsIdsStr == null) {
                subjectsIdsStr = "" + value;
            } else {
                subjectsIdsStr = subjectsIdsStr + "," + value;
            }
        }


        if (firstNameStr == null || firstNameStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterFirstName), Snackbar.LENGTH_SHORT).show();
        } else if (lastNameStr == null || lastNameStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterLastName), Snackbar.LENGTH_SHORT).show();
        } else if (mobileStr == null || mobileStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterMobile), Snackbar.LENGTH_SHORT).show();
        } else if (emailStr == null || emailStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterEmail), Snackbar.LENGTH_SHORT).show();
        } else if (userNameStr == null || userNameStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterUserName), Snackbar.LENGTH_SHORT).show();
        } else if (!FixControl.isValidEmail(emailStr)) {
            Snackbar.make(loading, getString(R.string.invalidEmail), Snackbar.LENGTH_SHORT).show();
        } else {
            if (sessionManager.isTeacher()) {
                if (countryStr.equals(getString(R.string.country))) {
                    Snackbar.make(loading, getString(R.string.selectYourCountry), Snackbar.LENGTH_SHORT).show();
                } else if (bankNameStr == null || bankNameStr.isEmpty()) {
                    Snackbar.make(loading, getString(R.string.enterBankName), Snackbar.LENGTH_SHORT).show();
                } else if (swiftCodeStr == null || swiftCodeStr.isEmpty()) {
                    Snackbar.make(loading, getString(R.string.enterSwiftCode), Snackbar.LENGTH_SHORT).show();
                } else if (isGulfCountry && (IBANStr == null || IBANStr.isEmpty())) {
                    Snackbar.make(loading, getString(R.string.enterIBAN), Snackbar.LENGTH_SHORT).show();
                } else if (!isGulfCountry && (accountNumStr == null || accountNumStr.isEmpty())) {
                    Snackbar.make(loading, getString(R.string.enterAccountNumber), Snackbar.LENGTH_SHORT).show();
                } else if (!isGulfCountry && (bankAddressStr == null || bankAddressStr.isEmpty())) {
                    Snackbar.make(loading, getString(R.string.enterBankAddress), Snackbar.LENGTH_SHORT).show();
                } else if (!isGulfCountry && (personalAddressStr == null || personalAddressStr.isEmpty())) {
                    Snackbar.make(loading, getString(R.string.enterPersonalAddress), Snackbar.LENGTH_SHORT).show();
                } else if (subjectsIdsStr == null || subjectsIdsStr.isEmpty()) {
                    Snackbar.make(loading, getString(R.string.selectSubjects), Snackbar.LENGTH_SHORT).show();
                } else {
                    TeacherRequest teacher = new TeacherRequest();
                    teacher.userId = sessionManager.getUserId();
                    teacher.firstName = firstNameStr;
                    teacher.middleName = middleNameStr;
                    teacher.lastName = lastNameStr;
                    teacher.mobile = mobileStr;
                    teacher.email = emailStr;
                    teacher.userName = userNameStr;
                    teacher.swiftCode = swiftCodeStr;
                    teacher.IBAN = IBANStr;
                    teacher.bankName = bankNameStr;
                    teacher.bankAddress = bankAddressStr;
                    teacher.accountNumber = accountNumStr;
                    teacher.personalAddress = personalAddressStr;
                    teacher.description = descriptionStr;
                    teacher.countryId = countryId;
                    teacher.subjectsIds = subjectsIdsStr;
                    editTeacherProfileApi(teacher);
                }
            } else {
                StudentRequest student = new StudentRequest();
                student.userId = sessionManager.getUserId();
                student.firstName = firstNameStr;
                student.middleName = middleNameStr;
                student.lastName = lastNameStr;
                student.mobile = mobileStr;
                student.email = emailStr;
                student.username = userNameStr;
                editStudentProfileApi(student);
            }
        }
    }

    private void setData() {
        container.setVisibility(View.VISIBLE);

        firstName.setText(userData.firstName);
        middleName.setText(userData.middleName);
        lastName.setText(userData.lastName);
        mobile.setText(userData.mobile);
        email.setText(userData.email);
        userName.setText(userData.userName);
        if (sessionManager.isTeacher()) {
            countryId = userData.countryId;
            for (Country value : countriesList) {
                if (value.id == countryId) {
                    country.setText(value.getName());
                    isGulfCountry = value.isGulfCountry;
                }
            }
            countryWord.setVisibility(View.VISIBLE);
            country.setVisibility(View.VISIBLE);
            subjectWord.setVisibility(View.VISIBLE);
            subjects.setVisibility(View.VISIBLE);
            descriptionWord.setVisibility(View.VISIBLE);
            description.setVisibility(View.VISIBLE);
            swiftCode.setVisibility(View.VISIBLE);
            swiftCodeWord.setVisibility(View.VISIBLE);
            bankName.setVisibility(View.VISIBLE);
            bankNameWord.setVisibility(View.VISIBLE);
            if (isGulfCountry) {
                IBAN.setVisibility(View.VISIBLE);
                IBANWord.setVisibility(View.VISIBLE);
            } else {
                accountNum.setVisibility(View.VISIBLE);
                accountNumWord.setVisibility(View.VISIBLE);
                bankAddress.setVisibility(View.VISIBLE);
                bankAddressWord.setVisibility(View.VISIBLE);
                personalAddress.setVisibility(View.VISIBLE);
                personalAddressWord.setVisibility(View.VISIBLE);
            }
            IBAN.setText(userData.IBAN);
            swiftCode.setText(userData.swiftCode);
            accountNum.setText(userData.accountNumber);
            bankAddress.setText(userData.bankAddress);
            bankName.setText(userData.bankName);
            personalAddress.setText(userData.personalAddress);
            description.setText(userData.description);
        }
    }

    public void SelectCountryPopUp(final Context context, final ArrayList<Country> countriesList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View popUpView = ((Activity) context).getLayoutInflater().inflate(R.layout.custom_dialog_filter, null);
        RecyclerView popUpRecycler = (RecyclerView) popUpView.findViewById(R.id.custom_dialog_filter_rv_filterBy);
        TextView title = (TextView) popUpView.findViewById(R.id.custom_dialog_filter_tv_title);
        title.setText(getString(R.string.selectCountry));

        popUpRecycler.setLayoutManager(new GridLayoutManager(context, 3));
        popUpRecycler.setAdapter(new FilterAdapter(context, null, countriesList));
        builder.setCancelable(true);
        builder.setView(popUpView);
        dialog = builder.create();
        dialog.show();
        popUpRecycler.addOnItemTouchListener(new RecyclerItemClickListener(context, popUpRecycler, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                country.setText(countriesList.get(position).getName());
                isGulfCountry = countriesList.get(position).isGulfCountry;
                countryId = countriesList.get(position).id;

                if (isGulfCountry) {
                    IBAN.setVisibility(View.VISIBLE);
                    IBANWord.setVisibility(View.VISIBLE);

                    accountNum.setVisibility(View.GONE);
                    accountNumWord.setVisibility(View.GONE);
                    bankAddress.setVisibility(View.GONE);
                    bankAddressWord.setVisibility(View.GONE);
                    personalAddress.setVisibility(View.GONE);
                    personalAddressWord.setVisibility(View.GONE);
                } else {
                    accountNum.setVisibility(View.VISIBLE);
                    accountNumWord.setVisibility(View.VISIBLE);
                    bankAddress.setVisibility(View.VISIBLE);
                    bankAddressWord.setVisibility(View.VISIBLE);
                    personalAddress.setVisibility(View.VISIBLE);
                    personalAddressWord.setVisibility(View.VISIBLE);

                    IBAN.setVisibility(View.GONE);
                    IBANWord.setVisibility(View.GONE);
                }
                closePopUp();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }

    public void closePopUp() {
        dialog.cancel();

    }

    private void userByIdApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().userById(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<User>() {
                    @Override
                    public void success(User user, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (user != null) {
                                userData = user;
                                setData();
                            }
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

    private void editStudentProfileApi(StudentRequest student) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().editStudentProfile(
                sessionManager.getUserToken(),
                student,
                new Callback<User>() {
                    @Override
                    public void success(User user, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            Snackbar.make(loading, getString(R.string.profileUpdatedSuccessfully), Snackbar.LENGTH_SHORT).show();
                            Navigator.loadFragment(activity, MyAccountFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        if (error.getResponse() != null && error.getResponse().getStatus() == 201) {
                            Snackbar.make(loading, getString(R.string.emailExists), Snackbar.LENGTH_SHORT).show();
                        } else if (error.getResponse() != null && error.getResponse().getStatus() == 202) {
                            Snackbar.make(loading, getString(R.string.userNameExists), Snackbar.LENGTH_SHORT).show();
                        } else if (error.getResponse() != null && error.getResponse().getStatus() == 203) {
                            Snackbar.make(loading, getString(R.string.mobileExists), Snackbar.LENGTH_SHORT).show();
                        } else {
                            GlobalFunctions.generalErrorMessage(loading, activity);
                        }
                    }
                }
        );
    }

    private void editTeacherProfileApi(TeacherRequest teacherRequest) {
        loading2.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().editTeacherProfile(
                sessionManager.getUserToken(),
                teacherRequest,
                new Callback<User>() {
                    @Override
                    public void success(User user, Response response) {
                        loading2.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            Snackbar.make(loading, getString(R.string.profileUpdatedSuccessfully), Snackbar.LENGTH_SHORT).show();
                            Navigator.loadFragment(activity, MyAccountFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading2.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        if (error.getResponse() != null && error.getResponse().getStatus() == 201) {
                            Snackbar.make(loading, getString(R.string.emailExists), Snackbar.LENGTH_SHORT).show();
                        } else if (error.getResponse() != null && error.getResponse().getStatus() == 202) {
                            Snackbar.make(loading, getString(R.string.userNameExists), Snackbar.LENGTH_SHORT).show();
                        } else if (error.getResponse() != null && error.getResponse().getStatus() == 203) {
                            Snackbar.make(loading, getString(R.string.mobileExists), Snackbar.LENGTH_SHORT).show();
                        } else {
                            GlobalFunctions.generalErrorMessage(loading2, activity);
                        }
                    }
                }
        );
    }

    private void subjectsApi() {
        EsaalApiConfig.getCallingAPIInterface().subjects(null,
                new Callback<ArrayList<Subject>>() {
                    @Override
                    public void success(ArrayList<Subject> subjects, Response response) {
                        int status = response.getStatus();
                        if (status == 200) {
                            subjectsList.addAll(subjects);
                            userSubjectsApi();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getResponse() != null && error.getResponse().getStatus() == 202) {
                            Snackbar.make(loading, getString(R.string.noSubjects), Snackbar.LENGTH_SHORT).show();
                        } else {
                            GlobalFunctions.generalErrorMessage(loading, activity);
                        }
                    }
                });
    }

    private void userSubjectsApi() {
        EsaalApiConfig.getCallingAPIInterface().userSubjects(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<ArrayList<Subject>>() {
                    @Override
                    public void success(ArrayList<Subject> subjects, Response response) {
                        container.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            subjectsIds.clear();
                            for (Subject value : subjects) {
                                subjectsIds.add(value.id);
                            }
                            subjectsAdapter = new StaggeredSubjectsAdapter(activity, subjectsList, subjectsIds, true);
                            fragment.subjects.setAdapter(subjectsAdapter);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getResponse() != null && error.getResponse().getStatus() == 202) {
                            Snackbar.make(loading, getString(R.string.noSubjects), Snackbar.LENGTH_SHORT).show();
                        } else {
                            GlobalFunctions.generalErrorMessage(loading, activity);
                        }
                    }
                });
    }

    private void countriesApi() {
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().countries(
                new Callback<ArrayList<Country>>() {
                    @Override
                    public void success(ArrayList<Country> country, Response response) {
                        int status = response.getStatus();
                        if (status == 200) {
                            countriesList.addAll(country);
                            subjectsApi();
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
