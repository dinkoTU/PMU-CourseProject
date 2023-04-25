package in.sunilpaulmathew.weatherwidget.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import in.sunilpaulmathew.weatherwidget.R;
import in.sunilpaulmathew.weatherwidget.activities.MainActivity;
import in.sunilpaulmathew.weatherwidget.interfaces.AcquireWeatherData;
import in.sunilpaulmathew.weatherwidget.interfaces.LocationListener;
import in.sunilpaulmathew.weatherwidget.utils.Utils;
import in.sunilpaulmathew.weatherwidget.utils.Weather;
import in.sunilpaulmathew.weatherwidget.utils.WeatherItems;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 23, 2023
 */
public class WidgetProviderFull extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            try {
                update(appWidgetManager, appWidgetId, context);
            } catch (JSONException | IOException ignored) {
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Utils.remove("appwidget" + appWidgetId, context);
        }
    }

    public static void update(AppWidgetManager appWidgetManager, int appWidgetId, Context context) throws JSONException, IOException {
        RemoteViews mViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_full);
        Intent mIntent = new Intent(context, MainActivity.class);

        new LocationListener(context) {
            @Override
            public void onLocationInitialized(String latitude, String longitude, String address) {
                Utils.saveBoolean("reAcquire", true, context);
                Utils.saveString("latitude", latitude, context);
                Utils.saveString("longitude", longitude, context);
                Utils.saveString("location", address, context);
            }
        }.initialize();

        new AcquireWeatherData(Weather.getLatitude(context), Weather.getLongitude(context), context) {

            @Override
            public void successLister(List<WeatherItems> weatherItems) {

                for (WeatherItems items : weatherItems) {
                    if (items.isSuccess()) {
                        mViews.setImageViewBitmap(R.id.weather_button, Utils.getBitmapFromDrawable(items.getWeatherIcon()));
                        mViews.setTextViewText(R.id.weather_status, items.getWeatherStatus());
                        mViews.setTextViewText(R.id.temperature_status, items.getTemperatureStatus());
                        mViews.setTextColor(R.id.temperature_status, items.getAccentColor(context));
                        mViews.setTextViewText(R.id.temperature_unit, Weather.getTemperatureUnit(context));
                        if (Utils.getBoolean("transparentBackground", false, context)) {
                            mViews.setInt(R.id.layout, "setBackgroundColor", Utils.getColor(android.R.color.transparent, context));
                        } else {
                            mViews.setInt(R.id.layout, "setBackgroundResource", R.drawable.ic_card_background);
                        }
                        PendingIntent mPendingIntent = PendingIntent.getActivity(context, appWidgetId, mIntent, PendingIntent.FLAG_IMMUTABLE);
                        mViews.setOnClickPendingIntent(R.id.layout, mPendingIntent);
                        appWidgetManager.updateAppWidget(appWidgetId, mViews);
                    } else {
                        mViews.setViewVisibility(R.id.temperature_status, View.GONE);
                        mViews.setViewVisibility(R.id.temperature_unit, View.GONE);
                        mViews.setViewVisibility(R.id.weather_button, View.GONE);
                        mViews.setTextColor(R.id.weather_status, ContextCompat.getColor(context, R.color.color_red));
                    }
                }
            }
        }.acquire();

    }

}