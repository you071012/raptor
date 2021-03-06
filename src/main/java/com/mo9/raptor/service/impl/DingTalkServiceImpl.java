package com.mo9.raptor.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mo9.raptor.service.DingTalkService;
import com.mo9.raptor.utils.httpclient.HttpClientApi;
import com.mo9.raptor.utils.httpclient.bean.HttpResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by ycheng on 2018/7/6.
 *
 * @author ycheng
 */
@Service(value = "dingTalkService")
public class DingTalkServiceImpl implements DingTalkService {

    @Value(value = "${suona.dingtalk.notice.url}")
    private String dingTalkNoticeUrl;

    @Value(value = "${suona.dingtalk.notice.send}")
    private boolean dingTalkNoticeSend;

    @Value(value = "${suona.dingtalk.notice.hook}")
    private String dingTalkNoticeHook;

    @Value(value = "${raptor.environment}")
    private String environment;

    @Autowired
    private HttpClientApi httpClientApi;

    /**
     * 发送钉钉
     *
     * @param title
     * @param message
     * @return
     */
    @Override
    public void sendNotice(String title, String message) {

        if (!dingTalkNoticeSend) {
            return;
        }

        JSONObject params = new JSONObject();
        JSONArray target = new JSONArray();

        String content =  title  + message;

        params.put("mediaType", "link");
        params.put("hook", dingTalkNoticeHook);
        params.put("message", content);
        params.put("title", "【" + environment + "】" + title);
        params.put("target", target);

        try {
            HttpResult result = httpClientApi.doPostJson(dingTalkNoticeUrl, String.valueOf(params));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 发送钉钉
     *
     * @param message
     * @return
     */
    @Override
    public void sendText(String message) {

        if (!dingTalkNoticeSend) {
            return;
        }

        JSONObject params = new JSONObject();


        params.put("mediaType", "text");
        params.put("hook", "https://oapi.dingtalk.com/robot/send?access_token=51c1205303ab1ea4fb4d1e7d9739d7a0730509795e5218e5766cd21dd267151b");
        params.put("message","【" + environment + "】 + \n" + message);
        params.put("target", "13564546025");

        String url = "https://suona.ioex.com/suonaApi/ding_notice/sendNotie";

        try {
            HttpResult result = httpClientApi.doPostJson(url, String.valueOf(params));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
