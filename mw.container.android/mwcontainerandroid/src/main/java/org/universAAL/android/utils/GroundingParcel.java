/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.android.utils;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Helper class that bundles the information of a metadata grounding into a
 * Parcelable object that can be sent across Android components embedded in an
 * intent.
 * 
 * @author alfiva
 * 
 */
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
	
	/**
	 * Required to build a Parcelable
	 */
	public static final Parcelable.Creator<GroundingParcel> CREATOR = new Parcelable.Creator<GroundingParcel>() {
		public GroundingParcel createFromParcel(Parcel in) {
			return new GroundingParcel(in);
		}

		public GroundingParcel[] newArray(int size) {
			return new GroundingParcel[size];
		}
	};

	/**
	 * Constructor to build a Parcelable grounding.
	 * 
	 * @param act
	 *            Main intent action.
	 * @param cat
	 *            Main intent category.
	 * @param ground
	 *            Serialization of the grounding.
	 * @param repAct
	 *            Reply intent action.
	 * @param repCat
	 *            Reply intent category.
	 * @param rem
	 *            Code for the remote import through Gateway .
	 * @param kIN
	 *            Input mappings keys.
	 * @param vIN
	 *            Input mappings values.
	 * @param kOUT
	 *            Output mappings keys.
	 * @param vOUT
	 *            Output mappings values.
	 */
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

	/** Constructor to build a Parcelable grounding.
	 * @param in Parcel representation of the grounding.
	 */
	public GroundingParcel(Parcel in) {
		// There used to be another constructor using InputStream. Check SVN history
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

	/**
	 * Get the serialized grounding.
	 * 
	 * @return the serialized grounding.
	 */
	public String getGrounding() {
		return grounding;
	}

	/**
	 * Get the reply intent action.
	 * 
	 * @return the reply intent action.
	 */
	public String getReplyAction() {
		return replyAction;
	}

	/**
	 * Get the reply category.
	 * 
	 * @return the reply category.
	 */
	public String getReplyCategory() {
		return replyCategory;
	}

	/**
	 * Get the main intent action.
	 * 
	 * @return the main intent action.
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Get the main intent category.
	 * 
	 * @return the main intent category.
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Get the input keys.
	 * 
	 * @return the input keys.
	 */
	public String[] getKeysIN() {
		return keysIN;
	}

	/**
	 * Get the input values.
	 * 
	 * @return the input values.
	 */
	public String[] getValuesIN() {
		return valuesIN;
	}

	/**
	 * Get the output keys.
	 * 
	 * @return the output keys.
	 */
	public String[] getKeysOUT() {
		return keysOUT;
	}

	/**
	 * Get the output values.
	 * 
	 * @return the output values.
	 */
	public String[] getValuesOUT() {
		return valuesOUT;
	}

	/**
	 * Get the amount of inputs.
	 * 
	 * @return the amount of inputs.
	 */
	public int getLengthIN() {
		return lengthIN;
	}

	/**
	 * Get the amount of outputs.
	 * 
	 * @return the amount of outputs.
	 */
	public int getLengthOUT() {
		return lengthOUT;
	}

	/**
	 * Get the code for remote import through GW.
	 * 
	 * @return the code for remote import through GW.
	 */
	public String getRemote() {
		return remote;
	}

}
