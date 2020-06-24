package nooblife.lockit.model;

public class Application {

    private String applicationName;
    private String applicationPackageName;
    private String applicationVersion;
    private String applicationIconEncoded;
    private boolean isSelected = false;

    public Application(String applicationName, String applicationPackageName, String applicationVersion, String applicationIconEncoded) {
        this.applicationName = applicationName;
        this.applicationPackageName = applicationPackageName;
        this.applicationVersion = applicationVersion;
        this.applicationIconEncoded = applicationIconEncoded;
    }

    public Application() {

    }

    public String getApplicationIconEncoded() {
        return applicationIconEncoded;
    }

    public void setApplicationIconEncoded(String applicationIconEncoded) {
        this.applicationIconEncoded = applicationIconEncoded;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationPackageName() {
        return applicationPackageName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
