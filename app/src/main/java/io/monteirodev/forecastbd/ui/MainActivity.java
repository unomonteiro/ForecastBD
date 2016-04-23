package io.monteirodev.forecastbd.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.monteirodev.forecastbd.R;
import io.monteirodev.forecastbd.db.ForecastDataSource;
import io.monteirodev.forecastbd.services.Forecast;
import io.monteirodev.forecastbd.services.ForecastService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();

    protected ForecastDataSource mDataSource;

    protected double[] mTemperatures;
    @Bind(R.id.selectButton) TextView mSelectButton;
    @Bind(R.id.updateButton) TextView mUpdateButton;
    @Bind(R.id.deleteButton) TextView mDeleteButton;
    @Bind(R.id.highTextView) TextView mHighTextView;
    @Bind(R.id.lowTextView) TextView mLowTextView;

    protected long mHighTemp;
    protected long mLowTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getActionBar().hide();

        mDataSource = new ForecastDataSource(MainActivity.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Will open");
        mDataSource.open();
        Log.d(TAG, "did open");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataSource.close();
    }

    @SuppressWarnings("unused") // used, injected by Butter Knife
    @OnClick(R.id.insertButton)
    protected void loadForecastData() {
        ForecastService service = new ForecastService();
        service.loadForecastData(mForecastCallback);
    }

    @SuppressWarnings("unused") // used, injected by Butter Knife
    @OnClick(R.id.selectButton)
    // selectButtonClick(View view)
    protected void selectButtonClick() {
        startActivity(new Intent(MainActivity.this, ViewForecastActivity.class));
    }
    /* @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ViewForecastActivity.class));
            } */

    protected Callback<Forecast> mForecastCallback = new Callback<Forecast>() {
        @Override
        public void success(Forecast forecast, Response response) {
            mTemperatures = new double[forecast.hourly.data.size()];
            for (int i = 0; i < forecast.hourly.data.size(); i++) {
                mTemperatures[i] = forecast.hourly.data.get(i).temperature;
                Log.v(TAG, "Temp " + i + ": " + mTemperatures[i]);
            }

            mDataSource.insertForecast(forecast);
            updateHighAndLow();
            enableOtherButtons();
        }

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    @SuppressWarnings("unused") // used, injected by Butter Knife
    @OnClick(R.id.updateButton)
    protected void updateButtonClick(){
        mDataSource.updateTemperature(100);
        updateHighAndLow();
    }

    @SuppressWarnings("unused") // used, injected by Butter Knife
    @OnClick(R.id.deleteButton)
    protected void deleteButtonClick(){
        mDataSource.deleteAll();
        resetHighAndLow();
    }

    private void updateHighAndLow() {
        mHighTemp = mDataSource.selectHighTemp();
        mLowTemp = mDataSource.selectLowTemp();
        mHighTextView.setText(String.format("Upcoming high: %d", mHighTemp));
        mLowTextView.setText(String.format("Upcoming low: %d", mLowTemp));
    }

    public void enableOtherButtons(){
        mSelectButton.setEnabled(true);
        mUpdateButton.setEnabled(true);
        mDeleteButton.setEnabled(true);
    }

    private void resetHighAndLow() {
        mHighTemp = 0;
        mLowTemp = 0;
        mHighTextView.setText("");
        mLowTextView.setText("");
    }
}
