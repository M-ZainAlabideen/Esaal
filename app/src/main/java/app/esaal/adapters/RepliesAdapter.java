package app.esaal.adapters;

import android.content.Context;
import android.support.annotation.BinderThread;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import app.esaal.R;
import app.esaal.classes.FixControl;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.responses.questionsAndReplies.Attachment;
import app.esaal.webservices.responses.questionsAndReplies.Reply;
import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * the adapter of replies items of each question*/
public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.viewHolder> {
    Context context;
    ArrayList<Reply> repliesList;
    SessionManager sessionManager;
    OnItemClickListener listener;
    //variables for saving image and video Url  of each item
    String imageUrl;
    String videoUrl;

    public RepliesAdapter(Context context,
                          ArrayList<Reply> repliesList, OnItemClickListener listener) {
        this.context = context;
        this.repliesList = repliesList;
        this.listener = listener;
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_reply_iv_avatarImg)
        ImageView avatarImg;
        @BindView(R.id.item_reply_tv_title)
        TextView title;
        @BindView(R.id.item_reply_v_reply)
        View reply;
        @BindView(R.id.item_reply_tv_replyText)
        TextView replyText;
        @BindView(R.id.item_reply_iv_replyImg)
        ImageView replyImg;
        @BindView(R.id.item_reply_tv_replyWord)
        TextView replyWord;
        @BindView(R.id.item_reply_iv_imgAttach)
        ImageView imgAttach;
        @BindView(R.id.item_reply_cl_videoContainer)
        ConstraintLayout videoContainer;
        @BindView(R.id.item_reply_iv_videoAttach)
        ImageView videoAttach;
        @BindView(R.id.item_reply_iv_play)
        ImageView play;
        @BindView(R.id.item_reply_iv_like)
        ImageView like;
        @BindView(R.id.item_reply_iv_dislike)
        ImageView dislike;
        @BindView(R.id.item_reply_v_separatedLine)
        View separatedLine;
        @BindView(R.id.item_reply_pb_loading)
        ProgressBar loading;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            sessionManager = new SessionManager(context);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public RepliesAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_reply, viewGroup, false);
        return new RepliesAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RepliesAdapter.viewHolder viewHolder, final int position) {
        //each reply (item) has loading(ProgressBar)
        viewHolder.loading.setVisibility(View.GONE);

        //in case of the userId equal the userId of reply >> the user can not reply fo himself
        if (sessionManager.getUserId() == repliesList.get(position).userId) {
            viewHolder.replyImg.setVisibility(View.GONE);
            viewHolder.replyWord.setVisibility(View.GONE);
        }

        if (position == repliesList.size() - 1) {
            viewHolder.separatedLine.setVisibility(View.GONE);
        }
        viewHolder.replyText.setText(repliesList.get(position).replyMessage);


        if ((repliesList.get(position).userId == sessionManager.getUserId() && sessionManager.isTeacher()) ||
                (repliesList.get(position).userId != sessionManager.getUserId() && !sessionManager.isTeacher())) {
            viewHolder.title.setText(context.getString(R.string.esaalTeacher));
            viewHolder.title.setTextColor(context.getResources().getColor(R.color.green));
            viewHolder.avatarImg.setImageResource(R.mipmap.ic_teacher);
        } else if ((repliesList.get(position).userId == sessionManager.getUserId() && !sessionManager.isTeacher()) ||
                (repliesList.get(position).userId != sessionManager.getUserId() && sessionManager.isTeacher())) {
            viewHolder.title.setText(context.getString(R.string.esaalStudent));
            viewHolder.title.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            viewHolder.avatarImg.setImageResource(R.mipmap.ic_student);
        }
        if (sessionManager.isTeacher() || (!sessionManager.isTeacher() && (repliesList.get(position).userId == sessionManager.getUserId()))) {
            viewHolder.like.setVisibility(View.GONE);
            viewHolder.dislike.setVisibility(View.GONE);
        }
        if (repliesList.get(position).isLiked) {
            viewHolder.like.setImageResource(R.mipmap.ic_like_sel);
        } else {
            viewHolder.like.setImageResource(R.mipmap.ic_like_unsel);
        }

        if (repliesList.get(position).isDisliked) {
            viewHolder.dislike.setImageResource(R.mipmap.ic_dislike_sel);
        } else {
            viewHolder.dislike.setImageResource(R.mipmap.ic_dislike_unsel);
        }

        for (Attachment value : repliesList.get(position).attachments) {
            if (value.fileUrl != null && !value.fileUrl.isEmpty()) {
                if (value.fileType.equals("i")) {
                    loadImages(value.fileUrl, viewHolder.imgAttach);
                    imageUrl = value.fileUrl;
                } else if (value.fileType.equals("v")) {
                    loadImages(value.videoFrameUrl, viewHolder.videoAttach);
                    videoUrl = value.fileUrl;
                }
            }
        }

        if (imageUrl == null || imageUrl.isEmpty()) {
            viewHolder.imgAttach.setVisibility(View.GONE);
        }
        if (videoUrl == null || videoUrl.isEmpty()) {
            viewHolder.videoContainer.setVisibility(View.GONE);
        }

        //reset imageUrl value and videoUrl value
        imageUrl = null;
        videoUrl = null;

        viewHolder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.replyClick(position);
            }
        });

        viewHolder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.likeClick(position, viewHolder.like, viewHolder.dislike, viewHolder.loading);
            }
        });

        viewHolder.dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.dislikeClick(position, viewHolder.like, viewHolder.dislike, viewHolder.loading);
            }
        });

        viewHolder.imgAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Attachment value : repliesList.get(position).attachments) {
                    if (value.fileUrl != null && !value.fileUrl.isEmpty()) {
                        if (value.fileType.equals("i")) {
                            imageUrl = value.fileUrl;
                        }
                    }
                }
                ArrayList<String> images = new ArrayList<>();
                images.add(imageUrl);
                listener.imgAttach(position, images);
            }
        });

        viewHolder.videoAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Attachment value : repliesList.get(position).attachments) {
                    if (value.fileUrl != null && !value.fileUrl.isEmpty()) {
                        if (value.fileType.equals("v")) {
                            videoUrl = value.fileUrl;
                        }
                    }
                }
                listener.videoAttach(position, videoUrl);
            }
        });


    }

    private void loadImages(String url, ImageView image) {
        int Width = FixControl.getImageWidth(context, R.mipmap.placeholder_attach);
        int Height = FixControl.getImageHeight(context, R.mipmap.placeholder_attach);
        image.getLayoutParams().height = Height;
        image.getLayoutParams().width = Width;

        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                        .error(R.mipmap.placeholder_attach))
                .into(image);
    }


    @Override
    public int getItemCount() {
        return repliesList.size();
    }


    public interface OnItemClickListener {
        void replyClick(int position);

        void likeClick(int position, ImageView likeImg, ImageView dislikeImg, ProgressBar loading);

        void dislikeClick(int position, ImageView likeImg, ImageView dislikeImg, ProgressBar loading);

        void imgAttach(int position, ArrayList<String> images);

        void videoAttach(int position, String url);
    }
}


