package com.looseboxes.idisc.newsminutefree;

import com.looseboxes.idisc.appbilling.ApplicationWithInAppPurchase;

/**
 * @author Chinomso Ikwuagwu on 11/18/2015.
 */
public class NewsMinuteFreeApplication extends ApplicationWithInAppPurchase {

    //@todo
    /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     *
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    public final String getAppKey() {
        String s = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjM5MbQcEa+zFkM8zpYc5yDW9r3kzl2TPhtAswT9YZQLbkzL003rv2I1Tezo5KVEEeLYwYZdSghZSGsa86ysNn2koPrENLfDkKFTARzL8RRZoCONF6v0+UkXKFwVShgaadCNWiuV8BDyKcDtu5Mm03I2ENc2pf/vL4D435USVrCzpcfk+UBbpFbDmrOz5pKih6nMkLWVqmFfex7APxjJLf99VtWNiftLUpJfcmHwK+aFYvZFqp5gnCgl+YSBlxt/ViAxsq9ZJCRzP8oLDvip37cqpa+Ai7JiTmPggxm1u280U70nSffnMMl1PAk+m9LiM4guh6y9kuHosaCUvi9c3QwIDAQAB";
        return s.replaceAll("\\s", "");
    }
}
