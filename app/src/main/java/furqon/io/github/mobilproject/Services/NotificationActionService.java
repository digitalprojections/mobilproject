package furqon.io.github.mobilproject.Services;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import furqon.io.github.mobilproject.Furqon;

    public class NotificationActionService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("TRACKS_TRACKS")
        .putExtra("actionname", Furqon.ACTION_PREV));



    }
}
