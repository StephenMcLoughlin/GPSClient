package com.example.stephen.gpsclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText nameText;
    EditText ipAddressText;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nameText = (EditText) findViewById(R.id.nameText);
        ipAddressText = (EditText) findViewById(R.id.addressText);
        submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, GPSActivity.class);
        String name = nameText.getText().toString();
        String ipAddress = ipAddressText.getText().toString();

        intent.putExtra("name", name);
        intent.putExtra("ip", ipAddress);
        startActivity(intent);
    }
}
