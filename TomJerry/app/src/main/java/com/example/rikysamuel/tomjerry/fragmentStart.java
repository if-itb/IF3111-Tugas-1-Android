package com.example.rikysamuel.tomjerry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rikysamuel on 3/6/2015.
 */
public class fragmentStart extends Fragment {
    private static final int REQUEST_CODE = 10;     // http request
    private static final int QR_REQUEST_CODE = 20;  // qr-code

    private double latitude;
    private double longitude;
    private int valid_until;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_start_layout, container, false);

        // spikeOnClick
        ImageButton spike = (ImageButton) v.findViewById(R.id.imageButton); // you have to use rootview object..
        spike.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                Toast.makeText(getActivity(), "HTTP Request", Toast.LENGTH_LONG).show();
                Intent i = new Intent(getActivity(),getLatLng.class);
                startActivityForResult(i, REQUEST_CODE);
                TextView t = (TextView) getActivity().findViewById(R.id.textView);
                t.setText("HTTP Request......");
            }
        });

        //catchOnClick
        ImageButton catchJerry = (ImageButton) v.findViewById(R.id.imageButton2); // you have to use rootview object..
        catchJerry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                Toast.makeText(getActivity(), "QR-Code Scanner", Toast.LENGTH_LONG).show();
                Intent i = new Intent(getActivity(),qrCode.class);
                startActivityForResult(i, QR_REQUEST_CODE);
                TextView t = (TextView) getActivity().findViewById(R.id.textView);
                t.setText("Opening App... Please wait.....");
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            int lon; int lat; int val;
            if (data.hasExtra("long")) {
                longitude = data.getExtras().getDouble("long");
            }
            if (data.hasExtra("lat")){
                latitude = data.getExtras().getDouble("lat");
            }
            if (data.hasExtra("val")){
                valid_until = data.getExtras().getInt("val");
            }

            TextView t = (TextView) getActivity().findViewById(R.id.textView);
            t.setText("long: " + longitude + " lat: " + latitude + " val: " + valid_until);
            alert();
        }
        if (resultCode == Activity.RESULT_OK && requestCode == QR_REQUEST_CODE){
            String response = "";
            if (data.hasExtra("response")){
                response = data.getExtras().getString("response");
            }
            if (response!=null){
                TextView t = (TextView) getActivity().findViewById(R.id.textView);
                try {
                    JSONObject jso = new JSONObject(response);
                    String msg = jso.getString("message");
                    int code = jso.getInt("code");
                    t.setText("Message: " + msg + " with code: " + code);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else{
                TextView t = (TextView) getActivity().findViewById(R.id.textView);
                t.setText("No response");
            }
        }
    }

    public void alert(){
        new AlertDialog.Builder(getActivity())
                .setTitle("Google Maps")
                .setMessage("Do you want to see Jerry's Position on Map?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Google Maps",Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getActivity(), MapsActivity.class);
                        i.putExtra("long", longitude);
                        i.putExtra("lat", latitude);
                        i.putExtra("val", valid_until);
                        startActivity(i);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


}
