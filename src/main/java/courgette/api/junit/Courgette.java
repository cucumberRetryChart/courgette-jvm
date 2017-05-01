package courgette.api.junit;

import courgette.api.CourgetteOptions;
import courgette.runtime.CourgetteException;
import courgette.runtime.CourgetteProperties;
import courgette.runtime.CourgetteRunner;
import courgette.runtime.CourgetteRunnerFilter;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.junit.FeatureRunner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Courgette extends Cucumber {
    private final CourgetteOptions courgetteOptions;

    public Courgette(Class clazz) throws IOException, InitializationError {
        super(clazz);
        courgetteOptions = getCourgetteOptions(clazz);
    }

    @Override
    public List<FeatureRunner> getChildren() {
        return getFilteredChildren();
    }

    @Override
    public void run(RunNotifier notifier) {
        final CourgetteProperties properties = new CourgetteProperties(courgetteOptions, createSessionId(), getMaxThreads());

        final CourgetteRunner courgetteRunner = new CourgetteRunner(getChildren(), properties);
        courgetteRunner.run();

        courgetteRunner.createReport();
        courgetteRunner.createExecutionReport();

        if (courgetteRunner.allFeaturesPassed()) {
            System.exit(0x0);
        } else {
            courgetteRunner.createRerunFile();
            System.exit(0x1);
        }
    }

    private String createSessionId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private CourgetteOptions getCourgetteOptions(Class clazz) {
        return (CourgetteOptions)
                Arrays.stream(clazz.getDeclaredAnnotations())
                        .filter(annotation -> annotation.annotationType().equals(CourgetteOptions.class))
                        .findFirst()
                        .orElseThrow(() -> new CourgetteException("Class is not annotated with @CourgetteOptions"));
    }

    private Integer getMaxThreads() {
        return courgetteOptions.threads() > getChildren().size()
                ? getChildren().size()
                : courgetteOptions.threads() < 1
                ? 1
                : courgetteOptions.threads();
    }

    private List<FeatureRunner> getFilteredChildren() {
        return CourgetteRunnerFilter.filter(super.getChildren(), courgetteOptions.cucumberOptions().tags());
    }
}