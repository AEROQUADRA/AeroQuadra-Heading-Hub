package com.example.aeroquadraheadinghub;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private TextView headingTextView;
    private EditText ipEditText, portEditText;
    private Button connectButton;

    private boolean isConnected = false;
    private Socket socket;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        headingTextView = findViewById(R.id.headingTextView);
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);
        connectButton = findViewById(R.id.connectButton);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        // Set initial heading text
        if (rotationVectorSensor == null) {
            headingTextView.setText("Rotation vector sensor not available");
        } else {
            headingTextView.setText("Heading: 0°");
        }

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected) {
                    connectToServer();
                } else {
                    disconnectFromServer();
                }
            }
        });

        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            } else {
                registerSensorListener();
            }
        } else {
            registerSensorListener();
        }
    }

    private void registerSensorListener() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Rotation vector sensor not available.", Toast.LENGTH_SHORT).show();
            // Update heading text
            headingTextView.setText("Rotation vector sensor not available");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rotationVectorSensor != null) {
            registerSensorListener();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rotationVectorSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            // Convert radians to degrees
            float azimuth = (float) Math.toDegrees(orientation[0]);
            azimuth = (azimuth + 360) % 360;

            final String heading = "Heading: " + Math.round(azimuth) + "°";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    headingTextView.setText(heading);
                }
            });

            if (isConnected) {
                sendToServer(heading);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerSensorListener();
            } else {
                Toast.makeText(this, "Permission denied. App needs location permission to function.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void connectToServer() {
        String ipAddress = ipEditText.getText().toString().trim();
        String portStr = portEditText.getText().toString().trim();

        if (ipAddress.isEmpty() || portStr.isEmpty()) {
            Toast.makeText(this, "Please enter IP address and port number.", Toast.LENGTH_SHORT).show();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid port number.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ipAddress, port);
                    outputStream = socket.getOutputStream();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Connected to server.", Toast.LENGTH_SHORT).show();
                            isConnected = true;
                            connectButton.setText("Disconnect");
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void disconnectFromServer() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            Toast.makeText(this, "Disconnected from server.", Toast.LENGTH_SHORT).show();
            isConnected = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectButton.setText("Connect");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToServer(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.write(message.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
