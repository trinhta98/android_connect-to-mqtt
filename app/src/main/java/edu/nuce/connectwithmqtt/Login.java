package edu.nuce.connectwithmqtt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Login extends Activity {
    private Button btnConnect;
    private Button btnSend;
    private Button btnClear;
    private EditText edtTopic;
    private EditText edtMessage;
    private EditText edtServer;
    private EditText edtPort;
    private EditText edtUser;
    private EditText edtPass;
    private ListView lvListMessage = null;
    private ArrayList<String> listMessage = null;
//    private ArrayAdapter<String> arrAdapter = null;
    private CustomAdapter arrAdapter;
    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private String clientId;
    private static String topic;
    private static boolean check = false;
    private static final String TAG = "mqtt";
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        listMessage = new ArrayList<>();
        arrAdapter = new CustomAdapter(this, R.layout.custom_listview_item, listMessage);
        lvListMessage.setAdapter(arrAdapter);


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!check){
                    connect();
                    btnConnect.setText("Disconnect");
                }else{
                    disconnect();
                    btnConnect.setText("Connect");
                }
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check){
                    publish(edtMessage.getText().toString());
                }
                else {
                    Toast.makeText(Login.this, "You are not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtServer.setText("");
                edtPort.setText("");
                edtUser.setText("");
                edtPass.setText("");
            }
        });
        edtTopic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                topic = edtTopic.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        });
    }
    public void init(){
        mAdView = findViewById(R.id.adView);
        edtServer = findViewById(R.id.edtServer);
        edtPort = findViewById(R.id.edtPort);
        edtUser = findViewById(R.id.edtUser);
        edtPass = findViewById(R.id.edtPass);
        btnConnect = findViewById(R.id.btnConnect);
        btnSend = findViewById(R.id.btnSend);
        btnClear = findViewById(R.id.btnClear);
        edtTopic = findViewById(R.id.edtTopic);
        edtMessage = findViewById(R.id.edtMessage);
        lvListMessage = findViewById(R.id.lvListMessage);



        edtTopic.setText("CAMBIENMUA");
        edtServer.setText("tailor.cloudmqtt.com");
        edtPort.setText("15321");
        edtUser.setText("hkiaooev");
        edtPass.setText("_VOy1bLzPLJt");

        topic = edtTopic.getText().toString();

        options = new MqttConnectOptions();
        options.setUserName(edtUser.getText().toString());
        options.setPassword(edtPass.getText().toString().toCharArray());
        clientId = MqttClient.generateClientId();
    }
    public void connect(){
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + edtServer.getText().toString() +":" + edtPort.getText().toString(), clientId);
        callback();
        try {
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    check = true;
                    Log.d(TAG, "connected");
                    subcribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void disconnect(){
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                    check = false;
                    listMessage.clear();
                    arrAdapter.notifyDataSetChanged();
                    Log.d(TAG, "disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void publish(String payload){
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }
    public void subcribe(){
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Log.d(TAG, "onFailure ");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void unsubcribe(){
        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                    Log.d(TAG, "unsubscribe");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void callback(){
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, message.toString());
                listMessage.add(message.toString());
                arrAdapter.notifyDataSetChanged();
//                Toast.makeText(Login.this, "Topic: " + topic + ", Message: " + message.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
    }
}
