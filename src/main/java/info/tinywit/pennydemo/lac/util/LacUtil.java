package info.tinywit.pennydemo.lac.util;

import com.baidu.nlp.LAC;
import info.tinywit.pennydemo.lac.business.LacBusiness;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

public class LacUtil {
    private final static Logger LOG = LoggerFactory.getLogger(LacBusiness.class);
    public static LAC lac;

    static {
        System.out.println("please put liblacjni.so in dir: " + System.getProperty("java.library.path"));
        System.loadLibrary("lacjni");
        lac = new LAC(System.getProperty("user.dir") + "/models_general/lac_model");
        //lac = new LAC("/home/k/Desktop/models_general/lac_model");
    }

    public static String run(final String text, int workCnt) {
        FutureTask<String>[] tasks = new FutureTask[workCnt];
        final String[] textLines = StringUtils.split(text, "\n");
        if (textLines == null || text.length() == 0) {
            return null;
        }

        int begin = 0;
        int step = textLines.length / workCnt + 1;
        for (int i = 0; i < tasks.length; i++) {
            final int s = begin;
            final int e = Math.min(begin + step, textLines.length);
            final int t = i;
            begin = e;
            LAC threadLac = new LAC(lac);
            FutureTask<String> stringFutureTask = new FutureTask<>(() -> {
                for (int j = s; j < e; j++) {
                    ArrayList<String> words = new ArrayList<>();
                    ArrayList<String> tags = new ArrayList<>();
                    threadLac.run(textLines[j], words, tags);
                    textLines[j] = StringUtils.joinWith("//", words.toArray());
                    if (j % 100 == 0) {
                        LOG.info("## lac-thread-" + t + " ## " + ((j - s) / (e - s)));
                    }
                }
                LOG.info("## lac-thread-" + t + " ## " + "100%");
                return null;
            });
            tasks[i] = stringFutureTask;
            (new Thread(stringFutureTask)).start();
        }

        for (FutureTask<String> task : tasks) {
            try {
                task.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return StringUtils.joinWith(System.lineSeparator(), textLines);
    }
}
