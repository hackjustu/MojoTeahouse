package com.mojoteahouse.mojotea.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.data.MojoImage;
import com.mojoteahouse.mojotea.data.MojoMenu;
import com.mojoteahouse.mojotea.data.Topping;
import com.mojoteahouse.mojotea.util.DataUtils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MojoMenuItemAdapter extends RecyclerView.Adapter<MojoMenuItemAdapter.MojoMenuViewHolder> {

    private LayoutInflater layoutInflater;
    private List<MojoMenu> mojoMenuList;
    private MojoMenuItemClickListener itemClickListener;
    private String priceFormat;
    private String toppingFormat;

    public MojoMenuItemAdapter(Context context,
                               List<MojoMenu> mojoMenuList,
                               MojoMenuItemClickListener itemClickListener) {
        layoutInflater = LayoutInflater.from(context);
        Collections.sort(mojoMenuList, new MenuComparator());
        this.mojoMenuList = mojoMenuList;
        this.itemClickListener = itemClickListener;
        priceFormat = context.getString(R.string.mojo_menu_price_text);
        toppingFormat = context.getString(R.string.mojo_menu_topping_text);
    }

    @Override
    public MojoMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View menuView = layoutInflater.inflate(R.layout.mojo_menu_list_item, parent, false);
        return new MojoMenuViewHolder(menuView);
    }

    @Override
    public void onBindViewHolder(final MojoMenuViewHolder holder, int position) {
        MojoMenu mojoMenu = mojoMenuList.get(position);

        ParseQuery<MojoImage> mojoImageQuery = MojoImage.getQuery();
        mojoImageQuery.fromLocalDatastore();
        mojoImageQuery.whereEqualTo(MojoImage.IMAGE_ID, mojoMenu.getImageId());
        mojoImageQuery.getFirstInBackground(new GetCallback<MojoImage>() {
            @Override
            public void done(MojoImage mojoImage, ParseException e) {
                if (e == null) {
                    holder.mojoMenuImageView.setParseFile(mojoImage.getImage());
                    holder.mojoMenuImageView.loadInBackground();
                }
            }
        });

        final List<Integer> availableToppingIdList = mojoMenu.getToppingIdList();
        if (availableToppingIdList.isEmpty()) {
            holder.toppingText.setVisibility(View.GONE);
        } else {
            ParseQuery<Topping> toppingQuery = Topping.getQuery();
            toppingQuery.fromLocalDatastore();
            toppingQuery.findInBackground(new FindCallback<Topping>() {
                @Override
                public void done(List<Topping> toppings, ParseException e) {
                    if (e != null) {
                        holder.toppingText.setVisibility(View.GONE);
                    } else {
                        List<String> toppingString = new ArrayList<>();
                        for (Topping topping : toppings) {
                            if (availableToppingIdList.contains(topping.getToppingId())) {
                                toppingString.add(topping.getName());
                            }
                        }
                        if (toppingString.isEmpty()) {
                            holder.toppingText.setVisibility(View.GONE);
                        } else {
                            Collections.sort(toppingString);
                            holder.toppingText.setText(String.format(toppingFormat,
                                    DataUtils.getToppingListString(toppingString)));
                        }
                    }
                }
            });
        }

        holder.nameText.setText(mojoMenu.getName());
        holder.priceText.setText(String.format(priceFormat, mojoMenu.getPrice()));
        if (mojoMenu.isSoldOut()) {
            holder.soldOutImageView.setVisibility(View.VISIBLE);
        } else if (mojoMenu.getPriority() > MojoMenu.DEFAULT_PRIORITY) {
            holder.newProductImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mojoMenuList == null
                ? 0
                : mojoMenuList.size();
    }

    public void updateMojoMenuList(List<MojoMenu> mojoMenuList) {
        if (mojoMenuList != null) {
            Collections.sort(mojoMenuList, new MenuComparator());
            this.mojoMenuList.clear();
            this.mojoMenuList.addAll(mojoMenuList);
            notifyDataSetChanged();
        }
    }


    public interface MojoMenuItemClickListener {

        void onItemClicked(MojoMenu mojoMenu);
    }

    protected class MojoMenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ParseImageView mojoMenuImageView;
        private ImageView newProductImageView;
        private ImageView soldOutImageView;
        private TextView nameText;
        private TextView priceText;
        private TextView toppingText;

        public MojoMenuViewHolder(View itemView) {
            super(itemView);

            mojoMenuImageView = (ParseImageView) itemView.findViewById(R.id.mojo_menu_image_view);
            newProductImageView = (ImageView) itemView.findViewById(R.id.new_mojo_item_image);
            soldOutImageView = (ImageView) itemView.findViewById(R.id.sold_out_image);
            nameText = (TextView) itemView.findViewById(R.id.name_text);
            priceText = (TextView) itemView.findViewById(R.id.price_text);
            toppingText = (TextView) itemView.findViewById(R.id.topping_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            MojoMenu mojoMenu = mojoMenuList.get(getLayoutPosition());
            itemClickListener.onItemClicked(mojoMenu);
        }
    }

    private class MenuComparator implements Comparator<MojoMenu> {

        @Override
        public int compare(MojoMenu firstMenu, MojoMenu secondMenu) {
            int firstPriority = firstMenu.getPriority();
            int secondPriority = secondMenu.getPriority();
            if (firstPriority > secondPriority) {
                return -1;
            } else if (firstPriority < secondPriority) {
                return 1;
            } else {
                return firstMenu.getName().compareTo(secondMenu.getName());
            }
        }
    }
}
