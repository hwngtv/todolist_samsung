package com.example.myapplication;
import java.util.ArrayList;
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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Collections;
import java.util.List;
public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder> implements ItemTouchHelperAdapter{

    private Context context;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note note) {
        holder.titleTextView.setText(note.getTitle());
        holder.contentTextView.setText(note.getContent());
        holder.timestampTextView.setText(Utility.timestampToString(note.getTimestamp()));

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
                    Intent intent = new Intent(context, NoteDetailsActivity.class);
                    intent.putExtra("title", note.getTitle());
                    intent.putExtra("content", note.getContent());
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
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onItemDismiss(int position) {
        String docId = getSnapshots().getSnapshot(position).getId();
        Utility.getCollectionReferenceForNotes().document(docId).delete()
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

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, timestampTextView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title_text_view);
            contentTextView = itemView.findViewById(R.id.note_content_text_view);
            timestampTextView = itemView.findViewById(R.id.note_timestamp_text_view);
        }
    }
}
