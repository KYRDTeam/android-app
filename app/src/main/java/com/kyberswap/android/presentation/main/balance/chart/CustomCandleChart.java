package com.kyberswap.android.presentation.main.balance.chart;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CandleStickChart;

public class CustomCandleChart extends CandleStickChart {

    private static final int MAX_CLICK_DURATION = 500;
    private long startClickTime;

    public CustomCandleChart(Context context) {
        super(context);
    }

    public CustomCandleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCandleChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }



//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN: {
//                startClickTime = Calendar.getInstance().getTimeInMillis();
//                break;
//            }
//            case MotionEvent.ACTION_UP: {
//                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
//                if (clickDuration > MAX_CLICK_DURATION) {
//                    return super.onTouchEvent(event);
//                }
//            }
//        }
//        return false;
//
//    }

    private boolean isShowingMarker() {
        return mMarker != null && isDrawMarkersEnabled() && valuesToHighlight();
    }
}
