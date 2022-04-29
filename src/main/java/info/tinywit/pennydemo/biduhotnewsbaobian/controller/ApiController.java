package info.tinywit.pennydemo.biduhotnewsbaobian.controller;

import com.alibaba.fastjson.JSON;
import info.tinywit.pennydemo.biduhotnewsbaobian.business.BiduHotNews;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api")
public class ApiController {
    private final static Logger LOG = LoggerFactory.getLogger(ApiController.class);
    @Autowired
    BiduHotNews biduHotNews;

    @RequestMapping(value = "/bidu", produces = "application/json;charset=UTF-8")
    public String biduHotNewsBaobian() {
        String ret = JSON.toJSONString(biduHotNews.index());
        LOG.debug("/api/bidu resp: " + ret);
        return ret;
    }
}
