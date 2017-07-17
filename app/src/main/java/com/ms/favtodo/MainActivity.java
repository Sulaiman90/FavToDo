package com.ms.favtodo;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ms.favtodo.db.TaskContract;
import com.ms.favtodo.db.TaskContract.TaskEntry;
import com.ms.favtodo.db.TaskDbHelper;
import com.ms.favtodo.model.CustomListAdapter;
import com.ms.favtodo.model.TaskDetails;
import com.ms.favtodo.model.TaskOperation;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TaskDbHelper dbHelper;
    private TaskOperation taskOperation;

    private static final String TAG = "FavDo_Main";

    private EditText quickTask;
    private ImageButton tickBtn;
    private FloatingActionButton fab;
    private LinearLayout footer;
    private TextView noTasks;

    private String todoTitle;

    private Boolean mIsSpinnerFirstCall = true;
    private Boolean completedTasksOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        quickTask = (EditText) findViewById(R.id.quick_task);
        tickBtn = (ImageButton) findViewById(R.id.tick_btn);
        footer = (LinearLayout) findViewById(R.id.footer);
        noTasks = (TextView) findViewById(R.id.no_tasks);

        tickBtn.setVisibility(View.GONE);

        dbHelper = new TaskDbHelper(this);
        taskOperation = new TaskOperation(this);

        int totalTasks = dbHelper.rowcount();
       // Log.d(TAG,"totalTasks "+totalTasks);
        if(totalTasks!=0){
            loadTasks();
        }
        else{
            setInfoText(completedTasksOnly);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,NewTask.class);
                intent.putExtra("NewTask",true);
                startActivity(intent);
            }
        });

        quickTask.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                checkIfTextEntered();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        tickBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveQuickTodo(todoTitle);
                quickTask.clearFocus();
                quickTask.getText().clear();
                tickBtn.setVisibility(View.GONE);
                hideDefaultKeyboard();
                loadTasks();
                noTasks.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_spinner_menu, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_list_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!mIsSpinnerFirstCall) {
                    if (position == 0) {
                        // not completed
                        completedTasksOnly = false;
                        hideShowFooterViews(completedTasksOnly);
                    } else {
                        completedTasksOnly = true;
                        hideShowFooterViews(completedTasksOnly);
                    }
                    loadTasks();
                }
                mIsSpinnerFirstCall = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return true;
    }


    private void loadTasks(){
        int totalTasks = taskOperation.retrieveTasks(completedTasksOnly);
        if(totalTasks==0){
            setInfoText(completedTasksOnly);
        }
        else{
            noTasks.setVisibility(View.GONE);
        }
    }

    private void setInfoText(Boolean completedTasksOnly){
        noTasks.setVisibility(View.VISIBLE);
        if(completedTasksOnly){
            noTasks.setText(getResources().getString(R.string.no_finished_tasks));
        }
        else{
            noTasks.setText(getResources().getString(R.string.no_tasks_todo));
        }
    }

    private void checkIfTextEntered(){
        todoTitle = quickTask.getText().toString();
        if(todoTitle.matches("")){
            tickBtn.setVisibility(View.GONE);
        }
        else{
            tickBtn.setVisibility(View.VISIBLE);
        }
    }

    private void saveQuickTodo(String todoTitle){
        dbHelper.insertTask(todoTitle,"","","",0,0,0,0);
    }

    private void hideDefaultKeyboard() {
        // Check if no view has focus:
       /* View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }*/
        taskOperation.hideKeyboard(MainActivity.this);
    }

    private void hideShowFooterViews(Boolean hide){
        View shadow = findViewById(R.id.shadow);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)shadow.getLayoutParams();
        if(hide){
            fab.setVisibility(View.GONE);
            quickTask.clearFocus();
            quickTask.getText().clear();
            tickBtn.setVisibility(View.GONE);
            footer.setVisibility(View.GONE);
            layoutParams.removeRule(RelativeLayout.ABOVE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            hideDefaultKeyboard();
        }
        else{
            fab.setVisibility(View.VISIBLE);
            footer.setVisibility(View.VISIBLE);
            layoutParams.addRule(RelativeLayout.ABOVE,R.id.footer);
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
    }
}

