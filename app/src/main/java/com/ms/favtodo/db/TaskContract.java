package com.ms.favtodo.db;

import android.provider.BaseColumns;

/**
 * Created by MOHAMED SULAIMAN on 22-12-2016.
 */

public class TaskContract {

    public static final String DATABASE_NAME = "Todo";
    public static final int DATABASE_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";

        public static final String TASK_ID = "_id";
        public static final String TASK_TITLE = "title";
        public static final String TASK_DATE = "date";
        public static final String TASK_TIME = "time";
        public static final String TASK_DATE_AND_TIME = "datetime";
        public static final String TASK_DONE = "finishedOrNot";
        public static final String TASK_DATE_IN_MS ="dateInMs";
        public static final String TASK_HOUR = "hour";
        public static final String TASK_MINUTE = "minute";
    }
}
