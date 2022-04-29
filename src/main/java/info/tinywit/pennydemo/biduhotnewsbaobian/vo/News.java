package info.tinywit.pennydemo.biduhotnewsbaobian.vo;

import java.math.BigDecimal;

public class News {
    private String url;
    private String title;
    private BigDecimal index;

    public News(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getIndex() {
        return index;
    }

    public void setIndex(BigDecimal index) {
        this.index = index;
    }
}
