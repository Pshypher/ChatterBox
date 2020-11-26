package com.example.chatterbox.chat;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterbox.R;
import com.example.chatterbox.chat.models.Message;
import com.example.chatterbox.entry.MainActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class IMActivity extends AppCompatActivity {

    private String topic;
    private MessageAdapter adapter;
    private RecyclerView chatRecyclerView;


    private MqttAndroidClient client;
    private MqttConnectOptions options;

    private Vibrator vibrator;
    private Ringtone ringtone;
    private Toast toast;

    private static final String MQTT_HOST = "tcp://3.81.24.74:1883";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im);

        init();
    }

    private void init() {
        adapter = MessageAdapter.getInstance();
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(adapter);

        if (getIntent() != null) topic = getIntent().getStringExtra(MainActivity.CHANNEL);
        if (topic == null) {
            displayToast(R.string.error_string);
            return;
        }

        // Add username and password
        options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);

        // Setup MQTT android client as a middleware between the MQTT service and
        // this Activity/Fragment
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(getApplicationContext(), MQTT_HOST, clientId);
    }

    public void connect() {
        // Establish connection to MQTT service
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    displayToast(R.string.connected);
                    subscribe(topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    displayToast(R.string.connection_failed);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void displayToast(@StringRes int message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(IMActivity.this, message,
                Toast.LENGTH_LONG);
        toast.show();
    }

    public void subscribe(final String topic) {
        try {
            client.subscribe(topic, 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                final int DURATION = 500;
                // subText.setText(new String(message.getPayload()));
                Message msg = parse(message);
                vibrator.vibrate(DURATION);
                ringtone.play();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private Message parse(MqttMessage message) {
        String payload = new String(message.getPayload());
        String[] fields = payload.split(" ");
        Message.USER user;
        if (fields[0].equals(Message.USER.RESPONDER)) {
            user = Message.USER.RESPONDER;
        } else {
            user = Message.USER.SENDER;
        }
        return new Message(user, fields[1], Long.parseLong(fields[2]));
    }

    public void publish() {
        if (!client.isConnected()) return;

        

        String message = "Hello World!";
        try {
            String topic = this.topic;
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}