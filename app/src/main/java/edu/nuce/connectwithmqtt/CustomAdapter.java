package edu.nuce.connectwithmqtt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CustomAdapter extends ArrayAdapter {
    private Context context;
    private int resource;
    private List<String> listMessage;
    public CustomAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.listMessage = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvItem =convertView.findViewById(R.id.tvItem);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder =(ViewHolder) convertView.getTag();
        }
        String item = listMessage.get(position);
        viewHolder.tvItem.setText(item);
        return convertView;
    }
    public class ViewHolder{
        TextView tvItem;
    }
}
