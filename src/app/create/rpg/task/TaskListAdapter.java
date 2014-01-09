package app.create.rpg.task;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import app.create.rpg.R;
import app.create.rpg.ServiceFileTask.MyBinder;
import app.create.rpg.ServiceFileTask.MyQueue;

public class TaskListAdapter extends ArrayAdapter<MyQueue> {
	
	public TaskListAdapter (Context context) {
		super(context, R.layout.list_task_item, android.R.id.text1);
	}
	
	public void update (MyBinder binder) {
		SparseArray<MyQueue> queues = binder.getTasks();
		synchronized (queues) {
			setNotifyOnChange(false);
			clear();
			for (int i = 0; i < queues.size(); ++i)
				add(queues.valueAt(i));
			notifyDataSetChanged();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		ProgressBar bar = (ProgressBar) view.findViewById(R.id.progressBar);
		float prog = getItem(position).getProgress();
		if (prog < 0f || prog > 1f)
			bar.setVisibility(View.GONE);
		else {
			bar.setVisibility(View.VISIBLE);
			bar.setProgress((int) (prog * 10000));
		}
		return view;
	}
	
}
