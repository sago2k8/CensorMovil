package co.campeoncloudcomputing.santomas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import co.campeoncloudcomputing.santomas.adapters.ServiciosAdapter;
import co.campeoncloudcomputing.santomas.broadcast.AlarmReceiver;
import co.campeoncloudcomputing.santomas.entidades.Servicio;
import co.campeoncloudcomputing.santomas.utils.Cons;
import co.campeoncloudcomputing.santomas.utils.Sesion;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockActivity implements WsInt{

	ListView lstServicios;
	ProgressBar progress;
	TextView txtNombreConductor;
	Activity act;
	List<Servicio> servicios;
	ServiciosAdapter adapter;
	boolean creando;
	private SharedPreferences settings ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
        Sesion.usuario.setNombres(settings.getString("nombres", ""));
		Sesion.usuario.setIdConductor(settings.getString("idconductor", ""));
		Sesion.usuario.setIdTransportador(settings.getString("idtransportador", ""));
		Sesion.usuario.setCedula(settings.getString("cedula", ""));
        creando = true; //para que la primera vez no refresque la lista dos veces
        act = this;
        String stUsuario = Sesion.usuario.getCedula();
        if (!"0".equals(stUsuario) && !"".equals(stUsuario)){
        	if (!Sesion.usuario.isCorriendoServicio()){
        		programarAlertas();
        	}
        }
        lstServicios = (ListView)findViewById(R.id.ser_lis_listaservicios);
        progress = (ProgressBar)findViewById(R.id.ser_pro_sincronizar);
        txtNombreConductor = (TextView)findViewById(R.id.ser_txt_nombre_conductor);
        txtNombreConductor.setText(Sesion.usuario.getNombres());
        servicios = new ArrayList<Servicio>();
        getListaServicios();
        adapter = new ServiciosAdapter(getApplicationContext(),R.layout.row_servicios,servicios);
        adapter.setInterface(this);
        lstServicios.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long resource) {
				Servicio servicio = servicios.get(pos);
				if (!servicio.getTrazabilidad().equals(Cons.ESTADO_INICIAL)){
					Intent zoom = new Intent(act,ZoomActivity.class);
					Sesion.tag = servicio;
					act.startActivity(zoom);
				}else{
					Toast.makeText(getApplicationContext(), "Antes de ver el servicio debe Aceptarlo", Toast.LENGTH_SHORT).show();
				}
			}
		});


        lstServicios.setAdapter(adapter);
    }


    private void programarAlertas() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 30 seconds
        long firstMillis = System.currentTimeMillis() + (60*1000); // first run of alarm is immediate
        int intervalMillis = 60*1000; // 30 seconds
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, pIntent);
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Sesion.usuario.setActivo(true);
		if (!creando){
			getListaServicios();
		}else{
			creando = false;
		}
	}

	@Override
	protected void onStop(){
		super.onStop();
		Sesion.usuario.setActivo(false);
	}

	private void getListaServicios() {

    	new sincronizarTask(Cons.WS_SERVICIOS,null).execute();
	}

    public class sincronizarTask extends AsyncTask<Void, String, String>{

    	String tipo;
    	Object obj;
    	public sincronizarTask(String tipo, Object obj){
    		this.tipo = tipo;
    		this.obj = obj;
    	}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progress.setVisibility(View.INVISIBLE);
			adapter.notifyDataSetChanged();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}

		@Override
		protected String doInBackground(Void... params) {
			try{
				if (Cons.WS_SERVICIOS.equals(tipo)){
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
				    	servicio.setChatSaliente("N");
				    	if (obj.has("chatSaliente"))
				    		servicio.setChatSaliente(obj.getString("chatSaliente"));
				    	servicios.add(servicio);
					}
				}else if (Cons.WS_ESTADO.equals(tipo)){
					Map tmpMap = (HashMap)obj;
					String stJSon = getJSon("rest/actualizarServicio?operacion="+tmpMap.get("operacion")+
							"&servicio="+tmpMap.get("id")+"&conductor="+tmpMap.get("id_conductor")+"&transportador="+tmpMap.get("id_transportador")+
							"&nombreConductor="+URLEncoder.encode(Sesion.usuario.getNombres(),"UTF-8"));
					JSONObject objson = new JSONObject(stJSon);
					if(objson.getString("estado").equals("ok")){
						getListaServicios();
					}
				}
			}catch(Exception e){
				Vibrator v = (Vibrator) act.getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(50);
				e.printStackTrace();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		MenuItem menuRefresh = menu.add("Sincronizar");
		menuRefresh.setIcon(R.drawable.refresh);
		menuRefresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuRefresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				getListaServicios();
				return true;
			}
		});
		MenuItem menuLogout = menu.add("Logout");
		menuLogout.setIcon(R.drawable.logout);
		menuLogout.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuLogout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				AlertDialog.Builder alert = new AlertDialog.Builder(act);
				alert.setTitle("Salir");
				alert.setMessage("Se desconectara y debera acceder de nuevo ¿Esta seguro?");
				alert.setPositiveButton("Desconectar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Sesion.usuario.setCedula(null);
						SharedPreferences settings = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
						settings.edit().putString("idconductor", null).commit();
						Intent miIntent = new Intent(MainActivity.this,LoginActivity.class);
						MainActivity.this.startActivity(miIntent);
						MainActivity.this.finish();
					}
				});
				alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
				return true;
			}
		});
		return true;
	}

	@Override
	public void putEstado(Map estadoMap) {
		new sincronizarTask(Cons.WS_ESTADO, estadoMap).execute();
	}

	@Override
	public void abrirChat(Map estadoMap) {
		int pos = (Integer)estadoMap.get("pos");
		Servicio servicio = servicios.get(pos);
		Sesion.tag = servicio;
		Intent chat = new Intent(act,ChatActivity.class);
		startActivity(chat);
	}
}
