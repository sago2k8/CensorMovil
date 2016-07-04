package co.campeoncloudcomputing.santomas.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import co.campeoncloudcomputing.santomas.R;
import co.campeoncloudcomputing.santomas.WsInt;
import co.campeoncloudcomputing.santomas.entidades.Servicio;
import co.campeoncloudcomputing.santomas.utils.Cons;
import co.campeoncloudcomputing.santomas.utils.Utils;

public class ServiciosAdapter extends ArrayAdapter {

	private Context mContext = null;
	private ArrayList<Servicio> mLista = null;
	private int mResource;
    private WsInt listener;

    public void setInterface(WsInt listener){
        this.listener = listener;
    }

	public ServiciosAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		// TODO Auto-generated constructor stub
	}

	public ServiciosAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
		// TODO Auto-generated constructor stub
	}

	public ServiciosAdapter(Context context, int textViewResourceId, Object[] objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	public ServiciosAdapter(Context context, int resourceId, List objects) {
		super(context, resourceId, objects);
		this.mContext = context;
		this.mLista = (ArrayList<Servicio>)objects;
		this.mResource = resourceId;
	}

	public ServiciosAdapter(Context context, int resource, int textViewResourceId, Object[] objects) {
		super(context, resource, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	public ServiciosAdapter(Context context, int resource, int textViewResourceId, List objects) {
		super(context, resource, textViewResourceId, objects);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder hold;
		final int pos = position;
		Servicio servicio = mLista.get(position);
		final Map estadoMap = new HashMap();
		estadoMap.put("id", servicio.getId());
		estadoMap.put("id_conductor", servicio.getIdConductor());
		estadoMap.put("id_transportador", servicio.getIdTransportador());
		if (convertView == null){
			hold = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(this.mResource, parent, false);
			for (int i = 0; i < ((LinearLayout)convertView).getChildCount(); i++){
				View v = ((LinearLayout)convertView).getChildAt(i);
				if (v instanceof LinearLayout && ((LinearLayout)v).getChildCount() > 0){
					for (int j = 0; j < ((LinearLayout)v).getChildCount(); j++){
						View v2 = ((LinearLayout)v).getChildAt(j);
						if (v2 instanceof TextView){
							Utils.setFont((TextView)v2);
						}
					}
				}else if (v instanceof TextView){
					Utils.setFont((TextView)v);
				}
			}
			hold.txtExpediente = (TextView) convertView.findViewById(R.id.row_ser_txt_expediente);
			hold.txtFecha = (TextView) convertView.findViewById(R.id.row_ser_txt_fechahora);
			hold.txtDireccion = (TextView) convertView.findViewById(R.id.row_ser_txt_direccion);
			hold.btnAceptar = (ImageButton)convertView.findViewById(R.id.row_ser_btn_aceptar);
			hold.btnChat = (ImageButton)convertView.findViewById(R.id.row_ser_btn_chat);

			convertView.setTag(hold);
		}else{
			hold = (ViewHolder)convertView.getTag();
		}
		hold.btnAceptar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				estadoMap.put("operacion", Cons.ESTADO_ACEPTADO);
				listener.putEstado(estadoMap);
			}
		});
		hold.btnChat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				estadoMap.put("pos", pos);
				listener.abrirChat(estadoMap);
			}
		});
		if (!servicio.getTrazabilidad().equals(Cons.ESTADO_INICIAL)){
			hold.btnAceptar.setVisibility(View.INVISIBLE);
			if ("1".equals(servicio.getChatSaliente())){
				hold.btnChat.setVisibility(View.VISIBLE);
			}else{
				hold.btnChat.setVisibility(View.INVISIBLE);
			}
		}else{
			hold.btnAceptar.setVisibility(View.VISIBLE);
			hold.btnChat.setVisibility(View.INVISIBLE);
		}

		hold.txtExpediente.setText(servicio.getExpediente());
		hold.txtFecha.setText(servicio.getFechaHora());
		hold.txtDireccion.setText(servicio.getDirInicial());
		return convertView;
	}

	static class ViewHolder{
		TextView txtExpediente;
		TextView txtFecha;
		TextView txtDireccion;
		ImageButton btnAceptar;
		ImageButton btnChat;
	}
}
