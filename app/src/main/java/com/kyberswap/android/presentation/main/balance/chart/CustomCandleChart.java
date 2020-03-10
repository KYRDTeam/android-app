package com.kyberswap.android.presentation.main.balance.chart;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CandleStickChart;

public class CustomCandleChart extends CandleStickChart {

    public CustomCandleChart(Context context) {
        super(context);
    }

    public CustomCandleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCandleChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        setOnTouchListener(new CandleStickChartTouchListener(this, mViewPortHandler.getMatrixTouch(), 3f));
    }

    @Override
    public void computeScroll() {
        if (mChartTouchListener instanceof CandleStickChartTouchListener)
            ((CandleStickChartTouchListener) mChartTouchListener).computeScroll();
    }
}
