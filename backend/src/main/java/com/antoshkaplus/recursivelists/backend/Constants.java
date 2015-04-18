package com.antoshkaplus.recursivelists.backend;


import com.google.api.server.spi.Constant;

// don't forget to change ids
public class Constants {

    public static final String WEB_CLIENT_ID = "582892993246-g35aia2vqj3dl9umucp57utfvmvt57u3.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID_HOME = "582892993246-6uderlmpa1nk5futkg4569h3ajj5vmj6.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID_WORK = "582892993246-9epv5mak38citkhigfspgv75ie6a34os.apps.googleusercontent.com";
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;
    public static final String API_EXPLORER_CLIENT_ID = Constant.API_EXPLORER_CLIENT_ID;

    // this one is invalid
    public static final String ANDROID_CLIENT_ID_RELEASE = "955907089846-vrecqngobngm2c0ci99mjq8iki87i3ul.apps.googleusercontent.com";

    public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
}