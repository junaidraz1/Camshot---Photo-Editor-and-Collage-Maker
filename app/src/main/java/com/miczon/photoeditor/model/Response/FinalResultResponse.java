package com.miczon.photoeditor.model.Response;

import com.google.gson.annotations.SerializedName;
import com.miczon.photoeditor.model.FinalResultDetails;

import java.io.Serializable;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class FinalResultResponse implements Serializable {

    @SerializedName("status")
    public String status;

    @SerializedName("image_process_response")
    public FinalResultDetails finalResultDetails;

}
