package cosc341.group4.prescriptionpal;

import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TodayActivity extends AppCompatActivity {

    ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        setDate();

        //Create the list view and hash map for storing the prescription info
        expandableListView = findViewById(R.id.today_expandableListView);
        HashMap<String, List<String>> item = new HashMap<>();
        HashMap<String, Boolean> check = new HashMap<>();

        JSONObject json;

        try {
            //Get the json object from the today.json file
            json = getJsonObject();
            //Get a json array of each prescription
            assert json != null;
            JSONArray prescriptionArray = json.getJSONArray("Prescriptions");

            //Array list containing infoArrays for each prescription
            ArrayList<String[]> infoArrayList = new ArrayList<>();

            //Iterate through the json array and retrieve each prescription's info
            for (int i = 0; i < prescriptionArray.length(); i++){
                JSONObject prescriptionDetail = prescriptionArray.getJSONObject(i);

                String name = prescriptionDetail.getString("Name");
                String dosage = prescriptionDetail.getString("Dosage");
                String time = prescriptionDetail.getString("Time");
                String addInfo = prescriptionDetail.getString("Additional Info");

                Boolean taken = prescriptionDetail.getBoolean("Taken");
                //Put whether the current medication has been taken into the check hashmap
                check.put(name, taken);


                String[] infoArray = {
                        name,
                        "Dosage: " + dosage,
                        "When: " + time,
                        "Additional Information:\n" + addInfo
                };
                //Add the infoArray to the Array list
                infoArrayList.add(infoArray);
            }

            for (String[] infoArray : infoArrayList) addPrescription(infoArray, item);


            TodayPrescriptionListAdapter adapter = new TodayPrescriptionListAdapter(item, check);
            expandableListView.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Called when a checkbox is ticked/unticked on the today page
    //Updates today.json 'taken' values for medications
    public void Confirmed(View view){

        CheckBox checkBox = view.findViewById(R.id.today_confirm_checkbox);
        Boolean taken = checkBox.isChecked();
        String name = checkBox.getContentDescription().toString();

        System.out.println("NAME: " + name + " TAKEN: " + taken);

        JSONObject json;
        try {
            //Get the json object from the today.json file
            json = getJsonObject();
            //Get a json array of each prescription
            assert json != null;
            JSONArray prescriptionArray = json.getJSONArray("Prescriptions");
            JSONArray newPrescriptionArray = new JSONArray();

            for (int i = 0; i < prescriptionArray.length(); i++) {
                JSONObject prescriptionDetail = prescriptionArray.getJSONObject(i);
                //Replace old prescription with updated values

                if (prescriptionDetail.getString("Name").equals(name)) {
                    prescriptionDetail.put("Taken", taken);

                }

                newPrescriptionArray.put(prescriptionDetail);
                json.put("Prescriptions", newPrescriptionArray);
            }

            //Rewrite updated json to today.json
            Writer output;
            File file = new File(getFilesDir()+"/today.json");
            output = new BufferedWriter(new FileWriter(file));
            output.write(json.toString());
            output.close();

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }

    private void addPrescription(String[] infoArray,  HashMap<String, List<String>> item){
        ArrayList<String> prescriptionInfo = new ArrayList<>();
        prescriptionInfo.add(infoArray[1]); //Dosage
        prescriptionInfo.add(infoArray[2]); //Time
        prescriptionInfo.add(infoArray[3]); //Additional Info

        item.put(infoArray[0], prescriptionInfo);
    }

    //Updates date textview to show the current date
    private void setDate(){
        TextView date = findViewById(R.id.today_date_textview);
        //Format the date
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE\nMMMM d");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);
        //Update the textview
        date.setText(dayOfTheWeek);
    }

    //CODE ADAPTED FROM: https://abhiandroid.com/programming/json
    private JSONObject getJsonObject() throws JSONException {
        String json;
        try{
            InputStream inputStream = getApplicationContext().openFileInput("today.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return new JSONObject(json);
    }

    public void goHome(View view){
        finish();
    }
}
