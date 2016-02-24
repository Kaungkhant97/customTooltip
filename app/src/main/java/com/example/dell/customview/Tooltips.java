package com.example.dell.customview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Dell on 2/17/2016.
 */
public class Tooltips {
  private static final String MyPREFERENCES1 = "MyPrefs";
  public static boolean dbg = false;
  private static SharedPreferences sharedpreferences;
  private static SharedPreferences.Editor editor;
  private static Hashtable<Integer, TooltipView> TooltipViewWithSequenceTable;
  private static Hashtable<Integer, TooltipView> TooltipViewWithoutSequenceTable;
  private static List<Integer> keylist;

  private static int count = 0;
  private static int withoutSequence = 375;
  public Tooltips() {
  }

  public static void make(Context context, Builder builder) {
//j
    if (count == 0) {
      TooltipViewWithSequenceTable = new Hashtable<Integer, TooltipView>();
      keylist = new ArrayList<>();
      sharedpreferences =
          Utils.getActivity(context).getSharedPreferences(MyPREFERENCES1, Context.MODE_PRIVATE);
      editor = sharedpreferences.edit();
      TooltipViewWithoutSequenceTable = new Hashtable<Integer, TooltipView>();
    }
    if (builder.isSequence) {
      keylist.add(builder.sequenceID);
      Collections.sort(keylist);
    } else if (!builder.isSequence) {
      withoutSequence++;
      TooltipViewWithoutSequenceTable.put(withoutSequence, new TooltipView(context, builder));
    }
    TooltipViewWithSequenceTable.put(builder.sequenceID, new TooltipView(context, builder));
    count++;
  }

  private static void checkAndShowSequenceTooltips() {
    for (int keys : keylist) {
      if (!TooltipIsSeen(keys)) {
        TooltipViewWithSequenceTable.get(keys).show();
        break;
      }
    }
  }

  private static boolean TooltipIsSeen(int keynum) {
    Log.e("sasd", "check tool tip");
    return sharedpreferences.getBoolean("" + keynum, false);
  }

  private static void checkAndShownormalTooltips() {
    for (int i = 376; i <= withoutSequence; i++) {
      if (!TooltipIsSeen(i)) {
        TooltipViewWithoutSequenceTable.get(i).show();
      }
    }
  }

  private static void markToolTip(int keynum) {
    editor.putBoolean("" + keynum, true);
    Log.e("mark", keynum + "tool tip is marked");
    editor.apply();
    editor.commit();
  }


  public static class TooltipView extends ViewGroup {
    private static final List<Direction> DIRECTION_LIST = new ArrayList<>(
        Arrays.asList(Direction.LEFT, Direction.RIGHT, Direction.BOTTOM, Direction.TOP));
     private final List<Direction> arrowDirections = new ArrayList<>(DIRECTION_LIST);
    private int mWidth;
    private int mtextColor;
    private int mColor;
    private  Typeface mTypeface;


    private WeakReference<View> mViewAnchor;
    private Rect mHitRect = new Rect();
    private Rect mViewRect;
    private Rect mDrawRect;
    private boolean mRestrict;
    private String mText;
    private int mTextResId;
    private final int[] mTempLocation = new int[2];
    private int[] mOldLocation;
    private int mPadding;
    private int mToolTipId;
    private Direction mDirection;
    private boolean mAttached;
    private Rect mScreenRect = new Rect();
    private View mView;
    private BubbleTextVew mTextView;
    private final Rect mTempRect = new Rect();

    private final ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener =
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            Log.e("onGloabellayout","here");
            if (!mAttached) {

              return;
            }

            if (null != mViewAnchor) {
              View view = mViewAnchor.get();

              if (null != view) {

                view.getHitRect(mTempRect);
                view.getLocationOnScreen(mTempLocation);



                if (!mTempRect.equals(mHitRect)) {
                  mHitRect.set(mTempRect);

                  mTempRect.offsetTo(mTempLocation[0], mTempLocation[1]);
                  mViewRect.set(mTempRect);
                  calculatePositions();
                }
              }
            }
          }
        };
    private final ViewTreeObserver.OnPreDrawListener mPreDrawListener =
        new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            Log.e("on predraw","here");
            if (!mAttached) {
              return true;
            }

            if (null != mViewAnchor) {
              View view = mViewAnchor.get();
              if (null != view) {
                view.getLocationOnScreen(mTempLocation);

                if (mOldLocation == null) {
                  mOldLocation = new int[] { mTempLocation[0], mTempLocation[1] };
                }

                if (mOldLocation[0] != mTempLocation[0] || mOldLocation[1] != mTempLocation[1]) {
                  mView.setTranslationX(
                      mTempLocation[0] - mOldLocation[0] + mView.getTranslationX());
                  mView.setTranslationY(
                      mTempLocation[1] - mOldLocation[1] + mView.getTranslationY());


                }

                mOldLocation[0] = mTempLocation[0];
                mOldLocation[1] = mTempLocation[1];
                calculatePositions();
              }
            }
            return true;
          }
        };
    private final OnAttachStateChangeListener mAttachedStateListener =
        new OnAttachStateChangeListener() {
          @Override public void onViewAttachedToWindow(final View v) {
           checkAndShownormalTooltips();
            checkAndShowSequenceTooltips();
          }

          @Override @TargetApi(17) public void onViewDetachedFromWindow(final View v) {

          }
        };


    public TooltipView(Context context, final Builder builder) {
      super(context);

      this.mPadding = 30;

      if (builder.isSequence) {
        this.mToolTipId = builder.sequenceID;
      } else this.mToolTipId = withoutSequence;

      this.mText = builder.Text;
      this.mTypeface = builder.typeface;
      this.mWidth = builder.maxwidth;
      this.mColor = builder.color;
      this.mtextColor = builder.textColor;
      this.mDirection = builder.arrowDirection;
      this.mTextResId = builder.textResId;
      this.mRestrict = builder.restrictToScreenEdges;

      setClipChildren(false);
      setClipToPadding(false);

      this.mDrawRect = new Rect();

      if (builder.anchor != null) {
        mViewRect = new Rect();

        builder.anchor.getHitRect(mHitRect); //get anchor view reactangle in mHitRect
        builder.anchor.getLocationOnScreen(mTempLocation);//get anchor view location on screen in mTemplocation

        //set anchor view rectangle and location in mViewRect
        mViewRect.set(mHitRect);
        mViewRect.offsetTo(mTempLocation[0], mTempLocation[1]);

        mViewAnchor = new WeakReference<>(builder.anchor);

        if (builder.anchor.getViewTreeObserver().isAlive()) {
          builder.anchor.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
          builder.anchor.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
          builder.anchor.addOnAttachStateChangeListener(mAttachedStateListener);

        }
      }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      int myWidth = 0;
      int myHeight = 0;
      Log.e("on measure","here");
      final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
      final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
      int widthSize = MeasureSpec.getSize(widthMeasureSpec);
      int heightSize = MeasureSpec.getSize(heightMeasureSpec);

      // Record our dimensions if they are known;
      if (widthMode != MeasureSpec.UNSPECIFIED) {
        myWidth = widthSize;
      }

      if (heightMode != MeasureSpec.UNSPECIFIED) {
        myHeight = heightSize;
      }

      if (null != mView) {
        if (mView.getVisibility() != GONE) {
          int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(myWidth, MeasureSpec.AT_MOST);
          int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(myHeight, MeasureSpec.AT_MOST);
          mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
          myWidth = 0;
          myHeight = 0;
        }
      }
    }

    @Override protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      mAttached = true;
      WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      display.getRectSize(mScreenRect);
      initializedView();

    }

    private void initializedView() {
      Log.e("on initialize","herhe");


      LayoutParams params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
      mView = LayoutInflater.from(getContext()).inflate(mTextResId, this, false);
      mView.setLayoutParams(params);
      mTextView = (BubbleTextVew) mView.findViewById(android.R.id.text1);
      mTextView.setText(mText);
      if(mColor!=0){
        mTextView.setBubbleColor(mColor);
      }if(mTypeface!=null){
        mTextView.setTypeface(mTypeface);
      }if(mtextColor != 0){
        mTextView.setTextColor(mtextColor);
      }else  mTextView.setTextColor(Color.parseColor("#eefeef"));


       mTextView.setMaxWidth(this.mWidth);



     switch(mDirection){
        case LEFT:mTextView.setmArrowLocation(BubbleDrawable.ArrowLocation.RIGHT);
          break;
        case RIGHT:mTextView.setmArrowLocation(BubbleDrawable.ArrowLocation.LEFT);
          break;
        case TOP:mTextView.setmArrowLocation(BubbleDrawable.ArrowLocation.BOTTOM);
          break;
        case BOTTOM:mTextView.setmArrowLocation(BubbleDrawable.ArrowLocation.TOP);
          break;
      }

      mTextView.setOnClickListener(new OnClickListener() {
        @Override public void onClick(View v) {
          markToolTip(mToolTipId);
          mTextView.setVisibility(View.GONE);
          checkAndShowSequenceTooltips();
        }
      });


      mTextView.setPadding(40,20,30,30);

      this.addView(mView);
    }

    public void show() {
      if (getParent() == null) {

        final Activity act = Utils.getActivity(getContext());
        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        if (act != null) {
          ViewGroup rootView;
          rootView = (ViewGroup) (act.getWindow().getDecorView());
          rootView.addView(this, params);
        }
      }
    }
    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
      Log.e("on layout", "here");
      if (null != mView) {
        mView.layout(mView.getLeft(), mView.getTop(), mView.getMeasuredWidth(),
            mView.getMeasuredHeight());
      }
      if (changed) {
        if (mViewAnchor != null) {
          View view = mViewAnchor.get();
          if (null != view) {
            view.getHitRect(mTempRect);
            view.getLocationOnScreen(mTempLocation);
            mTempRect.offsetTo(mTempLocation[0], mTempLocation[1]);
            mViewRect.set(mTempRect);
          }
        }
        calculatePositions();
      }

    }

  /*  private void calculatePositions() {
      calculatePositions(mRestrict);
    }
    private void calculatePositions(boolean restrict) {
      arrowDirections.clear();
      arrowDirections.addAll(DIRECTION_LIST);
      arrowDirections.remove(mDirection);
      arrowDirections.add(0, mDirection);
      calculatePositions(arrowDirections, restrict);

    }*/
    private void calculatePositions() {




       final int screenTop = mScreenRect.top;

      int width = mView.getWidth();
      int height = mView.getHeight();



      switch (mDirection){

      }
      if (direction == Direction.BOTTOM) {
        if (calculatePositionBottom(checkEdges, screenTop, width, height)) {
          calculatePositions(directions, checkEdges);
          return;
        }
      } else if (direction == Direction.TOP) {
        if (calculatePositionTop(checkEdges, screenTop, width, height)) {

          calculatePositions(directions, checkEdges);
          return;
        }
      } else if (direction == Direction.RIGHT) {
        if (calculatePositionRight(checkEdges, screenTop, width, height)) {
          calculatePositions(directions, checkEdges);
          return;
        }
      } else if (direction == Direction.LEFT) {
        if (calculatePositionLeft(checkEdges, screenTop, width, height)) {
          calculatePositions(directions, checkEdges);
          return;
        }
      }

      if (direction != mDirection) {
        mDirection = direction;
      }
      // translate the text view

      mView.setTranslationX(mDrawRect.left);
      mView.setTranslationY(mDrawRect.top);
    }
    private boolean calculatePositionLeft(final boolean checkEdges,
        final int screenTop, final int width, final int height) {
      mDrawRect.set(mViewRect.left - width, mViewRect.centerY() - height / 2, mViewRect.left,
          mViewRect.centerY() + height / 2);

        if (mDrawRect.bottom > mScreenRect.bottom) {
          mDrawRect.offset(0, mScreenRect.bottom - mDrawRect.bottom);
        } else if (mDrawRect.top < screenTop) {
          mDrawRect.offset(0, screenTop - mDrawRect.top);
        }
        if (mDrawRect.left < mScreenRect.left) {
          // this means there's no enough space!
          return true;
        } else if (mDrawRect.right > mScreenRect.right) {
          mDrawRect.offset(mScreenRect.right - mDrawRect.right, 0);
        }

      return false;
    }

    private boolean calculatePositionRight(final boolean checkEdges,
        final int screenTop, final int width, final int height) {
      mDrawRect.set(mViewRect.right, mViewRect.top, mViewRect.right + width,
          mViewRect.centerY() + height / 2);//left,top,right,bot

        if (mDrawRect.bottom > mScreenRect.bottom) {
          mDrawRect.offset(0, mScreenRect.bottom - mDrawRect.bottom);
        } else if (mDrawRect.top < screenTop) {
          mDrawRect.offset(0, screenTop - mDrawRect.top);
        }
        if (mDrawRect.right > mScreenRect.right) {
          // this means there's no enough space!
          return true;
        } else if (mDrawRect.left < mScreenRect.left) {
          mDrawRect.offset(mScreenRect.left - mDrawRect.left, 0);
        }

      return false;
    }

    private boolean calculatePositionTop(final boolean checkEdges,
        final int screenTop, final int width, final int height) {
      mDrawRect.set(mViewRect.centerX() - width / 2, mViewRect.top - height,
          mViewRect.centerX() + width / 2, mViewRect.top);


        if (mDrawRect.right > mScreenRect.right) {
          mDrawRect.offset(mScreenRect.right - mDrawRect.right, 0);
        } else if (mDrawRect.left < mScreenRect.left) {
          mDrawRect.offset(-mDrawRect.left, 0);
        }
        if (mDrawRect.top < screenTop) {
          // this means there's no enough space!
          return true;
        } else if (mDrawRect.bottom > mScreenRect.bottom) {
          mDrawRect.offset(0, mScreenRect.bottom - mDrawRect.bottom);
        }

      return false;
    }

    private boolean calculatePositionBottom(final boolean checkEdges,
        final int screenTop, final int width, final int height) {
      mDrawRect.set(mViewRect.centerX() - width / 2, mViewRect.bottom,
          mViewRect.centerX() + width / 2, mViewRect.bottom + height);

        if (mDrawRect.right > mScreenRect.right) {
          mDrawRect.offset(mScreenRect.right - mDrawRect.right, 0);
        } else if (mDrawRect.left < mScreenRect.left) {
          mDrawRect.offset(-mDrawRect.left, 0);
        }
        if (mDrawRect.bottom > mScreenRect.bottom) {
          // this means there's no enough space!
          return true;
        } else if (mDrawRect.top < screenTop) {
          mDrawRect.offset(0, screenTop - mDrawRect.top);
        }

      return false;
    }



  }
  public  enum Direction {
    LEFT, RIGHT, TOP, BOTTOM,
  }
  public static class Builder {
    private String Text;
    private View anchor;
    private int color;
    private Typeface typeface;
    private Tooltips.Direction arrowDirection;
    private int sequenceID=0;
    private boolean isSequence = false;
    private boolean completed = false;
    private boolean restrictToScreenEdges = true;
    private int textResId = R.layout.tooltip_textview;
    private int maxwidth = 300;
    private int textColor;

    public Builder(String Text, View anchor, Direction direction) {

      this.Text = Text;
      this.anchor = anchor;
      this.arrowDirection = direction;


    }



    private void CheckBuilderHaveBuilt() {
      if (completed) {
        throw new IllegalStateException("Builder cannot be modified");
      }
    }
    public Builder setColor(int color){
      CheckBuilderHaveBuilt();
      this.color= color;
      return this;
    }
    public Builder setTypeface(Typeface face){
      CheckBuilderHaveBuilt();
      this.typeface = face;
      return this;
    }
    public Builder setmaxWidth(int maxwidth){
      CheckBuilderHaveBuilt();
      this.maxwidth = maxwidth;
      return this;
    }
    public Builder setTextColor(int color){
      CheckBuilderHaveBuilt();
      this.textColor= color;
      return this;
    }

    public Builder withSquence(int id) {
      CheckBuilderHaveBuilt();
      this.sequenceID = id;
      this.isSequence = true;
      return this;
    }

    public Builder build() {
      CheckBuilderHaveBuilt();

      completed = true;

      return this;
    }
  }
}
