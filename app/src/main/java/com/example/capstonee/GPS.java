package com.example.capstonee;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class GPS extends AppCompatActivity {

    public static String getAdrress(double latitude, double longitude){
        try {
            return getRegionAddress(getJSONData(getApiAddress(latitude, longitude)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private  static String getApiAddress(double latitude, double longitude) {
        String apiURL = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + latitude + "," + longitude + "&language=ko&key=AIzaSyDL8kj8cKgxceSeMTu3h83dSe6svIDIKOw";
        return apiURL;
    }

    private  static String getJSONData(String apiURL) throws Exception {
        Log.e("getJSONData 시작","");
        String jsonString = new String();
        String buf;
        URL url = new URL(apiURL);
        URLConnection conn = url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        while ((buf = br.readLine()) != null) {
            jsonString += buf;
        }
        Log.e("getJSONData 종료","");
        return jsonString;
    }

    private  static String getRegionAddress(String jsonString) {
        JSONObject jObj = (JSONObject) JSONValue.parse(jsonString);
        JSONArray jArray = (JSONArray) jObj.get("results");
        jObj = (JSONObject) jArray.get(0);
        return (String) jObj.get("formatted_address");
    }

    public String[] currentLocation(Context context, Activity activity) throws Exception {

        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            final String provider;
            final double longitude;
            final double latitude;
            final double altitude;

            if(location!= null){
                provider = location.getProvider();
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                altitude = location.getAltitude();
            }else {
                provider = "제공 하지 못함.";
                latitude = -1;
                longitude = -1;
                altitude = -1;
            }

            // 네트워킹에서는 강제로 분리 왜냐하면 네트워크 받아오는 동안 프로그램이 멈추니까
            // 프로그램에서 자체적으로 Thread를 사용하게 만든 거 같다.
            Log.e("Thread 시전","??");

            return new String[]{provider, longitude + "", latitude + "", altitude + ""};


        }
        return null;

    }

//    private Button button1;
//    private TextView txtResult;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_gps);
//        button1 = (Button) findViewById(R.id.button1);
//        txtResult = (TextView) findViewById(R.id.txtResult);
//
//        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (Build.VERSION.SDK_INT >= 23 &&
//                        ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(GPS.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                            0);
//                } else {
//                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    String provider = location.getProvider();
//                    double longitude = location.getLongitude();
//                    double latitude = location.getLatitude();
//                    double altitude = location.getAltitude();
//
//                    txtResult.setText("위치정보 : " + provider + "\n" +
//                            "위도 : " + longitude + "\n" +
//                            "경도 : " + latitude + "\n" +
//                            "고도  : " + altitude);
//
//                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                            1000,
//                            1,
//                            gpsLocationListener);
//                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                            1000,
//                            1,
//                            gpsLocationListener);
//                }
//            }
//        });
//
//    }
//
//    final LocationListener gpsLocationListener = new LocationListener() {
//        public void onLocationChanged(Location location) {
//
//            String provider = location.getProvider();
//            double longitude = location.getLongitude();
//            double latitude = location.getLatitude();
//            double altitude = location.getAltitude();
//
//            txtResult.setText("위치정보 : " + provider + "\n" +
//                    "위도 : " + longitude + "\n" +
//                    "경도 : " + latitude + "\n" +
//                    "고도  : " + altitude);
//
//        }
//
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//
//        public void onProviderEnabled(String provider) {
//        }
//
//        public void onProviderDisabled(String provider) {
//        }
//    };
}
