package app.esaal.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cuneytayyildiz.gestureimageview.GestureImageView;

import java.util.ArrayList;

import app.esaal.R;

public class ImageGestureAdapter extends PagerAdapter {
public final ArrayList<String> paintings;
public Context context;

public ImageGestureAdapter(Context context, ArrayList<String> paintings) {

        this.context=context;
        this.paintings = paintings;
        }


@Override
public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = LayoutInflater.from(context).inflate(R.layout.item_image_gesture, view, false);
        assert imageLayout != null;
final GestureImageView imageView = (GestureImageView) imageLayout.findViewById(R.id.item_image_gesture_image);

        Glide.with(context).load(paintings.get(position)).apply(new RequestOptions().placeholder(R.mipmap.placeholder_slider)).into(imageView);

        view.addView(imageLayout, 0);
        return imageLayout;
        }


@Override
public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        }

@Override
public int getCount() {
        return paintings.size();
        }

@Override
public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
        }
        }


