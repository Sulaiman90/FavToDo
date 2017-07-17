package com.ms.favtodo;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.model.AlertDialogFragment;
import com.ms.favtodo.model.TaskOperation;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

public class NewTask extends AppCompatActivity {

    private static final String TAG = "FavDo_NewTask";
    private static EditText mTitleText;
    private static EditText mDateText;
    private static EditText mTimeText;
    private static TextView mTaskStatus;
    private static CheckBox mTaskDone;

    private static ImageButton mClearDate;
    private static ImageButton mClearTime;

    private TaskDbHelper dbHelper;
    private static TaskOperation taskOperation;

    private Boolean newTask = false;

    private DialogFragment mDialog;

    private static long dateInMillis = 0;
    private static int taskHour = -1;
    private static int taskMinute = -1;

    private static String dateText;
    private static String timeText;
    private int taskId = 0;

    private static String[] date = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // toolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.red));
       // getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));

        dbHelper = new TaskDbHelper(this);

        taskOperation = new TaskOperation(this);

        mTitleText = (EditText) findViewById(R.id.title);
        mDateText = (EditText) findViewById(R.id.dateText);
        mTimeText = (EditText) findViewById(R.id.timeText);
        mTaskStatus = (TextView) findViewById(R.id.task_status);
        mTaskDone = (CheckBox) findViewById(R.id.task_finished);

        mClearDate = (ImageButton) findViewById(R.id.clear_date);
        mClearTime = (ImageButton) findViewById(R.id.clear_time);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mark_as_done);

        Intent intent = getIntent();
        newTask = intent.getBooleanExtra("NewTask", false);

        mClearDate.setVisibility(View.GONE);
        mTimeText.setVisibility(View.GONE);
        mClearTime.setVisibility(View.GONE);
        //Log.d(TAG,"New Task "+newTask);

        if (newTask) {
            linearLayout.setVisibility(View.GONE);
            mTimeText.setVisibility(View.INVISIBLE);
        } else {
            getSupportActionBar().setTitle("");
            Bundle extras = getIntent().getExtras();
            mTitleText.setText(extras.getString("title"));
            int done = extras.getInt("doneOrNot");
            taskId  = extras.getInt("id");
            dateInMillis = extras.getLong("timeInMs");
            taskHour = extras.getInt("hour");
            taskMinute = extras.getInt("minute");

            dateText = extras.getString("date");
            mDateText.setText(dateText);
            timeText = extras.getString("time");
            mTimeText.setText(timeText);

            if (done == 1) {
                mTaskDone.setChecked(true);
            } else {
                mTaskDone.setChecked(false);
            }

            Log.d(TAG, "Date " + extras.getString("date") +" time "+timeText);

            if (!dateText.matches("")) {
                showHideButtons(mClearDate, true);
                checkIfDatePassed(dateInMillis,dateText);
                mTimeText.setVisibility(View.VISIBLE);
                if (!timeText.matches("")) {
                    checkIfTimePassed(taskHour,taskMinute);
                    showHideButtons(mClearTime, true);
                }
            }

        }

        mTitleText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // mTitleText.setFocusable(true);
            }
        });

        mDateText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                taskOperation.hideKeyboard(NewTask.this);
                showDatePickerDialog();
            }
        });

        mTimeText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                taskOperation.hideKeyboard(NewTask.this);
                showTimePickerDialog();
            }
        });

        mTaskDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                taskOperation.hideKeyboard(NewTask.this);
                String stringDone = getResources().getString(R.string.task_finished_ques);
                String stringNotDone = getResources().getString(R.string.task_finished_excl);
                if (mTaskDone.isChecked()) {
                    mTaskStatus.setText(stringDone);
                    mTaskStatus.setTypeface(null, Typeface.BOLD);
                    mTaskStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    resetTextColors();
                } else {
                    mTaskStatus.setText(stringNotDone);
                    mTaskStatus.setTypeface(null, Typeface.NORMAL);
                    mTaskStatus.setTextColor(ContextCompat.getColor(NewTask.this, R.color.black));
                    checkIfDatePassed(dateInMillis,dateText);
                    checkIfTimePassed(taskHour,taskMinute);
                }
            }
        });

        mClearDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // mClearDate.setVisibility(View.GONE);
                showHideButtons(mClearDate, false);
                showHideButtons(mClearTime, false);
                mTimeText.setVisibility(View.GONE);
                resetTexts();
                resetTextColors();
            }
        });

        mClearTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // mClearTime.setVisibility(View.GONE);
                showHideButtons(mClearTime, false);
                resetTimeText();
                if(!taskOperation.isDatePassed(dateInMillis)){
                    mDateText.setTextColor(ContextCompat.getColor(NewTask.this, R.color.black));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "newTask " + newTask);
        if (newTask) {
            menu.findItem(R.id.delete_task).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_task) {
            saveTodo();
            return true;
        } else if (id == R.id.delete_task) {
            // continueDelete();
            // Create a new AlertDialogFragment
            mDialog = AlertDialogFragment.newInstance();
            // Show AlertDialogFragment
            mDialog.show(getFragmentManager(), "Alert");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveTodo() {
        String todoTitle = mTitleText.getText().toString();
        String todoDate = "";
        String todoTime = "";
        String todoDateAndTime = "";
        int todoFinished = 0;


        //Log.d(TAG,"time "+todoTitle);
        if (!TextUtils.isEmpty(todoTitle)) {
            //todoDate = mDateText.getText().toString();
            todoDate = dateText;
            todoTime = mTimeText.getText().toString();
            if (!TextUtils.isEmpty(todoTime)) {
                todoDateAndTime = todoDate + ", " + todoTime;
            } else {
                todoDateAndTime = todoDate;
            }
            if (mTaskDone.isChecked()) {
                todoFinished = 1;
            } else {
                todoFinished = 0;
            }
            //Toast.makeText(getApplicationContext(),todoDateAndTime,Toast.LENGTH_LONG).show();
            // Log.d(TAG,"time "+todoDateAndTime +" "+dateInMillis);
            Log.d(TAG, "date & time " + todoDate + " " + todoTime);
            Log.d(TAG, "taskHour " + taskHour + " taskMinute " + taskMinute);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TaskEntry.TASK_TITLE, todoTitle);
            values.put(TaskEntry.TASK_DATE, todoDate);
            values.put(TaskEntry.TASK_TIME, todoTime);
            values.put(TaskEntry.TASK_DATE_AND_TIME, todoDateAndTime);
            values.put(TaskEntry.TASK_DONE, todoFinished);
            values.put(TaskEntry.TASK_DATE_IN_MS, dateInMillis);
            values.put(TaskEntry.TASK_HOUR, taskHour);
            values.put(TaskEntry.TASK_MINUTE, taskMinute);

            if (newTask) {
                //db.insert(TaskEntry.TABLE_NAME, null, values);
                // dbHelper.insertTask(todoTitle,todoDate,todoTime,todoDateAndTime,todoFinished,dateInMillis);
                dbHelper.insertTask(values);
            } else {
                dbHelper.updateTask(taskId, values);
            }
            db.close();

            Intent intent = new Intent(NewTask.this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.task_alert), Toast.LENGTH_SHORT).show();
        }
    }

    public void continueDelete(Boolean delete) {
        if(delete) {
            String todoTitle = mTitleText.getText().toString();
            int deleteCount = dbHelper.deleteTask(taskId);
            Log.i(TAG, "task deleted " + deleteCount);
            Intent intent = new Intent(NewTask.this, MainActivity.class);
            startActivity(intent);
        }
        else{
            mDialog.dismiss();
        }
    }

    public static void showHideButtons(ImageButton imageButton, Boolean show) {
        if (show) {
            imageButton.setVisibility(View.VISIBLE);
        } else {
            imageButton.setVisibility(View.GONE);
        }
    }

    public void resetTexts(){
        mDateText.setText("");
        dateText = "";
        resetTimeText();
    }

    public void resetTimeText(){
        mTimeText.setText("");
        timeText = "";
        taskHour = -1;
        taskMinute = -1;
        mTimeText.setTextColor(ContextCompat.getColor(NewTask.this, R.color.black));
    }

    public void resetTextColors(){
        mDateText.setTextColor(ContextCompat.getColor(NewTask.this, R.color.black));
        mTimeText.setTextColor(ContextCompat.getColor(NewTask.this, R.color.black));
    }

    public static void checkIfDatePassed(long date, String dateString) {
        String result = taskOperation.checkDates(dateInMillis);
        if (result == "") {
            mDateText.setText(dateString);
        } else {
            mDateText.setText(result);
        }
        if(!mTaskDone.isChecked()) {
            if (taskOperation.isDatePassed(date)) {
                mDateText.setTextColor(ContextCompat.getColor(mDateText.getContext(), R.color.red));
                mTimeText.setTextColor(ContextCompat.getColor(mTimeText.getContext(), R.color.red));
            } else {
                mDateText.setTextColor(ContextCompat.getColor(mDateText.getContext(), R.color.black));
                mTimeText.setTextColor(ContextCompat.getColor(mTimeText.getContext(), R.color.black));
            }
        }
    }

    public static void checkIfTimePassed(int selectedHour, int selectedMinute) {
        if(!mTaskDone.isChecked() && DateUtils.isToday(dateInMillis)){
            Log.d(TAG,"isTimePassed "+taskOperation.isTimePassed(selectedHour,selectedMinute));
            if(taskOperation.isTimePassed(selectedHour,selectedMinute)){
                mDateText.setTextColor(ContextCompat.getColor(mDateText.getContext(), R.color.red));
                mTimeText.setTextColor(ContextCompat.getColor(mTimeText.getContext(), R.color.red));
            }
            else {
                mDateText.setTextColor(ContextCompat.getColor(mDateText.getContext(), R.color.black));
                mTimeText.setTextColor(ContextCompat.getColor(mTimeText.getContext(), R.color.black));
            }
        }
    }


    private void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    // DialogFragment used to pick a ToDoItem deadline date

    public static class DatePickerFragment extends DialogFragment implements
            OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current date as the default date in the picker

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int dayName = c.get(Calendar.DAY_OF_WEEK);


            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth,
                              int selectedDay) {

            Calendar cal = Calendar.getInstance();

            cal.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
            cal.set(Calendar.MONTH, datePicker.getMonth());
            cal.set(Calendar.YEAR, datePicker.getYear());

            SimpleDateFormat day_date = new SimpleDateFormat("EEE");
            SimpleDateFormat month_date = new SimpleDateFormat("MMM");
            String dayName = day_date.format(cal.getTime());
            String monthName = month_date.format(cal.getTime());
           // Log.d(TAG,"month_name "+	monthName +" day_name "+dayName);

            //Log.d(TAG,"dateInMillis "+	dateInMillis+" "+ Calendar.getInstance().getTimeInMillis());

           /* Log.d(TAG,"result "+result);
            Log.d(TAG,"isPassed "+taskOperation.isPassed(dateInMillis));
            Log.d(TAG,"checkIfNextWeek "+taskOperation.checkIfNextWeek(dateInMillis));
            Log.d(TAG,"checkIfNextMonth "+taskOperation.checkIfNextMonth(dateInMillis));*/

            dateInMillis = cal.getTimeInMillis();

            String dateString = setDateString(selectedYear, monthName, selectedDay, dayName);
            dateText = dateString;

            checkIfDatePassed(dateInMillis,dateText);

            mTimeText.setVisibility(View.VISIBLE);
            showHideButtons(mClearDate, true);

        }
    }

    private static String setDateString(int year, String monthOfYear, int dayOfMonth, String dayName) {

        // Increment monthOfYear for Calendar/Date -> Time Format setting
        String mon = "" + monthOfYear;
        String day = "" + dayOfMonth;

        if (dayOfMonth < 10)
            day = "0" + dayOfMonth;

        return dayName + ", " + mon + " " + day + ", " + year;
    }

    public static class TimePickerFragment extends DialogFragment
            implements OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, false
                    /*DateFormat.is24HourFormat(getActivity())*/);
        }

        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {

            int hour = selectedHour;
            int minutes = selectedMinute;
            String timeSet = "";
            if (hour > 12) {
                hour -= 12;
                timeSet = "PM";
            } else if (hour == 0) {
                hour += 12;
                timeSet = "AM";
            } else if (hour == 12){
                timeSet = "PM";
            }else{
                timeSet = "AM";
            }

            String min = "";
            if (minutes < 10)
                min = "0" + minutes ;
            else
                min = String.valueOf(minutes);

            String timeString;
            //timeString = setTimeString(hourOfDay, minute);
            timeString = hour +":"+min +" "+timeSet;
            mTimeText.setText(timeString);
            timeText = timeString;
            showHideButtons(mClearTime, true);

            taskHour = selectedHour;
            taskMinute = selectedMinute;

           // Log.d(TAG,"timeString "+timeString);

            Log.d(TAG," selectedHour "+selectedHour + " selectedMinute "+selectedMinute);
           // Log.d(TAG,"isPassed "+taskOperation.isPassed(dateInMillis));

            checkIfTimePassed(taskHour,taskMinute);
        }
    }

    private static String setTimeString(int hourOfDay, int minute) {

        String min = "" + minute;
        String hour = "" + hourOfDay;

        if (minute < 10)
            min = "0" + minute;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);

        String amOrPm;
        c = Calendar.getInstance();
        int am_pm = c.get(Calendar.AM_PM);
        if (am_pm == Calendar.AM) {
            amOrPm = "AM";
        } else {
            amOrPm = "PM";
        }
        return hour + ":" + min + " " + amOrPm;
    }


}
