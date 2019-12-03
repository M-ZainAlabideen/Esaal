package com.riontech.staggeredtextgridview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


public class StaggeredTextGridView extends ScrollView {

	private static final String TAG = StaggeredTextGridView.class.getSimpleName();
	// Custom adapter
	private BaseAdapter mAdapter;
	// Row full width
	private int mMaxRowWidth;
	// Width of added child in single row.
	private int mRowWidth;
	// HORIZONTAL LinearLayout row
	private LinearLayout mRow;
	// VERTICAL LinearLayout Parent
	private LinearLayout mParent;
	// Activity context
	private Context mContext;
	// Item horizontal space
	private int mHorizontalSpace = 7;
	// Item vertical space
	private int mVerticalSpace = 7;
	// Allow last row width fit to screen or wrap content
	private boolean mIsFitToScreen = false;

	public StaggeredTextGridView(Context context) {
		super(context);
		this.mContext = context;
		init();
	}


	@SuppressLint("NewApi")
	public StaggeredTextGridView(Context context, AttributeSet attrs,
								 int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.mContext = context;
		init();
//		setAttributes(attrs);
	}


	@SuppressLint("NewApi")
	public StaggeredTextGridView(Context context, AttributeSet attrs,
								 int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.mContext = context;
		init();
//		setAttributes(attrs);
	}


	public StaggeredTextGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
//		setAttributes(attrs);
	}

	/**
	 * Custom attributes
	 * @param attrs
	 */



	/**
	 * calculate maximum row width
	 * @param attrs
	 */
	private void calculateMaxRowWidth(AttributeSet attrs) {
		// padding attributes array
		int[] attributes = new int[]{android.R.attr.paddingLeft,
				android.R.attr.paddingRight,
				android.R.attr.paddingStart,
				android.R.attr.paddingEnd};

		int[] padding = new int[]{android.R.attr.padding};
		TypedArray ta = mContext.obtainStyledAttributes(attrs, padding);

		if (ta.hasValue(0)) {
			int pad = ta.getDimensionPixelSize(0, -1);
			// reduce maximum row width by padding
			mMaxRowWidth = mMaxRowWidth - (pad * 2);
		} else {
			//then obtain typed array
			TypedArray arr = mContext.obtainStyledAttributes(attrs, attributes);
			//and get values you need by indexes from your array attributes defined above
			int leftPadding = arr.getDimensionPixelSize(0, -1);
			int rightPadding = arr.getDimensionPixelSize(1, 0);

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
				leftPadding = arr.getDimensionPixelSize(2, -1);
				rightPadding = arr.getDimensionPixelSize(3, -1);
			}

			// reduce maximum row width by padding
			mMaxRowWidth = mMaxRowWidth - (leftPadding + rightPadding);
		}
	}

	/**
	 * set default maximum row width,
	 * Equal to device width
	 */
	private void calculateDeviceWidth(){
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);

		mMaxRowWidth = displaymetrics.widthPixels;
	}

	private void init() {
		calculateDeviceWidth();
		// ScrollView params
		//LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		setPadding(5,5,5,5);
//		setLayoutParams(params);

		// LinearLayout params
		mParent = new LinearLayout(mContext);
		mParent.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mParent.setLayoutParams(params);
		addView(mParent);
	}

	public void setAdapter(BaseAdapter adapter) {
		this.mAdapter = adapter;
		generateSpannableTextGridView();
	}

	private void generateSpannableTextGridView() {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			// get textview from adapter
			TextView textView = (TextView) mAdapter.getView(i, null, this);
			//int padding = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
			// padding calculation
			int padding;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				padding = textView.getPaddingEnd() + textView.getPaddingStart();
			} else {
				padding = textView.getPaddingLeft() + textView.getPaddingRight();
			}

			// get string object
			String item = (String) mAdapter.getItem(i);
			// init item width base on its text width and padding
			int itemWidth = (int) (textView.getPaint().measureText(item) + padding);
			// add spaces of left and right
			itemWidth = itemWidth + (mVerticalSpace * 2);

			// init first row
			if (i == 0) {
				mRow = getRow();
				addChildView(textView, itemWidth);
			} else {
				// add TextView into row as columns
				if (mRowWidth + itemWidth <= mMaxRowWidth) {
					addChildView(textView, itemWidth);
				} else {
					setFullWidthRow();
					mParent.addView(mRow);
					mRow = getRow();
					addChildView(textView, itemWidth);
				}
				// add last row into parent view
			}
			if (i == (mAdapter.getCount() - 1)) {
				mParent.addView(mRow);
					setWrapWidthRow();
			}
		}
	}

	/**
	 * wrap content row width base on it's child
	 */
	private void setWrapWidthRow() {
		// reset width of all child
		for (int i = 0; i < mRow.getChildCount(); i++) {
			View view = mRow.getChildAt(i);
			resetChildWidth(view, 0);
		}
	}

	/**
	 * Distribute and append equally remaining
	 * free space width to all row's child and fill row
	 * base on device width
	 */
	private void setFullWidthRow() {
		// Difference between row with child and device width
		int remainWidth = mMaxRowWidth - mRowWidth;
		// Distributes equally remaining space between child
		int childSpace = remainWidth / mRow.getChildCount();
		int spaceReminder = remainWidth % mRow.getChildCount();

		// reset width of all child
		for (int i = 0; i < mRow.getChildCount(); i++) {

			// Add space reminder into last child
			if (spaceReminder > 0 && i == (mRow.getChildCount() - 1)) {
				childSpace = childSpace + spaceReminder;
			}

			View view = mRow.getChildAt(i);
			resetChildWidth(view, childSpace);
		}

		mRowWidth = 0;
	}

	/**
	 * Append child space with child width
	 *
	 * @param view       child TextView
	 * @param childSpace remaining space
	 */
	private void resetChildWidth(final View view, final int childSpace) {

		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				int childWidth = view.getWidth();
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				params.width = childWidth + childSpace;
				params.setMargins(mHorizontalSpace, 0, mHorizontalSpace, 0);
				view.setLayoutParams(params);

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
					view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				else
					view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});

	}

	/**
	 * Add child into row
	 *
	 * @param view     child TextView
	 * @param newWidth width of child TextView
	 */
	private void addChildView(View view, int newWidth) {
		mRow.addView(view);
		resizeRow(newWidth, view);
	}

	/**
	 * Resize row width base on child
	 *
	 * @param width incremental width row
	 * @param view  child view will add into row
	 */
	private void resizeRow(int width, View view) {
		// LinearLayout row params
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRow
				.getLayoutParams();
		mRowWidth = mRowWidth + width;
		params.weight = mRow.getChildCount();
		mRow.setLayoutParams(params);
	}

	/**
	 * Generate new row
	 *
	 * @return row LinearLayout
	 */
	@SuppressLint("InflateParams")
	private LinearLayout getRow() {
		final LinearLayout lRow = (LinearLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.row_item_spanneble, null);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, mVerticalSpace, 0, mVerticalSpace);
		lRow.setLayoutParams(params);
		lRow.setOrientation(LinearLayout.HORIZONTAL);
		return lRow;
	}

	public boolean isFitToScreen() {
		return mIsFitToScreen;
	}

	public void setFitToScreen(boolean isFitToScreen) {
		this.mIsFitToScreen = isFitToScreen;
	}

	public int getVerticalSpace() {
		return mVerticalSpace;
	}

	public void setVerticalSpace(int verticalSpace) {
		this.mVerticalSpace = verticalSpace;
	}

	public int getHorizontalSpace() {
		return mHorizontalSpace;
	}

	public void setHorizontalSpace(int horizontalSpace) {
		this.mHorizontalSpace = horizontalSpace;
	}
}
