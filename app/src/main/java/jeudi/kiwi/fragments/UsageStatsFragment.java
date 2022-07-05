/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package jeudi.kiwi.fragments;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jeudi.kiwi.Divers.AppsListManager;
import jeudi.kiwi.adapters.CustomAppItem;
import jeudi.kiwi.adapters.CustomStatItem;
import jeudi.kiwi.R;
import jeudi.kiwi.adapters.UsageListAdapter;

/**
 * Fragment that demonstrates how to use App Usage Statistics API.
 */
public class UsageStatsFragment extends Fragment {

    private static final String LOGTAG = "UsageStatsFragment";

    //VisibleForTesting for variables below
    UsageStatsManager mUsageStatsManager;
    UsageListAdapter mUsageListAdapter;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    TextView mUsageAccessMissing;
    Spinner mSpinner;
    AppsListManager appsListManager;
    DateFormat nDateFormat;

    Context context;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link UsageStatsFragment}.
     */
    public static UsageStatsFragment newInstance() {
        return new UsageStatsFragment();
    }

    public UsageStatsFragment() {
        // Required empty public constructor
        nDateFormat = DateFormat.getDateInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        appsListManager = new AppsListManager(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mUsageStatsManager = (UsageStatsManager) getActivity()
                    .getSystemService(Context.USAGE_STATS_SERVICE); //Context.USAGE_STATS_SERVICE
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_usage_statistics, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        mUsageListAdapter = new UsageListAdapter();
        mRecyclerView = rootView.findViewById(R.id.recyclerview_app_usage);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mUsageListAdapter);
        mUsageAccessMissing = rootView.findViewById(R.id.text_usage_missing);
        mSpinner = rootView.findViewById(R.id.spinner_time_span);
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.action_list, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {



            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            actualize(position);
                        }
                    }).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void actualize(int position){
        String[] strings = getResources().getStringArray(R.array.action_list);
        StatsUsageInterval statsUsageInterval = StatsUsageInterval
                .getValue(strings[position]);
        if (statsUsageInterval != null) {
            List<UsageStats> usageStatsList =
                    getUsageStatistics(statsUsageInterval.mInterval);
            Collections.sort(usageStatsList, new LastTimeLaunchedComparatorDesc());

            updateAppsList(usageStatsList);
        }
    }

    /**
     * Returns the {@link #mRecyclerView} including the time span specified by the
     * intervalType argument.
     *
     * @param intervalType The time interval by which the stats are aggregated.
     *                     Corresponding to the value of {@link UsageStatsManager}.
     *                     E.g. {@link UsageStatsManager#INTERVAL_DAILY}, {@link
     *                     UsageStatsManager#INTERVAL_WEEKLY},
     *
     * @return A list of {@link UsageStats}.
     */
    public List<UsageStats> getUsageStatistics(int intervalType) {
        // Get the app statistics since one year ago from the current time.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        List<UsageStats> queryUsageStats = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            queryUsageStats = mUsageStatsManager
                    .queryUsageStats(intervalType, cal.getTimeInMillis(),
                            System.currentTimeMillis());
        }

        if (queryUsageStats==null||queryUsageStats.size() == 0) {
            Log.i(LOGTAG, "The user may not allow the access to apps usage. ");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getActivity(),
                      //      getString(R.string.explanation_access_to_appusage_is_not_enabled),
                      //      Toast.LENGTH_LONG).show();
                    mUsageAccessMissing.setVisibility(View.VISIBLE);
                    mUsageAccessMissing.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            actualize(0);
                        }
                    });
                }
            });

        }
        return queryUsageStats;
    }

    /**
     * Updates the {@link #mRecyclerView} with the list of {@link UsageStats} passed as an argument.
     *
     * @param usageStatsList A list of {@link UsageStats} from which update the
     *                       {@link #mRecyclerView}.
     */
    //VisibleForTesting
    void updateAppsList(List<UsageStats> usageStatsList) {
        List<CustomStatItem> customUsageStatsList = new ArrayList<>();
        long lastTimeStamp=-1;
        for (int i = 0; i < usageStatsList.size(); i++) {
            UsageStats usageStats = usageStatsList.get(i);
            if(usageStats.getTotalTimeInForeground()<=2000)continue;
            if(lastTimeStamp != usageStats.getFirstTimeStamp()){
                customUsageStatsList.add(createSeparator(usageStats));
                lastTimeStamp = usageStatsList.get(i).getFirstTimeStamp();
            }
            CustomStatItem customUsageStats = new CustomStatItem();
            customUsageStats.usageStats = usageStats;
            customUsageStats.appIcon = appsListManager.getIcon(usageStats.getPackageName());
            customUsageStats.appName = appsListManager.getApplicationName(usageStats.getPackageName());
            customUsageStatsList.add(customUsageStats);
        }
        mUsageListAdapter.setCustomUsageStatsList(customUsageStatsList);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUsageListAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(0);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        actualize(mSpinner.getSelectedItemPosition());
    }

    public CustomStatItem createSeparator(UsageStats usageStats){
        CustomStatItem customUsageStatsSep = new CustomStatItem();
        customUsageStatsSep.breakInTime = "around the "+nDateFormat.format(new Date((usageStats.getFirstTimeStamp()+usageStats.getLastTimeStamp())/2));
        return customUsageStatsSep;
    }

    /**
     * The {@link Comparator} to sort a collection of {@link UsageStats} sorted by the timestamp
     * last time the app was used in the descendant order.
     */
    private static class LastTimeLaunchedComparatorDesc implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats left, UsageStats right) {
            if(right.getFirstTimeStamp()==left.getFirstTimeStamp())return Long.compare(right.getTotalTimeInForeground(), left.getTotalTimeInForeground());
            return Long.compare(right.getFirstTimeStamp(), left.getFirstTimeStamp());
        }
    }

    /**
     * Enum represents the intervals for {@link UsageStatsManager} so that
     * values for intervals can be found by a String representation.
     *
     */
    //VisibleForTesting
    enum StatsUsageInterval {
        DAILY("Daily", UsageStatsManager.INTERVAL_DAILY),
        WEEKLY("Weekly", UsageStatsManager.INTERVAL_WEEKLY),
        MONTHLY("Monthly", UsageStatsManager.INTERVAL_MONTHLY),
        YEARLY("Yearly", UsageStatsManager.INTERVAL_YEARLY);

        private int mInterval;
        private String mStringRepresentation;

        StatsUsageInterval(String stringRepresentation, int interval) {
            mStringRepresentation = stringRepresentation;
            mInterval = interval;
        }

        static StatsUsageInterval getValue(String stringRepresentation) {
            for (StatsUsageInterval statsUsageInterval : values()) {
                if (statsUsageInterval.mStringRepresentation.equals(stringRepresentation)) {
                    return statsUsageInterval;
                }
            }
            return null;
        }
    }
}
