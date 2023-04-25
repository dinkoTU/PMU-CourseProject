package in.sunilpaulmathew.weatherwidget.activities;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.sunilpaulmathew.weatherwidget.interfaces.SingleChoiceDialog;
import in.sunilpaulmathew.weatherwidget.BuildConfig;
import in.sunilpaulmathew.weatherwidget.R;
import in.sunilpaulmathew.weatherwidget.adapters.SettingsAdapter;
import in.sunilpaulmathew.weatherwidget.utils.SettingsItems;
import in.sunilpaulmathew.weatherwidget.utils.Utils;
import in.sunilpaulmathew.weatherwidget.utils.Weather;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 23, 2023
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Utils.getOrientation(
                this)  == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1));
        SettingsAdapter mAdapter = new SettingsAdapter(getData());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((position, v) -> {
            if (getData().get(position).getUrl() != null) {
                Utils.launchUrl(getData().get(position).getUrl(), this);
            } else if (position == 0) {
                Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                settings.setData(uri);
                startActivity(settings);
                finish();
            } else if (position == 1) {
                if (Utils.isLocationAccessDenied(this)) {
                    locationPermissionRequest.launch(new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                } else {
                    Utils.saveBoolean("useGPS", !Utils.getBoolean("useGPS", true, this), this);
                    recreate();
                }
            } else if (position == 2) {
                new SingleChoiceDialog(R.drawable.ic_thermostat, getString(R.string.temperature_unit),
                        new String[] {
                                getString(R.string.centigrade),
                                getString(R.string.fahrenheit)
                        }, temperatureUnitPosition(), SettingsActivity.this) {
                    @Override
                    public void onItemSelected(int position) {
                        if (position == temperatureUnitPosition()) return;
                        if (position == 1) {
                            Utils.saveString("temperatureUnit", "&temperature_unit=fahrenheit", SettingsActivity.this);
                        } else {
                            Utils.saveString("temperatureUnit", "", SettingsActivity.this);
                        }
                        Weather.deleteDataFile(SettingsActivity.this);
                        Utils.restartApp(SettingsActivity.this);
                    }
                }.show();
            } else if (position == 3) {
                new SingleChoiceDialog(R.drawable.ic_days, getString(R.string.forecast_days),
                        new String[] {
                                getString(R.string.days, "3"),
                                getString(R.string.days, "7"),
                                getString(R.string.days, "14")
                        }, forecastDaysPosition(), SettingsActivity.this) {
                    @Override
                    public void onItemSelected(int position) {
                        if (position == forecastDaysPosition()) return;
                        if (position == 2) {
                            Utils.saveString("forecastDays", "&forecast_days=14", SettingsActivity.this);
                        } else if (position == 1) {
                            Utils.saveString("forecastDays", "", SettingsActivity.this);
                        } else {
                            Utils.saveString("forecastDays", "&forecast_days=3", SettingsActivity.this);
                        }
                        Weather.deleteDataFile(SettingsActivity.this);
                        Utils.restartApp(SettingsActivity.this);
                    }
                }.show();
            } else if (position == 4) {
                Utils.saveBoolean("transparentBackground", !Utils.getBoolean("transparentBackground", false, this), this);
                Utils.updateWidgets(this);
                recreate();
            }
        });
    }

    private ArrayList<SettingsItems> getData() {
        ArrayList<SettingsItems> mData = new ArrayList<>();
        mData.add(new SettingsItems(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME + " ("
                + BuildConfig.VERSION_CODE + ")", "Copyright: ©2023-2024, sunilpaulmathew",
                Utils.getDrawable(R.drawable.ic_info, this), null));
        mData.add(new SettingsItems(getString(R.string.location_service), getString(R.string.location_service_summary),
                Utils.getDrawable(R.drawable.ic_gps, this), null));
        mData.add(new SettingsItems(getString(R.string.temperature_unit), getTemperatureUnit(),
                Utils.getDrawable(R.drawable.ic_thermostat, this), null));
        mData.add(new SettingsItems(getString(R.string.forecast_days), getForecastDays(),
                Utils.getDrawable(R.drawable.ic_days, this), null));
        mData.add(new SettingsItems(getString(R.string.transparent_background), getString(R.string.transparent_background_summary),
                Utils.getDrawable(R.drawable.ic_eye, this), null));
        mData.add(new SettingsItems(getString(R.string.source_code), getString(R.string.source_code_summary),
                Utils.getDrawable(R.drawable.ic_github, this), "https://github.com/sunilpaulmathew/Weather"));
        mData.add(new SettingsItems(getString(R.string.report_issue), getString(R.string.report_issue_summary),
                Utils.getDrawable(R.drawable.ic_issue, this), "https://github.com/sunilpaulmathew/Weather/issues/new/choose"));
        return mData;
    }

    private int temperatureUnitPosition() {
        if (Utils.getString("temperatureUnit", "", this).equals("&temperature_unit=fahrenheit")) {
            return 1;
        } else {
            return 0;
        }
    }

    private int forecastDaysPosition() {
        String days = Utils.getString("forecastDays", "", this);
        if (days.equals("&forecast_days=14")) {
            return 2;
        } else if (days.equals("&forecast_days=3")) {
            return 0;
        } else {
            return 1;
        }
    }

    private String getTemperatureUnit() {
        if (Utils.getString("temperatureUnit", "", this).equals("&temperature_unit=fahrenheit")) {
            return getString(R.string.fahrenheit);
        } else {
            return getString(R.string.centigrade);
        }
    }

    private String getForecastDays() {
        String days = Utils.getString("forecastDays", "", this);
        if (days.equals("&forecast_days=14")) {
            return getString(R.string.days, "14");
        } else if (days.equals("&forecast_days=3")) {
            return getString(R.string.days, "3");
        } else {
            return getString(R.string.days, "7");
        }
    }

    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION,false);
                Utils.saveBoolean("useGPS", fineLocationGranted != null && fineLocationGranted
                                || coarseLocationGranted != null && coarseLocationGranted, this);
                        recreate();
                    }
            );

}