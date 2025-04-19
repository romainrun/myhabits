package com.rrtech.myhabits.adapter;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.data.model.Routine;

import java.util.List;

public class SpinnerRoutineAdapter extends ArrayAdapter<Routine> {

    private final LayoutInflater inflater;

    public SpinnerRoutineAdapter(@NonNull Context context, @NonNull List<Routine> routines) {
        super(context, R.layout.spinner_dropdown_item, routines);
        inflater = LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        TextView text = convertView.findViewById(R.id.text_item);
        Routine routine = getItem(position);

        if (routine != null) {
            text.setText(routine.getName());

            if (routine.isPlaceholder()) {
                text.setTextColor(Color.GRAY);
            } else {
                text.setTextColor(Color.BLACK);
            }
        }

        return convertView;
    }


    private View createCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        Routine routine = getItem(position);
        TextView textView = convertView.findViewById(R.id.text_item);

        if (routine != null) {
            textView.setText(routine.getName());
        }

        return convertView;
    }
}
