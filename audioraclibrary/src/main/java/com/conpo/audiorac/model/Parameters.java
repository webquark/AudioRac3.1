package com.conpo.audiorac.model;

import java.util.HashMap;

/**
 * Http Request용 Parameters
 * Created by hansolo on 2016-08-01.
 */

public class Parameters extends HashMap<String, Object> {

    public Parameters() {

    }

    public String toQueryParameters() {
        String res = "";
        int i = 0;

        for (Entry<?, ?> entry : this.entrySet()) {
            if (i++ > 0) {
                res += "&";
            }

            res += (String.valueOf(entry.getKey()) + "=" + entry.getValue().toString());
        }

        return res;
    }
}
