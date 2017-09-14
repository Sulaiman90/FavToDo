package com.ms.favtodo.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.ms.favtodo.R;
import com.ms.favtodo.activity.MainActivity;
import com.ms.favtodo.activity.NewTask;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.model.TaskDetails;
import com.ms.favtodo.sync.ReminderTasks;
import com.ms.favtodo.sync.TaskReminderIntentService;

/**
 * Created by MOHAMED SULAIMAN on 09-09-2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final int REMINDER_PENDING_INTENT_ID = 1140;

    private static final String TAG = "AlarmReceiver";

    private static TaskDbHelper dbHelper;

    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            Bundle b = intent.getExtras();
            String TaskTitle = b.getString("TaskTitle");
            long rowId =  b.getLong("TaskRowId");

            Toast.makeText(context, TaskTitle, Toast.LENGTH_SHORT).show();

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setContentTitle(TaskTitle+" "+rowId)
                    .setContentText(rowId+"")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(TaskTitle))
                    .setSmallIcon(R.drawable.ic_done_white_24dp)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentIntent(contentIntent(context,rowId))
                    .addAction(taskCompletedAction(context,rowId))
                    .addAction(ignoreReminderAction(context,rowId))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            }

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

        /*  REMINDER_NOTIFICATION_ID allows you to update or cancel the notification later on */
            notificationManager.notify((int)rowId, notificationBuilder.build());
        }
    }

    private static PendingIntent contentIntent(Context context,long rowId) {
        Intent startActivityIntent = new Intent(context, NewTask.class);

        dbHelper = new TaskDbHelper(context);
        Cursor c1 =  dbHelper.fetchTask(rowId);

        //Log.e(TAG,"rowId "+rowId);

        TaskDetails task = new TaskDetails();
        task.setTaskId(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_ID)));
        task.setTitle(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TITLE)));
        task.setDateAndTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE_AND_TIME)));
        task.setDate(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_DATE)));
        task.setTime(c1.getString(c1.getColumnIndexOrThrow(TaskEntry.TASK_TIME)));
        task.setTaskDone(c1.getInt(c1.getColumnIndexOrThrow(TaskEntry.TASK_DONE)));
        task.setDateInMilliSeconds(c1.getLong(c1.getColumnIndex(TaskEntry.TASK_DATE_IN_MS)));
        task.setTaskHour(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_HOUR)));
        task.setTaskMinute(c1.getInt(c1.getColumnIndex(TaskEntry.TASK_MINUTE)));

        startActivityIntent.putExtra("title", task.getTitle());
        startActivityIntent.putExtra("id", task.getTaskId());
        startActivityIntent.putExtra("date", task.getDate());
        startActivityIntent.putExtra("time", task.getTime());
        startActivityIntent.putExtra("doneOrNot", task.getTaskDone());
        startActivityIntent.putExtra("timeInMs", task.getDateInMilliSeconds());
        startActivityIntent.putExtra("hour", task.getTaskHour());
        startActivityIntent.putExtra("minute", task.getTaskMinute());
        startActivityIntent.putExtra("TaskRowId",rowId);

        return PendingIntent.getActivity(
                context,
                (int)rowId,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Action taskCompletedAction(Context context, long rowID){
        Intent launchIntent = new Intent(context, TaskReminderIntentService.class);
        launchIntent.setAction(ReminderTasks.ACTION_TASK_COMPLETED);
        launchIntent.putExtra("taskRowId",rowID);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                (int)rowID,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Action taskDoneAction = new Action(R.drawable.ic_done_white_24dp,
                "I did it",
                pendingIntent);
        return taskDoneAction;
    }

    public static Action ignoreReminderAction(Context context, long rowID){
        Intent ignoreReminderIntent = new Intent(context, TaskReminderIntentService.class);
        ignoreReminderIntent.setAction(ReminderTasks.ACTION_DISMISS_NOTIFICATION);
        ignoreReminderIntent.putExtra("taskRowId",rowID);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                (int)rowID,
                ignoreReminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Action ignoreReminderAction = new Action(R.drawable.ic_cancel_black_24px,
                "No thanks",
                pendingIntent);
        return ignoreReminderAction;
    }

}
