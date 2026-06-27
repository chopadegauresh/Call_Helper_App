package com.codewithgauresh.call_helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithgauresh.call_helper.database.CallSchedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CallScheduleAdapter extends RecyclerView.Adapter<CallScheduleAdapter.ViewHolder> implements Filterable {
    private List<CallSchedule> schedules = new ArrayList<>();
    private List<CallSchedule> schedulesFull = new ArrayList<>();

    public interface OnItemClickListener {
        void onEditClick(CallSchedule schedule);
        void onDeleteClick(CallSchedule schedule);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSchedules(List<CallSchedule> schedules) {
        this.schedules = schedules;
        this.schedulesFull = new ArrayList<>(schedules);
        notifyDataSetChanged();
    }

    public CallSchedule getScheduleAt(int position) {
        return schedules.get(position);
    }

    @Override
    public Filter getFilter() {
        return scheduleFilter;
    }

    private final Filter scheduleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CallSchedule> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(schedulesFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (CallSchedule item : schedulesFull) {
                    if (item.getContactName().toLowerCase().contains(filterPattern) ||
                        item.getPhoneNumber().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            schedules.clear();
            if (results.values != null) {
                schedules.addAll((List<CallSchedule>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallSchedule schedule = schedules.get(position);
        holder.tvName.setText(schedule.getContactName());
        holder.tvNumber.setText(schedule.getPhoneNumber());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvTime.setText(sdf.format(schedule.getScheduledTime()));

        if (schedule.getNotes() != null && !schedule.getNotes().isEmpty()) {
            holder.tvNotes.setText(schedule.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        holder.ivRecurring.setVisibility(schedule.isRecurring() ? View.VISIBLE : View.GONE);

        // Status Indicator and Logic
        if (schedule.isCompleted()) {
            // History items
            String statusText = String.format(Locale.getDefault(), "History: %s", schedule.getStatus());
            if (schedule.getSnoozeCount() > 0) {
                statusText += String.format(Locale.getDefault(), " (Snoozed %d times)", schedule.getSnoozeCount());
            }
            holder.tvStatus.setText(statusText);
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setTextColor(android.graphics.Color.YELLOW);
            holder.btnEdit.setVisibility(View.GONE);
            holder.statusIndicator.setBackgroundColor(android.graphics.Color.RED);
        } else {
            // Main Screen Logic
            if (schedule.getStatus() != null && (schedule.getStatus().contains("Finished") || schedule.getStatus().contains("Cancelled"))) {
                // Completed but kept on main screen
                holder.tvStatus.setText(schedule.getStatus());
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setTextColor(android.graphics.Color.LTGRAY);
                holder.statusIndicator.setBackgroundColor(android.graphics.Color.RED);
            } else if ("Snoozed".equals(schedule.getStatus())) {
                // Snoozed - Show as Green (Active)
                String snoozedText = String.format(Locale.getDefault(), "Snoozed: %s", sdf.format(schedule.getScheduledTime()));
                holder.tvStatus.setText(snoozedText);
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setTextColor(android.graphics.Color.GREEN);
                holder.statusIndicator.setBackgroundColor(android.graphics.Color.GREEN);
            } else {
                // Pending/Upcoming
                holder.tvStatus.setVisibility(View.GONE);
                holder.statusIndicator.setBackgroundColor(android.graphics.Color.GREEN);
            }
            holder.btnEdit.setVisibility(View.VISIBLE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(schedule);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(schedule);
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber, tvTime, tvNotes, tvStatus;
        View btnEdit, btnDelete, statusIndicator;
        View ivRecurring;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvNotes = itemView.findViewById(R.id.tvItemNotes);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            ivRecurring = itemView.findViewById(R.id.ivRecurring);
        }
    }
}
