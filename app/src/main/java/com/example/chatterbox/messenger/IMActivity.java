package com.example.chatterbox.messenger;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterbox.R;
import com.example.chatterbox.main.MainActivity;
import com.example.chatterbox.messenger.models.Message;
import com.google.android.material.textfield.TextInputEditText;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class IMActivity extends AppCompatActivity implements MessageAdapter.OnChangeMessageListener {

    private String topic;
    private boolean edit;

    private int messageId;

    private TextInputEditText msgInputEditText;
    private MessageAdapter adapter;

    private MqttAndroidClient client;
    private MqttConnectOptions options;

    private Vibrator vibrator;
    private Ringtone ringtone;
    private Toast toast;

    private static final String MQTT_HOST = "tcp://3.81.24.74:1883";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String TAG = "IMActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im);

        init();
        connect();
        Log.d(TAG, "onCreate: " + getMacAddress());
    }

    private void init() {
        msgInputEditText = findViewById(R.id.msg_input_edit_text);
        msgInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    msgInputEditText.setText("");
                    msgInputEditText.setTextColor(
                            ContextCompat.getColor(IMActivity.this, android.R.color.black));
                } else {
                    if (TextUtils.isEmpty(Objects.requireNonNull(
                            msgInputEditText.getText()).toString().trim()))
                        msgInputEditText.setTextColor(
                                ContextCompat.getColor(IMActivity.this, R.color.light_gray));
                    msgInputEditText.setText(getString(R.string.chat_box_hint));
                }
            }
        });
        ImageButton sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(view -> {
            String message = Objects.requireNonNull(msgInputEditText.getText()).toString();
            if (TextUtils.isEmpty(message)) return;
            msgInputEditText.setText("");
            publish(message);
        });
        RecyclerView chatRecyclerView = findViewById(R.id.chat_recycler_view);
        adapter = MessageAdapter.getInstance(getMacAddress(), new ArrayList<Message>(), this);
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
                    subscribe();
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

    public void subscribe() {
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
                Message msg = parse(message);
                if (msg.getSource().equals(getMacAddress())) return;
                adapter.addMessage(msg);
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
        String[] fields = payload.split(",");
        return new Message(fields[0], fields[1], Long.parseLong(fields[2]));
    }

    public void publish(String body) {
        if (!client.isConnected()) return;

        String source = getMacAddress();
        long timestamp = System.currentTimeMillis();

        try {
            String message = String.format(Locale.getDefault(), "%s,%s,%d", source, body, timestamp);
            if (!edit) {
                adapter.addMessage(new Message(source, body, timestamp));
            }
            else adapter.editMessage(body, messageId);
            edit = false;
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ignored) {

        }
        return "02:00:00:00:00:00";
    }

    @Override
    public void onEditClicked(Message msg, int index) {
        edit = true;
        messageId = index;
        msgInputEditText.setText(msg.getMessageBody());
    }
}