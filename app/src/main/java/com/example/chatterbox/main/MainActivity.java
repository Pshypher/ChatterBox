package com.example.chatterbox.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatterbox.R;
import com.example.chatterbox.messenger.IMActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL = "channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    /**
     * Initializes the relevant properties within this class
     */
    private void init() {

        TextInputEditText topicEditText = findViewById(R.id.topic_edit_text);
        topicEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                {
                    topicEditText.setText("");
                    topicEditText.setTextColor(
                            ContextCompat.getColor(MainActivity.this, android.R.color.black));
                }
            }
        });
        String topic = Objects.requireNonNull(topicEditText.getText()).toString().trim();
        TextView joinButton = findViewById(R.id.join_button);
        TextInputLayout topicInputLayout = findViewById(R.id.topic_input_layout);

        joinButton.setOnClickListener( view -> {
            boolean noError = true;
            if (TextUtils.isEmpty(topic)) {
                topicInputLayout.setError(getString(R.string.error_string));
                noError = false;
            } else {
                topicInputLayout.setError(null);
            }

            // Text input Field is not empty and there is internet connection
            if (noError && isOnline()) {
                Intent intent = new Intent(MainActivity.this, IMActivity.class);
                //Pass the Unique ID through an intent to the Chat Activity
                intent.putExtra(CHANNEL, topic.toLowerCase());

                startActivity(intent);

            } else if(noError && !isOnline())  {
                Toast.makeText(MainActivity.this, getString(R.string.no_internet),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}