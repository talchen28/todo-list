package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vishnusivadas.advanced_httpurlconnection.PutData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    
    Button add;
    Button logout;
    EditText etText;
    LinearLayout lists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        add = findViewById(R.id.add);
        logout = findViewById(R.id.logout);
        etText = findViewById(R.id.edit);
        lists = findViewById(R.id.lists);
        
        showLists();
        
        logout();
        
        add();
    }

    private void add() {
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            
                String task;
                task = String.valueOf(etText.getText());


                // Making sure everything is filled by the user
                if (!task.equals("")) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Starting Write and Read data with URL
                            //Creating array for parameters
                            String[] field = new String[2];
                            field[0] = "task";
                            field[1] = "user_id";
                            //Creating array for data
                            String[] data = new String[2];
                            data[0] = task;
                            //get preferences
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            //get the value from editor of user_id
                            String user_id = preferences.getString("user_id", "");
                            data[1] = user_id;
                            PutData putData = new PutData("http://android2021.eu5.net/insert.php", "POST", field, data);
                            if (putData.startPut()) {
                                if (putData.onComplete()) {
                                    String result = putData.getResult();
                                    // If succeed, move to other screen
                                    if (result.equals("unable to insert task, please reconnect")) {
                                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                                    } else {
                                        showLists();
                                    }
                                }
                            }
                        }
                    });
                } else { // If something is not filled
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logout() {

        logout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                preferences.edit().remove("user_id").apply();
                
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //shows the list of tasks
    public void showLists() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //Starting Write and Read data with URL
                //Creating array for parameters
                String[] field = new String[1];
                field[0] = "user_id";
                //Creating array for data
                String[] data = new String[1];
                //get preferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                //get the value from editor of user_id
                String user_id = preferences.getString("user_id", "");
                data[0] = user_id;
                PutData getData = new PutData("http://android2021.eu5.net/showAll.php", "POST", field, data);
                if (getData.startPut()) {
                    if (getData.onComplete()) {
                        try {
                            //convert String(json) to object of JSONObject
                            JSONArray jArray = new JSONArray(getData.getResult());
                            //remove all elements from listTasks
                            lists.removeAllViews();
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject oneObject = jArray.getJSONObject(i);
                                // Pulling items from the array
                                String taskData = oneObject.getString("task");
                                String taskId = oneObject.getString("id");
                                TextView taskText = new TextView(MainActivity.this);
                                //set in taskText String value from json
                                taskText.setText(taskData);
                                //add to view all tasks

                                Button deleteBtn = new Button(MainActivity.this);
                                // Delete button
                                deleteBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String[] deleteField = new String[1];
                                        deleteField[0] = "task_id";
                                        //Creating array for data
                                        String[] data = new String[1];
                                        data[0] = taskId;
                                        PutData deleteData = new PutData("http://android2021.eu5.net/delete.php", "POST", deleteField, data);
                                        if (deleteData.startPut()) {
                                            if (deleteData.onComplete()) {
                                                showLists();
                                            }
                                        }
                                    }
                                });

                                // Update button
//                                Button updateBtn = new Button(MainActivity.this);
//
//                                updateBtn.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Intent intent = new Intent(getApplicationContext(),
//                                                Update.class);
//                                        //pass the task id and the task data to the update screen
//                                        intent.putExtra("task_id", taskId);
//                                        intent.putExtra("task_data", taskData);
//                                        // open update screen
//
//                                        startActivityForResult(intent, LAUNCH_UPDATE_ACTIVITY);
//                                    }
//                                });

                                LinearLayout taskContainer = new LinearLayout(MainActivity.this);
                                taskContainer.addView(taskText);
                                //taskContainer.addView(updateBtn);
                                taskContainer.addView(deleteBtn);

                                lists.addView(taskContainer);
                            }
                        } catch (JSONException e) {
                            System.out.println(getData.getResult());
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}