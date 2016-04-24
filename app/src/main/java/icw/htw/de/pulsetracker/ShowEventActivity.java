package icw.htw.de.pulsetracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.htw.icw.pulsesensorlib.DefaultHeartRateEvent;
import de.htw.icw.pulsesensorlib.DefaultHeartRateMonitor;
import de.htw.icw.pulsesensorlib.HeartRateEvent;
import de.htw.icw.pulsesensorlib.HeartRateListener;
import de.htw.icw.pulsesensorlib.HeartRateMonitor;
import de.htw.icw.pulsesensorlib.NoNegativeHeartRatesPossibleException;

public class ShowEventActivity extends Activity implements HeartRateListener {

    private static final String TAG = ShowEventActivity.class.getName();

    private BluetoothLeService bluetoothLeService;
    private String mDeviceAddress;
    private boolean isConnected = false;

    private LineChart lineChart;
    private LineData lineData;
    private LineDataSet lineDataSetHeartRates;
    private LineDataSet lineDataSetEvents;

    private HeartRateMonitor heartRateMonitor;
    private HeartRateEvent heartRateEvent;

    private MyListAdapter listViewAdapter;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                Log.d(TAG, "connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                Log.d(TAG, "disconnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(bluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String pulseData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d(TAG, "received data: " + pulseData);

                double pulseDataValue = Double.parseDouble(pulseData);

                addHeartRateToMonitorAndUpdateView(pulseDataValue);
            }
        }
    };

    private void addHeartRateToMonitorAndUpdateView(double pulseDataValue) {

        try {
            long timestamp = heartRateMonitor.addHeartRate(pulseDataValue);

            TextView textViewPulseValue = (TextView) findViewById(R.id.textViewValuePulse);
            textViewPulseValue.setText(String.valueOf(pulseDataValue));

            Entry entry = new Entry((float) pulseDataValue, lineDataSetHeartRates.getValueCount());

            lineData.addXValue(String.valueOf(timestamp));

            lineDataSetHeartRates.addEntry(entry);
            lineDataSetHeartRates.notifyDataSetChanged();

            lineChart.notifyDataSetChanged();
            lineChart.moveViewToX(lineDataSetHeartRates.getValueCount());
            lineChart.invalidate();

        } catch (NoNegativeHeartRatesPossibleException e) {
            Log.e(TAG, "received negative pulse value: " + e.toString());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showevent);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //load default values for control items from sharedpreferences file
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.key_preferences_file), Context.MODE_PRIVATE);

        int defaultValueHighPulse = getResources().getInteger(R.integer.default_high_pulse);
        int valueHighPulse = sharedPref.getInt(getString(R.string.key_high_pulse), defaultValueHighPulse);

        int defaultValueLowPulse = getResources().getInteger(R.integer.default_low_pulse);
        int valueLowPulse = sharedPref.getInt(getString(R.string.key_low_pulse), defaultValueLowPulse);

        int defaultValueWaitingTime = getResources().getInteger(R.integer.default_waiting_time);
        int valueWaitingTime = sharedPref.getInt(getString(R.string.key_waiting_time), defaultValueWaitingTime);

        int defaultObservationTimeInMillisecons = getResources().getInteger(R.integer.default_waiting_time_variation);
        int valueObservationTimeInMillisecons = sharedPref.getInt(getString(R.string.key_waiting_time_variation), defaultObservationTimeInMillisecons);

        int defaultVariationTresholdPercentage = getResources().getInteger(R.integer.default_variation_percentage);
        int valueVariationTresholdPercentage = sharedPref.getInt(getString(R.string.key_variation_percentage), defaultVariationTresholdPercentage);

        //ListView
        ListView listView = (ListView) findViewById(R.id.listViewLastReceivedEvent);
        listViewAdapter = new MyListAdapter(this);
        listView.setAdapter(listViewAdapter);

        //PulseTrackingLib
        heartRateMonitor = new DefaultHeartRateMonitor();
        heartRateEvent = new DefaultHeartRateEvent(valueHighPulse, valueLowPulse, valueWaitingTime, valueObservationTimeInMillisecons, valueVariationTresholdPercentage);
        heartRateMonitor.subscribe(heartRateEvent);
        heartRateEvent.subscribe(this);

        //LineChart
        List<Entry> entryList = new ArrayList<Entry>();

        this.lineDataSetHeartRates = new LineDataSet(entryList, "HeartRates");
        lineDataSetHeartRates.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetHeartRates.notifyDataSetChanged();
        lineDataSetHeartRates.setDrawCircles(false);
        lineDataSetHeartRates.setDrawFilled(true);

        this.lineDataSetEvents = new LineDataSet(new ArrayList<Entry>(), "Events");
        lineDataSetEvents.setCircleColor(Color.MAGENTA);
        lineDataSetEvents.setLineWidth(0.0f);
        lineDataSetEvents.setDrawValues(true);

        lineData = new LineData();
        this.lineData.addDataSet(lineDataSetHeartRates);
        this.lineData.addDataSet(lineDataSetEvents);

        lineData.setDrawValues(false);
        lineData.setHighlightEnabled(false);

        this.lineChart = (LineChart) findViewById(R.id.lineChartPulse);
        lineChart.setVisibleXRange(0, 10);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(false);//no legend

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinValue(0f);
        yAxis.setAxisMaxValue(200f);
//        yAxis.setStartAtZero(false);

        lineChart.getAxisRight().setEnabled(false);//no right axis needed

        lineChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothLeService != null) {
            final boolean result = bluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        bluetoothLeService = null;
        heartRateMonitor.unsubscribe(heartRateEvent);
        heartRateEvent.unsubscribe(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_showevent);
        item.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            Intent intent = new Intent(this, DeviceControlActivity.class);
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
            startActivity(intent);
        }
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, DeviceScanActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onHighPulse(double pulse) {
        addEventToLastEventListView("high pulse "+String.valueOf(pulse));
        addEntryToGraph(pulse);
    }

    @Override
    public void onLowPulse(double pulse) {
        addEventToLastEventListView("low pulse "+String.valueOf(pulse));
        addEntryToGraph(pulse);
    }

    @Override
    public void onNoPulse() {
        addEventToLastEventListView("no pulse");
        addEntryToGraph(0);
    }

    @Override
    public void onPulseIncreased(double startHeartRate, double endHeartRate, long startTimestamp, long endTimestamp, double increasingPercantage) {
        String startIncreaseString = String.valueOf(startHeartRate);
        String endIncreaseString = String.valueOf(endHeartRate);

        addEventToLastEventListView("onPulseIncreased from "+startIncreaseString+" to "+endIncreaseString+" over "+(int)increasingPercantage+"%");

        addEntryToGraph(startHeartRate);
        addEntryToGraph(endHeartRate);
    }

    @Override
    public void onPulseDecreased(double startHeartRate, double endHeartRate, long startTimestamp, long endTimestamp, double decreasingPercantage) {
        String startDecreaseString = String.valueOf(startHeartRate);
        String endDecreaseString = String.valueOf(endHeartRate);

        addEventToLastEventListView("onPulseDecreased from "+startDecreaseString+" to "+endDecreaseString+" over "+(int)decreasingPercantage+"%");

        addEntryToGraph(startHeartRate);
        addEntryToGraph(endHeartRate);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onSubscribed() {
        addEventToLastEventListView("onSubscribed()");
    }

    @Override
    public void onUnsubscribed() {
        addEventToLastEventListView("onUnubscribed()");
    }

    private void addEntryToGraph(double pulse) {

        Entry entry = new Entry((float) pulse, lineDataSetHeartRates.getValueCount());

        lineDataSetEvents.addEntry(entry);
        lineDataSetEvents.notifyDataSetChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private void addEventToLastEventListView(String event) {

        SimpleDateFormat sdfmt = new SimpleDateFormat();
        sdfmt.applyPattern("hh:mm:ss");

        listViewAdapter.add(new String[]{sdfmt.format(new Date()), event});
        listViewAdapter.notifyDataSetChanged();

        ListView listView = (ListView) findViewById(R.id.listViewLastReceivedEvent);
        listView.setSelection(listView.getCount() - 1);
    }

    class MyListAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<String[]> values;

        MyListAdapter(Context context) {
            this.context = context;
            this.values = new ArrayList<String[]>();
        }

        public void add(String[] value) {
            values.add(value);
        }

        @Override
        public int getCount() {
            return values.size();
        }

        @Override
        public Object getItem(int i) {
            return values.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.listitem_heartrate, parent, false);


            TextView text_timestamp = (TextView) view.findViewById(R.id.textview_timestamp);
            TextView text_heartrate = (TextView) view.findViewById(R.id.textview_heartrate);

            text_timestamp.setText(values.get(position)[0]);
            text_heartrate.setText(values.get(position)[1]);

            return view;
        }
    }
}
