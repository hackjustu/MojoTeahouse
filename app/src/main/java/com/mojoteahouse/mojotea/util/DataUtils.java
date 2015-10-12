package com.mojoteahouse.mojotea.util;

import android.content.Context;
import android.provider.Settings;

import com.mojoteahouse.mojotea.data.MojoMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataUtils {

    public static List<String> getMojoMenuCategoryList(List<MojoMenu> mojoMenuList) {
        List<String> categoryList = new ArrayList<>();
        if (mojoMenuList != null) {
            for (MojoMenu mojoMenu : mojoMenuList) {
                String category = mojoMenu.getCategory();
                if (!categoryList.contains(category)) {
                    categoryList.add(category);
                }
            }
        }
        Collections.sort(categoryList);
        return categoryList;
    }

    public static String getToppingListString(List<String> toppingList) {
        StringBuilder sb = new StringBuilder();
        int size = toppingList.size();
        for (int i = 0; i < size; i++) {
            sb.append(toppingList.get(i));
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
