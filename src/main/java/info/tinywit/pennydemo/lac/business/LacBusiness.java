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
    @Value("${app.lac.worker.cnt:1}")
    private Integer workCnt;

    @Async
    public String run(String text, String flag) {
        LOG.info("xxxxxx" + flag + ": start");
        long startTime = System.currentTimeMillis();
        String run = LacUtil.run(text, workCnt);
        LOG.info("-> " + run);
        long duration = System.currentTimeMillis() - startTime;
        LOG.info("xxxxxx" + flag + ": end, " + duration / 1000 + "S" + " (" + duration / 1000 / 60 + "Min)");
        return run;
    }
}
