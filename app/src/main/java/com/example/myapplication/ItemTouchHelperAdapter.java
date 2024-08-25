package com.example.myapplication;


public interface ItemTouchHelperAdapter {
  //  boolean onItemMove(int fromPosition, int toPosition); // Xử lý di chuyển
    void onItemDismiss(int position); // Xử lý vuốt để xóa
}
