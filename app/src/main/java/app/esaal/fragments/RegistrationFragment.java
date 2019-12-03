package app.esaal.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.riontech.staggeredtextgridview.StaggeredTextGridView;

import java.io.File;
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
import app.esaal.webservices.responses.authorization.UserResponse;
import app.esaal.webservices.responses.countries.Country;
import app.esaal.webservices.responses.subjects.Subject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import static android.app.Activity.RESULT_OK;

public class RegistrationFragment extends Fragment {
    public static FragmentActivity activity;
    public static RegistrationFragment fragment;
    private SessionManager sessionManager;
    private ArrayList<Subject> subjectsList = new ArrayList<>();
    StaggeredSubjectsAdapter subjectsAdapter;

    private ArrayList<Country> countriesList = new ArrayList<>();
    private AlertDialog dialog;
    private boolean isGulfCountry;
    private int countryId;
    public static ArrayList<Integer> subjectsIds = new ArrayList<>();
    private TypedFile imageTypedFile, civilIdFrontFile, civilIdBackFile, certificationFile;
    private String type;

    @BindView(R.id.fragment_registration_nestedsv_scrollContainer)
    NestedScrollView scrollContainer;
    @BindView(R.id.fragment_registration_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_registration_iv_profileImg)
    ImageView profileImg;
    @BindView(R.id.fragment_registration_iv_add)
    ImageView add;
    @BindView(R.id.fragment_registration_tv_addImgTv)
    TextView addImgTv;
    @BindView(R.id.fragment_registration_et_firstName)
    EditText firstName;
    @BindView(R.id.fragment_registration_et_middleName)
    EditText middleName;
    @BindView(R.id.fragment_registration_et_lastName)
    EditText lastName;
    @BindView(R.id.fragment_registration_et_email)
    EditText email;
    @BindView(R.id.fragment_registration_et_mobile)
    EditText mobile;
    @BindView(R.id.fragment_registration_et_password)
    EditText password;
    @BindView(R.id.fragment_registration_et_userName)
    EditText userName;
    @BindView(R.id.fragment_registration_et_accountNum)
    EditText accountNum;
    @BindView(R.id.fragment_registration_et_swiftCode)
    EditText swiftCode;
    @BindView(R.id.fragment_registration_et_bankName)
    EditText bankName;
    @BindView(R.id.fragment_registration_et_bankAddress)
    EditText bankAddress;
    @BindView(R.id.fragment_registration_et_IBAN)
    EditText IBAN;
    @BindView(R.id.fragment_registration_et_personalAddress)
    EditText personalAddress;
    @BindView(R.id.fragment_registration_et_description)
    EditText description;
    @BindView(R.id.fragment_registration_tv_country)
    TextView country;

    @BindView(R.id.fragment_registration_tv_attachWord)
    TextView attachWord;
    @BindView(R.id.fragment_registration_tv_subjectsWord)
    TextView subjectsWord;
    @BindView(R.id.fragment_registration_iv_certification)
    ImageView certification;
    @BindView(R.id.fragment_registration_tv_certificationTv)
    TextView certificationTv;
    @BindView(R.id.fragment_registration_iv_civilIdFront)
    ImageView civilIdFront;
    @BindView(R.id.fragment_registration_iv_civilIdBack)
    ImageView civilIdBack;
    @BindView(R.id.fragment_registration_tv_civilIdFrontTv)
    TextView civilIdFrontTv;
    @BindView(R.id.fragment_registration_tv_civilIdBackTv)
    TextView civilIdBackTv;
    @BindView(R.id.fragment_registration_stv_subjects)
    StaggeredTextGridView subjects;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.loading2)
    ProgressBar loading2;


    public static RegistrationFragment newInstance(FragmentActivity activity) {
        fragment = new RegistrationFragment();
        RegistrationFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_registration, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(activity);
        FixControl.setupUI(container, activity);
        loading2.setVisibility(View.GONE);
        //GONE those components in general cases , then in case of Teacher registration and select country the required components will be visible
        accountNum.setVisibility(View.GONE);
        swiftCode.setVisibility(View.GONE);
        bankAddress.setVisibility(View.GONE);
        bankName.setVisibility(View.GONE);
        IBAN.setVisibility(View.GONE);
        personalAddress.setVisibility(View.GONE);

        String titleValue;
        if (sessionManager.isTeacher()) {
            titleValue = getString(R.string.teacherRegisterWord);
            subjectsAdapter = new StaggeredSubjectsAdapter(activity, subjectsList, subjectsIds,false);
            subjects.setAdapter(subjectsAdapter);
            container.setVisibility(View.GONE);
            if (countriesList.size() > 0) {
                loading.setVisibility(View.GONE);
            } else {
                countriesApi();
            }

        } else {
            titleValue = getString(R.string.studentRegisterWord);
            add.setVisibility(View.GONE);
            profileImg.setVisibility(View.GONE);
            addImgTv.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            attachWord.setVisibility(View.GONE);
            subjectsWord.setVisibility(View.GONE);
            certification.setVisibility(View.GONE);
            certificationTv.setVisibility(View.GONE);
            civilIdFront.setVisibility(View.GONE);
            civilIdBack.setVisibility(View.GONE);
            subjects.setVisibility(View.GONE);
            country.setVisibility(View.GONE);
            civilIdBack.setVisibility(View.GONE);
            civilIdFront.setVisibility(View.GONE);
            civilIdBackTv.setVisibility(View.GONE);
            civilIdFrontTv.setVisibility(View.GONE);
            description.setVisibility(View.GONE);
        }
        MainActivity.setupAppbar(true, false, false, false, "", titleValue);

    }

    private void captureImage() {
        ImagePicker.with(this)              //  Initialize ImagePicker with activity or fragment context
                .setCameraOnly(false)               //  Camera mode
                .setMultipleMode(false)              //  Select multiple images or single image
                .setFolderMode(true)                //  Folder mode
                .setShowCamera(true)                //  Show camera button
                .setMaxSize(1)                     //  Max images can be selected
                .setSavePath("ImagePicker")         //  Image capture folder name
                .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                .setKeepScreenOn(true)              //  Keep screen on when selecting images
                .start();
    }

    @OnClick(R.id.fragment_registration_tv_country)
    public void countryClick() {
        createSignUpPopUp(activity, countriesList);
    }

    @OnClick(R.id.fragment_registration_iv_profileImg)
    public void profileImgClick() {
        type = "profile";
        captureImage();
    }

    @OnClick(R.id.fragment_registration_iv_civilIdFront)
    public void civilIdFrontClick() {
        type = "front";
        captureImage();
    }

    @OnClick(R.id.fragment_registration_iv_civilIdBack)
    public void civilIdBackClick() {
        type = "back";
        captureImage();
    }

    @OnClick(R.id.fragment_registration_iv_certification)
    public void certificationClick() {
        type = "certification";
        captureImage();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            if (images != null) {
                for (Image uri : images) {
                    if (type.equals("profile")) {
                        imageTypedFile = new TypedFile("image/*", new File(uri.getPath()));
                        Glide.with(activity)
                                .load(uri.getPath())
                                .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                                        .error(R.mipmap.placeholder_attach))
                                .into(profileImg);
                    } else if (type.equals("back")) {
                        civilIdBackFile = new TypedFile("image/*", new File(uri.getPath()));
                        Glide.with(activity)
                                .load(uri.getPath())
                                .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                                        .error(R.mipmap.placeholder_attach))
                                .into(civilIdBack);
                    } else if (type.equals("front")) {
                        civilIdFrontFile = new TypedFile("image/*", new File(uri.getPath()));
                        Glide.with(activity)
                                .load(uri.getPath())
                                .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                                        .error(R.mipmap.placeholder_attach))
                                .into(civilIdFront);
                    } else if (type.equals("certification")) {
                        certificationFile = new TypedFile("image/*", new File(uri.getPath()));
                        Glide.with(activity)
                                .load(uri.getPath())
                                .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                                        .error(R.mipmap.placeholder_attach))
                                .into(certification);
                    }
                }
            }
        }

    }


    @OnClick(R.id.fragment_registration_tv_done)
    public void doneClick() {
        String firstNameStr = firstName.getText().toString();
        String middleNameStr = middleName.getText().toString();
        String lastNameStr = lastName.getText().toString();
        String userNameStr = userName.getText().toString();
        String emailStr = email.getText().toString();
        String mobileStr = mobile.getText().toString();
        String passwordStr = password.getText().toString();
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
        } else if (passwordStr == null || passwordStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterPassword), Snackbar.LENGTH_SHORT).show();
        } else if (!FixControl.isValidEmail(emailStr)) {
            Snackbar.make(loading, getString(R.string.invalidEmail), Snackbar.LENGTH_SHORT).show();
        } else if (passwordStr.length() < 3) {
            Snackbar.make(loading, getString(R.string.invalidPassword), Snackbar.LENGTH_SHORT).show();
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
                } else if (civilIdFrontFile == null) {
                    Snackbar.make(loading, getString(R.string.selectCivilIdFrontImg), Snackbar.LENGTH_SHORT).show();
                } else if (civilIdBackFile == null) {
                    Snackbar.make(loading, getString(R.string.selectCivilIdBackImg), Snackbar.LENGTH_SHORT).show();
                } else if (certificationFile == null) {
                    Snackbar.make(loading, getString(R.string.selectCertificationImg), Snackbar.LENGTH_SHORT).show();
                } else if (subjectsIdsStr == null || subjectsIdsStr.isEmpty()) {
                    Snackbar.make(loading, getString(R.string.selectSubjects), Snackbar.LENGTH_SHORT).show();
                } else {
                    teacherSignUpApi(
                            firstNameStr, middleNameStr, lastNameStr, mobileStr, emailStr, userNameStr, passwordStr
                            , subjectsIdsStr, swiftCodeStr, bankNameStr, accountNumStr
                            , countryId, IBANStr, bankAddressStr, personalAddressStr, descriptionStr,
                            imageTypedFile, civilIdFrontFile, civilIdBackFile, certificationFile);
                }

            } else {
                StudentRequest student = new StudentRequest();
                student.firstName = firstNameStr;
                student.middleName = middleNameStr;
                student.lastName = lastNameStr;
                student.email = emailStr;
                student.mobile = mobileStr;
                student.password = passwordStr;
                student.username = userNameStr;
                studentSignUp(student);
            }
        }
    }


    public void createSignUpPopUp(final Context context, final ArrayList<Country> countriesList) {
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
                swiftCode.setVisibility(View.VISIBLE);
                bankName.setVisibility(View.VISIBLE);
                if (isGulfCountry) {
                    IBAN.setVisibility(View.VISIBLE);

                    accountNum.setVisibility(View.GONE);
                    bankAddress.setVisibility(View.GONE);
                    personalAddress.setVisibility(View.GONE);
                } else {
                    accountNum.setVisibility(View.VISIBLE);
                    bankAddress.setVisibility(View.VISIBLE);
                    personalAddress.setVisibility(View.VISIBLE);

                    IBAN.setVisibility(View.GONE);

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

    private void studentSignUp(StudentRequest student) {
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().studentSignUp(
                student, new Callback<UserResponse>() {
                    @Override
                    public void success(UserResponse userResponse, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            GlobalFunctions.clearLastStack(activity);
                            setupSession(userResponse);
                            Navigator.loadFragment(activity, PackagesFragment.newInstance(activity,"registration"), R.id.activity_main_fl_container, false);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        int failureStatus = error.getResponse().getStatus();
                        if (failureStatus == 201) {
                            Snackbar.make(loading, getString(R.string.emailExists), Snackbar.LENGTH_SHORT).show();
                        } else if (failureStatus == 202) {
                            Snackbar.make(loading, getString(R.string.userNameExists), Snackbar.LENGTH_SHORT).show();
                        } else if (failureStatus == 203) {
                            Snackbar.make(loading, getString(R.string.mobileExists), Snackbar.LENGTH_SHORT).show();
                        } else {
                            GlobalFunctions.generalErrorMessage(loading, activity);
                        }
                    }
                }
        );
    }

    private void teacherSignUpApi(String firstName, String middleName,
                                  String lastName, String phone,
                                  String email, String userName, String password,
                                  String subjectsIds, String swiftCode, String bankName
            , String accountNum, int countryId, String IBAN, String bankAddress, String personalAddress,
                                  String description, TypedFile userImage,
                                  TypedFile civilIdFront, TypedFile civilIdBack, TypedFile certification) {
        loading2.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().teacherSignUp(
                firstName, middleName, lastName, phone, email, userName, password, subjectsIds,
                countryId, IBAN, swiftCode, accountNum, bankName, bankAddress, personalAddress,
                description,
                userImage, civilIdFront, civilIdBack, certification,
                new Callback<UserResponse>() {
                    @Override
                    public void success(UserResponse userResponse, Response response) {
                        int status = response.getStatus();
                        loading2.setVisibility(View.GONE);
                        if (status == 200) {
                            setupSession(userResponse);
                            Snackbar.make(loading, getString(R.string.accountCreatedSuccessfully), Snackbar.LENGTH_SHORT).show();
                            Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading2.setVisibility(View.GONE);
                        int failureStatus = error.getResponse().getStatus();
                        if (failureStatus == 201) {
                            Snackbar.make(loading, getString(R.string.emailExists), Snackbar.LENGTH_SHORT).show();
                        } else if (failureStatus == 202) {
                            Snackbar.make(loading, getString(R.string.userNameExists), Snackbar.LENGTH_SHORT).show();
                        } else if (failureStatus == 203) {
                            Snackbar.make(loading, getString(R.string.mobileExists), Snackbar.LENGTH_SHORT).show();
                        } else {
                            GlobalFunctions.generalErrorMessage(loading2, activity);
                        }
                    }
                }

        );
    }

    private void setupSession(UserResponse userResponse) {
        sessionManager.setUserToken(userResponse.token);
        sessionManager.setUserId(userResponse.user.id);
        sessionManager.setTeacher(userResponse.user.isTeacher);
        sessionManager.setBalanceRequest(userResponse.user.isRequest);
        sessionManager.LoginSession();
    }

    private void subjectsApi() {
        EsaalApiConfig.getCallingAPIInterface().subjects(null,
                new Callback<ArrayList<Subject>>() {
                    @Override
                    public void success(ArrayList<Subject> subjects, Response response) {
                        container.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            subjectsList.addAll(subjects);
                            subjectsAdapter = new StaggeredSubjectsAdapter(activity, subjectsList,null,false);
                            fragment.subjects.setAdapter(subjectsAdapter);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        int failureStatus = error.getResponse().getStatus();
                        if (failureStatus == 202) {
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

                    }
                }
        );
    }

}

