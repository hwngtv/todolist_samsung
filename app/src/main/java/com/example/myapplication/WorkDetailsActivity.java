package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WorkDetailsActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    public static final String TAG = "abc123" ;

    EditText titleEditText, contentEditText;
    ImageButton saveWorkBtn, backWorkDetailBtn;
    TextView pageTitleTextView;
    String title, content, docId, date, time;
    boolean isEditMode = false;
    TextView deleteWorkTextViewBtn;
    private ImageView imageImageView;
    private Uri imageUri;

    ImageView calendarImageView, clockImageView;
    TextView dateTextView, timeTextView;

    // Variables to store selected date and time
    private Calendar selectedDate = Calendar.getInstance();
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    private AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_details);


        titleEditText = findViewById(R.id.works_title_text);
        contentEditText = findViewById(R.id.works_content_text);
        saveWorkBtn = findViewById(R.id.save_work_btn);
        backWorkDetailBtn = findViewById(R.id.back_work_detail);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteWorkTextViewBtn = findViewById(R.id.delete_work_text_view_btn);

        calendarImageView = findViewById(R.id.calendarImageView);
        clockImageView = findViewById(R.id.clockImageView);
        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);
        imageImageView = findViewById(R.id.imageImageView);

        // Receive data
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");

        if (docId != null && !docId.isEmpty()) {
            isEditMode = true;
        }

        titleEditText.setText(title);
        contentEditText.setText(content);
        dateTextView.setText(date);
        timeTextView.setText(time);

        if (isEditMode) {
            pageTitleTextView.setText("Edit your work");
            deleteWorkTextViewBtn.setVisibility(View.VISIBLE);
        } else {
            dateTextView.setText("Date");
            timeTextView.setText("Time");
        }

        // Calendar and time pickers
        calendarImageView.setOnClickListener(v -> showDatePickerDialog());
        clockImageView.setOnClickListener(v -> showTimePickerDialog());

        saveWorkBtn.setOnClickListener(v -> uploadImageToFirebaseStorage());
        backWorkDetailBtn.setOnClickListener(v -> finish());
        deleteWorkTextViewBtn.setOnClickListener(v -> confirmDeleteWork());
        imageImageView.setOnClickListener(v -> openFileChooser());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(WorkDetailsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
            selectedDate.set(year, monthOfYear, dayOfMonth);
            String selectedDateStr = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year);
            dateTextView.setText(selectedDateStr);
            selectedYear = year;
            selectedMonth = monthOfYear;
            selectedDay = dayOfMonth;
        }, currentYear, currentMonth, currentDay);

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        final Calendar currentDateTime = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(WorkDetailsActivity.this, (view, hourOfDay, minute) -> {
            selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDate.set(Calendar.MINUTE, minute);
            selectedDate.set(Calendar.SECOND, 0);

            if (selectedDate.after(currentDateTime)) {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                String selectedTimeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                timeTextView.setText(selectedTimeStr);
            } else {
                Toast.makeText(WorkDetailsActivity.this, "Please select a time in the future", Toast.LENGTH_SHORT).show();
            }
        }, currentDateTime.get(Calendar.HOUR_OF_DAY), currentDateTime.get(Calendar.MINUTE), true);

        timePickerDialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageImageView.setImageURI(imageUri);
        }
    }

    private void uploadImageToFirebaseStorage() {
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("images/" + UUID.randomUUID().toString());
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveWork(uri.toString()); // Lưu công việc với URL ảnh
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(WorkDetailsActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            saveWork(null); // Lưu công việc mà không có URL ảnh
        }
    }

    private void saveWork(String urlImage) {
        String workTitle = titleEditText.getText().toString().trim();
        String workContent = contentEditText.getText().toString().trim();
        String date = dateTextView.getText().toString().trim();
        String time = timeTextView.getText().toString().trim();

        if (TextUtils.isEmpty(workTitle) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || "Date".equals(date) || "Time".equals(time)) {
            Toast.makeText(WorkDetailsActivity.this, "Vui lòng nhập tiêu đề, ngày, và giờ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tiếp tục lưu công việc nếu tất cả thông tin hợp lệ
        Work work = new Work();
        work.setTitle(workTitle);
        work.setContent(workContent);
        work.setTimestamp(Timestamp.now());
        work.setTime(time);
        work.setDate(date);
        work.setUrlImage(urlImage); // Lưu URL ảnh vào Firestore

        saveWorkToFirebase(work);
    }

    private void saveWorkToFirebase(Work work) {
        DocumentReference documentReference;
        if (isEditMode) {
            documentReference = Utility.getCollectionReferenceForWorks().document(docId);
        } else {
            documentReference = Utility.getCollectionReferenceForWorks().document();
        }

        documentReference.set(work).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Utility.showToast(WorkDetailsActivity.this, "Work added successfully");
                //Set up the alarm for the reminder
                setReminder(work);
                finish();
            } else {
                Utility.showToast(WorkDetailsActivity.this, "Failed while adding work");
            }
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setReminder(Work work) {
        //Create a Calendar object with the selected date and time
        Calendar calendar = Calendar.getInstance();
        calendar.set(work.getYear(), work.getMonth()-1, work.getDay(), work.getHour(), work.getMinute());
        calendar.set(Calendar.SECOND, 0);

//        Intent intent = new Intent("com.example.ALARM_ACTION");
//        intent.putExtra("title", work.getTitle());
//        intent.putExtra("content", work.getContent());
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(WorkDetailsActivity.this, 0, intent,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT );
//        // Cài đặt AlarmManager
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager != null) {
//            Log.d(TAG,"preparing to notification");
//            Calendar curCalendar = Calendar.getInstance();
//            Log.d(TAG,"time: " + curCalendar.getTime()) ;
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()-3600000, pendingIntent);
//        }else {
//            Log.e(TAG, "AlarmManager is null");
//        }
        // Đặt báo thức để khởi động Service
        // Đặt công việc bằng WorkManager
        Calendar curCalendar = Calendar.getInstance();
        long timeDelay = calendar.getTimeInMillis() - curCalendar.getTimeInMillis() - 3600000;

        WorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(timeDelay, TimeUnit.MILLISECONDS) // Thay delayInMillis bằng thời gian trễ tính bằng milliseconds
                .build();

        WorkManager.getInstance().enqueue(workRequest);


    }

    private void confirmDeleteWork() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Work")
                .setMessage("Are you sure you want to delete this work?")
                .setPositiveButton("Yes", (dialog, which) -> deleteWorkFromFirebase())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteWorkFromFirebase() {
        DocumentReference documentReference = Utility.getCollectionReferenceForWorks().document(docId);
        documentReference.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Utility.showToast(WorkDetailsActivity.this, "Work deleted successfully");
                finish();
            } else {
                Utility.showToast(WorkDetailsActivity.this, "Failed to delete work");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
