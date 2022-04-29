package info.tinywit.pennydemo.biduhotnewsbaobian.business;

import com.alibaba.fastjson.JSON;
import info.tinywit.pennydemo.biduhotnewsbaobian.vo.BaobianIndex;
import info.tinywit.pennydemo.biduhotnewsbaobian.vo.News;
import info.tinywit.pennydemo.util.HttpclientUtil;
import org.jsoup.nodes.Element;
import org.seimicrawler.xpath.JXDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

@Component
public class BiduHotNews {
    private final static Logger LOG = LoggerFactory.getLogger(BiduHotNews.class);
    private final static BigDecimal NEGATIVE_INDEX_THRESHOLD = new BigDecimal("-0.5");
    private final static String BAOBIAN_API = "http://baobianapi.pullword.com:9091/get.php";
    private final static String BIDU_NEWS_URL = "http://news.baidu.com";

    public Collection<News> index() {
        List<News> newsList = fetchHotNews();
        return multiPost(newsList);
    }

    private Collection<News> multiPost(final List<News> list) {
        ConcurrentLinkedQueue<News> ret = new ConcurrentLinkedQueue<>();
        final int threadCnt = 5;
        final int step = list.size() / threadCnt;
        CountDownLatch countDownLatch = new CountDownLatch(threadCnt);
        for (int i = 0; i < threadCnt; i++) {
            final int idxBegin = i * step;
            final int idxEnd = Math.min((i + 1) * step, list.size());
            new Thread(() -> {
                for (int idx = idxBegin; idx < idxEnd; idx++) {
                    String resp;
                    try {
                        resp = HttpclientUtil.post(BAOBIAN_API, list.get(idx).getTitle());
                    } catch (Exception e) {
                        LOG.error("err#req baobian api; title: {}", list.get(idx).getTitle());
                        continue;
                    }

                    BaobianIndex baobianIndex;
                    try {
                        baobianIndex = JSON.parseObject(resp, BaobianIndex.class);
                    } catch (Exception e) {
                        LOG.error("err#parse baobian resp; title: {}, resp: {}", list.get(idx).getTitle(), resp);
                        continue;
                    }
                    if (isNeg(baobianIndex.getResult())) {
                        News news = list.get(idx);
                        news.setIndex(baobianIndex.getResult());
                        try {
                            news.setUrl(URLDecoder.decode(news.getUrl(), "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            // ignore
                        }
                        ret.add(news);
                    }
                }
                countDownLatch.countDown();
            }).start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        LOG.info("statistics#total count of neg news: {}", ret.size());

        return ret;
    }

    private boolean isNeg(BigDecimal idx) {
        return idx.compareTo(NEGATIVE_INDEX_THRESHOLD) == -1;
    }

    private List<News> fetchHotNews() {
        String page = HttpclientUtil.get(BIDU_NEWS_URL);
        JXDocument jxDocument = JXDocument.create(page);
        String xPath = "//div[@id='body']//ul/li//a[@target='_blank']";
        List<Object> list = jxDocument.sel(xPath);
        ArrayList<News> newsList = new ArrayList<>();
        for (Object e : list) {
            if (e instanceof Element) {
                String url = ((Element) e).attr("href");
                String title = ((Element) e).html();
                newsList.add(new News(url, title));
            }
        }

        LOG.info("statistics#total count of extracted url: {}", newsList.size());

        return newsList;
    }
}
