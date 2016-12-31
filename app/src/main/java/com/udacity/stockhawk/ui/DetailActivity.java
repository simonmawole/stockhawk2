package com.udacity.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by Simon on 31-Dec-16.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.chart)
    LineChart lineChart;
    private String mSymbol;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mSymbol = getIntent().getStringExtra("symbol");

        //Set title to stock symbol
        if(mSymbol != null) setTitle(mSymbol);

    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS,
                Contract.Quote.COLUMN_SYMBOL + " = ? ",
                new String[]{mSymbol},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            List<Entry> entries = new ArrayList<>();

            String history = data.getString(Contract.Quote.POSITION_HISTORY);
            history = history.trim();
            Timber.d("DATA="+history);
            String[] splitHistory = history.split("\n");
            Timber.d("SPLIT="+splitHistory.length);

            for (int i = 0; i < splitHistory.length; i++) {
                String item = splitHistory[i];
                String[] itemSplit = item.split(",");
                Timber.d(getSimpleDateTime(Long.valueOf(itemSplit[0]))+"="+Float.parseFloat(itemSplit[1]));
                //turn your data into Entry objects

                float x = 0F;
                float y = 0F;
                try {
                    x = Float.parseFloat(itemSplit[1]);
                    y = getSimpleDateTime(Long.valueOf(itemSplit[0]));
                }catch (Exception e){
                    e.printStackTrace();
                }

                entries.add(new Entry( y, x));
            }

            Collections.sort(entries, new EntryXComparator());

            LineDataSet dataSet = new LineDataSet(entries, "Stock price"); // add entries to dataset
            dataSet.setColor(getResources().getColor(R.color.colorPrimary));
            dataSet.setValueTextColor(getResources().getColor(R.color.colorAccent)); // styling

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.invalidate(); // refresh

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Convert date from milliseconds to d MMM yyyy hh:mm a
     *
     * @param millisec
     * */
    public static Float getSimpleDateTime(long millisec){
        String formatedDate = "";
        if(millisec!= 0) {
            SimpleDateFormat userFormat = new SimpleDateFormat("ddMMyy");
            formatedDate = userFormat.format(millisec);
        }

        return Float.parseFloat(formatedDate);
    }

}
