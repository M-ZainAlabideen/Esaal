package app.esaal.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.FixControl;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.requests.SendMessageRequest;
import app.esaal.webservices.responses.authorization.User;
import app.esaal.webservices.responses.contact.Contact;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ContactUsFragment extends Fragment {
    public static FragmentActivity activity;
    public static ContactUsFragment fragment;
    private SessionManager sessionManager;
    private ArrayList<Contact> contactsList = new ArrayList<>();
    private String facebookLink, instagramLink, twitterLink, youtubeLink;
    private User user;

    @BindView(R.id.fragment_contact_us_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_contact_us_et_name)
    EditText name;
    @BindView(R.id.fragment_contact_us_et_phone)
    EditText phone;
    @BindView(R.id.fragment_contact_us_et_email)
    EditText email;
    @BindView(R.id.fragment_contact_us_et_subject)
    EditText subject;
    @BindView(R.id.fragment_contact_us_et_message)
    EditText message;

    @BindView(R.id.loading)
    ProgressBar loading;

    public static ContactUsFragment newInstance(FragmentActivity activity) {
        fragment = new ContactUsFragment();
        ContactUsFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_contact_us, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        MainActivity.setupAppbar(true, true, false, false, "more", getString(R.string.contactUs));
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);

        if (contactsList.size() > 0) {
            loading.setVisibility(View.GONE);
        } else {
            contactsApi();
        }

        if (user != null || sessionManager.getUserId() == 0) {
            loading.setVisibility(View.GONE);
        } else {
            userByIdApi();
        }

    }

    @OnClick(R.id.fragment_contact_us_iv_facebook)
    public void facebookClick() {
        if (facebookLink != null && !facebookLink.isEmpty()) {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, facebookLink,"link"), R.id.activity_main_fl_container, true);
        } else {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.noLink), Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.fragment_contact_us_iv_instagram)
    public void instagramClick() {
        if (instagramLink != null && !instagramLink.isEmpty()) {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, instagramLink,"link"), R.id.activity_main_fl_container, true);
        } else {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.noLink), Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.fragment_contact_us_iv_twitter)
    public void twitterClick() {
        if (twitterLink != null && !twitterLink.isEmpty()) {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, twitterLink,"link"), R.id.activity_main_fl_container, true);
        } else {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.noLink), Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.fragment_contact_us_iv_youtube)
    public void youtubeClick() {
        if (youtubeLink != null && !youtubeLink.isEmpty()) {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, youtubeLink,"link"), R.id.activity_main_fl_container, true);
        } else {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.noLink), Snackbar.LENGTH_SHORT).show();
        }
    }


    @OnClick(R.id.fragment_contact_us_tv_send)
    public void sendClick() {
        String nameStr = name.getText().toString();
        String phoneStr = phone.getText().toString();
        String emailStr = email.getText().toString();
        String subjectStr = subject.getText().toString();
        String messageStr = message.getText().toString();
        if (nameStr == null || nameStr.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.enterFullName), Snackbar.LENGTH_SHORT).show();
        } else if (phoneStr == null || phoneStr.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.enterMobile), Snackbar.LENGTH_SHORT).show();
        } else if (emailStr == null || emailStr.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.enterEmail), Snackbar.LENGTH_SHORT).show();
        } else if (subjectStr == null || subjectStr.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.enterSubject), Snackbar.LENGTH_SHORT).show();
        } else if (messageStr == null || messageStr.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.enterMessage), Snackbar.LENGTH_SHORT).show();
        } else if (!FixControl.isValidEmail(emailStr)) {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.invalidEmail), Snackbar.LENGTH_SHORT).show();
        } else if (phoneStr.length() < 8 || !FixControl.isValidPhone(phoneStr)) {
            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.invalidPhoneNumber), Snackbar.LENGTH_SHORT).show();
        } else {
            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.name = nameStr;
            sendMessageRequest.mobile = phoneStr;
            sendMessageRequest.email = emailStr;
            sendMessageRequest.subject = subjectStr;
            sendMessageRequest.message = messageStr;
            sendMessage(sendMessageRequest);
        }
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
                                fragment.user = user;
                                name.setText(fragment.user.firstName + " " + fragment.user.lastName);
                                phone.setText(fragment.user.mobile);
                                email.setText(fragment.user.email);
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        GlobalFunctions.EnableLayout(container);
                    }
                }
        );
    }

    private void contactsApi() {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().contacts(
                new Callback<ArrayList<Contact>>() {
                    @Override
                    public void success(ArrayList<Contact> contacts, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            contactsList.clear();
                            contactsList.addAll(contacts);
                            for (Contact value : contactsList) {
                                if (value.name.equals("facebook"))
                                    facebookLink = value.value;
                                else if (value.name.equals("instagram"))
                                    instagramLink = value.value;
                                else if (value.name.equals("twitter"))
                                    twitterLink = value.value;
                                else if (value.name.equals("youtube"))
                                    youtubeLink = value.value;
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        GlobalFunctions.EnableLayout(container);
                    }
                }
        );
    }

    private void sendMessage(SendMessageRequest sendMessageRequest) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().sendMessage(
                sendMessageRequest,
                new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer), getString(R.string.thanksMessage), Snackbar.LENGTH_SHORT).show();
                            Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_contact_us_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        GlobalFunctions.EnableLayout(container);
                    }
                }
        );
    }


}
