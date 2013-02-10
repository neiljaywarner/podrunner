package info.bottiger.podcast.adapters;

import info.bottiger.podcast.R;
import info.bottiger.podcast.provider.FeedItem;
import info.bottiger.podcast.provider.Subscription;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class SubscriptionListCursorAdapter extends AbstractPodcastAdapter {

	public SubscriptionListCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}
	
	public SubscriptionListCursorAdapter(
			Context context,
			int listItem,
			Cursor cursor,
			String[] strings,
			int[] is,
			SubscriptionListCursorAdapter.FieldHandler[] fields) {
		super(context, listItem, cursor, strings, is, fields);
		
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View listViewItem;
		Cursor subscriptionCursor = (Cursor) getItem(position);

		log.debug("inside getView()");
		
		if (!subscriptionCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position "
					+ position);
		}

		
		log.debug("inside getView() 2 => " + subscriptionCursor.toString());

		if (convertView == null) {
			listViewItem = newView(mContext, subscriptionCursor, parent);
		} else {
			listViewItem = convertView;
		}
		
		log.debug("inside getView() 3");

		bindView(listViewItem, mContext, subscriptionCursor);

		log.debug("inside getView() 4");
		
		return listViewItem;
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View view = mInflater.inflate(R.layout.subscriptions_list, null);

		view.setTag(R.id.list_image, view.findViewById(R.id.list_image));
		view.setTag(R.id.title, view.findViewById(R.id.title));
		view.setTag(R.id.podcast, view.findViewById(R.id.podcast));
		return view;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		Subscription sub = null;
		try {
			sub = Subscription.getByCursor(cursor);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			log.debug("inside illegal state exception subscription");
		}
		
		if (sub != null)
			log.debug("inside bindView() 1 => " + sub.toString());
		else
			log.debug("inside bindView() 1 => sub == null");

		/*
		 * http://drasticp.blogspot.dk/2012/04/viewholder-is-dead.html
		 */
		ImageView icon = (ImageView) view.getTag(R.id.list_image);
		TextView mainTitle = (TextView) view.getTag(R.id.title);
		TextView subTitle = (TextView) view.getTag(R.id.podcast);

		
		if (sub != null) {
			
			log.debug("inside bindView()");


			if (sub.title != null)
				mainTitle.setText(sub.title);


			if (sub.imageURL != null && !sub.imageURL.equals("")) {
				ImageLoader imageLoader = getImageLoader(context);
				imageLoader.displayImage(sub.imageURL, icon);
			}

		}
	}


}
