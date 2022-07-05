/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jeudi.kiwi.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import jeudi.kiwi.R;

/**
 * Provide views to RecyclerView with the directory entries.
 */
public class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> {

    private List<CustomStatItem> mCustomUsageStatsList = new ArrayList<>();
    private Context context;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView appNameTextView;
        private final RelativeLayout mLayout;
        private final TextView mTimeUsedTextView;
        private final TextView breakInTimeTextView;
        private final ImageView mAppIconImgView;


        ViewHolder(View v) {
            super(v);
            appNameTextView = v.findViewById(R.id.textview_package_name);
            mTimeUsedTextView = v.findViewById(R.id.textview_time_used);
            breakInTimeTextView = v.findViewById(R.id.period_text);
            mAppIconImgView = v.findViewById(R.id.app_icon);
            mLayout = v.findViewById(R.id.ussage_row_layout);
        }


        public TextView getBreakInTimeTextView() {
            return breakInTimeTextView;
        }

        TextView getTimeUsedTextView() {
            return mTimeUsedTextView;
        }

        TextView getAppNameTextView() {
            return appNameTextView;
        }

        public RelativeLayout getLayout() {
            return mLayout;
        }

        ImageView getAppIconImageView() {
            return mAppIconImgView;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_usage, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        try {
            CustomStatItem statItem = mCustomUsageStatsList.get(position);
            if (statItem.breakInTime != null) {
                viewHolder.getAppIconImageView().setVisibility(View.GONE);
                viewHolder.getLayout().setBackgroundColor(Color.WHITE);
                viewHolder.getTimeUsedTextView().setVisibility(View.GONE);
                viewHolder.getAppNameTextView().setVisibility(View.GONE);
                viewHolder.getBreakInTimeTextView().setText(statItem.breakInTime);
                viewHolder.getBreakInTimeTextView().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getBreakInTimeTextView().setVisibility(View.GONE);
                viewHolder.getTimeUsedTextView().setVisibility(View.VISIBLE);
                viewHolder.getAppNameTextView().setVisibility(View.VISIBLE);
                viewHolder.getLayout().setBackgroundColor(Color.DKGRAY);
                viewHolder.getAppIconImageView().setVisibility(View.VISIBLE);
                viewHolder.getAppNameTextView().setText(statItem.appName);
                putIconInto(viewHolder.getAppIconImageView(), statItem.appIcon);
                String text = String.format(Locale.getDefault(), "%02dh%02d",
                        TimeUnit.MILLISECONDS.toHours(statItem.usageStats.getTotalTimeInForeground()),
                        TimeUnit.MILLISECONDS.toMinutes(statItem.usageStats.getTotalTimeInForeground()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(statItem.usageStats.getTotalTimeInForeground())));
                viewHolder.getTimeUsedTextView().setText(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void putIconInto(ImageView img, File f) {
        try {
            Picasso.get().load(f).into(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mCustomUsageStatsList.size();
    }

    public void setCustomUsageStatsList(List<CustomStatItem> customUsageStats) {
        mCustomUsageStatsList = customUsageStats;
    }
}