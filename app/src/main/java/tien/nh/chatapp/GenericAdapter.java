package tien.nh.chatapp;


import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

public abstract class GenericAdapter<T, V extends GenericAdapter.ViewHolder> extends BaseAdapter {
    private Context context;
    private ArrayList<T> dataList;

    public GenericAdapter(Context context, ArrayList<T> dataList) {
        this.context = context;
        this.dataList = dataList;

    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public T getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        V viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(getLayoutRes(), parent, false);

            viewHolder = createViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (V) convertView.getTag();
        }

        T data = dataList.get(position);
        bindData(viewHolder, data);

        return convertView;
    }

    protected abstract int getLayoutRes();
    protected abstract V createViewHolder(View convertView);
    protected abstract void bindData(V viewHolder, T data);

    protected static class ViewHolder {
        // Tham chiếu đến các view trong item layout
    }

}

