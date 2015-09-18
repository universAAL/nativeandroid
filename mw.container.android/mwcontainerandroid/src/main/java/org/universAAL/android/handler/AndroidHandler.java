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
package org.universAAL.android.handler;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.android.R;
import org.universAAL.android.activities.HandlerActivity;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.AppConstants;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.owl.Modality;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.rdf.FormControl;
import org.universAAL.middleware.ui.rdf.Group;
import org.universAAL.middleware.ui.rdf.Input;
import org.universAAL.middleware.ui.rdf.InputField;
import org.universAAL.middleware.ui.rdf.Label;
import org.universAAL.middleware.ui.rdf.MediaObject;
import org.universAAL.middleware.ui.rdf.Range;
import org.universAAL.middleware.ui.rdf.Repeat;
import org.universAAL.middleware.ui.rdf.Select;
import org.universAAL.middleware.ui.rdf.Select1;
import org.universAAL.middleware.ui.rdf.SimpleOutput;
import org.universAAL.middleware.ui.rdf.Submit;
import org.universAAL.middleware.ui.rdf.TextArea;
import org.universAAL.ontology.profile.AssistedPerson;
import org.universAAL.ontology.profile.Caregiver;
import org.universAAL.ontology.profile.User;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Android UI Handler, connects to HandlerActivity to show things
 * @author alfiva
 *
 */
public class AndroidHandler extends UIHandler {
	private static final String TAG = "AndroidHandler";
//	private static final String IMAGE_FOLDER = "/data/felix/configurations/etc/images/";// this is just the default
	//"Constants." are not constant! Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + "saied" does not have value until MW inits
	private String mUserURI=null;
    //TODO Change to WeakRefs?
	private static HandlerActivity mActivity;
	private UIRequest mCurrentOutput = null;
	private static boolean mNoLabels = false;
	private static int mMainColor=Color.parseColor("#388f90"); //uAAL color. If changed in resources, change here too
	
	/**
	 * Default constructor
	 * 
	 * @param context
	 *            uAAL Module Context
	 * @param initialSubscription
	 *            Initial handler profile for subscription
	 */
	protected AndroidHandler(ModuleContext context,
			UIHandlerProfile initialSubscription, String user) {
		super(context, initialSubscription);
		mUserURI=user;
	}

	public AndroidHandler(AndroidContext context, String string) {
		this(context, getSubscriptions(), string);
	}

	private static UIHandlerProfile getSubscriptions() {
		UIHandlerProfile oep = new UIHandlerProfile();
		//TODO Real profile
    	oep.setSupportedInputModalities(new Modality[]{Modality.gui});
    	return oep;
	}

	/**
	 * Links the handler to the android activity and view
	 * 
	 * @param activity
	 *            Android activity
	 */
	public static void setActivity(HandlerActivity activity) {
		mActivity = activity;
	}
	
	public void render(){
		if (mCurrentOutput != null) {
			handleUICall(mCurrentOutput);
		}else{
			this.userLoggedIn(makeUser(mUserURI), null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.universAAL.middleware.ui.UIHandler#adaptationParametersChanged(java
	 * .lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void adaptationParametersChanged(String arg0, String arg1,
			Object arg2) {
		// TODO For now do nothing, but refresh output
		Log.d(TAG, "adaptation params changed" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.universAAL.middleware.ui.UIHandler#communicationChannelBroken()
	 */
	@Override
	public void communicationChannelBroken() {
		// TODO For now do nothing, but warn and reset?
		Log.d(TAG, "comm channel broken" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.universAAL.middleware.ui.UIHandler#cutDialog(java.lang.String)
	 */
	@Override
	public Resource cutDialog(String dialogID) {
		// TODO For now do nothing, but return current inputs and close
		Log.d(TAG, "cut dialog" );
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.universAAL.middleware.ui.UIHandler#handleUICall(org.universAAL.middleware
	 * .ui.UIRequest)
	 */
	@Override
	public void handleUICall(final UIRequest output) {
		Log.d(TAG,  "handle ui request" );
		mCurrentOutput = output;
		if (mActivity != null) {
			// TODO Handle user? not for now
			// This is where operations mentioned in the comment below should be
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					mActivity.setContentView(R.layout.handler);//Just in case. This does nothing if already set.
					/*
					 * These next operations should be outside the
					 * runOnUiThread, to avoid performing lengthy operations in
					 * UI thread, but dont know why they wouldnt work here
					 * for the Galaxy Tab. They do work there in "official"
					 * Android (and on my HTC).
					 */
					Form f = mCurrentOutput.getDialogForm();
					View controls;
					View submits;
					if (f.isSystemMenu()) {
						controls = renderGroupControl(mCurrentOutput.getDialogForm().getIOControls(), true);
						submits = renderGroupControl(mCurrentOutput.getDialogForm().getStandardButtons(), mActivity.getResources().getBoolean(R.bool.landscape));
					} else if (f.isMessage()) {
						controls = renderGroupControl(mCurrentOutput.getDialogForm().getIOControls(), true);
						submits = renderGroupControl(mCurrentOutput.getDialogForm().getSubmits(), mActivity.getResources().getBoolean(R.bool.landscape));
					} else {
						controls = renderGroupControl(mCurrentOutput.getDialogForm().getIOControls(), true);
						submits = renderGroupControl(mCurrentOutput.getDialogForm().getSubmits(), mActivity.getResources().getBoolean(R.bool.landscape));
					}
					// ...until here

					// Find the controls and submits areas in the screen layout
					LinearLayout mainView = (LinearLayout) mActivity.findViewById(R.id.mainView);
					FrameLayout mainControlsView = (FrameLayout) mActivity.findViewById(R.id.mainScrollView);
					FrameLayout mainSubmitsView = (FrameLayout) mActivity.findViewById(R.id.mainHorizontalScrollView);
					// Remove all their previous contents
					mainControlsView.removeAllViews();
					// By default mainSubmits is GONE
					mainSubmitsView.setVisibility(View.VISIBLE);
					mainSubmitsView.removeAllViews();
					// Add the new contents
					if (controls != null){
						mainControlsView.addView(controls);
					}
					if (submits != null){
						mainSubmitsView.addView(submits);
					}
					// Refresh the screen, showing the new contents
					mainView.setVisibility(View.VISIBLE);
					mainView.postInvalidate();
				}
			});
			Log.d(TAG,   "invalidated" );
		}
	}

	/**
	 * When pressed some button in the screen, send the response
	 * 
	 * @param submit
	 *            The submit that was pressed, along with all it s info
	 */
	public void performSubmit(Submit submit) {
		Log.d(TAG,   "pressed submit" );
		this.dialogFinished(new UIResponse(makeUser(mUserURI), null, submit));
		Log.d(TAG,  "submit processed" );
	}

	// ============================END HANDLER=============================

	// ============================RENDERERS===============================
	// Every renderer that introduces a button (submit) will have in its
	// "onclick" method the activation of the process for "inputpublish". In
	// this process all "store inputs" will be made

	private View renderGroupControl(Group ctrl, boolean vertical,
			LinearLayout current) {
		Log.d(TAG,   "rendering group" );
		// Default error view for not adding/returning null
		TextView errorOut = new TextView(mActivity);
		LinearLayout currentView;
		if (current == null) {
			currentView = new LinearLayout(mActivity);
			currentView.setOrientation(vertical ? LinearLayout.VERTICAL
					: LinearLayout.HORIZONTAL);
		} else {
			currentView = current;
		}
		try {
			FormControl[] children = ctrl.getChildren();
			if (children == null || children.length == 0) {
				errorOut.setText("Empty View!!!");
				errorOut.setTextColor(Color.GRAY);
				return errorOut;
			}

			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof InputField) {
					renderInputControl(currentView, (InputField) children[i]);
				} else if (children[i] instanceof SimpleOutput) {
					renderOutputControl(currentView, (SimpleOutput) children[i]);
				} else if (children[i] instanceof Select1) {
					renderSelect1Control(currentView, (Select1) children[i]);
				} else if (children[i] instanceof Select) {
					renderSelectControl(currentView, (Select) children[i]);
				} else if (children[i] instanceof Repeat) {
					renderRepeat(currentView, (Repeat) children[i]);
				} else if (children[i] instanceof Group) {
					if (children[i].getLabel() != null)
						if (children[i].getLabel().getText() != null) {
							TextView label = new TextView(mActivity, null,
									android.R.attr.listSeparatorTextViewStyle);
							label.setText(children[i].getLabel().getText());
							currentView.addView(label);
						}
					View group = renderGroupControl((Group) children[i],
							vertical);
					if (group != null)
						currentView.addView(group);
				} else if (children[i] instanceof Submit) {
					// also instances of SubdialogTrigger can be treated the same
					renderSubmitControl(currentView, (Submit) children[i],
							vertical);
				} else if (children[i] instanceof MediaObject) {
					renderMediaObject(currentView, (MediaObject) children[i]);
				} else if (children[i] instanceof TextArea) {
					renderTextArea(currentView, (TextArea) children[i]);
				} else if (children[i] instanceof Range) {
					renderSpinnerControl(currentView, (Range) children[i]);
				} else {
					currentView.addView(errorOut); // TODO return error view
				}
				// separator - kept for legacy in case we want it back
//				if (vertical && (i + 1 < children.length)) {
//					ImageView separator = new ImageView(mActivity);
//					Drawable sepDraw = Drawable.createFromPath(confHome
//							+ "/separatorlist.png");
//					if (sepDraw != null)
//						separator.setBackgroundDrawable(sepDraw);
//					currentView.addView(separator, new LayoutParams(
//							LayoutParams.FILL_PARENT,
//							LayoutParams.WRAP_CONTENT, 0));
//				}
			}
		} catch (Exception e) {
			Log.e(TAG,   "problems rendering group", e);
			errorOut.setText("Null View!!!");
			errorOut.setTextColor(Color.RED);
			return errorOut;
		}
		// group separator - kept for legacy in case we want it back
//		if (vertical) {
//			ImageView separator = new ImageView(mActivity);
//			Drawable sepDraw = Drawable.createFromPath(confHome
//					+ "/separator.png");
//			if (sepDraw != null)
//				separator.setBackgroundDrawable(sepDraw);
//			currentView.addView(separator, new LayoutParams(
//					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 0));
//		}
		return currentView;
	}

	private View renderGroupControl(Group ctrl, boolean vertical) {
		return renderGroupControl(ctrl, vertical, null);
	}

	private void renderSpinnerControl(LinearLayout currentView, Range range) {
		renderLabel(currentView, range);
		Spinner spin = new Spinner(mActivity);
		ArrayAdapter adapter = new ArrayAdapter(mActivity,
				android.R.layout.simple_spinner_item);
		int max = (Integer) range.getMaxValue();
		int min = (Integer) range.getMinValue();
		int delta = Integer.parseInt(range.getStep().toString());
		Integer initVal = (Integer) range.getValue();
		int val = (initVal != null) ? initVal : min;
		int initial = val;
		for (int i = min; i < max; i += delta) {
			adapter.add(new Integer(i));
			if (val == i){
				initial = i - min;
			}
		}
		spin.setAdapter(adapter);
		spin.setOnItemSelectedListener(new SpinListener(range));
		spin.setSelection(initial);
		currentView.addView(spin);
		renderHintAndHelp(currentView, range);
	}

	private void renderTextArea(LinearLayout currentView, TextArea textArea) {
		renderLabel(currentView, textArea);
		EditText text = new EditText(mActivity);
		text.setLines(3);
		if (textArea.getValue() != null) {
			text.setText(textArea.getValue().toString());
		}
		text.addTextChangedListener(new InputListener(textArea));
		currentView.addView(text);
		renderHintAndHelp(currentView, textArea);
	}

	private void renderMediaObject(LinearLayout currentView,
			MediaObject mediaObject) {
		renderLabel(currentView, mediaObject);
		ImageView img = new ImageView(mActivity);
		String confHome = PreferenceManager.getDefaultSharedPreferences(
				mActivity).getString(AppConstants.Keys.IFOLDER, AppConstants.Defaults.IFOLDER);
		Drawable draw = Drawable.createFromPath(confHome
				+ mediaObject.getContentURL());
		if (draw != null){
			img.setImageDrawable(draw);
		}else{
			img.setImageResource(R.drawable.img_notfound);
		}
		img.setAdjustViewBounds(true);
		img.setMaxHeight(70);
		currentView.addView(img);
		renderHintAndHelp(currentView, mediaObject);
	}

	private void renderSubmitControl(LinearLayout currentView, Submit submit,
			boolean vertical) {
		Button button = new Button(mActivity);
		button.setText(submit.getLabel().getText());
		button.setOnClickListener(new SubmitListener(submit));
		if (!vertical){
			button.setMaxWidth(155);
		}
		if (button.getHint() != null){
			button.setHint(button.getHint());
		}
		currentView.addView(button);
	}

	private void renderRepeat(LinearLayout currentView, Repeat repeat) {
		// Title style label
		if (repeat.getLabel() != null)
			if (repeat.getLabel().getText() != null) {
				TextView label = new TextView(mActivity, null,
						android.R.attr.listSeparatorTextViewStyle);
				label.setText(repeat.getLabel().getText());
				currentView.addView(label);
			}

		FormControl[] elems = repeat.getChildren();
		boolean groupflag = false;
		TableLayout table = new TableLayout(mActivity);
		if (elems == null || elems.length != 1){
			throw new IllegalArgumentException("Malformed argument!");
		}
		if (elems[0] instanceof Group) {
			groupflag = true;
			FormControl[] elems2 = ((Group) elems[0]).getChildren();
			if (elems2 == null || elems2.length == 0)
				throw new IllegalArgumentException("Malformed argument!");
			TableRow row = new TableRow(mActivity);
			for (int i = 0; i < elems2.length; i++) {
				if (elems2[i].getLabel() != null) {
					renderLabel(row, elems2[i]);
				} else {
					TextView label = new TextView(mActivity);
					label.setText(elems2[i].getType());
					row.addView(label);
				}
			}
			table.addView(row);
		} else if (elems[0] == null){
			throw new IllegalArgumentException("Malformed argument!");
		}
		for (int i = 0; i < repeat.getNumberOfValues(); i++) {
			TableRow row = new TableRow(mActivity);
			repeat.setSelection(i);
			mNoLabels = true;
			renderGroupControl((groupflag ? (Group) elems[0] : repeat), false,
					row);
			mNoLabels = false;
			table.addView(row);
		}
		table.setShrinkAllColumns(true);
		table.setStretchAllColumns(false);
		currentView.addView(table);
		renderHintAndHelp(currentView, repeat);
	}

	private void renderSelectControl(LinearLayout currentView, Select select) {
		renderLabel(currentView, select);
		Label[] labels = select.getChoices();
		if (labels != null) {
			// TODO: Initial value?????????????
			LinearLayout checkgroup = new LinearLayout(mActivity);
			checkgroup.setOrientation(LinearLayout.VERTICAL);
			CheckBox[] opts = new CheckBox[labels.length];
			ArrayList list = new ArrayList(labels.length);
			for (int i = 0; i < labels.length; i++) {
				opts[i] = new CheckBox(mActivity);
				opts[i].setText(labels[i].getText());
				opts[i].setOnCheckedChangeListener(new CheckListener(select,
						list, i));
				checkgroup.addView(opts[i]);
			}
			currentView.addView(checkgroup);
		}
		renderHintAndHelp(currentView, select);
	}

	private void renderSelect1Control(LinearLayout currentView, Select1 select1) {
		renderLabel(currentView, select1);
		Label[] labels = select1.getChoices();
		if (labels != null) {
			// TODO: Initial value?????????????????????????
			RadioGroup radiogroup = new RadioGroup(mActivity);
			RadioButton[] opts = new RadioButton[labels.length];
			// Label initVal=(Label)select1.getValue();
			for (int i = 0; i < labels.length; i++) {
				opts[i] = new RadioButton(mActivity);
				opts[i].setText(labels[i].getText());
				radiogroup.addView(opts[i]);
				// if(initVal!=null&&initVal.getText().equals(labels[i].getText()))
				// radiogroup.check(opts[i].getId());
			}
			currentView.addView(radiogroup);
			radiogroup.setOnCheckedChangeListener(new RadioListener(select1));
		}
		renderHintAndHelp(currentView, select1);
	}

	private void renderOutputControl(LinearLayout currentView,
			SimpleOutput simpleOutput) {
		renderLabel(currentView, simpleOutput);
		if (simpleOutput.getValue() != null) {
			TextView text = new TextView(mActivity);
			text.setText(simpleOutput.getValue().toString());
			currentView.addView(text);
		}
		renderHintAndHelp(currentView, simpleOutput);
	}

	private void renderInputControl(LinearLayout currentView,
			InputField inputField) {
		if (inputField.isOfBooleanType()) {
			CheckBox mark = new CheckBox(mActivity);
			if (inputField.getLabel() != null)
				if (inputField.getLabel().getText() != null) {
					mark.setText(inputField.getLabel().getText());
				}
			if ((Boolean) inputField.getValue() != null) {
				mark.setChecked((Boolean) inputField.getValue());
			}
			mark.setOnCheckedChangeListener(new InputListener(inputField));
			currentView.addView(mark);
		} else {
			renderLabel(currentView, inputField);
			EditText text = new EditText(mActivity);
			text.setSingleLine();
			if (inputField.getValue() != null) {
				text.setText(inputField.getValue().toString());
			}
			text.addTextChangedListener(new InputListener(inputField));
			currentView.addView(text);
		}
		renderHintAndHelp(currentView, inputField);
	}

	private void renderHintAndHelp(LinearLayout currentView, FormControl ctrl) {
		if (ctrl.getHelpString() != null) {
			TextView help = new TextView(mActivity);
			help.setText(ctrl.getHelpString());
			currentView.addView(help);
			currentView.setContentDescription(ctrl.getHelpString());
		}
		if (ctrl.getHintString() != null) {
			TextView hint = new TextView(mActivity);
			hint.setText(ctrl.getHintString());
			currentView.addView(hint);
			currentView.setContentDescription(ctrl.getHintString());
		}
	}

	private void renderLabel(LinearLayout currentView, FormControl ctrl) {
		if (!mNoLabels) {
			if (ctrl.getLabel() != null){
				if (ctrl.getLabel().getText() != null) {
					TextView label = new TextView(mActivity);
					label.setText(ctrl.getLabel().getText()+" ");
					label.setTextColor(mMainColor);
					label.setTypeface(Typeface.DEFAULT_BOLD);
					currentView.addView(label);
				}
			}
		}
	}

	// ==============================END RENDERERS===============================

	// ==============================LISTENERS===================================

	public class SubmitListener implements OnClickListener {
		private Submit submit;

		SubmitListener(Submit sub) {
			submit = sub;
		}

		public void onClick(View arg0) {
			performSubmit(submit);
		}
	}

	public class SpinListener implements OnItemSelectedListener {
		private Range range;

		SpinListener(Range rng) {
			range = rng;
		}

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			range.storeUserInput((Integer) range.getMinValue() + arg2);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			range.storeUserInput(null);
		}

	}

	public class RadioListener implements
			android.widget.RadioGroup.OnCheckedChangeListener {
		private Select1 select1;

		RadioListener(Select1 sel) {
			select1 = sel;
		}

		public void onCheckedChanged(RadioGroup arg0, int arg1) {
			RadioButton button = (RadioButton) mActivity.findViewById(arg1);
			String txt = button.getText().toString();
			select1.storeUserInputByLabelString(txt);
		}

	}

	public class CheckListener implements OnCheckedChangeListener {
		private Select select;
		private List list;
		private int index;

		CheckListener(Select sel, List lst, int ind) {
			select = sel;
			list = lst;
			index = ind;
		}

		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if (arg1) {
				list.add(select.getChoices()[index]);
			} else {
				list.remove(select.getChoices()[index]);
			}
			select.storeUserInput(list);
		}

	}

	public class InputListener implements TextWatcher, OnCheckedChangeListener {
		private Input input;

		InputListener(Input inp) {
			input = inp;
		}

		public void afterTextChanged(Editable text) {
			if (input.isOfBooleanType()) {
				input.storeUserInput(text.toString());
			} else {
				input.storeUserInput(text.toString());
			}
		}

		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// Nothing
		}

		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// Nothing
		}

		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			input.storeUserInput((Boolean.valueOf(arg1)));
		}
	}

	// ==============================END LISTENERS===============================

	private User makeUser(String uri){
		switch (MiddlewareService.mUserType) {
		case AppConstants.USER_TYPE_AP:
			return new AssistedPerson(uri);
		case AppConstants.USER_TYPE_CARE:
			return new Caregiver(uri);
		default:
			return new User(uri);
		}
	}
}
