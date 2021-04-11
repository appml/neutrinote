package com.appmindlab.nano;

/**
 * Created by saelim on 1/20/2016.
 */
public class CustomFont {
    private String mFontFamily;
    private String mPath;
    private String mUrl;
    private String mCSS;

    public CustomFont(String fontFamily, String path, String url, String css) {
        this.mFontFamily = fontFamily;
        this.mPath = path;
        this.mUrl = url;
        this.mCSS = css;
    }

    public String getFontFamily() {return this.mFontFamily;}
    public void setmFontFamily(String fontFamily) {this.mFontFamily = fontFamily;}

    public String getPath() {return this.mPath;}
    public void setPath(String path) {this.mPath = path;}

    public String getUrl() {return this.mUrl;}
    public void setUrl(String url) {this.mUrl = url;}

    public String getCSS() {return this.mCSS;}
    public void setCSS(String css) {this.mCSS = css;}
}
