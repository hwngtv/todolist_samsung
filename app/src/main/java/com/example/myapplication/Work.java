package com.example.myapplication;


import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Work {
    String title;
    String content;
    Timestamp timestamp;
    String date;
    String time;
    String urlImage;

    public String getUrlImage() {
        return urlImage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    //private String id;
//    public String getId() {
//        return id;
//    }
    public Work() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getDay() {
        Calendar calendar = getParsedDate();
        if (calendar != null) {
            return calendar.get(Calendar.DAY_OF_MONTH);
        }
        return -1; // Return -1 if parsing failed
    }

    public int getMonth() {
        Calendar calendar = getParsedDate();
        if (calendar != null) {
            return calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH starts from 0
        }
        return -1; // Return -1 if parsing failed
    }

    public int getYear() {
        Calendar calendar = getParsedDate();
        if (calendar != null) {
            return calendar.get(Calendar.YEAR);
        }
        return -1; // Return -1 if parsing failed
    }

    private Calendar getParsedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            if (date != null) {
                calendar.setTime(sdf.parse(date));
                return calendar;
            }
        } catch (ParseException e) {
            e.printStackTrace(); // Handle the exception appropriately in production code
        }

        return null; // Return null if parsing failed
    }

    public int getHour() {
        if (time != null && !time.isEmpty()) {
            String[] parts = time.split(":");
            if (parts.length == 2) {
                try {
                    return Integer.parseInt(parts[0]); // Lấy giờ từ chuỗi time
                } catch (NumberFormatException e) {
                    e.printStackTrace(); // Xử lý lỗi nếu việc chuyển đổi thất bại
                }
            }
        }
        return -1; // Trả về -1 nếu có lỗi
    }

    public int getMinute() {
        if (time != null && !time.isEmpty()) {
            String[] parts = time.split(":");
            if (parts.length == 2) {
                try {
                    return Integer.parseInt(parts[1]); // Lấy phút từ chuỗi time
                } catch (NumberFormatException e) {
                    e.printStackTrace(); // Xử lý lỗi nếu việc chuyển đổi thất bại
                }
            }
        }
        return -1; // Trả về -1 nếu có lỗi
    }


}
