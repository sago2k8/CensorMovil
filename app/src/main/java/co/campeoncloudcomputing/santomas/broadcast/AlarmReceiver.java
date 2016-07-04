package co.campeoncloudcomputing.santomas.broadcast;

import co.campeoncloudcomputing.santomas.services.RefreshService;
import co.campeoncloudcomputing.santomas.utils.Sesion;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		if (!Sesion.usuario.isActivo()){
			Intent i = new Intent(context,RefreshService.class);
			context.startService(i);
			Sesion.usuario.setCorriendoServicio(true);
		}
	}

}
