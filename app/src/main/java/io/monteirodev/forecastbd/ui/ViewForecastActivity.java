package io.monteirodev.forecastbd.ui;

import android.app.ActionBar;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

import io.monteirodev.forecastbd.R;
import io.monteirodev.forecastbd.db.ForecastDataSource;
import io.monteirodev.forecastbd.db.ForecastHelper;

public class ViewForecastActivity extends ListActivity {

    // declare mDataSource
    protected ForecastDataSource mDataSource;
    protected ArrayList<BigDecimal> mTemperatures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_forecast);

        configureActionBar();

        // Instantiate mDataSource
        mDataSource = new ForecastDataSource(ViewForecastActivity.this);
        mTemperatures = new ArrayList<BigDecimal>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataSource.open();

        Cursor cursor = mDataSource.selectAllTemperatures();
        updateList(cursor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataSource.close();
    }

    private void updateList(Cursor cursor) {
        mTemperatures.clear();

        // Loop through cursor to populate mTemperatures
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            // do stuff
            int i = cursor.getColumnIndex(ForecastHelper.COLUMN_TEMPERATURE);
            double temperature = cursor.getDouble(i);

            // when converting from double adds multiple decimal values
            // instead of: new BigDecimal(temperature));
            // use new BigDecimal(temperature, MathContext.DECIMAL32)
            mTemperatures.add(new BigDecimal(temperature, MathContext.DECIMAL32));

            cursor.moveToNext();
        }

        ArrayAdapter<BigDecimal> adapter = new ArrayAdapter<BigDecimal>(
                ViewForecastActivity.this,
                android.R.layout.simple_list_item_1,
                mTemperatures);

        setListAdapter(adapter);
    }

    protected void filterTemperatures(String minTemp) {
        // select Greater than
        Cursor cursor = mDataSource.selectTempsGreaterThan(minTemp);
        updateList(cursor);

    }

    private void configureActionBar() {
        android.app.ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.numeric_filter);

        final EditText minTempField = (EditText) actionBar
                .getCustomView().findViewById(R.id.minTempField);
        minTempField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                filterTemperatures(minTempField.getText().toString());
                return false;
            }
        });

        // more on bitwise pipe http://www.tutorialspoint.com/java/java_bitwise_operators_examples.htm
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
    }

}
