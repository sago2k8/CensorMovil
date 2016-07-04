package co.campeoncloudcomputing.santomas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import co.campeoncloudcomputing.santomas.entidades.Servicio;
import co.campeoncloudcomputing.santomas.utils.Cons;
import co.campeoncloudcomputing.santomas.utils.Sesion;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ChatActivity extends SherlockActivity {

	TextView txtHistorial;
	TextView txtEnviar;
	ImageButton btnEnviar;
	ImageButton btnVoice;
	Activity act;
	Servicio servicio;
	String estadoActual;
	View alertDialogView;
	MenuItem menuChat;
	String stJSon;
	private static final int REQUEST_CODE = 1234;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		act = this;
		servicio = (Servicio) Sesion.tag;
		txtHistorial = (TextView) findViewById(R.id.chat_txt_historial);
		txtEnviar = (TextView) findViewById(R.id.chat_txt_enviar);
		btnEnviar = (ImageButton) findViewById(R.id.chat_btn_enviar);
		btnVoice = (ImageButton) findViewById(R.id.chat_btn_voice);

		txtHistorial.setText("");
		txtEnviar.setText("");
		btnEnviar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ("".equals((""+txtEnviar.getText()).trim()))
					return;
				putChat();
				txtEnviar.requestFocus();
			}
		});

		btnVoice.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try{
					Intent intent = new Intent(
							RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					startActivityForResult(intent, REQUEST_CODE);
				}catch(Exception e){
					Toast.makeText(act, "No tiene instalado el reconocimiento de voz", Toast.LENGTH_LONG).show();
				}

			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getChat();
	}

	public class sincronizarTask extends AsyncTask<Void, String, String> {

		String tipo;

		public sincronizarTask(String tipo) {
			this.tipo = tipo;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (Cons.WS_GET_CHAT.equals(tipo)) {
				if (!"".equals(result)) {
					result = result.replaceAll("\\[", "\n\\[");
					txtHistorial.setText(result);
				}
			}
			if (Cons.WS_PUT_CHAT.equals(tipo)) {
				if (!"".equals(result)) {
					txtHistorial.setText(result);
					txtEnviar.setText("");
				}
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
			try {
				if (Cons.WS_GET_CHAT.equals(tipo)) {
					String stJSon = getJSon("rest/getMensajes?servicio="
							+ servicio.getId() + "&conductor="
							+ servicio.getIdConductor());
					JSONObject objson = new JSONObject(stJSon);
					if (objson.has("mensajes")) {
						return (objson.getString("mensajes"));
					}
				} else if (Cons.WS_PUT_CHAT.equals(tipo)) {
					stJSon = getJSonPost("setMensajes");
					JSONObject objson = new JSONObject(stJSon);
					if (objson.has("estado")) {
						if (objson.getString("estado").equals("ok")) {
							return (objson.getString("mensajes"));
						} else {
							return "Error: Estado NO";
						}
					} else {
						return "Error: Mal jSON";
					}
				}
			} catch (Exception e) {
				Vibrator v = (Vibrator) act
						.getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(50);
				System.out.println(stJSon);
				e.printStackTrace();
			}

			return "";
		}
	}

	private String getJSon(String stServicio) throws Exception {
		String stUrl = getSharedPreferences("PREFERENCES", MODE_PRIVATE)
				.getString("url", null);

		HttpClient httpclient = new DefaultHttpClient();

		HttpGet httpget = new HttpGet();

		httpget.setURI(new URI(stUrl + stServicio));

		HttpResponse response = httpclient.execute(httpget);

		StringBuilder sb = new StringBuilder();

		if (response != null) {
			InputStream is = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

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

	private String getJSonPost(String stServicio) throws Exception {
		String stUrl = getSharedPreferences("PREFERENCES", MODE_PRIVATE)
				.getString("url", null);

		JSONObject post = new JSONObject();
		post.put("idServicio", servicio.getId());
		post.put("idConductor", servicio.getIdConductor());
		post.put("mensaje", txtEnviar.getText());
		/*
		 * HttpParams params = new BasicHttpParams();
		 * HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		 * HttpProtocolParams.setContentCharset(params, "utf-8");
		 * params.setBooleanParameter("http.protocol.expect-continue", false);
		 */

		HttpClient httpclient = new DefaultHttpClient();

		// List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		// nameValuePairs.add(new BasicNameValuePair("json", post.toString()));
		StringEntity url = new StringEntity(post.toString());
		HttpPost httppost = new HttpPost(stUrl + "rest/" + stServicio);

		httppost.setEntity(url);

		HttpResponse response = httpclient.execute(httppost);

		StringBuilder sb = new StringBuilder();

		if (response != null) {
			InputStream is = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

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
		menuRefresh
				.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						getChat();
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

	public void putChat() {
		new sincronizarTask(Cons.WS_PUT_CHAT).execute();
	}

	public void getChat() {
		new sincronizarTask(Cons.WS_GET_CHAT).execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> textMatchList = data
					    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			if (!textMatchList.isEmpty()){
				txtEnviar.setText(textMatchList.get(0));
			}
		}else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
		    showToastMessage("Audio Error");
		 }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
		    showToastMessage("Cliente Error");
		 }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
		    showToastMessage("Error de Internet");
		 }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
		    showToastMessage("No se entiende");
		 }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
		    showToastMessage("Error de servidor, trate de nuevo");
	   }

		super.onActivityResult(requestCode, resultCode, data);
	}

	 void showToastMessage(String message){
	   Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	 }

}
