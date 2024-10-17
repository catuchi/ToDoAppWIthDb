package com.example.todoappwithdb;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Todo> tasks;
    private TodoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasks = new ArrayList<>();

//        loadDataFromDatabase();

        new LoadDataTask().execute();

        ListView listview = findViewById(R.id.taskListView);
        EditText input = findViewById(R.id.taskInput);
        Button addButton = findViewById(R.id.addButton);
        Switch urgentSwitch = findViewById(R.id.urgentSwitch);

        adapter = new TodoAdapter();
        listview.setAdapter(adapter);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task = input.getText().toString();
                boolean urgent = urgentSwitch.isChecked();

                MyOpener dbOpener = new MyOpener(MainActivity.this);
                SQLiteDatabase db = dbOpener.getWritableDatabase();

                ContentValues newRowValues = new ContentValues();
                newRowValues.put(MyOpener.COL_TASK, task);
                newRowValues.put(MyOpener.COL_URGENT, urgent ? 1 : 0);

                long newRowId = db.insert(MyOpener.TABLE_NAME, null, newRowValues);

                db.close();

//                Tod newTask = new Tod(task, urgent);
                Todo newTask = new Todo((int) newRowId, task, urgent);
                tasks.add(newTask);
                adapter.notifyDataSetChanged();

                input.setText("");
                urgentSwitch.setChecked(false);
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
                return true;
            }
        });
    }

    // AsyncTask for loading data from the database in the background
    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            loadDataFromDatabase(); // Perform the database query in the background
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.notifyDataSetChanged(); // Update the UI after the data is loaded
        }
    }

    public void loadDataFromDatabase() {
        MyOpener dbOpener = new MyOpener(this);
        SQLiteDatabase db = dbOpener.getReadableDatabase();

        String[] columns = {MyOpener.COL_ID, MyOpener.COL_TASK, MyOpener.COL_URGENT};

        Cursor results = db.query(false, MyOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        if (results == null) {
            Log.d("DatabaseDebug", "Cursor is null. No data retrieved.");
            return; // Exit if no data is available
        }

        if (!results.moveToFirst()) {
            Log.d("DatabaseDebug", "No rows in database.");
            results.close();
            db.close();
            return; // Exit if there are no rows
        }

        tasks.clear();

        int idColIndex = results.getColumnIndex(MyOpener.COL_ID);
        int taskColIndex = results.getColumnIndex(MyOpener.COL_TASK);
        int urgentColIndex = results.getColumnIndex(MyOpener.COL_URGENT);

        while (results.moveToNext()) {
            int id = results.getInt(idColIndex);
            String task = results.getString(taskColIndex);
            boolean isUrgent = results.getInt(urgentColIndex) == 1;

//            Tod tod = new Tod(task, isUrgent);
            Todo todo = new Todo(id, task, isUrgent);
            tasks.add(todo);
        }

        Log.d("DatabaseDebug", "Finished loading data");
        results.close();
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void showDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.delete_dialog_title));
        builder.setMessage(getString(R.string.delete_dialog_message, position));

        int todoId = tasks.get(position).getId();

        MyOpener dbOpener = new MyOpener(MainActivity.this);
        SQLiteDatabase db = dbOpener.getWritableDatabase();

        builder.setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.delete(MyOpener.TABLE_NAME, MyOpener.COL_ID + "=?", new String[]{String.valueOf(todoId)});

                tasks.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton(getString(R.string.no_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(dialog -> db.close());
        builder.create().show();
    }

    public void printCursor(Cursor c) {
        // Log the database version
        int dbVersion = MyOpener.VERSION_NUMBER;
        Log.d("DatabaseDebug", "Database Version: " + dbVersion);

        // Log the number of columns
        int numColumns = c.getColumnCount();
        Log.d("DatabaseDebug", "Number of Columns: " + numColumns);

        // Log the names of the columns
        String[] columnNames = c.getColumnNames();
        Log.d("DatabaseDebug", "Column Names: ");
        for (String columnName : columnNames) {
            Log.d("DatabaseDebug", " - " + columnName);
        }

        // Log the number of results
        int numOfResults = c.getCount();
        Log.d("DatabaseDebug", "Number of Results: " + numOfResults);

        // Log each row of results
        Log.d("DatabaseDebug", "Rows:");
        if (c.moveToFirst()) {
            do {
                StringBuilder row = new StringBuilder();
                for (int i = 0; i < numColumns; i++) {
                    row.append(c.getString(i)).append(" | ");
                }
                Log.d("DatabaseDebug", row.toString());
            } while (c.moveToNext());
        } else {
            Log.d("DatabaseDebug", "No rows in cursor.");
        }
    }

    private class TodoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                convertView = inflater.inflate(R.layout.todo_item, parent, false);
            }

            Todo currentTodo = tasks.get(position);

            TextView taskTextView = convertView.findViewById(R.id.todoItemTextView);

            taskTextView.setText(currentTodo.getTask());

            if (currentTodo.isUrgent()) {
                convertView.setBackgroundColor(Color.RED);
                taskTextView.setTextColor(Color.WHITE);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                taskTextView.setTextColor(Color.BLACK);
            }

            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return tasks.get(position);
        }
    }
}