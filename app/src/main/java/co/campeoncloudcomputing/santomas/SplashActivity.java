package co.campeoncloudcomputing.santomas;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import co.campeoncloudcomputing.santomas.utils.Sesion;
import co.campeoncloudcomputing.santomas.utils.Utils;

public class SplashActivity extends Activity {

	private final int TIMEOUT = 1200;
	private SharedPreferences settings ;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.splash);

		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				Intent miIntent = null;
				if (Sesion.usuario.getIdConductor() == null){
					miIntent = new Intent(SplashActivity.this,LoginActivity.class);
				}else{
					miIntent = new Intent(SplashActivity.this,MainActivity.class);
				}
				SplashActivity.this.startActivity(miIntent);
				SplashActivity.this.finish();
			}
		}, TIMEOUT);
		settings = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
		String fecha = settings.getString("fecha", null);
		if (fecha != null){
			Date fechaConexion = Utils.parse(fecha);
			Calendar cal = Calendar.getInstance();
			cal.setTime(fechaConexion);
			cal.add(Calendar.DATE, 1);

			if (cal.getTime().before(new Date())){
				Sesion.usuario.setIdConductor(null);
			}else{
				Sesion.usuario.setIdConductor(settings.getString("idconductor", null));
			}

		}else{
			Sesion.usuario.setIdConductor(null);
		}
		Typeface bold = Typeface.createFromAsset(this.getAssets(),"fonts/roboto-bold-condensed.ttf");
		Typeface normal = Typeface.createFromAsset(this.getAssets(),"fonts/roboto-condensed.ttf");
		Utils.normal = normal;
	}
}
