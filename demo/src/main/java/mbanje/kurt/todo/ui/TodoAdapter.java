package mbanje.kurt.todo.ui;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import mbanje.kurt.todo.R;
import mbanje.kurt.todo.Todo;
import mbanje.kurt.todo.provider.TodoHelper;

/**
 * Created by kurt on 2014/07/18.
 */
public class TodoAdapter extends BaseAdapter {

    private final List<Todo> items;
    private final Context context;
    private final LayoutInflater inflater;
    private SparseBooleanArray selections = new SparseBooleanArray();

    public TodoAdapter(Context context, List<Todo> items) {
        this.items = items;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * for keeping track of the currently selected items when we're in multi select mode
     *
     * @param position
     * @param add
     */
    public void updateSelections(int position, boolean add) {
        if (add) {
            selections.put(position, true);
        } else {
            selections.put(position, false);
        }
        notifyDataSetChanged();
    }

    public void clearSelections() {
        selections.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Todo getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.todo_item, parent, false);
            holder.label = (TextView) view.findViewById(R.id.item_label);
            holder.description = (TextView) view.findViewById(R.id.item_description);
            holder.completed = (CheckBox) view.findViewById(R.id.item_completed);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Todo item = items.get(position);
        holder.label.setText(item.label);
        holder.description.setText(item.description);
        holder.completed.setOnCheckedChangeListener(null);
        holder.completed.setChecked(item.completed);
        holder.completed.setOnCheckedChangeListener(new TodoCheckListener(item));

        if (selections.get(position)) {
            view.setBackgroundColor(context.getResources().getColor(R.color.list_item_selected));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.list_item_default));
        }
        return view;
    }

    static class ViewHolder {
        public TextView label;
        public TextView description;
        public CheckBox completed;
    }


    /**
     * Created by kurt on 2014/07/21.
     */
    private class TodoCheckListener implements CompoundButton.OnCheckedChangeListener {

        private final Todo item;

        private TodoCheckListener(Todo item) {
            this.item = item;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            item.completed = isChecked;
            TodoHelper.updateTodo(context.getContentResolver(), item);
        }
    }

}
