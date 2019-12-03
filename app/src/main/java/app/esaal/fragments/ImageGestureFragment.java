package app.esaal.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duolingo.open.rtlviewpager.RtlViewPager;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.ImageGestureAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageGestureFragment extends Fragment {
    static FragmentActivity activity;
    static ImageGestureFragment fragment;
    private ArrayList<String> paintings=new ArrayList<>();
    int position;

    @BindView(R.id.paintings_view_pager)
    RtlViewPager viewPager;
    String photoUrl;
    public static ImageGestureFragment newInstance(FragmentActivity activity,ArrayList<String> paintings,int position) {
        fragment = new ImageGestureFragment();
        ImageGestureFragment.activity = activity;
        fragment.paintings = paintings;
        fragment.position = position;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_image_gesture, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //hidden MainAppBarLayout and Ads for making the image fullScreen
        //paintings.add(photoUrl);
        MainActivity.setupAppbar(false,false,false,false,"","");
        viewPager.setAdapter(new ImageGestureAdapter(activity, paintings));
        viewPager.setCurrentItem(position);

    }
}
