package com.android.testlibrary;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.testlibrary.databinding.ActivityMainBinding;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.library.eventanalytics.EventCaptureHelper;
import com.library.eventanalytics.EventContext;
import com.library.eventanalytics.Page;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private EventCaptureHelper eventCaptureHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        EventCaptureHelper.init("xyz");
        eventCaptureHelper = EventCaptureHelper.getInstance(this);
        eventCaptureHelper.setUrl("http://dataout.recosenselabs.com/n18/webhooks");

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                eventCaptureHelper.sendEvent(createEventModel("fab"));

            }
        });

        binding.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                eventCaptureHelper.sendEvent(createEventModel("like"));

            }
        });
    }

    private JsonObject createEventModel(String action) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", "userid");
        jsonObject.addProperty("action_type", action);
        jsonObject.addProperty("item_id", "itemid");
        jsonObject.addProperty("rate_value", "4");
        jsonObject.addProperty("client_id", "clientid");
        jsonObject.addProperty("channel", "Android app");

        EventContext eventContext =new EventContext(new Page("xyz","abc","xyz","cdf","zxcv"),
                MainActivity.this);
        // get jsonelement from EventContext object
        JsonElement contextString = EventCaptureHelper.getJsonElement(eventContext);
        jsonObject.add("context",contextString);
        return jsonObject;
    }
}