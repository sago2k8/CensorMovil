package co.campeoncloudcomputing.santomas.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import co.campeoncloudcomputing.santomas.MainActivity;
import co.campeoncloudcomputing.santomas.R;
import co.campeoncloudcomputing.santomas.SplashActivity;
import co.campeoncloudcomputing.santomas.broadcast.AlarmReceiver;
import co.campeoncloudcomputing.santomas.entidades.Servicio;
import co.campeoncloudcomputing.santomas.utils.Cons;
import co.campeoncloudcomputing.santomas.utils.Sesion;

public class RefreshService extends IntentService {

	ArrayList<Servicio> servicios;
	public RefreshService() {
		super("RefreshService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		if (servicios == null)
			servicios = new ArrayList<Servicio>();
		getListaServicios();
	}

	private void getListaServicios() {
    	new sincronizarTask().execute();
	}

	private void procesarAlertas() {
		boolean hayChat = false;
		int inServiciosSinAceptar = 0;
		for (Servicio servicio : servicios) {

			if(!hayChat && "1".equals(servicio.getChatSaliente()))
				hayChat = true;

			if (servicio.getTrazabilidad().equals(Cons.ESTADO_INICIAL)){
				inServiciosSinAceptar++;
			}
		}
		 if (hayChat || inServiciosSinAceptar > 0){
			String titulo = "";
			String contenidoCorto = "";
			String contenidoLargo = "";
			Intent intent = new Intent(this, MainActivity.class);
			PendingIntent pi = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
			builder.setSmallIcon(R.drawable.logo);
			builder.setContentIntent(pi);
			builder.setDefaults(Notification.DEFAULT_SOUND);
			builder.setAutoCancel(true);
			NotificationManager notification = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

				if (hayChat){
					titulo = "Nuevo Chat";
					contenidoCorto = "Hay nuevo chat.";
				}
				if (inServiciosSinAceptar > 0){
					if (!"".equals(titulo)){
						titulo += " y ";
					}
					titulo +="Servicios";
					contenidoCorto = "Hay servicios sin aceptar por favor confirmelos";
					contenidoLargo = "Existen "+inServiciosSinAceptar+" que no ha aceptado, por favor confirme ahora mismo";
				}
				builder.setContentTitle(titulo);
				builder.setContentText(contenidoCorto);

				if (!"".equals(contenidoLargo)){
					NotificationCompat.BigTextStyle inboxStyle = new NotificationCompat.BigTextStyle();
					inboxStyle.setBigContentTitle(titulo);
					inboxStyle.bigText(contenidoLargo);
					builder.setStyle(inboxStyle);
				}

				Notification notify = builder.build();
				notification.notify(0,notify);
		 }
	}

    public class sincronizarTask extends AsyncTask<Void, String, String>{

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if ("ok".equals(result)){
				procesarAlertas();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}

		@Override
		protected String doInBackground(Void... params) {
			try{
				String stJSon = getJSon("rest/serviciosConductor?conductor="+Sesion.usuario.getIdConductor()+"&transportador="+Sesion.usuario.getIdTransportador());
				JSONArray respuesta = new JSONArray(stJSon);
				servicios.clear();
				for (int i = 0; i < respuesta.length(); i++) {
					JSONObject obj = respuesta.getJSONObject(i);

			    	Servicio servicio = new Servicio();
			    	servicio.setId(obj.getString("id"));
			    	servicio.setExpediente(obj.getString("expediente"));
			    	servicio.setFechaHora(obj.getString("fechaHora"));
			    	servicio.setDirInicial(obj.getString("dirInicial"));
			    	servicio.setAsegurado(obj.getString("asegurado"));
			    	servicio.setVehiculo(obj.getString("vehiculo"));
			    	servicio.setEstado(obj.getString("estado"));
			    	servicio.setNombreEstado(obj.getString("nombreEstado"));
			    	servicio.setIdConductor(obj.getString("id_conductor"));
			    	servicio.setNombreConductor(obj.getString("nombreConductor"));
			    	servicio.setIdTransportador(obj.getString("id_transportador"));
			    	servicio.setNombreTransportador(obj.getString("nombreTransportador"));
			    	servicio.setDirFinal(obj.getString("dirFinal"));
			    	servicio.setTrazabilidad(obj.getString("trazabilidad"));
			    	servicio.setChatSaliente("0");
			    	if (obj.has("chatSaliente"))
			    		servicio.setChatSaliente(obj.getString("chatSaliente"));
			    	servicios.add(servicio);
				}
				return "ok";
			}catch(Exception e){
			}

			return "";
		}
    }

	private String getJSon(String servicio) throws Exception {
    	String stUrl = getSharedPreferences("PREFERENCES", MODE_PRIVATE).getString("url", null);

    	HttpClient httpclient = new DefaultHttpClient();

    	HttpGet httpget = new HttpGet();

    	httpget.setURI(new URI(stUrl+servicio));

		HttpResponse response = httpclient.execute(httpget);

		StringBuilder sb = new StringBuilder();

		if (response != null) {
			InputStream is = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			} finally {
				if (is != null)
					is.close();
			}
		}
		return sb.toString();
	}
}
