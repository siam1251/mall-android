package factory;

import android.util.Log;

import com.ivanhoecambridge.mall.account.KcpAccount;
import com.ivanhoecambridge.mall.constants.Constants;

import java.util.HashMap;

import static com.ivanhoecambridge.mall.constants.Constants.HEADER_KEY_ACCEPT;
import static com.ivanhoecambridge.mall.constants.Constants.HEADER_KEY_AUTHORIZATION;
import static com.ivanhoecambridge.mall.constants.Constants.HEADER_KEY_CLIENT_TOKEN;
import static com.ivanhoecambridge.mall.constants.Constants.HEADER_KEY_CONTENT_TYPE;
import static com.ivanhoecambridge.mall.constants.Constants.HEADER_KEY_DATAHUB_CATALOG;
import static com.ivanhoecambridge.mall.constants.Constants.HEADER_KEY_DATAHUB_LOCALE;

/**
 * Created by Kay on 2016-05-13.
 */
public class HeaderFactory {

    //------------------------------ END POINT ------------------------------
    public static String HEADER_VALUE_DATAHUB_CATALOG = "vaughan-mills";
    //------------------------------ END POINT ------------------------------

    public static String MALL_NAME = "Vaughan Mills";
    public static String MAP_VENUE_NAME = "Vaughan Mills";
    private final static String SEARCH_INDEX_VM_STAGING = "indexes/staging/vaughan-mills-index.msgpack";
    private final static String SEARCH_INDEX_VM_PRODUCTION = "indexes/production/vaughan-mills-index.msgpack";

    public final static String HEADER_VALUE_ACCEPT = 			"application/json";

    private static HashMap<String, String> mHeaders;
    public static HashMap<String, String> getHeaders(){
        if(mHeaders == null) {
            constructHeader();
        }
        return mHeaders;
    }

    public static void changeCatalog(String catalog){
        HEADER_VALUE_DATAHUB_CATALOG = catalog;
        constructHeader();
    }

    public static void constructHeader() {
        mHeaders = new HashMap<>();

        mHeaders.put(HEADER_KEY_DATAHUB_CATALOG,    HEADER_VALUE_DATAHUB_CATALOG);
        mHeaders.put(HEADER_KEY_DATAHUB_LOCALE,     Constants.HEADER_VALUE_DATAHUB_LOCALE);
        mHeaders.put(HEADER_KEY_CLIENT_TOKEN,       getClientToken());
        mHeaders.put(HEADER_KEY_AUTHORIZATION,      KcpAccount.getInstance().getUserTokenWithBearer());

        //below two headers are specially needed for view_all_content
        mHeaders.put(HEADER_KEY_CONTENT_TYPE,       Constants.HEADER_VALUE_CONTENT_TYPE);
        mHeaders.put(HEADER_KEY_ACCEPT,             HEADER_VALUE_ACCEPT);
    }

    /**
     * Utility method to update the authorization token.
     * @param token Authorization token.
     */
    public static void updateAuthorizationToken(String token) {
        if (mHeaders == null) return;
        Log.i("Autho", "Update with " + token);
        mHeaders.put(HEADER_KEY_AUTHORIZATION, token);
    }

    public static String getClientToken(){
        if(Constants.IS_APP_IN_PRODUCTION) return Constants.HEADER_VALUE_CLIENT_TOKEN_PRODUCTION;
        else return Constants.HEADER_VALUE_CLIENT_TOKEN_STAGING;
    }

    public static String getSearchIndexUrl(){
        if(Constants.IS_APP_IN_PRODUCTION) return SEARCH_INDEX_VM_PRODUCTION;
        else return SEARCH_INDEX_VM_STAGING;
    }
}
