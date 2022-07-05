package com.example.linecodesencoder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {

    Spinner codeLining;
    GraphView graph;

    EditText etBitDuration;
    EditText etAmplitude;

    TextView tvBitNumber;
    EditText etBitNumber;

    TextView tvBitVector;
    EditText etBitVector;

    Button encode;

    ImageView random, deterministic;

    int bitDuration, Amplitude;

    boolean randomB = true;

    ArrayList<Boolean> bitVector;
    int vectorLength;

    TextView tvRandomBitNumber;

    LinkedList<LineGraphSeries<DataPoint>> LineCodingSeries;

    boolean drawing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        codeLining = findViewById(R.id.lineCoding);
        graph = findViewById(R.id.graph);

        etBitDuration = findViewById(R.id.bitDuration);
        etAmplitude = findViewById(R.id.etAmplitude);

        etAmplitude.setInputType(InputType.TYPE_CLASS_NUMBER);
        etBitDuration.setInputType(InputType.TYPE_CLASS_NUMBER);

        encode = findViewById(R.id.encode);

        random = findViewById(R.id.random);
        deterministic = findViewById(R.id.deterministic);

        etBitNumber = findViewById(R.id.etBitNumber);
        tvBitNumber = findViewById(R.id.tvBitNumber);

        etBitVector = findViewById(R.id.etBitVector);
        tvBitVector = findViewById(R.id.tvBitVector);

        etBitNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        etBitVector.setInputType(InputType.TYPE_CLASS_NUMBER);

        tvRandomBitNumber = findViewById(R.id.tvRandomBitsN);

        String[] codes = new String[]{"Unipolar NRZ", "Polar RZ", "Alternate Mark Inversion", "Manchester"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, codes);
        codeLining.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= 16) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }

        encode.setOnClickListener(view -> {
            if (isInputValid() && !drawing) {
                drawing = true;
                encodeFunction();
                drawing = false;
            }
        });

        random.setOnClickListener(view -> {
            if (!randomB) {
                random.setImageResource(R.drawable.tick);
                deterministic.setImageResource(R.drawable.untick);
                randomB = true;

                tvBitNumber.setVisibility(View.VISIBLE);
                etBitNumber.setVisibility(View.VISIBLE);

                tvBitVector.setVisibility(View.GONE);
                etBitVector.setVisibility(View.GONE);
            }

        });
        deterministic.setOnClickListener(view -> {
            if (randomB) {
                random.setImageResource(R.drawable.untick);
                deterministic.setImageResource(R.drawable.tick);
                randomB = false;

                tvBitVector.setVisibility(View.VISIBLE);
                etBitVector.setVisibility(View.VISIBLE);

                tvBitNumber.setVisibility(View.GONE);
                etBitNumber.setVisibility(View.GONE);
            }

        });

    }

    private boolean isInputValid() {
        if (etAmplitude.getText().toString().length() < 1) {
            Toast.makeText(getBaseContext(), "Enter signal Amplitude!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etAmplitude.getText().toString().length() < 1) {
            Toast.makeText(getBaseContext(), "Enter bit Duration!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (randomB) {
            if (etBitNumber.getText().toString().length() < 1) {
                Toast.makeText(getBaseContext(), "Enter bit Number!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (etBitVector.getText().toString().length() > 0) {
                String vector = etBitVector.getText().toString();
                for (int i = 0; i < vector.length(); i++) {
                    char c = vector.charAt(i);
                    if (c != '0' && c != '1') {
                        Toast.makeText(getBaseContext(), "Vector must consist of 1's and 0's only!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void encodeFunction() {
        if (bitVector == null)
            bitVector = new ArrayList<>();
        else
            bitVector.clear();

        Amplitude = Integer.parseInt(etAmplitude.getText().toString());
        bitDuration = Integer.parseInt(etBitDuration.getText().toString());


        if (randomB) {
            vectorLength = Integer.parseInt(etBitNumber.getText().toString());
            StringBuilder bits = new StringBuilder();
            if (vectorLength > 20)
                vectorLength = 20;
            for (int i = 0; i < vectorLength; i++) {
                if (Math.random() < 0.5) {
                    bitVector.add(false);
                    bits.append("0");
                } else {
                    bitVector.add(true);
                    bits.append("1");
                }
            }
            tvRandomBitNumber.setText(bits.toString());
        } else {
            String vector = etBitVector.getText().toString();
            for (int i = 0; i < vector.length(); i++) {
                if (vector.charAt(i) == '0')
                    bitVector.add(false);
                else
                    bitVector.add(true);
            }
            tvRandomBitNumber.setText("-------");
        }
        switch (codeLining.getSelectedItemPosition()) {
            case 0:
                Log.d("Drawing", "Unipolar NRZ");
                UnipolarNRZ();
                break;
            case 1:
                Log.d("Drawing", "Polar RZ");
                PolarRZ();
                break;
            case 2:
                Log.d("Drawing", "Alternate Mark Inversion");
                AMI();
                break;
            case 3:
                Log.d("Drawing", "Manchester");
                Manchester();
                break;
            default:
                Log.d("Code", "None");
                break;
        }
    }

    private void UnipolarNRZ() {
        graph.removeAllSeries();
        double t, amp;
        if (LineCodingSeries == null)
            LineCodingSeries = new LinkedList<>();
        else
            LineCodingSeries.clear();
        for (int k = 0; k < bitVector.size(); k++) {
            boolean color = (k % 2 == 0);
            LineCodingSeries.add(new LineGraphSeries<>());
            int pointsNumber;
            if (k == 0) {
                t = 0;
                amp = 0;
                if (bitVector.get(k)) {
                    pointsNumber = (int) 500 * Amplitude;
                    for (int i = 0; i < pointsNumber; i++) {
                        amp += 0.002;
                        try {
                            LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                        } catch (Exception e) {
                            Log.d("Unipolar NRZ", "Error");
                        }
                    }
                    if (color)
                        LineCodingSeries.getLast().setColor(Color.BLUE);
                    else
                        LineCodingSeries.getLast().setColor(Color.RED);
                    graph.addSeries(LineCodingSeries.getLast());
                    Log.d("Unipolar NRZ", "Vertical Success");
                }

                LineCodingSeries.add(new LineGraphSeries<>());
                pointsNumber = (int) 100 * bitDuration;
                for (int i = 0; i < pointsNumber; i++) {
                    t += 0.01;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Unipolar NRZ", "Error");
                    }
                }
            } else {
                t = k * bitDuration;
                if (bitVector.get(k) != bitVector.get(k - 1)) {
                    amp = 0;
                    LineCodingSeries.add(new LineGraphSeries<>());
                    pointsNumber = (int) 500 * Amplitude;
                    for (int i = 0; i < pointsNumber; i++) {
                        amp += 0.002;
                        try {
                            LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                        } catch (Exception e) {
                            Log.d("Unipolar NRZ", "Error");
                        }
                    }
                    if (color)
                        LineCodingSeries.getLast().setColor(Color.BLUE);
                    else
                        LineCodingSeries.getLast().setColor(Color.RED);
                    graph.addSeries(LineCodingSeries.getLast());
                    Log.d("Unipolar NRZ", "Vertical Success");
                }
                if (bitVector.get(k)) {
                    amp = Amplitude;
                    LineCodingSeries.getLast().setColor(Color.BLUE);
                } else {
                    amp = 0;
                    LineCodingSeries.getLast().setColor(Color.RED);
                }
                pointsNumber = (int) 100 * bitDuration;
                LineCodingSeries.add(new LineGraphSeries<>());
                for (int i = 0; i < pointsNumber; i++) {
                    t += 0.01;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Unipolar NRZ", "Error");
                    }
                }
            }
            if (color)
                LineCodingSeries.getLast().setColor(Color.BLUE);
            else
                LineCodingSeries.getLast().setColor(Color.RED);
            graph.addSeries(LineCodingSeries.getLast());
            Log.d("Unipolar NRZ", "Horizontal Success");
            if (k == bitVector.size() - 1 && bitVector.get(k)) {
                amp = 0;
                pointsNumber = (int) 500 * Amplitude;
                LineCodingSeries.add(new LineGraphSeries<>());
                for (int i = 0; i < pointsNumber; i++) {
                    amp += 0.002;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Unipolar NRZ", "Error");
                    }
                }
                if (color)
                    LineCodingSeries.getLast().setColor(Color.BLUE);
                else
                    LineCodingSeries.getLast().setColor(Color.RED);
                graph.addSeries(LineCodingSeries.getLast());
                Log.d("Unipolar NRZ", "Vertical Success");
            }
        }
    }

    private void PolarRZ() {
        graph.removeAllSeries();
        double t, amp;
        if (LineCodingSeries == null)
            LineCodingSeries = new LinkedList<>();
        else
            LineCodingSeries.clear();

        for (int k = 0; k < bitVector.size(); k++) {
            boolean color = (k % 2 == 0);
            int pointsNumber;
            t = k * bitDuration;
            amp = 0;
            pointsNumber = (int) 500 * Amplitude;
            LineCodingSeries.add(new LineGraphSeries<>());
            if (bitVector.get(k)) {
                for (int i = 0; i < pointsNumber; i++) {
                    amp += 0.002;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Polar RZ", "Error");
                    }
                }
            } else {
                for (int i = 0; i < pointsNumber; i++) {
                    amp -= 0.002;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Polar RZ", "Error");
                    }
                }
            }
            if (color)
                LineCodingSeries.getLast().setColor(Color.BLUE);
            else
                LineCodingSeries.getLast().setColor(Color.RED);
            graph.addSeries(LineCodingSeries.getLast());
            Log.d("Polar RZ", "Vertical Success");

            pointsNumber = (int) 100 * bitDuration / 2;
            LineCodingSeries.add(new LineGraphSeries<>());
            for (int i = 0; i < pointsNumber; i++) {
                t += 0.01;
                try {
                    LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                } catch (Exception e) {
                    Log.d("Polar RZ", "Error");
                }
            }
            if (color)
                LineCodingSeries.getLast().setColor(Color.BLUE);
            else
                LineCodingSeries.getLast().setColor(Color.RED);
            graph.addSeries(LineCodingSeries.getLast());
            Log.d("Polar RZ", "Horizontal Success");

            pointsNumber = (int) 500 * Amplitude;
            LineCodingSeries.add(new LineGraphSeries<>());
            if (bitVector.get(k)) {
                for (int i = 0; i < pointsNumber; i++) {
                    amp -= 0.002;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Polar RZ", "Error");
                    }
                }
            } else {
                for (int i = 0; i < pointsNumber; i++) {
                    amp += 0.002;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Polar RZ", "Error");
                    }
                }
            }
            if (color)
                LineCodingSeries.getLast().setColor(Color.BLUE);
            else
                LineCodingSeries.getLast().setColor(Color.RED);
            graph.addSeries(LineCodingSeries.getLast());
            Log.d("Polar RZ", "Vertical Success");

            pointsNumber = (int) 100 * bitDuration / 2;
            LineCodingSeries.add(new LineGraphSeries<>());
            for (int i = 0; i < pointsNumber; i++) {
                t += 0.01;
                try {
                    LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                } catch (Exception e) {
                    Log.d("Polar RZ", "Error");
                }
            }
            if (color)
                LineCodingSeries.getLast().setColor(Color.BLUE);
            else
                LineCodingSeries.getLast().setColor(Color.RED);
            graph.addSeries(LineCodingSeries.getLast());
            Log.d("Polar RZ", "Horizontal Success");
        }
    }

    private void AMI() {
        graph.removeAllSeries();
        double t, amp;
        if (LineCodingSeries == null)
            LineCodingSeries = new LinkedList<>();
        else
            LineCodingSeries.clear();

        boolean ones = true;

        for (int k = 0; k < bitVector.size(); k++) {
            boolean color = (k % 2 == 0);
            int pointsNumber;
            t = k * bitDuration;
            amp = 0;
            pointsNumber = (int) 500 * Amplitude;
            LineCodingSeries.add(new LineGraphSeries<>());
            if (bitVector.get(k)) {
                double adder;
                if (ones)
                    adder = 0.002;
                else
                    adder = -0.002;
                ones = !ones;
                for (int i = 0; i < pointsNumber; i++) {
                    amp += adder;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("AMI", "Error");
                    }
                }

                if (color)
                    LineCodingSeries.getLast().setColor(Color.BLUE);
                else
                    LineCodingSeries.getLast().setColor(Color.RED);
                graph.addSeries(LineCodingSeries.getLast());
                Log.d("AMI", "Vertical Success");

                pointsNumber = (int) 100 * bitDuration / 2;
                LineCodingSeries.add(new LineGraphSeries<>());
                for (int i = 0; i < pointsNumber; i++) {
                    t += 0.01;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("AMI", "Error");
                    }
                }
                if (color)
                    LineCodingSeries.getLast().setColor(Color.BLUE);
                else
                    LineCodingSeries.getLast().setColor(Color.RED);
                graph.addSeries(LineCodingSeries.getLast());
                Log.d("AMI", "Horizontal Success");

                pointsNumber = (int) 500 * Amplitude;
                LineCodingSeries.add(new LineGraphSeries<>());
                for (int i = 0; i < pointsNumber; i++) {
                    amp -= adder;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("AMI", "Error");
                    }
                }
                if (color)
                    LineCodingSeries.getLast().setColor(Color.BLUE);
                else
                    LineCodingSeries.getLast().setColor(Color.RED);
                graph.addSeries(LineCodingSeries.getLast());
                Log.d("AMI", "Vertical Success");

                pointsNumber = (int) 100 * bitDuration / 2;
                LineCodingSeries.add(new LineGraphSeries<>());
                for (int i = 0; i < pointsNumber; i++) {
                    t += 0.01;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("AMI", "Error");
                    }
                }
                if (color)
                    LineCodingSeries.getLast().setColor(Color.BLUE);
                else
                    LineCodingSeries.getLast().setColor(Color.RED);
                graph.addSeries(LineCodingSeries.getLast());
                Log.d("AMI", "Horizontal Success");
            } else {
                pointsNumber = (int) 100 * bitDuration;
                for (int i = 0; i < pointsNumber; i++) {
                    t += 0.01;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("AMI", "Error");
                    }
                }
                if (color)
                    LineCodingSeries.getLast().setColor(Color.BLUE);
                else
                    LineCodingSeries.getLast().setColor(Color.RED);
                graph.addSeries(LineCodingSeries.getLast());
                Log.d("AMI", "Horizontal Success");
            }
        }
    }

    private void Manchester() {
        graph.removeAllSeries();
        double t, amp;
        if (LineCodingSeries == null)
            LineCodingSeries = new LinkedList<>();
        else
            LineCodingSeries.clear();

        for (int k = 0; k < bitVector.size(); k++) {
            boolean color = (k % 2 == 0);
            int pointsNumber;
            t = k * bitDuration;
            if (k == 0 || bitVector.get(k) == bitVector.get(k - 1)) {
                amp = 0;
                pointsNumber = (int) 500 * Amplitude;
                LineCodingSeries.add(new LineGraphSeries<>());
                if (bitVector.get(k)) {
                    for (int i = 0; i < pointsNumber; i++) {
                        amp += 0.002;
                        try {
                            LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                        } catch (Exception e) {
                            Log.d("Manchester", "Error");
                        }
                    }
                } else {
                    for (int i = 0; i < pointsNumber; i++) {
                        amp -= 0.002;
                        try {
                            LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                        } catch (Exception e) {
                            Log.d("Manchester", "Error");
                        }
                    }
                }
                if (color)
                    LineCodingSeries.getLast().setColor(Color.BLUE);
                else
                    LineCodingSeries.getLast().setColor(Color.RED);
                graph.addSeries(LineCodingSeries.getLast());
                Log.d("Manchester", "Vertical Success");
            }
            if (bitVector.get(k))
                amp = Amplitude;
            else
                amp = -Amplitude;

            pointsNumber = (int) 100 * bitDuration / 2;
            LineCodingSeries.add(new LineGraphSeries<>());
            for (int i = 0; i < pointsNumber; i++) {
                t += 0.01;
                try {
                    LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                } catch (Exception e) {
                    Log.d("Manchester", "Error");
                }
            }
            if (color)
                LineCodingSeries.getLast().setColor(Color.BLUE);
            else
                LineCodingSeries.getLast().setColor(Color.RED);
            graph.addSeries(LineCodingSeries.getLast());
            Log.d("Manchester", "Horizontal Success");

            pointsNumber = (int) 1000 * Amplitude;
            LineCodingSeries.add(new LineGraphSeries<>());
            if (bitVector.get(k)) {
                for (int i = 0; i < pointsNumber; i++) {
                    amp -= 0.002;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Manchester", "Error");
                    }
                }
            } else {
                for (int i = 0; i < pointsNumber; i++) {
                    amp += 0.002;
                    try {
                        LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Manchester", "Error");
                    }
                }
            }
            if (color)
                LineCodingSeries.getLast().setColor(Color.BLUE);
            else
                LineCodingSeries.getLast().setColor(Color.RED);
            graph.addSeries(LineCodingSeries.getLast());
            Log.d("Manchester", "Vertical Success");

            pointsNumber = (int) 100 * bitDuration / 2;
            LineCodingSeries.add(new LineGraphSeries<>());
            for (int i = 0; i < pointsNumber; i++) {
                t += 0.01;
                try {
                    LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                } catch (Exception e) {
                    Log.d("Manchester", "Error");
                }
            }
            if (color)
                LineCodingSeries.getLast().setColor(Color.BLUE);
            else
                LineCodingSeries.getLast().setColor(Color.RED);
            graph.addSeries(LineCodingSeries.getLast());
            Log.d("Manchester", "Horizontal Success");

            if (k == bitVector.size() - 1 || bitVector.get(k) == bitVector.get(k + 1)) {
                pointsNumber = (int) 500 * Amplitude;
                LineCodingSeries.add(new LineGraphSeries<>());
                if (bitVector.get(k)) {
                    for (int i = 0; i < pointsNumber; i++) {
                        amp += 0.002;
                        try {
                            LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                        } catch (Exception e) {
                            Log.d("Manchester", "Error");
                        }
                    }
                } else {
                    for (int i = 0; i < pointsNumber; i++) {
                        amp -= 0.002;
                        try {
                            LineCodingSeries.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                        } catch (Exception e) {
                            Log.d("Manchester", "Error");
                        }
                    }
                }
                if (color)
                    LineCodingSeries.getLast().setColor(Color.BLUE);
                else
                    LineCodingSeries.getLast().setColor(Color.RED);
                graph.addSeries(LineCodingSeries.getLast());
                Log.d("Manchester", "Vertical Success");
            }
        }
    }
}