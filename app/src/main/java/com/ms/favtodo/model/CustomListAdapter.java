package com.ms.favtodo.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ms.favtodo.R;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeSet;

import static android.R.attr.animation;

/**
 * Created by MOHAMED SULAIMAN on 05-01-2017.
 */

public class CustomListAdapter extends BaseAdapter{

    private ArrayList<TaskDetails> taskList;
    private boolean[] checkBoxState;
    private TaskDbHelper dbHelper;

    private TaskOperation taskOperation;
    private Boolean completedTasksOnly = false;

    private static final int TYPE_TASK = 0;
    private static final int TYPE_HEADER = 1;
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

    private LayoutInflater inflater;
    private Context mContext;
    private TaskDetails taskDetails;

    private static final String TAG = "FavDo_Cus";


    public CustomListAdapter(Context context, ArrayList<TaskDetails> list,Boolean completedOnly){
        mContext = context;
        taskList = list;
        dbHelper = new TaskDbHelper(context);
        taskOperation = new TaskOperation(context);
        completedTasksOnly = completedOnly;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public CustomListAdapter(Context context,Boolean completedOnly){
        mContext = context;
        dbHelper = new TaskDbHelper(context);
        taskOperation = new TaskOperation(context);
        completedTasksOnly = completedOnly;
    }

    @Override
    public int getViewTypeCount() {
        // TYPE_TASK and TYPE_HEADER
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof TaskDetails) {
            return TYPE_TASK;
        }
        return TYPE_HEADER;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        int type = getItemViewType(position);

        if(convertView==null){
            viewHolder = new ViewHolder();

            switch (type) {
                case TYPE_TASK:
                    taskDetails = taskList.get(position);
                    convertView = inflater.inflate(R.layout.item_todo, parent, false);
                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.title);
                    viewHolder.tvdateTime = (TextView) convertView.findViewById(R.id.date_time);
                    viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.task_done);
               /*   Log.d(TAG,"tasks:position "+position +" title "+taskDetails.getTitle());*/
                    break;
                case TYPE_HEADER:
                    convertView = inflater.inflate(R.layout.section_header, parent, false);
                    viewHolder.tvHeader = (TextView) convertView.findViewById(R.id.section_title);
                    break;
            }
            convertView.setTag(viewHolder);
        }
        else{
          //  Log.d(TAG,"Else block: type "+type +" "+position);
            viewHolder = (ViewHolder) convertView.getTag();
            if(type == TYPE_TASK){
                taskDetails = taskList.get(position);
            }
        }


        if(type == TYPE_TASK) {

            try {
               // Log.d(TAG,"if type "+type +" "+position);

                viewHolder.tvTitle.setText(taskDetails.getTitle());
                String dateAndTime = taskDetails.getDateAndTime();
                String time = taskDetails.getTime();

                long dateInMilliSeconds = taskDetails.getDateInMilliSeconds();

                String result = taskOperation.checkDates(dateInMilliSeconds);

                 Log.d(TAG,"Task Title "+taskDetails.getTitle() +" hour "+taskDetails.getTaskHour() + " minute "+taskDetails.getTaskMinute());

                if (result != "") {
                    viewHolder.tvdateTime.setVisibility(View.VISIBLE);
                    if(!time.matches("")){
                        result = result + ", " + time;
                        viewHolder.tvdateTime.setText(result);
                    }
                    else{
                        viewHolder.tvdateTime.setText(result);
                    }
                } else if (dateAndTime != null && !dateAndTime.matches("")) {
                    viewHolder.tvdateTime.setVisibility(View.VISIBLE);
                    viewHolder.tvdateTime.setText(dateAndTime);
                } else {
                    viewHolder.tvdateTime.setVisibility(View.GONE);
                }

                if(completedTasksOnly) {
                    //Log.d(TAG,"in "+taskDetails.getTitle());
                    viewHolder.checkBox.setChecked(true);
                    viewHolder.tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                    viewHolder.tvdateTime.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                }
                else{
                    //Log.d(TAG,"Task Done else "+viewHolder.tvdateTime.getText());
                    viewHolder.checkBox.setChecked(false);
                    viewHolder.tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    if(!TextUtils.isEmpty(viewHolder.tvdateTime.getText())){
                        if(TaskOperation.isPassed(dateInMilliSeconds,taskDetails.getTaskHour(),taskDetails.getTaskMinute())){
                            // Log.d(TAG,"iff");
                            viewHolder.tvdateTime.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                        }
                        else{
                            // Log.d(TAG,"else");
                            viewHolder.tvdateTime.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                        }
                    }
                }

               viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                       // Log.d(TAG,"checkbox clicked "+((CheckBox) v).isChecked());
                      //  Log.d(TAG,"checked:task "+taskDetails.getTitle()+" "+position);
                        ContentValues values = new ContentValues();
                        taskDetails = taskList.get(position);
                        if (((CheckBox) v).isChecked()) {
                            values.put(TaskEntry.TASK_DONE, 1);
                        } else {
                            values.put(TaskEntry.TASK_DONE, 0);
                        }
                        dbHelper.updateTaskStatus(taskDetails.getTitle(), values);
                        View v1 = (View) v.getParent();
                        removeListItem(v1,position);
                    }
                });
            }
            catch(Exception e){
               //Log.d(TAG,"error "+type );
            }
        }
        else{
             //Log.d(TAG,"else type "+type +" "+position);
            String titleString = (String)getItem(position);
            viewHolder.tvHeader.setText(titleString);
            if (!completedTasksOnly) {
                if (titleString.equals(mContext.getResources().getString(R.string.overdue))) {
                    viewHolder.tvHeader.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                }
                else if (titleString.equals(mContext.getResources().getString(R.string.no_date))) {
                    viewHolder.tvHeader.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey600));
                } else {
                    viewHolder.tvHeader.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                }
            }
        }
        return convertView;
    }

    protected void removeListItem(View rowView, final int position) {
        // TODO Auto-generated method stub
       // Log.d(TAG,"removeListItem");
        final Animation animation = AnimationUtils.loadAnimation(rowView.getContext(), R.anim.slide_out);
        rowView.startAnimation(animation);
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                taskList.remove(position);
                notifyDataSetChanged();
                animation.cancel();
                taskOperation.retrieveTasks(completedTasksOnly);
            }
        }, 300);
    }


    static class ViewHolder{
        TextView tvTitle;
        TextView tvdateTime;
        CheckBox checkBox;
        TextView tvHeader;

    }
}


/*int states[][] = {{android.R.attr.state_checked}, {}};
                int colors[] = {R.color.black, R.color.colorPrimary};
                CompoundButtonCompat.setButtonTintList(viewHolder.checkBox, new ColorStateList(states, colors));*/

/*final Animation animation = AnimationUtils.loadAnimation(v1.getContext(), R.anim.slide_out);

                        animation.setAnimationListener(new AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                taskList.remove(position);
                                notifyDataSetChanged();
                                taskOperation.retrieveTasks(completedTasksOnly);
                            }
                        });

                        v1.startAnimation(animation);*/