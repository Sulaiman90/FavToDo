package com.ms.favtodo.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ms.favtodo.MainActivity;
import com.ms.favtodo.NewTask;
import com.ms.favtodo.R;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by MOHAMED SULAIMAN on 14-01-2017.
 */

public class TaskOperation {

    private TaskDbHelper dbHelper;
    private Context mContext;

    private static final String TAG = "FavDo_TaskOperation";

    public TaskOperation(Context context){
        mContext = context;
        dbHelper = new TaskDbHelper(context);
    }

    public int retrieveTasks(Boolean completedTasksOnly){


        ArrayList<TaskDetails> taskList = new ArrayList<>();
        taskList.clear();

        ArrayList finalTaskList = new ArrayList();
        taskList.clear();

        ArrayList<TaskDetails>  overdueTasks = new ArrayList<>();
        overdueTasks.clear();
        ArrayList<TaskDetails>  todayTasks = new ArrayList<>();
        todayTasks.clear();
        ArrayList<TaskDetails>  tomorrowTasks = new ArrayList<>();
        tomorrowTasks.clear();
        ArrayList<TaskDetails>  thisWeekTasks = new ArrayList<>();
        thisWeekTasks.clear();
        ArrayList<TaskDetails>  nextWeekTasks = new ArrayList<>();
        nextWeekTasks.clear();
        ArrayList<TaskDetails> thisMonthTasks =new ArrayList<>();
        thisMonthTasks.clear();
        ArrayList<TaskDetails>  nextMonthTasks =new ArrayList<>();
        nextMonthTasks.clear();
        ArrayList<TaskDetails>  laterTasks = new ArrayList<>();
        laterTasks.clear();

        ArrayList<TaskDetails>  noDateTasks = new ArrayList<>();
        noDateTasks.clear();

        Cursor c1;
        int totalTasks = 0;

        if(completedTasksOnly){
            c1 = dbHelper.fetchCompletedTasks(1);
        }
        else{
            c1 = dbHelper.fetchCompletedTasks(0);
        }

        Log.d(TAG,"getCount "+c1.getCount());
       /* Log.d(TAG,"completedTasksOnly "+ completedTasksOnly);

        int totalTasks = dbHelper.rowcount();
        Log.d(TAG,"totalTasks "+totalTasks);*/

        if(c1!=null){
            totalTasks = c1.getCount();
            if(c1.getCount()!=0 && c1.moveToFirst()){
                do{
                    TaskDetails task = new TaskDetails();
                    task.setTitle(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE)));
                    task.setDateAndTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE_AND_TIME)));

                    Log.d(TAG,"Title "+c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE))
                            + " date "+c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE)));

                    task.setDate(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE)));
                    task.setTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TIME)));
                    task.setTaskDone(c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.TASK_DONE)));
                    task.setDateInMilliSeconds(c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS)));
                    task.setTaskId(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_ID)));
                    task.setTaskHour(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_HOUR)));
                    task.setTaskMinute(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_MINUTE)));
                    taskList.add(task);

                    long date = c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS));

                    if(date==0){
                        noDateTasks.add(task);
                    }
                    else if(isPassed(date,task.getTaskHour(),task.getTaskMinute())){
                       // Log.d(TAG,"isPassed:title "+task.getTitle() + " hour "+task.getTaskHour() + " minute "+task.getTaskMinute());
                        overdueTasks.add(task);
                    }
                    else if(DateUtils.isToday(date)){
                       // Log.d(TAG,"isToday "+c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE)));
                        todayTasks.add(task);
                    }
                    else if(isTomorrow(date)){
                        tomorrowTasks.add(task);
                    }
                    else if(checkIfThisWeek(date)){
                        thisWeekTasks.add(task);
                    }
                    else if(checkIfNextWeek(date)){
                        nextWeekTasks.add(task);
                    }
                    else if(checkIfThisMonth(date)){
                        thisMonthTasks.add(task);
                    }
                    else if(checkIfNextMonth(date)){
                        nextMonthTasks.add(task);
                    }
                    else if(checkIfLater(date)){
                        laterTasks.add(task);
                    }
                }
                while (c1.moveToNext());
            }
            else if(c1.getCount()==0){
                c1.close();
                setInfoText(completedTasksOnly);
            }
        }

       if(overdueTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.overdue));
            finalTaskList.addAll(overdueTasks);
        }
        if(todayTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.today));
            finalTaskList.addAll(todayTasks);
        }
        if(tomorrowTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.tomorrow));
            finalTaskList.addAll(tomorrowTasks);
        }
        if(thisWeekTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.this_week));
            finalTaskList.addAll(thisWeekTasks);
        }
        if(nextWeekTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.next_week));
            finalTaskList.addAll(nextWeekTasks);
        }
        if(thisMonthTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.this_month));
            finalTaskList.addAll(thisMonthTasks);
        }
        if(nextMonthTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.next_month));
            finalTaskList.addAll(nextMonthTasks);
        }
        if(laterTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.later));
            finalTaskList.addAll(laterTasks);
        }
        if(noDateTasks.size() > 0){
            finalTaskList.add(mContext.getResources().getString(R.string.no_date));
            finalTaskList.addAll(noDateTasks);
        }

        CustomListAdapter adapter;
        if(completedTasksOnly){
            adapter = new CustomListAdapter(mContext,taskList,true);
        }
        else{
            adapter = new CustomListAdapter(mContext,finalTaskList,completedTasksOnly);
        }

        ListView listView = (ListView) ((Activity)mContext).findViewById(R.id.todo_lists);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (parent.getItemAtPosition(position) instanceof TaskDetails) {
                        TaskDetails tasks = (TaskDetails) parent.getItemAtPosition(position);
                        Intent intent = new Intent(mContext, NewTask.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("title", tasks.getTitle());
                        Log.d(TAG,"Item:title "+tasks.getTitle() +" "+tasks.getDate());
                        //Log.d(TAG,"Item:ms "+tasks.getTaskDone() +" "+tasks.getDateInMilliSeconds());
                        bundle.putInt("id", tasks.getTaskId());
                        bundle.putString("date", tasks.getDate());
                        bundle.putString("time", tasks.getTime());
                        bundle.putInt("doneOrNot", tasks.getTaskDone());
                        bundle.putLong("timeInMs", tasks.getDateInMilliSeconds());
                        bundle.putInt("hour", tasks.getTaskHour());
                        bundle.putInt("minute", tasks.getTaskMinute());

                        intent.putExtras(bundle);
                        //mContext.startActivity(intent);
                        ((Activity) mContext).startActivityForResult(intent,MainActivity.TODO_REQUEST_CODE);
                    }
                }
                catch (Exception e) {
                    //Log.d(TAG,"Selected Header ");
                }
            }
        });
        return totalTasks;
    }

    private void setInfoText(Boolean completedTasksOnly){
        TextView noFinishedTasks = (TextView) ((Activity)mContext).findViewById(R.id.no_tasks);
        LinearLayout mEmptyLayout = (LinearLayout) ((Activity)mContext).findViewById(R.id.toDoEmptyView);
      /*  noTasks.setVisibility(View.VISIBLE);
        if(completedTasksOnly){
            noTasks.setText(mContext.getResources().getString(R.string.no_finished_tasks));
        }
        else{
            noTasks.setText(mContext.getResources().getString(R.string.no_tasks_todo));
        }*/
        if(completedTasksOnly){
            noFinishedTasks.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.GONE);
        }
        else {
            mEmptyLayout.setVisibility(View.VISIBLE);
            noFinishedTasks.setVisibility(View.GONE);
        }
    }

    public String checkDates(long date){
        String dateStr = "";
        if(DateUtils.isToday(date)){
            dateStr = mContext.getResources().getString(R.string.today);
        }
        else if(isTomorrow(date)){
            dateStr = mContext.getResources().getString(R.string.tomorrow);
        }
        else if(isYesterday(date)){
            dateStr = mContext.getResources().getString(R.string.yesterday);
        }
        return dateStr;
    }

    public static boolean isPassed(long date,int hour,int minute) {
        if(DateUtils.isToday(date) && hour>=0 && minute>=0){
            return isTimePassed(hour,minute);
        }
        else if(!DateUtils.isToday(date)){
            Calendar now = Calendar.getInstance();
            Calendar cdate = Calendar.getInstance();
            cdate.setTimeInMillis(date);
            return cdate.before(now);
        }
        return false;
    }

    public static boolean isDatePassed(long date) {
        if(!DateUtils.isToday(date)){
            Calendar now = Calendar.getInstance();
            Calendar cdate = Calendar.getInstance();
            cdate.setTimeInMillis(date);
            return cdate.before(now);
        }
        return false;
    }

    public static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);

        now.add(Calendar.DATE,-1);

        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

    public static boolean isTomorrow(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);

        now.add(Calendar.DATE,1);

        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

    public static boolean checkIfThisWeek(long date){
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int year1 = cal1.get(Calendar.YEAR);
        int week1 = cal1.get(Calendar.WEEK_OF_YEAR);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        cal2.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int year2 = cal2.get(Calendar.YEAR);
        int week2 = cal2.get(Calendar.WEEK_OF_YEAR);

        if(year1 == year2 && week2 == week1){
           // Log.d(TAG,"This week ");
            return true;
        }
        return false;
    }

    public static boolean checkIfNextWeek(long date){
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int year1 = cal1.get(Calendar.YEAR);
        int week1 = cal1.get(Calendar.WEEK_OF_YEAR);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        cal2.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int year2 = cal2.get(Calendar.YEAR);
        int week2 = cal2.get(Calendar.WEEK_OF_YEAR);

        if(year1 == year2 && week2 > week1 && week2-week1 == 1){
          // Log.d(TAG,"Next week ");
            return true;
        }
        return false;
    }

    public static boolean checkIfThisMonth(long date){
        Calendar cal1 = Calendar.getInstance();
        int year1 = cal1.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH) +1 ;

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        int year2 = cal2.get(Calendar.YEAR);
        int month2 = cal2.get(Calendar.MONTH) + 1;

        if(year1 == year2 && (month2 == month1)){
           // Log.d(TAG,"This month ");
            return true;
        }
        return false;
    }

    public static boolean checkIfNextMonth(long date){
        Calendar cal1 = Calendar.getInstance();
        int year1 = cal1.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH) +1 ;

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        int year2 = cal2.get(Calendar.YEAR);
        int month2 = cal2.get(Calendar.MONTH) + 1;

       // Log.d(TAG,"Next month "+week1 +" "+week2);
        if(year1 == year2 && (month2 > month1) && (month2-month1) == 1){
            //Log.d(TAG,"Next month ");
            return true;
        }
        return false;
    }

    public static boolean checkIfLater(long date){
        Calendar cal1 = Calendar.getInstance();
        int year1 = cal1.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH) +1 ;

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        int year2 = cal2.get(Calendar.YEAR);
        int month2 = cal2.get(Calendar.MONTH) + 1;

        // Log.d(TAG,"Next month "+year2 +" "+year1);
        if(year2 > year1){
            //Log.d(TAG,"After this current Year ");
            return true;
        }
        else  if(year1 == year2 && (month2 > month1) && (month2-month1) > 1){
           // Log.d(TAG,"After next month ");
            return true;
        }
        return false;
    }

    public static boolean isTimePassed(int hour,int minute){
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        if(hour>=0 && minute>=0){
            if(hour < currentHour){
                return true;
            }
            else if(hour == currentHour){
                if(minute <= currentMinute){
                    return true;
                }
            }
        }
        return false;
    }

    public static void hideKeyboard(@NonNull Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
