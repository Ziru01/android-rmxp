package app.create.rpg;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends ArrayAdapter<File> {

	public FileListAdapter(Context context, File[] objects) {
		super(context, R.layout.dialog_file_row, R.id.row_text, objects);
	}

	public FileListAdapter(Context context, List<File> objects) {
		super(context, R.layout.dialog_file_row, R.id.row_text, objects);
	}

	public FileListAdapter(Context context) {
		super(context, R.layout.dialog_file_row, R.id.row_text);
	}
	
	private View processView(int position, View view) {
		TextView tv = (TextView) view.findViewById(R.id.row_text);
		File item = getItem(position);
		if (tv == null) return view;
		tv.setText(item.getName());
		ImageView iv = (ImageView) view.findViewById(R.id.row_image);
		if (iv == null) return view;
		iv.setImageResource(item.isDirectory() ? R.drawable.ic_btn_folder : R.drawable.ic_btn_file);
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return processView(position, super.getDropDownView(position, convertView, parent));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return processView(position, super.getView(position, convertView, parent));
	}

}
