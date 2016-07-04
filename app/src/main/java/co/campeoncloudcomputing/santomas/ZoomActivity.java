package co.campeoncloudcomputing.santomas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import co.campeoncloudcomputing.santomas.entidades.Servicio;
import co.campeoncloudcomputing.santomas.utils.Cons;
import co.campeoncloudcomputing.santomas.utils.Sesion;
import co.campeoncloudcomputing.santomas.utils.Utils;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ZoomActivity extends SherlockActivity {

	ProgressBar progress;
	TextView txtExpediente;
	TextView txtFechaHora;
	TextView txtDireccionInicial;
	TextView txtDireccionFinal;
	TextView txtAsegurado;
	TextView txtVehiculo;
	TextView txtEstado;
	TextView txtConductor;
	TextView txtAyudante;
	Button btnLlegada;
	Button btnFinServicio;
	Button btnContacto;
	Button btnPlacas;
	Button btnInicioServicio;
	TableLayout tblCampos;
	Activity act;
	Servicio servicio;
	String estadoActual;
	View alertDialogView;
	MenuItem menuChat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zoom);
        act = this;
        servicio = (Servicio) Sesion.tag;
        progress = (ProgressBar)findViewById(R.id.zoom_prg_sincro);
    	txtExpediente = (TextView)findViewById(R.id.zoom_txt_expediente);
    	txtFechaHora = (TextView)findViewById(R.id.zoom_txt_fechahora);
    	txtDireccionInicial = (TextView)findViewById(R.id.zoom_txt_dir_inicial);
    	txtDireccionFinal = (TextView)findViewById(R.id.zoom_txt_dir_final);
    	txtAsegurado = (TextView)findViewById(R.id.zoom_txt_asegurado);
    	txtVehiculo = (TextView)findViewById(R.id.zoom_txt_vehiculo);
    	txtEstado = (TextView)findViewById(R.id.zoom_txt_estado);
    	txtConductor = (TextView)findViewById(R.id.zoom_txt_conductor);
    	txtAyudante = (TextView)findViewById(R.id.zoom_txt_acompanante);
    	btnLlegada = (Button)findViewById(R.id.zoom_btn_llegada);
    	btnFinServicio = (Button)findViewById(R.id.zoom_btn_fin_servicio);
    	btnContacto = (Button)findViewById(R.id.zoom_btn_contacto);
    	btnPlacas = (Button)findViewById(R.id.zoom_btn_placas);
    	btnInicioServicio = (Button)findViewById(R.id.zoom_btn_inicio_servicio);
    	tblCampos = (TableLayout)findViewById(R.id.zoom_tbl_campos);

    	for (int i = 0; i < tblCampos.getChildCount(); i++){
			View v = tblCampos.getChildAt(i);
			for (int j = 0; j < ((TableRow)v).getChildCount(); j++){
				View v2 = ((LinearLayout)v).getChildAt(j);
				if (v2 instanceof TextView){
					Utils.setFont((TextView)v2);
				}
			}
		}

    	txtExpediente.setText(servicio.getExpediente());
    	txtFechaHora.setText(servicio.getFechaHora());
    	txtDireccionInicial.setText(servicio.getDirInicial());
    	txtDireccionFinal.setText(servicio.getDirFinal());
    	txtAsegurado.setText(servicio.getAsegurado());
    	txtVehiculo.setText(servicio.getVehiculo());
    	txtEstado.setText(servicio.getNombreEstado());
    	txtConductor.setText(servicio.getNombreConductor());
    	txtAyudante.setText(servicio.getNombreTransportador());
    	estadoActual = servicio.getTrazabilidad();
    	visualizarBotones();

    	btnLlegada.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Map estadoMap = new HashMap();
				estadoMap.put("id", servicio.getId());
				estadoMap.put("id_conductor", servicio.getIdConductor());
				estadoMap.put("id_transportador", servicio.getIdTransportador());
				estadoMap.put("operacion", Cons.ESTADO_EN_DIRECCION);
				estadoActual = Cons.ESTADO_EN_DIRECCION;
				putEstado(estadoMap);
			}
		});

    	btnPlacas.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
		    	builder.setTitle("Reportar Placas");
		    	LayoutInflater inflater = LayoutInflater.from(act);

		    	alertDialogView = inflater.inflate(R.layout.comentario, null);
		    	TextView txtContenido = (TextView)alertDialogView.findViewById(R.id.dlg_txt_contenido);
		    	txtContenido.setText("Ingrese las placas del vehiculo");
		        final TextView txtURL = (TextView)alertDialogView.findViewById(R.id.dlg_edt_campo);
		        txtURL.setText("");
		        txtURL.setTextSize(20);
		        builder.setView(alertDialogView);
		        builder.setPositiveButton("Reportar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		        final AlertDialog alert = builder.create();
		        alert.show();
		        Button btn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
		        btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						String stPlacas = (""+txtURL.getText()).trim().toUpperCase();
						stPlacas = stPlacas.replaceAll(" ",	"");
		        		if ("".equals(stPlacas)){
		        			Toast.makeText(act, "Debe ingresar una placa valida", Toast.LENGTH_SHORT).show();
		        		}else{
							Map estadoMap = new HashMap();
							estadoMap.put("id", servicio.getId());
							estadoMap.put("id_conductor", servicio.getIdConductor());
							estadoMap.put("id_transportador", servicio.getIdTransportador());
							estadoMap.put("operacion", Cons.ESTADO_PLACAS);
							estadoMap.put("mensaje", stPlacas);
							estadoActual = Cons.ESTADO_PLACAS;
							putEstado(estadoMap);
							alert.dismiss();
		        		}
					}
				});
			}
		});

    	btnContacto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Map estadoMap = new HashMap();
				estadoMap.put("id", servicio.getId());
				estadoMap.put("id_conductor", servicio.getIdConductor());
				estadoMap.put("id_transportador", servicio.getIdTransportador());
				estadoMap.put("operacion", Cons.ESTADO_CONTACTADO);
				estadoActual = Cons.ESTADO_CONTACTADO;
				putEstado(estadoMap);
			}
		});
    	btnInicioServicio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Map estadoMap = new HashMap();
				estadoMap.put("id", servicio.getId());
				estadoMap.put("id_conductor", servicio.getIdConductor());
				estadoMap.put("id_transportador", servicio.getIdTransportador());
				estadoMap.put("operacion", Cons.ESTADO_INICIO_SERVICIO);
				estadoActual = Cons.ESTADO_INICIO_SERVICIO;
				putEstado(estadoMap);
			}
		});
    	btnFinServicio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Map estadoMap = new HashMap();
				estadoMap.put("id", servicio.getId());
				estadoMap.put("id_conductor", servicio.getIdConductor());
				estadoMap.put("id_transportador", servicio.getIdTransportador());
				estadoMap.put("operacion", Cons.ESTADO_FIN_SERVICIO);
				estadoActual = Cons.ESTADO_FIN_SERVICIO;
				putEstado(estadoMap);
			}
		});
    }

    public void visualizarBotones(){
    	if(estadoActual.equals(Cons.ESTADO_ACEPTADO)){
    		btnLlegada.setEnabled(true);
    		btnContacto.setEnabled(false);
    		btnPlacas.setEnabled(false);
    		btnFinServicio.setEnabled(false);
    		btnInicioServicio.setEnabled(false);
    	}else if(estadoActual.equals(Cons.ESTADO_EN_DIRECCION)){
    		btnLlegada.setEnabled(false);
    		btnContacto.setEnabled(true);
    		btnPlacas.setEnabled(false);
    		btnFinServicio.setEnabled(false);
    		btnInicioServicio.setEnabled(false);
    	}else if(estadoActual.equals(Cons.ESTADO_CONTACTADO)){
    		btnLlegada.setEnabled(false);
    		btnContacto.setEnabled(false);
    		btnPlacas.setEnabled(true);
    		btnFinServicio.setEnabled(false);
    		btnInicioServicio.setEnabled(false);
    	}else if(estadoActual.equals(Cons.ESTADO_PLACAS)){
    		btnLlegada.setEnabled(false);
    		btnContacto.setEnabled(false);
    		btnPlacas.setEnabled(false);
    		btnInicioServicio.setEnabled(true);
    		btnFinServicio.setEnabled(false);
    	}else if(estadoActual.equals(Cons.ESTADO_INICIO_SERVICIO)){
    		btnLlegada.setEnabled(false);
    		btnContacto.setEnabled(false);
    		btnPlacas.setEnabled(false);
    		btnInicioServicio.setEnabled(false);
    		btnFinServicio.setEnabled(true);
    	}else if(estadoActual.equals(Cons.ESTADO_FIN_SERVICIO)){
    		btnLlegada.setEnabled(false);
    		btnContacto.setEnabled(false);
    		btnPlacas.setEnabled(false);
    		btnInicioServicio.setEnabled(false);
    		btnFinServicio.setEnabled(false);
    	}
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
			visualizarBotones();
			if ("1".equals(result)){
				menuChat.setIcon(R.drawable.chaton);
			}
			if (Cons.WS_ESTADO.equals(tipo)){
				Map tmpMap = (HashMap)obj;
				if (Cons.ESTADO_SOLICITAR_CONFERENCIA.equals(tmpMap.get("operacion"))){
					if ("".equals(result)){
						Toast.makeText(act, "No se pudo solicitar conferencia,intenta de nuevo", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(act, "Solicitud de conferencia exitosa, espere la llamada", Toast.LENGTH_SHORT).show();
					}
				}
			}
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
				if (Cons.WS_ESTADO.equals(tipo)){
					Map tmpMap = (HashMap)obj;
					String stJSon = getJSon("rest/actualizarServicio?operacion="+tmpMap.get("operacion")+
							"&servicio="+tmpMap.get("id")+"&conductor="+tmpMap.get("id_conductor")+"&transportador="+tmpMap.get("id_transportador")+
							"&nombreConductor="+URLEncoder.encode(Sesion.usuario.getNombres(),"UTF-8")+
							"&mensaje="+((tmpMap.get("mensaje") == null )?"":URLEncoder.encode(""+tmpMap.get("mensaje"),"UTF-8")));
					JSONObject objson = new JSONObject(stJSon);
					if(objson.getString("estado").equals("ok")){
						estadoActual = objson.getString("trazabilidad");
						String stHayChat = objson.getString("chatSaliente");
						if ("1".equals(stHayChat)){
							return "1";
						}
						return "ok";
					}
				}
			}catch(Exception e){
				Vibrator v = (Vibrator) act.getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(50);
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
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		super.onCreateOptionsMenu(menu);
		MenuItem menuRefresh = menu.add("Sincronizar");
		menuRefresh.setIcon(R.drawable.refresh);
		menuRefresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuRefresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Map estadoMap = new HashMap();
				estadoMap.put("id", servicio.getId());
				estadoMap.put("id_conductor", servicio.getIdConductor());
				estadoMap.put("id_transportador", servicio.getIdTransportador());
				estadoMap.put("operacion", Cons.ESTADO_REFRESCAR);
				putEstado(estadoMap);
				return true;
			}
		});
		MenuItem menuConferencia = menu.add("Conferencia");
		menuConferencia.setIcon(R.drawable.phone);
		menuConferencia.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuConferencia.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Map estadoMap = new HashMap();
				estadoMap.put("id", servicio.getId());
				estadoMap.put("id_conductor", servicio.getIdConductor());
				estadoMap.put("id_transportador", servicio.getIdTransportador());
				estadoMap.put("operacion", Cons.ESTADO_SOLICITAR_CONFERENCIA);
				putEstado(estadoMap);
				return true;
			}
		});
		menuChat = menu.add("Chat");
		menuChat.setIcon(R.drawable.chatoff);
		menuChat.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuChat.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent chat = new Intent(act,ChatActivity.class);
				startActivity(chat);
				return true;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void putEstado(Map estadoMap) {
		new sincronizarTask(Cons.WS_ESTADO, estadoMap).execute();
	}

	@Override
	protected void onResume(){
		super.onResume();
		Map estadoMap = new HashMap();
		estadoMap.put("id", servicio.getId());
		estadoMap.put("id_conductor", servicio.getIdConductor());
		estadoMap.put("id_transportador", servicio.getIdTransportador());
		estadoMap.put("operacion", Cons.ESTADO_REFRESCAR);
		putEstado(estadoMap);
	}
}
