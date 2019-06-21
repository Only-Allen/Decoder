package com.chx.decoder.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.chx.decoder.R;
import com.chx.decoder.comparator.ComparatorFactory;
import com.chx.decoder.decoder.SwiftDecoder;
import com.chx.decoder.decoder.result.Bounds;
import com.chx.decoder.decoder.result.DecoderResult;
import com.chx.decoder.decoder.result.Point;
import com.chx.decoder.event.ROIFinishedEvent;
import com.chx.decoder.event.ROIRemovedEvent;
import com.chx.decoder.event.ROISelectedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DecodeActivity extends BaseActivity {

    protected FrameLayout mResultContainer, mImageResultContainer;
    protected FrameLayout mDrawLayout;
    protected ScrollView mTextResultContainer;
    protected LinearLayout mOperationLayout;
    protected Spinner mSpinner;
    protected CheckBox mCheckBox;
    protected List<DecoderResult> mResults;
    protected ComparatorFactory.Type mType = ComparatorFactory.Type.LINE;
    protected final int VIEW_SIZE = 80;
    protected final int VIEW_MARGIN = 20;
    protected final int TEXT_SIZE = 16;
    private boolean isMove;
    private final int COUNT_INGORE = 5;
    private int count;

    protected List<Rect> mRects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        initView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SwiftDecoder.getInstance().release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    protected abstract int getLayoutResource();

    protected void initView() {
        mResultContainer = (FrameLayout) findViewById(R.id.result_container);
        mResultContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResultClick();
            }
        });
        mImageResultContainer = (FrameLayout) findViewById(R.id.result_container_image);
        mTextResultContainer = (ScrollView) findViewById(R.id.result_container_text);
        mTextResultContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isMove = false;
                        count = 0;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isMove) {
                            onResultClick();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isMove) {
                            if (count++ > COUNT_INGORE) {
                                isMove = true;
                            }
                        }
                        break;
                }
                return false;
            }
        });

        mOperationLayout = (LinearLayout) findViewById(R.id.layout_operation);
        findViewById(R.id.btn_decode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDecodeClick();
            }
        });
        mCheckBox = (CheckBox) findViewById(R.id.roi_check_box);
        mSpinner = (Spinner) findViewById(R.id.sort_spinner);
        mSpinner.setAdapter(ArrayAdapter.createFromResource(
                this, R.array.sort_mode, android.R.layout.simple_list_item_1));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mType = ComparatorFactory.Type.LINE;
                        break;
                    case 1:
                        mType = ComparatorFactory.Type.ROW;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDrawLayout = (FrameLayout) findViewById(R.id.layout_region);
    }

    protected abstract void onDecodeClick();

    protected void onResultClick() {
        mResults = null;
        mResultContainer.setVisibility(View.GONE);
        mOperationLayout.setVisibility(View.VISIBLE);
        clearAndHideDrawLayout();
    }

    protected void decodeBitmap(Bitmap bitmap) {
        if (SwiftDecoder.getInstance().decode(bitmap) == 0) {
            Toast.makeText(getApplicationContext(), "Decoding failed", Toast.LENGTH_LONG).show();
        } else {
            List<DecoderResult> results = SwiftDecoder.getInstance().getResults();
            if (results == null || results.size() == 0) {
                return;
            }
            beforeShowResults();
            if (isROIEnabled()) {
                mResults = results;
                showROILayout();
            } else {
                onShowResults(results);
            }
        }
    }

    protected void beforeShowResults() {
        mOperationLayout.setVisibility(View.INVISIBLE);
    }

    private void onShowResults(List<DecoderResult> results) {
        mResultContainer.setVisibility(View.VISIBLE);
        sortResults(results);
        showResultsByText(results);
        showResultsByImage(results);
    }

    private void sortResults(List<DecoderResult> results) {
        Collections.sort(results, ComparatorFactory.getComparator(mType));
    }

    private void showResultsByText(List<DecoderResult> results) {
        mTextResultContainer.removeAllViews();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView v = new TextView(this);
        v.setPadding(10, 10, 10, 10);
        v.setTextColor(Color.WHITE);
        v.setTextSize(16);
//        StringBuilder sb = new StringBuilder("total:" + results.size() + "\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            DecoderResult result = results.get(i);
            if (result == null) {
                sb.append((i + 1) + "NO READ\n");
            } else {
                sb.append((i + 1) + ":" + result.getResult() + "\n");
            }
            if (i < results.size() - 1) {
                sb.append("\n");
            }
        }
        v.setText(sb.toString());
        mTextResultContainer.addView(v, layoutParams);
    }

    private void showResultsByImage(List<DecoderResult> results) {
        mImageResultContainer.removeAllViews();
        int size = results.size();
        for (int i = 0; i < size; i++) {
            DecoderResult result = results.get(i);
            if (result != null) {
                TextView tv = new TextView(this);
                tv.setTextColor(getResources().getColor(R.color.mark_text));
                tv.setTextSize(TEXT_SIZE);
                tv.setBackground(getResources().getDrawable(R.drawable.mark_view_style));
                tv.setText(String.format("%d/%d", i + 1, size));
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(VIEW_SIZE, VIEW_SIZE);
                fillParams(result, params);
                mImageResultContainer.addView(tv, params);
            }
        }
    }

    private void fillParams(DecoderResult result, FrameLayout.LayoutParams params) {
        Point point;
        if (mCheckBox.isChecked()) {
            point = result.getBounds().getTopLeft();
        } else {
            point = getViewPointByBitmapPoint(result.getBounds().getMarkPoint());
        }
        int marginLeft = point.getX() - VIEW_MARGIN;
        if (marginLeft < 0) marginLeft = 0;
        int marginTop = point.getY() - VIEW_MARGIN;
        if (marginTop < 0) marginTop = 0;
        params.setMargins(marginLeft, marginTop, 0, 0);
    }

    protected abstract Point getViewPointByBitmapPoint(Point point);

    private boolean isROIEnabled() {
        return mCheckBox.isChecked();
    }

    private void showROILayout() {
        mDrawLayout.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onROISelected(ROISelectedEvent event) {
        Rect rect = event.getRect();
        if (mRects == null) {
            mRects = new ArrayList<>();
        }
        mRects.add(rect);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                rect.right - rect.left, rect.bottom - rect.top);
        params.setMargins(rect.left, rect.top, rect.right, rect.bottom);
        TextView tv = new TextView(this);
        tv.setBackground(getResources().getDrawable(R.drawable.roi_border));
        tv.setTextColor(getResources().getColor(R.color.green_light));
        tv.setTag(rect);
        tv.setGravity(Gravity.CENTER);
        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDrawLayout.removeView(v);
                Rect r = (Rect) v.getTag();
                mRects.remove(r);
                EventBus.getDefault().post(new ROIRemovedEvent(r));
                return true;
            }
        });
        mDrawLayout.addView(tv, params);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onROIFinished(ROIFinishedEvent event) {
        List<DecoderResult> results = new ArrayList<>();
//        for (DecoderResult result : mResults) {
//            if (isInROI(result)) {
//                results.add(result);
//            }
//        }
        checkAndMarkRoi(results);
        onShowResults(results);
    }

    public boolean isInROI(DecoderResult result) {
        if (mRects == null) {
            return false;
        }
        for (Rect rect : mRects) {
            if (isResultInRect(rect, result)) {
                return true;
            }
        }
        return false;
    }

    private void checkAndMarkRoi(List<DecoderResult> results) {
        for (int i = 0; i < mRects.size(); i++) {
            Rect rect = mRects.get(i);
            boolean hasResult = false;
            DecoderResult decoderResult = null;
            for (DecoderResult result : mResults) {
                if (isResultInRect(rect, result)) {
                    hasResult = true;
                    if (decoderResult == null) {
                        result.setSiteWithRect(rect);
                        decoderResult = result;
                        results.add(result);
                    } else {
                        decoderResult.setResult(decoderResult.getResult()
                                + "\n--" + result.getResult());
                    }
                }
            }
            if (!hasResult) {
                ((TextView) mDrawLayout.getChildAt(i + 1)).setText("No Read");
                DecoderResult r = new DecoderResult(new Point(), new Bounds(new Point(),
                        new Point(), new Point(), new Point()), "No Read", 7);
                r.setSiteWithRect(rect);
                r.setResult("No Read");
                results.add(r);
            }
        }
    }

    private boolean isResultInRect(Rect rect, DecoderResult result) {
//        检测四个边界坐标都在rect中
//        Point topLeftPoint = getViewPointByBitmapPoint(result.getBounds().getTopLeft());
//        Point topRightPoint = getViewPointByBitmapPoint(result.getBounds().getTopRight());
//        Point bottomLeftPoint = getViewPointByBitmapPoint(result.getBounds().getBottomLeft());
//        Point bottomRightPoint = getViewPointByBitmapPoint(result.getBounds().getBottomRight());
//        return rect.contains(topLeftPoint.getX(), topLeftPoint.getY())
//                && rect.contains(topRightPoint.getX(), topRightPoint.getY())
//                && rect.contains(bottomLeftPoint.getX(), bottomLeftPoint.getY())
//                && rect.contains(bottomRightPoint.getX(), bottomRightPoint.getY());

//      检测中心坐标在rect中
        Point center = getViewPointByBitmapPoint(result.getCenter());
        return rect.contains(center.getX(), center.getY());
    }

    private void clearAndHideDrawLayout() {
        if (!mCheckBox.isChecked()) {
            return;
        }
        mRects.clear();
        while (mDrawLayout.getChildCount() > 1) {
            mDrawLayout.removeViewAt(1);
        }
        mDrawLayout.setVisibility(View.GONE);
    }
}
