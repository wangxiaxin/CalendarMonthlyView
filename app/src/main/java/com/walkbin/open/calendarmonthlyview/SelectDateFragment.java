package com.walkbin.open.calendarmonthlyview;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.walkbin.open.library.CalendarMonthView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SelectDateFragment extends Fragment {
    
    static final int DEFAULT_MONTH_COUNT = 24;

    final int COLOR_GRAY = Color.parseColor("#cccccc");
    final int COLOR_ORANGE = Color.parseColor("#ff7700");
    final int COLOR_GRAY_DEEP = Color.parseColor("#666666");


    private CalendarParam selectDayParam;
    private CalendarAdapter calendarAdapter;
    private ArrayList<CalendarParam> indDayParamList;

    private CalendarMonthView.DaySelectCallback daySelectCallback = new CalendarMonthView.DaySelectCallback() {
        @Override
        public void onDaySelected(int year, int month, int day) {
            selectDayParam.year = year;
            selectDayParam.month = month;
            selectDayParam.day = day;
            calendarAdapter.notifyDataSetChanged();
            final Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, selectDayParam.year);
            calendar.set(Calendar.MONTH, selectDayParam.month);
            calendar.set(Calendar.DAY_OF_MONTH, selectDayParam.day);
            Toast.makeText(getContext(),calendar.toString(),Toast.LENGTH_SHORT
            ).show();
        }
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_course_time, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Calendar todayCalendar = Calendar.getInstance();
        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = savedInstanceState;
        }
        
        ListView calendarListView = (ListView) view.findViewById(R.id.lv_calendar);
        ArrayList<CalendarParam> calendarParams = new ArrayList<>();
        int count = DEFAULT_MONTH_COUNT;
        final long startTime = System.currentTimeMillis() - (30*24*3600*1000L);
        final long selectDate = System.currentTimeMillis() + (7*24*3600*1000L);
        todayCalendar.setTimeInMillis(startTime);
        selectDayParam = new CalendarParam(todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                todayCalendar.get(Calendar.DAY_OF_MONTH));
        
        final int startYear = todayCalendar.get(Calendar.YEAR);
        final int startMonth = todayCalendar.get(Calendar.MONTH);
        
        // 设置选中日期
        if (selectDate > 0) {
            todayCalendar.setTimeInMillis(selectDate);
            selectDayParam.year = todayCalendar.get(Calendar.YEAR);
            selectDayParam.month = todayCalendar.get(Calendar.MONTH);
            selectDayParam.day = todayCalendar.get(Calendar.DAY_OF_MONTH);
        }
        
        // 组装数据
        int selectPos = -1;
        CalendarParam startCalendarParam = new CalendarParam(startYear, startMonth);
        calendarParams.add(startCalendarParam);
        if (selectDayParam.year == startCalendarParam.year
                && selectDayParam.month == startCalendarParam.month) {
            selectPos = 0;
        }
        
        while (count > 1) {
            startCalendarParam = startCalendarParam.nextMonth();
            calendarParams.add(startCalendarParam);
            if (selectDayParam.year == startCalendarParam.year
                    && selectDayParam.month == startCalendarParam.month) {
                selectPos = calendarParams.indexOf(startCalendarParam);
            }
            --count;
        }
        
        long[] indDateList = null;//bundle.getLongArray(PARAM_LONG_ARRAY_IND_DATE);
        // for test
        // if(indDateList == null){
        // indDateList = new
        // long[]{1478079975000L,1483117261000L,1488214861000L,1495904461000L};
        // }
        
        if (indDateList != null) {
            indDayParamList = new ArrayList<>();
            Calendar tmpCalendar = Calendar.getInstance();
            for (long indDate : indDateList) {
                tmpCalendar.setTimeInMillis(indDate);
                indDayParamList.add(new CalendarParam(tmpCalendar.get(Calendar.YEAR),
                        tmpCalendar.get(Calendar.MONTH), tmpCalendar
                                .get(Calendar.DAY_OF_MONTH)));
            }
        }
        
        calendarAdapter = new CalendarAdapter(getActivity(), calendarParams);
        calendarListView.setAdapter(calendarAdapter);
        if (selectPos >= 0) {
            calendarListView.setSelection(selectPos);
        }
    }
    
    private class CalendarAdapter extends BaseAdapter<CalendarParam> {
        
        CalendarAdapter(Context context, List<CalendarParam> list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return new CalendarMonthView(context);
        }
        
        @Override
        public ViewHolder<CalendarParam> createViewHolder() {
            return new CalendarViewHolder();
        }
    }
    
    private class CalendarViewHolder extends BaseAdapter.ViewHolder<CalendarParam> {
        
        CalendarMonthView view;
        
        @Override
        public void init(Context context, View convertView) {
            view = (CalendarMonthView) convertView;

            // 这里设置显示样式
            CalendarMonthView.ShowParam showParam = new CalendarMonthView.ShowParam.Builder()
                    .setShowWeekTitle(false)
                    .setShowPrevAndNextDays(false)
                    .setDateColor(COLOR_GRAY_DEEP)
                    .setTitleColor(COLOR_ORANGE)
                    .setDateSelectBgColor(COLOR_ORANGE)
                    .setDateTodayColor(COLOR_ORANGE)
                    .setDateIndicationColor(COLOR_ORANGE)
                    .setDatePassedColor(COLOR_GRAY).build();
            view.setShowParam(showParam).setOnDaySelectCallback(daySelectCallback);
        }
        
        @Override
        public void update(Context context, CalendarParam data) {
            CalendarMonthView.DateParam.Builder dateParamBuilder = new CalendarMonthView.DateParam.Builder()
                    .setYear(data.year).setMonth(data.month);
            
            if (data.year != selectDayParam.year || data.month != selectDayParam.month) {
                dateParamBuilder.setSelectDate(0);
            }
            else {
                dateParamBuilder.setSelectDate(selectDayParam.day);
            }
            
            if (indDayParamList != null) {
                for (CalendarParam indParam : indDayParamList) {
                    if (indParam.year == data.year && indParam.month == data.month) {
                        dateParamBuilder.addIndDay(indParam.day);
                    }
                }
            }
            view.setDateParam(dateParamBuilder.build());
        }
    }
}
