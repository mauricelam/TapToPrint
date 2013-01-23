package com.mauricelam.taptoprint;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.mauricelam.taptoprint.CloudPrint.Job;

/**
 * A ListView adapter for the Print Queue. Uses the listitem_print_queue.xml as layout.
 * 
 * @author Maurice Lam
 * 
 */
public class JobsAdapter extends ArrayAdapter<Job> {
	private Context context;

	public JobsAdapter(Context context, Job[] objects) {
		super(context, R.layout.listitem_print_queue, R.id.text, objects);
		this.context = context;
	}

	public JobsAdapter(Context context, List<Job> objects) {
		super(context, R.layout.listitem_print_queue, R.id.text, objects);
		this.context = context;
	}

	public JobsAdapter(Context context) {
		super(context, R.layout.listitem_print_queue, R.id.text);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		Job job = this.getItem(position);
		ImageView statusLight = (ImageView) view.findViewById(R.id.status);
		Drawable statusDrawable = getStatusDrawable(job.status);
		statusLight.setImageDrawable(statusDrawable);

		Drawable drawable = statusLight.getDrawable();
		if (drawable instanceof AnimationDrawable) {
			((AnimationDrawable) drawable).start();
		}

		return view;
	}

	/**
	 * Returns the color corresponding to the status
	 * 
	 * @param status
	 * @return
	 */
	private Drawable getStatusDrawable(Job.Status status) {
		switch (status) {
		case DONE:
			return createBubble(Color.GREEN);
		case QUEUED:
			return createBubble(Color.GRAY);
		case ERROR:
			return createBubble(Color.RED);
		case IN_PROGRESS:
			AnimationDrawable progress = (AnimationDrawable) context.getResources().getDrawable(
					R.drawable.progress);
			return progress;
		}
		// The switch statement should be exhaustive
		return null;
	}

	/**
	 * Create a bubble of the specified color.
	 * 
	 * @param color
	 *            An integer representing the color in ARGB format. Note that this is the same
	 *            format as used in {@link android.graphics.Color}
	 * @return
	 */
	private Drawable createBubble(int color) {
		Drawable bubble = context.getResources().getDrawable(R.drawable.print_status);
		bubble.setColorFilter(color, PorterDuff.Mode.SRC_IN);
		return bubble;
	}

}
