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
    private List<String> selectedToppingList;
    private String toppingFormat;

    public ToppingItemAdapter(Context context, List<Topping> toppingList, ToppingItemClickListener toppingItemClickListener) {
        layoutInflater = LayoutInflater.from(context);
        this.toppingList = toppingList;
        this.toppingItemClickListener = toppingItemClickListener;
        selectedToppingList = new ArrayList<>();
        toppingFormat = context.getString(R.string.topping_format);
    }

    @Override
    public ToppingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.topping_list_item, parent, false);
        return new ToppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ToppingViewHolder holder, final int position) {
        Topping topping = toppingList.get(position);
        holder.checkedTextView.setText(String.format(toppingFormat, topping.getName(), topping.getPrice()));
        holder.checkedTextView.setTag(topping);
        holder.checkedTextView.setChecked(selectedToppingList.contains(topping.getName()));
    }

    @Override
    public int getItemCount() {
        return toppingList == null
                ? 0
                : toppingList.size();
    }

    public List<String> getSelectedToppingList() {
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

    public void updateWithSelectedToppingList(List<Topping> toppingList, List<String> selectedToppingList) {
        if (toppingList != null) {
            if (selectedToppingList != null) {
                this.selectedToppingList.clear();
                this.selectedToppingList.addAll(selectedToppingList);
            }
            this.toppingList.clear();
            this.toppingList.addAll(toppingList);
            notifyDataSetChanged();
        }
    }


    public interface ToppingItemClickListener {

        void onToppingItemClicked(double toppingPrice);
    }

    protected class ToppingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CheckedTextView checkedTextView;

        public ToppingViewHolder(View itemView) {
            super(itemView);

            checkedTextView = (CheckedTextView) itemView.findViewById(R.id.checked_text_view);
            checkedTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            checkedTextView.toggle();
            Topping currentTopping = (Topping) checkedTextView.getTag();
            if (checkedTextView.isChecked()) {
                selectedToppingList.add(currentTopping.getName());
                toppingItemClickListener.onToppingItemClicked(currentTopping.getPrice());
            } else {
                selectedToppingList.remove(currentTopping.getName());
                toppingItemClickListener.onToppingItemClicked(-currentTopping.getPrice());
            }
        }
    }
}
