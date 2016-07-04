package co.campeoncloudcomputing.santomas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.campeoncloudcomputing.santomas.utils.Sesion;
import co.campeoncloudcomputing.santomas.utils.Utils;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class LoginActivity extends SherlockActivity{

	EditText edUsuario,edPassword;
	Button btnIngresar;
	Activity act;
	SharedPreferences settings;
	View alertDialogView;
	String stUsuario;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.login);
		edUsuario = (EditText)findViewById(R.id.log_txt_usuario);
		edPassword = (EditText)findViewById(R.id.log_txt_password);
		btnIngresar = (Button)findViewById(R.id.log_but_ingresar);
		act = this;
		settings = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
		LinearLayout lay = (LinearLayout)findViewById(R.id.log_lay_login);
		for (int i = 0; i < lay.getChildCount(); i++){
			View v = lay.getChildAt(i);
			if (v instanceof TextView){
				Utils.setFont((TextView)v);
			}
		}

		btnIngresar.setOnClickListener(

				new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Llamar Servicio web
				stUsuario = ""+edUsuario.getText();
				String stClave = ""+edPassword.getText();

				if (stUsuario.trim().equals("")){
					Toast.makeText(getApplicationContext(), "Ingrese un usuario valido", Toast.LENGTH_SHORT).show();
					edUsuario.setText("");
					return;
				}

				if (stClave.trim().equals("")){
					Toast.makeText(getApplicationContext(), "Ingrese una clave valida", Toast.LENGTH_SHORT).show();
					edPassword.setText("");
					return;
				}

				new sincronizarTask().execute();
			}
		}

				);
	}


    public class sincronizarTask extends AsyncTask<Void, String, String>{

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Toast.makeText(act, result, Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			String stUrl = settings.getString("url", null);
			if (stUrl == null){
				Toast.makeText(act, "Falta configurar el servidor", Toast.LENGTH_SHORT).show();
				this.cancel(true);
			}else{
				Vibrator v = (Vibrator) act.getSystemService(Context.VIBRATOR_SERVICE);
				 v.vibrate(100);
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}

		@Override
		protected String doInBackground(Void... params) {
			stUsuario = (""+edUsuario.getText()).trim();
			String stClave = (""+edPassword.getText()).trim();
			String stMensaje = "";
			try{
				String stJSon = getJSon("rest/loginConductor?cedula="+stUsuario+"&clave="+stClave);
				JSONArray respuestaArr = new JSONArray(stJSon);
				JSONObject respuesta = respuestaArr.getJSONObject(0);
				if ("ok".equals(respuesta.getString("estado"))){
					Intent intent = new Intent(act, MainActivity.class);
					stMensaje = "Bienvenido "+respuesta.getString("nombres");
					Sesion.usuario.setNombres(respuesta.getString("nombres"));
					if ("".equals(respuesta.getString("id_conductor")))
						Sesion.usuario.setIdConductor("0");
					else
						Sesion.usuario.setIdConductor(respuesta.getString("id_conductor"));
					if ("".equals(respuesta.getString("id_transportador")))
						Sesion.usuario.setIdTransportador("0");
					else
						Sesion.usuario.setIdTransportador(respuesta.getString("id_transportador"));
					settings.edit().putString("nombres", Sesion.usuario.getNombres()).commit();
					settings.edit().putString("idconductor", Sesion.usuario.getIdConductor()).commit();
					settings.edit().putString("idtransportador", Sesion.usuario.getIdTransportador()).commit();
					settings.edit().putString("cedula", stUsuario).commit();
					settings.edit().putString("fecha", Utils.format(new Date())).commit();

					startActivity(intent);
					act.finish();
				}else{
					stMensaje = "El usuario "+stUsuario+ " no es valido";
					edUsuario.setText("");
					edPassword.setText("");
					edUsuario.requestFocus();
				}
			}catch(Exception e){
				e.printStackTrace();
				stMensaje = "Error de comunicacion";
			}

			return stMensaje;
		}
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		MenuItem menuRefresh = menu.add("Configurar");
		menuRefresh.setIcon(android.R.drawable.ic_menu_preferences);
		menuRefresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuRefresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				String stUrl = settings.getString("url", "");

				AlertDialog.Builder builder = new AlertDialog.Builder(act);
		    	builder.setTitle("Cambiar Servidor");
		    	LayoutInflater inflater = LayoutInflater.from(act);

		    	alertDialogView = inflater.inflate(R.layout.comentario, null);
		    	TextView txtContenido = (TextView)alertDialogView.findViewById(R.id.dlg_txt_contenido);
		    	txtContenido.setText("Ingrese la direccion del servidor");
		        TextView txtURL = (TextView)alertDialogView.findViewById(R.id.dlg_edt_campo);
		        txtURL.setText(stUrl);
		        builder.setView(alertDialogView);
		        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						TextView txtURL = (TextView)alertDialogView.findViewById(R.id.dlg_edt_campo);
						String stUrl = (""+txtURL.getText()).trim();
						SharedPreferences.Editor editor = settings.edit();
					      editor.putString("url", stUrl);
					      editor.commit();
					}
				});
		        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
				return true;
			}
		});
		return true;
	}

	private String getJSon(String servicio) throws Exception {
    	String stUrl = settings.getString("url", null);

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
