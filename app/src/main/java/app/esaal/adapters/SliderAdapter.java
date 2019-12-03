package app.esaal.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import app.esaal.R;
import app.esaal.classes.Navigator;
import app.esaal.fragments.ImageGestureFragment;
import app.esaal.webservices.responses.slider.Slider;

public class SliderAdapter extends PagerAdapter {
    Context context;
    ArrayList<Slider> sliderList;

    public SliderAdapter(Context context, ArrayList<Slider> sliderList) {
        this.context = context;
        this.sliderList = sliderList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_slider, container, false);
        ImageView sliderImage = (ImageView) childView.findViewById(R.id.item_slider_iv_sliderImg);
        if (sliderList.get(position).imagePath != null
                && !sliderList.get(position).imagePath.isEmpty()) {
            Glide.with(context.getApplicationContext()).load(sliderList.get(position).imagePath).apply(new RequestOptions()
                    .placeholder(R.mipmap.placeholder_slider)).into(sliderImage);
        }

        sliderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> paths = new ArrayList<>();
                for (Slider value : sliderList) {
                    paths.add(value.imagePath);
                }
               Navigator.loadFragment((FragmentActivity) context, ImageGestureFragment.newInstance((FragmentActivity) context, paths, position), R.id.activity_main_fl_container, true);
            }
        });

        container.addView(childView, 0);
        return childView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return sliderList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return PagerAdapter.POSITION_NONE;
    }

}

