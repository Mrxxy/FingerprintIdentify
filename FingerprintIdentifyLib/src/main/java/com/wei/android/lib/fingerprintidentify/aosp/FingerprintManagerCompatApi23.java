/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.wei.android.lib.fingerprintidentify.aosp;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;


@RequiresApi(23)
@TargetApi(23)
@RestrictTo(LIBRARY_GROUP)
public final class FingerprintManagerCompatApi23 {

    private static FingerprintManager getFingerprintManagerOrNull(Context context) {
        return (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
    }

    public static boolean hasEnrolledFingerprints(Context context) {
        final FingerprintManager fp = getFingerprintManagerOrNull(context);
        return (fp != null) && fp.hasEnrolledFingerprints();
    }

    public static boolean isHardwareDetected(Context context) {
        final FingerprintManager fp = getFingerprintManagerOrNull(context);
        return (fp != null) && fp.isHardwareDetected();
    }

    public static void authenticate(Context context, CryptoObject crypto, int flags, Object cancel, AuthenticationCallback callback, Handler handler) {
        final FingerprintManager fp = getFingerprintManagerOrNull(context);
        if (fp != null) {
            fp.authenticate(wrapCryptoObject(crypto), (CancellationSignal) cancel, flags, wrapCallback(callback), handler);
        }
    }

    private static FingerprintManager.CryptoObject wrapCryptoObject(CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        } else if (cryptoObject.getCipher() != null) {
            return new FingerprintManager.CryptoObject(cryptoObject.getCipher());
        } else if (cryptoObject.getSignature() != null) {
            return new FingerprintManager.CryptoObject(cryptoObject.getSignature());
        } else if (cryptoObject.getMac() != null) {
            return new FingerprintManager.CryptoObject(cryptoObject.getMac());
        } else {
            return null;
        }
    }

    private static CryptoObject unwrapCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        } else if (cryptoObject.getCipher() != null) {
            return new CryptoObject(cryptoObject.getCipher());
        } else if (cryptoObject.getSignature() != null) {
            return new CryptoObject(cryptoObject.getSignature());
        } else if (cryptoObject.getMac() != null) {
            return new CryptoObject(cryptoObject.getMac());
        } else {
            return null;
        }
    }

    private static FingerprintManager.AuthenticationCallback wrapCallback(final AuthenticationCallback callback) {
        return new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                callback.onAuthenticationError(errMsgId, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                callback.onAuthenticationHelp(helpMsgId, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                callback.onAuthenticationSucceeded(new AuthenticationResultInternal(unwrapCryptoObject(result.getCryptoObject())));
            }

            @Override
            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        };
    }

    public static class CryptoObject {

        private final Signature mSignature;
        private final Cipher mCipher;
        private final Mac mMac;

        public CryptoObject(Signature signature) {
            mSignature = signature;
            mCipher = null;
            mMac = null;
        }

        public CryptoObject(Cipher cipher) {
            mCipher = cipher;
            mSignature = null;
            mMac = null;
        }

        public CryptoObject(Mac mac) {
            mMac = mac;
            mCipher = null;
            mSignature = null;
        }

        public Signature getSignature() {
            return mSignature;
        }

        public Cipher getCipher() {
            return mCipher;
        }

        public Mac getMac() {
            return mMac;
        }
    }

    public static final class AuthenticationResultInternal {
        private CryptoObject mCryptoObject;

        public AuthenticationResultInternal(CryptoObject crypto) {
            mCryptoObject = crypto;
        }

        public CryptoObject getCryptoObject() {
            return mCryptoObject;
        }
    }

    public static abstract class AuthenticationCallback {

        public void onAuthenticationError(int errMsgId, CharSequence errString) {
        }

        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        }

        public void onAuthenticationSucceeded(AuthenticationResultInternal result) {
        }

        public void onAuthenticationFailed() {
        }
    }
}
