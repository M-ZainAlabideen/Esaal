package app.esaal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionPlan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v7.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.LocaleHelper;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.fragments.AddQuestionFragment;
import app.esaal.fragments.HomeFragment;
import app.esaal.fragments.LoginFragment;
import app.esaal.fragments.MoreFragment;
import app.esaal.fragments.MyAccountFragment;
import app.esaal.fragments.NotificationsFragment;
import app.esaal.fragments.PackagesFragment;
import app.esaal.fragments.QuestionDetailsFragment;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.notifications.Notification;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    public static ConstraintLayout appbar;
    public static LinearLayout bottomAppbar;
    public static TextView title;
    public static ImageView back;
    public static SearchView search;
    public static ImageView filter;
    public static ImageView addQuestion;
    public static ImageView home;
    public static ImageView account;
    public static ImageView notification;
    public static ImageView more;
    public static ImageView homeDash;
    public static ImageView accountDash;
    public static ImageView notifiDash;
    public static ImageView moreDash;
    public static boolean isEnglish;
    public static SessionManager sessionManager;
    public static boolean hasNewNotifications;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(LocaleHelper.onAttach(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        GlobalFunctions.setDefaultLanguage(this);
        GlobalFunctions.setUpFont(this);
        GlobalFunctions.hasNewNotificationsApi(this);
        sessionManager = new SessionManager(this);

        appbar = (ConstraintLayout) findViewById(R.id.activity_main_cl_appbar);
        bottomAppbar = (LinearLayout) findViewById(R.id.main_linearLayout_bottomAppbar);
        title = (TextView) findViewById(R.id.activity_main_tv_title);
        back = (ImageView) findViewById(R.id.activity_main_iv_back);
        search = (SearchView) findViewById(R.id.activity_main_sv_search);
        filter = (ImageView) findViewById(R.id.activity_main_iv_filter);
        addQuestion = (ImageView) findViewById(R.id.activity_main_iv_addQuestion);
        home = (ImageView) findViewById(R.id.activity_main_iv_home);
        account = (ImageView) findViewById(R.id.activity_main_iv_account);
        notification = (ImageView) findViewById(R.id.activity_main_iv_notification);
        more = (ImageView) findViewById(R.id.activity_main_iv_more);
        homeDash = (ImageView) findViewById(R.id.activity_main_iv_homeDash);
        accountDash = (ImageView) findViewById(R.id.activity_main_iv_accountDash);
        notifiDash = (ImageView) findViewById(R.id.activity_main_iv_notifiDash);
        moreDash = (ImageView) findViewById(R.id.activity_main_iv_moreDash);

        if (MainActivity.isEnglish) {
            Typeface enBold = Typeface.createFromAsset(getAssets(), "montserrat_medium.ttf");
            title.setTypeface(enBold);
        } else {
            Typeface arBold = Typeface.createFromAsset(getAssets(), "droid_arabic_kufi_bold.ttf");
            title.setTypeface(arBold);
        }

        if (sessionManager.isGuest() || sessionManager.isLoggedIn()) {
            Navigator.loadFragment(this, HomeFragment.newInstance(this), R.id.activity_main_fl_container, false);
        } else {
            Navigator.loadFragment(this, LoginFragment.newInstance(this), R.id.activity_main_fl_container, false);
        }

    }


    @OnClick(R.id.activity_main_iv_back)
    public void backCLick() {
        if (QuestionDetailsFragment.isRunning) {
            QuestionDetailsFragment.countDownTimer.cancel();
        }
        if (!search.isIconified()) {
            search.onActionViewCollapsed();
        } else {
            if (getSupportFragmentManager().findFragmentByTag("backPackage") != null && getSupportFragmentManager().findFragmentByTag("backPackage").isVisible()) {
                clearStack();
                Navigator.loadFragment(this, HomeFragment.newInstance(this), R.id.activity_main_fl_container, false);
            } else {
                onBackPressed();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            backCLick();
        }
        return true;
    }

    @OnClick(R.id.activity_main_iv_addQuestion)
    public void addQuestionCLick() {
        if (sessionManager.isGuest()) {
            Navigator.loadFragment(MainActivity.this, LoginFragment.newInstance(MainActivity.this), R.id.activity_main_fl_container, false);
        } else {
            Navigator.loadFragment(MainActivity.this, AddQuestionFragment.newInstance(MainActivity.this, "add", null), R.id.activity_main_fl_container, true);
        }
    }

    @OnClick(R.id.activity_main_ll_homeContainer)
    public void homeCLick() {
        Navigator.loadFragment(MainActivity.this, HomeFragment.newInstance(MainActivity.this), R.id.activity_main_fl_container, true);
    }

    @OnClick(R.id.activity_main_ll_accountContainer)
    public void accountCLick() {
        if (sessionManager.isGuest()) {
            Navigator.loadFragment(MainActivity.this, LoginFragment.newInstance(MainActivity.this), R.id.activity_main_fl_container, false);
        } else {
            Navigator.loadFragment(MainActivity.this, MyAccountFragment.newInstance(MainActivity.this), R.id.activity_main_fl_container, true);
        }
    }

    @OnClick(R.id.activity_main_ll_notifiContainer)
    public void notificationCLick() {
        if (sessionManager.isGuest()) {
            Navigator.loadFragment(MainActivity.this, LoginFragment.newInstance(MainActivity.this), R.id.activity_main_fl_container, false);
        } else {
            Navigator.loadFragment(MainActivity.this, NotificationsFragment.newInstance(MainActivity.this), R.id.activity_main_fl_container, true);
        }
    }

    @OnClick(R.id.activity_main_ll_moreContainer)
    public void moreCLick() {
        Navigator.loadFragment(MainActivity.this, MoreFragment.newInstance(MainActivity.this), R.id.activity_main_fl_container, true);
    }


    public static void setupAppbar(boolean hasAppbar, boolean hasBottomAppbar, boolean isHome, boolean isQuestions, String bottomSelection, String titleValue) {
        hideAppbarComponents();
        title.setText(titleValue);
        if (hasAppbar) {
            back.setVisibility(View.VISIBLE);
            appbar.setVisibility(View.VISIBLE);
        }
        if (hasBottomAppbar) {
            bottomAppbar.setVisibility(View.VISIBLE);
        }
        if (isHome) {
            back.setVisibility(View.GONE);
            if (!sessionManager.isTeacher()) {
                addQuestion.setVisibility(View.VISIBLE);
            }
        }

        if (isQuestions) {
            filter.setVisibility(View.VISIBLE);
            search.setVisibility(View.VISIBLE);
        }
        if (bottomSelection.equals("home")) {
            home.setImageResource(R.mipmap.ic_home_sel);
            account.setImageResource(R.mipmap.ic_account_unsel);
            if (hasNewNotifications)
                MainActivity.notification.setImageResource(R.mipmap.ic_notifi_new);
            else
                notification.setImageResource(R.mipmap.ic_notifi_unsel);
            more.setImageResource(R.mipmap.ic_more_unsel);
            homeDash.setVisibility(View.VISIBLE);
            accountDash.setVisibility(View.INVISIBLE);
            notifiDash.setVisibility(View.INVISIBLE);
            moreDash.setVisibility(View.INVISIBLE);
        } else if (bottomSelection.equals("account")) {
            home.setImageResource(R.mipmap.ic_home_unsel);
            account.setImageResource(R.mipmap.ic_account_sel);
            if (hasNewNotifications)
                MainActivity.notification.setImageResource(R.mipmap.ic_notifi_new);
            else
                notification.setImageResource(R.mipmap.ic_notifi_unsel);
            more.setImageResource(R.mipmap.ic_more_unsel);
            homeDash.setVisibility(View.INVISIBLE);
            accountDash.setVisibility(View.VISIBLE);
            notifiDash.setVisibility(View.INVISIBLE);
            moreDash.setVisibility(View.INVISIBLE);
        } else if (bottomSelection.equals("notifications")) {
            home.setImageResource(R.mipmap.ic_home_unsel);
            account.setImageResource(R.mipmap.ic_account_unsel);
            notification.setImageResource(R.mipmap.ic_notifi_sel);
            more.setImageResource(R.mipmap.ic_more_unsel);
            homeDash.setVisibility(View.INVISIBLE);
            accountDash.setVisibility(View.INVISIBLE);
            notifiDash.setVisibility(View.VISIBLE);
            moreDash.setVisibility(View.INVISIBLE);
        } else if (bottomSelection.equals("more")) {
            home.setImageResource(R.mipmap.ic_home_unsel);
            account.setImageResource(R.mipmap.ic_account_unsel);
            if (hasNewNotifications)
                MainActivity.notification.setImageResource(R.mipmap.ic_notifi_new);
            else
                notification.setImageResource(R.mipmap.ic_notifi_unsel);
            more.setImageResource(R.mipmap.ic_more_sel);
            homeDash.setVisibility(View.INVISIBLE);
            accountDash.setVisibility(View.INVISIBLE);
            notifiDash.setVisibility(View.INVISIBLE);
            moreDash.setVisibility(View.VISIBLE);
        }
    }

    public static void hideAppbarComponents() {
        appbar.setVisibility(View.GONE);
        bottomAppbar.setVisibility(View.GONE);
        search.setVisibility(View.GONE);
        filter.setVisibility(View.GONE);
        addQuestion.setVisibility(View.GONE);
    }

    private void clearStack() {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        gotoDetails(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        gotoDetails(getIntent());

    }

    private void gotoDetails(Intent intent) {
        if (intent.hasExtra("Id")) {
            if (intent.getStringExtra("type").equalsIgnoreCase("Q")) {
                Navigator.loadFragment(this, QuestionDetailsFragment.newInstance(this, Integer.parseInt(intent.getStringExtra("Id"))),
                        R.id.activity_main_fl_container, true);
            }
        }
    }

}
