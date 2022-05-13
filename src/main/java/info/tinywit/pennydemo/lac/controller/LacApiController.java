package info.tinywit.pennydemo.lac.controller;

import com.alibaba.fastjson.JSON;
import info.tinywit.pennydemo.lac.business.LacBusiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController()
@RequestMapping("/lac")
public class LacApiController {
    private final static Logger LOG = LoggerFactory.getLogger(LacApiController.class);

    @Autowired
    LacBusiness lacBusiness;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String lac(@RequestBody(required = false) String body, @RequestParam(required = false) String flag) {
        HashMap<String, String> ret = new HashMap<>();
        ret.put("code", "0");
        ret.put("msg", "doing, wait a moment!");
        try {
            lacBusiness.run(body, flag);
            //System.out.println("1");
        } catch (Exception e) {
            ret.put("code", "-1");
            ret.put("code", "err!");
        }
        return JSON.toJSONString(ret);
    }
}
