package com.mojoteahouse.mojotea.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.data.Topping;

import java.util.ArrayList;
import java.util.List;

public class ToppingItemAdapter extends RecyclerView.Adapter<ToppingItemAdapter.ToppingViewHolder> {

    private LayoutInflater layoutInflater;
    private List<Topping> toppingList;
    private ToppingItemClickListener toppingItemClickListener;
    private ArrayList<String> selectedToppingList;

    public ToppingItemAdapter(Context context, List<Topping> toppingList, ToppingItemClickListener toppingItemClickListener) {
        layoutInflater = LayoutInflater.from(context);
        this.toppingList = toppingList;
        this.toppingItemClickListener = toppingItemClickListener;
        selectedToppingList = new ArrayList<>();
    }

    @Override
    public ToppingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.topping_list_item, parent, false);
        return new ToppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ToppingViewHolder holder, final int position) {
        Topping topping = toppingList.get(position);
        holder.checkedTextView.setText(topping.getName());
        holder.checkedTextView.setTag(topping);
        holder.checkedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkedTextView.setChecked(!holder.checkedTextView.isChecked());
                Topping currentTopping = (Topping) holder.checkedTextView.getTag();
                if (holder.checkedTextView.isChecked()) {
                    selectedToppingList.add(currentTopping.getName());
                    toppingItemClickListener.onToppingItemClicked(currentTopping.getPrice());
                } else {
                    selectedToppingList.remove(currentTopping.getName());
                    toppingItemClickListener.onToppingItemClicked(-currentTopping.getPrice());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return toppingList == null
                ? 0
                : toppingList.size();
    }

    public ArrayList<String> getSelectedToppingList() {
        return selectedToppingList;
    }

    public void updateToppingList(List<Topping> toppingList) {
        if (toppingList != null) {
            selectedToppingList.clear();
            this.toppingList.clear();
            this.toppingList.addAll(toppingList);
            notifyDataSetChanged();
        }
    }


    public interface ToppingItemClickListener {

        void onToppingItemClicked(double toppingPrice);
    }

    protected class ToppingViewHolder extends RecyclerView.ViewHolder {

        private CheckedTextView checkedTextView;

        public ToppingViewHolder(View itemView) {
            super(itemView);

            checkedTextView = (CheckedTextView) itemView.findViewById(R.id.checked_text_view);
        }
    }
}
