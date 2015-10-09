package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("MojoImage")
public class MojoImage extends ParseObject {

    public static final String IMAGE_ID = "imageId";
    private static final String IMAGE = "image";

    public static ParseQuery<MojoImage> getQuery() {
        return ParseQuery.getQuery(MojoImage.class);
    }

    public MojoImage() {

    }

    public int getImageId() {
        return getInt(IMAGE_ID);
    }

    public ParseFile getImage() {
        return getParseFile(IMAGE);
    }
}
