package com.korovkin.yandexfotki.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by Nik on 14.02.2016.
 */
public class Entries {
    public String title;
    public Img img;
    public Geo geo;

    @SerializedName("tags")
    public HashMap<String, String> tags;

    public class Img {
        @SerializedName("XXS")
        public Images xxs;
        @SerializedName("XL")
        public Images xl;
        @SerializedName("M")
        public Images m;
        @SerializedName("L")
        public Images l;
        @SerializedName("XXXS")
        public Images xxxs;
        @SerializedName("XXXL")
        public Images xxxl;
        @SerializedName("S")
        public Images s;
        @SerializedName("XS")
        public Images xs;
        @SerializedName("XXL")
        public Images xxl;
        @SerializedName("orig")
        public Images orig;
    }
}
