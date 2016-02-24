package com.example.dell.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by lgp on 2015/3/24.
 */
public class BubbleTextVew extends TextView {
    private BubbleDrawable bubbleDrawable;
    private float mArrowWidth = 10;
    private float mAngle = 10;
    private float mArrowHeight = 10;
    private float mArrowPosition = 20;
    private int bubbleColor;
    private BubbleDrawable.ArrowLocation mArrowLocation = BubbleDrawable.ArrowLocation.LEFT;
    private View anchorView;

    public BubbleTextVew(Context context) {
        super(context);

        initView(null);
    }

    public void setmArrowLocation(BubbleDrawable.ArrowLocation mArrowLocation) {
        this.mArrowLocation = mArrowLocation;
    }

    public BubbleTextVew(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public BubbleTextVew(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(attrs);
    }

    private void initView(AttributeSet attrs){
        if (attrs != null){
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.BubbleView);

            mArrowWidth = array.getDimension(R.styleable.BubbleView_arrowWidth,
                BubbleDrawable.Builder.DEFAULT_ARROW_WITH);
            mArrowHeight = array.getDimension(R.styleable.BubbleView_arrowHeight,
                BubbleDrawable.Builder.DEFAULT_ARROW_HEIGHT);
            mAngle = array.getDimension(R.styleable.BubbleView_angle,
                BubbleDrawable.Builder.DEFAULT_ANGLE);
            mArrowPosition = array.getDimension(R.styleable.BubbleView_arrowPosition,
                BubbleDrawable.Builder.DEFAULT_ARROW_POSITION);
            bubbleColor = array.getColor(R.styleable.BubbleView_bubbleColor, 0);
            int location = array.getInt(R.styleable.BubbleView_arrowLocation, 0);
            mArrowLocation = BubbleDrawable.ArrowLocation.mapIntToValue(location);

            if(bubbleColor==0){
                bubbleColor=getContext().getResources().getColor(R.color.colorAccent);
            }

            array.recycle();
        }
        setUpPadding();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0){
            setUp(w, h);
        }
    }

    public void setBubbleColor(int bubbleColor) {
        this.bubbleColor = bubbleColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bubbleDrawable != null)
            bubbleDrawable.draw(canvas);
        super.onDraw(canvas);
    }

    private void setUp(int width, int height){
        setUp(0, width , 0, height);
    }

    private void setUp(){
        setUp(getWidth(), getHeight());
    }

    private void setUp(int left, int right, int top, int bottom){
        RectF rectF = new RectF(left, top, right, bottom);
        bubbleDrawable = new BubbleDrawable.Builder()
                .rect(rectF)
                .arrowLocation(mArrowLocation)
                .bubbleType(BubbleDrawable.BubbleType.COLOR)
                .angle(mAngle)
                .arrowHeight(mArrowHeight)
                .arrowWidth(mArrowWidth)
                .bubbleColor(bubbleColor)
                .arrowPosition(mArrowPosition)
                .build();

    }

    private void setUpPadding(){
        int left = getPaddingLeft();
        int right = getPaddingRight();
        int top = getPaddingTop();
        int bottom = getPaddingBottom();
        switch (mArrowLocation){
            case LEFT:
                left += mArrowWidth + 20;
                break;
            case RIGHT:
                right += mArrowWidth+10;
                break;
            case TOP:
                top += mArrowHeight + 20;
                break;
            case BOTTOM:
                bottom += mArrowHeight + 30;
                break;
        }
        setPadding(left, top, right, bottom);
    }

}
