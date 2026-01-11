import java.io.Serializable;
import java.util.Objects;

public class AppSettings implements Serializable {
    private char splitter = ',';
    private boolean autoLoadEnabled = false;
    private String dateFormat = "dd.MM.yyyy";

    public char getSplitter() {
        return splitter;
    }

    public void setSplitter(char splitter) {
        this.splitter = splitter;
    }

    public boolean isAutoLoadEnabled() {
        return autoLoadEnabled;
    }

    public void setAutoLoadEnabled(boolean autoLoadEnabled) {
        this.autoLoadEnabled = autoLoadEnabled;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AppSettings that = (AppSettings) obj;
        return splitter == that.splitter &&
                autoLoadEnabled == that.autoLoadEnabled &&
                Objects.equals(dateFormat, that.dateFormat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(splitter, autoLoadEnabled, dateFormat);
    }

    @Override
    public String toString() {
        return String.format("AppSettings[splitter='%c', autoLoad=%s, dateFormat=%s]",
                splitter, autoLoadEnabled, dateFormat);
    }
}