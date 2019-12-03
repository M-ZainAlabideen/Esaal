package app.esaal.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.esaal.R;
import app.esaal.fragments.EditProfileFragment;
import app.esaal.fragments.RegistrationFragment;
import app.esaal.webservices.responses.subjects.Subject;

public class StaggeredSubjectsAdapter extends BaseAdapter {
    private Context context;
    private List<Subject> data;
    private LayoutInflater inflater = null;
    private ArrayList<Integer> subjectsIds;
    private boolean updateProfile;

    public StaggeredSubjectsAdapter(Context context, List<Subject> jArray, ArrayList<Integer> subjectsIds, boolean updateProfile) {
        this.context = context;
        this.data = jArray;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.subjectsIds = subjectsIds;
        this.updateProfile = updateProfile;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position).getName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_staggered_subject, null);
                viewHolder = new ViewHolder();
                viewHolder.view = (TextView) convertView.findViewById(R.id.item_staggered_subject_tv_subjectName);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (getItem(position) != null) {

                viewHolder.view.setText(data.get(position).getName());
                if (subjectsIds != null) {

                    for (Integer value : subjectsIds) {
                        if (value == data.get(position).id) {
                            viewHolder.view.setBackgroundResource(R.drawable.bg_staggered_shadow);
                            viewHolder.view.setTextColor(Color.parseColor("#FFFFFF"));
                        }
                    }

                }
                viewHolder.view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (viewHolder.view.getCurrentTextColor() == context.getResources().getColor(R.color.black)) {
                            viewHolder.view.setBackgroundResource(R.drawable.bg_staggered_shadow);
                            viewHolder.view.setTextColor(Color.parseColor("#FFFFFF"));
                            if (updateProfile) {
                                EditProfileFragment.subjectsIds.add(data.get(position).id);
                            } else {
                                RegistrationFragment.subjectsIds.add(data.get(position).id);
                            }
                        } else if (viewHolder.view.getCurrentTextColor() == context.getResources().getColor(R.color.white)) {
                            viewHolder.view.setBackgroundResource(R.drawable.bg_staggered_shadow_white);
                            viewHolder.view.setTextColor(Color.parseColor("#000000"));
                            if (updateProfile) {
                                EditProfileFragment.subjectsIds.remove((Integer) data.get(position).id);
                            } else {
                                RegistrationFragment.subjectsIds.remove((Integer) data.get(position).id);
                            }
                        }

                    }
                });

            }
            return viewHolder.view;
        } catch (Exception e) {
            return null;
        }
    }

    public class ViewHolder {
        TextView view;

    }


}
