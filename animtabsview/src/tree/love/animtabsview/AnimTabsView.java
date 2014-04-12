package tree.love.animtabsview;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

public class AnimTabsView extends RelativeLayout {
	
	private static final int ANIM_DURATION = 600;
	
	private Context mContext;
	private int mTotalItemsCount = 0;
	private int mCurrentItemPosition = 0;
	
	private LinearLayout mItemsLayout;
	private Scroller mScroller;
	private Bitmap mSlideIcon;
	private Bitmap mShadow;
	private Rect mLeftDrawRect;
	private Rect mRightDrawRect;
	
	private int mCurrentSlideX;
	private int mCurrentSlideY;
	
	
	private IAnimTabsItemViewChangeListener mItemViewChangeListener;
	private View.OnClickListener mItemClickListener = new TabsItemViewClickListener(this);

	public AnimTabsView(Context context) {
		this(context, null);
	}

	public AnimTabsView(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		
		setWillNotDraw(false);
		
		this.mContext = context;
		this.mLeftDrawRect = new Rect();
		this.mRightDrawRect = new Rect();
		this.mSlideIcon = BitmapFactory.decodeResource(getResources(), R.drawable.blk_menubtn_arr);
		this.mShadow = BitmapFactory.decodeResource(getResources(), R.drawable.blk_menubtn_shadow);
		LinearLayout slideLayout = new LinearLayout(this.mContext);
		slideLayout.setOrientation(LinearLayout.VERTICAL);
		View localView = new View(this.mContext);
		localView.setBackgroundResource(R.drawable.blk_menubtn_bg);
		slideLayout.addView(localView, new LinearLayout.LayoutParams(-1, 0, 1.0F));
		slideLayout.addView(new View(this.mContext), new LinearLayout.LayoutParams(-1, this.mSlideIcon.getHeight()));
		this.mItemsLayout = new LinearLayout(this.mContext);
		this.mItemsLayout.setBackgroundColor(0);
		RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams( -1, -1);
		float f1 = context.getResources().getDisplayMetrics().density;
//		this.mItemsLayout.setPadding((int) (f1 * 20), 0, (int) (f1 * 20), 0);
		localLayoutParams.setMargins(0, 0, 0, (int) (f1 * 4.0F));
		addView(slideLayout);
		addView(this.mItemsLayout, localLayoutParams);
	}

	public int getCount() {
		return this.mTotalItemsCount;
	}

	public void selecteItem(int selectePosition) {
		selecteItem(selectePosition, true);
	}

	public void selecteItem(int selectePosition, boolean isAnimate) {
		if (this.mCurrentItemPosition == selectePosition)
			return;
		if (this.mScroller == null) {
			this.mScroller = new Scroller(this.mContext, new DecelerateInterpolator());
		}
		((RelativeLayout) getItemView(this.mCurrentItemPosition)).setSelected(false);
		this.mCurrentItemPosition = selectePosition;
		getItemView(this.mCurrentItemPosition).setSelected(true);
		int oldX = this.mCurrentSlideX;
		int oldY = this.mCurrentSlideY;
		if ((this.mTotalItemsCount > 0) && (getItemView(this.mCurrentItemPosition) != null)) {
			View currentSeletedItemView = getItemView(this.mCurrentItemPosition);
			this.mCurrentSlideX = (currentSeletedItemView.getLeft() + currentSeletedItemView.getWidth() / 2 - this.mSlideIcon .getWidth() / 2);
			if (isAnimate) {				
				this.mScroller.startScroll(oldX, oldY, this.mCurrentSlideX - oldX, this.mCurrentSlideY - oldY, ANIM_DURATION);
			}
		}
		invalidate();
	}

	public void setOnAnimTabsItemViewChangeListener(IAnimTabsItemViewChangeListener listener) {
		this.mItemViewChangeListener = listener;
	}

	public void addItem(String itemText) {
		RelativeLayout itemLayout = (RelativeLayout) View.inflate(this.mContext, R.layout.anim_tab_item, null);
		((TextView) itemLayout.getChildAt(0)).setText(itemText);
		itemLayout.setTag(Integer.valueOf(this.mTotalItemsCount));
		if (this.mTotalItemsCount == 0) {			
			itemLayout.setSelected(true);
		}
		itemLayout.setOnClickListener(this.mItemClickListener);
		this.mTotalItemsCount = (1 + this.mTotalItemsCount);
		this.mItemsLayout.addView(itemLayout, new LinearLayout.LayoutParams(0, -1, 1.0F));
	}

	public int getCurrentItemPosition() {
		return this.mCurrentItemPosition;
	}

	public View getItemView(int itemPosition) {
		if ((itemPosition >= 0) && (itemPosition < this.mTotalItemsCount)) {
			return this.mItemsLayout.getChildAt(itemPosition);
		}
		return null;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if ((this.mScroller == null) || (!this.mScroller.computeScrollOffset())) {
			this.mLeftDrawRect.set(0, this.mCurrentSlideY, this.mCurrentSlideX, this.mCurrentSlideY + this.mShadow.getHeight());
			canvas.drawBitmap(this.mShadow, null, this.mLeftDrawRect, null);
			this.mRightDrawRect.set(this.mCurrentSlideX + this.mSlideIcon.getWidth(), this.mCurrentSlideY, getWidth(), this.mCurrentSlideY
					+ this.mShadow.getHeight());
			canvas.drawBitmap(this.mShadow, null, this.mRightDrawRect, null);
			canvas.drawBitmap(this.mSlideIcon, this.mCurrentSlideX, this.mCurrentSlideY, null);
			return;
		}
		int scrollX = this.mScroller.getCurrX();
		int scrollY = this.mScroller.getCurrY();
		this.mLeftDrawRect.set(0, scrollY, scrollX, scrollY + this.mShadow.getHeight());
		canvas.drawBitmap(this.mShadow, null, this.mLeftDrawRect, null);
		this.mRightDrawRect.set(scrollX + this.mSlideIcon.getWidth(), scrollY, getWidth(),
				scrollY + this.mShadow.getHeight());
		canvas.drawBitmap(this.mShadow, null, this.mRightDrawRect, null);
		canvas.drawBitmap(this.mSlideIcon, scrollX, scrollY, null);
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if ((this.mTotalItemsCount > 0) && (getItemView(this.mCurrentItemPosition) != null)) {
			View currentItemView = getItemView(this.mCurrentItemPosition);
			this.mCurrentSlideX = (currentItemView.getLeft() + currentItemView.getWidth() / 2 - this.mSlideIcon.getWidth() / 2);
			this.mCurrentSlideY = (b - t - this.mSlideIcon.getHeight());
		}
	}

	private static class TabsItemViewClickListener implements View.OnClickListener {
		
		private WeakReference<AnimTabsView> mAnimTabsViewReference = null;
		
		TabsItemViewClickListener(AnimTabsView animTabsView) {
			mAnimTabsViewReference = new WeakReference<AnimTabsView>(animTabsView);
		}

		public void onClick(View view) {
			int clickItemPosition = ((Integer) view.getTag()).intValue();
			int currentSeletedItemPosition = (mAnimTabsViewReference.get().getCurrentItemPosition());
			if (currentSeletedItemPosition != clickItemPosition && mAnimTabsViewReference.get() != null) {
				mAnimTabsViewReference.get().selecteItem(clickItemPosition);
				if (mAnimTabsViewReference.get().mItemViewChangeListener != null)
					mAnimTabsViewReference.get().mItemViewChangeListener.onChange(mAnimTabsViewReference.get(), clickItemPosition, currentSeletedItemPosition);
			}
		}
	}

	public abstract interface IAnimTabsItemViewChangeListener {
		public abstract void onChange(AnimTabsView tabsView, int oldPosition, int currentPosition);
	}
}