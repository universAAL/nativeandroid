package org.universAAL.android.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import org.universAAL.middleware.container.utils.StringUtils;

import android.os.Parcel;
import android.os.Parcelable;

public class GroundingParcel implements Parcelable{
	private String action;
	private String category;
	private String grounding;
	private String replyAction;
	private String replyCategory;
	private String remote;
	private int lengthIN;
	private String[] keysIN;
	private String[] valuesIN;
	private int lengthOUT;
	private String[] keysOUT;
	private String[] valuesOUT;
	
	public static final Parcelable.Creator<GroundingParcel> CREATOR = new Parcelable.Creator<GroundingParcel>() {
		public GroundingParcel createFromParcel(Parcel in) {
			return new GroundingParcel(in);
		}

		public GroundingParcel[] newArray(int size) {
			return new GroundingParcel[size];
		}
	};

	//TODO Deprecate this method
	public GroundingParcel(InputStream is) throws InvalidPropertiesFormatException, IOException {
		Properties props=new Properties();
		props.loadFromXML(is);
		// TODO use another format/method? (Especially because characters < and
		// > of the turtle grounding are escaped in this format. Can be annoying)
		is.close();
		action = props.getProperty("action");
		category = props.getProperty("category");
		grounding = props.getProperty("grounding");
		replyAction = props.getProperty("replyAction");
		replyCategory = props.getProperty("replyCategory");
		remote = props.getProperty("remote");
		// variables
		List<String> listKeysVAR=new ArrayList<String>();
		List<String> listValuesVAR=new ArrayList<String>();
		List<String> listKeysURI=new ArrayList<String>();
		List<String> listValuesURI=new ArrayList<String>();
		for(Object key:props.keySet()){
			if(key instanceof String){
				String keystr=(String)key;
				if(!(keystr.equals("action")||keystr.equals("action")||keystr.equals("action")||keystr.equals("action")||keystr.equals("action"))){
					//Its a custom property (a variable assignation)
					if(StringUtils.isQualifiedName(keystr)){
						//Its a URI
						listKeysURI.add(keystr);
						listValuesURI.add(props.getProperty(keystr));
					}else{
						//Its a variable
						listKeysVAR.add(keystr);
						listValuesVAR.add(props.getProperty(keystr));
					}
				}
			}else{
				//TODO ???
			}
		}
		lengthIN=listKeysVAR.size();
		if(lengthIN>0){
			keysIN=listKeysVAR.toArray(new String[lengthIN]);
			valuesIN=listValuesVAR.toArray(new String[lengthIN]);
		}else{
			keysIN=new String[0];
			valuesIN=new String[0];
		}
		lengthOUT=listKeysURI.size();
		if(lengthOUT>0){
			keysOUT=listKeysURI.toArray(new String[lengthOUT]);
			valuesOUT=listValuesURI.toArray(new String[lengthOUT]);
		}else{
			keysOUT=new String[0];
			valuesOUT=new String[0];
		}
	}

	public GroundingParcel(String act, String cat, String ground,
			String repAct, String repCat, String rem, List<String> kIN,
			List<String> vIN, List<String> kOUT, List<String> vOUT) {
		action = act;
		category = cat;
		grounding = ground;
		replyAction = repAct;
		replyCategory = repCat;
		remote=rem;
		lengthIN = kIN.size();
		if (lengthIN > 0) {
			keysIN = kIN.toArray(new String[lengthIN]);
			valuesIN = vIN.toArray(new String[lengthIN]);
		} else {
			keysIN = new String[0];
			valuesIN = new String[0];
		}
		lengthOUT = kOUT.size();
		if (lengthOUT > 0) {
			keysOUT = kOUT.toArray(new String[lengthOUT]);
			valuesOUT = vOUT.toArray(new String[lengthOUT]);
		} else {
			keysOUT = new String[0];
			valuesOUT = new String[0];
		}
	}

	public GroundingParcel(Parcel in) {
		action=in.readString();
		category=in.readString();
		grounding=in.readString();
		replyAction=in.readString();
		replyCategory=in.readString();
		remote=in.readString();
		lengthIN=in.readInt();
		keysIN=new String[lengthIN];
		in.readStringArray(keysIN);
		valuesIN=new String[lengthIN];
		in.readStringArray(valuesIN);
		lengthOUT=in.readInt();
		keysOUT=new String[lengthOUT];
		in.readStringArray(keysOUT);
		valuesOUT=new String[lengthOUT];
		in.readStringArray(valuesOUT);
	}
	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(action);
		dest.writeString(category);
		dest.writeString(grounding);
		dest.writeString(replyAction);
		dest.writeString(replyCategory);
		dest.writeString(remote);
		dest.writeInt(lengthIN);
		dest.writeStringArray(keysIN);
		dest.writeStringArray(valuesIN);
		dest.writeInt(lengthOUT);
		dest.writeStringArray(keysOUT);
		dest.writeStringArray(valuesOUT);
	}
	
	public int describeContents() {
		return 0;
	}

	public String getGrounding() {
		return grounding;
	}

	public String getReplyAction() {
		return replyAction;
	}

	public String getReplyCategory() {
		return replyCategory;
	}

	public String getAction() {
		return action;
	}

	public String getCategory() {
		return category;
	}
	
	public String[] getKeysIN() {
		return keysIN;
	}

	public String[] getValuesIN() {
		return valuesIN;
	}

	public String[] getKeysOUT() {
		return keysOUT;
	}

	public String[] getValuesOUT() {
		return valuesOUT;
	}
	
	public int getLengthIN() {
		return lengthIN;
	}

	public int getLengthOUT() {
		return lengthOUT;
	}

	public String getRemote() {
		return remote;
	}

}
