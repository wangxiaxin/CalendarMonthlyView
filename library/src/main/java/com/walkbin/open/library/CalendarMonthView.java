package com.walkbin.open.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by wangxiaxin on 2016/9/18.<br>
 * <br>
 * 日历的月视图<br>
 * 显示相关的配置 请看 {@link CalendarMonthView.ShowParam}<br>
 * 日期相关的配置 请看 {@link CalendarMonthView.DateParam}
 */
public class CalendarMonthView extends View {
    
    public interface DaySelectCallback {
        void onDaySelected(int year, int month, int day);
    }
    
    /** 一周几天 定值，也固定了显示列数 */
    private static final int DAYS_OF_WEEK = 7;
    
    /** 当月需要显示几行 */
    private int rowCount;
    
    private ShowParam showParam;
    private DateParam dateParam;
    private final StringBuilder stringBuilder = new StringBuilder(50);
    private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();
    
    private Paint monthPaint;
    private Paint weekPaint;
    private Paint dayDisablePaint;
    private Paint dayPaint;
    private Paint selectDayBgPaint;
    private Paint testBgPaint;
    private Paint dayIndPaint;
    
    private Calendar todayCalendar;
    private int width;
    private DaySelectCallback selectCallback;
    private DisplayMetrics displayMetrics;
    
    private SimpleDateFormat yearAndMonthFormat = new SimpleDateFormat("yy年M月",
            Locale.CHINA);
    private SimpleDateFormat monthFormat = new SimpleDateFormat("M月", Locale.CHINA);
    
    public CalendarMonthView(Context context) {
        this(context, null);
    }
    
    public CalendarMonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public CalendarMonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDisplay();
    }
    
    /** 设置相关参数 */
    public CalendarMonthView setShowParam(ShowParam param) {
        showParam = param;
        checkShowParam();
        preparePaint();
        if (todayCalendar == null) {
            todayCalendar = Calendar.getInstance();
        }
        return this;
    }
    
    public CalendarMonthView setDateParam(DateParam param) {
        dateParam = param;
        rowCount = calcRowCount();
        requestLayout();
        return this;
    }
    
    public CalendarMonthView setOnDaySelectCallback(DaySelectCallback callback) {
        selectCallback = callback;
        return this;
    }
    
    private void initDisplay() {
        displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
    }
    
    private int dp2px(float dp) {
        return (int) (dp * displayMetrics.density + 0.5f);
    }
    
    private void checkShowParam() {
        if (showParam != null) {
            
            if (showParam.monthTitleHeight <= 0) {
                showParam.monthTitleHeight = dp2px(24);
            }
            
            if (showParam.rowHeight <= 0) {
                showParam.rowHeight = dp2px(35);
            }
            
            if (showParam.titleFontSize <= 0) {
                showParam.titleFontSize = dp2px(14);
            }
            
            if (showParam.weekFontSize <= 0) {
                showParam.weekFontSize = dp2px(12);
            }
            
            if (showParam.dateFontSize <= 0) {
                showParam.dateFontSize = dp2px(14);
            }
            
            if (showParam.dataUnSelectFontSize <= 0) {
                showParam.dataUnSelectFontSize = dp2px(14);
            }
        }
    }
    
    /** 准备画笔 */
    private void preparePaint() {
        monthPaint = new Paint();
        // monthPaint.setFakeBoldText(true);
        monthPaint.setAntiAlias(true);
        monthPaint.setTextSize(showParam.titleFontSize);
        // monthPaint.setTypeface(Typeface.create(mMonthTitleTypeface,
        // Typeface.BOLD));
        monthPaint.setColor(showParam.titleColor);
        monthPaint.setTextAlign(Paint.Align.LEFT);
        monthPaint.setStyle(Paint.Style.FILL);
        
        weekPaint = new Paint();
        weekPaint.setAntiAlias(true);
        weekPaint.setTextSize(showParam.weekFontSize);
        weekPaint.setColor(showParam.weekColor);
        // weekPaint.setTypeface(Typeface.create(mDayOfWeekTypeface,
        // Typeface.NORMAL));
        weekPaint.setStyle(Paint.Style.FILL);
        weekPaint.setTextAlign(Paint.Align.CENTER);
        weekPaint.setFakeBoldText(true);
        
        dayPaint = new Paint();
        dayPaint.setAntiAlias(true);
        dayPaint.setTextSize(showParam.dateFontSize);
        dayPaint.setStyle(Paint.Style.FILL);
        dayPaint.setTextAlign(Paint.Align.CENTER);
        dayPaint.setFakeBoldText(false);
        
        dayDisablePaint = new Paint();
        dayDisablePaint.setAntiAlias(true);
        dayDisablePaint.setTextSize(showParam.dataUnSelectFontSize);
        dayDisablePaint.setColor(showParam.dateUnSelectColor);
        dayDisablePaint.setStyle(Paint.Style.FILL);
        dayDisablePaint.setTextAlign(Paint.Align.CENTER);
        dayDisablePaint.setFakeBoldText(false);
        
        selectDayBgPaint = new Paint();
        selectDayBgPaint.setFakeBoldText(true);
        selectDayBgPaint.setAntiAlias(true);
        selectDayBgPaint.setColor(showParam.dateSelectBgColor);
        // selectDayBgPaint.setTextAlign(Paint.Align.CENTER);
        selectDayBgPaint.setStyle(Paint.Style.FILL);
        // selectDayBgPaint.setAlpha(SELECTED_CIRCLE_ALPHA);
        
        dayIndPaint = new Paint();
        dayIndPaint.setAntiAlias(true);
        dayIndPaint.setColor(showParam.dateIndColor);
        dayIndPaint.setStyle(Paint.Style.FILL);
        
        testBgPaint = new Paint();
        testBgPaint.setStyle(Paint.Style.FILL);
        testBgPaint.setAntiAlias(true);
        testBgPaint.setColor(Color.parseColor("#ff0000"));
    }
    
    private int findDayOffset() {
        int dayOfWeek = dateParam.dayOfWeekStart;
        if (dayOfWeek < showParam.showStartDayOfWeek) {
            dayOfWeek += DAYS_OF_WEEK;
        }
        
        return dayOfWeek - showParam.showStartDayOfWeek;
    }
    
    private int calcRowCount() {
        int offset = findDayOffset();
        int dividend = (offset + dateParam.days) / DAYS_OF_WEEK;
        int remainder = (offset + dateParam.days) % DAYS_OF_WEEK;
        return (dividend + (remainder > 0 ? 1 : 0));
    }
    
    private int dayByTouch(float x, float y) {
        
        if (y <= showParam.titleHeight())
            return 0;
        
        final int dayOffset = findDayOffset();
        final int cellWidth = width / DAYS_OF_WEEK;
        final int cellHeight = showParam.rowHeight;
        int day = ((int) y - showParam.titleHeight()) / cellHeight * DAYS_OF_WEEK
                + ((int) x) / cellWidth + 1;
        if (isPastedDay(dateParam.year, dateParam.month, day - dayOffset))
            return 0;
        
        if (day > dayOffset && day <= dayOffset + dateParam.days) {
            return day - dayOffset;
        }
        else {
            return 0;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int day = dayByTouch(event.getX(), event.getY());
            if (day > 0) {
                dateParam.selectDate = day;
                invalidate();
                if (selectCallback != null) {
                    selectCallback.onDaySelected(dateParam.year, dateParam.month, day);
                }
            }
        }
        return true;
    }
    
    private String getMonthAndYearString() {
        // int flags = DateUtils.FORMAT_SHOW_DATE |
        // DateUtils.FORMAT_NO_MONTH_DAY;
        // stringBuilder.setLength(0);
        final int month = dateParam.calendar.get(Calendar.MONTH);
        if (month == Calendar.DECEMBER) {
            return yearAndMonthFormat
                    .format(new Date(dateParam.calendar.getTimeInMillis()));
            // flags |= DateUtils.FORMAT_SHOW_YEAR;
        }
        else {
            return monthFormat.format(new Date(dateParam.calendar.getTimeInMillis()));
        }
        // long millis = dateParam.calendar.getTimeInMillis();
        // return DateUtils.formatDateRange(getContext(), millis, millis,
        // flags);
    }
    
    private void drawMonthTitle(Canvas canvas) {
        
        // testBgPaint.setColor(Color.RED);
        // canvas.drawRect(0, 0, width, showParam.monthTitleHeight(),
        // testBgPaint);
        
        Paint.FontMetricsInt fontMetrics = monthPaint.getFontMetricsInt();
        int x = dp2px(20);
        int y = (showParam.monthTitleHeight() - fontMetrics.bottom - fontMetrics.top) / 2;
        
        StringBuilder stringBuilder = new StringBuilder(
                getMonthAndYearString().toLowerCase());
        stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
        canvas.drawText(stringBuilder.toString(), x, y, monthPaint);
    }
    
    private void drawWeekTitle(Canvas canvas) {
        if (!showParam.showWeekTitle)
            return;
        
        Paint.FontMetricsInt fontMetrics = monthPaint.getFontMetricsInt();
        int y = showParam.monthTitleHeight()
                + (showParam.titleHeight() - showParam.monthTitleHeight()
                        - fontMetrics.bottom - fontMetrics.top) / 2;
        int dayWidthHalf = (width /*- mPadding * 2*/) / (DAYS_OF_WEEK * 2);
        
        // testBgPaint.setColor(Color.GREEN);
        // canvas.drawRect(0, showParam.monthTitleHeight(), width,
        // showParam.monthTitleHeight() + showParam.weekFontSize, testBgPaint);
        
        for (int i = 0; i < DAYS_OF_WEEK; i++) {
            int dayOfWeek = (i + showParam.showStartDayOfWeek) % (DAYS_OF_WEEK + 1);
            int x = (2 * i + 1) * dayWidthHalf /* + mPadding */;
            canvas.drawText(mDateFormatSymbols.getShortWeekdays()[dayOfWeek]
                    .toUpperCase(Locale.getDefault()), x, y, weekPaint);
        }
    }
    
    private boolean isToday(int year, int month, int day) {
        return todayCalendar.get(Calendar.YEAR) == year
                && todayCalendar.get(Calendar.MONTH) == month
                && todayCalendar.get(Calendar.DAY_OF_MONTH) == day;
    }
    
    private boolean isPastedDay(int year, int month, int day) {
        return todayCalendar.get(Calendar.YEAR) == year
                && todayCalendar.get(Calendar.MONTH) == month
                && day < todayCalendar.get(Calendar.DAY_OF_MONTH);
    }
    
    private void drawDays(Canvas canvas) {
        int x, y;
        // int paddingDay = (width/* - 2 * mPadding */) / (2 * DAYS_OF_WEEK);
        final int dayOffset = findDayOffset();
        final int cellWidth = width / DAYS_OF_WEEK;
        int top, bottom;
        int day;
        
        todayCalendar.setTimeInMillis(System.currentTimeMillis());
        
        // final int[] colors =
        // {Color.BLUE,Color.YELLOW,Color.CYAN,Color.MAGENTA,Color.RED,Color.GREEN,Color.BLUE};
        
        Paint.FontMetricsInt fontMetrics = monthPaint.getFontMetricsInt();
        for (int i = 0; i < rowCount * DAYS_OF_WEEK; i++) {
            
            top = showParam.titleHeight() + (i / DAYS_OF_WEEK) * showParam.rowHeight;
            bottom = top + showParam.rowHeight;
            
            y = (top + bottom - fontMetrics.bottom - fontMetrics.top) / 2;
            x = (i % DAYS_OF_WEEK) * cellWidth + cellWidth / 2;
            
            // if(i % DAYS_OF_WEEK == 0){
            // testBgPaint.setColor(colors[i / DAYS_OF_WEEK]);
            // canvas.drawRect(0, showParam.titleHeight() + (i / DAYS_OF_WEEK) *
            // showParam.rowHeight, width,
            // showParam.titleHeight() + (i / DAYS_OF_WEEK) *
            // showParam.rowHeight + showParam.rowHeight, testBgPaint);
            // }
            
            if (i < dayOffset) {
                // 前一个月的几天
                if (showParam.showPreAndNextDays) {
                    day = dateParam.daysPrevMonth - (dayOffset - i);
                    canvas.drawText(String.format(Locale.CHINA, "%d", day), x, y,
                            dayDisablePaint);
                }
            }
            else if (i >= (dateParam.days + dayOffset)) {
                // 后一个月的前几天
                if (showParam.showPreAndNextDays) {
                    day = i + 1 - (dateParam.days + dayOffset);
                    canvas.drawText(String.format(Locale.CHINA, "%d", day), x, y,
                            dayDisablePaint);
                }
            }
            else {
                // 当月的天
                day = i + 1 - dayOffset;
                
                if (dateParam.isSelectDay(dateParam.year, dateParam.month, day)) {
                    
                    switch (showParam.dateSelectBgShape) {
                        case CIRCLE:
                            canvas.drawCircle(x, (top + bottom) >> 1,
                                    showParam.rowHeight >> 1, selectDayBgPaint);
                            break;
                        case ROUND_RECT:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                canvas.drawRoundRect(x, top, x + cellWidth, bottom, 6, 6,
                                        selectDayBgPaint);
                            }
                            else {
                                canvas.drawRect(x, top, x + cellWidth, bottom,
                                        selectDayBgPaint);
                            }
                            break;
                        case RECT:
                            canvas.drawRect(x, top, x + cellWidth, bottom,
                                    selectDayBgPaint);
                            break;
                    }
                    
                    dayPaint.setFakeBoldText(true);
                    dayPaint.setColor(showParam.dateSelectColor);
                }
                else if (isToday(dateParam.year, dateParam.month, day)) {
                    dayPaint.setFakeBoldText(true);
                    dayPaint.setColor(showParam.dateTodayColor);
                }
                else if (isPastedDay(dateParam.year, dateParam.month, day)) {
                    dayPaint.setFakeBoldText(false);
                    dayPaint.setColor(showParam.datePassedColor);
                }
                else {
                    dayPaint.setFakeBoldText(false);
                    dayPaint.setColor(showParam.dateColor);
                }
                
                final int theDay = i + 1 - dayOffset;
                canvas.drawText(String.format(Locale.CHINA, "%d", theDay), x, y,
                        dayPaint);
                if (dateParam.indicationDays != null
                        && dateParam.indicationDays.indexOf(theDay) >= 0) {
                    canvas.drawCircle(x, y + (showParam.dateFontSize >> 1), dp2px(3),
                            dayIndPaint);
                }
            }
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawWeekTitle(canvas);
        drawDays(canvas);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                showParam.rowHeight * rowCount + showParam.titleHeight());
    }
    
    public static class DateParam {
        private int year;
        private int month;
        private int yearOfPrevMonth;
        private int prevMonth;
        private int selectDate;
        private int days;
        private int daysPrevMonth;
        /** 当月的第一天是周几 */
        private int dayOfWeekStart;
        // /** 当月的最后一天是周几 */
        // private int dayOfWeekEnd;
        private Calendar calendar;
        private ArrayList<Integer> indicationDays;// 需要提示显示的天
        
        private DateParam(Builder builder) {
            
            calendar = Calendar.getInstance();
            if (builder.year > 0) {
                calendar.set(Calendar.YEAR, builder.year);
            }
            if (builder.month >= 0) {
                calendar.set(Calendar.MONTH, builder.month);
            }
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            days = CalendarUtil.daysOfMonth(year, month);
            if (month == Calendar.JANUARY) {
                yearOfPrevMonth = year - 1;
                prevMonth = Calendar.DECEMBER;
            }
            else {
                yearOfPrevMonth = year;
                prevMonth = month - 1;
            }
            
            daysPrevMonth = CalendarUtil.daysOfMonth(yearOfPrevMonth, prevMonth);
            dayOfWeekStart = calendar.get(Calendar.DAY_OF_WEEK);
            // dayOfWeekEnd = calendar.get(Calendar.DAY_OF_WEEK);
            if (builder.selectDate > 0) {
                selectDate = builder.selectDate;
            }
            
            if (builder.indicationDays != null) {
                indicationDays = new ArrayList<>();
                indicationDays.addAll(builder.indicationDays);
            }
        }
        
        boolean isSelectDay(long year, long month, long day) {
            return year == this.year && month == this.month && day == selectDate;
        }
        
        public static class Builder {
            
            private int year = -1;
            private int month = -1;
            private int selectDate = -1;
            private ArrayList<Integer> indicationDays;// 需要提示显示的天
            
            public Builder() {}
            
            public Builder setYear(int year) {
                this.year = year;
                return this;
            }
            
            public Builder setMonth(int month) {
                this.month = month;
                return this;
            }
            
            public Builder setSelectDate(int date) {
                this.selectDate = date;
                return this;
            }
            
            public Builder addIndDay(int day) {
                if (indicationDays == null) {
                    indicationDays = new ArrayList<>();
                }
                indicationDays.add(day);
                return this;
            }
            
            public DateParam build() {
                return new DateParam(this);
            }
        }
    }
    
    public static class ShowParam {
        
        public enum BgShape {
            CIRCLE, ROUND_RECT, RECT
        }
        
        private int monthTitleHeight;
        private int rowHeight;
        private int showStartDayOfWeek;
        private int bgColor;
        
        private int titleColor;
        private int titleFontSize;
        
        private int weekColor;
        private int weekFontSize;
        
        private int dateColor;
        private int datePassedColor;
        private int dateTodayColor;
        private int dateFontSize;
        
        private int dateUnSelectColor;
        private int dataUnSelectFontSize;
        
        private int dateSelectColor;
        private int dateSelectBgColor;
        private BgShape dateSelectBgShape;
        private boolean showWeekTitle;
        private boolean showPreAndNextDays;
        
        private int dateIndColor;
        
        private ShowParam(Builder builder) {
            monthTitleHeight = builder.monthTitleHeight;
            rowHeight = builder.rowHeight;
            showStartDayOfWeek = builder.showStartDayOfWeek;
            bgColor = builder.bgColor;
            titleColor = builder.titleColor;
            titleFontSize = builder.titleFontSize;
            weekColor = builder.weekColor;
            weekFontSize = builder.weekFontSize;
            dateColor = builder.dateColor;
            dateTodayColor = builder.dateTodayColor;
            datePassedColor = builder.datePassedColor;
            dateFontSize = builder.dateFontSize;
            dateUnSelectColor = builder.dateUnSelectColor;
            dataUnSelectFontSize = builder.dataUnSelectFontSize;
            dateSelectColor = builder.dateSelectColor;
            dateSelectBgColor = builder.dateSelectBgColor;
            dateSelectBgShape = builder.dateSelectBgShape;
            showWeekTitle = builder.showWeekTitle;
            showPreAndNextDays = builder.showPreAndNextDays;
            dateIndColor = builder.dateIndColor;
        }
        
        public int titleHeight() {
            int height = monthTitleHeight();
            if (showWeekTitle) {
                height += weekFontSize;
            }
            
            return height;
        }
        
        public int monthTitleHeight() {
            return monthTitleHeight;
        }
        
        public static class Builder {
            
            private int monthTitleHeight;
            private int rowHeight;
            
            private int showStartDayOfWeek = Calendar.SUNDAY;
            private int bgColor = Color.TRANSPARENT;
            
            private int titleColor = Color.DKGRAY;
            private int titleFontSize;
            
            private int weekColor = Color.GRAY;
            private int weekFontSize;
            
            private int dateColor = Color.GRAY;
            private int datePassedColor = dateColor;
            private int dateTodayColor = Color.YELLOW;
            private int dateFontSize;
            
            private int dateUnSelectColor = Color.LTGRAY;
            private int dataUnSelectFontSize;
            
            private int dateSelectColor = Color.WHITE;
            private int dateSelectBgColor = Color.CYAN;
            private BgShape dateSelectBgShape = BgShape.CIRCLE;
            
            private int dateIndColor = Color.DKGRAY;
            
            private boolean showWeekTitle = true;
            private boolean showPreAndNextDays = true;
            
            public Builder() {}
            
            public Builder setMonthTitleHeight(int monthTitleHeight) {
                this.monthTitleHeight = monthTitleHeight;
                return this;
            }
            
            public Builder setRowHeight(int rowHeight) {
                this.rowHeight = rowHeight;
                return this;
            }
            
            public Builder setShowStartDayOfWeek(int showStartDayOfWeek) {
                this.showStartDayOfWeek = showStartDayOfWeek;
                return this;
            }
            
            public Builder setBgColor(int bgColor) {
                this.bgColor = bgColor;
                return this;
            }
            
            public Builder setTitleColor(int titleColor) {
                this.titleColor = titleColor;
                return this;
            }
            
            public Builder setTitleFontSize(int sizePx) {
                this.titleFontSize = sizePx;
                return this;
            }
            
            public Builder setWeekColor(int weekColor) {
                this.weekColor = weekColor;
                return this;
            }
            
            public Builder setWeekFontSize(int sizePx) {
                this.weekFontSize = sizePx;
                return this;
            }
            
            public Builder setDateColor(int dateColor) {
                this.dateColor = dateColor;
                return this;
            }
            
            public Builder setDateTodayColor(int dateColor) {
                this.dateTodayColor = dateColor;
                return this;
            }
            
            public Builder setDatePassedColor(int dateColor) {
                this.datePassedColor = dateColor;
                return this;
            }
            
            public Builder setDateFontSize(int sizePx) {
                this.dateFontSize = sizePx;
                return this;
            }
            
            public Builder setDateUnSelectColor(int dateColor) {
                this.dateUnSelectColor = dateColor;
                return this;
            }
            
            public Builder setDateUnSelectFontSize(int sizePx) {
                this.dataUnSelectFontSize = sizePx;
                return this;
            }
            
            public Builder setDateSelectColor(int dateSelectColor) {
                this.dateSelectColor = dateSelectColor;
                return this;
            }
            
            public Builder setDateSelectBgColor(int dateSelectBgColor) {
                this.dateSelectBgColor = dateSelectBgColor;
                return this;
            }
            
            public Builder setDateSelectBgShape(BgShape shape) {
                this.dateSelectBgShape = shape;
                return this;
            }
            
            public Builder setShowWeekTitle(boolean show) {
                this.showWeekTitle = show;
                return this;
            }
            
            public Builder setShowPrevAndNextDays(boolean show) {
                this.showPreAndNextDays = show;
                return this;
            }
            
            public Builder setDateIndicationColor(int color) {
                this.dateIndColor = color;
                return this;
            }
            
            public ShowParam build() {
                return new ShowParam(this);
            }
        }
    }
}
