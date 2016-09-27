package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
    private String LOG_TAG = ForecastAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean mUseTodayLayout = true;

    private Cursor mCursor;
    private Context mContext;
    private ForecastAdapterOnClickHandler mClickHandler;

    /*
    * RecyleView 标准化了 viewholder 模式。必须使用
    * */
    public class ForecastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;

        public ForecastViewHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            view.setOnClickListener(this);
        }

        // 为每个 item view 设置监听器，获取当前 item 日期数据。传入回调函数
        @Override
        public void onClick(View v) {
            int adapterPosition = getPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mClickHandler.onClickHandler(mCursor.getLong(dateColumnIndex), this);
        }
    }

    // 定义一个给外部的接口，当点击时传递数据
    public static interface ForecastAdapterOnClickHandler {
        void onClickHandler(Long Date, ForecastViewHolder vh);
    }

    // 构造函数除 Context 外必须接收一个 onClickHandler 用于处理点击事件
    public ForecastAdapter(Context context,ForecastAdapterOnClickHandler onClickHandler) {
        mContext = context;
        mClickHandler = onClickHandler;
    }

    @Override
    public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ForecastViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        /*
        * 今日布局将使用彩色天气图标，未来几日将使用黑白天气图标
        * */
        int viewType = getItemViewType(position);
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        if (viewType == VIEW_TYPE_TODAY) {
            holder.mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        } else {
            holder.mIconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }

        long dateInMillis = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        if (viewType == VIEW_TYPE_TODAY) {
            String location = Utility.getPreferredLocation(mContext);
            holder.mDateView.setText(
                    String.format("%s | %s", Utility.getFriendlyDayString(mContext, dateInMillis), Utility.getPreferredLocation(mContext)));
        } else {
            holder.mDateView.setText(Utility.getFriendlyDayString(mContext, dateInMillis));
        }

        String description = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.mDescriptionView.setText(description);

        // 无障碍使用列表中的一个辅助功能，点击图标时，能够读出相应的天气概况
        holder.mIconView.setContentDescription(description);

        boolean isMetric = Utility.isMetric(mContext);
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        holder.mHighTempView.setText(Utility.formatTemperature(mContext, high, isMetric));

        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.mLowTempView.setText(Utility.formatTemperature(mContext, low, isMetric));
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

//    public Cursor getCursor() {
//        return mCursor;
//    }

}