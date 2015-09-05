/*
 * Copyright (c) 2012 Kurt Mbanje
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   ckurtm at gmail dot com
 *   https://github.com/ckurtm/DroidProvider
 */

package mbanje.kurt.todo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import mbanje.kurt.todo.R;

/**
 * Created by kurt on 2014/07/21.
 */
public class ProgressView extends View {
//    String TAG = ProgressView.class.getSimpleName();

    private int percent = 0;
    private int barColor = Color.BLUE;
    int width = -1;
    int height = -1;
    private Rect bounds = new Rect();
    private Rect barBounds = new Rect();
    private Paint barPaint;
    private int boundsStep = 0;
    private static final Handler handler = new Handler();
    private int step = 0;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };


    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, 0);
        try {
            barColor = a.getColor(R.styleable.ProgressView_barColor, Color.BLUE);
        } finally {
            a.recycle();
        }
        init();
    }

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public void setPercent(int percent) {
//        Log.d(TAG, "setPercent() : [percent:" + percent + "] [step:" + step + "]");
        if (percent >= 0 && percent <= 100) {
            this.percent = percent;
            invalidate();
        }
    }

    public void init() {
        barPaint = new Paint();
        barPaint.setAntiAlias(true);
        barPaint.setColor(barColor);
        barPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (width <= 0) {
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();
            width = getWidth() - paddingLeft - paddingRight;
            height = getHeight() - paddingTop - paddingBottom;
            bounds.set(0, 0, width, height);
            barBounds.set(0, step, 0, height);
            boundsStep = (int) Math.ceil((float) bounds.width() / 100f);
//            Log.d(TAG, "bounds: " + bounds);
//            Log.d(TAG, "barbounds: " + barBounds);
//            Log.d(TAG, "boundsStep: " + boundsStep);
        }
    }

    public int getPercent() {
        return percent;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (step > percent) {
            barBounds.right = barBounds.right - boundsStep;
            step--;
        } else {
            step++;
            barBounds.right = barBounds.right + boundsStep;
        }
        canvas.drawRect(barBounds, barPaint);
        if (step != percent) {
            handler.postDelayed(runnable, 20);
        }
    }

}

