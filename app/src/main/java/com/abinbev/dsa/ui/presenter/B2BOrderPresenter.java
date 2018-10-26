package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;

import com.abinbev.dsa.model.Auth_Keys__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * this class is used to get b2b order token
 */
public class B2BOrderPresenter implements Presenter<B2BOrderPresenter.ViewModel> {


    private String TAG = getClass().getSimpleName();
    private ViewModel viewModel;


    public interface ViewModel {
        void getAccountResult(B2BParams params);
    }

    public class B2BParams {
        public String rsp;
        public String res;
        public DataBean data;

        public class DataBean {
            public boolean status;
            public String message;
            public String access_token;
        }
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        HashMap<String, String> params = new HashMap<>();
        params.put(AbInBevConstants.B2BOrderFields.Format, "json");
        params.put(AbInBevConstants.B2BOrderFields.Method, Auth_Keys__c.getAppointFieldValue(AbInBevConstants.AuthKeysFielsd.ChinaBackend_Auth_Method_Name__c));
        params.put(AbInBevConstants.B2BOrderFields.ABI_CODE, User.getCurrentUser().getEmployeeCode());
        String sign = getSign(params);
        params.put(AbInBevConstants.B2BOrderFields.SIGN, sign);
        sendPost(params);
    }

    private void sendPost(Map<String, String> params) {
        MultipartBody.Builder urlBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (params != null) {
            for (String key : params.keySet()) {
                if (params.get(key) != null) {
                    urlBuilder.addFormDataPart(key, params.get(key));
                }
            }
        }
        OkHttpClient mOkHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .headers(new Headers.Builder().build())
                .url(TextUtils.isEmpty(Auth_Keys__c.getAcessTokenUrl()) ? "http://" : Auth_Keys__c.getAcessTokenUrl())
                .post(urlBuilder.build())
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                B2BParams b2BParams = new B2BParams();
                b2BParams.rsp = "error";
                if (viewModel != null) viewModel.getAccountResult(b2BParams);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                B2BParams b2BParams = new Gson().fromJson(string, B2BParams.class);
                if (viewModel != null) viewModel.getAccountResult(b2BParams);
            }
        });
    }


    private String getSign(Map<String, String> params) {
        StringBuilder param = new StringBuilder();
        String token = Auth_Keys__c.getAppointFieldValue(AbInBevConstants.AuthKeysFielsd.AccountKey__c);
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(params.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> mapping1, Map.Entry<String, String> mapping2) {
                return mapping1.getKey().compareTo(mapping2.getKey());
            }
        });
        for (Map.Entry<String, String> entry : list) {
            param.append(entry.getKey()).append(entry.getValue());
        }
        String sign = "";
        sign = MD5(param.toString(), "utf-8");
        sign = sign.toUpperCase() + token;
        sign = MD5(sign, "utf-8");
        return sign.toUpperCase();
    }

    private static String MD5(String str, String charset) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes(charset));
            byte[] result = md.digest();
            StringBuffer sb = new StringBuffer(32);
            for (int i = 0; i < result.length; i++) {
                int val = result[i] & 0xff;
                if (val <= 0xf) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(val));
            }
            return sb.toString().toLowerCase();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void stop() {

    }
}
