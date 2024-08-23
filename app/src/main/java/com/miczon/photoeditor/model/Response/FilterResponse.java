package com.miczon.photoeditor.model.Response;

import com.google.gson.annotations.SerializedName;
import com.miczon.photoeditor.model.FilterProcessResponse;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class FilterResponse {

    @SerializedName("status")
    public String status;

    @SerializedName("image_process_response")
    public FilterProcessResponse filterProcessResponse;
}
