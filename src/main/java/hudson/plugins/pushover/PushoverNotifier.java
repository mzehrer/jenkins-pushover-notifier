package hudson.plugins.pushover;


import com.google.common.base.Strings;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 09.03.13
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
public class PushoverNotifier extends Notifier {

    private final static Logger LOG = Logger.getLogger(PushoverNotifier.class.getName());

    public final String appToken;
    public final String userToken;
    public final String device;
    public final boolean notifyOnSuccess;
    private transient PushoverApi pushoverApi;

    @DataBoundConstructor
    public PushoverNotifier(String appToken,  String userToken, String device,  boolean notifyOnSuccess) {
        this.appToken = appToken;
        this.userToken = userToken;
        this.device = device;
        this.notifyOnSuccess = notifyOnSuccess;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    private void initializePushover()
            throws IOException {
        if (pushoverApi == null) {
            LOG.info("Initializig Pushover plugin");
            if (Strings.isNullOrEmpty(this.device)) {
                pushoverApi = new PushoverApi(appToken, userToken);
            } else {
                pushoverApi = new PushoverApi(appToken, userToken, device);
            }
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        if (!build.getResult().toString().equals(Result.SUCCESS.toString()) || notifyOnSuccess) {
            initializePushover();
            String message = build.getProject().getName() + ": " + build.getResult().toString() + "\n";
            if (!build.getCulprits().isEmpty()) {
                for (User user : build.getCulprits()) {
                    message = message + "Possible Culprit: " + user.getDisplayName();
                }
            }
            LOG.info("Sending Pushover message");
            pushoverApi.sendMessage(message);
        }
        return true;
    }

    @Extension
    public static final class DescriptorImpl
            extends BuildStepDescriptor<Publisher> {
        /*
         * (non-Javadoc)
         *
         * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
         */

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /*
         * (non-Javadoc)
         *
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Pushover";
        }
    }
}