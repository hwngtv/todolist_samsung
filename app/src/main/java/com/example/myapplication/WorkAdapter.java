package com.example.myapplication;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class WorkAdapter extends FirestoreRecyclerAdapter<Work, WorkAdapter.WorkViewHolder> implements ItemTouchHelperAdapter{

    private Context context;
    private TextView monthTextView;
    private TextView dateTextView;
    private TextView yearTextView;

    public WorkAdapter(@NonNull FirestoreRecyclerOptions<Work> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkViewHolder holder, int position, @NonNull Work work) {
        holder.titleTextView.setText(work.getTitle());
        holder.contentTextView.setText(work.getContent());
        holder.timestampTextView.setText(Utility.timestampToString(work.getTimestamp()));
        holder.monthTextView.setText(getMonthString(work.getMonth()));
        holder.yearTextView.setText(String.valueOf(work.getYear()));
        holder.dateTextView.setText(String.valueOf(work.getDay()));

        // Đếm số lần click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            private int clickCount = 0;
            private Handler handler = new Handler();
            private final int DOUBLE_CLICK_DELAY = 300; // Thời gian giữa 2 lần click

            @Override
            public void onClick(View v) {
                clickCount++;

                // Tạo Runnable để reset clickCount nếu không có lần click thứ hai
                Runnable resetClickCount = () -> clickCount = 0;

                // Nếu click lần thứ hai trong khoảng thời gian cho phép, mở chi tiết
                if (clickCount == 2) {
                    // Hủy Runnable reset click nếu người dùng đã click 2 lần
                    handler.removeCallbacks(resetClickCount);
                    clickCount = 0; // Reset lại bộ đếm click

                    // Chuyển sang màn hình chi tiết
                    Intent intent = new Intent(context, WorkDetailsActivity.class);
                    intent.putExtra("title", work.getTitle());
                    intent.putExtra("content", work.getContent());
                    intent.putExtra("date",work.getDate());
                    intent.putExtra("time",work.getTime());
                    String docId = getSnapshots().getSnapshot(position).getId();
                    intent.putExtra("docId", docId);
                    context.startActivity(intent);
                } else {
                    // Nếu chỉ click một lần, đợi xem người dùng có click lần thứ hai không
                    handler.postDelayed(resetClickCount, DOUBLE_CLICK_DELAY);
                }
            }
        });
    }


    @NonNull
    @Override
    public WorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_work_item, parent, false);
        return new WorkViewHolder(view);
    }

    @Override
    public void onItemDismiss(int position) {
        String docId = getSnapshots().getSnapshot(position).getId();
        Utility.getCollectionReferenceForWorks().document(docId).delete()
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật lại toàn bộ danh sách sau khi xóa
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                    Utility.showToast(context, "Remove succeeded");
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(context, "Failed to remove");
                });
    }

    class WorkViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, timestampTextView,monthTextView,dateTextView,yearTextView;

        public WorkViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.work_title_text_view);
            contentTextView = itemView.findViewById(R.id.work_content_text_view);
            timestampTextView = itemView.findViewById(R.id.work_timestamp_text_view);
            monthTextView = itemView.findViewById(R.id.monthTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            yearTextView = itemView.findViewById(R.id.yearTextView);
        }
    }
    private String getMonthString(int month) {
        String[] monthArray = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        if (month >= 1 && month <= 12) {
            return monthArray[month - 1];
        } else {
            return "";
        }
    }
}
