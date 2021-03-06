package com.example.chemproject4;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class Error_page_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_page_activity);
        final Button error_button = (Button) findViewById(R.id.ErrorViewButton); /// used to declare the button listiner.
        error_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                load_errors();

            }
        });

    }
    public ArrayList<String> error_messages = new ArrayList<>();

    public void errors_add(String error){
        error_messages.add(error);
    }

    public void load_errors() {
       TextView error_text_view = findViewById(R.id.error_text_view);
       error_text_view.append("error log start");
        for(int i = 0; i < error_messages.size(); i++){
            error_text_view.setText(error_messages.get(i));
        }
        error_text_view.setText("error log loaded");

    }

}
