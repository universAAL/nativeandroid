/*
	Copyright 2011-2012 TSB, http://www.tsbtecnologias.es
	TSB - Tecnologías para la Salud y el Bienestar
	
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
package org.universaal.nativeandroid.aalfficiencyclient.utils;

import org.universaal.nativeandroid.aalfficiencyclient.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public final class TipsListAdapter extends ArrayAdapter<Tip> {

	private final int newsItemLayoutResource;

	public TipsListAdapter(final Context context, final int newsItemLayoutResource) {
		super(context, 0);
		this.newsItemLayoutResource = newsItemLayoutResource;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		
		final View view = getWorkingView(convertView);
		final ViewHolder viewHolder = getViewHolder(view);
		final Tip tip = getItem(position);

		viewHolder.titleView.setText(tip.getType());

		viewHolder.subTitleView.setText(tip.getText());

		if (tip.getType().compareTo("Electricity")==0)
			viewHolder.imageView.setImageResource(R.drawable.electricity);
		else
			viewHolder.imageView.setImageResource(R.drawable.activity);

		return view;
	}

	private View getWorkingView(final View convertView) {
		
		View workingView = null;

		if(null == convertView) {
			final Context context = getContext();
			final LayoutInflater inflater = (LayoutInflater)context.getSystemService
		      (Context.LAYOUT_INFLATER_SERVICE);

			workingView = inflater.inflate(newsItemLayoutResource, null);
		} else {
			workingView = convertView;
		}

		return workingView;
	}

	private ViewHolder getViewHolder(final View workingView) {
		
		final Object tag = workingView.getTag();
		ViewHolder viewHolder = null;


		if(null == tag || !(tag instanceof ViewHolder)) {
			viewHolder = new ViewHolder();

			viewHolder.titleView = (TextView) workingView.findViewById(R.id.tip_type);
			viewHolder.subTitleView = (TextView) workingView.findViewById(R.id.tip_text);
			viewHolder.imageView = (ImageView) workingView.findViewById(R.id.tip_icon);

			workingView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) tag;
		}

		return viewHolder;
	}

	
	private static class ViewHolder {
		public TextView titleView;
		public TextView subTitleView;
		public ImageView imageView;
	}


}