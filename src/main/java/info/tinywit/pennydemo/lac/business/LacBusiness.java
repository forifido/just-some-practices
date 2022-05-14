package info.tinywit.pennydemo.lac.business;

import info.tinywit.pennydemo.lac.util.LacUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class LacBusiness {
    private final static Logger LOG = LoggerFactory.getLogger(LacBusiness.class);
    @Value("${app.lac.worker.defcnt:2}")
    private Integer defWorkerCnt;
    @Value("${app.lac.worker.maxcnt:10}")
    private Integer maxWorkerCnt;

    @Async
    public String run(String text, String flag, Integer tc) {
        LOG.info("xxxxxx-{}: start", flag);
        long startTime = System.currentTimeMillis();
        if (tc == null || tc < 1 || tc > maxWorkerCnt) {
            tc = defWorkerCnt;
        }
        String ret = LacUtil.run(text, tc);
        LOG.debug("ret -> " + ret);
        long duration = System.currentTimeMillis() - startTime;
        LOG.info("xxxxxx-{}: end, thread cnt:{}, size:{}MB, {}Sec, {}Min",
                flag, tc, text.length()/1024/1024, duration/1000, duration/1000/60);
        return ret;
    }
}
