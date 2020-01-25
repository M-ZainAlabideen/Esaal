package app.esaal.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.webservices.responses.payments.Payment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.viewHolder> {
    Context context;
    ArrayList<Payment> paymentsList;
    public PaymentsAdapter(Context context,ArrayList<Payment> paymentsList) {
        this.context = context;
        this.paymentsList = paymentsList;
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_payment_tv_packageName)
        TextView packageName;
        @BindView(R.id.item_payment_tv_cost)
        TextView cost;
        @BindView(R.id.item_payment_tv_date)
        TextView date;
        @BindView(R.id.item_payment_tv_fromDate)
        TextView fromDate;
        @BindView(R.id.item_payment_tv_toDate)
        TextView toDate;
        @BindView(R.id.item_payment_tv_paidWord)
        TextView paidWord;
        @BindView(R.id.item_payment_tv_renewWord)
        TextView renewWord;
        @BindView(R.id.item_payment_tv_fromWord)
        TextView formWord;
        @BindView(R.id.item_payment_tv_toWord)
        TextView toWord;
        @BindView(R.id.item_payment_tv_remainsQuestions)
        TextView remainsQuestions;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public PaymentsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View childView = LayoutInflater.from(context).inflate(R.layout.item_payment, viewGroup, false);
        return new PaymentsAdapter.viewHolder(childView);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentsAdapter.viewHolder viewHolder, final int position) {
        viewHolder.packageName.setText(paymentsList.get(position).paymentPackage.getName());
        viewHolder.cost.setText(paymentsList.get(position).paymentPackage.price+".000"+" "+context.getString(R.string.currency));
        viewHolder.date.setText(paymentsList.get(position).getCreationDate());
        viewHolder.fromDate.setText(paymentsList.get(position).getPaymentDate());
        viewHolder.toDate.setText(paymentsList.get(position).getEndDate());
        String questionWord = "";
        if (paymentsList.get(position).remainQuestionNumber <= 10)
            questionWord = context.getString(R.string.questions);
        else
            questionWord = context.getString(R.string.question);
        viewHolder.remainsQuestions.setText(context.getString(R.string.remainsQuestions)+": "+
                paymentsList.get(position).remainQuestionNumber + " "+ questionWord);

        if (MainActivity.isEnglish) {
            Typeface enBold = Typeface.createFromAsset(context.getAssets(), "montserrat_medium.ttf");
            viewHolder.packageName.setTypeface(enBold);
            viewHolder.cost.setTypeface(enBold);
            viewHolder.fromDate.setTypeface(enBold);
            viewHolder.toDate.setTypeface(enBold);

        } else {
            Typeface arBold = Typeface.createFromAsset(context.getAssets(), "cairo_bold.ttf");
            Typeface cairo = Typeface.createFromAsset(context.getAssets(), "cairo_regular.ttf");

            viewHolder.packageName.setTypeface(arBold);
            viewHolder.cost.setTypeface(arBold);
            viewHolder.fromDate.setTypeface(arBold);
            viewHolder.toDate.setTypeface(arBold);
            viewHolder.paidWord.setTypeface(cairo);
            viewHolder.renewWord.setTypeface(cairo);
            viewHolder.formWord.setTypeface(cairo);
            viewHolder.toWord.setTypeface(cairo);

        }
    }

    @Override
    public int getItemCount() {
        return paymentsList.size();
    }
}