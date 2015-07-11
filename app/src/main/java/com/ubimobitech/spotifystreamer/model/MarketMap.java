/*
 * Copyright 2015, Isaac Ben-Akiva
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
 * limitations under the License.
 */
/**
 * FILE: MarketMap.java
 * AUTHOR: Dr. Isaac Ben-Akiva <isaac.ben-akiva@ubimobitech.com>
 * <p/>
 * CREATED ON: 11/07/15
 */

package com.ubimobitech.spotifystreamer.model;

import android.content.Context;

import com.ubimobitech.spotifystreamer.R;

import java.util.HashMap;

/**
 * Created by benakiva on 11/07/15.
 */
public class MarketMap {
    private static HashMap<String, String> mCountries = new HashMap<>();
    private static Context mContext;

    public MarketMap(Context context) {
        mContext = context;
    }

    static {
        String[] countryNames = mContext.getResources().getStringArray(R.array.country_names);
        String[] contryCodes = mContext.getResources().getStringArray(R.array.country_codes);

        for (int i = 0; i < countryNames.length; i++) {
            mCountries.put(countryNames[i], contryCodes[i]);
        }
    }

    public static String getContryCode(final String name) {
        return mCountries.get(name);
    }

    public String[] getContriesList() {
        return mContext.getResources().getStringArray(R.array.country_names);
    }
}
