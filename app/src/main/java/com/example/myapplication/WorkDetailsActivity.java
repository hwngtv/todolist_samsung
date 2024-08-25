package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.Calendar;
import java.util.Locale;

public class WorkDetailsActivity extends AppCompatActivity {

    EditText titleEditText,contentEditText;
    ImageButton saveWorkBtn,backWorkDetailBtn;
    TextView pageTitleTextView;
    String title,content,docId,date,time;
    boolean isEditMode = false;
    TextView deleteWorkTextViewBtn;

    // Variables to store selected date and time
    private Calendar selectedDate = Calendar.getInstance();
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    private int year, month, day, hour, minute;
    ImageView calendarImageView,clockImageView;
    TextView dateTextView,timeTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_details);

        titleEditText = findViewById(R.id.works_title_text);
        contentEditText = findViewById(R.id.works_content_text);
        saveWorkBtn = findViewById(R.id.save_work_btn);
        backWorkDetailBtn = findViewById(R.id.back_work_detail);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteWorkTextViewBtn  = findViewById(R.id.delete_work_text_view_btn);

        calendarImageView = findViewById(R.id.calendarImageView);
        clockImageView = findViewById(R.id.clockImageView);
        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);

        //receive data
        title = getIntent().getStringExtra("title");
        content= getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");

        if(docId!=null && !docId.isEmpty()){
            isEditMode = true;
        }

        titleEditText.setText(title);
        contentEditText.setText(content);
        dateTextView.setText(date);
        timeTextView.setText(time);
        if(isEditMode){
            pageTitleTextView.setText("Edit your work");
            deleteWorkTextViewBtn.setVisibility(View.VISIBLE);
        }else{
            dateTextView.setText("Date");
            timeTextView.setText("Time");
        }


        // Set click listener for the calendar image view
        calendarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current date
                final Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                // Create a DatePickerDialog to allow the user to select a date
                DatePickerDialog datePickerDialog = new DatePickerDialog(WorkDetailsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set the selected date in the selectedDate Calendar object
                        selectedDate.set(year, monthOfYear, dayOfMonth);

                        // Display the selected date
                        String selectedDateStr = dayOfMonth + "/0" + (monthOfYear + 1) + "/" + year;
                        dateTextView.setText(selectedDateStr);

                        // Store the selected date in variables
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;
                    }
                }, currentYear, currentMonth, currentDay);

                // Show the DatePickerDialog
                datePickerDialog.show();
            }
        });

        // Set click listener for the clock image view
        clockImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current date and time
                final Calendar currentDateTime = Calendar.getInstance();

                // Create a TimePickerDialog to allow the user to select a time
                TimePickerDialog timePickerDialog = new TimePickerDialog(WorkDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Set the selected time in the selectedDate Calendar object
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDate.set(Calendar.MINUTE, minute);
                        selectedDate.set(Calendar.SECOND, 0);  // Reset seconds to 0 for accurate comparison

                        // Compare the selected date and time with the current date and time
                        if (selectedDate.after(currentDateTime)) {
                            // The selected date and time is in the future
                            selectedHour = hourOfDay;
                            selectedMinute = minute;

                            // Display the selected time
                            String selectedTimeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                            timeTextView.setText(selectedTimeStr);
                        } else {
                            // The selected time is in the past or too close to the current time
                            Toast.makeText(WorkDetailsActivity.this, "Please select a time in the future", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, currentDateTime.get(Calendar.HOUR_OF_DAY), currentDateTime.get(Calendar.MINUTE), true);

                // Show the TimePickerDialog
                timePickerDialog.show();
            }
        });


        saveWorkBtn.setOnClickListener( (v)-> saveWork());
        backWorkDetailBtn.setOnClickListener((v)->backworkDetail());
        deleteWorkTextViewBtn.setOnClickListener((v)-> deleteWorkFromFirebase() );

    }
    void backworkDetail(){
        finish();
    }
    void saveWork(){
        String workTitle = titleEditText.getText().toString();
        String workContent = contentEditText.getText().toString();
        String date = dateTextView.getText().toString().trim();
        String time = timeTextView.getText().toString().trim();
//        if(workTitle==null || workTitle.isEmpty() ){
//            titleEditText.setError("Title is required");
//            return;
//        }
        // Validate the input
        if (TextUtils.isEmpty(workTitle) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            // Show error message if title, date, or time is empty
            Toast.makeText(WorkDetailsActivity.this, "Please enter both title, date and time", Toast.LENGTH_SHORT).show();
            return;
        }
        Work work = new Work();
        work.setTitle(workTitle);
        work.setContent(workContent);
        work.setTimestamp(Timestamp.now());
        work.setTime(time);
        work.setDate(date);

        saveWorkToFirebase(work);

    }

    void saveWorkToFirebase(Work work){
        DocumentReference documentReference;
        if(isEditMode){
            //update the work
            documentReference = Utility.getCollectionReferenceForWorks().document(docId);
        }else{
            //create new work
            documentReference = Utility.getCollectionReferenceForWorks().document();
        }



        documentReference.set(work).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //work is added
                    Utility.showToast(WorkDetailsActivity.this,"Work added successfully");
                    finish();
                }else{
                    Utility.showToast(WorkDetailsActivity.this,"Failed while adding work");
                }
            }
        });

    }

    void deleteWorkFromFirebase(){
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForWorks().document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //work is deleted
                    Utility.showToast(WorkDetailsActivity.this,"Work deleted successfully");
                    finish();
                }else{
                    Utility.showToast(WorkDetailsActivity.this,"Failed while deleting work");
                }
            }
        });
    }
}