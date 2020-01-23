package com.iwdnb.blqs.example.test;

import java.util.List;

import lombok.Data;

@Data
public class AccountDTO {

    private String           name;

    private String           legalName;

    private String           idcard;

    private String           mobile;

    private UploadFile       file;

    private List<UploadFile> fileList;

}
