package app.create.rpg;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import app.create.rpg.file.CachedFile;
import app.create.rpg.file.CachedFile.User;
import app.create.rpg.file.CachedRMData;

import com.eb.rpg.Actor;
import com.eb.rpg.Armor;
import com.eb.rpg.Named;

public class FragmentActors extends AbstractFragmentPage implements OnItemSelectedListener, User {

	protected CachedRMData mFileActors, mFileClasses, mFileArmors, mFileWeapons;
	protected static final int[] GRAPH_IDS = new int[]{R.id.viewParamMaxHP, R.id.viewParamMaxSP, R.id.viewParamSTR, R.id.viewParamDEX, R.id.viewParamAGI, R.id.viewParamINT};
	protected static final int[] GRAPH_COLORS = new int[]{0xffc83c78, 0xff3c78c8, 0xffc8783c, 0xff78c83c, 0xff3cc878, 0xff783cc8};
	protected Bitmap[] mGraph = new Bitmap[GRAPH_IDS.length];

	public static class MyCreator extends CustomPagerAdapter.FragmentCreator {
		public MyCreator() { super(); }
		public MyCreator(Parcel source) { super(source); }
		public Fragment newInstance(Context context) { return new FragmentActors(); }
		public String getTitle(Context context) { return context.getString(R.string.menu_actors); }
		public Class<?> getFragmentClass() { return FragmentActors.class; }
		public boolean equals(Object o) { return o instanceof MyCreator; }
		public static final MyCreator EMPTY = new MyCreator();
		public static final Creator<MyCreator> CREATOR = new Creator<MyCreator>() {
			public MyCreator[] newArray(int size) { return new MyCreator[size]; }
			public MyCreator createFromParcel(Parcel source) { return new MyCreator(source); }
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		setName(getString(R.string.menu_actors));
		View view = inflater.inflate(R.layout.page_actors, null);
		final Spinner spinner = (Spinner) view.findViewById(R.id.spinActors);
		spinner.setOnItemSelectedListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final ActivityProject act = (ActivityProject) getActivity();
		// Takes too long to read; need to use multithreading..
		act.require(this, "Classes");
		act.require(this, "Armors");
		act.require(this, "Weapons");
		act.require(this, "Actors");
	}

	@SuppressWarnings("unchecked")
	public void updateSpinner() {
		if (mFileActors == null || mFileActors.getData() == null)
			return;
		final Spinner spinner = (Spinner) getView().findViewById(R.id.spinActors);
		ArrayAdapter<Object> adapter = (ArrayAdapter<Object>) spinner.getAdapter();
		boolean unset;
		if (unset = (adapter == null)) {
			adapter = new ArrayAdapter<Object>(getActivity(), android.R.layout.simple_spinner_item);
		}
		adapter.setNotifyOnChange(false);
		adapter.clear();
		for (Object o : mFileActors.getData())
			adapter.add(o);
		adapter.notifyDataSetChanged();
		if (unset)
			spinner.setAdapter(adapter);
		onItemSelected(spinner, spinner.getSelectedView(), spinner.getSelectedItemPosition(), spinner.getSelectedItemId());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		for (int i = 0; i < mGraph.length; i++) {
			if (mGraph[i] != null)
				mGraph[i].recycle();
			mGraph[i] = null;
		}
		if (mFileClasses != null) 
			mFileClasses.terminate();
		mFileClasses = null;
		if (mFileArmors != null)
			mFileArmors.terminate();
		mFileArmors = null;
		if (mFileWeapons != null)
			mFileWeapons.terminate();
		mFileWeapons = null;
		if (mFileActors != null) 
			mFileActors.terminate();
		mFileActors = null;
	}

	protected void setSelectedById(AdapterView<?> adapterView, int id) {
		Adapter a = (Adapter) adapterView.getAdapter();
		int c = a.getCount(), i;
		if (id == 0) {
			adapterView.setSelection(0);
			return;
		}
		for (i = 1; i < c; i++) {
			Object o = a.getItem(i);
			if (o instanceof Named && ((Named) o).id == id) {
				adapterView.setSelection(i);
				break;
			}
		}
		adapterView.setSelection(0);
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View itemView, int position, long id) {
		final View rv = getView();
		switch (adapterView.getId()) {
			case R.id.spinActors: {
					Actor data = (Actor) ((Adapter) adapterView.getAdapter()).getItem(position);
					((EditText) rv.findViewById(R.id.editName)).setText(data.name);
					((Spinner) rv.findViewById(R.id.spinClass)).setSelection(data.class_id - 1);
					((EditText) rv.findViewById(R.id.editInitLevel)).setText(Integer.toString(data.initial_level));
					((EditText) rv.findViewById(R.id.editFinalLevel)).setText(Integer.toString(data.final_level));
					((Button) rv.findViewById(R.id.btnExpCurve)).setText("Basis : " + data.exp_basis + ", Inflation : " + data.exp_inflation);
					((ImageView) rv.findViewById(R.id.imgBtnCharacter)).setImageDrawable(mHelper.getGraphicsMaterial("Characters", data.character_name, data.character_hue));
					((ImageView) rv.findViewById(R.id.imgBtnBattler)).setImageDrawable(mHelper.getGraphicsMaterial("Battlers", data.battler_name, data.battler_hue));

					((Spinner) rv.findViewById(R.id.spinWeapon)).setSelection(data.weapon_id);
					((CheckBox) rv.findViewById(R.id.chkFixWeapon)).setChecked(data.weapon_fix);

					setSelectedById((Spinner) rv.findViewById(R.id.spinShield), data.armor1_id);
					((CheckBox) rv.findViewById(R.id.chkFixShield)).setChecked(data.armor1_fix);

					setSelectedById((Spinner) rv.findViewById(R.id.spinHelmet), data.armor2_id);
					((CheckBox) rv.findViewById(R.id.chkFixHelmet)).setChecked(data.armor2_fix);

					setSelectedById((Spinner) rv.findViewById(R.id.spinBodyArmor), data.armor3_id);
					((CheckBox) rv.findViewById(R.id.chkFixBodyArmor)).setChecked(data.armor3_fix);

					setSelectedById((Spinner) rv.findViewById(R.id.spinAccessory), data.armor4_id);
					((CheckBox) rv.findViewById(R.id.chkFixAccessory)).setChecked(data.armor4_fix);

					int j, k;
					int[] colors;
					short[][][] param = data.parameters.getInnerArray();
					for (int i = 0; i < GRAPH_IDS.length; i++) {
						View view = rv.findViewById(GRAPH_IDS[i]);
						Bitmap bmp = mGraph[i];
						if (bmp == null) {
							colors = new int[9900];
							int param_h = i > 1 ? 999 : 9999;
							for (j = 0; j < 99; j++) {
								for (k = 100 - ((param[0][j + 1][i] & 0xFFFF) * 100 / param_h); k < 100; k++)
									colors[(k * 99) + j] = GRAPH_COLORS[i];
							}
							mGraph[i] = bmp = Bitmap.createBitmap(colors, 99, 100, Config.ARGB_8888);
						}
						BitmapDrawable bd = new BitmapDrawable(getResources(), bmp);
						try {
							View.class.getMethod("setBackground", Drawable.class).invoke(view, bd);
						} catch (Exception e) {
							try {
								View.class.getMethod("setBackgroundDrawable", Drawable.class).invoke(view, bd);
							} catch (Exception e1) { }
						}
					}
				}
				break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) { }

	@Override
	public void onOpen(CachedFile file) {
		Log.d("CreateRPG", "Open " + file.getName());
		onUpdate(file);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onUpdate(CachedFile file) {
		Log.d("CreateRPG", "Update " + file.getName());
		String name = file.getName().toLowerCase(Locale.US);
		List<Object> list;
		View view = getView();
		// final Spinner spinActors = (Spinner) view.findViewById(R.id.spinActors);
		// Spinner spin1;
		if (name.equals("actors")) {
			for (int i = 0; i < mGraph.length; ++i) {
				if (mGraph[i] != null)
					mGraph[i].recycle();
				mGraph[i] = null;
			}
			list = (mFileActors = (CachedRMData) file).getData();
//			ArrayAdapter<Object> adapter = (ArrayAdapter<Object>) spinActors.getAdapter();
//			if (adapter == null)
//				spinActors.setAdapter(new ArrayAdapter<Object>(getActivity(), android.R.layout.simple_spinner_item, list));
//			else {
//				adapter.setNotifyOnChange(false);
//				adapter.clear();
//				adapter.addAll(list);
//			}
		} else if (name.equals("classes")) {
			list = (mFileClasses = (CachedRMData) file).getData();
			((Spinner) view.findViewById(R.id.spinClass))
				.setAdapter(new ArrayAdapter<Object>(getActivity(), android.R.layout.simple_spinner_item, list));
		} else if (name.equals("weapons")) {
			mFileWeapons = (CachedRMData) file;
			list = new ArrayList<Object>(1+mFileWeapons.getData().size());
			list.add(new Named());
			list.addAll(mFileWeapons.getData());
			((Spinner) view.findViewById(R.id.spinWeapon))
				.setAdapter(new ArrayAdapter<Object>(getActivity(), android.R.layout.simple_spinner_item, list));
		} else if (name.equals("armors")) {
			list = (mFileArmors = (CachedRMData) file).getData();
			BaseAdapter[] adapt = new BaseAdapter[4];
			int i;
			for (i = 0; i < 4; i++) {
				List<Object> ln = new ArrayList<Object>();
				ln.add(new Named());
				adapt[i] = new ArrayAdapter<Object>(getActivity(), android.R.layout.simple_spinner_item, ln);
			}
			i = 0;
			for (Object o : list) {
				((ArrayAdapter<Object>)adapt[((Armor)o).kind]).add(o);
			}
			for (i = 0; i < 4; i++) {
				((Spinner) view.findViewById(Armor.KIND_TO_ID[i]))
					.setAdapter(adapt[i]);
			}
		}
		updateSpinner();
	}

	@Override
	public void onClosed(CachedFile file) {

	}
}
