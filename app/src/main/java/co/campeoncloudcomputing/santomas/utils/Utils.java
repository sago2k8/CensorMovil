package co.campeoncloudcomputing.santomas.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Typeface;
import android.widget.TextView;



public class Utils{

	static Typeface bold;
	public static Typeface normal;
	private static SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	public static String encode(String st){
		st = st.replaceAll("á", "a");
		st = st.replaceAll("é", "e");
		st = st.replaceAll("í", "i");
		st = st.replaceAll("ó", "o");
		st = st.replaceAll("ú", "u");
		st = st.replaceAll("Á", "A");
		st = st.replaceAll("É", "E");
		st = st.replaceAll("Í", "I");
		st = st.replaceAll("Ó", "O");
		st = st.replaceAll("Ú", "U");
		st = st.replaceAll("ñ", "n");
		st = st.replaceAll("Ñ", "N");
		return st;
	}
	
	public static void setFont(TextView t){
		t.setTypeface(normal);
	}
	
	public static String format(Date d){
		return sd.format(d);
	}
	
	public static Date parse(String s){
		try {
			return sd.parse(s);
		} catch (ParseException e) {
			return null;
		}
	}
}