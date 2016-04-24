package icw.htw.de.pulsetracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private final static String TAG = SettingsActivity.class.getSimpleName();
    private String mDeviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS);

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

        //setup textviews which contain the current values
        TextView textViewHighPulseValue = (TextView) findViewById(R.id.textViewSetHighPulseValue);
        textViewHighPulseValue.setText(String.valueOf(valueHighPulse));

        TextView textViewLowPulseValue = (TextView) findViewById(R.id.textViewSetLowPulseValue);
        textViewLowPulseValue.setText(String.valueOf(valueLowPulse));

        TextView textViewWaitingTimeValue = (TextView) findViewById(R.id.textViewSetWaitingTimeValue);
        textViewWaitingTimeValue.setText(String.valueOf(valueWaitingTime));

        TextView textViewWaitingTimeVariationValue = (TextView) findViewById(R.id.textViewSetPulseVariationTresholdWaitingTimeValue);
        textViewWaitingTimeVariationValue.setText(String.valueOf(valueObservationTimeInMillisecons));

        TextView textViewPulseVariationPercentageValue = (TextView) findViewById(R.id.textViewSetPulseVariationTresholdPercentageValue);
        textViewPulseVariationPercentageValue.setText(String.valueOf(valueVariationTresholdPercentage));

        //setup seekbars with default values and implement onChangeListeners
        SeekBar setHighPulseSeekbar = (SeekBar) findViewById(R.id.seekBarSetHighPulse);
        setHighPulseSeekbar.setProgress(valueHighPulse);
        setHighPulseSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int newProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                if(progress == 0){
                    seekBar.setProgress(1);
                    newProgress = 1;
                }else {
                    newProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"Set High Pulse: "+newProgress);
                writeToSharedPreferences(getString(R.string.key_high_pulse),newProgress);

                TextView textViewHighPulseValue = (TextView) findViewById(R.id.textViewSetHighPulseValue);
                textViewHighPulseValue.setText(String.valueOf(newProgress));
            }


        });

        SeekBar setLowPulseSeekbar = (SeekBar) findViewById(R.id.seekBarSetLowPulse);
        setLowPulseSeekbar.setProgress(valueLowPulse);
        setLowPulseSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int newProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    seekBar.setProgress(1);
                    newProgress = 1;
                }else {
                    newProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "Set Low Pulse: " + newProgress);
                writeToSharedPreferences(getString(R.string.key_low_pulse), newProgress);

                TextView textViewLowPulseValue = (TextView) findViewById(R.id.textViewSetLowPulseValue);
                textViewLowPulseValue.setText(String.valueOf(newProgress));
            }


        });

        SeekBar setWaitingTimeSeekbar = (SeekBar) findViewById(R.id.seekBarSetWaitingTime);
        setWaitingTimeSeekbar.setProgress(valueWaitingTime/100);
        setWaitingTimeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int newProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    seekBar.setProgress(1);
                    newProgress = 1;
                }else {
                    newProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int realWaitingTime = newProgress*100;

                Log.d(TAG, "Set Waiting Time: " + realWaitingTime);
                writeToSharedPreferences(getString(R.string.key_waiting_time), realWaitingTime);

                TextView textViewWaitingTimeValue = (TextView) findViewById(R.id.textViewSetWaitingTimeValue);
                textViewWaitingTimeValue.setText(String.valueOf(realWaitingTime));
            }
        });

        SeekBar setPulseVariationPercentageTresholdSeekBar = (SeekBar) findViewById(R.id.seekBarSetPulseVariationTresholdPercentage);
        setPulseVariationPercentageTresholdSeekBar.setProgress(valueVariationTresholdPercentage);
        setPulseVariationPercentageTresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int newProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                if(progress == 0){
                    seekBar.setProgress(1);
                    newProgress = 1;
                }else {
                    newProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"Set Pulse Variation Percentage Treshold: "+newProgress);
                writeToSharedPreferences(getString(R.string.key_variation_percentage),newProgress);

                TextView textViewSetPulseVariationPercentageValue = (TextView) findViewById(R.id.textViewSetPulseVariationTresholdPercentageValue);
                textViewSetPulseVariationPercentageValue.setText(String.valueOf(newProgress));
            }
        });

        SeekBar setWaitingTimeVariationSeekbar = (SeekBar) findViewById(R.id.seekBarSetPulseVariationTresholdWaitingTime);
        setWaitingTimeVariationSeekbar.setProgress(valueObservationTimeInMillisecons/100);
        setWaitingTimeVariationSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int newProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    seekBar.setProgress(1);
                    newProgress = 1;
                }else {
                    newProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int realWaitingTime = newProgress*100;

                Log.d(TAG, "Set Waiting Time Variation: " + realWaitingTime);
                writeToSharedPreferences(getString(R.string.key_waiting_time_variation), realWaitingTime);

                TextView textViewWaitingTimeVariationValue = (TextView) findViewById(R.id.textViewSetPulseVariationTresholdWaitingTimeValue);
                textViewWaitingTimeVariationValue.setText(String.valueOf(realWaitingTime));
            }
        });
    }

    private void writeToSharedPreferences(String key, int value){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.key_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_settings);
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
        if (id == R.id.action_showevent) {
            Intent intent = new Intent(this,ShowEventActivity.class);
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
            startActivity(intent);
        }
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, DeviceScanActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
