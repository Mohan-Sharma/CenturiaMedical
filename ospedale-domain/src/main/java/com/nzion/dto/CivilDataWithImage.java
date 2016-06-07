package com.nzion.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;

/**
 * Created by Mohan Sharma on 7/10/2015.
 */
public class CivilDataWithImage {

    private CivilUserDto civilUserDto;
    private String image;

    public CivilUserDto getCivilUserDto() {
        return civilUserDto;
    }

    public void setCivilUserDto(CivilUserDto civilUserDto) {
        this.civilUserDto = civilUserDto;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public static CivilDataWithImage getCivilUserDtoFromJson(String json){
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("dd/MM/yyyy").create();
        CivilDataWithImage civilDataWithImage = gson.fromJson(json, CivilDataWithImage.class);
        return civilDataWithImage;
    }

    public byte[] getByteArrayFromBase64String(){
        return Base64.decodeBase64(this.getImage());
    }
}
