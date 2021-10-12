package com.spi.mockcardreaderservice.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spi.mockcardreaderservice.MockResponseData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import co.poynt.cardreader.CardScheme;

public class MockCardreaderHelper {
    private final String TAG = "MockCardreaderHelper";
    Context mContext = null;

    public MockCardreaderHelper(Context context) {
        mContext = context;
    }

    List<MockResponseData> responseDataList;

    public void loadResponseJsonFileFromAssets() {
        File cardreaderResponseFile = new File(dirEnv, "CardReaderResponse.json");

        Log.d("mContext.dirEnv", dirEnv.toString());

        if (cardreaderResponseFile.exists()) {
            Log.d("Cardreaderhelper", "Cardreader Response json File exists" +
                    cardreaderResponseFile.toString());
        } else {
            Log.d("Akhila", "not good");
        }

        FileInputStream inputStream = null;
        try {
                inputStream = new FileInputStream(cardreaderResponseFile);
                int size = inputStream.available();
                // Read the entire asset into a local byte buffer.
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                String testDataJson = new String(buffer);
                Gson gson = new Gson();
              //  Type listType = new TypeToken<List<TestData>>() {
               // }.getType();
                //List<TestData> posts = gson.fromJson(testDataJson, listType);
               // return posts;
            } catch (IOException e) {
                e.printStackTrace();
                //return null;
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught due to " + e.getMessage());
                }
            }
        }


/*
    //JSON parser object to parse read file
    JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("employees.json"))
    {
        //Read JSON file
        Object obj = jsonParser.parse(reader);

        JSONArray employeeList = (JSONArray) obj;
        System.out.println(employeeList);

        //Iterate over employee array
        employeeList.forEach( emp -> parseEmployeeObject( (JSONObject) emp ) );

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (ParseException e) {
        e.printStackTrace();
    }*/
}
