package com.ivanhoecambridge.mall.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivanhoecambridge.kcpandroidsdk.managers.KcpCategoryManager;
import com.ivanhoecambridge.kcpandroidsdk.service.ServiceFactory;
import com.ivanhoecambridge.kcpandroidsdk.utils.KcpUtility;
import com.ivanhoecambridge.mall.R;
import com.ivanhoecambridge.mall.giftcard.GiftCard;
import com.ivanhoecambridge.mall.giftcard.GiftCardResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by Kay on 2017-02-16.
 */

public class GiftCardManager {

    private final String END_POINT = "https://webservices.storefinancial.net/api/v1/en/cards/";
    private final long timeOutDuration = 1;

    private static Context mContext;
    private final static String KEY_GSON_GIFT_CARD = "GIFT_CARD";
    private final static String KEY_GSON_GIFT_CARD_SIGN_UP = "GIFT_CARD_SIGN_UP";
    private final static String PARAM_PROGRAM = "Ivanhoe138";
    private final static String PARAM_FIELDS = "available_balance,status";

    private final static String HEADER_KEY_ACCEPT = "Accept";
    private final static String HEADER_KEY_AUTH = "Authorization";
    private final static String HEADER_KEY_ICMP_AUTO = "ICMPStoreFinancialAuthName";
    private final static String HEADER_KEY_ICMP_PASS = "ICMPStoreFinancialAuthPassword";

    private final static String HEADER_VALUE_ACCEPT = "application/json";
    private final static String HEADER_VALUE_ICMP_AUTO = "Ivanhoestats";
    private final static String HEADER_VALUE_ICMP_PASS = "1Iv@nh0e!";

    private HashMap<String, String> headers;
    private static HashMap<String, GiftCard> giftCards;
    private static GiftCardManager sGiftCardManager;
    protected GiftCardService mGiftCardService;
    protected Handler mHandler;
    private GiftCardListener giftCardListener;
    public GiftCardUpdateListener mGiftCardUpdateListener;

    public static final int DOWNLOAD_FAILED = -1;
    public static final int DOWNLOAD_STARTED = 1;
    public static final int DOWNLOAD_COMPLETE = 2;
    public static final int DATA_ADDED = 3;
    public static final int TASK_COMPLETE = 4;

    public static final int ERROR_INVALID_CARD = 11;
    public static final int ERROR_DUPLICATE_CARD = 12;

    /**
     * Gift card callback to notify when the gift card has been added.
     */
    public interface GiftCardListener {
        /**
         * Notifies the user that a gift card has been added successfully.
         */
        void onGiftCardAdded();

        /**
         * Notifies the user that an error has occured when attempting to add the gift card.
         * @param giftCardError GiftCard error type.
         */
        void onGiftCardError(int giftCardError);
    }

    /**
     * Gift card listener that listens for changes on already existing gift cards on profile screen.
     */
    public interface GiftCardUpdateListener {
        void onGiftCardUpdated();
        void onGiftCardUpdateFailed(String errorMessage);
    }

    public static GiftCardManager getInstance(Context context) {
        mContext = context;
        if(sGiftCardManager == null) sGiftCardManager = new GiftCardManager();
        return sGiftCardManager;
    }

    public GiftCardManager(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    public GiftCardManager() {
        giftCards = loadGiftCard();
    }

    public interface GiftCardService {
        @GET
        Call<GiftCardResponse> getGCBalance(
                @HeaderMap Map<String, String> headers,
                @Url String url,
                @Query("program") String program,
                @Query("fields") String fields);
    }



    /**
     * Sets a callback listener for gift card updates on the profile screen.
     * @param giftCardUpdateListener GiftCardUpdateListener callback.
     */
    public void setGiftCardUpdateListener(GiftCardUpdateListener giftCardUpdateListener){
        mGiftCardUpdateListener = giftCardUpdateListener;
    }

    /**
     * Sets a callback listener for Gift Card adding.
     * @param giftCardListener GiftCardListener callback.
     */
    public void setGiftCardListener(GiftCardListener giftCardListener) {
        this.giftCardListener = giftCardListener;
    }

    public HashMap<String, GiftCard> getGiftCards() {
        return giftCards;
    }

    public static HashMap<String, GiftCard> loadGiftCard(){
        Gson gson = new Gson();
        String json = mContext.getSharedPreferences("PreferenceManager", Context.MODE_PRIVATE).getString(KEY_GSON_GIFT_CARD, "");
        Type listType = new TypeToken<HashMap<String, GiftCard>>() {}.getType();
        HashMap<String, GiftCard> obj = gson.fromJson(json, listType);
        if(obj == null) return new HashMap<String, GiftCard>();
        else return obj;
    }

    /**
     * Saves a single gift card into the cache until after the user has signed up.
     * @param giftCard GiftCard object to save
     */
    public void saveGiftCardAfterSignUp(GiftCard giftCard) {
        KcpUtility.saveGson(mContext, KEY_GSON_GIFT_CARD_SIGN_UP, giftCard);
    }

    public void applySavedGiftCardToAccount() {
        GiftCard savedGiftCard = KcpUtility.getObjectFromCache(mContext, KEY_GSON_GIFT_CARD_SIGN_UP, GiftCard.class);
        if (savedGiftCard != null) {
            saveCardToAccount(savedGiftCard);
            KcpUtility.removeFromCache(mContext, KEY_GSON_GIFT_CARD_SIGN_UP);
        }
    }

    private void saveGiftCard(){
        KcpUtility.saveGson(mContext, KEY_GSON_GIFT_CARD, giftCards);
    }

    public boolean isCardAdded(String cardNumber){
        if(giftCards == null || !giftCards.containsKey(cardNumber)) return false;
        else return true;
    }

    public boolean addCard(String cardNumber, float cardBalance){
        boolean cardExist = isCardAdded(cardNumber);
        giftCards.put(cardNumber, new GiftCard(cardNumber, cardBalance));
        saveGiftCard();
        return cardExist;
    }

    public void saveCardToAccount(GiftCard giftCard) {
        if (giftCards.containsKey(giftCard.getCardNumber())) {
            if (giftCardListener != null) {
                giftCardListener.onGiftCardError(ERROR_DUPLICATE_CARD);
            }
        } else {
            giftCards.put(giftCard.getCardNumber(), giftCard);
            saveGiftCard();
            if (giftCardListener != null) {
                giftCardListener.onGiftCardAdded();
            }
        }
    }

    public boolean removeCard(String cardNumber){
        if(giftCards.containsKey(cardNumber)) {
            giftCards.remove(cardNumber);
            saveGiftCard();
            return true;
        } return false;
    }

    /**
     * update balance of all cards saved
     */
    public void updateBalance(){
        List<String> giftCardList = new ArrayList<>(giftCards.keySet());
        if (giftCardList.isEmpty()) {
            if (mGiftCardUpdateListener != null) {
                mGiftCardUpdateListener.onGiftCardUpdated();
            }
        } else {
            for (int i = 0; i < giftCardList.size(); i++) {
                updateBalance(giftCardList.get(i), i == giftCardList.size() - 1);
            }
        }
    }


    /**
     * update balance of a single card
     * @param cardNumber
     * @param showToast show toast to indicate update's been done ex. when checking the last giftcard blaance
     */
    private void updateBalance(final String cardNumber, final boolean showToast){
        if(giftCards.containsKey(cardNumber)) {

            GiftCardManager giftCardManager = new GiftCardManager(mContext, new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {
                    switch (inputMessage.arg1) {
                        case KcpCategoryManager.DOWNLOAD_FAILED:
                            if (mGiftCardUpdateListener != null) mGiftCardUpdateListener.onGiftCardUpdateFailed(mContext.getString(R.string.drawer_gc_failed_to_update));
                            break;
                        case KcpCategoryManager.DOWNLOAD_COMPLETE:
                            GiftCardResponse giftCardResponse = (GiftCardResponse) inputMessage.obj;
                            addCard(cardNumber, giftCardResponse.getAvailableBalance());
                            if(mGiftCardUpdateListener != null) mGiftCardUpdateListener.onGiftCardUpdated();
                            break;
                        default:
                            super.handleMessage(inputMessage);
                    }
                }
            });
            giftCardManager.checkCardBalance(cardNumber);
        } else {
        }
    }



    public GiftCardService getKcpService(){
        headers = new HashMap();
        headers.put(HEADER_KEY_ACCEPT,     HEADER_VALUE_ACCEPT);
        headers.put(HEADER_KEY_AUTH,       getAuth());
        headers.put(HEADER_KEY_ICMP_AUTO,  HEADER_VALUE_ICMP_AUTO);
        headers.put(HEADER_KEY_ICMP_PASS,  HEADER_VALUE_ICMP_PASS);

        if(mGiftCardService == null) mGiftCardService = ServiceFactory.createRetrofitService(mContext, timeOutDuration, GiftCardService.class, END_POINT);
        return mGiftCardService;
    }

    private String getAuth(){
        String credentials = HEADER_VALUE_ICMP_AUTO + ":" + HEADER_VALUE_ICMP_PASS;
        final String basicAuth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP); //not working
        return basicAuth;
    }

    public void checkCardBalance(String cardNumber){
        cardNumber = cardNumber.replace("-", "");
        Call<GiftCardResponse> call = getKcpService().getGCBalance(headers, cardNumber, PARAM_PROGRAM, PARAM_FIELDS);
        call.enqueue(new Callback<GiftCardResponse>() {
            @Override
            public void onResponse(Call<GiftCardResponse> call, Response<GiftCardResponse> response) {
                if(response.isSuccessful()){
                    GiftCardResponse giftCardResponse = response.body();
                    handleState(DOWNLOAD_COMPLETE, giftCardResponse, 0);
                } else {
                    int code = response.code();
                    int errorCode = 0;
                    String errorMsg;
                    switch(code) {
                        case 400: //malformed syntax
                        case 401: //auth failed
                            errorMsg = mContext.getString(R.string.gc_error_request);
                            break;
                        case 404: //URL doesn't match any of defined routes
                            errorMsg = mContext.getString(R.string.error_code_404);
                            errorCode = ERROR_INVALID_CARD;
                            break;
                        case 500: //server error
                        case 504: //server error
                            errorMsg = mContext.getString(R.string.error_code_500);
                            break;
                        default:
                            errorMsg = response.message();
                            break;
                    }

                    handleState(DOWNLOAD_FAILED, errorMsg, errorCode);
                }
            }

            @Override
            public void onFailure(Call<GiftCardResponse> call, Throwable t) {
                handleState(DOWNLOAD_FAILED);
            }
        });
    }

    private void handleState(int state){
        handleState(state, mContext.getString(R.string.error_code_internet), 0);
    }

    private void handleState(int state, @Nullable Object result, int error){
        if(mHandler == null) return;
        Message message = new Message();
        message.arg1 = state;
        message.obj = result;
        message.what = error;
        mHandler.sendMessage(message);
    }

}
