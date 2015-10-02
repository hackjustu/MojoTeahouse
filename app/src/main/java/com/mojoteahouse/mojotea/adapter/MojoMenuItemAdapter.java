package com.mojoteahouse.mojotea.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.data.MojoMenu;

import java.util.List;

public class MojoMenuItemAdapter extends RecyclerView.Adapter<MojoMenuItemAdapter.MojoMenuViewHolder> {

    private LayoutInflater layoutInflater;
    private List<MojoMenu> mojoMenuList;
    private MojoMenuItemClickListener itemClickListener;

    public MojoMenuItemAdapter(Context context, List<MojoMenu> mojoMenuList, MojoMenuItemClickListener itemClickListener) {
        layoutInflater = LayoutInflater.from(context);
        this.mojoMenuList = mojoMenuList;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public MojoMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.mojo_menu_list_item, parent, false);
        return new MojoMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MojoMenuViewHolder holder, int position) {
        MojoMenu mojoMenu = mojoMenuList.get(position);

        holder.nameText.setText(mojoMenu.getName());
        holder.priceText.setText("$" + mojoMenu.getPrice());
    }

    @Override
    public int getItemCount() {
        return mojoMenuList == null
                ? 0
                : mojoMenuList.size();
    }

    public void updateMojoMenuList(List<MojoMenu> mojoMenuList) {
        if (mojoMenuList != null) {
            this.mojoMenuList.clear();
            this.mojoMenuList.addAll(mojoMenuList);
            notifyDataSetChanged();
        }
    }


    public interface MojoMenuItemClickListener {

        void onItemClicked(MojoMenu mojoMenu);
    }

    protected class MojoMenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private TextView nameText;
        private TextView priceText;

        public MojoMenuViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            nameText = (TextView) itemView.findViewById(R.id.name_text);
            priceText = (TextView) itemView.findViewById(R.id.price_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            MojoMenu mojoMenu = mojoMenuList.get(getLayoutPosition());
            itemClickListener.onItemClicked(mojoMenu);
        }
    }
}
